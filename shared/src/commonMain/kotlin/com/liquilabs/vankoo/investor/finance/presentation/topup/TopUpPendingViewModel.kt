package com.liquilabs.vankoo.investor.finance.presentation.topup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liquilabs.vankoo.investor.core.platform.UrlOpener
import com.liquilabs.vankoo.investor.core.result.AppError
import com.liquilabs.vankoo.investor.core.result.AppResult
import com.liquilabs.vankoo.investor.iam.application.ObserveSession
import com.liquilabs.vankoo.investor.finance.application.GetDeposit
import com.liquilabs.vankoo.investor.finance.application.GetWallet
import com.liquilabs.vankoo.investor.finance.domain.model.Currency
import com.liquilabs.vankoo.investor.finance.domain.model.Deposit
import com.liquilabs.vankoo.investor.finance.domain.model.DepositStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** Where the person is in the payment, as far as this screen can tell. */
enum class TopUpPhase {
    /** Finance has the deposit but not yet Stripe's page for it. */
    PREPARING,

    /** Stripe's page is up, over this screen. */
    AT_PROVIDER,

    /** They came back without paying: Stripe's back arrow, the sheet's X, or the sheet never opened. */
    RETURNED_UNPAID,

    /** Stripe said the payment went through; the webhook to Finance is what is left. */
    CONFIRMING,
}

/** What the waiting screen knows. */
data class TopUpPendingUiState(
    val phase: TopUpPhase = TopUpPhase.PREPARING,
    val deposit: Deposit? = null,
    val balanceMinor: Long? = null,
    /** The sheet could not be opened: nothing on the device handles the URL. */
    val couldNotOpenBrowser: Boolean = false,
    val error: AppError? = null,
) {
    val canPay: Boolean
        get() = deposit?.canProceedToProvider == true
}

/** How the wait ended. */
sealed interface TopUpOutcome {
    data object Completed : TopUpOutcome
    data class Failed(val depositId: String) : TopUpOutcome
}

/**
 * Waits on one deposit: first for Stripe's page to exist, then for the bank's answer.
 *
 * It polls rather than listens because Finance has no push channel to a phone, and
 * the two things it waits for both arrive as changes to the projection: the action
 * URL a beat after the 202, and the terminal status after the webhook. Polling runs
 * only while the screen is started — the screen says when — so the app in the
 * background, or under Stripe's sheet, does not keep asking.
 *
 * The page opens by itself the first time the URL shows up: the person already said
 * how much and pressed «Recargar», and a button that only says "go on then" is a tap
 * with nothing behind it. From then on the phase tells the screen what to say, and
 * the [ProviderReturnSignal] tells this class which way they came back — Stripe only
 * redirects on its two exits, so coming back with no signal means the sheet was
 * simply closed.
 */
class TopUpPendingViewModel(
    private val depositId: String,
    private val observeSession: ObserveSession,
    private val getDeposit: GetDeposit,
    private val getWallet: GetWallet,
    private val urlOpener: UrlOpener,
    private val providerReturns: ProviderReturnSignal,
) : ViewModel() {

    private val _state = MutableStateFlow(TopUpPendingUiState())
    val state = _state.asStateFlow()

    private val outcomeEvents = Channel<TopUpOutcome>(Channel.BUFFERED)
    val outcome = outcomeEvents.receiveAsFlow()

    private var polling: Job? = null

    /** Once. Coming back to this screen must not throw the person at Stripe again. */
    private var openedAutomatically = false

    init {
        // A return that arrived before this deposit existed cannot be about it.
        providerReturns.consume()

        viewModelScope.launch {
            val accountId = observeSession().first()?.user?.id ?: return@launch
            val result = getWallet(accountId, CURRENCY)
            if (result is AppResult.Success) {
                _state.update { it.copy(balanceMinor = result.data?.balance?.amountMinor ?: 0L) }
            }
        }
    }

    /**
     * Called every time the screen starts — the first time, and each return from
     * Stripe's sheet. The return is judged here, before the first poll, because the
     * signal is already waiting by then (see [ProviderReturnSignal]). It is judged
     * again on every poll: Android does not always stop the activity under a
     * Custom Tab, and when it only pauses it there is no new start to hang this on.
     */
    fun startPolling() {
        if (polling?.isActive == true) return
        settleReturn(screenRestarted = true)
        polling = viewModelScope.launch {
            while (isActive) {
                settleReturn(screenRestarted = false)
                when (val result = getDeposit(depositId)) {
                    is AppResult.Success -> {
                        val deposit = result.data
                        _state.update { it.copy(deposit = deposit, error = null) }
                        when (deposit.status) {
                            DepositStatus.SUCCEEDED -> {
                                outcomeEvents.send(TopUpOutcome.Completed)
                                return@launch
                            }

                            DepositStatus.FAILED, DepositStatus.CANCELLED -> {
                                outcomeEvents.send(TopUpOutcome.Failed(depositId))
                                return@launch
                            }

                            else -> openAutomaticallyIfReady(deposit)
                        }
                    }

                    is AppResult.Failure -> _state.update { it.copy(error = result.error) }
                }
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    fun stopPolling() {
        polling?.cancel()
        polling = null
    }

    /** «Volver al pago»: the same Stripe session, opened again by hand. */
    fun onPay() {
        val url = _state.value.deposit?.actionUrl ?: return
        open(url)
    }

    /**
     * Reads Stripe's return, if there is one, into the phase.
     *
     * A signal only means something once the person has been sent to Stripe: while
     * still PREPARING nothing was opened, so whatever is waiting is a leftover from
     * an earlier top-up and is dropped. "No signal" is itself an answer, but only on
     * a restart of the screen and only while AT_PROVIDER — that is the sheet closed
     * with its own X, the one exit Stripe does not redirect from.
     */
    private fun settleReturn(screenRestarted: Boolean) {
        val phase = _state.value.phase
        val signal = providerReturns.consume()
        val next = when {
            phase == TopUpPhase.PREPARING -> phase
            signal == ProviderReturn.SUCCESS -> TopUpPhase.CONFIRMING
            signal == ProviderReturn.CANCEL -> TopUpPhase.RETURNED_UNPAID
            screenRestarted && phase == TopUpPhase.AT_PROVIDER -> TopUpPhase.RETURNED_UNPAID
            else -> phase
        }
        if (next != phase) _state.update { it.copy(phase = next) }
    }

    private fun openAutomaticallyIfReady(deposit: Deposit) {
        if (openedAutomatically || _state.value.phase != TopUpPhase.PREPARING) return
        val url = deposit.actionUrl ?: return
        openedAutomatically = true
        open(url)
    }

    private fun open(url: String) {
        val opened = urlOpener.open(url)
        _state.update {
            it.copy(
                phase = if (opened) TopUpPhase.AT_PROVIDER else TopUpPhase.RETURNED_UNPAID,
                couldNotOpenBrowser = !opened,
            )
        }
    }

    companion object {
        val CURRENCY = Currency.PEN

        /** Fast enough that the URL feels immediate; slow enough not to hammer Finance. */
        const val POLL_INTERVAL_MS = 2_000L
    }
}
