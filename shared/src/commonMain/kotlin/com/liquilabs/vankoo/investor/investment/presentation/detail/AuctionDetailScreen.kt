package com.liquilabs.vankoo.investor.investment.presentation.detail

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.TrendingUp
import com.liquilabs.vankoo.investor.core.designsystem.VankooSize
import com.liquilabs.vankoo.investor.core.designsystem.VankooSpacing
import com.liquilabs.vankoo.investor.core.designsystem.VankooTheme
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooButton
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooButtonVariant
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooCard
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooEmptyState
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooMobileHeader
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooMoneyPanel
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooNotice
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooProgressBar
import com.liquilabs.vankoo.investor.core.format.MoneyFormat
import com.liquilabs.vankoo.investor.core.format.PercentFormat
import com.liquilabs.vankoo.investor.core.format.daysUntil
import com.liquilabs.vankoo.investor.core.format.formatLongDate
import com.liquilabs.vankoo.investor.investment.domain.model.AuctionDetail
import com.liquilabs.vankoo.investor.investment.domain.model.FinancialQuote
import com.liquilabs.vankoo.investor.investment.presentation.closedLabel
import com.liquilabs.vankoo.investor.investment.presentation.components.GreenInvoiceBadge
import com.liquilabs.vankoo.investor.investment.presentation.components.RiskGradeBadge
import com.liquilabs.vankoo.investor.investment.presentation.investmentMessage
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import vankoo.shared.generated.resources.Res
import vankoo.shared.generated.resources.auction_closes_today
import vankoo.shared.generated.resources.auction_days_to_close
import vankoo.shared.generated.resources.auction_detail_back_to_market
import vankoo.shared.generated.resources.auction_detail_breakdown_note
import vankoo.shared.generated.resources.auction_detail_breakdown_title
import vankoo.shared.generated.resources.auction_detail_discount_row
import vankoo.shared.generated.resources.auction_detail_due
import vankoo.shared.generated.resources.auction_detail_due_subtitle
import vankoo.shared.generated.resources.auction_detail_funded_caption
import vankoo.shared.generated.resources.auction_detail_invoice_amount
import vankoo.shared.generated.resources.auction_detail_invest
import vankoo.shared.generated.resources.auction_detail_invoice_title
import vankoo.shared.generated.resources.auction_detail_mype_net
import vankoo.shared.generated.resources.auction_detail_not_found_body
import vankoo.shared.generated.resources.auction_detail_not_found_title
import vankoo.shared.generated.resources.auction_detail_panel_caption
import vankoo.shared.generated.resources.auction_detail_payer_ruc
import vankoo.shared.generated.resources.auction_detail_platform_fee
import vankoo.shared.generated.resources.auction_detail_remaining_label
import vankoo.shared.generated.resources.auction_detail_target_label
import vankoo.shared.generated.resources.auction_detail_yield_label
import vankoo.shared.generated.resources.auction_detail_yield_value
import vankoo.shared.generated.resources.auction_term_days
import vankoo.shared.generated.resources.market_retry
import vankoo.shared.generated.resources.market_title

/**
 * One auction in full: `MK · Detalle de subasta`.
 *
 * Three deliberate departures from the mockup, all of them the contract's doing:
 *
 * - The headline is the payer, not the MYPE, and the subtitle carries no invoice
 *   number. `AuctionDetailsResource` sends `mypeId` and `invoiceId` as UUIDs and no
 *   names, so there is nothing to print. Since the mockup lists the payer again
 *   further down, that row is dropped rather than repeated.
 * - «Cómo se reparte» is not in the mockup's order. The mockup subtracts the discount
 *   to reach "Neto para la MYPE" and then the fee to reach "Objetivo de la subasta",
 *   which is the two labels the wrong way round: in `AuctionPricingCalculator` the
 *   target is `face − investorProfit` and the MYPE's advance is what is left of the
 *   target after the fee. The order here is the one that adds up.
 * - «Invertir» leads to the confirm screen, which is where the caveat now lives:
 *   Investment takes an investment without debiting anything, so the wallet does not
 *   move. Saying it there, next to the amount, beats saying it here next to a figure
 *   nobody is committing yet.
 *
 * And one row the mockups do not have, **pending design**: «Tu rendimiento esperado».
 * The term rate alone cannot be compared between auctions — 8.1 % over 201 days is a
 * worse deal than 2.4 % over 76 — so the screen was inviting the wrong choice. Its
 * wording and placement still want a pass in Figma.
 */
