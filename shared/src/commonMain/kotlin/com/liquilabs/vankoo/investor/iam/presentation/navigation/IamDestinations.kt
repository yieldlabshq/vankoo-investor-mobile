package com.liquilabs.vankoo.investor.iam.presentation.navigation

import kotlinx.serialization.Serializable

/**
 * Signing in, optionally with the address already typed.
 *
 * The argument is what carries someone from having just registered into the form
 * they now have to fill again — sign-up answers without a token, so there is no way
 * to skip it.
 */
@Serializable
data class SignInRoute(val prefilledEmail: String? = null)

@Serializable
data object SignUpRoute

@Serializable
data object RecoverPasswordRoute

/** The address is shown back on the screen, so it travels with the route. */
@Serializable
data class RecoverPasswordSentRoute(val email: String)

/**
 * Where the link from the recovery email lands.
 *
 * The token is nullable because this destination is also reachable from a deep link
 * that arrived malformed — a mail client that broke the URL across lines, a copy that
 * lost the tail. The screen says so rather than pretending it has something to send.
 */
@Serializable
data class ResetPasswordRoute(val token: String? = null)
