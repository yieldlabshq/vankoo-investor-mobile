package com.liquilabs.vankoo.investor.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.Dp
import com.liquilabs.vankoo.investor.core.designsystem.VankooElevation
import com.liquilabs.vankoo.investor.core.designsystem.VankooPalette
import com.liquilabs.vankoo.investor.core.designsystem.VankooShapes
import com.liquilabs.vankoo.investor.core.designsystem.VankooSize
import com.liquilabs.vankoo.investor.core.designsystem.VankooSpacing
import com.liquilabs.vankoo.investor.core.designsystem.VankooTheme

/**
 * A raised block of content: `surface/raised`, hairline `border/subtle`, `radius/xl`
 * and Elevation/1.
 *
 * The shadow is navy-tinted from the palette rather than Compose's default black,
 * which dirties a blue-leaning surface. In dark mode the raised surface does the
 * lifting and the shadow barely reads, which is the intended behaviour.
 */
@Composable
fun VankooCard(
    modifier: Modifier = Modifier,
    contentPadding: Dp = VankooSpacing.cardPadding,
    verticalGap: Dp = VankooSpacing.s12,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = VankooTheme.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = VankooElevation.level1,
                shape = VankooShapes.xl,
                ambientColor = VankooPalette.shadow08,
                spotColor = VankooPalette.shadow12,
            )
            .clip(VankooShapes.xl)
            .background(colors.surfaceRaised)
            .border(
                width = VankooSize.borderHairline,
                color = colors.borderSubtle,
                shape = VankooShapes.xl,
            )
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(verticalGap),
        content = content,
    )
}