@Composable
fun AuctionDetailScreen(
    auctionId: String,
    onBack: () -> Unit,
    onInvest: (String) -> Unit,
    viewModel: AuctionDetailViewModel = koinViewModel { parametersOf(auctionId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.refresh() }

    val colors = VankooTheme.colors
    val auction = state.auction

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surfaceBase),
    ) {
        VankooMobileHeader(
            title = stringResource(Res.string.market_title),
            onBack = onBack,
            backContentDescription = stringResource(Res.string.market_title),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(VankooSpacing.pageMargin),
            verticalArrangement = Arrangement.spacedBy(VankooSpacing.s16),
        ) {
            when {
                !state.loaded -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = VankooSpacing.s48),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = colors.actionPrimaryBg)
                }

                state.gone -> VankooCard(verticalGap = VankooSpacing.s16) {
                    VankooEmptyState(
                        icon = Lucide.TrendingUp,
                        title = stringResource(Res.string.auction_detail_not_found_title),
                        description = stringResource(Res.string.auction_detail_not_found_body),
                        modifier = Modifier.padding(vertical = VankooSpacing.s8),
                    )
                    VankooButton(
                        text = stringResource(Res.string.auction_detail_back_to_market),
                        onClick = onBack,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                auction != null -> AuctionBody(auction, onInvest = { onInvest(auction.auctionId) })
            }

            state.error?.let { error ->
                VankooNotice(text = error.investmentMessage())
                VankooButton(
                    text = stringResource(Res.string.market_retry),
                    onClick = viewModel::refresh,
                    modifier = Modifier.fillMaxWidth(),
                    variant = VankooButtonVariant.Ghost,
                    height = VankooSize.controlMd,
                    loading = state.refreshing,
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.AuctionBody(auction: AuctionDetail, onInvest: () -> Unit) {
    val colors = VankooTheme.colors
    val type = VankooTheme.typography
    val symbol = auction.invoiceAmount.currency.symbol

    Column(verticalArrangement = Arrangement.spacedBy(VankooSpacing.s8)) {
        Text(text = auction.payerName, style = type.h3, color = colors.textPrimary)
        Text(
            text = stringResource(Res.string.auction_detail_due_subtitle, formatLongDate(auction.dueDate)),
            style = type.caption,
            color = colors.textMuted,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(VankooSpacing.s8)) {
            RiskGradeBadge(auction.riskGrade)
            if (auction.greenCertified) GreenInvoiceBadge()
        }
    }

    val target = auction.target
    val remaining = auction.remaining
    if (target != null && remaining != null) {
        VankooMoneyPanel(
            primaryLabel = stringResource(Res.string.auction_detail_remaining_label),
            primaryValue = MoneyFormat.format(remaining.amountMinor, symbol),
            primaryCaption = fundingCaption(auction),
            secondaryLabel = stringResource(Res.string.auction_detail_target_label),
            secondaryValue = MoneyFormat.format(target.amountMinor, symbol),
            action = if (auction.status.closedLabel() == null) {
                {
                    VankooButton(
                        text = stringResource(Res.string.auction_detail_invest),
                        onClick = onInvest,
                        modifier = Modifier.fillMaxWidth(),
                        height = VankooSize.controlMd,
                    )
                }
            } else {
                // Fully funded, expired or cancelled: the panel keeps the figures and
                // drops the way in. The caption above already says which.
                null
            },
        )
        VankooProgressBar(fraction = auction.progressFraction)
    } else {
        // Nothing has been priced yet, so there is no target to fall short of and no
        // bar to fill. The invoice is all there is to show.
        VankooMoneyPanel(
            primaryLabel = stringResource(Res.string.auction_detail_invoice_amount),
            primaryValue = MoneyFormat.format(auction.invoiceAmount.amountMinor, symbol),
        )
    }

    VankooCard {
        auction.acceptedQuote?.let { quote -> Breakdown(quote, symbol) }
        InvoiceFacts(auction)
    }
}

/**
 * «Cómo se reparte», in the order `AuctionPricingCalculator` actually computes.
 *
 * Two subtractions, each with its own divider, because they belong to different
 * people: the discount is what the investor earns and turns the invoice into the
 * target; the fee is what Vankoo charges and turns the target into what the MYPE
 * receives. Running all four as one column would let someone read the invoice minus
 * both charges as the target, which is the mockup's mistake.
 *
 * The fee's percentage is the one this deal came out at — the total over the invoice
 * — rather than the monthly rate the quote carries, because "0.3 % a month" answers a
 * question nobody on this screen asked.
 */
@Composable
private fun ColumnScope.Breakdown(quote: FinancialQuote, symbol: String) {
    val colors = VankooTheme.colors
    val type = VankooTheme.typography

    Text(
        text = stringResource(Res.string.auction_detail_breakdown_title),
        style = type.bodyStrong,
        color = colors.textPrimary,
    )

    MoneyRow(
        label = stringResource(Res.string.auction_detail_invoice_amount),
        value = MoneyFormat.format(quote.fundableAmount.amountMinor, symbol),
    )
    MoneyRow(
        label = stringResource(
            Res.string.auction_detail_discount_row,
            PercentFormat.format(quote.investorTermRatePct),
        ),
        value = MoneyFormat.format(-quote.investorGrossProfit.amountMinor, symbol, signed = true),
    )

    Divider()
    MoneyRow(
        label = stringResource(Res.string.auction_detail_target_label),
        value = MoneyFormat.format(quote.fundingTarget.amountMinor, symbol),
        emphasised = true,
    )

    MoneyRow(
        label = stringResource(
            Res.string.auction_detail_platform_fee,
            PercentFormat.format(effectiveFeePct(quote)),
        ),
        value = MoneyFormat.format(-quote.platformFeeTotal.amountMinor, symbol, signed = true),
    )
    Divider()
    MoneyRow(
        label = stringResource(Res.string.auction_detail_mype_net),
        value = MoneyFormat.format(quote.mypeAdvance.amountMinor, symbol),
    )

    Text(
        text = stringResource(Res.string.auction_detail_breakdown_note),
        style = type.caption,
        color = colors.textSecondary,
    )

    // PENDING DESIGN. Not in the mockups. It is a row and not a sentence because the
    // card already speaks in rows, and because the term rate is right above it —
    // «Descuento (1.9 %)» — so the two numbers sit on screen together without anyone
    // having to write out that they are the same deal. "Esperado" carries the caveat
    // a paragraph was carrying before: a TEA on a deposit is promised, this one
    // arrives only if the payer pays.
    MoneyRow(
        label = stringResource(Res.string.auction_detail_yield_label),
        value = stringResource(
            Res.string.auction_detail_yield_value,
            PercentFormat.format(quote.investorTeaPct),
            pluralStringResource(Res.plurals.auction_term_days, quote.termDays, quote.termDays),
        ),
        emphasised = true,
    )
}

/** «La factura»: what the auction is a claim on. */
@Composable
private fun ColumnScope.InvoiceFacts(auction: AuctionDetail) {
    val colors = VankooTheme.colors
    val type = VankooTheme.typography

    Divider()
    Text(
        text = stringResource(Res.string.auction_detail_invoice_title),
        style = type.bodyStrong,
        color = colors.textPrimary,
    )
    MoneyRow(
        label = stringResource(Res.string.auction_detail_payer_ruc),
        value = auction.payerRuc,
    )
    MoneyRow(
        label = stringResource(Res.string.auction_detail_due),
        value = formatLongDate(auction.dueDate),
    )
}

/**
 * «62 % fondeado · cierra en 23 días», or what is true instead.
 *
 * An auction that no longer takes money says so rather than counting down to a
 * deadline that has already done its work, and one with no expiry — which the details
 * resource can return, since only publishing sets it — drops the second half.
 */
@Composable
private fun fundingCaption(auction: AuctionDetail): String {
    val percent = PercentFormat.format(auction.progressPct)
    val closed = auction.status.closedLabel()
    val expiresAt = auction.expiresAt

    return when {
        closed != null -> stringResource(Res.string.auction_detail_panel_caption, percent, closed)
        expiresAt == null -> stringResource(Res.string.auction_detail_funded_caption, percent)
        else -> {
            val days = daysUntil(expiresAt)
            val tail = if (days == 0) {
                stringResource(Res.string.auction_closes_today)
            } else {
                pluralStringResource(Res.plurals.auction_days_to_close, days, days)
            }
            stringResource(Res.string.auction_detail_panel_caption, percent, tail)
        }
    }
}

/** What this deal's fee works out to over the invoice, in percentage points. */
private fun effectiveFeePct(quote: FinancialQuote): Double {
    val base = quote.fundableAmount.amountMinor
    if (base <= 0L) return 0.0
    return quote.platformFeeTotal.amountMinor.toDouble() / base * 100.0
}

@Composable
private fun MoneyRow(label: String, value: String, emphasised: Boolean = false) {
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
            color = colors.textSecondary,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = if (emphasised) type.numeric else type.caption,
            color = colors.textPrimary,
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
