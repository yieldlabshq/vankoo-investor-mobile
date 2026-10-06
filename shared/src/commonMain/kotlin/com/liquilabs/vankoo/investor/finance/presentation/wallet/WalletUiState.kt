package com.liquilabs.vankoo.investor.finance.presentation.wallet

import com.liquilabs.vankoo.investor.core.result.AppError
import com.liquilabs.vankoo.investor.finance.domain.model.Wallet
import com.liquilabs.vankoo.investor.finance.domain.model.WalletMovement

/**
 * What the balance screen knows.
 *
 * [wallet] null with [loaded] true is a real state — no wallet yet — and not the
 * absence of an answer; that is what [loaded] is for.
 */
data class WalletUiState(
    val loaded: Boolean = false,
    val refreshing: Boolean = false,
    val wallet: Wallet? = null,
    val movements: List<WalletMovement> = emptyList(),
    val hasMoreMovements: Boolean = false,
    val loadingMore: Boolean = false,
    /** What went wrong with the last load. Held untranslated: the screen owns the wording. */
    val error: AppError? = null,
) {
    /** Nothing to show and nothing went wrong: the "top up to start" card. */
    val isEmpty: Boolean
        get() = loaded && error == null && wallet == null && movements.isEmpty()
}
