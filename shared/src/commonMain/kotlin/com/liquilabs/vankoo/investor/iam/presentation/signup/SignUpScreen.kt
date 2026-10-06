package com.liquilabs.vankoo.investor.iam.presentation.signup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liquilabs.vankoo.investor.core.designsystem.VankooSpacing
import com.liquilabs.vankoo.investor.core.designsystem.VankooTheme
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooButton
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooCheckbox
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooNotice
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooPasswordField
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooTextField
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooTextLink
import com.liquilabs.vankoo.investor.core.result.AppError
import com.liquilabs.vankoo.investor.iam.domain.model.IamFailure
import com.liquilabs.vankoo.investor.iam.presentation.components.AuthScaffold
import com.liquilabs.vankoo.investor.iam.presentation.iamMessage
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import vankoo.shared.generated.resources.Res
import vankoo.shared.generated.resources.iam_error_email_already_in_use
import vankoo.shared.generated.resources.iam_error_email_shape
import vankoo.shared.generated.resources.iam_field_email
import vankoo.shared.generated.resources.iam_field_password
import vankoo.shared.generated.resources.iam_field_password_confirmation
import vankoo.shared.generated.resources.iam_field_password_hide
import vankoo.shared.generated.resources.iam_field_password_show
import vankoo.shared.generated.resources.sign_up_accept_terms
import vankoo.shared.generated.resources.sign_up_have_account
import vankoo.shared.generated.resources.sign_up_password_hint
import vankoo.shared.generated.resources.sign_up_password_mismatch
import vankoo.shared.generated.resources.sign_up_sign_in_link
import vankoo.shared.generated.resources.sign_up_submit
import vankoo.shared.generated.resources.sign_up_submitting
import vankoo.shared.generated.resources.sign_up_subtitle
import vankoo.shared.generated.resources.sign_up_title

@Composable
fun SignUpScreen(
    onSignedUp: (email: String) -> Unit,
    onSignIn: () -> Unit,
    viewModel: SignUpViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.signedUp.collect(onSignedUp)
    }

    SignUpContent(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onPasswordConfirmationChange = viewModel::onPasswordConfirmationChange,
        onPasswordVisibilityToggle = viewModel::onPasswordVisibilityToggle,
        onTermsAcceptedChange = viewModel::onTermsAcceptedChange,
        onSubmit = viewModel::onSubmit,
        onSignIn = onSignIn,
    )
}

@Composable
private fun SignUpContent(
    state: SignUpUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordConfirmationChange: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onTermsAcceptedChange: (Boolean) -> Unit,
    onSubmit: () -> Unit,
    onSignIn: () -> Unit,
) {
    val colors = VankooTheme.colors
    val type = VankooTheme.typography

    // The one failure that belongs to a field goes under it; everything else is a
    // banner. An address already taken is about the address, and putting it at the
    // top of the form would leave the person hunting for what to change.
    val emailTaken = state.error.isEmailAlreadyInUse()
    val emailError = when {
        state.emailMalformed -> stringResource(Res.string.iam_error_email_shape)
        emailTaken -> stringResource(Res.string.iam_error_email_already_in_use)
        else -> null
    }

    // Sign-up is denser than sign-in — three fields, a checkbox and a footer — so the
    // mockup tightens the rhythm rather than letting the form run off the screen.
    AuthScaffold(topPadding = VankooSpacing.s24, verticalGap = VankooSpacing.s12) {
        Text(text = stringResource(Res.string.sign_up_title), style = type.h1, color = colors.textPrimary)
        Text(
            text = stringResource(Res.string.sign_up_subtitle),
            style = type.body,
            color = colors.textSecondary,
        )

        state.error?.takeUnless { emailTaken }?.let { error ->
            VankooNotice(text = error.iamMessage(), modifier = Modifier.fillMaxWidth())
        }

        VankooTextField(
            value = state.email,
            onValueChange = onEmailChange,
            label = stringResource(Res.string.iam_field_email),
            modifier = Modifier.fillMaxWidth(),
            isError = emailError != null,
            supportingText = emailError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
        )

        VankooPasswordField(
            value = state.password,
            onValueChange = onPasswordChange,
            label = stringResource(Res.string.iam_field_password),
            visible = state.passwordVisible,
            onVisibilityToggle = onPasswordVisibilityToggle,
            showContentDescription = stringResource(Res.string.iam_field_password_show),
            hideContentDescription = stringResource(Res.string.iam_field_password_hide),
            modifier = Modifier.fillMaxWidth(),
            // The rule is always on show; when it is broken the same line turns red,
            // which is one message instead of two that say the same thing.
            supportingText = stringResource(Res.string.sign_up_password_hint),
            isError = state.passwordTooShort,
            imeAction = ImeAction.Next,
        )

        VankooPasswordField(
            value = state.passwordConfirmation,
            onValueChange = onPasswordConfirmationChange,
            label = stringResource(Res.string.iam_field_password_confirmation),
            visible = state.passwordVisible,
            onVisibilityToggle = onPasswordVisibilityToggle,
            showContentDescription = stringResource(Res.string.iam_field_password_show),
            hideContentDescription = stringResource(Res.string.iam_field_password_hide),
            modifier = Modifier.fillMaxWidth(),
            supportingText = stringResource(Res.string.sign_up_password_mismatch)
                .takeIf { state.passwordMismatch },
            isError = state.passwordMismatch,
            keyboardActions = KeyboardActions(onDone = { onSubmit() }),
        )

        VankooCheckbox(
            checked = state.termsAccepted,
            onCheckedChange = onTermsAcceptedChange,
            label = stringResource(Res.string.sign_up_accept_terms),
            modifier = Modifier.fillMaxWidth(),
        )

        VankooButton(
            text = stringResource(
                if (state.submitting) Res.string.sign_up_submitting else Res.string.sign_up_submit
            ),
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth(),
            enabled = state.canSubmit,
            loading = state.submitting,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(VankooSpacing.s4, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.sign_up_have_account),
                style = type.caption,
                color = colors.textMuted,
            )
            VankooTextLink(text = stringResource(Res.string.sign_up_sign_in_link), onClick = onSignIn)
        }
    }
}

private fun AppError?.isEmailAlreadyInUse(): Boolean =
    this is AppError.Business && reason == IamFailure.EmailAlreadyInUse
