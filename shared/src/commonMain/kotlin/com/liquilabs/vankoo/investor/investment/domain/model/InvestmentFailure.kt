package com.liquilabs.vankoo.investor.investment.domain.model

import com.liquilabs.vankoo.investor.core.result.AppError
import com.liquilabs.vankoo.investor.core.result.BusinessReason

/**
 * Every reason this context turns an investment down by name.
 *
 * Investment itself has not adopted the problem+json contract: its
 * `GlobalExceptionHandler` answers `{"message": "..."}` in English, with no `code` to
 * branch on. So a 400, a 409 or a 403 from it arrives as `AppError.Server(status)` and
 * the screen says something generic. The coded answers these screens can meet come
 * from elsewhere: the gateway's own 401 on the routes that carry a token, and the
 * wallet's refusals, translated at the [com.liquilabs.vankoo.investor.investment.domain.repository.InvestorWallet]
 * seam so that Finance's names never travel this far.
 */
enum class InvestmentFailure(val code: String) : BusinessReason {
    Unauthenticated("unauthenticated"),

    /**
     * The wallet would not cover the amount when the debit was tried — the balance
     * the screen showed was already stale — or there is no wallet at all.
     */
    InsufficientBalance("insufficient-balance"),
    ;

    companion object {
        private val BY_CODE = entries.associateBy(InvestmentFailure::code)

        fun fromCode(code: String): InvestmentFailure? = BY_CODE[code]
    }
}

/**
 * The money left the wallet and the share was not bought.
 *
 * The one outcome of an investment that is neither success nor a clean refusal, and
 * the one the person must be told about in so many words. The debit is already
 * applied under [debitId]; what failed is the second call, for [cause]. A retry with
 * the same key replays the debit — same [debitId], nothing taken twice — and tries
 * Investment again, which is the right move when the cause was transient. When it was
 * not (the auction filled while the debit was in flight), there is no endpoint yet
 * that puts the money back: that gap is recorded in Finance's wallet contract, and
 * this type exists so the app never papers over it.
 */
data class DebitedButNotInvested(
    val debitId: String,
    val cause: AppError,
) : BusinessReason
