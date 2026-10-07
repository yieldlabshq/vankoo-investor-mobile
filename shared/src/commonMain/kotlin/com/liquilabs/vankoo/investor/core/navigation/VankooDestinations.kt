package com.liquilabs.vankoo.investor.core.navigation

import kotlinx.serialization.Serializable

/**
 * Destinations every context needs to reach.
 *
 * A context keeps its own screens in its own `presentation/navigation`; only
 * the entry points that another context navigates to belong here. The four below
 * are the tabs: the bar has to name them, and it lives above every context.
 */

/** The root of the signed-in stack. Every tab pops back to it. */
@Serializable
data object HomeRoute

/**
 * The investment context's nested graph, not a screen.
 *
 * Navigating here lands on the marketplace — the context's start destination — and
 * the detail of one auction sits inside it, which is what keeps the Mercado tab lit
 * while someone is reading one. Same shape, and same reason, as [FinanceSection].
 */
@Serializable
data object MarketSection

/**
 * The finance context's nested graph, not a screen.
 *
 * Navigating here lands on the wallet — the context's start destination — and every
 * screen finance owns, top-up included, sits inside it. That is what lets the bar keep the
 * Billetera tab lit while someone is three screens into a top-up, the way the mockups
 * draw it, without the bar knowing what those screens are.
 */
@Serializable
data object FinanceSection

@Serializable
data object ProfileRoute
