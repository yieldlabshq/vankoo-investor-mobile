package com.liquilabs.vankoo.investor.investment.presentation.navigation

import kotlinx.serialization.Serializable

/** The list of auctions open for investment. The section's start. */
@Serializable
data object MarketplaceRoute

/**
 * One auction in full.
 *
 * Only the id travels. The screen fetches the auction itself, because what it shows —
 * how much is left to fund, whether it still takes money — is exactly what changes
 * while someone is reading a list of them.
 */
@Serializable
data class AuctionDetailRoute(val auctionId: String)

/**
 * «¿Cuánto quieres invertir?», for one auction.
 *
 * The id again, and for the same reason: what is left to fund is exactly what the
 * screen has to be right about, and it is what other people are changing.
 */
@Serializable
data class ConfirmInvestmentRoute(val auctionId: String)
