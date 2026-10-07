package com.liquilabs.vankoo.investor.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.liquilabs.vankoo.investor.core.designsystem.VankooShapes
import com.liquilabs.vankoo.investor.core.designsystem.VankooTheme

/**
 * How far along something is, as the Figma `ProgressBar`: a 6dp pill with a filled
 * head.
 *
 * Not Material's `LinearProgressIndicator`: that one animates, carries its own track
 * and stop-indicator geometry, and rounds to its own token. This is two rectangles.
 *
 * The track is `border/strong` and not `border/default`, which at 6dp on a raised
 * surface does not reach 3:1 and simply disappears. The fill is
 * `accent/green-finance`, the token that exists for funding figures, so the brand
 * green stays reserved for actions.
 *
 * [fraction] is clamped: a server that reports more funding than the target — a race
 * between the projection and a partition — must round the bar down to full rather
 * than draw past the end of it.
 *
 * The bar carries no semantics of its own. Every caller prints the same number in
 * words beside it («62 % fondeado»), and a screen reader that met both would say it
 * twice.
 */
@Composable
fun VankooProgressBar(
    fraction: Float,
    modifier: Modifier = Modifier,
) {
    val colors = VankooTheme.colors

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(BAR_HEIGHT)
            .clip(VankooShapes.full)
            .background(colors.borderStrong)
            .clearAndSetSemantics { },
    ) {
        val filled = fraction.coerceIn(0f, 1f)
        if (filled > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(filled)
                    .fillMaxHeight()
                    .clip(VankooShapes.full)
                    .background(colors.accentGreenFinanceFg),
            )
        }
    }
}

private val BAR_HEIGHT = 6.dp
