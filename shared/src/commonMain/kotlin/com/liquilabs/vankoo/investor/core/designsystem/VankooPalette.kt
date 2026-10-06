package com.liquilabs.vankoo.investor.core.designsystem

import androidx.compose.ui.graphics.Color

/**
 * Raw colour ramps generated from the Figma library (Vankoo Design System).
 *
 * Internal on purpose: nothing outside this package should reach for a ramp
 * step directly, the same way the Figma primitives are hidden from the
 * pickers. Consume [VankooColors] instead.
 */
internal object VankooPalette {
    // green
    val green50 = Color(0xFFEAF9F2)
    val green100 = Color(0xFFD6F2E6)
    val green200 = Color(0xFFB4E8D2)
    val green300 = Color(0xFF7DDBB7)
    val green400 = Color(0xFF00C897)
    val green500 = Color(0xFF00B87E)
    val green600 = Color(0xFF009D65)
    val green700 = Color(0xFF00804F)
    val green800 = Color(0xFF006039)
    val green900 = Color(0xFF003E21)
    val green950 = Color(0xFF002812)

    // navy
    val navy50 = Color(0xFFF0F6FC)
    val navy100 = Color(0xFFE2ECF7)
    val navy200 = Color(0xFFCBDCF0)
    val navy300 = Color(0xFFABC8E9)
    val navy400 = Color(0xFF89B2E1)
    val navy500 = Color(0xFF669AD3)
    val navy600 = Color(0xFF4D80B8)
    val navy700 = Color(0xFF396698)
    val navy800 = Color(0xFF274C74)
    val navy900 = Color(0xFF0E2A47)
    val navy950 = Color(0xFF071D33)

    // gray
    val gray50 = Color(0xFFEFF6FD)
    val gray100 = Color(0xFFE5EBF3)
    val gray200 = Color(0xFFD5DBE2)
    val gray300 = Color(0xFFBFC5CC)
    val gray400 = Color(0xFFA9AFB6)
    val gray500 = Color(0xFF91969D)
    val gray600 = Color(0xFF787D84)
    val gray700 = Color(0xFF5F646B)
    val gray800 = Color(0xFF454A50)
    val gray900 = Color(0xFF2A2E34)
    val gray950 = Color(0xFF181C21)

    // indigo
    val indigo50 = Color(0xFFF1F4FF)
    val indigo100 = Color(0xFFE4E9FF)
    val indigo200 = Color(0xFFCFD7FF)
    val indigo300 = Color(0xFFB2BEFF)
    val indigo400 = Color(0xFF96A2FF)
    val indigo500 = Color(0xFF7B82FF)
    val indigo600 = Color(0xFF6366F1)
    val indigo700 = Color(0xFF4F4FCB)
    val indigo800 = Color(0xFF39389D)
    val indigo900 = Color(0xFF22206B)
    val indigo950 = Color(0xFF141049)

    // amber
    val amber50 = Color(0xFFFEF3E7)
    val amber100 = Color(0xFFFBE7D0)
    val amber200 = Color(0xFFF7D3AB)
    val amber300 = Color(0xFFF4B870)
    val amber400 = Color(0xFFF59E0B)
    val amber500 = Color(0xFFE27600)
    val amber600 = Color(0xFFC55B00)
    val amber700 = Color(0xFFA44500)
    val amber800 = Color(0xFF7D3000)
    val amber900 = Color(0xFF531900)
    val amber950 = Color(0xFF370B00)

    // red
    val red50 = Color(0xFFFFF0EE)
    val red100 = Color(0xFFFFE2DE)
    val red200 = Color(0xFFFFCAC4)
    val red300 = Color(0xFFFFA79F)
    val red400 = Color(0xFFFF7F77)
    val red500 = Color(0xFFEF4444)
    val red600 = Color(0xFFDD3135)
    val red700 = Color(0xFFB81823)
    val red800 = Color(0xFF8D0514)
    val red900 = Color(0xFF5F0005)
    val red950 = Color(0xFF400000)

    val white = Color(0xFFFFFFFF)
    val black = Color(0xFF000000)
    val transparent = Color.Transparent

    // Foregrounds for the fixed navy panel. Not steps of any ramp: they were tuned
    // against navy/800..950 in Figma and sit outside the ramps on purpose.
    val panelFgMuted = Color(0xFFC3CDD8)
    val panelFgPositive = Color(0xFF7DDBB7)

    // Navy-tinted shadows: pure black dirties a blue-leaning surface.
    val shadow08 = Color(0x14092C4C)
    val shadow12 = Color(0x1F092C4C)
    val shadow20 = Color(0x33092C4C)
}
