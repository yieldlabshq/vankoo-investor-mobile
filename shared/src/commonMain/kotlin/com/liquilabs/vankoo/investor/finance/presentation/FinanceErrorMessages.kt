package com.liquilabs.vankoo.investor.finance.presentation

import androidx.compose.runtime.Composable
import com.liquilabs.vankoo.investor.core.result.AppError
import com.liquilabs.vankoo.investor.finance.domain.model.DepositFailureReason
import com.liquilabs.vankoo.investor.finance.domain.model.FinanceFailure
import com.liquilabs.vankoo.investor.finance.domain.model.MovementKind
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import vankoo.shared.generated.resources.Res
import vankoo.shared.generated.resources.error_network
import vankoo.shared.generated.resources.error_server
import vankoo.shared.generated.resources.error_unexpected
import vankoo.shared.generated.resources.iam_error_unauthenticated
import vankoo.shared.generated.resources.finance_error_insufficient_balance
import vankoo.shared.generated.resources.finance_failure_declined_body
import vankoo.shared.generated.resources.finance_failure_declined_title
import vankoo.shared.generated.resources.finance_failure_expired_body
import vankoo.shared.generated.resources.finance_failure_expired_title
import vankoo.shared.generated.resources.finance_failure_invalid_method_body
import vankoo.shared.generated.resources.finance_failure_invalid_method_title
import vankoo.shared.generated.resources.finance_failure_provider_body
import vankoo.shared.generated.resources.finance_failure_provider_title
import vankoo.shared.generated.resources.finance_failure_unknown_body
import vankoo.shared.generated.resources.finance_failure_unknown_title
import vankoo.shared.generated.resources.finance_movement_commission
import vankoo.shared.generated.resources.finance_movement_investment
import vankoo.shared.generated.resources.finance_movement_other
import vankoo.shared.generated.resources.finance_movement_top_up
import vankoo.shared.generated.resources.finance_movement_withdrawal

/**
 * How this context says out loud what went wrong.
 *
 * Same shape as `iamMessage`: the failure travels untranslated and the wording is
 * chosen here, at render time, so a screen already showing an error follows a change
 * of language.
 */
@Composable
fun AppError.financeMessage(): String = when (this) {
    AppError.Network -> stringResource(Res.string.error_network)
    is AppError.Server -> stringResource(Res.string.error_server)
    is AppError.Unexpected -> stringResource(Res.string.error_unexpected)
    is AppError.Business -> when (reason) {
        FinanceFailure.Unauthenticated,
        FinanceFailure.Forbidden,
        -> stringResource(Res.string.iam_error_unauthenticated)
        FinanceFailure.InsufficientBalance -> stringResource(Res.string.finance_error_insufficient_balance)
        // The rest are answers to a request this app never builds by hand — a
        // malformed id, a reused key with a different body — so if one arrives it
        // is a bug on this side, and "something went wrong" is the honest wording.
        else -> stringResource(Res.string.error_unexpected)
    }
}

/** The row title for a movement. */
@Composable
fun MovementKind.label(): String = stringResource(
    when (this) {
        MovementKind.RECARGA -> Res.string.finance_movement_top_up
        MovementKind.INVERSION -> Res.string.finance_movement_investment
        MovementKind.RETIRO -> Res.string.finance_movement_withdrawal
        MovementKind.COMISION -> Res.string.finance_movement_commission
        MovementKind.UNKNOWN -> Res.string.finance_movement_other
    }
)

/** The headline of the failure notice, per normalised reason. */
fun DepositFailureReason?.titleResource(): StringResource = when (this) {
    DepositFailureReason.DECLINED -> Res.string.finance_failure_declined_title
    DepositFailureReason.EXPIRED -> Res.string.finance_failure_expired_title
    DepositFailureReason.INVALID_PAYMENT_METHOD -> Res.string.finance_failure_invalid_method_title
    DepositFailureReason.PROVIDER_ERROR -> Res.string.finance_failure_provider_title
    DepositFailureReason.UNKNOWN, null -> Res.string.finance_failure_unknown_title
}

fun DepositFailureReason?.bodyResource(): StringResource = when (this) {
    DepositFailureReason.DECLINED -> Res.string.finance_failure_declined_body
    DepositFailureReason.EXPIRED -> Res.string.finance_failure_expired_body
    DepositFailureReason.INVALID_PAYMENT_METHOD -> Res.string.finance_failure_invalid_method_body
    DepositFailureReason.PROVIDER_ERROR -> Res.string.finance_failure_provider_body
    DepositFailureReason.UNKNOWN, null -> Res.string.finance_failure_unknown_body
}
