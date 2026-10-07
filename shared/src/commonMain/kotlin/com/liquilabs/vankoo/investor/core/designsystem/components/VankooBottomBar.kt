package com.liquilabs.vankoo.investor.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.liquilabs.vankoo.investor.core.designsystem.VankooSize
import com.liquilabs.vankoo.investor.core.designsystem.VankooSpacing
import com.liquilabs.vankoo.investor.core.designsystem.VankooTheme

/** One entry of the tab bar: what it shows, and an identity the caller can compare. */
data class VankooBottomTab<T>(
    val id: T,
    val label: String,
    val icon: ImageVector,
)

/**
 * The tab bar at the foot of every root screen, as the Figma `BottomNavItem` row.
 *
 * Material's NavigationBar was not used: it is 80dp tall, draws a pill behind the
 * selected icon and animates it, all of which read as Android. This one is 64dp, flat,
 * and marks the selected tab with weight and colour alone — the same on both platforms
 * because the brand is not a platform.
 *
 * Each item is 72 wide and 56 tall, over the 44 touch floor both platforms publish.
 * Figma pads the item 8 on top of that and lets the label overflow the padding; here
 * the fixed height alone centres icon and label, because Compose clips what overflows.
 * The bar owns the navigation-bar inset so the items sit above the system gesture area.
 */
@Composable
fun <T> VankooBottomBar(
    tabs: List<VankooBottomTab<T>>,
    selected: T?,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = VankooTheme.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surfaceRaised),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(VankooSize.borderHairline)
                .background(colors.borderSubtle),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(BAR_HEIGHT)
                .padding(horizontal = VankooSpacing.pageMargin),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            tabs.forEach { tab ->
                val isSelected = tab.id == selected
                Column(
                    modifier = Modifier
                        .width(ITEM_WIDTH)
                        .height(ITEM_HEIGHT)
                        .selectable(
                            selected = isSelected,
                            role = Role.Tab,
                            onClick = { onSelect(tab.id) },
                        ),
                    verticalArrangement = Arrangement.spacedBy(VankooSpacing.s4, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = tab.icon,
                        // The label right under it says the same thing.
                        contentDescription = null,
                        tint = if (isSelected) colors.textPrimary else colors.textMuted,
                        modifier = Modifier.size(VankooSize.iconLg),
                    )
                    Text(
                        text = tab.label,
                        style = if (isSelected) VankooTheme.typography.captionStrong else VankooTheme.typography.caption,
                        color = if (isSelected) colors.textPrimary else colors.textMuted,
                        maxLines = 1,
                    )
                }
            }
        }
        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
    }
}

private val BAR_HEIGHT = 64.dp
private val ITEM_WIDTH = 72.dp
private val ITEM_HEIGHT = 56.dp
