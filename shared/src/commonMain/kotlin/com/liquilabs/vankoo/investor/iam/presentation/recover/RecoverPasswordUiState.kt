package com.liquilabs.vankoo.investor.iam.presentation.recover

import com.liquilabs.vankoo.investor.core.result.AppError

data class RecoverPasswordUiState(
    val email: String = "",
    val submitting: Boolean = false,
    /** Set when the address does not even look like one; checked before asking IAM. */
    val emailMalformed: Boolean = false,
    /** What came back from the last attempt. Held untranslated: the screen owns the wording. */
    val error: AppError? = null,
) {
    val canSubmit: Boolean = email.isNotBlank() && !submitting
}
