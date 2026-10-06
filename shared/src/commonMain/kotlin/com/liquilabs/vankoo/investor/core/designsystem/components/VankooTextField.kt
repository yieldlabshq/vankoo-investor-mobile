package com.liquilabs.vankoo.investor.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.VisualTransformation
import com.liquilabs.vankoo.investor.core.designsystem.VankooShapes
import com.liquilabs.vankoo.investor.core.designsystem.VankooSize
import com.liquilabs.vankoo.investor.core.designsystem.VankooSpacing
import com.liquilabs.vankoo.investor.core.designsystem.VankooTheme

/**
 * A labelled text field, as the Figma `Input` component draws it.
 *
 * Material's OutlinedTextField was tried first and cannot be bent into this shape:
 * its label floats into the border and its height is decided by its own padding.
 * Here the label is a sibling above the box, and the box is exactly
 * [VankooSize.controlMd] tall — the Mobile column of the density collection — so a
 * field is the same height on every screen regardless of what it contains.
 *
 * Error is a border, not a colour swap: the box keeps its surface and gains a
 * two-pixel `state/error/fg` outline, which is what the mockup shows and what keeps
 * the value readable while it is being corrected.
 */
@Composable
fun VankooTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    supportingText: String? = null,
    isError: Boolean = false,
    enabled: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = VankooTheme.colors
    val type = VankooTheme.typography

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(VankooSpacing.s8),
    ) {
        Text(
            text = label,
            style = type.bodyStrong,
            color = if (enabled) colors.textPrimary else colors.textDisabled,
        )

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(VankooSize.controlMd),
            enabled = enabled,
            singleLine = true,
            textStyle = type.body.copy(
                color = if (enabled) colors.textPrimary else colors.textDisabled,
            ),
            cursorBrush = SolidColor(colors.borderFocus),
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            decorationBox = { field ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(VankooShapes.md)
                        .background(if (enabled) colors.surfaceRaised else colors.surfaceSunken)
                        .border(
                            width = if (isError) VankooSize.borderThick else VankooSize.borderHairline,
                            color = if (isError) colors.stateErrorFg else colors.borderStrong,
                            shape = VankooShapes.md,
                        )
                        .padding(horizontal = VankooSpacing.s12),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (value.isEmpty() && placeholder != null) {
                            Text(text = placeholder, style = type.body, color = colors.textMuted)
                        }
                        field()
                    }
                    if (trailing != null) {
                        Spacer(Modifier.width(VankooSpacing.s8))
                        trailing()
                    }
                }
            },
        )

        if (supportingText != null) {
            Text(
                text = supportingText,
                style = type.caption,
                color = if (isError) colors.stateErrorFg else colors.textMuted,
            )
        }
    }
}
