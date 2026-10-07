package com.liquilabs.vankoo.investor.iam.presentation.recover

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.Lucide
import com.liquilabs.vankoo.investor.core.designsystem.VankooTheme
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooButton
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooNotice
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooTextField
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooTextLink
import com.liquilabs.vankoo.investor.iam.presentation.components.AuthScaffold
import com.liquilabs.vankoo.investor.iam.presentation.iamMessage
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import vankoo.shared.generated.resources.Res
import vankoo.shared.generated.resources.iam_error_email_shape
import vankoo.shared.generated.resources.iam_field_email
import vankoo.shared.generated.resources.recover_back
import vankoo.shared.generated.resources.recover_submit
import vankoo.shared.generated.resources.recover_submitting
import vankoo.shared.generated.resources.recover_subtitle
import vankoo.shared.generated.resources.recover_title

/**
 * Asks for a recovery link.
 *
 * It walks forward whenever IAM accepts the request, which it does for any address it
 * can parse — registered or not. That is the endpoint's whole shape, and a client
 * that showed something different for the two cases would undo it.
 */
@Composable
fun RecoverPasswordScreen(
    onSent: (email: String) -> Unit,
    onBack: () -> Unit,
    viewModel: RecoverPasswordViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.sent.collect(onSent)
    }

    val colors = VankooTheme.colors
    val type = VankooTheme.typography

    AuthScaffold {
        VankooTextLink(
            text = stringResource(Res.string.recover_back),
            onClick = onBack,
            leadingIcon = Lucide.ChevronLeft,
        )

        Text(text = stringResource(Res.string.recover_title), style = type.h1, color = colors.textPrimary)
        Text(
            text = stringResource(Res.string.recover_subtitle),
            style = type.body,
            color = colors.textSecondary,
        )

        state.error?.let { error ->
            VankooNotice(text = error.iamMessage(), modifier = Modifier.fillMaxWidth())
        }

        VankooTextField(
            value = state.email,
            onValueChange = viewModel::onEmailChange,
            label = stringResource(Res.string.iam_field_email),
            modifier = Modifier.fillMaxWidth(),
            isError = state.emailMalformed,
            supportingText = stringResource(Res.string.iam_error_email_shape)
                .takeIf { state.emailMalformed },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { viewModel.onSubmit() }),
        )

        VankooButton(
            text = stringResource(
                if (state.submitting) Res.string.recover_submitting else Res.string.recover_submit
            ),
            onClick = viewModel::onSubmit,
            modifier = Modifier.fillMaxWidth(),
            enabled = state.canSubmit,
            loading = state.submitting,
        )
    }
}
