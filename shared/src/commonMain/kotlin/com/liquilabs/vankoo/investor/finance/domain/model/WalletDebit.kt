package com.liquilabs.vankoo.investor.finance.domain.model

/**
 * A debit Finance has applied, as the app reads it back from the 201.
 *
 * [id] is the movement's reference — the same value the wallet's history shows on
 * that row — and the one thing the investment flow needs from here: it becomes the
 * `transactionId` Investment deduplicates on, so a retry that replays the debit also
 * replays the share instead of buying a second one.
 */
data class WalletDebit(
    val id: String,
    val walletId: String,
    val accountId: String,
    val amount: Money,
    val reason: DebitReason,
)

/** Why a wallet is being debited — Finance's `WalletMovementType`, on the write side. */
enum class DebitReason {
    INVERSION,
    RETIRO,
    COMISION,
    /** A reason this build has not been taught. */
    UNKNOWN,
}
