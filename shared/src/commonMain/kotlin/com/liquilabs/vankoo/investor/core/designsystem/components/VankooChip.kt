package com.liquilabs.vankoo.investor.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import com.liquilabs.vankoo.investor.core.designsystem.VankooShapes
import com.liquilabs.vankoo.investor.core.designsystem.VankooSize
import com.liquilabs.vankoo.investor.core.designsystem.VankooSpacing
import com.liquilabs.vankoo.investor.core.designsystem.VankooTheme

/**
 * A pill that offers a value — a suggested amount, a filter.
 *
 * Raised fill with a `border/strong` outline, the same recipe as the ghost button: it
 * is an action, so it stands proud of the surface. Selected swaps the fill for the
 * inverse surface, which is how the Mercado filters mark the active one.
 */
@Composable
fun VankooChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
) {
    val colors = VankooTheme.colors

    Text(
        text = text,
        style = VankooTheme.typography.caption,
        color = if (selected) colors.textInverse else colors.textMuted,
        modifier = modifier
            .clip(VankooShapes.full)
            .background(if (selected) colors.surfaceInverse else colors.surfaceRaised)
            .border(
                width = VankooSize.borderHairline,
                color = if (selected) colors.surfaceInverse else colors.borderStrong,
                shape = VankooShapes.full,
            )
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = VankooSpacing.s12, vertical = VankooSpacing.s8),
    )
}
