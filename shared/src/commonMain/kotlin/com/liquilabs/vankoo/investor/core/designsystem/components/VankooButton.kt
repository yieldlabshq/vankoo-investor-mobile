package com.liquilabs.vankoo.investor.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.liquilabs.vankoo.investor.core.designsystem.VankooShapes
import com.liquilabs.vankoo.investor.core.designsystem.VankooSize
import com.liquilabs.vankoo.investor.core.designsystem.VankooSpacing
import com.liquilabs.vankoo.investor.core.designsystem.VankooTheme

/**
 * Which of the three buttons this is.
 *
 * Secondary is navy, not a lighter green: the mockups use it where the action leaves
 * a flow rather than completing it — "back to sign in" on the sent screen — and a
 * second green would read as a second way forward.
 *
 * Ghost is the tertiary: raised fill and a `border/strong` outline, so it stands
 * proud of the surface the way a button should while a field sinks into it. Being
 * opaque, it reads the same on any surface, dot grid behind it or not.
 */
enum class VankooButtonVariant { Primary, Secondary, Ghost }

/**
 * A filled action.
 *
 * Height comes from the control tokens rather than from padding, so a button is
 * one of the three defined sizes instead of whatever its text happens to add up
 * to. The shape is passed explicitly because ButtonDefaults.shape resolves to a
 * Material token that is not one of the five Shapes slots, so filling
 * MaterialTheme.shapes does not reach it.
 */
@Composable
fun VankooButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    variant: VankooButtonVariant = VankooButtonVariant.Primary,
    height: Dp = VankooSize.controlLg,
) {
    val colors = VankooTheme.colors
    val container = when (variant) {
        VankooButtonVariant.Primary -> colors.actionPrimaryBg
        VankooButtonVariant.Secondary -> colors.actionSecondaryBg
        VankooButtonVariant.Ghost -> colors.surfaceRaised
    }
    val content = when (variant) {
        VankooButtonVariant.Primary -> colors.actionPrimaryFg
        VankooButtonVariant.Secondary -> colors.actionSecondaryFg
        VankooButtonVariant.Ghost -> colors.actionGhostFg
    }
    val border = when (variant) {
        VankooButtonVariant.Ghost -> BorderStroke(VankooSize.borderHairline, colors.borderStrong)
        else -> null
    }

    Button(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = height),
        enabled = enabled && !loading,
        shape = VankooShapes.md,
        border = border,
        colors = ButtonDefaults.buttonColors(
            containerColor = container,
            contentColor = content,
            disabledContainerColor = colors.actionDisabledBg,
            disabledContentColor = colors.actionDisabledFg,
        ),
        contentPadding = PaddingValues(horizontal = VankooSize.controlPaddingX),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(VankooSpacing.s8),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // The mockup keeps the label while loading and puts the spinner beside
            // it, so the button says what it is doing rather than going blank.
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(VankooSize.iconMd),
                    // A loading button is a disabled button, so the spinner takes the
                    // disabled foreground and not the variant's own.
                    color = colors.actionDisabledFg,
                    strokeWidth = 2.dp,
                )
            }
            Text(text = text, style = VankooTheme.typography.bodyStrong)
        }
    }
}
