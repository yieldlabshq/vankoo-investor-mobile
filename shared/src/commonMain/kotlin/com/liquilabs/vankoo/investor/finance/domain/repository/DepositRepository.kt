package com.liquilabs.vankoo.investor.finance.domain.repository

import com.liquilabs.vankoo.investor.core.result.AppResult
import com.liquilabs.vankoo.investor.finance.domain.model.Deposit
import com.liquilabs.vankoo.investor.finance.domain.model.Money

interface DepositRepository {
    /**
     * Asks Finance to start a top-up.
     *
     * Answers 202 with the deposit in PENDING and no action URL yet: the charge is
     * created afterwards. [idempotencyKey] is ours to generate and to keep for a
     * retry — the same key replays the same deposit instead of opening a second one.
     */
    suspend fun initiate(
        accountId: String,
        amount: Money,
        idempotencyKey: String,
    ): AppResult<Deposit>

    /** The deposit's projected state right now. */
    suspend fun get(depositId: String): AppResult<Deposit>
}
