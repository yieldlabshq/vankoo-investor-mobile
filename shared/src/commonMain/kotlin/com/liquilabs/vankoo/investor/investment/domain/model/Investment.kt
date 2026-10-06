package com.liquilabs.vankoo.investor.investment.domain.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * A share of one auction, bought. Investment's `Partition`, as the app reads it back.
 *
 * Everything here is the service's arithmetic, not a repeat of what the confirm screen
 * estimated before sending: the screen works out a participation from the numbers it
 * holds, and this is what the auction actually granted. They agree today, and when
 * they stop agreeing the one that counts is this one.
 */
@OptIn(ExperimentalTime::class)
data class Investment(
    val partitionId: String,
    val auctionId: String,
    val amount: Money,
    /** 0..100 of the auction's target. */
    val participationPct: Double,
    /** What this share collects at maturity: the amount plus the profit. */
    val expectedReturn: Money,
    val expectedProfit: Money,
    val returnRatePct: Double,
    val status: PartitionStatus,
    val purchasedAt: Instant?,
)

/** What has become of a share, as Investment's `PartitionStatus`. */
enum class PartitionStatus {
    Active,
    Paid,
    Defaulted,
    Cancelled,

    /** A status this build has not been taught. */
    Unknown,
}
