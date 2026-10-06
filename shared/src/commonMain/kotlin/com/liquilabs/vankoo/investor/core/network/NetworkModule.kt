package com.liquilabs.vankoo.investor.core.network

import org.koin.dsl.module

/**
 * The HTTP client, built once and shared.
 *
 * [AuthTokenSource] is not declared here: the context that owns the session binds it,
 * which is what keeps this module free of any of them.
 */
val networkModule = module {
    single { createVankooHttpClient(config = get(), tokens = get()) }
}
