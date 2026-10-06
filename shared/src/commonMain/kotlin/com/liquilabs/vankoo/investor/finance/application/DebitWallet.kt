package com.liquilabs.vankoo.investor.finance.application

import com.liquilabs.vankoo.investor.core.result.AppResult
import com.liquilabs.vankoo.investor.finance.domain.model.DebitReason
import com.liquilabs.vankoo.investor.finance.domain.model.Money
import com.liquilabs.vankoo.investor.finance.domain.model.WalletDebit
import com.liquilabs.vankoo.investor.finance.domain.repository.WalletRepository

/**
 * Takes money out of the wallet.
 *
 * The idempotency key is the caller's, like [InitiateDeposit]'s: whoever retries
 * after a dropped connection must send the same one, and only they know they are
 * retrying. Today the only caller is the investment context, through its own port —
 * nothing in finance's screens debits.
 */
class DebitWallet(private val wallets: WalletRepository) {
    suspend operator fun invoke(
        accountId: String,
        amount: Money,
        reason: DebitReason,
        idempotencyKey: String,
    ): AppResult<WalletDebit> = wallets.debit(accountId, amount, reason, idempotencyKey)
}
