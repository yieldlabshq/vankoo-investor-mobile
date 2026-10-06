package com.liquilabs.vankoo.investor.iam.presentation.signup

import com.liquilabs.vankoo.investor.core.result.AppError

data class SignUpUiState(
    val email: String = "",
    val password: String = "",
    val passwordConfirmation: String = "",
    val passwordVisible: Boolean = false,
    val termsAccepted: Boolean = false,
    val submitting: Boolean = false,
    /** Set when the address does not even look like one; checked before asking IAM. */
    val emailMalformed: Boolean = false,
    val passwordTooShort: Boolean = false,
    val passwordMismatch: Boolean = false,
    /** What came back from the last attempt. Held untranslated: the screen owns the wording. */
    val error: AppError? = null,
) {
    /**
     * The terms are part of this, so the button stays dead until the box is ticked.
     * The rest of the rules only run on submit — telling someone their password is
     * too short while they are still typing it is not help.
     */
    val canSubmit: Boolean = email.isNotBlank() &&
        password.isNotBlank() &&
        passwordConfirmation.isNotBlank() &&
        termsAccepted &&
        !submitting
}

/** What IAM will accept. Mirrored in the hint under the field. */
const val MIN_PASSWORD_LENGTH = 8
