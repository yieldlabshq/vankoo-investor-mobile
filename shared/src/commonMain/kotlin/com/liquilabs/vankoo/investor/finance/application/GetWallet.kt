package com.liquilabs.vankoo.investor.finance.application

import com.liquilabs.vankoo.investor.core.result.AppResult
import com.liquilabs.vankoo.investor.finance.domain.model.Currency
import com.liquilabs.vankoo.investor.finance.domain.model.Wallet
import com.liquilabs.vankoo.investor.finance.domain.repository.WalletRepository

/** The balance in one currency, or null while the wallet has not been opened. */
class GetWallet(private val wallets: WalletRepository) {
    suspend operator fun invoke(accountId: String, currency: Currency): AppResult<Wallet?> =
        wallets.getWallet(accountId, currency)
}
