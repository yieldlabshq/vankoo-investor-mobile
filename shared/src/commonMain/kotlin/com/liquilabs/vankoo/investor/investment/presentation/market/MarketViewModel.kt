package com.liquilabs.vankoo.investor.investment.presentation.market

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liquilabs.vankoo.investor.core.result.AppResult
import com.liquilabs.vankoo.investor.investment.application.GetMarketplace
import com.liquilabs.vankoo.investor.investment.domain.model.MarketplaceFilter
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Loads the marketplace, one page at a time, under one filter at a time.
 *
 * No account is needed: the marketplace route is public at the gateway, and an
 * auction on offer is the same auction for everyone. That is why this takes only the
 * use case, where every finance view model starts by reading the session.
 *
 * Switching filter cancels the load in flight. Without that, a slow first page can
 * land after a faster second one and repopulate the list with rows the person is no
 * longer asking for — and the cheapest way to make that impossible is to have at most
 * one page request alive.
 */
class MarketViewModel(
    private val getMarketplace: GetMarketplace,
) : ViewModel() {

    private val _state = MutableStateFlow(MarketUiState())
    val state = _state.asStateFlow()

    private var nextPage = 0
    private var loadJob: Job? = null

    /** Reloads the first page under the current filter. Runs on every arrival. */
    fun refresh() {
        load(_state.value.filter)
    }

    /** Switches filter and loads it from the top. A no-op if it is already the one shown. */
    fun selectFilter(filter: MarketplaceFilter) {
        if (_state.value.filter == filter) return
        load(filter)
    }

    private fun load(filter: MarketplaceFilter) {
        loadJob?.cancel()
        // The filter is applied to the state before the answer arrives so the chip
        // lights up on the tap rather than a round trip later.
        _state.update { it.copy(filter = filter, refreshing = true, error = null) }

        loadJob = viewModelScope.launch {
            when (val result = getMarketplace(filter, page = 0)) {
                is AppResult.Success -> {
                    nextPage = 1
                    _state.update {
                        it.copy(
                            loaded = true,
                            refreshing = false,
                            auctions = result.data.items,
                            hasMore = result.data.hasMore,
                            error = null,
                        )
                    }
                }

                is AppResult.Failure -> _state.update {
                    // The list already on screen is kept: a failed refresh of
                    // something that loaded a minute ago is a banner, not a reason to
                    // empty the screen.
                    it.copy(loaded = true, refreshing = false, error = result.error)
                }
            }
        }
    }

    fun loadMore() {
        val current = _state.value
        if (current.loadingMore || current.refreshing || !current.hasMore) return

        val filter = current.filter
        _state.update { it.copy(loadingMore = true) }

        viewModelScope.launch {
            when (val result = getMarketplace(filter, page = nextPage)) {
                is AppResult.Success -> {
                    // The filter can have changed while this was in flight, and this
                    // page belongs to the old one. Dropping it is the only correct
                    // answer; the new filter has already asked for its own page 0.
                    if (_state.value.filter != filter) return@launch
                    nextPage += 1
                    _state.update {
                        it.copy(
                            loadingMore = false,
                            auctions = it.auctions + result.data.items,
                            hasMore = result.data.hasMore,
                        )
                    }
                }

                is AppResult.Failure -> _state.update {
                    it.copy(loadingMore = false, error = result.error)
                }
            }
        }
    }
}
