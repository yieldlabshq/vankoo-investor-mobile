package com.liquilabs.vankoo.investor.finance.application

import com.liquilabs.vankoo.investor.core.result.AppResult
import com.liquilabs.vankoo.investor.finance.domain.model.Deposit
import com.liquilabs.vankoo.investor.finance.domain.model.Money
import com.liquilabs.vankoo.investor.finance.domain.repository.DepositRepository

/**
 * Starts a top-up.
 *
 * The idempotency key is the caller's: a screen that retries after a dropped
 * connection must send the same one, and only the screen knows it is retrying.
 */
class InitiateDeposit(private val deposits: DepositRepository) {
    suspend operator fun invoke(
        accountId: String,
        amount: Money,
        idempotencyKey: String,
    ): AppResult<Deposit> = deposits.initiate(accountId, amount, idempotencyKey)
}
