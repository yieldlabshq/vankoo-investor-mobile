package com.liquilabs.vankoo.investor.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.CircleCheck
import com.composables.icons.lucide.Lucide
import com.liquilabs.vankoo.investor.core.designsystem.VankooTheme

/**
 * The seal at the top of a screen that confirms something happened.
 *
 * It is decoration for a message the heading already carries, so it announces
 * nothing to a screen reader.
 */
@Composable
fun VankooSuccessMark(modifier: Modifier = Modifier) {
    val colors = VankooTheme.colors

    Box(
        modifier = modifier
            .size(MARK_SIZE)
            .background(color = colors.stateSuccessBg, shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Lucide.CircleCheck,
            contentDescription = null,
            tint = colors.stateSuccessFg,
            modifier = Modifier.size(ICON_SIZE),
        )
    }
}

private val MARK_SIZE = 56.dp
private val ICON_SIZE = 28.dp
