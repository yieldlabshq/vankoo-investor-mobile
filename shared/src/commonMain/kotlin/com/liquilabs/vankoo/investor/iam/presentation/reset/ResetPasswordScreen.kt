package com.liquilabs.vankoo.investor.iam.presentation.reset

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liquilabs.vankoo.investor.core.designsystem.VankooTheme
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooButton
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooButtonVariant
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooNotice
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooPasswordField
import com.liquilabs.vankoo.investor.iam.presentation.components.AuthScaffold
import com.liquilabs.vankoo.investor.iam.presentation.iamMessage
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import vankoo.shared.generated.resources.Res
import vankoo.shared.generated.resources.iam_field_password
import vankoo.shared.generated.resources.iam_field_password_confirmation
import vankoo.shared.generated.resources.iam_field_password_hide
import vankoo.shared.generated.resources.iam_field_password_show
import vankoo.shared.generated.resources.reset_ask_for_another
import vankoo.shared.generated.resources.reset_missing_token
import vankoo.shared.generated.resources.reset_submit
import vankoo.shared.generated.resources.reset_submitting
import vankoo.shared.generated.resources.reset_subtitle
import vankoo.shared.generated.resources.reset_title
import vankoo.shared.generated.resources.sign_up_password_hint
import vankoo.shared.generated.resources.sign_up_password_mismatch

/**
 * Where the link from the recovery email lands.
 *
 * **This screen has no mockup.** The IAM set in Figma has four, drawn while the
 * backend it needs did not exist. It is assembled from the same scaffold and the same
 * components as its neighbours, and it should be designed properly before anyone
 * calls it done.
 */
@Composable
fun ResetPasswordScreen(
    token: String,
    onPasswordChanged: () -> Unit,
    onAskForAnother: () -> Unit,
    viewModel: ResetPasswordViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = VankooTheme.colors
    val type = VankooTheme.typography

    LaunchedEffect(Unit) {
        viewModel.changed.collect { onPasswordChanged() }
    }

    AuthScaffold {
        Text(text = stringResource(Res.string.reset_title), style = type.h1, color = colors.textPrimary)

        // A link with no token is a broken link, not a rejected one: there is nothing
        // to send, so the form would only collect a password it could not use.
        if (token.isBlank()) {
            VankooNotice(
                text = stringResource(Res.string.reset_missing_token),
                modifier = Modifier.fillMaxWidth(),
            )
            VankooButton(
                text = stringResource(Res.string.reset_ask_for_another),
                onClick = onAskForAnother,
                modifier = Modifier.fillMaxWidth(),
                variant = VankooButtonVariant.Secondary,
            )
            return@AuthScaffold
        }

        Text(
            text = stringResource(Res.string.reset_subtitle),
            style = type.body,
            color = colors.textSecondary,
        )

        state.error?.let { error ->
            VankooNotice(text = error.iamMessage(), modifier = Modifier.fillMaxWidth())
        }

        VankooPasswordField(
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            label = stringResource(Res.string.iam_field_password),
            visible = state.passwordVisible,
            onVisibilityToggle = viewModel::onPasswordVisibilityToggle,
            showContentDescription = stringResource(Res.string.iam_field_password_show),
            hideContentDescription = stringResource(Res.string.iam_field_password_hide),
            modifier = Modifier.fillMaxWidth(),
            supportingText = stringResource(Res.string.sign_up_password_hint),
            isError = state.passwordTooShort,
            imeAction = ImeAction.Next,
        )

        VankooPasswordField(
            value = state.passwordConfirmation,
            onValueChange = viewModel::onPasswordConfirmationChange,
            label = stringResource(Res.string.iam_field_password_confirmation),
            visible = state.passwordVisible,
            onVisibilityToggle = viewModel::onPasswordVisibilityToggle,
            showContentDescription = stringResource(Res.string.iam_field_password_show),
            hideContentDescription = stringResource(Res.string.iam_field_password_hide),
            modifier = Modifier.fillMaxWidth(),
            supportingText = stringResource(Res.string.sign_up_password_mismatch)
                .takeIf { state.passwordMismatch },
            isError = state.passwordMismatch,
            keyboardActions = KeyboardActions(onDone = { viewModel.onSubmit(token) }),
        )

        VankooButton(
            text = stringResource(
                if (state.submitting) Res.string.reset_submitting else Res.string.reset_submit
            ),
            onClick = { viewModel.onSubmit(token) },
            modifier = Modifier.fillMaxWidth(),
            enabled = state.canSubmit,
            loading = state.submitting,
        )
    }
}
