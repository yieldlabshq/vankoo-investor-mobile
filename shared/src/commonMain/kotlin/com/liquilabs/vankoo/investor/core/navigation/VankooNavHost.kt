package com.liquilabs.vankoo.investor.core.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.composables.icons.lucide.House
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.TrendingUp
import com.composables.icons.lucide.User
import com.composables.icons.lucide.Wallet
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooBottomBar
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooBottomTab
import com.liquilabs.vankoo.investor.home.presentation.HomeScreen
import com.liquilabs.vankoo.investor.iam.application.ObserveSession
import com.liquilabs.vankoo.investor.iam.presentation.navigation.SignInRoute
import com.liquilabs.vankoo.investor.iam.presentation.navigation.iamGraph
import com.liquilabs.vankoo.investor.investment.presentation.navigation.investmentGraph
import com.liquilabs.vankoo.investor.profile.presentation.ProfileScreen
import com.liquilabs.vankoo.investor.finance.presentation.navigation.financeGraph
import kotlinx.coroutines.flow.first
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import vankoo.shared.generated.resources.Res
import vankoo.shared.generated.resources.tab_home
import vankoo.shared.generated.resources.tab_market
import vankoo.shared.generated.resources.tab_profile
import vankoo.shared.generated.resources.tab_wallet

/**
 * One graph for the whole app, with the tab bar under the part of it that has tabs.
 *
 * A single NavHost rather than one per tab: deep links land in one place, back works
 * across the shell, and a screen inside a section keeps that section's tab lit. The
 * bar shows whenever the current destination belongs to a tab and hides for the rest
 * — signing in has no bar — which is decided from the back stack, not by each screen.
 */
@Composable
fun VankooNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val observeSession: ObserveSession = koinInject()

    // Where the app opens depends on whether a session survived the last launch, and
    // that is a read from disk. It is answered once and never again: making it state
    // the NavHost follows would send someone back to sign-in mid-flow the moment the
    // token was cleared. Null means the answer has not arrived.
    var startDestination by remember { mutableStateOf<Any?>(null) }
    LaunchedEffect(Unit) {
        startDestination = if (observeSession().first() != null) HomeRoute else SignInRoute()
    }

    // One frame of nothing while a local file is read. There is no splash screen in
    // the mockups, and inventing one to cover a few milliseconds would be a screen
    // nobody designed.
    val start = startDestination ?: return

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentTab = backStackEntry?.destination?.tab()

    Column(modifier = modifier) {
        NavHost(
            navController = navController,
            startDestination = start,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            iamGraph(
                navController = navController,
                onSignedIn = {
                    navController.navigate(HomeRoute) {
                        // Sign-in leaves the stack, so back does not return to a
                        // form the investor has already passed.
                        popUpTo<SignInRoute> { inclusive = true }
                    }
                },
            )

            composable<HomeRoute> {
                HomeScreen(
                    onSignedOut = {
                        navController.navigate(SignInRoute()) {
                            popUpTo<HomeRoute> { inclusive = true }
                        }
                    },
                )
            }

            investmentGraph(navController)

            financeGraph(navController)

            composable<ProfileRoute> { ProfileScreen() }
        }

        if (currentTab != null) {
            VankooBottomBar(
                tabs = rememberTabs(),
                selected = currentTab,
                onSelect = { tab -> navController.openTab(tab) },
            )
        }
    }
}

/**
 * Switches tab the way both platforms expect: one copy of each section, its state
 * kept while you are elsewhere, and Home at the bottom of everything.
 *
 * Home is the root of the signed-in stack — sign-in pops itself out on the way in —
 * so popping to it drops whatever the previous tab had open without touching the
 * sign-in flow, and `saveState` lets that tab pick up where it was when tapped again.
 */
private fun NavHostController.openTab(tab: VankooTab) {
    navigate(tab.route()) {
        popUpTo<HomeRoute> { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun rememberTabs(): List<VankooBottomTab<VankooTab>> {
    val home = stringResource(Res.string.tab_home)
    val market = stringResource(Res.string.tab_market)
    val wallet = stringResource(Res.string.tab_wallet)
    val profile = stringResource(Res.string.tab_profile)
    return remember(home, market, wallet, profile) {
        listOf(
            VankooBottomTab(VankooTab.Home, home, Lucide.House),
            VankooBottomTab(VankooTab.Market, market, Lucide.TrendingUp),
            VankooBottomTab(VankooTab.Wallet, wallet, Lucide.Wallet),
            VankooBottomTab(VankooTab.Profile, profile, Lucide.User),
        )
    }
}
