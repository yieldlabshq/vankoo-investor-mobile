package com.liquilabs.vankoo.investor.investment.infrastructure

import com.liquilabs.vankoo.investor.core.result.AppError
import com.liquilabs.vankoo.investor.core.result.AppResult
import com.liquilabs.vankoo.investor.finance.application.DebitWallet
import com.liquilabs.vankoo.investor.finance.domain.model.DebitReason
import com.liquilabs.vankoo.investor.finance.domain.model.FinanceFailure
import com.liquilabs.vankoo.investor.investment.domain.model.Currency
import com.liquilabs.vankoo.investor.investment.domain.model.InvestmentFailure
import com.liquilabs.vankoo.investor.investment.domain.model.Money
import com.liquilabs.vankoo.investor.investment.domain.repository.InvestorWallet
import com.liquilabs.vankoo.investor.finance.domain.model.Currency as FinanceCurrency
import com.liquilabs.vankoo.investor.finance.domain.model.Money as FinanceMoney

/**
 * The second of the two files in this context that know finance exists — the first
 * is [FinanceInvestorBalance], and the reasoning is the same.
 *
 * This one translates in both directions: this context's `Money` into finance's on
 * the way out, and finance's failures into this context's on the way back. A
 * `FinanceFailure` must not reach the confirm screen, which only knows how to word an
 * [InvestmentFailure]; the two it can meet by name are mapped, and anything else
 * Finance refuses by code is a request this app built wrong, which is what
 * `AppError.Unexpected` means.
 *
 * The reason is always `INVERSION`: that is the only thing this context takes money
 * out for, and the wallet's history labels the row from it.
 */
class FinanceInvestorWallet(private val debitWallet: DebitWallet) : InvestorWallet {

    override suspend fun debit(accountId: String, amount: Money, idempotencyKey: String): AppResult<String> =
        when (val result = debitWallet(accountId, amount.toFinance(), DebitReason.INVERSION, idempotencyKey)) {
            is AppResult.Success -> AppResult.Success(result.data.id)
            is AppResult.Failure -> AppResult.Failure(result.error.toInvestment())
        }

    private fun Money.toFinance(): FinanceMoney = FinanceMoney(
        amountMinor = amountMinor,
        currency = when (currency) {
            Currency.PEN -> FinanceCurrency.PEN
            Currency.USD -> FinanceCurrency.USD
        },
    )

    private fun AppError.toInvestment(): AppError = when (this) {
        is AppError.Business -> when (reason) {
            // No wallet is no money: the confirm screen treats the two the same.
            FinanceFailure.InsufficientBalance,
            FinanceFailure.WalletNotFound,
            -> AppError.Business(InvestmentFailure.InsufficientBalance)
            FinanceFailure.Unauthenticated,
            FinanceFailure.Forbidden,
            -> AppError.Business(InvestmentFailure.Unauthenticated)
            else -> AppError.Unexpected()
        }
        else -> this
    }
}
