package com.liquilabs.vankoo.investor.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.User
import com.liquilabs.vankoo.investor.core.designsystem.VankooSpacing
import com.liquilabs.vankoo.investor.core.designsystem.VankooTheme
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooCard
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooEmptyState
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooMobileHeader
import org.jetbrains.compose.resources.stringResource
import vankoo.shared.generated.resources.Res
import vankoo.shared.generated.resources.app_name
import vankoo.shared.generated.resources.placeholder_body
import vankoo.shared.generated.resources.placeholder_title
import vankoo.shared.generated.resources.profile_title

/**
 * Stand-in for «Mi perfil».
 *
 * Blocked on the backend rather than on design: Profile has no way to find one's own
 * profile from the user id in the token (no `findByUserId`), so there is nothing to
 * fetch yet. Signing out stays on Home until this screen is real.
 */
@Composable
fun ProfileScreen() {
    val colors = VankooTheme.colors

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
            verticalArrangement = Arrangement.spacedBy(VankooSpacing.s16),
        ) {
            Text(
                text = stringResource(Res.string.profile_title),
                style = VankooTheme.typography.h1,
                color = colors.textPrimary,
            )
            VankooCard {
                VankooEmptyState(
                    icon = Lucide.User,
                    title = stringResource(Res.string.placeholder_title),
                    description = stringResource(Res.string.placeholder_body),
                )
            }
        }
    }
}
