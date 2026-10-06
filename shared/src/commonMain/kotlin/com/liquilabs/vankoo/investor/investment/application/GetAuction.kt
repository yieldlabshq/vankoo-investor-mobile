package com.liquilabs.vankoo.investor.investment.application

import com.liquilabs.vankoo.investor.core.result.AppResult
import com.liquilabs.vankoo.investor.investment.domain.model.AuctionDetail
import com.liquilabs.vankoo.investor.investment.domain.repository.AuctionRepository

/** One auction in full, as the detail screen shows it. */
class GetAuction(private val auctions: AuctionRepository) {
    suspend operator fun invoke(auctionId: String): AppResult<AuctionDetail> =
        auctions.getAuction(auctionId)
}
