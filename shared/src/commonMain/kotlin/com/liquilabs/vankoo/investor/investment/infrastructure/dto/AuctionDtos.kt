package com.liquilabs.vankoo.investor.investment.infrastructure.dto

import kotlinx.serialization.Serializable

/**
 * `GET /api/v1/auctions/marketplace`, mirroring `MarketplacePageResource`.
 *
 * Spring's own page shape is not what comes back — the service wraps it in a record of
 * its own — so the names here are its names and not `content`/`pageable`/`number`.
 */
@Serializable
data class MarketplacePageDto(
    val content: List<MarketplaceAuctionDto>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val sort: String,
)

/**
 * One row, mirroring `AuctionMarketplaceView`.
 *
 * `investorTeaPct` and `investorTermRatePct` arrive in percentage points already: the
 * resource multiplies the stored fraction by 100 before sending it. They are null on
 * an auction the projection has not priced yet.
 */
@Serializable
data class MarketplaceAuctionDto(
    val auctionId: String,
    val invoiceId: String,
    val mypeId: String,
    val payerRuc: String,
    val payerName: String,
    @Serializable(with = DecimalAsMinorUnits::class) val targetAmount: Long,
    @Serializable(with = DecimalAsMinorUnits::class) val currentFunding: Long,
    @Serializable(with = DecimalAsMinorUnits::class) val availableAmount: Long,
    val progressPct: Double,
    val currency: String,
    val investorTeaPct: Double? = null,
    val investorTermRatePct: Double? = null,
    val quotedTermDays: Int = 0,
    val daysToMaturity: Long = 0,
    val riskGrade: String? = null,
    val status: String,
    val dueDate: String,
    val publishedAt: String? = null,
    val expiresAt: String? = null,
    val greenCertified: Boolean = false,
)

/**
 * `GET /api/v1/auctions/{id}`, mirroring `AuctionDetailsResource`.
 *
 * Almost every amount is nullable on the wire: an auction that has not been priced
 * has no target and no quote, and the resource sends those as null rather than
 * leaving them out.
 */
@Serializable
data class AuctionDetailsDto(
    val auctionId: String,
    val invoiceId: String,
    val mypeId: String,
    val status: String,
    val riskGrade: String? = null,
    @Serializable(with = DecimalAsMinorUnits::class) val invoiceAmount: Long,
    @Serializable(with = DecimalAsMinorUnits::class) val fundableAmount: Long? = null,
    @Serializable(with = DecimalAsMinorUnits::class) val targetAmount: Long? = null,
    @Serializable(with = DecimalAsMinorUnits::class) val currentFunding: Long,
    val currency: String,
    val payerRuc: String,
    val payerName: String,
    val dueDate: String,
    val greenCertified: Boolean = false,
    val fullBalanceOutstandingConfirmed: Boolean = false,
    val publishedAt: String? = null,
    val expiresAt: String? = null,
    val closedAt: String? = null,
    val cancelledAt: String? = null,
    val cancellationReason: String? = null,
    val acceptedQuote: FinancialQuoteDto? = null,
)

/**
 * The accepted pricing, mirroring `FinancialQuoteResource`.
 *
 * The MYPE's half of the resource — `mypeTotalCost`, `mypeTceaPct`, the tax split —
 * is deliberately not declared: the client ignores unknown fields, and a DTO that
 * names them would invite a screen to show one.
 */
@Serializable
data class FinancialQuoteDto(
    val quoteId: String,
    val status: String,
    val currency: String,
    val termDays: Int,
    @Serializable(with = DecimalAsMinorUnits::class) val fundableAmount: Long,
    val investorTeaPct: Double,
    val investorTermRatePct: Double,
    @Serializable(with = DecimalAsMinorUnits::class) val fundingTarget: Long,
    @Serializable(with = DecimalAsMinorUnits::class) val investorGrossProfit: Long,
    val platformMonthlyFeeRatePct: Double,
    @Serializable(with = DecimalAsMinorUnits::class) val platformFeeTotal: Long,
    @Serializable(with = DecimalAsMinorUnits::class) val mypeAdvance: Long,
)

/**
 * `POST /api/v1/auctions/{id}/investments`, mirroring `CreateInvestmentResource`.
 *
 * `transactionId` is the client's own reference and doubles as the idempotency key:
 * `Auction.addInvestment` looks for a partition already bought under it and replays
 * that one instead of buying a second, and rejects it outright if the amount or the
 * investor differ. So a retry must carry the same one, and only a success may mint a
 * new one.
 */
@Serializable
data class CreateInvestmentRequestDto(
    val investorId: String,
    @Serializable(with = DecimalAsMinorUnits::class) val amount: Long,
    val currency: String,
    val transactionId: String,
)

/** The partition just bought, mirroring `InvestmentResponseResource`. */
@Serializable
data class InvestmentResponseDto(
    val partitionId: String,
    val auctionId: String,
    val investorId: String,
    @Serializable(with = DecimalAsMinorUnits::class) val amount: Long,
    val currency: String,
    val participationPct: Double,
    @Serializable(with = DecimalAsMinorUnits::class) val expectedMaturityAmount: Long,
    @Serializable(with = DecimalAsMinorUnits::class) val expectedGrossProfit: Long,
    val returnRatePct: Double,
    val status: String,
    val purchasedAt: String? = null,
    val transactionId: String? = null,
)
