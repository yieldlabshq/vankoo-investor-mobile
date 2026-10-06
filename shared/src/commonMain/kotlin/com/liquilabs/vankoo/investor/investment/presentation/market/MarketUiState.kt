package com.liquilabs.vankoo.investor.investment.presentation.market

import com.liquilabs.vankoo.investor.core.result.AppError
import com.liquilabs.vankoo.investor.investment.domain.model.MarketplaceAuction
import com.liquilabs.vankoo.investor.investment.domain.model.MarketplaceFilter

/**
 * What the market screen knows.
 *
 * [loaded] is separate from an empty list for the same reason it is on the wallet: an
 * empty market is an answer — "nothing is looking for funding right now" — and before
 * the first one arrives there is nothing to say at all.
 */
data class MarketUiState(
    val filter: MarketplaceFilter = MarketplaceFilter.All,
    val loaded: Boolean = false,
    val refreshing: Boolean = false,
    val auctions: List<MarketplaceAuction> = emptyList(),
    val hasMore: Boolean = false,
    val loadingMore: Boolean = false,
    /** What went wrong with the last load. Held untranslated: the screen owns the wording. */
    val error: AppError? = null,
) {
    /** Nothing to list and nothing went wrong. */
    val isEmpty: Boolean
        get() = loaded && error == null && auctions.isEmpty()

    /**
     * Whether the empty state should offer to drop the filter.
     *
     * Only when a filter is what emptied it. «Vencen pronto» is a sort and hides
     * nothing, so an empty market under it is the market being empty.
     */
    val isEmptyBecauseFiltered: Boolean
        get() = isEmpty && filter == MarketplaceFilter.Green
}
