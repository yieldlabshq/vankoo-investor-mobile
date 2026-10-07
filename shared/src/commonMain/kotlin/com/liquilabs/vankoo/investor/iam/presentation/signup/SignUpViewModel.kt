package com.liquilabs.vankoo.investor.iam.presentation.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liquilabs.vankoo.investor.core.result.AppResult
import com.liquilabs.vankoo.investor.iam.application.SignUp
import com.liquilabs.vankoo.investor.iam.domain.model.Email
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SignUpViewModel(private val signUp: SignUp) : ViewModel() {

    private val _state = MutableStateFlow(SignUpUiState())
    val state = _state.asStateFlow()

    /**
     * Carries the address of the account just created, because that is the whole
     * point of the event here: registering does not sign anyone in, so the next
     * screen is sign-in with the address already filled.
     */
    private val signedUpEvents = Channel<String>(Channel.BUFFERED)
    val signedUp = signedUpEvents.receiveAsFlow()

    fun onEmailChange(value: String) {
        _state.update { it.copy(email = value, emailMalformed = false, error = null) }
    }

    fun onPasswordChange(value: String) {
        _state.update {
            it.copy(password = value, passwordTooShort = false, passwordMismatch = false, error = null)
        }
    }

    fun onPasswordConfirmationChange(value: String) {
        _state.update { it.copy(passwordConfirmation = value, passwordMismatch = false, error = null) }
    }

    fun onPasswordVisibilityToggle() {
        _state.update { it.copy(passwordVisible = !it.passwordVisible) }
    }

    fun onTermsAcceptedChange(accepted: Boolean) {
        _state.update { it.copy(termsAccepted = accepted, error = null) }
    }

    fun onSubmit() {
        val current = _state.value
        if (!current.canSubmit) return

        val emailMalformed = !Email.isValid(current.email)
        val passwordTooShort = current.password.length < MIN_PASSWORD_LENGTH
        val passwordMismatch = current.password != current.passwordConfirmation
        if (emailMalformed || passwordTooShort || passwordMismatch) {
            _state.update {
                it.copy(
                    emailMalformed = emailMalformed,
                    passwordTooShort = passwordTooShort,
                    passwordMismatch = passwordMismatch,
                )
            }
            return
        }

        val email = current.email.trim()
        viewModelScope.launch {
            _state.update { it.copy(submitting = true, error = null) }

            when (val result = signUp(Email(email), current.password)) {
                is AppResult.Success -> {
                    _state.update { it.copy(submitting = false) }
                    signedUpEvents.send(result.data.email)
                }

                is AppResult.Failure -> {
                    _state.update { it.copy(submitting = false, error = result.error) }
                }
            }
        }
    }
}
