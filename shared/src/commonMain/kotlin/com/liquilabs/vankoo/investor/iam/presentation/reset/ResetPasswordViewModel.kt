package com.liquilabs.vankoo.investor.iam.presentation.reset

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liquilabs.vankoo.investor.core.result.AppResult
import com.liquilabs.vankoo.investor.iam.application.ResetPassword
import com.liquilabs.vankoo.investor.iam.presentation.signup.MIN_PASSWORD_LENGTH
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ResetPasswordViewModel(private val resetPassword: ResetPassword) : ViewModel() {

    private val _state = MutableStateFlow(ResetPasswordUiState())
    val state = _state.asStateFlow()

    private val changedEvents = Channel<Unit>(Channel.BUFFERED)
    val changed = changedEvents.receiveAsFlow()

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

    /**
     * The token comes from the deep link rather than from the form, so it is passed in
     * at submit time: it is not something anyone types, and it has no business being
     * part of the screen's state.
     */
    fun onSubmit(token: String) {
        val current = _state.value
        if (!current.canSubmit) return

        val passwordTooShort = current.password.length < MIN_PASSWORD_LENGTH
        val passwordMismatch = current.password != current.passwordConfirmation
        if (passwordTooShort || passwordMismatch) {
            _state.update { it.copy(passwordTooShort = passwordTooShort, passwordMismatch = passwordMismatch) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(submitting = true, error = null) }

            when (val result = resetPassword(token, current.password)) {
                is AppResult.Success -> {
                    _state.update { it.copy(submitting = false) }
                    changedEvents.send(Unit)
                }

                is AppResult.Failure -> {
                    _state.update { it.copy(submitting = false, error = result.error) }
                }
            }
        }
    }
}
