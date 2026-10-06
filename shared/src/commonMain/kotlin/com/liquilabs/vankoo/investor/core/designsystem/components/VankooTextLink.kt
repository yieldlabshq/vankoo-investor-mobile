package com.liquilabs.vankoo.investor.core.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import com.liquilabs.vankoo.investor.core.designsystem.VankooSize
import com.liquilabs.vankoo.investor.core.designsystem.VankooSpacing
import com.liquilabs.vankoo.investor.core.designsystem.VankooTheme

/**
 * Inline navigation inside a sentence or under a form.
 *
 * It is a link and not a ghost button: no surface, no padding, no height token. The
 * colour is `text/link` — green/700 in light, which is the token that passes contrast
 * on a light ground, and never the brand green.
 */
@Composable
fun VankooTextLink(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
) {
    Row(
        modifier = modifier.clickable(role = Role.Button, onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(VankooSpacing.s4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = VankooTheme.colors.textLink,
                modifier = Modifier.size(VankooSize.iconSm),
            )
        }
        Text(
            text = text,
            style = VankooTheme.typography.captionStrong,
            color = VankooTheme.colors.textLink,
        )
    }
}
