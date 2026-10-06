package com.liquilabs.vankoo.investor.iam.presentation.recover

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.liquilabs.vankoo.investor.core.designsystem.VankooTheme
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooButton
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooButtonVariant
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooSuccessMark
import com.liquilabs.vankoo.investor.iam.presentation.components.AuthScaffold
import org.jetbrains.compose.resources.stringResource
import vankoo.shared.generated.resources.Res
import vankoo.shared.generated.resources.recover_back
import vankoo.shared.generated.resources.recover_sent_body
import vankoo.shared.generated.resources.recover_sent_title

/**
 * Confirms nothing, deliberately.
 *
 * The wording says "if an account exists for that address" because saying otherwise
 * would turn this screen into a way of finding out who has an account — the same
 * reason the two sign-in failures answer identically. It is also literally true here:
 * nothing was sent, and nothing can be until IAM grows the endpoint.
 */
@Composable
fun RecoverPasswordSentScreen(
    email: String,
    onBackToSignIn: () -> Unit,
) {
    val colors = VankooTheme.colors
    val type = VankooTheme.typography

    AuthScaffold {
        VankooSuccessMark()

        Text(
            text = stringResource(Res.string.recover_sent_title),
            style = type.h1,
            color = colors.textPrimary,
        )
        Text(
            text = stringResource(Res.string.recover_sent_body, email),
            style = type.body,
            color = colors.textSecondary,
        )

        VankooButton(
            text = stringResource(Res.string.recover_back),
            onClick = onBackToSignIn,
            modifier = Modifier.fillMaxWidth(),
            variant = VankooButtonVariant.Secondary,
        )
    }
}
