package com.liquilabs.vankoo.investor.finance.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.liquilabs.vankoo.investor.core.navigation.FinanceSection
import com.liquilabs.vankoo.investor.finance.presentation.topup.TopUpFailedScreen
import com.liquilabs.vankoo.investor.finance.presentation.topup.TopUpPendingScreen
import com.liquilabs.vankoo.investor.finance.presentation.topup.TopUpScreen
import com.liquilabs.vankoo.investor.finance.presentation.wallet.WalletScreen

/**
 * The screens this context owns, nested under [FinanceSection] so the shell can tell
 * they are all finance's.
 *
 * Every edge here starts and ends inside the wallet, so it takes the controller and
 * decides the stack itself. The one thing the caller might want — where a finished
 * top-up lands — is the wallet again, so there is nothing to hand back.
 */
fun NavGraphBuilder.financeGraph(navController: NavHostController) {
    navigation<FinanceSection>(startDestination = WalletRoute) {
        composable<WalletRoute> {
            WalletScreen(
                onTopUp = { navController.navigate(TopUpRoute()) },
            )
        }

        composable<TopUpRoute> { entry ->
            TopUpScreen(
                initialAmountMinor = entry.toRoute<TopUpRoute>().amountMinor,
                onBack = { navController.popBackStack() },
                onDepositInitiated = { depositId ->
                    navController.navigate(TopUpPendingRoute(depositId)) {
                        // The form is spent: back from the waiting screen should not
                        // offer to send the same amount again.
                        popUpTo<TopUpRoute> { inclusive = true }
                    }
                },
            )
        }

        composable<TopUpPendingRoute> { entry ->
            TopUpPendingScreen(
                depositId = entry.toRoute<TopUpPendingRoute>().depositId,
                onCompleted = { navController.backToWallet() },
                onFailed = { depositId ->
                    navController.navigate(TopUpFailedRoute(depositId)) {
                        popUpTo<TopUpPendingRoute> { inclusive = true }
                    }
                },
                onLeave = { navController.backToWallet() },
            )
        }

        composable<TopUpFailedRoute> { entry ->
            TopUpFailedScreen(
                depositId = entry.toRoute<TopUpFailedRoute>().depositId,
                onBack = { navController.backToWallet() },
                onRetry = { amountMinor ->
                    navController.navigate(TopUpRoute(amountMinor)) {
                        popUpTo<TopUpFailedRoute> { inclusive = true }
                    }
                },
            )
        }
    }
}

/** Drops everything above the balance screen, which re-reads itself on return. */
private fun NavHostController.backToWallet() {
    popBackStack<WalletRoute>(inclusive = false)
}
