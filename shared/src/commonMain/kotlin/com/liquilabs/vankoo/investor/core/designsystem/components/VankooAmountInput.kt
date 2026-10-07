package com.liquilabs.vankoo.investor.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import com.liquilabs.vankoo.investor.core.designsystem.VankooShapes
import com.liquilabs.vankoo.investor.core.designsystem.VankooSize
import com.liquilabs.vankoo.investor.core.designsystem.VankooSpacing
import com.liquilabs.vankoo.investor.core.designsystem.VankooTheme

/**
 * The field where the number is the hero, as the Figma `AmountInput`.
 *
 * Not a [VankooTextField]: that one is 44 tall and made for data. Here the value is
 * set in Numeric XL with the currency symbol before it, smaller and muted, and the box
 * grows around them. `surface/field` fill like everything that receives content; the
 * `border/strong` hairline is what actually draws it.
 *
 * There is no label: the screen's title already asks the question («¿Cuánto quieres
 * recargar?») and repeating it inside the field would be noise. [supportingText] is
 * the line under the box — the current balance, or what went wrong.
 */
@Composable
fun VankooAmountInput(
    value: String,
    onValueChange: (String) -> Unit,
    currencySymbol: String,
    modifier: Modifier = Modifier,
    placeholder: String = "0.00",
    supportingText: String? = null,
    isError: Boolean = false,
    enabled: Boolean = true,
) {
    val colors = VankooTheme.colors
    val type = VankooTheme.typography

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(VankooSpacing.s8),
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = true,
            textStyle = type.numericXl.copy(color = colors.textPrimary),
            cursorBrush = SolidColor(colors.borderFocus),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            decorationBox = { field ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(VankooShapes.lg)
                        .background(colors.surfaceField)
                        .border(
                            width = if (isError) VankooSize.borderThick else VankooSize.borderHairline,
                            color = if (isError) colors.stateErrorFg else colors.borderStrong,
                            shape = VankooShapes.lg,
                        )
                        .padding(horizontal = VankooSpacing.s24, vertical = VankooSpacing.s16),
                    horizontalArrangement = Arrangement.spacedBy(VankooSpacing.s8),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = currencySymbol, style = type.numeric, color = colors.textMuted)
                    Box(modifier = Modifier.weight(1f)) {
                        if (value.isEmpty()) {
                            Text(text = placeholder, style = type.numericXl, color = colors.textMuted)
                        }
                        field()
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
