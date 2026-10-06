package com.liquilabs.vankoo.investor.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Bell
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.Lucide
import com.liquilabs.vankoo.investor.core.designsystem.VankooSize
import com.liquilabs.vankoo.investor.core.designsystem.VankooSpacing
import com.liquilabs.vankoo.investor.core.designsystem.VankooTheme

/**
 * The app header, as the Figma `MobileHeader` component draws it.
 *
 * Two kinds, told apart by whether [onBack] is given. A root screen — one with the
 * tab bar under it — has nowhere to go back to, so it shows no arrow and its title is
 * the brand. A detail screen shows the arrow and names where you came from
 * («Billetera», «Mercado»), which is the iOS pattern and reads fine on Android too.
 *
 * 64dp tall, not 56: with 40dp controls in the Mobile density the original height
 * left 8dp of air. It paints its own opaque surface under the status bar, because a
 * screen that scrolls would otherwise slide its content behind the clock.
 *
 * The bell is drawn but inert: there is no notifications context yet, and hiding it
 * would make every screen disagree with its mockup.
 */
@Composable
fun VankooMobileHeader(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    backContentDescription: String? = null,
) {
    val colors = VankooTheme.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surfaceRaised)
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        // The back target is 44 around a 24 glyph. The row starts 10 early and the gap
        // to the title shrinks by the same 10, so the glyph and the title land where
        // the mockup draws them — on the margin, and 12 past the glyph.
        val backInset = (VankooSize.touchMin - VankooSize.iconLg) / 2
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(HEADER_HEIGHT)
                .padding(
                    start = if (onBack != null) VankooSpacing.pageMargin - backInset else VankooSpacing.pageMargin,
                    end = VankooSpacing.pageMargin,
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(VankooSpacing.s12 - backInset),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (onBack != null) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(VankooSize.touchMin),
                    ) {
                        Icon(
                            imageVector = Lucide.ChevronLeft,
                            contentDescription = backContentDescription,
                            tint = colors.textPrimary,
                            modifier = Modifier.size(VankooSize.iconLg),
                        )
                    }
                }
                Text(
                    text = title,
                    style = VankooTheme.typography.h3,
                    color = colors.textPrimary,
                )
            }

            Icon(
                imageVector = Lucide.Bell,
                contentDescription = null,
                tint = colors.textPrimary,
                modifier = Modifier.size(VankooSize.iconLg),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(VankooSize.borderHairline)
                .background(colors.borderSubtle),
        )
    }
}

private val HEADER_HEIGHT = 64.dp
