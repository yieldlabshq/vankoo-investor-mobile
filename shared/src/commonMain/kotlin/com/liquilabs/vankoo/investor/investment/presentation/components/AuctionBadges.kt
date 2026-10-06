package com.liquilabs.vankoo.investor.investment.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.liquilabs.vankoo.investor.core.designsystem.VankooTheme
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooBadge
import com.liquilabs.vankoo.investor.investment.domain.model.RiskGrade
import com.liquilabs.vankoo.investor.investment.presentation.label
import org.jetbrains.compose.resources.stringResource
import vankoo.shared.generated.resources.Res
import vankoo.shared.generated.resources.auction_green_stamp

/**
 * The risk grade of an auction, as the Figma `RiskGrade` pill.
 *
 * The mapping from grade to ramp lives here rather than in the design system because
 * `ScoreGrade` is Investment's enum: a component that knew about it would make the
 * library depend on one service's vocabulary.
 *
 * Ungraded takes the neutral pair — sunken fill, muted text — on purpose. "Not graded"
 * is the absence of a grade, not a bad one, and painting it in the red end of the
 * `risk/A…C` ramp would tell the investor something nobody measured.
 */
@Composable
fun RiskGradeBadge(grade: RiskGrade, modifier: Modifier = Modifier) {
    val colors = VankooTheme.colors
    val foreground = when (grade) {
        RiskGrade.A -> colors.riskAFg
        RiskGrade.B -> colors.riskBFg
        RiskGrade.C -> colors.riskCFg
        RiskGrade.UnderEvaluation, RiskGrade.Unknown -> colors.textMuted
    }
    val background = when (grade) {
        RiskGrade.A -> colors.riskABg
        RiskGrade.B -> colors.riskBBg
        RiskGrade.C -> colors.riskCBg
        RiskGrade.UnderEvaluation, RiskGrade.Unknown -> colors.surfaceSunken
    }

    VankooBadge(
        text = grade.label(),
        foreground = foreground,
        background = background,
        modifier = modifier,
        // Caption Strong, as every pill that carries an enum is in the library.
        strong = true,
    )
}

/**
 * The green-invoice stamp.
 *
 * A plain [VankooBadge] and not a pill of its own, because `greenCertified` is a
 * boolean: there is no second value to render, so there is no variant to keep in step
 * with a backend enum.
 */
@Composable
fun GreenInvoiceBadge(modifier: Modifier = Modifier) {
    val colors = VankooTheme.colors
    VankooBadge(
        text = stringResource(Res.string.auction_green_stamp),
        foreground = colors.accentGreenFinanceFg,
        background = colors.accentGreenFinanceBg,
        modifier = modifier,
    )
}
