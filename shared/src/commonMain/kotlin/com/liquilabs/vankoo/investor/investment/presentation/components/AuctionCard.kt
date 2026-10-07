package com.liquilabs.vankoo.investor.investment.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import com.liquilabs.vankoo.investor.core.designsystem.VankooSpacing
import com.liquilabs.vankoo.investor.core.designsystem.VankooTheme
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooCard
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooProgressBar
import com.liquilabs.vankoo.investor.core.format.MoneyFormat
import com.liquilabs.vankoo.investor.core.format.PercentFormat
import com.liquilabs.vankoo.investor.investment.domain.model.MarketplaceAuction
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import vankoo.shared.generated.resources.Res
import vankoo.shared.generated.resources.auction_days_to_maturity
import vankoo.shared.generated.resources.auction_discount_pending
import vankoo.shared.generated.resources.auction_funding_summary
import vankoo.shared.generated.resources.auction_tea

/**
 * One row of the market, as the Figma `tarjeta` in the Inversión section.
 *
 * Two things the mockup prints are missing because `AuctionMarketplaceView` does not
 * carry them: the MYPE's name — only its UUID travels — and the invoice number. The
 * headline is [MarketplaceAuction.payerName] instead, which is defensible on its own
 * terms: the company on the card is the one whose payment the investor is buying, and
 * it is what the risk grade is about. The subtitle then carries the due date alone.
 *
 * The figures row prints the target and the percentage the service computed, never
 * one this screen divided out, so the number beside the bar and the bar agree.
 */
@Composable
fun AuctionCard(
    auction: MarketplaceAuction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = VankooTheme.colors
    val type = VankooTheme.typography

    VankooCard(
        modifier = modifier.clickable(role = Role.Button, onClick = onClick),
        verticalGap = VankooSpacing.s12,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(VankooSpacing.s8),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(VankooSpacing.s8),
            ) {
                Text(
                    text = auction.payerName,
                    style = type.bodyStrong,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = pluralStringResource(
                        Res.plurals.auction_days_to_maturity,
                        auction.daysToMaturity,
                        auction.daysToMaturity,
                    ),
                    style = type.caption,
                    color = colors.textMuted,
                )
            }
            RiskGradeBadge(auction.riskGrade)
        }

        VankooProgressBar(fraction = auction.progressFraction)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(VankooSpacing.s8),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(
                    Res.string.auction_funding_summary,
                    MoneyFormat.formatWhole(auction.target.amountMinor, auction.target.currency.symbol),
                    PercentFormat.format(auction.progressPct),
                ),
                style = type.caption,
                color = colors.textMuted,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                // PENDING DESIGN. The mockup prints the term discount here — «4,2 % de
                // descuento» — and this shows the annualised rate instead. The card has
                // room for one figure and the term rate is the one that cannot be
                // compared: 8.1 % over 201 days and 2.4 % over 76 are 15 % and 12 % a
                // year, so a market sorted by eye on the term rate rewards the worse
                // auction. The detail screen still prints both and explains the link.
                // Swapping this back is one string away if the design says otherwise.
                //
                // An auction with no accepted quote has no rate at all. It can only be
                // one the projection has not caught up with, since publishing is what
                // accepting a quote does, so the copy says "not set yet" rather than
                // inventing a zero.
                text = auction.teaPct
                    ?.let { stringResource(Res.string.auction_tea, PercentFormat.format(it)) }
                    ?: stringResource(Res.string.auction_discount_pending),
                style = type.captionStrong,
                color = colors.textPrimary,
            )
        }

        if (auction.greenCertified) {
            GreenInvoiceBadge()
        }
    }
}
