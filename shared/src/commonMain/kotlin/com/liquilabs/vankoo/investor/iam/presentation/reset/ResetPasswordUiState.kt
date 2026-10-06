package com.liquilabs.vankoo.investor.iam.presentation.reset

import com.liquilabs.vankoo.investor.core.result.AppError

data class ResetPasswordUiState(
    val password: String = "",
    val passwordConfirmation: String = "",
    val passwordVisible: Boolean = false,
    val submitting: Boolean = false,
    val passwordTooShort: Boolean = false,
    val passwordMismatch: Boolean = false,
    val error: AppError? = null,
) {
    val canSubmit: Boolean =
        password.isNotBlank() && passwordConfirmation.isNotBlank() && !submitting
}
