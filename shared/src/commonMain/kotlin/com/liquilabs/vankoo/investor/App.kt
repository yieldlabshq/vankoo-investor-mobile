package com.liquilabs.vankoo.investor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.liquilabs.vankoo.investor.core.designsystem.VankooTheme
import com.liquilabs.vankoo.investor.core.navigation.VankooNavHost

/**
 * Application shell, entered from both platforms.
 *
 * It establishes the theme — which follows the device setting rather than an
 * in-app switch — and hands the screen to the navigation graph.
 *
 * The controller is a parameter so a platform can hold on to it. Android needs to:
 * a deep link that arrives while the app is already open comes through onNewIntent,
 * where there is no composition happening to notice it.
 */
@Composable
fun App(navController: NavHostController = rememberNavController()) {
    VankooTheme {
        VankooNavHost(
            navController = navController,
            modifier = Modifier
                .fillMaxSize()
                .background(VankooTheme.colors.surfaceBase),
        )
    }
}
