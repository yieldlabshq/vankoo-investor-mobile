package com.liquilabs.vankoo.investor.core.di

import com.liquilabs.vankoo.investor.core.network.networkModule
import com.liquilabs.vankoo.investor.home.homeModule
import com.liquilabs.vankoo.investor.iam.iamModule
import com.liquilabs.vankoo.investor.finance.financeModule
import com.liquilabs.vankoo.investor.investment.investmentModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Bindings that only one platform can provide, such as secure token storage.
 *
 * This is where dependency injection meets expect/actual: common code asks for
 * an interface without knowing which implementation it gets.
 */
expect val platformModule: Module

/** Cross-cutting bindings shared by every context. */
val coreModule: Module = module { }

/** Every context's module, in one place. */
val vankooModules: List<Module> = listOf(coreModule, networkModule, iamModule, homeModule, financeModule, investmentModule)

/**
 * Starts the container. Each platform calls this once at launch through its own
 * entry point, which is what keeps Koin out of the platform app modules.
 */
fun initKoin(appDeclaration: KoinApplication.() -> Unit = {}) = startKoin {
    appDeclaration()
    modules(platformModule)
    modules(vankooModules)
}
