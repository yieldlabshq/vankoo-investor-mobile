package com.liquilabs.vankoo.investor.finance.infrastructure

import com.liquilabs.vankoo.investor.core.result.AppResult
import com.liquilabs.vankoo.investor.core.result.map
import com.liquilabs.vankoo.investor.finance.domain.model.Currency
import com.liquilabs.vankoo.investor.finance.domain.model.DebitReason
import com.liquilabs.vankoo.investor.finance.domain.model.Money
import com.liquilabs.vankoo.investor.finance.domain.model.MovementPage
import com.liquilabs.vankoo.investor.finance.domain.model.Wallet
import com.liquilabs.vankoo.investor.finance.domain.model.WalletDebit
import com.liquilabs.vankoo.investor.finance.domain.repository.WalletRepository
import com.liquilabs.vankoo.investor.finance.infrastructure.dto.CreateWalletDebitRequestDto

/** The wallet over HTTP. It builds the request, and the mappers do the rest. */
class HttpWalletRepository(private val api: FinanceApi) : WalletRepository {

    override suspend fun getWallet(accountId: String, currency: Currency): AppResult<Wallet?> =
        api.getWallet(accountId, currency.isoCode).map { it?.toDomain() }

    override suspend fun getMovements(
        accountId: String,
        currency: Currency,
        page: Int,
        size: Int,
    ): AppResult<MovementPage> =
        api.getMovements(accountId, currency.isoCode, page, size).map { it.toDomain() }

    override suspend fun debit(
        accountId: String,
        amount: Money,
        reason: DebitReason,
        idempotencyKey: String,
    ): AppResult<WalletDebit> =
        api.debitWallet(
            accountId = accountId,
            currency = amount.currency.isoCode,
            request = CreateWalletDebitRequestDto(amountMinor = amount.amountMinor, reason = reason.name),
            idempotencyKey = idempotencyKey,
        ).map { it.toDomain() }
}
