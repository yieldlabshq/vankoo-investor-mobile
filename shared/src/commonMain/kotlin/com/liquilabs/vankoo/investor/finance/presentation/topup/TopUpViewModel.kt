package com.liquilabs.vankoo.investor.finance.presentation.topup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liquilabs.vankoo.investor.core.format.MoneyFormat
import com.liquilabs.vankoo.investor.core.result.AppResult
import com.liquilabs.vankoo.investor.iam.application.ObserveSession
import com.liquilabs.vankoo.investor.finance.application.GetWallet
import com.liquilabs.vankoo.investor.finance.application.InitiateDeposit
import com.liquilabs.vankoo.investor.finance.domain.model.Currency
import com.liquilabs.vankoo.investor.finance.domain.model.Money
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * «¿Cuánto quieres recargar?».
 *
 * The idempotency key is minted once per attempt and kept until the attempt is
 * answered: a tap that times out and is tapped again sends the same key, and Finance
 * replays the deposit it already opened instead of opening a second one. Only a 2xx
 * spends the key.
 */
@OptIn(ExperimentalUuidApi::class)
class TopUpViewModel(
    initialAmountMinor: Long?,
    private val observeSession: ObserveSession,
    private val getWallet: GetWallet,
    private val initiateDeposit: InitiateDeposit,
) : ViewModel() {

    private val _state = MutableStateFlow(
        TopUpUiState(
            amountText = initialAmountMinor?.let { MoneyFormat.format(it, symbol = "").trim() } ?: "",
        )
    )
    val state = _state.asStateFlow()

    /** The id of the deposit just opened. A Channel: navigating is an event, not a state. */
    private val initiatedEvents = Channel<String>(Channel.BUFFERED)
    val initiated = initiatedEvents.receiveAsFlow()

    private var idempotencyKey: String = Uuid.random().toString()

    init {
        viewModelScope.launch {
            val accountId = observeSession().first()?.user?.id ?: return@launch
            val result = getWallet(accountId, CURRENCY)
            if (result is AppResult.Success) {
                _state.update { it.copy(balanceMinor = result.data?.balance?.amountMinor ?: 0L) }
            }
        }
    }

    fun onAmountChange(value: String) {
        // Only what could still become an amount gets in; a letter is dropped rather
        // than shown and then flagged.
        if (value.isNotEmpty() && MoneyFormat.parseMinor(value) == null) return
        _state.update { it.copy(amountText = value, error = null) }
    }

    fun onSuggestion(amountMinor: Long) {
        _state.update { it.copy(amountText = MoneyFormat.format(amountMinor, symbol = "").trim(), error = null) }
    }

    fun onSubmit() {
        val current = _state.value
        val amountMinor = current.amountMinor ?: return
        if (!current.canSubmit) return

        viewModelScope.launch {
            val accountId = observeSession().first()?.user?.id ?: return@launch
            _state.update { it.copy(submitting = true, error = null) }

            val result = initiateDeposit(
                accountId = accountId,
                amount = Money(amountMinor, CURRENCY),
                idempotencyKey = idempotencyKey,
            )

            when (result) {
                is AppResult.Success -> {
                    idempotencyKey = Uuid.random().toString()
                    _state.update { it.copy(submitting = false) }
                    initiatedEvents.send(result.data.id)
                }

                is AppResult.Failure -> _state.update {
                    it.copy(submitting = false, error = result.error)
                }
            }
        }
    }

    companion object {
        val CURRENCY = Currency.PEN

        /** The three chips of the mockup, in minor units. */
        val SUGGESTIONS_MINOR = listOf(1_000_00L, 5_000_00L, 10_000_00L)
    }
}
