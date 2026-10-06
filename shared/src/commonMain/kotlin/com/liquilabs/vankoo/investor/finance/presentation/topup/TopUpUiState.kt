package com.liquilabs.vankoo.investor.finance.presentation.topup

import com.liquilabs.vankoo.investor.core.format.MoneyFormat
import com.liquilabs.vankoo.investor.core.result.AppError

data class TopUpUiState(
    /** What is in the field, as typed. Parsed on demand, never reformatted under the finger. */
    val amountText: String = "",
    /** The balance shown under the field; null until it has been read, or if it could not be. */
    val balanceMinor: Long? = null,
    val submitting: Boolean = false,
    /** What came back from the last attempt. Held untranslated: the screen owns the wording. */
    val error: AppError? = null,
) {
    val amountMinor: Long?
        get() = MoneyFormat.parseMinor(amountText)

    val canSubmit: Boolean
        get() = (amountMinor ?: 0L) > 0L && !submitting
}
