package com.liquilabs.vankoo.investor.core.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Spacing, sizing and radii from the Figma library.
 *
 * The Density collection in Figma has Desktop and Mobile modes; a phone is
 * always Mobile, so the Desktop column is not represented here. That is why
 * these are plain constants rather than a mode-aware lookup.
 */
@Immutable
object VankooSpacing {
    // The scale, named by its value so it reads the same here, in the CSS and
    // in Figma. T-shirt names were tried first and do not survive the scale:
    // the step after xxxl would have to be xxxxl.
    val s0 = 0.dp
    val s2 = 2.dp
    val s4 = 4.dp
    val s8 = 8.dp
    val s12 = 12.dp
    val s16 = 16.dp
    val s24 = 24.dp
    val s32 = 32.dp
    val s40 = 40.dp
    val s48 = 48.dp
    val s56 = 56.dp
    val s64 = 64.dp
    val s80 = 80.dp
    val s96 = 96.dp
    val s120 = 120.dp

    // Named uses, from the Mobile column of the Density collection. Prefer
    // these over a raw step when the value has a reason.

    /** Horizontal breathing room at the screen edge. */
    val pageMargin = s16

    /** Gap between sibling blocks inside a screen. */
    val gutter = s16

    /** Gap between major sections. */
    val section = s32

    /** Inner padding of a card. */
    val cardPadding = s16

}

@Immutable
object VankooRadius {
    val none = 0.dp
    val sm = 4.dp
    val md = 8.dp
    val lg = 12.dp
    val xl = 16.dp

    /** Pill. Large enough that any realistic control ends up fully rounded. */
    val full = 9999.dp
}

@Immutable
object VankooSize {
    val controlSm: Dp = 40.dp
    val controlMd: Dp = 44.dp
    val controlLg: Dp = 56.dp
    val controlPaddingX: Dp = 16.dp

    /**
     * Minimum tappable area. 44 is the floor both Apple and Android publish;
     * anything the finger drives must not go below it.
     */
    val touchMin: Dp = 44.dp

    val borderHairline: Dp = 1.dp
    val borderThick: Dp = 2.dp
    val borderFocus: Dp = 3.dp

    val iconSm: Dp = 16.dp
    val iconMd: Dp = 20.dp
    val iconLg: Dp = 24.dp
}

/**
 * Corner shapes, from the Figma radius scale.
 *
 * These exist because Material 3 rounds its buttons fully by default. Vankoo
 * is a brand language rather than a platform one, so a button here has the
 * same 8dp corner it has on the web; leaving Material's default in place
 * would make the same product look different on each surface.
 */
object VankooShapes {
    val sm = androidx.compose.foundation.shape.RoundedCornerShape(VankooRadius.sm)
    val md = androidx.compose.foundation.shape.RoundedCornerShape(VankooRadius.md)
    val lg = androidx.compose.foundation.shape.RoundedCornerShape(VankooRadius.lg)
    val xl = androidx.compose.foundation.shape.RoundedCornerShape(VankooRadius.xl)

    /** Pill. Status badges and avatars only — never an action. */
    val full = androidx.compose.foundation.shape.RoundedCornerShape(VankooRadius.full)
}

/**
 * Elevation levels, matching Elevation/1..3 in the Figma library.
 *
 * These are single Dp values because Compose models one shadow per surface
 * while Figma stacks two, so the match is in intent rather than in exact
 * geometry. Pair them with [VankooPalette] shadow colours through
 * Modifier.shadow(ambientColor =, spotColor =) where the platform supports it.
 *
 * In dark mode prefer raising the surface (surfaceRaised over surfaceBase) to
 * adding a shadow: a shadow barely reads against a dark ground.
 */
object VankooElevation {
    /** A card at rest. */
    val level1 = 2.dp

    /** Dropdown, popover. */
    val level2 = 6.dp

    /** Modal, bottom sheet. */
    val level3 = 16.dp
}
