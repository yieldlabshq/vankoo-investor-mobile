package com.liquilabs.vankoo.investor.investment.application

import com.liquilabs.vankoo.investor.core.result.AppResult
import com.liquilabs.vankoo.investor.investment.domain.model.MarketplaceFilter
import com.liquilabs.vankoo.investor.investment.domain.model.MarketplacePage
import com.liquilabs.vankoo.investor.investment.domain.repository.AuctionRepository

/** One page of the auctions open for investment. */
class GetMarketplace(private val auctions: AuctionRepository) {
    suspend operator fun invoke(
        filter: MarketplaceFilter,
        page: Int = 0,
        size: Int = DEFAULT_PAGE_SIZE,
    ): AppResult<MarketplacePage> = auctions.getMarketplace(filter, page, size)

    companion object {
        /**
         * Ten rows. The service allows up to a hundred, but each card is a fifth of a
         * screen: a first page nobody scrolls to the end of only costs time to arrive.
         */
        const val DEFAULT_PAGE_SIZE = 10
    }
}
