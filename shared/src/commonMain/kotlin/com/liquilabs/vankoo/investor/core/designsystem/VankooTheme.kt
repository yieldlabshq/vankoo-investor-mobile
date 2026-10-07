package com.liquilabs.vankoo.investor.core.designsystem

import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import kotlinx.coroutines.launch

private val LocalVankooColors = staticCompositionLocalOf<VankooColors> {
    error("VankooColors not provided. Wrap the tree in VankooTheme { }.")
}

private val LocalVankooTypography = staticCompositionLocalOf<VankooTypography> {
    error("VankooTypography not provided. Wrap the tree in VankooTheme { }.")
}

/**
 * Entry point to the design system.
 *
 * [colors] and [typography] carry the full Vankoo token set, including the
 * things Material has no slot for — invoice status, risk grade, the finance
 * tokens. Material's own scheme is filled from the same source, so a Material
 * component and a Vankoo one cannot drift apart.
 */
object VankooTheme {
    val colors: VankooColors
        @Composable @ReadOnlyComposable get() = LocalVankooColors.current

    val typography: VankooTypography
        @Composable @ReadOnlyComposable get() = LocalVankooTypography.current
}

@Composable
fun VankooTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) vankooDarkColors else vankooLightColors
    val typography = rememberVankooTypography()

    CompositionLocalProvider(
        LocalVankooColors provides colors,
        LocalVankooTypography provides typography,
        // Material's ripple reads as Android on iOS. A flat press tint is
        // neutral on both, and the brand is not a platform.
        LocalIndication provides VankooPressIndication,
    ) {
        MaterialTheme(
            colorScheme = colors.toMaterialScheme(darkTheme),
            typography = typography.toMaterialTypography(),
            shapes = vankooMaterialShapes,
            content = content,
        )
    }
}

/**
 * Fills Material's fixed slots from the Vankoo tokens.
 *
 * Only the slots that a Material component actually reads are mapped; the rest
 * keep their defaults because nothing in the app should be reaching for them.
 */
private fun VankooColors.toMaterialScheme(dark: Boolean) =
    (if (dark) darkColorScheme() else lightColorScheme()).copy(
        primary = actionPrimaryBg,
        onPrimary = actionPrimaryFg,
        secondary = actionSecondaryBg,
        onSecondary = actionSecondaryFg,
        tertiary = accentAiFg,
        onTertiary = actionSecondaryFg,
        background = surfaceBase,
        onBackground = textPrimary,
        surface = surfaceRaised,
        onSurface = textPrimary,
        surfaceVariant = surfaceSunken,
        onSurfaceVariant = textSecondary,
        error = stateErrorFg,
        onError = actionDangerFg,
        errorContainer = stateErrorBg,
        onErrorContainer = stateErrorFg,
        outline = borderDefault,
        outlineVariant = borderSubtle,
        inverseSurface = surfaceInverse,
        inverseOnSurface = textInverse,
    )

private fun VankooTypography.toMaterialTypography() = Typography(
    displayLarge = display,
    headlineLarge = h1,
    headlineMedium = h2,
    headlineSmall = h3,
    titleLarge = h3,
    titleMedium = bodyStrong,
    bodyLarge = bodyLarge,
    bodyMedium = body,
    bodySmall = caption,
    labelLarge = bodyStrong,
    labelMedium = captionStrong,
    labelSmall = overline,
)

/**
 * Material's shape slots, filled from the Figma radius scale.
 *
 * Without this Material 3 rounds buttons fully, which is its opinion and not
 * ours: the Figma component says 8dp and so does the web.
 */
private val vankooMaterialShapes = Shapes(
    extraSmall = VankooShapes.sm,
    small = VankooShapes.md,
    medium = VankooShapes.md,
    large = VankooShapes.lg,
    extraLarge = VankooShapes.xl,
)

/** Flat press tint, used instead of Material's ripple. */
private object VankooPressIndication : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode =
        VankooPressNode(interactionSource)

    override fun equals(other: Any?): Boolean = other === this
    override fun hashCode(): Int = VankooPressIndication::class.hashCode()
}

private class VankooPressNode(
    private val interactionSource: InteractionSource,
) : Modifier.Node(), DrawModifierNode {

    private var pressed by mutableStateOf(false)

    override fun onAttach() {
        coroutineScope.launch {
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> pressed = true
                    is PressInteraction.Release, is PressInteraction.Cancel -> pressed = false
                }
            }
        }
    }

    override fun ContentDrawScope.draw() {
        if (pressed) {
            drawContent()
            drawRect(color = Color.Black.copy(alpha = 0.08f))
        } else {
            drawContent()
        }
    }
}
