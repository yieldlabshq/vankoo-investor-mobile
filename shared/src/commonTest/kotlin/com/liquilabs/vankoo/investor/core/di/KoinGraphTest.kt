package com.liquilabs.vankoo.investor.core.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.liquilabs.vankoo.investor.core.config.AppConfig
import com.liquilabs.vankoo.investor.core.network.AuthTokenSource
import com.liquilabs.vankoo.investor.core.platform.UrlOpener
import com.liquilabs.vankoo.investor.core.storage.createVankooDataStore
import com.liquilabs.vankoo.investor.iam.application.ObserveSession
import com.liquilabs.vankoo.investor.iam.application.RequestPasswordReset
import com.liquilabs.vankoo.investor.iam.application.ResetPassword
import com.liquilabs.vankoo.investor.iam.application.SignIn
import com.liquilabs.vankoo.investor.iam.application.SignOut
import com.liquilabs.vankoo.investor.iam.application.SignUp
import com.liquilabs.vankoo.investor.iam.domain.repository.AuthRepository
import com.liquilabs.vankoo.investor.iam.domain.repository.SessionStore
import com.liquilabs.vankoo.investor.iam.infrastructure.IamApi
import com.liquilabs.vankoo.investor.finance.application.DebitWallet
import com.liquilabs.vankoo.investor.finance.application.GetDeposit
import com.liquilabs.vankoo.investor.finance.application.GetWallet
import com.liquilabs.vankoo.investor.finance.application.GetWalletMovements
import com.liquilabs.vankoo.investor.finance.application.InitiateDeposit
import com.liquilabs.vankoo.investor.finance.domain.repository.DepositRepository
import com.liquilabs.vankoo.investor.finance.domain.repository.WalletRepository
import com.liquilabs.vankoo.investor.finance.infrastructure.FinanceApi
import com.liquilabs.vankoo.investor.investment.application.GetAuction
import com.liquilabs.vankoo.investor.investment.application.GetMarketplace
import com.liquilabs.vankoo.investor.investment.application.InvestInAuction
import com.liquilabs.vankoo.investor.investment.domain.repository.AuctionRepository
import com.liquilabs.vankoo.investor.investment.domain.repository.InvestorBalance
import com.liquilabs.vankoo.investor.investment.domain.repository.InvestorWallet
import com.liquilabs.vankoo.investor.investment.infrastructure.InvestmentApi
import io.ktor.client.HttpClient
import kotlin.test.AfterTest
import kotlin.test.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

/**
 * Koin resolves at runtime, so a missing binding is a crash on first use rather
 * than a compile error. This test is what buys back the guarantee Hilt gave for
 * free — it has to grow with every new definition.
 */
class KoinGraphTest {

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun everyDefinitionResolves() {
        val koin = startKoin {
            // Stand-ins for what each platform provides: the real bindings need an
            // Android Application or the iOS document directory, neither of which
            // exists in a host test. What is under test is that the shared graph has
            // no holes, and those two are its only inputs.
            modules(testPlatformModule)
            modules(vankooModules)
        }.koin

        koin.get<AppConfig>()
        koin.get<DataStore<Preferences>>()
        koin.get<HttpClient>()
        koin.get<AuthTokenSource>()
        koin.get<IamApi>()
        koin.get<AuthRepository>()
        koin.get<SessionStore>()
        koin.get<SignIn>()
        koin.get<SignUp>()
        koin.get<RequestPasswordReset>()
        koin.get<ResetPassword>()
        koin.get<SignOut>()
        koin.get<ObserveSession>()
        koin.get<FinanceApi>()
        koin.get<WalletRepository>()
        koin.get<DepositRepository>()
        koin.get<GetWallet>()
        koin.get<GetWalletMovements>()
        koin.get<InitiateDeposit>()
        koin.get<GetDeposit>()
        koin.get<DebitWallet>()
        koin.get<InvestmentApi>()
        koin.get<AuctionRepository>()
        koin.get<GetMarketplace>()
        koin.get<GetAuction>()
        koin.get<InvestInAuction>()
        koin.get<InvestorBalance>()
        koin.get<InvestorWallet>()
    }

    private val testPlatformModule = module {
        single { AppConfig(apiBaseUrl = "http://localhost") }
        single<UrlOpener> { UrlOpener { false } }
        // Never opened: DataStore does not touch the file until something reads it.
        single { createVankooDataStore { "build/koin-graph-test.preferences_pb" } }
    }
}
