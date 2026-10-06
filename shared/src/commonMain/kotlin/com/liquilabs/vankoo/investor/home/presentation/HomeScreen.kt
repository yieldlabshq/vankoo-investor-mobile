package com.liquilabs.vankoo.investor.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liquilabs.vankoo.investor.core.designsystem.VankooSpacing
import com.liquilabs.vankoo.investor.core.designsystem.VankooTheme
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooButton
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooMobileHeader
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import vankoo.shared.generated.resources.Res
import vankoo.shared.generated.resources.app_name
import vankoo.shared.generated.resources.home_sign_out
import vankoo.shared.generated.resources.home_signed_in_as

/**
 * Stand-in for the investor home.
 *
 * It shows who is signed in and lets them out again, which is the whole point
 * for now: it closes the loop the sign-in slice has to prove. The real home is the
 * next slice after the wallet and the marketplace, which it is a summary of.
 */
@Composable
fun HomeScreen(
    onSignedOut: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val email by viewModel.signedInAs.collectAsStateWithLifecycle()
    val colors = VankooTheme.colors
    val type = VankooTheme.typography

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surfaceBase),
    ) {
        VankooMobileHeader(title = stringResource(Res.string.app_name))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(VankooSpacing.pageMargin),
            verticalArrangement = Arrangement.spacedBy(VankooSpacing.s8, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(Res.string.home_signed_in_as),
                style = type.overline,
                color = colors.textMuted,
            )
            Text(
                text = email.orEmpty(),
                style = type.bodyStrong,
                color = colors.textSecondary,
                modifier = Modifier.padding(bottom = VankooSpacing.s16),
            )
            VankooButton(
                text = stringResource(Res.string.home_sign_out),
                onClick = {
                    viewModel.onSignOut()
                    onSignedOut()
                },
            )
        }
    }
}
