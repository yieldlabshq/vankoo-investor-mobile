package com.liquilabs.vankoo.investor.iam.presentation

import androidx.compose.runtime.Composable
import com.liquilabs.vankoo.investor.core.result.AppError
import com.liquilabs.vankoo.investor.iam.domain.model.IamFailure
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import vankoo.shared.generated.resources.Res
import vankoo.shared.generated.resources.error_network
import vankoo.shared.generated.resources.error_server
import vankoo.shared.generated.resources.error_unexpected
import vankoo.shared.generated.resources.iam_error_email_already_in_use
import vankoo.shared.generated.resources.iam_error_forbidden
import vankoo.shared.generated.resources.iam_error_invalid_credentials
import vankoo.shared.generated.resources.iam_error_invalid_password_reset_token
import vankoo.shared.generated.resources.iam_error_invalid_request
import vankoo.shared.generated.resources.iam_error_role_not_allowed
import vankoo.shared.generated.resources.iam_error_unauthenticated
import vankoo.shared.generated.resources.iam_error_user_not_found
import vankoo.shared.generated.resources.iam_error_validation_failed

/**
 * How this context says out loud what went wrong.
 *
 * The wording lives in presentation and the failure stays untranslated all the way
 * from the service, which is what lets a screen that is already showing an error
 * follow a change of language. A string chosen when the call failed would be frozen
 * in whatever language was current at that moment.
 */
@Composable
fun AppError.iamMessage(): String = when (this) {
    AppError.Network -> stringResource(Res.string.error_network)
    is AppError.Server -> stringResource(Res.string.error_server)
    is AppError.Unexpected -> stringResource(Res.string.error_unexpected)
    is AppError.Business -> when (val failure = reason) {
        is IamFailure -> stringResource(failure.messageResource())
        // Another context's reason reached a screen of this one: nothing sensible to
        // say, but nothing that should crash either.
        else -> stringResource(Res.string.error_unexpected)
    }
}

private fun IamFailure.messageResource(): StringResource = when (this) {
    IamFailure.EmailAlreadyInUse -> Res.string.iam_error_email_already_in_use
    IamFailure.InvalidCredentials -> Res.string.iam_error_invalid_credentials
    IamFailure.InvalidPasswordResetToken -> Res.string.iam_error_invalid_password_reset_token
    IamFailure.RoleNotAllowed -> Res.string.iam_error_role_not_allowed
    IamFailure.ValidationFailed -> Res.string.iam_error_validation_failed
    IamFailure.InvalidRequest -> Res.string.iam_error_invalid_request
    IamFailure.UserNotFound -> Res.string.iam_error_user_not_found
    IamFailure.Unauthenticated -> Res.string.iam_error_unauthenticated
    IamFailure.Forbidden -> Res.string.iam_error_forbidden
}
