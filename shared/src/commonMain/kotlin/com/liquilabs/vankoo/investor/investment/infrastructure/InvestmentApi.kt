package com.liquilabs.vankoo.investor.investment.infrastructure

import com.liquilabs.vankoo.investor.core.network.safeApiCall
import com.liquilabs.vankoo.investor.core.result.AppResult
import com.liquilabs.vankoo.investor.investment.domain.model.InvestmentFailure
import com.liquilabs.vankoo.investor.investment.infrastructure.dto.AuctionDetailsDto
import com.liquilabs.vankoo.investor.investment.infrastructure.dto.CreateInvestmentRequestDto
import com.liquilabs.vankoo.investor.investment.infrastructure.dto.InvestmentResponseDto
import com.liquilabs.vankoo.investor.investment.infrastructure.dto.MarketplacePageDto
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody

/**
 * The Investment service, as this app reaches it.
 *
 * The paths are relative, like [com.liquilabs.vankoo.investor.iam.infrastructure.IamApi]'s
 * and unlike Finance's: the gateway has routed `/investment/…` since 15 September
 * 2026, so there is no service address to configure and no provisional base URL to
 * unpick later. Both auction reads go through the public route, which is why neither
 * needs the token — the client attaches it anyway, and the service ignores it.
 */
class InvestmentApi(private val client: HttpClient) {

    /**
     * A page of the marketplace.
     *
     * Every parameter is left off when it is null so the service's own defaults
     * apply: sending `status=` empty or `greenCertified=null` would be a filter, and
     * a different one from asking for nothing.
     */
    suspend fun getMarketplace(
        page: Int,
        size: Int,
        sort: String,
        greenCertified: Boolean?,
        currency: String?,
    ): AppResult<MarketplacePageDto> =
        safeApiCall(InvestmentFailure::fromCode) {
            client.get(MARKETPLACE_PATH) {
                parameter("page", page)
                parameter("size", size)
                parameter("sort", sort)
                greenCertified?.let { parameter("greenCertified", it) }
                currency?.let { parameter("currency", it) }
            }
        }

    /** One auction in full. 404 once the id stops resolving. */
    suspend fun getAuction(auctionId: String): AppResult<AuctionDetailsDto> =
        safeApiCall(InvestmentFailure::fromCode) {
            client.get("$AUCTIONS_PATH/$auctionId")
        }

    /**
     * Buys a share. 201 with the partition, or 400/409 with `{"message": ...}` when
     * the auction turns it down — below the minimum, past what is left to fund, or an
     * auction that stopped accepting money while the screen was open.
     */
    suspend fun createInvestment(
        auctionId: String,
        request: CreateInvestmentRequestDto,
    ): AppResult<InvestmentResponseDto> =
        safeApiCall(InvestmentFailure::fromCode) {
            client.post("$AUCTIONS_PATH/$auctionId/investments") { setBody(request) }
        }

    private companion object {
        const val AUCTIONS_PATH = "/investment/api/v1/auctions"
        const val MARKETPLACE_PATH = "$AUCTIONS_PATH/marketplace"
    }
}
