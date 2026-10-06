package com.liquilabs.vankoo.investor.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Lucide
import com.liquilabs.vankoo.investor.core.designsystem.VankooShapes
import com.liquilabs.vankoo.investor.core.designsystem.VankooSize
import com.liquilabs.vankoo.investor.core.designsystem.VankooSpacing
import com.liquilabs.vankoo.investor.core.designsystem.VankooTheme

/**
 * A box and a label that read as one control.
 *
 * The whole row toggles rather than the 18dp box alone: the box is the size Figma
 * draws, which is well under the 44dp finger floor, and a label nobody can tap is a
 * label that makes the box the only target.
 */
@Composable
fun VankooCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = VankooTheme.colors

    Row(
        modifier = modifier.toggleable(
            value = checked,
            enabled = enabled,
            role = Role.Checkbox,
            onValueChange = onCheckedChange,
        ),
        horizontalArrangement = Arrangement.spacedBy(VankooSpacing.s8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(BOX_SIZE)
                .clip(VankooShapes.sm)
                .background(if (checked) colors.actionPrimaryBg else colors.surfaceRaised)
                .border(
                    width = VankooSize.borderHairline,
                    color = if (checked) colors.actionPrimaryBg else colors.borderStrong,
                    shape = VankooShapes.sm,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (checked) {
                Icon(
                    imageVector = Lucide.Check,
                    // The state is already announced by the toggleable role.
                    contentDescription = null,
                    tint = colors.actionPrimaryFg,
                    modifier = Modifier.size(CHECK_SIZE),
                )
            }
        }

        Text(
            text = label,
            style = VankooTheme.typography.body,
            color = if (enabled) colors.textPrimary else colors.textDisabled,
        )
    }
}

/** 18dp, from the Figma component — smaller than an icon, larger than the tick. */
private val BOX_SIZE = 18.dp
private val CHECK_SIZE = 12.dp
