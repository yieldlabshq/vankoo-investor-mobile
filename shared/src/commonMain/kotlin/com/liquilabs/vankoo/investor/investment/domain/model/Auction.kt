package com.liquilabs.vankoo.investor.investment.domain.model

import kotlinx.datetime.LocalDate
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * How far an auction is through its life, as Investment's `AuctionStatus`.
 *
 * The marketplace only ever lists [Published] and [Funding] unless the screen asks
 * for more, but the other members exist because the detail endpoint answers for any
 * auction: someone can be looking at one when the last partition lands, and coming
 * back to it must say "fully funded" rather than offer to invest.
 */
enum class AuctionStatus {
    PendingVerificationRisk,
    Draft,
    Published,
    Funding,
    FullyFunded,
    Closed,
    Expired,
    Cancelled,

    /** A status this build has not been taught. Treated as taking no more money. */
    Unknown,
}

/**
 * The risk grade of an auction, as Investment's `ScoreGrade`.
 *
 * Four values, not three: [UnderEvaluation] is what every auction is born with
 * (`RiskScore.pendingEvaluation()`) and the marketplace can return it. It is the
 * absence of a grade rather than a bad one, which is why the badge renders it in the
 * neutral ramp and not in the red end of the `risk/A…C` ramp.
 */
enum class RiskGrade {
    A,
    B,
    C,
    UnderEvaluation,
    Unknown,
}

/**
 * One row of the marketplace, as Investment's `AuctionMarketplaceView`.
 *
 * It is a read model and not a trimmed aggregate: the funding percentage and the days
 * to maturity are computed by the service, so the list does not do arithmetic on
 * dates and two clients cannot round the same bar differently.
 *
 * Two things the mockup prints are missing on purpose, because the view does not
 * carry them: the MYPE's name — only its UUID travels — and the invoice number. The
 * card is headlined with [payerName] instead, which is the company whose payment the
 * investor is actually buying.
 */
@OptIn(ExperimentalTime::class)
data class MarketplaceAuction(
    val auctionId: String,
    val payerName: String,
    val payerRuc: String,
    val target: Money,
    val currentFunding: Money,
    val available: Money,
    /** 0..100, as the service computed it. */
    val progressPct: Double,
    /** The investor's return over the term, in percentage points. Null before a quote. */
    val termRatePct: Double?,
    /**
     * The same return annualised — the TEA — in percentage points. Null before a quote.
     *
     * It is what makes two auctions comparable: 8.1 % over 201 days and 2.4 % over 76
     * are 15 % and 12 % a year, and a screen that shows only the term rate invites
     * picking the worse one.
     */
    val teaPct: Double?,
    val daysToMaturity: Int,
    /** Days the quote priced, which is not the same as the days left to maturity. */
    val termDays: Int,
    val riskGrade: RiskGrade,
    val status: AuctionStatus,
    val dueDate: LocalDate,
    val expiresAt: Instant?,
    val greenCertified: Boolean,
) {
    /** 0f..1f for the bar, from the percentage the service already worked out. */
    val progressFraction: Float
        get() = (progressPct / 100.0).toFloat()
}

/** A page of the marketplace, with enough to know whether to offer more. */
data class MarketplacePage(
    val items: List<MarketplaceAuction>,
    val page: Int,
    val totalPages: Int,
) {
    val hasMore: Boolean
        get() = page + 1 < totalPages
}

/**
 * One auction in full, as Investment's `AuctionDetailsResource`.
 *
 * [acceptedQuote] is null until a MYPE accepts the pricing, which is also when the
 * auction gets published — so in practice anything reachable from the marketplace has
 * one. The detail screen still has to survive its absence, because the endpoint is
 * open to any id and an auction that has not been priced yet has no discount to show.
 */
@OptIn(ExperimentalTime::class)
data class AuctionDetail(
    val auctionId: String,
    val payerName: String,
    val payerRuc: String,
    val status: AuctionStatus,
    val riskGrade: RiskGrade,
    val invoiceAmount: Money,
    val target: Money?,
    val currentFunding: Money,
    val dueDate: LocalDate,
    val greenCertified: Boolean,
    val expiresAt: Instant?,
    val acceptedQuote: FinancialQuote?,
) {
    /** What is left to raise. Null while the auction has no target to raise it against. */
    val remaining: Money?
        get() = target?.let { it - currentFunding }

    /** 0f..1f. Computed here because the details resource, unlike the list, sends no percentage. */
    val progressFraction: Float
        get() {
            val targetMinor = target?.amountMinor ?: return 0f
            if (targetMinor <= 0L) return 0f
            return (currentFunding.amountMinor.toDouble() / targetMinor).toFloat()
        }

    val progressPct: Double
        get() = progressFraction * 100.0
}

/**
 * The pricing a MYPE accepted, as Investment's `FinancialQuoteResource`.
 *
 * Only the figures the investor's screen prints are kept. The resource carries the
 * MYPE's side of the deal too — its TCEA, its total cost — and none of that belongs
 * on a screen for the other party.
 *
 * Every rate arrives already in percentage points (`4.200000`, not `0.042`), which is
 * the service's own conversion, and is held as a Double: a rate is shown and never
 * summed, so it has none of the reasons money has to stay an integer.
 */
data class FinancialQuote(
    val quoteId: String,
    val termDays: Int,
    /** What investors are paid back at maturity: the target plus their profit. */
    val fundableAmount: Money,
    val fundingTarget: Money,
    val investorGrossProfit: Money,
    val investorTermRatePct: Double,
    /** The same rate annualised. See [MarketplaceAuction.teaPct]. */
    val investorTeaPct: Double,
    val platformFeeTotal: Money,
    val platformMonthlyFeeRatePct: Double,
    val mypeAdvance: Money,
)
