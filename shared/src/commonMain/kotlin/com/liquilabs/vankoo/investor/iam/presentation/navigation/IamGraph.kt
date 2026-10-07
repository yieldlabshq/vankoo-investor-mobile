package com.liquilabs.vankoo.investor.iam.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.liquilabs.vankoo.investor.iam.presentation.recover.RecoverPasswordScreen
import com.liquilabs.vankoo.investor.iam.presentation.recover.RecoverPasswordSentScreen
import com.liquilabs.vankoo.investor.iam.presentation.reset.ResetPasswordScreen
import com.liquilabs.vankoo.investor.iam.presentation.signin.SignInScreen
import com.liquilabs.vankoo.investor.iam.presentation.signup.SignUpScreen

/**
 * The screens this context owns and how they reach each other.
 *
 * It takes the controller rather than a callback per edge: with four screens that
 * all lead back to sign-in, threading them out one lambda at a time would say more
 * about this graph in the caller than the caller has any business knowing. Where
 * signing in *succeeds* is still the caller's decision — iam does not need to know
 * that a home screen exists.
 */
fun NavGraphBuilder.iamGraph(
    navController: NavHostController,
    onSignedIn: () -> Unit,
) {
    composable<SignInRoute> { entry ->
        SignInScreen(
            onSignedIn = onSignedIn,
            onCreateAccount = { navController.navigate(SignUpRoute) },
            onForgotPassword = { navController.navigate(RecoverPasswordRoute) },
            prefilledEmail = entry.toRoute<SignInRoute>().prefilledEmail,
        )
    }

    composable<SignUpRoute> {
        SignUpScreen(
            onSignedUp = { email -> navController.backToSignIn(email) },
            onSignIn = { navController.backToSignIn() },
        )
    }

    composable<RecoverPasswordRoute> {
        RecoverPasswordScreen(
            onSent = { email -> navController.navigate(RecoverPasswordSentRoute(email)) },
            onBack = { navController.backToSignIn() },
        )
    }

    composable<RecoverPasswordSentRoute> { entry ->
        RecoverPasswordSentScreen(
            email = entry.toRoute<RecoverPasswordSentRoute>().email,
            onBackToSignIn = { navController.backToSignIn() },
        )
    }

    composable<ResetPasswordRoute>(
        // The only destination reachable from outside the app. The scheme is our own
        // rather than https because an https link can only open the app once the site
        // serves the App Links files from a domain we control, and it does not yet.
        deepLinks = listOf(navDeepLink<ResetPasswordRoute>(basePath = "vankoo://reset-password")),
    ) { entry ->
        ResetPasswordScreen(
            token = entry.toRoute<ResetPasswordRoute>().token.orEmpty(),
            // Straight to sign-in: what anyone wants next is to get in, and that form
            // is where the new password proves itself.
            onPasswordChanged = { navController.backToSignIn() },
            onAskForAnother = { navController.navigate(RecoverPasswordRoute) },
        )
    }
}

/**
 * Returns to sign-in and drops whatever came after it.
 *
 * `popUpTo` matches on the route's type, so it finds the entry whether or not it was
 * created with an address. Popping the stack instead would keep the old form — the
 * one thing this must not do after registering, when the point is to arrive with the
 * new address already in the field.
 */
private fun NavHostController.backToSignIn(prefilledEmail: String? = null) {
    navigate(SignInRoute(prefilledEmail)) {
        popUpTo<SignInRoute> { inclusive = true }
    }
}
