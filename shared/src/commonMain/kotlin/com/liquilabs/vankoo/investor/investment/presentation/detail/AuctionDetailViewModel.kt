package com.liquilabs.vankoo.investor.investment.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liquilabs.vankoo.investor.core.result.AppError
import com.liquilabs.vankoo.investor.core.result.AppResult
import com.liquilabs.vankoo.investor.investment.application.GetAuction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Loads one auction.
 *
 * The id arrives as a route argument rather than being looked up from the list the
 * previous screen held: the funding figures move while someone reads, and a detail
 * built from a row that was fetched a minute ago would offer a target that has since
 * been met. So it re-reads, and re-reads again on every return.
 */
class AuctionDetailViewModel(
    private val auctionId: String,
    private val getAuction: GetAuction,
) : ViewModel() {

    private val _state = MutableStateFlow(AuctionDetailUiState())
    val state = _state.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(refreshing = true, error = null) }

            when (val result = getAuction(auctionId)) {
                is AppResult.Success -> _state.update {
                    it.copy(
                        loaded = true,
                        refreshing = false,
                        auction = result.data,
                        gone = false,
                        error = null,
                    )
                }

                is AppResult.Failure -> _state.update {
                    val gone = result.error == AUCTION_GONE
                    it.copy(
                        loaded = true,
                        refreshing = false,
                        gone = gone,
                        error = if (gone) null else result.error,
                    )
                }
            }
        }
    }

    private companion object {
        /**
         * Investment answers a missing auction with 404 and `{"message": ...}`, which
         * has no `code` to read, so the status is all there is to go on. It is enough:
         * this endpoint has exactly one thing it cannot find.
         */
        val AUCTION_GONE = AppError.Server(404)
    }
}
