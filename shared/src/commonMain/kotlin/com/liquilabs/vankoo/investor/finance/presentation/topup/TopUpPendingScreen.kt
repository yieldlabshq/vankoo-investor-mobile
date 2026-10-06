package com.liquilabs.vankoo.investor.finance.presentation.topup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composables.icons.lucide.Lock
import com.composables.icons.lucide.Lucide
import com.liquilabs.vankoo.investor.core.designsystem.VankooSize
import com.liquilabs.vankoo.investor.core.designsystem.VankooSpacing
import com.liquilabs.vankoo.investor.core.designsystem.VankooTheme
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooButton
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooButtonVariant
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooCard
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooMobileHeader
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooMoneyPanel
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooNotice
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooNoticeTone
import com.liquilabs.vankoo.investor.core.format.MoneyFormat
import com.liquilabs.vankoo.investor.finance.presentation.financeMessage
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import vankoo.shared.generated.resources.Res
import vankoo.shared.generated.resources.common_back
import vankoo.shared.generated.resources.top_up_pending_back_to_wallet
import vankoo.shared.generated.resources.top_up_pending_balance_label
import vankoo.shared.generated.resources.top_up_pending_confirming_body
import vankoo.shared.generated.resources.top_up_pending_confirming_title
import vankoo.shared.generated.resources.top_up_pending_label
import vankoo.shared.generated.resources.top_up_pending_leave
import vankoo.shared.generated.resources.top_up_pending_no_browser
import vankoo.shared.generated.resources.top_up_pending_opening
import vankoo.shared.generated.resources.top_up_pending_preparing
import vankoo.shared.generated.resources.top_up_pending_resume
import vankoo.shared.generated.resources.top_up_pending_title
import vankoo.shared.generated.resources.top_up_pending_trust
import vankoo.shared.generated.resources.top_up_pending_unpaid_body
import vankoo.shared.generated.resources.top_up_pending_unpaid_title
import vankoo.shared.generated.resources.top_up_title_short

/**
 * «Pago seguro» — the screen under Stripe's sheet.
 *
 * Most of the time nobody looks at it: the sheet opens by itself as soon as Finance
 * has Stripe's URL and closes by itself when Stripe redirects back. What it shows is
 * the phase in between — preparing, confirming — and the one case that needs a
 * decision, coming back without having paid, where the same session can be reopened
 * or left behind.
 *
 * «Salir sin pagar» only leaves: Finance has no route to cancel a deposit. The one
 * left behind stays PENDING until Stripe's session expires and the webhook closes it,
 * which is a fact about the backend and not a bug here. The wording says so — it was
 * «Cancelar la recarga» once, and that promised something the app cannot do.
 */
@Composable
fun TopUpPendingScreen(
    depositId: String,
    onCompleted: () -> Unit,
    onFailed: (depositId: String) -> Unit,
    onLeave: () -> Unit,
    viewModel: TopUpPendingViewModel = koinViewModel { parametersOf(depositId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LifecycleStartEffect(viewModel) {
        viewModel.startPolling()
        onStopOrDispose { viewModel.stopPolling() }
    }

    LaunchedEffect(viewModel) {
        viewModel.outcome.collect { outcome ->
            when (outcome) {
                TopUpOutcome.Completed -> onCompleted()
                is TopUpOutcome.Failed -> onFailed(outcome.depositId)
            }
        }
    }

    val colors = VankooTheme.colors
    val type = VankooTheme.typography
    val symbol = TopUpPendingViewModel.CURRENCY.symbol
    val deposit = state.deposit

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surfaceBase),
    ) {
        VankooMobileHeader(
            title = stringResource(Res.string.top_up_title_short),
            onBack = onLeave,
            backContentDescription = stringResource(Res.string.common_back),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(VankooSpacing.pageMargin),
            verticalArrangement = Arrangement.spacedBy(VankooSpacing.s16),
        ) {
            Text(
                text = stringResource(Res.string.top_up_pending_title),
                style = type.h1,
                color = colors.textPrimary,
            )

            VankooMoneyPanel(
                primaryLabel = stringResource(Res.string.top_up_pending_label),
                primaryValue = deposit?.let { MoneyFormat.format(it.amount.amountMinor, symbol) } ?: "—",
                secondaryLabel = stringResource(Res.string.top_up_pending_balance_label),
                secondaryValue = state.balanceMinor?.let { MoneyFormat.format(it, symbol) } ?: "—",
            )

            VankooCard(verticalGap = VankooSpacing.s16) {
                state.error?.let { error -> VankooNotice(text = error.financeMessage()) }

                when (state.phase) {
                    TopUpPhase.PREPARING -> WaitingRow(stringResource(Res.string.top_up_pending_preparing))
                    TopUpPhase.AT_PROVIDER -> WaitingRow(stringResource(Res.string.top_up_pending_opening))
                    TopUpPhase.CONFIRMING -> WaitingRow(
                        title = stringResource(Res.string.top_up_pending_confirming_title),
                        body = stringResource(Res.string.top_up_pending_confirming_body),
                    )

                    TopUpPhase.RETURNED_UNPAID -> {
                        if (state.couldNotOpenBrowser) {
                            VankooNotice(text = stringResource(Res.string.top_up_pending_no_browser))
                        } else {
                            VankooNotice(
                                tone = VankooNoticeTone.Info,
                                title = stringResource(Res.string.top_up_pending_unpaid_title),
                                text = stringResource(Res.string.top_up_pending_unpaid_body),
                            )
                        }
                        VankooButton(
                            text = stringResource(Res.string.top_up_pending_resume),
                            onClick = viewModel::onPay,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = state.canPay,
                        )
                    }
                }

                TrustLine()

                VankooButton(
                    text = stringResource(
                        if (state.phase == TopUpPhase.CONFIRMING) Res.string.top_up_pending_back_to_wallet
                        else Res.string.top_up_pending_leave
                    ),
                    onClick = onLeave,
                    modifier = Modifier.fillMaxWidth(),
                    variant = VankooButtonVariant.Ghost,
                )
            }
        }
    }
}

/** A spinner beside what it is waiting for. */
@Composable
private fun WaitingRow(title: String, body: String? = null) {
    val colors = VankooTheme.colors
    val type = VankooTheme.typography

    Row(
        horizontalArrangement = Arrangement.spacedBy(VankooSpacing.s12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(VankooSize.iconLg),
            color = colors.actionPrimaryBg,
            strokeWidth = SPINNER_STROKE,
        )
        Column(verticalArrangement = Arrangement.spacedBy(VankooSpacing.s4)) {
            Text(text = title, style = type.bodyStrong, color = colors.textPrimary)
            if (body != null) {
                Text(text = body, style = type.caption, color = colors.textMuted)
            }
        }
    }
}

/** Who is handling the card, in one line, under every phase. */
@Composable
private fun TrustLine() {
    val colors = VankooTheme.colors

    Row(
        horizontalArrangement = Arrangement.spacedBy(VankooSpacing.s8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Lucide.Lock,
            contentDescription = null,
            tint = colors.textMuted,
            modifier = Modifier.size(VankooSize.iconSm),
        )
        Text(
            text = stringResource(Res.string.top_up_pending_trust),
            style = VankooTheme.typography.caption,
            color = colors.textMuted,
        )
    }
}

// Thinner than Material's 4dp: at icon size the default ring reads as a solid dot.
private val SPINNER_STROKE = 2.5.dp
