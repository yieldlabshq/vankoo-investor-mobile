package com.liquilabs.vankoo.investor.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.CircleAlert
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.TriangleAlert
import com.liquilabs.vankoo.investor.core.designsystem.VankooColors
import com.liquilabs.vankoo.investor.core.designsystem.VankooShapes
import com.liquilabs.vankoo.investor.core.designsystem.VankooSize
import com.liquilabs.vankoo.investor.core.designsystem.VankooSpacing
import com.liquilabs.vankoo.investor.core.designsystem.VankooTheme

/** What kind of news the banner carries; it picks the colour and the icon. */
enum class VankooNoticeTone {
    /** The server rejected the whole request. The default, and the only one until now. */
    Error,

    /** Something the person should know, with nothing to fix. */
    Info,

    /** Nothing broke, but the next step needs care. */
    Warning,
}

/**
 * The banner a screen shows for news that belongs to no single field.
 *
 * Anything a field owns — a malformed address, a password that is too short — goes
 * under that field as supporting text, where the person is already looking.
 *
 * With a [title] it becomes the two-line `Alert` of the library: a headline that says
 * what happened and a body that says what to do about it. The icon then aligns to the
 * first line rather than to the centre, because that is the line it belongs to.
 */
@Composable
fun VankooNotice(
    text: String,
    modifier: Modifier = Modifier,
    title: String? = null,
    tone: VankooNoticeTone = VankooNoticeTone.Error,
) {
    val palette = tone.palette(VankooTheme.colors)

    Row(
        modifier = modifier
            .clip(VankooShapes.md)
            .background(palette.background)
            .border(
                width = VankooSize.borderHairline,
                color = palette.border,
                shape = VankooShapes.md,
            )
            .padding(horizontal = HORIZONTAL_PADDING, vertical = VERTICAL_PADDING),
        horizontalArrangement = Arrangement.spacedBy(GAP),
        verticalAlignment = if (title == null) Alignment.CenterVertically else Alignment.Top,
    ) {
        Icon(
            imageVector = palette.icon,
            // The message right beside it says the same thing.
            contentDescription = null,
            tint = palette.foreground,
            modifier = Modifier
                .size(ICON_SIZE)
                // Sits on the first line's optical centre when there is a title.
                .then(if (title != null) Modifier.padding(top = TITLE_ICON_NUDGE) else Modifier),
        )
        Column(verticalArrangement = Arrangement.spacedBy(VankooSpacing.s4)) {
            if (title != null) {
                Text(
                    text = title,
                    style = VankooTheme.typography.bodyStrong,
                    color = palette.foreground,
                )
            }
            Text(
                text = text,
                style = if (title == null) VankooTheme.typography.caption else VankooTheme.typography.body,
                color = palette.foreground,
            )
        }
    }
}

private class NoticePalette(
    val foreground: Color,
    val background: Color,
    val border: Color,
    val icon: ImageVector,
)

// Info borrows the accent blue on purpose: the token set has one blue and it already
// plays the informational role (see VankooColors).
private fun VankooNoticeTone.palette(colors: VankooColors): NoticePalette = when (this) {
    VankooNoticeTone.Error -> NoticePalette(
        foreground = colors.stateErrorFg,
        background = colors.stateErrorBg,
        border = colors.stateErrorBorder,
        icon = Lucide.CircleAlert,
    )

    VankooNoticeTone.Info -> NoticePalette(
        foreground = colors.accentAiFg,
        background = colors.accentAiBg,
        border = colors.accentAiBorder,
        icon = Lucide.Info,
    )

    VankooNoticeTone.Warning -> NoticePalette(
        foreground = colors.stateWarningFg,
        background = colors.stateWarningBg,
        border = colors.stateWarningBorder,
        icon = Lucide.TriangleAlert,
    )
}

// Off the spacing scale on purpose: these are the frame's own values in Figma, and
// rounding 14 to 16 would move the icon off the text's optical line.
private val HORIZONTAL_PADDING = 14.dp
private val VERTICAL_PADDING = 12.dp
private val GAP = 10.dp
private val ICON_SIZE = 18.dp
private val TITLE_ICON_NUDGE = 3.dp
