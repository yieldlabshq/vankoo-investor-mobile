package com.liquilabs.vankoo.investor.core.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.liquilabs.vankoo.investor.core.designsystem.VankooColors
import com.liquilabs.vankoo.investor.core.designsystem.VankooSpacing
import com.liquilabs.vankoo.investor.core.designsystem.VankooTheme
import com.liquilabs.vankoo.investor.core.designsystem.rememberVankooWordmark

/**
 * The band of brand at the top of every entry screen.
 *
 * It is navy in both themes, which is why everything on it uses `decor/panel-fg`
 * instead of `text/inverse`: an inverse token flips with the theme, and in dark mode
 * that put navy text on a navy panel — 1.92:1, unreadable. A fixed surface needs a
 * fixed foreground.
 *
 * The decoration is structure, not ornament: a radial wash and two out-of-frame green
 * lights, both well under the 20% opacity the expression rules cap decorative green
 * at, and never behind the text.
 */
@Composable
fun VankooBrandPanel(
    tagline: String,
    modifier: Modifier = Modifier,
    height: Dp = BAND_HEIGHT,
) {
    val colors = VankooTheme.colors
    val wordmark = rememberVankooWordmark(letters = colors.decorPanelFg)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            // Compose does not clip to bounds on its own, and both the wash and the
            // lights are drawn far larger than the band on purpose — they are a
            // window on to a 560x1024 panel. Without this they paint over the form.
            .clipToBounds()
            .drawBehind { drawBrandDecor(colors) },
    ) {
        Column(
            // The band runs under the status bar; its content does not.
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars),
            verticalArrangement = Arrangement.spacedBy(VankooSpacing.s8, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                imageVector = wordmark,
                contentDescription = WORDMARK_DESCRIPTION,
                modifier = Modifier.width(WORDMARK_WIDTH),
            )
            Text(
                text = tagline,
                style = VankooTheme.typography.h2,
                color = colors.decorPanelFg,
            )
        }
    }
}

/**
 * Paints the panel: the wash first, then the lights over it.
 *
 * The wash is an ellipse in Figma and Compose only draws circular gradients, so it is
 * drawn round and stretched by the transform. The lights stand in for a 240px layer
 * blur with a gradient that fades to transparent — the same silhouette at a fraction
 * of the cost, and blur is not something every target can afford per frame.
 */
private fun DrawScope.drawBrandDecor(colors: VankooColors) {
    drawRect(color = colors.decorPanelTo)

    val centre = Offset(x = size.width * WASH_CENTRE_X, y = size.height * WASH_CENTRE_Y)
    val radius = size.width * WASH_RADIUS_X
    withTransform({ scale(scaleX = 1f, scaleY = WASH_STRETCH_Y, pivot = centre) }) {
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(colors.decorPanelFrom, colors.decorPanelTo),
                center = centre,
                radius = radius,
            ),
            topLeft = Offset(centre.x - radius, centre.y - radius),
            size = Size(radius * 2, radius * 2),
        )
    }

    drawLight(
        colour = colors.decorLightPrimary,
        alpha = PRIMARY_LIGHT_ALPHA,
        centre = Offset(size.width * PRIMARY_LIGHT_X, size.height * PRIMARY_LIGHT_Y),
        radius = size.width * PRIMARY_LIGHT_RADIUS,
    )
    drawLight(
        colour = colors.decorLightSecondary,
        alpha = SECONDARY_LIGHT_ALPHA,
        centre = Offset(size.width * SECONDARY_LIGHT_X, size.height * SECONDARY_LIGHT_Y),
        radius = size.width * SECONDARY_LIGHT_RADIUS,
    )
}

private fun DrawScope.drawLight(colour: Color, alpha: Float, centre: Offset, radius: Float) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(colour.copy(alpha = alpha), Color.Transparent),
            center = centre,
            radius = radius,
        ),
        radius = radius,
        center = centre,
    )
}

/** 250dp, fixed frame by frame in the mockups; the status bar sits inside it. */
private val BAND_HEIGHT = 250.dp
private val WORDMARK_WIDTH = 228.dp
private const val WORDMARK_DESCRIPTION = "Vankoo"

// The panel in Figma is 560x1024 and the band shows a 390x250 window on to it, so
// every one of these is that window's share of the original geometry.
private const val WASH_CENTRE_X = 0.564f
private const val WASH_CENTRE_Y = -0.192f
private const val WASH_RADIUS_X = 0.626f
private const val WASH_STRETCH_Y = 1.824f

private const val PRIMARY_LIGHT_X = 0.051f
private const val PRIMARY_LIGHT_Y = 1.080f
private const val PRIMARY_LIGHT_RADIUS = 1.410f
private const val PRIMARY_LIGHT_ALPHA = 0.16f

private const val SECONDARY_LIGHT_X = 1.231f
private const val SECONDARY_LIGHT_Y = -1.880f
private const val SECONDARY_LIGHT_RADIUS = 1.077f
private const val SECONDARY_LIGHT_ALPHA = 0.10f
