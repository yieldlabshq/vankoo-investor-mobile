package com.liquilabs.vankoo.investor.investment.presentation.confirm

import com.liquilabs.vankoo.investor.core.format.MoneyFormat
import com.liquilabs.vankoo.investor.core.result.AppError
import com.liquilabs.vankoo.investor.investment.domain.model.AuctionDetail
import com.liquilabs.vankoo.investor.investment.domain.model.DebitedButNotInvested
import com.liquilabs.vankoo.investor.investment.domain.model.Investment

/**
 * What «¿Cuánto quieres invertir?» knows.
 *
 * The three frames of the mockup — the form, «Saldo insuficiente» and «Hecho» — are
 * one screen and one state here, because they are the same question at three moments
 * and two of them differ by a single number. [done] non-null is the third.
 */
data class ConfirmInvestmentUiState(
    val loaded: Boolean = false,
    val auction: AuctionDetail? = null,
    /** Cents in the wallet. Null while unread, or if the wallet has never been opened. */
    val balanceMinor: Long? = null,
    /** What is in the field, as typed. Parsed on demand, never reformatted under the finger. */
    val amountText: String = "",
    val submitting: Boolean = false,
    /** The partition the auction granted. Non-null once it is bought. */
    val done: Investment? = null,
    val error: AppError? = null,
    /**
     * A debit that was applied for the amount in the field and never became a share.
     * Kept until a retry turns it into one: while it is here the amount cannot change,
     * because a new amount would be a new debit on top of this one.
     */
    val pendingDebit: DebitedButNotInvested? = null,
) {
    val awaitsRetryOfDebitedAmount: Boolean
        get() = pendingDebit != null

    val amountMinor: Long?
        get() = MoneyFormat.parseMinor(amountText).takeIf { it != null && it > 0L }

    /**
     * The most that can go in: what is left to fund.
     *
     * `Auction.addInvestment` is the one that enforces it — the app only spares the
     * person a rejection it can see coming.
     */
    val maxInvestableMinor: Long?
        get() = auction?.remaining?.amountMinor?.takeIf { it > 0L }

    /**
     * Cents missing from the wallet for the amount typed, or null when it is covered.
     *
     * Only asked once the amount is one the auction would take. Topping up cannot fix
     * an amount that is past what is left to fund, and offering it would send someone
     * to Stripe to solve the wrong problem.
     */
    val shortfallMinor: Long?
        get() {
            if (belowMinimum || aboveAuction) return null
            val amount = amountMinor ?: return null
            val balance = balanceMinor ?: return null
            return (amount - balance).takeIf { it > 0L }
        }

    /** The largest amount that could be confirmed right now: the lesser of wallet and auction. */
    val affordableMinor: Long?
        get() {
            val balance = balanceMinor?.takeIf { it > 0L } ?: return null
            val max = maxInvestableMinor ?: return balance
            return minOf(balance, max)
        }

    val belowMinimum: Boolean
        get() = amountMinor?.let { it < MINIMUM_INVESTMENT_MINOR } == true

    val aboveAuction: Boolean
        get() {
            val amount = amountMinor ?: return false
            val max = maxInvestableMinor ?: return false
            return amount > max
        }

    /** Whether the figures under the divider mean anything: a share over 100 % does not. */
    val showsProjection: Boolean
        get() = amountMinor != null && !aboveAuction

    val canSubmit: Boolean
        get() = amountMinor != null &&
            !submitting &&
            done == null &&
            shortfallMinor == null &&
            !belowMinimum &&
            !aboveAuction

    companion object {
        /**
         * The floor, in cents, mirroring `INVESTMENT_MINIMUM_PEN` in the service.
         *
         * Hardcoded because nothing exposes it: the marketplace and the details
         * resource both leave it out, so the only way to learn it is to be told no.
         * Duplicated here so the field can say it up front, and it will drift the day
         * someone changes the server's value — which is a worse failure than it looks,
         * since the app would be promising a floor the auction does not honour. The
         * mockups say S/ 500; the service says S/ 50.
         */
        const val MINIMUM_INVESTMENT_MINOR = 50_00L
    }
}
