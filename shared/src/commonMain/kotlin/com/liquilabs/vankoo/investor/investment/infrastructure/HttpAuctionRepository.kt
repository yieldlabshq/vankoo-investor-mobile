package com.liquilabs.vankoo.investor.investment.infrastructure

import com.liquilabs.vankoo.investor.core.result.AppResult
import com.liquilabs.vankoo.investor.core.result.map
import com.liquilabs.vankoo.investor.investment.domain.model.AuctionDetail
import com.liquilabs.vankoo.investor.investment.domain.model.Investment
import com.liquilabs.vankoo.investor.investment.domain.model.MarketplaceFilter
import com.liquilabs.vankoo.investor.investment.domain.model.MarketplacePage
import com.liquilabs.vankoo.investor.investment.domain.model.Money
import com.liquilabs.vankoo.investor.investment.domain.repository.AuctionRepository
import com.liquilabs.vankoo.investor.investment.infrastructure.dto.CreateInvestmentRequestDto

/**
 * Auctions over HTTP. It turns a filter into query parameters, and the mappers do the
 * rest.
 *
 * `currency` is left unset on purpose: the app holds one wallet in soles, but an
 * auction priced in dollars is still worth seeing, and the day a screen offers to
 * switch currencies this is where that choice arrives.
 */
class HttpAuctionRepository(private val api: InvestmentApi) : AuctionRepository {

    override suspend fun getMarketplace(
        filter: MarketplaceFilter,
        page: Int,
        size: Int,
    ): AppResult<MarketplacePage> =
        api.getMarketplace(
            page = page,
            size = size,
            sort = filter.sort,
            greenCertified = filter.greenCertified,
            currency = null,
        ).map { it.toDomain() }

    override suspend fun getAuction(auctionId: String): AppResult<AuctionDetail> =
        api.getAuction(auctionId).map { it.toDomain() }

    override suspend fun invest(
        auctionId: String,
        investorId: String,
        amount: Money,
        transactionId: String,
    ): AppResult<Investment> =
        api.createInvestment(
            auctionId = auctionId,
            request = CreateInvestmentRequestDto(
                investorId = investorId,
                amount = amount.amountMinor,
                currency = amount.currency.isoCode,
                transactionId = transactionId,
            ),
        ).map { it.toDomain() }
}
