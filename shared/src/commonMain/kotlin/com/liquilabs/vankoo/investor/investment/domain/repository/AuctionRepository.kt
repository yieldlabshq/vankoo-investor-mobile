package com.liquilabs.vankoo.investor.investment.domain.repository

import com.liquilabs.vankoo.investor.core.result.AppResult
import com.liquilabs.vankoo.investor.investment.domain.model.AuctionDetail
import com.liquilabs.vankoo.investor.investment.domain.model.Investment
import com.liquilabs.vankoo.investor.investment.domain.model.MarketplaceFilter
import com.liquilabs.vankoo.investor.investment.domain.model.MarketplacePage
import com.liquilabs.vankoo.investor.investment.domain.model.Money

interface AuctionRepository {
    /**
     * A page of auctions open for investment.
     *
     * An empty market is a 200 with no rows, never a 404: the endpoint answers for
     * the whole marketplace and not for a resource that may be missing.
     */
    suspend fun getMarketplace(
        filter: MarketplaceFilter,
        page: Int,
        size: Int,
    ): AppResult<MarketplacePage>

    /** One auction in full. Fails with `AppError.Server(404)` once the id stops resolving. */
    suspend fun getAuction(auctionId: String): AppResult<AuctionDetail>

    /**
     * Buys a share of an auction.
     *
     * [transactionId] is both the caller's reference and the idempotency key: sending
     * the same one twice replays the partition already bought rather than buying a
     * second, so a retry has to reuse it.
     */
    suspend fun invest(
        auctionId: String,
        investorId: String,
        amount: Money,
        transactionId: String,
    ): AppResult<Investment>
}
