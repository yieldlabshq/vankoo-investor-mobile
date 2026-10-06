package com.liquilabs.vankoo.investor.core.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.composables.icons.lucide.Eye
import com.composables.icons.lucide.EyeOff
import com.composables.icons.lucide.Lucide
import com.liquilabs.vankoo.investor.core.designsystem.VankooSize
import com.liquilabs.vankoo.investor.core.designsystem.VankooTheme

/**
 * A password field: [VankooTextField] plus the eye that reveals what was typed.
 *
 * Visibility is state the caller owns rather than state hidden in here, because a
 * form with two password fields decides for itself whether they reveal together.
 */
@Composable
fun VankooPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    visible: Boolean,
    onVisibilityToggle: () -> Unit,
    showContentDescription: String,
    hideContentDescription: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    isError: Boolean = false,
    enabled: Boolean = true,
    imeAction: ImeAction = ImeAction.Done,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    VankooTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        supportingText = supportingText,
        isError = isError,
        enabled = enabled,
        visualTransformation = if (visible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = imeAction,
        ),
        keyboardActions = keyboardActions,
        trailing = {
            Icon(
                imageVector = if (visible) Lucide.EyeOff else Lucide.Eye,
                contentDescription = if (visible) hideContentDescription else showContentDescription,
                tint = VankooTheme.colors.textMuted,
                modifier = Modifier
                    .size(VankooSize.iconLg)
                    .clickable(enabled = enabled, onClick = onVisibilityToggle),
            )
        },
    )
}
