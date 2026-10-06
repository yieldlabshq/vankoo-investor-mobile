package com.liquilabs.vankoo.investor.finance.domain.model

import com.liquilabs.vankoo.investor.core.result.BusinessReason

/**
 * Every reason Finance gives by name for turning a request down.
 *
 * Finance adopted the problem+json contract with the debit endpoint (Trello #55):
 * every error now carries a kebab-case `code`, the same shape IAM and the gateway
 * use, and these are the ones it documents. Two of them share a status — both
 * [InsufficientBalance] and [IdempotencyKeyConflict] are 409 — which is exactly why
 * the app branches on the code and never on the status alone. A code this build has
 * not been taught still arrives as `AppError.Server(status)`.
 */
enum class FinanceFailure(val code: String) : BusinessReason {
    /** A malformed id, an unsupported currency, a missing `Idempotency-Key`. */
    InvalidRequest("invalid-request"),
    /** Bean Validation on the body; the field is named in `errors[]`. */
    ValidationFailed("validation-failed"),
    Unauthenticated("unauthenticated"),
    /** The token's user is not the `{accountId}` in the path. */
    Forbidden("forbidden"),
    /** No deposit has ever landed in that currency, so there is no wallet to read or debit. */
    WalletNotFound("wallet-not-found"),
    DepositNotFound("deposit-not-found"),
    /** The same `Idempotency-Key` sent again with a different body. */
    IdempotencyKeyConflict("idempotency-key-conflict"),
    /** The debit is larger than the balance. The key is released; top up and retry. */
    InsufficientBalance("insufficient-balance"),
    ;

    companion object {
        private val BY_CODE = entries.associateBy(FinanceFailure::code)

        fun fromCode(code: String): FinanceFailure? = BY_CODE[code]
    }
}
