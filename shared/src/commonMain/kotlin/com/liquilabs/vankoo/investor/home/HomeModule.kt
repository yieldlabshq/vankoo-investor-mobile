package com.liquilabs.vankoo.investor.home

import com.liquilabs.vankoo.investor.home.presentation.HomeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val homeModule = module {
    viewModelOf(::HomeViewModel)
}
