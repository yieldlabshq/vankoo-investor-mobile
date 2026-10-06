package com.liquilabs.vankoo.investor.iam.presentation.signin

import com.liquilabs.vankoo.investor.core.result.AppError

data class SignInUiState(
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val submitting: Boolean = false,
    /** Set when the address does not even look like one; checked before asking IAM. */
    val emailMalformed: Boolean = false,
    /** What came back from the last attempt. Held untranslated: the screen owns the wording. */
    val error: AppError? = null,
) {
    val canSubmit: Boolean =
        email.isNotBlank() && password.isNotBlank() && !submitting
}
