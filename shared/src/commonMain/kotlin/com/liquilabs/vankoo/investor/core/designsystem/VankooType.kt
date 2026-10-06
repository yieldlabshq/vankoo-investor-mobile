package com.liquilabs.vankoo.investor.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import vankoo.shared.generated.resources.PlusJakartaSans_Bold
import vankoo.shared.generated.resources.PlusJakartaSans_ExtraBold
import vankoo.shared.generated.resources.PlusJakartaSans_Medium
import vankoo.shared.generated.resources.PlusJakartaSans_Regular
import vankoo.shared.generated.resources.PlusJakartaSans_SemiBold
import vankoo.shared.generated.resources.Poppins_Bold
import vankoo.shared.generated.resources.Poppins_Regular
import vankoo.shared.generated.resources.Poppins_SemiBold
import vankoo.shared.generated.resources.Res

/** Plus Jakarta Sans — the product voice. Everything inside the app uses it. */
@Composable
fun vankooSansFamily(): FontFamily = FontFamily(
    Font(Res.font.PlusJakartaSans_Regular, FontWeight.Normal, FontStyle.Normal),
    Font(Res.font.PlusJakartaSans_Medium, FontWeight.Medium, FontStyle.Normal),
    Font(Res.font.PlusJakartaSans_SemiBold, FontWeight.SemiBold, FontStyle.Normal),
    Font(Res.font.PlusJakartaSans_Bold, FontWeight.Bold, FontStyle.Normal),
    Font(Res.font.PlusJakartaSans_ExtraBold, FontWeight.ExtraBold, FontStyle.Normal),
)

/**
 * Poppins — the brand voice, and only that.
 *
 * It is the letterform of the wordmark, so it belongs to the logo and to
 * marketing headlines. Do not use it for product copy: its numerals are wide
 * and uneven, which makes a column of amounts harder to scan.
 */
@Composable
fun vankooDisplayFamily(): FontFamily = FontFamily(
    Font(Res.font.Poppins_Regular, FontWeight.Normal, FontStyle.Normal),
    Font(Res.font.Poppins_SemiBold, FontWeight.SemiBold, FontStyle.Normal),
    Font(Res.font.Poppins_Bold, FontWeight.Bold, FontStyle.Normal),
)

/**
 * The type ramp, in the Mobile column of the Figma Density collection.
 *
 * Body is 16sp rather than the 15sp used on desktop: at arm's length a phone
 * needs the extra size, and on iOS anything under 16 makes the browser and
 * some inputs zoom.
 */
@Immutable
data class VankooTypography(
    val display: TextStyle,
    val h1: TextStyle,
    val h2: TextStyle,
    val h3: TextStyle,
    val bodyLarge: TextStyle,
    val body: TextStyle,
    val bodyStrong: TextStyle,
    val caption: TextStyle,
    val captionStrong: TextStyle,
    /**
     * Small caps label above a figure. Carries the +8% tracking from Figma;
     * the uppercasing is the caller's job, because Compose has no
     * text-transform — pass the string already uppercased.
     */
    val overline: TextStyle,
    /** Large money figures. */
    val numericXl: TextStyle,
    /** Amounts and invoice ids inside lists and tables. */
    val numeric: TextStyle,
)

@Composable
fun rememberVankooTypography(): VankooTypography {
    val sans = vankooSansFamily()
    return remember(sans) {
        VankooTypography(
            display = TextStyle(fontFamily = sans, fontWeight = FontWeight.ExtraBold, fontSize = 36.sp, lineHeight = 44.sp),
            h1 = TextStyle(fontFamily = sans, fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 32.sp),
            h2 = TextStyle(fontFamily = sans, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 26.sp),
            h3 = TextStyle(fontFamily = sans, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 24.sp),
            bodyLarge = TextStyle(fontFamily = sans, fontWeight = FontWeight.Normal, fontSize = 18.sp, lineHeight = 27.sp),
            body = TextStyle(fontFamily = sans, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
            bodyStrong = TextStyle(fontFamily = sans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp),
            caption = TextStyle(fontFamily = sans, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp),
            captionStrong = TextStyle(fontFamily = sans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, lineHeight = 18.sp),
            overline = TextStyle(fontFamily = sans, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.08.em),
            numericXl = TextStyle(fontFamily = sans, fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 32.sp),
            numeric = TextStyle(fontFamily = sans, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 24.sp),
        )
    }
}
