package com.liquilabs.vankoo.investor.investment.infrastructure

import com.liquilabs.vankoo.investor.core.result.AppResult
import com.liquilabs.vankoo.investor.core.result.map
import com.liquilabs.vankoo.investor.finance.application.GetWallet
import com.liquilabs.vankoo.investor.investment.domain.model.Currency
import com.liquilabs.vankoo.investor.investment.domain.repository.InvestorBalance
import com.liquilabs.vankoo.investor.finance.domain.model.Currency as FinanceCurrency

/**
 * The only file in this context that knows finance exists.
 *
 * An anti-corruption layer, deliberately one class wide: it translates this context's
 * `Currency` into finance's and hands back a plain number of cents. Every other file
 * in `investment` talks to [InvestorBalance] and stays unaware that a wallet, a
 * different `Money` or a `FinanceFailure` are involved.
 *
 * The two contexts are siblings and neither owns the other, so the alternative was
 * injecting `GetWallet` into a view model and letting finance's types travel through
 * the screen. That works right up until Finance renames something.
 */
class FinanceInvestorBalance(private val getWallet: GetWallet) : InvestorBalance {

    override suspend fun of(accountId: String, currency: Currency): AppResult<Long?> =
        getWallet(accountId, currency.toFinance()).map { wallet -> wallet?.balance?.amountMinor }

    private fun Currency.toFinance(): FinanceCurrency = when (this) {
        Currency.PEN -> FinanceCurrency.PEN
        Currency.USD -> FinanceCurrency.USD
    }
}
