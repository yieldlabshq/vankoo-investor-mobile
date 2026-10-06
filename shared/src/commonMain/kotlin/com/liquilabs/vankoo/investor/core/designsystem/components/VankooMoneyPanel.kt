package com.liquilabs.vankoo.investor.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.liquilabs.vankoo.investor.core.designsystem.VankooShapes
import com.liquilabs.vankoo.investor.core.designsystem.VankooSize
import com.liquilabs.vankoo.investor.core.designsystem.VankooSpacing
import com.liquilabs.vankoo.investor.core.designsystem.VankooTheme

/**
 * The brand surface that holds money, as the Figma `MoneyPanel` in its Vertical form.
 *
 * Navy in both themes, so everything on it uses the fixed `decor/panel-*` foregrounds
 * and never `text/inverse` — an inverse token flips with the theme and would vanish
 * here in dark mode. One per screen: it is the figure the screen is about.
 *
 * Block A is the headline amount; block B, when given, is a second figure under a
 * divider. The action slot takes whatever the screen wants under the numbers, which
 * in practice is one button.
 */
@Composable
fun VankooMoneyPanel(
    primaryLabel: String,
    primaryValue: String,
    modifier: Modifier = Modifier,
    primaryCaption: String? = null,
    secondaryLabel: String? = null,
    secondaryValue: String? = null,
    action: (@Composable () -> Unit)? = null,
) {
    val colors = VankooTheme.colors
    val type = VankooTheme.typography

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(VankooShapes.xl)
            .drawBehind {
                drawRect(colors.decorPanelFrom)
                // The single green light of the mobile panel: a 420 disc whose centre
                // sits past the bottom-right corner, so only its glow reaches in. Drawn
                // as a gradient to transparent instead of the 240 layer blur in Figma.
                val radius = size.width * LIGHT_RADIUS
                val centre = Offset(size.width * LIGHT_X, size.height + radius * LIGHT_Y_OVERHANG)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(colors.decorLightPrimary.copy(alpha = LIGHT_ALPHA), Color.Transparent),
                        center = centre,
                        radius = radius,
                    ),
                    radius = radius,
                    center = centre,
                )
            }
            .padding(VankooSpacing.cardPadding),
        verticalArrangement = Arrangement.spacedBy(VankooSpacing.s16),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(VankooSpacing.s8)) {
            Text(text = primaryLabel, style = type.caption, color = colors.decorPanelFgMuted)
            Text(text = primaryValue, style = type.numericXl, color = colors.decorPanelFg)
            if (primaryCaption != null) {
                Text(text = primaryCaption, style = type.caption, color = colors.decorPanelFgMuted)
            }
        }

        if (secondaryLabel != null && secondaryValue != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(VankooSize.borderHairline)
                    .drawBehind { drawRect(colors.decorPanelFg.copy(alpha = DIVIDER_ALPHA)) },
            )
            Column(verticalArrangement = Arrangement.spacedBy(VankooSpacing.s8)) {
                Text(text = secondaryLabel, style = type.caption, color = colors.decorPanelFgMuted)
                Text(text = secondaryValue, style = type.numeric, color = colors.decorPanelFg)
            }
        }

        if (action != null) {
            action()
        }
    }
}

// The light in Figma is a 420 disc at (140, 120) on a 358-wide panel. Its centre is
// therefore 350 from the left and well below the panel's own bottom edge.
private const val LIGHT_RADIUS = 420f / 358f / 2f
private const val LIGHT_X = 350f / 358f
private const val LIGHT_Y_OVERHANG = 0.35f
private const val LIGHT_ALPHA = 0.16f
private const val DIVIDER_ALPHA = 0.24f
