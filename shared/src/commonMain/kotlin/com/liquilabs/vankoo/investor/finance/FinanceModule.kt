package com.liquilabs.vankoo.investor.finance

import com.liquilabs.vankoo.investor.finance.application.DebitWallet
import com.liquilabs.vankoo.investor.finance.application.GetDeposit
import com.liquilabs.vankoo.investor.finance.application.GetWallet
import com.liquilabs.vankoo.investor.finance.application.GetWalletMovements
import com.liquilabs.vankoo.investor.finance.application.InitiateDeposit
import com.liquilabs.vankoo.investor.finance.domain.repository.DepositRepository
import com.liquilabs.vankoo.investor.finance.domain.repository.WalletRepository
import com.liquilabs.vankoo.investor.finance.infrastructure.FinanceApi
import com.liquilabs.vankoo.investor.finance.infrastructure.HttpDepositRepository
import com.liquilabs.vankoo.investor.finance.infrastructure.HttpWalletRepository
import com.liquilabs.vankoo.investor.finance.presentation.topup.ProviderReturnSignal
import com.liquilabs.vankoo.investor.finance.presentation.topup.TopUpFailedViewModel
import com.liquilabs.vankoo.investor.finance.presentation.topup.TopUpPendingViewModel
import com.liquilabs.vankoo.investor.finance.presentation.topup.TopUpViewModel
import com.liquilabs.vankoo.investor.finance.presentation.wallet.WalletViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Everything the finance context needs to run: the wallet and its top-ups.
 *
 * The three top-up view models take a route argument first — the amount to pre-fill,
 * the deposit to wait on — so they are declared with an explicit lambda that pulls it
 * from the parameters the screen passes; `viewModelOf` cannot tell a route argument
 * from a dependency.
 */
val financeModule = module {
    single { FinanceApi(client = get()) }
    single<WalletRepository> { HttpWalletRepository(get()) }
    single<DepositRepository> { HttpDepositRepository(get()) }

    factory { GetWallet(get()) }
    factory { GetWalletMovements(get()) }
    factory { InitiateDeposit(get()) }
    factory { GetDeposit(get()) }
    factory { DebitWallet(get()) }

    // One for the whole process: the platform writes it from the return link, the
    // waiting screen reads it. See ProviderReturnSignal for why it is not a flow.
    single { ProviderReturnSignal() }

    viewModelOf(::WalletViewModel)
    viewModel { (initialAmountMinor: Long?) ->
        TopUpViewModel(initialAmountMinor, get(), get(), get())
    }
    viewModel { (depositId: String) ->
        TopUpPendingViewModel(depositId, get(), get(), get(), get(), get())
    }
    viewModel { (depositId: String) ->
        TopUpFailedViewModel(depositId, get(), get(), get())
    }
}
