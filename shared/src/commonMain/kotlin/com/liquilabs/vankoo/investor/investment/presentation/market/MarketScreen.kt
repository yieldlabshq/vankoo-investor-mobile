package com.liquilabs.vankoo.investor.investment.presentation.market

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooChip
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooEmptyState
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooMobileHeader
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooNotice
import com.liquilabs.vankoo.investor.investment.domain.model.MarketplaceFilter
import com.liquilabs.vankoo.investor.investment.presentation.components.AuctionCard
import com.liquilabs.vankoo.investor.investment.presentation.investmentMessage
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import vankoo.shared.generated.resources.Res
import vankoo.shared.generated.resources.app_name
import vankoo.shared.generated.resources.market_empty_body
import vankoo.shared.generated.resources.market_empty_clear_filter
import vankoo.shared.generated.resources.market_empty_green_body
import vankoo.shared.generated.resources.market_empty_title
import vankoo.shared.generated.resources.market_filter_all
import vankoo.shared.generated.resources.market_filter_closing_soon
import vankoo.shared.generated.resources.market_filter_green
import vankoo.shared.generated.resources.market_more
import vankoo.shared.generated.resources.market_retry
import vankoo.shared.generated.resources.market_title

/**
 * The invoices open for investment: `MK · Mercado` and `MK · Mercado · Vacío`.
 *
 * A LazyColumn rather than the wallet's scrolling Column, because this list has no
 * ceiling — the marketplace pages, and the wallet's history shows five rows and a
 * button. The title and the filters ride inside it so they scroll away with the
 * first card, which is what the mockup's 844 frame implies once there are more cards
 * than fit.
 *
 * The mockup's fourth chip, «Riesgo A», is not here: there is no risk-grade filter in
 * the marketplace query, and narrowing the page the app happens to hold would drop
 * grade-A auctions sitting on the next one. See `MarketplaceFilter`.
 *
 * The empty state's «Avísame cuando haya» is not here either: nothing in the platform
 * can take that subscription, and a button that only closes itself is worse than no
 * button. Under the green filter the offer becomes one that does work — clearing it.
 */
@Composable
fun MarketScreen(
    onOpenAuction: (String) -> Unit,
    viewModel: MarketViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.refresh() }

    val colors = VankooTheme.colors
    val type = VankooTheme.typography

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surfaceBase),
    ) {
        VankooMobileHeader(title = stringResource(Res.string.app_name))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(VankooSpacing.pageMargin),
            verticalArrangement = Arrangement.spacedBy(VankooSpacing.s16),
        ) {
            item(key = "title") {
                Text(
                    text = stringResource(Res.string.market_title),
                    style = type.h1,
                    color = colors.textPrimary,
                )
            }

            item(key = "filters") {
                FilterRow(
                    selected = state.filter,
                    onSelect = viewModel::selectFilter,
                )
            }

            if (!state.loaded) {
                item(key = "loading") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = VankooSpacing.s48),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = colors.actionPrimaryBg)
                    }
                }
                return@LazyColumn
            }

            state.error?.let { error ->
                item(key = "error") {
                    Column(verticalArrangement = Arrangement.spacedBy(VankooSpacing.s16)) {
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

            if (state.isEmpty) {
                item(key = "empty") {
                    VankooCard(contentPadding = VankooSpacing.s16, verticalGap = VankooSpacing.s16) {
                        VankooEmptyState(
                            icon = Lucide.TrendingUp,
                            title = stringResource(Res.string.market_empty_title),
                            description = stringResource(
                                if (state.isEmptyBecauseFiltered) Res.string.market_empty_green_body
                                else Res.string.market_empty_body
                            ),
                            modifier = Modifier.padding(vertical = VankooSpacing.s8),
                        )
                        if (state.isEmptyBecauseFiltered) {
                            VankooButton(
                                text = stringResource(Res.string.market_empty_clear_filter),
                                onClick = { viewModel.selectFilter(MarketplaceFilter.All) },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }

            items(state.auctions, key = { it.auctionId }) { auction ->
                AuctionCard(
                    auction = auction,
                    onClick = { onOpenAuction(auction.auctionId) },
                )
            }

            if (state.hasMore) {
                item(key = "more") {
                    VankooButton(
                        text = stringResource(Res.string.market_more),
                        onClick = viewModel::loadMore,
                        modifier = Modifier.fillMaxWidth(),
                        variant = VankooButtonVariant.Ghost,
                        height = VankooSize.controlMd,
                        loading = state.loadingMore,
                    )
                }
            }
        }
    }
}

/**
 * The filter chips, in one scrolling row.
 *
 * They scroll horizontally because a translation is free to be longer than the three
 * Spanish labels, and a chip cut off by the right margin is a control nobody can
 * reach. At the current widths nothing actually moves.
 */
@Composable
private fun FilterRow(
    selected: MarketplaceFilter,
    onSelect: (MarketplaceFilter) -> Unit,
) {
    val labels = listOf(
        MarketplaceFilter.All to stringResource(Res.string.market_filter_all),
        MarketplaceFilter.Green to stringResource(Res.string.market_filter_green),
        MarketplaceFilter.ClosingSoon to stringResource(Res.string.market_filter_closing_soon),
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(VankooSpacing.s8),
    ) {
        labels.forEach { (filter, label) ->
            VankooChip(
                text = label,
                onClick = { onSelect(filter) },
                selected = filter == selected,
            )
        }
    }
}
