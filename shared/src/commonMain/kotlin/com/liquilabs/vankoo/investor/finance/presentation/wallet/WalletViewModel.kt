package com.liquilabs.vankoo.investor.finance.presentation.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liquilabs.vankoo.investor.core.result.AppResult
import com.liquilabs.vankoo.investor.iam.application.ObserveSession
import com.liquilabs.vankoo.investor.finance.application.GetWallet
import com.liquilabs.vankoo.investor.finance.application.GetWalletMovements
import com.liquilabs.vankoo.investor.finance.domain.model.Currency
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Loads the balance and the first page of history, together.
 *
 * The account is the signed-in user: Finance keys wallets by the `sub` of the token
 * and knows nothing of Profile, so there is no id to look up first. That is a
 * decision recorded in Trello #53, not an accident of this file.
 *
 * Only soles for now. The backend accepts USD too, but nothing in the app offers a
 * way to hold two currencies yet, and a tab per currency is a design that does not
 * exist.
 */
class WalletViewModel(
    private val observeSession: ObserveSession,
    private val getWallet: GetWallet,
    private val getMovements: GetWalletMovements,
) : ViewModel() {

    private val _state = MutableStateFlow(WalletUiState())
    val state = _state.asStateFlow()

    private var nextPage = 0

    /** Reloads everything. Called on every arrival at the screen, including returns. */
    fun refresh() {
        viewModelScope.launch {
            val accountId = observeSession().first()?.user?.id ?: return@launch
            _state.update { it.copy(refreshing = true, error = null) }

            // Two independent reads; waiting for one before asking the other would
            // make the screen twice as slow for no reason.
            val (walletResult, movementsResult) = coroutineScope {
                val wallet = async { getWallet(accountId, CURRENCY) }
                val movements = async { getMovements(accountId, CURRENCY, page = 0) }
                wallet.await() to movements.await()
            }

            when {
                walletResult is AppResult.Failure -> _state.update {
                    it.copy(loaded = true, refreshing = false, error = walletResult.error)
                }

                movementsResult is AppResult.Failure -> _state.update {
                    it.copy(loaded = true, refreshing = false, error = movementsResult.error)
                }

                walletResult is AppResult.Success && movementsResult is AppResult.Success -> {
                    nextPage = 1
                    _state.update {
                        it.copy(
                            loaded = true,
                            refreshing = false,
                            wallet = walletResult.data,
                            movements = movementsResult.data.items,
                            hasMoreMovements = movementsResult.data.hasMore,
                            error = null,
                        )
                    }
                }
            }
        }
    }

    fun loadMore() {
        val current = _state.value
        if (current.loadingMore || !current.hasMoreMovements) return

        viewModelScope.launch {
            val accountId = observeSession().first()?.user?.id ?: return@launch
            _state.update { it.copy(loadingMore = true) }

            when (val result = getMovements(accountId, CURRENCY, page = nextPage)) {
                is AppResult.Success -> {
                    nextPage += 1
                    _state.update {
                        it.copy(
                            loadingMore = false,
                            movements = it.movements + result.data.items,
                            hasMoreMovements = result.data.hasMore,
                        )
                    }
                }

                is AppResult.Failure -> _state.update {
                    it.copy(loadingMore = false, error = result.error)
                }
            }
        }
    }

    companion object {
        val CURRENCY = Currency.PEN
    }
}
