package com.liquilabs.vankoo.investor.investment.presentation

import androidx.compose.runtime.Composable
import com.liquilabs.vankoo.investor.core.result.AppError
import com.liquilabs.vankoo.investor.investment.domain.model.AuctionStatus
import com.liquilabs.vankoo.investor.investment.domain.model.DebitedButNotInvested
import com.liquilabs.vankoo.investor.investment.domain.model.InvestmentFailure
import com.liquilabs.vankoo.investor.investment.domain.model.RiskGrade
import org.jetbrains.compose.resources.stringResource
import vankoo.shared.generated.resources.Res
import vankoo.shared.generated.resources.auction_status_cancelled
import vankoo.shared.generated.resources.auction_status_closed
import vankoo.shared.generated.resources.auction_status_expired
import vankoo.shared.generated.resources.auction_status_fully_funded
import vankoo.shared.generated.resources.error_network
import vankoo.shared.generated.resources.error_server
import vankoo.shared.generated.resources.error_unexpected
import vankoo.shared.generated.resources.iam_error_unauthenticated
import vankoo.shared.generated.resources.investment_error_debited_not_invested
import vankoo.shared.generated.resources.investment_error_insufficient_balance
import vankoo.shared.generated.resources.risk_grade_a
import vankoo.shared.generated.resources.risk_grade_b
import vankoo.shared.generated.resources.risk_grade_c
import vankoo.shared.generated.resources.risk_grade_under_evaluation

/**
 * How this context says out loud what went wrong.
 *
 * Same shape as `iamMessage` and `financeMessage`: the failure travels untranslated
 * and the wording is chosen here, at render time, so a screen already showing an
 * error follows a change of language.
 *
 * A 404 does not pass through here. On the detail screen it is not an error at all —
 * it is the auction being gone — and it gets its own empty state rather than a red
 * banner saying the server misbehaved.
 */
@Composable
fun AppError.investmentMessage(): String = when (this) {
    AppError.Network -> stringResource(Res.string.error_network)
    is AppError.Server -> stringResource(Res.string.error_server)
    is AppError.Unexpected -> stringResource(Res.string.error_unexpected)
    is AppError.Business -> when (val reason = reason) {
        InvestmentFailure.Unauthenticated -> stringResource(Res.string.iam_error_unauthenticated)
        InvestmentFailure.InsufficientBalance -> stringResource(Res.string.investment_error_insufficient_balance)
        // Told in full: the balance already went down. The auction's own reason is
        // not repeated — "retry with this amount" is the only useful instruction.
        is DebitedButNotInvested -> stringResource(Res.string.investment_error_debited_not_invested)
        else -> stringResource(Res.string.error_unexpected)
    }
}

/**
 * The badge text for a grade.
 *
 * [RiskGrade.Unknown] borrows the "not graded" wording: a grade this build cannot
 * name is, from where the investor sits, exactly as informative as no grade at all.
 */
@Composable
fun RiskGrade.label(): String = stringResource(
    when (this) {
        RiskGrade.A -> Res.string.risk_grade_a
        RiskGrade.B -> Res.string.risk_grade_b
        RiskGrade.C -> Res.string.risk_grade_c
        RiskGrade.UnderEvaluation, RiskGrade.Unknown -> Res.string.risk_grade_under_evaluation
    }
)

/**
 * Why an auction is taking no more money, or null while it still is.
 *
 * Null for the statuses that never reach an investor's screen from the marketplace
 * either — a draft, one waiting on risk — because naming those would explain the
 * pipeline rather than the auction.
 */
@Composable
fun AuctionStatus.closedLabel(): String? = when (this) {
    AuctionStatus.FullyFunded -> stringResource(Res.string.auction_status_fully_funded)
    AuctionStatus.Expired -> stringResource(Res.string.auction_status_expired)
    AuctionStatus.Closed -> stringResource(Res.string.auction_status_closed)
    AuctionStatus.Cancelled -> stringResource(Res.string.auction_status_cancelled)
    else -> null
}
