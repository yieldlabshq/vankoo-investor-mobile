package com.liquilabs.vankoo.investor.finance.presentation.topup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liquilabs.vankoo.investor.core.result.AppError
import com.liquilabs.vankoo.investor.core.result.AppResult
import com.liquilabs.vankoo.investor.iam.application.ObserveSession
import com.liquilabs.vankoo.investor.finance.application.GetDeposit
import com.liquilabs.vankoo.investor.finance.application.GetWallet
import com.liquilabs.vankoo.investor.finance.domain.model.Currency
import com.liquilabs.vankoo.investor.finance.domain.model.Deposit
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TopUpFailedUiState(
    val deposit: Deposit? = null,
    val balanceMinor: Long? = null,
    val error: AppError? = null,
)

/** Reads the failed deposit once, and the balance it did not change. */
class TopUpFailedViewModel(
    depositId: String,
    observeSession: ObserveSession,
    getDeposit: GetDeposit,
    getWallet: GetWallet,
) : ViewModel() {

    private val _state = MutableStateFlow(TopUpFailedUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val accountId = observeSession().first()?.user?.id ?: return@launch
            val (depositResult, walletResult) = coroutineScope {
                val deposit = async { getDeposit(depositId) }
                val wallet = async { getWallet(accountId, Currency.PEN) }
                deposit.await() to wallet.await()
            }
            _state.update {
                it.copy(
                    deposit = (depositResult as? AppResult.Success)?.data,
                    balanceMinor = (walletResult as? AppResult.Success)?.data?.balance?.amountMinor ?: 0L,
                    error = (depositResult as? AppResult.Failure)?.error,
                )
            }
        }
    }
}
