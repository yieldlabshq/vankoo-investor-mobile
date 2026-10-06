package com.liquilabs.vankoo.investor.core.navigation

import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy

/** The four tabs of the signed-in shell, in the order the bar shows them. */
enum class VankooTab {
    Home,
    Market,
    Wallet,
    Profile,
}

/**
 * Which tab a destination belongs to, or null for a screen outside the shell — the
 * sign-in flow, where there is no bar.
 *
 * It walks the hierarchy rather than matching the destination itself so a screen
 * nested inside a section counts for that section's tab.
 */
fun NavDestination.tab(): VankooTab? = hierarchy.firstNotNullOfOrNull { destination ->
    when {
        destination.hasRoute<HomeRoute>() -> VankooTab.Home
        destination.hasRoute<MarketSection>() -> VankooTab.Market
        destination.hasRoute<FinanceSection>() -> VankooTab.Wallet
        destination.hasRoute<ProfileRoute>() -> VankooTab.Profile
        else -> null
    }
}

/** The route a tab opens. */
fun VankooTab.route(): Any = when (this) {
    VankooTab.Home -> HomeRoute
    VankooTab.Market -> MarketSection
    VankooTab.Wallet -> FinanceSection
    VankooTab.Profile -> ProfileRoute
}
