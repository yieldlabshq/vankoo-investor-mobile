package com.liquilabs.vankoo.investor.investment

import com.liquilabs.vankoo.investor.investment.application.GetAuction
import com.liquilabs.vankoo.investor.investment.application.GetMarketplace
import com.liquilabs.vankoo.investor.investment.application.InvestInAuction
import com.liquilabs.vankoo.investor.investment.domain.repository.AuctionRepository
import com.liquilabs.vankoo.investor.investment.domain.repository.InvestorBalance
import com.liquilabs.vankoo.investor.investment.domain.repository.InvestorWallet
import com.liquilabs.vankoo.investor.investment.infrastructure.FinanceInvestorBalance
import com.liquilabs.vankoo.investor.investment.infrastructure.FinanceInvestorWallet
import com.liquilabs.vankoo.investor.investment.infrastructure.HttpAuctionRepository
import com.liquilabs.vankoo.investor.investment.infrastructure.InvestmentApi
import com.liquilabs.vankoo.investor.investment.presentation.confirm.ConfirmInvestmentViewModel
import com.liquilabs.vankoo.investor.investment.presentation.detail.AuctionDetailViewModel
import com.liquilabs.vankoo.investor.investment.presentation.market.MarketViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Everything the investment context needs to run: the marketplace and one auction.
 *
 * [InvestmentApi] takes only the client, not the config: Investment is reached
 * through the gateway like IAM, so there is no service address to pass it.
 *
 * The detail view model takes the auction id first, which is a route argument and not
 * a dependency, so it is declared with an explicit lambda — `viewModelOf` cannot tell
 * the two apart.
 */
val investmentModule = module {
    single { InvestmentApi(client = get()) }
    single<AuctionRepository> { HttpAuctionRepository(get()) }
    // The two bindings that reach outside this context, and the only two: see
    // FinanceInvestorBalance for why the seam is a port and not an injected use case.
    single<InvestorBalance> { FinanceInvestorBalance(get()) }
    single<InvestorWallet> { FinanceInvestorWallet(get()) }

    factory { GetMarketplace(get()) }
    factory { GetAuction(get()) }
    factory { InvestInAuction(get(), get()) }

    viewModelOf(::MarketViewModel)
    viewModel { (auctionId: String) -> AuctionDetailViewModel(auctionId, get()) }
    viewModel { (auctionId: String) ->
        ConfirmInvestmentViewModel(auctionId, get(), get(), get(), get())
    }
}
