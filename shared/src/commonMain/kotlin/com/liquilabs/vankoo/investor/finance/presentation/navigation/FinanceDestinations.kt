package com.liquilabs.vankoo.investor.finance.presentation.navigation

import kotlinx.serialization.Serializable

/** The balance and the history. The section's start. */
@Serializable
data object WalletRoute

/** «¿Cuánto quieres recargar?». [amountMinor] pre-fills the field on a retry. */
@Serializable
data class TopUpRoute(val amountMinor: Long? = null)

/**
 * Waiting on a deposit: for Stripe's page to exist, then for the bank's answer.
 *
 * Only the id travels. The screen fetches the deposit itself, because what it shows
 * — the amount, whether the URL is ready, whether it went through — is exactly what
 * changes while it is open.
 */
@Serializable
data class TopUpPendingRoute(val depositId: String)

@Serializable
data class TopUpFailedRoute(val depositId: String)
