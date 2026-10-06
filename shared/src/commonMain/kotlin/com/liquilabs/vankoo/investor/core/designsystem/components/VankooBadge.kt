package com.liquilabs.vankoo.investor.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.liquilabs.vankoo.investor.core.designsystem.VankooShapes
import com.liquilabs.vankoo.investor.core.designsystem.VankooSpacing
import com.liquilabs.vankoo.investor.core.designsystem.VankooTheme

/**
 * A dotted pill that states a fact, as the Figma `Badge` draws it.
 *
 * It states, it does not act: no click, no outline, `radius/full`. That is what tells
 * it apart from [VankooChip], which looks similar and is a control — a chip offers a
 * filter you can turn on, a badge reports what something already is.
 *
 * The colours are parameters rather than a closed set of tones because what a badge
 * marks belongs to whoever renders it: the risk grade of an auction maps to the
 * `risk/A…C` ramp, the green-invoice stamp to `accent/green-finance`, and neither
 * mapping is this file's business. [strong] follows Figma, where a pill that carries
 * an enum is Caption Strong and a free-standing label is Caption.
 *
 * The dot is drawn rather than imported: it is a 6dp filled circle in the badge's own
 * foreground, so there is no glyph to get wrong, and exporting one asset per colour
 * would tie the ramp to a set of files.
 */
@Composable
fun VankooBadge(
    text: String,
    foreground: Color,
    background: Color,
    modifier: Modifier = Modifier,
    strong: Boolean = false,
) {
    Row(
        modifier = modifier
            .clip(VankooShapes.full)
            .background(background)
            .padding(horizontal = VankooSpacing.s12, vertical = VankooSpacing.s4),
        horizontalArrangement = Arrangement.spacedBy(VankooSpacing.s8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(DOT_SIZE)
                .background(color = foreground, shape = CircleShape),
        )
        Text(
            text = text,
            style = if (strong) VankooTheme.typography.captionStrong else VankooTheme.typography.caption,
            color = foreground,
        )
    }
}

private val DOT_SIZE = 6.dp
