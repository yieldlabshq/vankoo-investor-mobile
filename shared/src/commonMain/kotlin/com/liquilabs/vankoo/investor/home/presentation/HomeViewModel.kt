package com.liquilabs.vankoo.investor.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liquilabs.vankoo.investor.iam.application.ObserveSession
import com.liquilabs.vankoo.investor.iam.application.SignOut
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    observeSession: ObserveSession,
    private val signOut: SignOut,
) : ViewModel() {

    val signedInAs = observeSession()
        .map { it?.user?.email }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun onSignOut() {
        viewModelScope.launch { signOut() }
    }
}
