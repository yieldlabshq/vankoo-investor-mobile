package com.liquilabs.vankoo.investor.finance.application

import com.liquilabs.vankoo.investor.core.result.AppResult
import com.liquilabs.vankoo.investor.finance.domain.model.Deposit
import com.liquilabs.vankoo.investor.finance.domain.repository.DepositRepository

/** Where a top-up stands right now. Polled by the screen that is waiting on it. */
class GetDeposit(private val deposits: DepositRepository) {
    suspend operator fun invoke(depositId: String): AppResult<Deposit> = deposits.get(depositId)
}
