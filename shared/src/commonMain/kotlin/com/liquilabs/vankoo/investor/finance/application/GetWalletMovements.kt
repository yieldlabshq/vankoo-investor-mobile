package com.liquilabs.vankoo.investor.finance.application

import com.liquilabs.vankoo.investor.core.result.AppResult
import com.liquilabs.vankoo.investor.finance.domain.model.Currency
import com.liquilabs.vankoo.investor.finance.domain.model.MovementPage
import com.liquilabs.vankoo.investor.finance.domain.repository.WalletRepository

/** One page of the wallet's history. */
class GetWalletMovements(private val wallets: WalletRepository) {
    suspend operator fun invoke(
        accountId: String,
        currency: Currency,
        page: Int,
        size: Int = DEFAULT_PAGE_SIZE,
    ): AppResult<MovementPage> = wallets.getMovements(accountId, currency, page, size)

    companion object {
        /** The mockup shows four rows and a "see more"; five is one past the fold. */
        const val DEFAULT_PAGE_SIZE = 5
    }
}
