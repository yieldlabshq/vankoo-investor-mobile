package com.liquilabs.vankoo.investor.iam.presentation.recover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liquilabs.vankoo.investor.core.result.AppResult
import com.liquilabs.vankoo.investor.iam.application.RequestPasswordReset
import com.liquilabs.vankoo.investor.iam.domain.model.Email
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RecoverPasswordViewModel(private val requestPasswordReset: RequestPasswordReset) : ViewModel() {

    private val _state = MutableStateFlow(RecoverPasswordUiState())
    val state = _state.asStateFlow()

    /** Carries the address to the confirmation screen, which shows where it wrote. */
    private val sentEvents = Channel<String>(Channel.BUFFERED)
    val sent = sentEvents.receiveAsFlow()

    fun onEmailChange(value: String) {
        _state.update { it.copy(email = value, emailMalformed = false, error = null) }
    }

    fun onSubmit() {
        val current = _state.value
        if (!current.canSubmit) return

        if (!Email.isValid(current.email)) {
            _state.update { it.copy(emailMalformed = true) }
            return
        }

        val email = current.email.trim()
        viewModelScope.launch {
            _state.update { it.copy(submitting = true, error = null) }

            when (val result = requestPasswordReset(Email(email))) {
                // Success says the request was accepted and nothing more. Whether that
                // address has an account is not something this client is told, on
                // purpose, so there is nothing else to branch on here.
                is AppResult.Success -> {
                    _state.update { it.copy(submitting = false) }
                    sentEvents.send(email)
                }

                is AppResult.Failure -> {
                    _state.update { it.copy(submitting = false, error = result.error) }
                }
            }
        }
    }
}
