package com.liquilabs.vankoo.investor.investment.presentation.confirm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liquilabs.vankoo.investor.core.designsystem.VankooSize
import com.liquilabs.vankoo.investor.core.designsystem.VankooSpacing
import com.liquilabs.vankoo.investor.core.designsystem.VankooTheme
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooAmountInput
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooButton
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooButtonVariant
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooCard
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooMobileHeader
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooMoneyPanel
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooNotice
import com.liquilabs.vankoo.investor.core.format.MoneyFormat
import com.liquilabs.vankoo.investor.core.format.PercentFormat
import com.liquilabs.vankoo.investor.core.format.formatLongDate
import com.liquilabs.vankoo.investor.core.format.formatLongDateTime
import com.liquilabs.vankoo.investor.investment.domain.model.AuctionDetail
import com.liquilabs.vankoo.investor.investment.domain.model.Investment
import com.liquilabs.vankoo.investor.investment.presentation.closedLabel
import com.liquilabs.vankoo.investor.investment.presentation.investmentMessage
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import vankoo.shared.generated.resources.Res
import vankoo.shared.generated.resources.confirm_above_auction
import vankoo.shared.generated.resources.confirm_amount_label
import vankoo.shared.generated.resources.confirm_available
import vankoo.shared.generated.resources.confirm_below_minimum
import vankoo.shared.generated.resources.confirm_closed
import vankoo.shared.generated.resources.confirm_collect_on
import vankoo.shared.generated.resources.confirm_collect_on_value
import vankoo.shared.generated.resources.confirm_done_amount_label
import vankoo.shared.generated.resources.confirm_done_back
import vankoo.shared.generated.resources.confirm_done_collect
import vankoo.shared.generated.resources.confirm_done_collect_value
import vankoo.shared.generated.resources.confirm_done_expected_date
import vankoo.shared.generated.resources.confirm_done_title
import vankoo.shared.generated.resources.confirm_expected_return
import vankoo.shared.generated.resources.confirm_minimum
import vankoo.shared.generated.resources.confirm_participation
import vankoo.shared.generated.resources.confirm_participation_value
import vankoo.shared.generated.resources.confirm_shortfall
import vankoo.shared.generated.resources.confirm_submit
import vankoo.shared.generated.resources.confirm_title
import vankoo.shared.generated.resources.confirm_top_up
import vankoo.shared.generated.resources.confirm_use_affordable

/**
 * «¿Cuánto quieres invertir?»: the three frames `MK · Confirmar inversión`,
 * `· Saldo insuficiente` and `· Hecho`, as one screen.
 *
 * The figures under the field are worked out here and not asked for: nothing quotes an
 * investment before it is made. Participation is the amount over the target and the
 * expected return is that same share of what the auction pays at maturity — the two
 * lines `Auction.addInvestment` runs on the other side. They are an estimate until the
 * service answers, and «Hecho» prints what it actually granted.
 *
 * Confirming moves the wallet: the amount is debited before the share is bought (see
 * `InvestInAuction`), so «Disponible» is a real limit and «Saldo insuficiente» has a
 * server-side twin for the race the screen cannot see. The one outcome that gets its
 * own wording is the money leaving and the share not arriving — the field freezes on
 * that amount and the notice asks for a retry, because only a retry with the same
 * amount can finish what the first tap started.
 */
