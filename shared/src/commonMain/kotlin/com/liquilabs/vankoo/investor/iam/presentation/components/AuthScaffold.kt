package com.liquilabs.vankoo.investor.iam.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.liquilabs.vankoo.investor.core.designsystem.VankooSpacing
import com.liquilabs.vankoo.investor.core.designsystem.VankooTheme
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooBrandPanel
import org.jetbrains.compose.resources.stringResource
import vankoo.shared.generated.resources.Res
import vankoo.shared.generated.resources.app_tagline

/**
 * The shape all four entry screens share: the brand band, then the form.
 *
 * It lives in this context rather than in the design system, next to `AuthLayout.tsx`
 * on the web and for the same reason — the band belongs to signing in, not to Vankoo
 * at large. Once there is an app shell behind the login, nothing else will use it.
 *
 * The whole thing scrolls and lifts for the keyboard, because a 250dp band plus a
 * form plus a keyboard does not fit on a phone, and the field being typed into is the
 * one that must stay visible.
 */
@Composable
fun AuthScaffold(
    modifier: Modifier = Modifier,
    topPadding: Dp = VankooSpacing.s24,
    verticalGap: Dp = VankooSpacing.s16,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VankooTheme.colors.surfaceBase)
            .verticalScroll(rememberScrollState())
            .imePadding(),
    ) {
        VankooBrandPanel(tagline = stringResource(Res.string.app_tagline))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = VankooSpacing.pageMargin)
                .padding(top = topPadding, bottom = VankooSpacing.s24)
                .windowInsetsPadding(WindowInsets.navigationBars),
            verticalArrangement = Arrangement.spacedBy(verticalGap),
            content = content,
        )
    }
}
