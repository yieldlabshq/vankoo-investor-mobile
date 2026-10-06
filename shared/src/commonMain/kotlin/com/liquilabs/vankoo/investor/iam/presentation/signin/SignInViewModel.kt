package com.liquilabs.vankoo.investor.iam.presentation.signin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liquilabs.vankoo.investor.core.result.AppResult
import com.liquilabs.vankoo.investor.iam.application.SignIn
import com.liquilabs.vankoo.investor.iam.domain.model.Email
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SignInViewModel(private val signIn: SignIn) : ViewModel() {

    private val _state = MutableStateFlow(SignInUiState())
    val state = _state.asStateFlow()

    /**
     * Signalled once per successful sign-in. A Channel rather than state,
     * because navigating is something that happens, not something the screen is.
     */
    private val signedInEvents = Channel<Unit>(Channel.BUFFERED)
    val signedIn = signedInEvents.receiveAsFlow()

    fun onEmailChange(value: String) {
        _state.update { it.copy(email = value, emailMalformed = false, error = null) }
    }

    fun onPasswordChange(value: String) {
        _state.update { it.copy(password = value, error = null) }
    }

    fun onPasswordVisibilityToggle() {
        _state.update { it.copy(passwordVisible = !it.passwordVisible) }
    }

    fun onSubmit() {
        val current = _state.value
        if (!current.canSubmit) return

        if (!Email.isValid(current.email)) {
            _state.update { it.copy(emailMalformed = true) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(submitting = true, error = null) }

            when (val result = signIn(Email(current.email.trim()), current.password)) {
                is AppResult.Success -> {
                    _state.update { it.copy(submitting = false) }
                    signedInEvents.send(Unit)
                }

                is AppResult.Failure -> {
                    _state.update { it.copy(submitting = false, error = result.error) }
                }
            }
        }
    }
}
