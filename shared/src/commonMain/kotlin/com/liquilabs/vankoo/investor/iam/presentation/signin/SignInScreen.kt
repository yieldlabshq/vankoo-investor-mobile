package com.liquilabs.vankoo.investor.iam.presentation.signin

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
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooNotice
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooPasswordField
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooTextField
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooTextLink
import com.liquilabs.vankoo.investor.iam.presentation.components.AuthScaffold
import com.liquilabs.vankoo.investor.iam.presentation.iamMessage
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import vankoo.shared.generated.resources.Res
import vankoo.shared.generated.resources.iam_error_email_shape
import vankoo.shared.generated.resources.iam_field_email
import vankoo.shared.generated.resources.iam_field_password
import vankoo.shared.generated.resources.iam_field_password_hide
import vankoo.shared.generated.resources.iam_field_password_show
import vankoo.shared.generated.resources.sign_in_create_one
import vankoo.shared.generated.resources.sign_in_forgot_password
import vankoo.shared.generated.resources.sign_in_no_account
import vankoo.shared.generated.resources.sign_in_submit
import vankoo.shared.generated.resources.sign_in_submitting
import vankoo.shared.generated.resources.sign_in_subtitle
import vankoo.shared.generated.resources.sign_in_title

@Composable
fun SignInScreen(
    onSignedIn: () -> Unit,
    onCreateAccount: () -> Unit,
    onForgotPassword: () -> Unit,
    prefilledEmail: String? = null,
    viewModel: SignInViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Filled once per address, not on every recomposition, so it never fights with
    // someone editing what it put there.
    LaunchedEffect(prefilledEmail) {
        if (prefilledEmail != null) viewModel.onEmailChange(prefilledEmail)
    }

    LaunchedEffect(Unit) {
        viewModel.signedIn.collect { onSignedIn() }
    }

    SignInContent(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onPasswordVisibilityToggle = viewModel::onPasswordVisibilityToggle,
        onSubmit = viewModel::onSubmit,
        onCreateAccount = onCreateAccount,
        onForgotPassword = onForgotPassword,
    )
}

/**
 * The screen without its ViewModel, so it can be previewed and later tested by
 * handing it a state instead of a container.
 */
@Composable
private fun SignInContent(
    state: SignInUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onSubmit: () -> Unit,
    onCreateAccount: () -> Unit,
    onForgotPassword: () -> Unit,
) {
    val colors = VankooTheme.colors
    val type = VankooTheme.typography

    AuthScaffold {
        Text(text = stringResource(Res.string.sign_in_title), style = type.h1, color = colors.textPrimary)
        Text(
            text = stringResource(Res.string.sign_in_subtitle),
            style = type.body,
            color = colors.textSecondary,
        )

        // Signing in fails as a whole or not at all — the service answers the same
        // 401 whether the account is unknown or the password is wrong, on purpose —
        // so there is no field to hang the message under.
        state.error?.let { error ->
            VankooNotice(text = error.iamMessage(), modifier = Modifier.fillMaxWidth())
        }

        VankooTextField(
            value = state.email,
            onValueChange = onEmailChange,
            label = stringResource(Res.string.iam_field_email),
            modifier = Modifier.fillMaxWidth(),
            isError = state.emailMalformed,
            supportingText = stringResource(Res.string.iam_error_email_shape)
                .takeIf { state.emailMalformed },
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
            keyboardActions = KeyboardActions(onDone = { onSubmit() }),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            VankooTextLink(
                text = stringResource(Res.string.sign_in_forgot_password),
                onClick = onForgotPassword,
            )
        }

        VankooButton(
            text = stringResource(
                if (state.submitting) Res.string.sign_in_submitting else Res.string.sign_in_submit
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
                text = stringResource(Res.string.sign_in_no_account),
                style = type.caption,
                color = colors.textMuted,
            )
            VankooTextLink(
                text = stringResource(Res.string.sign_in_create_one),
                onClick = onCreateAccount,
            )
        }
    }
}
