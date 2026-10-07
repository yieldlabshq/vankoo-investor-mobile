package com.liquilabs.vankoo.investor.investment.presentation.confirm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liquilabs.vankoo.investor.core.format.MoneyFormat
import com.liquilabs.vankoo.investor.core.result.AppError
import com.liquilabs.vankoo.investor.core.result.AppResult
import com.liquilabs.vankoo.investor.iam.application.ObserveSession
import com.liquilabs.vankoo.investor.investment.application.GetAuction
import com.liquilabs.vankoo.investor.investment.application.InvestInAuction
import com.liquilabs.vankoo.investor.investment.domain.model.Currency
import com.liquilabs.vankoo.investor.investment.domain.model.DebitedButNotInvested
import com.liquilabs.vankoo.investor.investment.domain.model.Money
import com.liquilabs.vankoo.investor.investment.domain.repository.InvestorBalance
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * «¿Cuánto quieres invertir?».
 *
 * It reads the auction again rather than trusting what the detail screen held: the
 * number that matters here is what is left to fund, and that is exactly what other
 * people are changing while this screen is open. The wallet is read beside it, in
 * parallel, because neither answer depends on the other.
 *
 * Confirming costs money now: [InvestInAuction] debits the wallet and then buys the
 * share, and what this screen holds across attempts is the debit's Idempotency-Key —
 * the same bargain the top-up makes with its own. It is minted once per attempt and
 * only spent on success: the same key replays the same debit, whose reference is
 * what Investment deduplicates the share on, so a tap that timed out anywhere can be
 * tapped again without paying or buying twice.
 *
 * A new amount is a new attempt and gets a new key — except after
 * [DebitedButNotInvested], when the money for the old amount has already left the
 * wallet and only a retry with that same amount can turn it into a share. The field
 * is frozen then, and the notice says why.
 */
@OptIn(ExperimentalUuidApi::class)
class ConfirmInvestmentViewModel(
    private val auctionId: String,
    private val observeSession: ObserveSession,
    private val getAuction: GetAuction,
    private val investorBalance: InvestorBalance,
    private val investInAuction: InvestInAuction,
) : ViewModel() {

    private val _state = MutableStateFlow(ConfirmInvestmentUiState())
    val state = _state.asStateFlow()

    private var idempotencyKey: String = Uuid.random().toString()

    fun load() {
        viewModelScope.launch {
            val accountId = observeSession().first()?.user?.id ?: return@launch

            val (auction, balance) = coroutineScope {
                val auctionResult = async { getAuction(auctionId) }
                val balanceResult = async { investorBalance.of(accountId, CURRENCY) }
                auctionResult.await() to balanceResult.await()
            }

            _state.update { current ->
                current.copy(
                    loaded = true,
                    auction = (auction as? AppResult.Success)?.data ?: current.auction,
                    // A wallet that has never been opened is 404 and arrives as a null
                    // balance, which is zero to spend — not an unknown.
                    balanceMinor = (balance as? AppResult.Success)?.data ?: 0L,
                    error = (auction as? AppResult.Failure)?.error ?: current.error,
                )
            }
        }
    }

    fun onAmountChange(value: String) {
        // Only what could still become an amount gets in; a letter is dropped rather
        // than shown and then flagged.
        if (value.isNotEmpty() && MoneyFormat.parseMinor(value) == null) return
        // The amount is spoken for: see the class comment.
        if (_state.value.awaitsRetryOfDebitedAmount) return
        // A different amount under the same key would be refused as a conflict, and
        // rightly: it is a different request.
        idempotencyKey = Uuid.random().toString()
        _state.update { it.copy(amountText = value, error = null) }
    }

    /** «Invertir S/ 3,200 en su lugar»: drops the amount to what can actually be paid. */
    fun useAffordableAmount() {
        val affordable = _state.value.affordableMinor ?: return
        onAmountChange(MoneyFormat.format(affordable, symbol = "").trim())
    }

    fun onSubmit() {
        val current = _state.value
        val amountMinor = current.amountMinor ?: return
        if (!current.canSubmit) return

        viewModelScope.launch {
            val investorId = observeSession().first()?.user?.id ?: return@launch
            _state.update { it.copy(submitting = true, error = null) }

            val result = investInAuction(
                auctionId = auctionId,
                investorId = investorId,
                amount = Money(amountMinor, CURRENCY),
                idempotencyKey = idempotencyKey,
            )

            when (result) {
                is AppResult.Success -> {
                    idempotencyKey = Uuid.random().toString()
                    _state.update { it.copy(submitting = false, done = result.data, pendingDebit = null) }
                }

                is AppResult.Failure -> _state.update {
                    // Remembered beyond this one error: a later failure of another
                    // kind must not unfreeze the amount while the money is still out.
                    val debited = (result.error as? AppError.Business)?.reason as? DebitedButNotInvested
                    it.copy(
                        submitting = false,
                        error = result.error,
                        pendingDebit = debited ?: it.pendingDebit,
                    )
                }
            }
        }
    }

    companion object {
        /** Soles only, like the wallet. Nothing in the app holds a second currency yet. */
        val CURRENCY = Currency.PEN
    }
}
