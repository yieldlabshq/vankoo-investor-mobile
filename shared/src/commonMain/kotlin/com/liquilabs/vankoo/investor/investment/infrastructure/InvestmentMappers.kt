package com.liquilabs.vankoo.investor.investment.infrastructure

import com.liquilabs.vankoo.investor.investment.domain.model.AuctionDetail
import com.liquilabs.vankoo.investor.investment.domain.model.AuctionStatus
import com.liquilabs.vankoo.investor.investment.domain.model.Currency
import com.liquilabs.vankoo.investor.investment.domain.model.FinancialQuote
import com.liquilabs.vankoo.investor.investment.domain.model.MarketplaceAuction
import com.liquilabs.vankoo.investor.investment.domain.model.Investment
import com.liquilabs.vankoo.investor.investment.domain.model.MarketplacePage
import com.liquilabs.vankoo.investor.investment.domain.model.Money
import com.liquilabs.vankoo.investor.investment.domain.model.PartitionStatus
import com.liquilabs.vankoo.investor.investment.domain.model.RiskGrade
import com.liquilabs.vankoo.investor.investment.infrastructure.dto.AuctionDetailsDto
import com.liquilabs.vankoo.investor.investment.infrastructure.dto.FinancialQuoteDto
import com.liquilabs.vankoo.investor.investment.infrastructure.dto.InvestmentResponseDto
import com.liquilabs.vankoo.investor.investment.infrastructure.dto.MarketplaceAuctionDto
import com.liquilabs.vankoo.investor.investment.infrastructure.dto.MarketplacePageDto
import kotlinx.datetime.LocalDate
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Wire shapes into domain ones.
 *
 * Every enum is read leniently — a value this build has not been taught becomes the
 * Unknown member — because Investment adding a status must not crash a phone nobody
 * has updated. Amounts arrive as cents already: the DTOs do that conversion with
 * `DecimalAsMinorUnits`, so nothing here divides.
 */

@OptIn(ExperimentalTime::class)
internal fun MarketplacePageDto.toDomain(): MarketplacePage = MarketplacePage(
    items = content.map(MarketplaceAuctionDto::toDomain),
    page = page,
    totalPages = totalPages,
)

@OptIn(ExperimentalTime::class)
internal fun MarketplaceAuctionDto.toDomain(): MarketplaceAuction {
    val money = currency.toCurrency()
    return MarketplaceAuction(
        auctionId = auctionId,
        payerName = payerName,
        payerRuc = payerRuc,
        target = Money(targetAmount, money),
        currentFunding = Money(currentFunding, money),
        available = Money(availableAmount, money),
        progressPct = progressPct,
        termRatePct = investorTermRatePct,
        teaPct = investorTeaPct,
        // The service already clamps this at zero and sends it as a long; nothing
        // here is going to be more than a few hundred days out.
        daysToMaturity = daysToMaturity.coerceIn(0, Int.MAX_VALUE.toLong()).toInt(),
        termDays = quotedTermDays,
        riskGrade = riskGrade.toRiskGrade(),
        status = status.toAuctionStatus(),
        dueDate = LocalDate.parse(dueDate),
        expiresAt = expiresAt?.let(::parseInstantOrNull),
        greenCertified = greenCertified,
    )
}

@OptIn(ExperimentalTime::class)
internal fun AuctionDetailsDto.toDomain(): AuctionDetail {
    val money = currency.toCurrency()
    return AuctionDetail(
        auctionId = auctionId,
        payerName = payerName,
        payerRuc = payerRuc,
        status = status.toAuctionStatus(),
        riskGrade = riskGrade.toRiskGrade(),
        invoiceAmount = Money(invoiceAmount, money),
        target = targetAmount?.let { Money(it, money) },
        currentFunding = Money(currentFunding, money),
        dueDate = LocalDate.parse(dueDate),
        greenCertified = greenCertified,
        expiresAt = expiresAt?.let(::parseInstantOrNull),
        acceptedQuote = acceptedQuote?.toDomain(),
    )
}

internal fun FinancialQuoteDto.toDomain(): FinancialQuote {
    val money = currency.toCurrency()
    return FinancialQuote(
        quoteId = quoteId,
        termDays = termDays,
        fundableAmount = Money(fundableAmount, money),
        fundingTarget = Money(fundingTarget, money),
        investorGrossProfit = Money(investorGrossProfit, money),
        investorTermRatePct = investorTermRatePct,
        investorTeaPct = investorTeaPct,
        platformFeeTotal = Money(platformFeeTotal, money),
        platformMonthlyFeeRatePct = platformMonthlyFeeRatePct,
        mypeAdvance = Money(mypeAdvance, money),
    )
}

@OptIn(ExperimentalTime::class)
internal fun InvestmentResponseDto.toDomain(): Investment {
    val money = currency.toCurrency()
    return Investment(
        partitionId = partitionId,
        auctionId = auctionId,
        amount = Money(amount, money),
        participationPct = participationPct,
        expectedReturn = Money(expectedMaturityAmount, money),
        expectedProfit = Money(expectedGrossProfit, money),
        returnRatePct = returnRatePct,
        status = status.toPartitionStatus(),
        purchasedAt = purchasedAt?.let(::parseInstantOrNull),
    )
}

private fun String?.toPartitionStatus(): PartitionStatus = when (this) {
    "ACTIVE" -> PartitionStatus.Active
    "PAID" -> PartitionStatus.Paid
    "DEFAULTED" -> PartitionStatus.Defaulted
    "CANCELLED" -> PartitionStatus.Cancelled
    else -> PartitionStatus.Unknown
}

/**
 * Investment always sends PEN or USD, so an unknown code is a bug on one side or the
 * other. PEN is the fallback for the same reason it is in Finance: a wrong symbol
 * next to a right number is recoverable where a crash is not.
 */
private fun String.toCurrency(): Currency = Currency.fromIsoCode(this) ?: Currency.PEN

private fun String?.toAuctionStatus(): AuctionStatus = when (this) {
    "PENDING_VERIFICATION_RISK" -> AuctionStatus.PendingVerificationRisk
    "DRAFT" -> AuctionStatus.Draft
    "PUBLISHED" -> AuctionStatus.Published
    "FUNDING" -> AuctionStatus.Funding
    "FULLY_FUNDED" -> AuctionStatus.FullyFunded
    "CLOSED" -> AuctionStatus.Closed
    "EXPIRED" -> AuctionStatus.Expired
    "CANCELLED" -> AuctionStatus.Cancelled
    else -> AuctionStatus.Unknown
}

private fun String?.toRiskGrade(): RiskGrade = when (this) {
    "A" -> RiskGrade.A
    "B" -> RiskGrade.B
    "C" -> RiskGrade.C
    // A null grade and an explicit UNDER_EVALUATION mean the same thing to a screen:
    // nobody has graded this yet.
    "UNDER_EVALUATION", null -> RiskGrade.UnderEvaluation
    else -> RiskGrade.Unknown
}

@OptIn(ExperimentalTime::class)
private fun parseInstantOrNull(text: String): Instant? = runCatching { Instant.parse(text) }.getOrNull()
