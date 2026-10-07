package com.liquilabs.vankoo.investor.finance.infrastructure

import com.liquilabs.vankoo.investor.core.result.AppResult
import com.liquilabs.vankoo.investor.core.result.map
import com.liquilabs.vankoo.investor.finance.domain.model.Deposit
import com.liquilabs.vankoo.investor.finance.domain.model.Money
import com.liquilabs.vankoo.investor.finance.domain.repository.DepositRepository
import com.liquilabs.vankoo.investor.finance.infrastructure.dto.CreateDepositRequestDto

/** Deposits over HTTP. */
class HttpDepositRepository(private val api: FinanceApi) : DepositRepository {

    override suspend fun initiate(
        accountId: String,
        amount: Money,
        idempotencyKey: String,
    ): AppResult<Deposit> =
        api.createDeposit(
            request = CreateDepositRequestDto(
                accountId = accountId,
                amountMinor = amount.amountMinor,
                currency = amount.currency.isoCode,
                // The only provider Finance has, and the only one the mockups show.
                provider = PROVIDER_STRIPE,
            ),
            idempotencyKey = idempotencyKey,
        ).map { it.toDomain() }

    override suspend fun get(depositId: String): AppResult<Deposit> =
        api.getDeposit(depositId).map { it.toDomain() }

    private companion object {
        /** Finance's `Provider` enum, by name. */
        const val PROVIDER_STRIPE = "STRIPE"
    }
}
