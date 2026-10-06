package com.liquilabs.vankoo.investor.investment.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.liquilabs.vankoo.investor.core.navigation.FinanceSection
import com.liquilabs.vankoo.investor.core.navigation.MarketSection
import com.liquilabs.vankoo.investor.investment.presentation.confirm.ConfirmInvestmentScreen
import com.liquilabs.vankoo.investor.investment.presentation.detail.AuctionDetailScreen
import com.liquilabs.vankoo.investor.investment.presentation.market.MarketScreen

/**
 * The screens this context owns, nested under [MarketSection] so the shell can tell
 * they are all the market's.
 *
 * Nested for the same reason finance is: the bottom bar decides which tab is lit by
 * walking the destination's hierarchy, so a detail inside this graph keeps Mercado
 * highlighted instead of leaving the bar with nothing to point at.
 */
fun NavGraphBuilder.investmentGraph(navController: NavHostController) {
    navigation<MarketSection>(startDestination = MarketplaceRoute) {
        composable<MarketplaceRoute> {
            MarketScreen(
                onOpenAuction = { auctionId -> navController.navigate(AuctionDetailRoute(auctionId)) },
            )
        }

        composable<AuctionDetailRoute> { entry ->
            AuctionDetailScreen(
                auctionId = entry.toRoute<AuctionDetailRoute>().auctionId,
                onBack = { navController.popBackStack() },
                onInvest = { auctionId -> navController.navigate(ConfirmInvestmentRoute(auctionId)) },
            )
        }

        composable<ConfirmInvestmentRoute> { entry ->
            ConfirmInvestmentScreen(
                auctionId = entry.toRoute<ConfirmInvestmentRoute>().auctionId,
                onBack = { navController.popBackStack() },
                // The wallet, not the top-up form: reaching TopUpRoute would mean this
                // context importing finance's destinations, and the wallet is one tap
                // from «Recargar» anyway. The tab bar keeps its own back stack, so the
                // half-filled form is still here on the way back.
                onTopUp = { navController.navigate(FinanceSection) },
                // Nothing to come back to: the partition is bought and the detail
                // behind is stale by exactly that amount.
                onDone = { navController.popBackStack<MarketplaceRoute>(inclusive = false) },
            )
        }
    }
}
