package com.liquilabs.vankoo.investor.finance.presentation.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.draw.clip
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Wallet
import com.liquilabs.vankoo.investor.core.designsystem.VankooShapes
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
import com.liquilabs.vankoo.investor.core.format.MoneyFormat
import com.liquilabs.vankoo.investor.core.format.formatShortDate
import com.liquilabs.vankoo.investor.finance.domain.model.WalletMovement
import com.liquilabs.vankoo.investor.finance.presentation.label
import com.liquilabs.vankoo.investor.finance.presentation.financeMessage
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import vankoo.shared.generated.resources.Res
import vankoo.shared.generated.resources.app_name
import vankoo.shared.generated.resources.wallet_available_caption
import vankoo.shared.generated.resources.wallet_available_caption_empty
import vankoo.shared.generated.resources.wallet_available_label
import vankoo.shared.generated.resources.wallet_empty_body
import vankoo.shared.generated.resources.wallet_empty_title
import vankoo.shared.generated.resources.wallet_movements_more
import vankoo.shared.generated.resources.wallet_movements_title
import vankoo.shared.generated.resources.wallet_retry
import vankoo.shared.generated.resources.wallet_title
import vankoo.shared.generated.resources.wallet_top_up

/**
 * The balance and the history: `MK · Billetera` and `MK · Billetera · Vacía`.
 *
 * It reloads every time it comes into view, which is how it picks up a top-up that
 * just landed: the pending screen pops back here and this runs again.
 *
 * Two things the mockup shows are not here on purpose. «Invertido ahora mismo» has
 * no backend — there is no query of a person's investments — so the panel's second
 * block is left out rather than shown at S/ 0.00, which would be a number nobody
 * measured. And a movement's subtitle is its date alone: the wire has neither the
 * card («Visa ···· 4242») nor the invoice («F001-1283») the mockup prints there.
 */
@Composable
fun WalletScreen(
    onTopUp: () -> Unit,
    viewModel: WalletViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.refresh() }

    val colors = VankooTheme.colors
    val type = VankooTheme.typography
    val symbol = WalletViewModel.CURRENCY.symbol

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surfaceBase),
    ) {
        VankooMobileHeader(title = stringResource(Res.string.app_name))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(VankooSpacing.pageMargin),
            verticalArrangement = Arrangement.spacedBy(VankooSpacing.s16),
        ) {
            Text(
                text = stringResource(Res.string.wallet_title),
                style = type.h1,
                color = colors.textPrimary,
            )

            if (!state.loaded) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = VankooSpacing.s48),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = colors.actionPrimaryBg)
                }
                return@Column
            }

            val balanceMinor = state.wallet?.balance?.amountMinor ?: 0L
            VankooMoneyPanel(
                primaryLabel = stringResource(Res.string.wallet_available_label),
                primaryValue = MoneyFormat.format(balanceMinor, symbol),
                primaryCaption = stringResource(
                    if (state.wallet == null) Res.string.wallet_available_caption_empty
                    else Res.string.wallet_available_caption
                ),
                action = {
                    VankooButton(
                        text = stringResource(Res.string.wallet_top_up),
                        onClick = onTopUp,
                        modifier = Modifier.fillMaxWidth(),
                        height = VankooSize.controlMd,
                    )
                },
            )

            state.error?.let { error ->
                VankooNotice(text = error.financeMessage())
                VankooButton(
                    text = stringResource(Res.string.wallet_retry),
                    onClick = viewModel::refresh,
                    modifier = Modifier.fillMaxWidth(),
                    variant = VankooButtonVariant.Ghost,
                    height = VankooSize.controlMd,
                    loading = state.refreshing,
                )
            }

            if (state.isEmpty) {
                VankooCard(contentPadding = VankooSpacing.s16, verticalGap = VankooSpacing.s16) {
                    VankooEmptyState(
                        icon = Lucide.Wallet,
                        title = stringResource(Res.string.wallet_empty_title),
                        description = stringResource(Res.string.wallet_empty_body),
                        modifier = Modifier.padding(vertical = VankooSpacing.s8),
                    )
                    VankooButton(
                        text = stringResource(Res.string.wallet_top_up),
                        onClick = onTopUp,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            } else if (state.movements.isNotEmpty()) {
                VankooCard {
                    Text(
                        text = stringResource(Res.string.wallet_movements_title),
                        style = type.bodyStrong,
                        color = colors.textPrimary,
                    )
                    MovementList(movements = state.movements, symbol = symbol)
                    if (state.hasMoreMovements) {
                        VankooButton(
                            text = stringResource(Res.string.wallet_movements_more),
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
}

/** The bordered stack of rows, as the mockup's `movimientos` frame. */
@Composable
private fun MovementList(movements: List<WalletMovement>, symbol: String) {
    val colors = VankooTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(VankooShapes.xl)
            .border(
                width = VankooSize.borderHairline,
                color = colors.borderSubtle,
                shape = VankooShapes.xl,
            ),
    ) {
        movements.forEachIndexed { index, movement ->
            if (index > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(VankooSize.borderHairline)
                        .background(colors.borderSubtle),
                )
            }
            MovementRow(movement = movement, symbol = symbol)
        }
    }
}

@Composable
private fun MovementRow(movement: WalletMovement, symbol: String) {
    val colors = VankooTheme.colors
    val type = VankooTheme.typography
    val signed = movement.signedAmountMinor

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = VankooSpacing.s16, vertical = VankooSpacing.s12),
        horizontalArrangement = Arrangement.spacedBy(VankooSpacing.s12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(VankooSpacing.s2),
        ) {
            Text(text = movement.kind.label(), style = type.bodyStrong, color = colors.textPrimary)
            Text(
                text = formatShortDate(movement.occurredAt),
                style = type.caption,
                color = colors.textMuted,
            )
        }
        Text(
            text = MoneyFormat.format(signed, symbol, signed = true),
            style = type.numeric,
            color = if (signed < 0) colors.financeNegativeFg else colors.financePositiveFg,
        )
    }
}
