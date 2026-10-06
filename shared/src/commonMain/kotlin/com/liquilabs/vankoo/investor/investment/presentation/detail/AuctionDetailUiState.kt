package com.liquilabs.vankoo.investor.investment.presentation.detail

import com.liquilabs.vankoo.investor.core.result.AppError
import com.liquilabs.vankoo.investor.investment.domain.model.AuctionDetail

/**
 * What the detail screen knows.
 *
 * [gone] is carried apart from [error] because a 404 here is not a failure: the
 * auction was fondeable when the list was drawn and is not any more, which is a
 * normal end for one. It gets its own empty state and a way back to the market,
 * where a red "the server did not answer as expected" would blame the platform for
 * the auction doing what auctions do.
 */
data class AuctionDetailUiState(
    val loaded: Boolean = false,
    val refreshing: Boolean = false,
    val auction: AuctionDetail? = null,
    val gone: Boolean = false,
    val error: AppError? = null,
)
