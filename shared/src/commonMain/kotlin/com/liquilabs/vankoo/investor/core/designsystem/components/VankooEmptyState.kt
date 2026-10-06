package com.liquilabs.vankoo.investor.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.liquilabs.vankoo.investor.core.designsystem.VankooSize
import com.liquilabs.vankoo.investor.core.designsystem.VankooSpacing
import com.liquilabs.vankoo.investor.core.designsystem.VankooTheme

/**
 * What a list shows when it has nothing to list, as the Figma `EmptyState`.
 *
 * The icon is a parameter because not every empty is a missing document: the wallet
 * without movements and the 404 each ask for their own. The box behind it is
 * `surface/sunken`, the one neutral that reads as "nothing here" on a raised card.
 */
@Composable
fun VankooEmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    val colors = VankooTheme.colors
    val type = VankooTheme.typography

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = VankooSpacing.s48),
        verticalArrangement = Arrangement.spacedBy(VankooSpacing.s12),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(ICON_BOX_SIZE)
                .background(color = colors.surfaceSunken, shape = CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                // The title right under it is the message.
                contentDescription = null,
                tint = colors.textPrimary,
                modifier = Modifier.size(VankooSize.iconLg),
            )
        }
        Text(
            text = title,
            style = type.h3,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = description,
            style = type.body,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

private val ICON_BOX_SIZE = 56.dp