@Composable
fun ConfirmInvestmentScreen(
    auctionId: String,
    onBack: () -> Unit,
    onTopUp: () -> Unit,
    onDone: () -> Unit,
    viewModel: ConfirmInvestmentViewModel = koinViewModel { parametersOf(auctionId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.load() }

    val colors = VankooTheme.colors
    val auction = state.auction
    val done = state.done

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surfaceBase),
    ) {
        VankooMobileHeader(
            title = auction?.payerName.orEmpty(),
            onBack = onBack,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(VankooSpacing.pageMargin),
            verticalArrangement = Arrangement.spacedBy(VankooSpacing.s16),
        ) {
            when {
                !state.loaded || auction == null -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = VankooSpacing.s48),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = colors.actionPrimaryBg)
                }

                done != null -> DoneBody(auction = auction, investment = done, onBack = onDone)

                else -> FormBody(
                    auction = auction,
                    state = state,
                    onAmountChange = viewModel::onAmountChange,
                    onSubmit = viewModel::onSubmit,
                    onTopUp = onTopUp,
                    onUseAffordable = viewModel::useAffordableAmount,
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.FormBody(
    auction: AuctionDetail,
    state: ConfirmInvestmentUiState,
    onAmountChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onTopUp: () -> Unit,
    onUseAffordable: () -> Unit,
) {
    val colors = VankooTheme.colors
    val type = VankooTheme.typography
    val symbol = auction.invoiceAmount.currency.symbol
    val closed = auction.status.closedLabel()

    Text(
        text = stringResource(Res.string.confirm_title),
        style = type.h1,
        color = colors.textPrimary,
    )

    if (closed != null) {
        VankooNotice(text = stringResource(Res.string.confirm_closed))
        return
    }

    VankooCard(verticalGap = VankooSpacing.s16) {
        Text(
            text = stringResource(Res.string.confirm_amount_label),
            style = type.bodyStrong,
            color = colors.textPrimary,
        )

        VankooAmountInput(
            value = state.amountText,
            onValueChange = onAmountChange,
            currencySymbol = symbol,
            isError = state.shortfallMinor != null || state.belowMinimum || state.aboveAuction,
            // Frozen while a debit for this amount waits on its share: the view model
            // would drop the keystroke anyway, and a field that looks live but is not
            // would be worse than one that says so.
            enabled = !state.submitting && !state.awaitsRetryOfDebitedAmount,
        )

        // The two ends of the field: what there is to spend, and the first rule that
        // would turn the amount down. Only one complaint at a time — the field says the
        // nearest reason, not every reason.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(VankooSpacing.s8),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val shortfall = state.shortfallMinor
            Text(
                text = when {
                    state.belowMinimum -> stringResource(
                        Res.string.confirm_below_minimum,
                        MoneyFormat.format(ConfirmInvestmentUiState.MINIMUM_INVESTMENT_MINOR, symbol),
                    )
                    state.aboveAuction -> stringResource(
                        Res.string.confirm_above_auction,
                        MoneyFormat.format(state.maxInvestableMinor ?: 0L, symbol),
                    )
                    shortfall != null ->
                        stringResource(Res.string.confirm_shortfall, MoneyFormat.format(shortfall, symbol))
                    else -> stringResource(
                        Res.string.confirm_available,
                        MoneyFormat.format(state.balanceMinor ?: 0L, symbol),
                    )
                },
                style = type.caption,
                color = if (shortfall != null || state.belowMinimum || state.aboveAuction) {
                    colors.stateErrorFg
                } else {
                    colors.textMuted
                },
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(
                    Res.string.confirm_minimum,
                    MoneyFormat.formatWhole(ConfirmInvestmentUiState.MINIMUM_INVESTMENT_MINOR, symbol),
                ),
                style = type.captionStrong,
                color = colors.textLink,
            )
        }

        Divider()

        val amount = state.amountMinor
        val target = auction.target
        val quote = auction.acceptedQuote
        if (state.showsProjection && amount != null && target != null && target.amountMinor > 0L && quote != null) {
            val share = amount.toDouble() / target.amountMinor
            LabelRow(
                label = stringResource(Res.string.confirm_participation),
                value = stringResource(Res.string.confirm_participation_value, PercentFormat.format(share * 100.0)),
            )
            LabelRow(
                label = stringResource(Res.string.confirm_expected_return),
                value = MoneyFormat.format((quote.fundableAmount.amountMinor * share).toLong(), symbol),
                emphasised = true,
            )
        }
        LabelRow(
            label = stringResource(Res.string.confirm_collect_on),
            value = stringResource(Res.string.confirm_collect_on_value, formatLongDate(auction.dueDate)),
        )

        state.error?.let { VankooNotice(text = it.investmentMessage()) }

        if (state.shortfallMinor != null) {
            VankooButton(
                text = stringResource(Res.string.confirm_top_up),
                onClick = onTopUp,
                modifier = Modifier.fillMaxWidth(),
            )
            state.affordableMinor?.let { affordable ->
                VankooButton(
                    text = stringResource(
                        Res.string.confirm_use_affordable,
                        MoneyFormat.format(affordable, symbol),
                    ),
                    onClick = onUseAffordable,
                    modifier = Modifier.fillMaxWidth(),
                    variant = VankooButtonVariant.Secondary,
                )
            }
        } else {
            VankooButton(
                text = stringResource(Res.string.confirm_submit),
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
                enabled = state.canSubmit,
                loading = state.submitting,
            )
        }
    }
}

/** `MK · Confirmar · Hecho`, printing what the auction granted rather than the estimate. */
@Composable
private fun ColumnScope.DoneBody(
    auction: AuctionDetail,
    investment: Investment,
    onBack: () -> Unit,
) {
    val colors = VankooTheme.colors
    val type = VankooTheme.typography
    val symbol = investment.amount.currency.symbol

    Text(
        text = stringResource(Res.string.confirm_done_title),
        style = type.h1,
        color = colors.textPrimary,
    )

    VankooMoneyPanel(
        primaryLabel = stringResource(Res.string.confirm_done_amount_label),
        primaryValue = MoneyFormat.format(investment.amount.amountMinor, symbol),
        primaryCaption = investment.purchasedAt?.let { formatLongDateTime(it) },
        secondaryLabel = stringResource(Res.string.confirm_expected_return),
        secondaryValue = MoneyFormat.format(investment.expectedReturn.amountMinor, symbol),
    )

    VankooCard {
        LabelRow(
            label = stringResource(Res.string.confirm_participation),
            value = stringResource(
                Res.string.confirm_participation_value,
                PercentFormat.format(investment.participationPct),
            ),
        )
        LabelRow(
            label = stringResource(Res.string.confirm_done_collect),
            value = stringResource(Res.string.confirm_done_collect_value),
        )
        LabelRow(
            label = stringResource(Res.string.confirm_done_expected_date),
            value = formatLongDate(auction.dueDate),
        )
    }

    // The mockup offers «Ver mis inversiones» first. That screen does not exist —
    // `/auctions/investor/{id}` cannot say how much anyone put in — so the only way
    // out is the one that leads somewhere.
    VankooButton(
        text = stringResource(Res.string.confirm_done_back),
        onClick = onBack,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun LabelRow(label: String, value: String, emphasised: Boolean = false) {
    val colors = VankooTheme.colors
    val type = VankooTheme.typography

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(VankooSpacing.s8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = type.caption,
            color = colors.textMuted,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = if (emphasised) type.numeric else type.captionStrong,
            color = colors.textPrimary,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun Divider() {
    val colors = VankooTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(VankooSize.borderHairline)
            .background(colors.borderSubtle),
    )
}
