package com.liquilabs.vankoo.investor.finance.presentation.topup

import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liquilabs.vankoo.investor.core.designsystem.VankooSize
import com.liquilabs.vankoo.investor.core.designsystem.VankooSpacing
import com.liquilabs.vankoo.investor.core.designsystem.VankooTheme
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooButton
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooCard
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooMobileHeader
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooNotice
import com.liquilabs.vankoo.investor.core.format.MoneyFormat
import com.liquilabs.vankoo.investor.core.format.formatLongDateTime
import com.liquilabs.vankoo.investor.finance.presentation.bodyResource
import com.liquilabs.vankoo.investor.finance.presentation.titleResource
import com.liquilabs.vankoo.investor.finance.presentation.financeMessage
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import vankoo.shared.generated.resources.Res
import vankoo.shared.generated.resources.common_back
import vankoo.shared.generated.resources.top_up_failed_amount
import vankoo.shared.generated.resources.top_up_failed_balance_unchanged
import vankoo.shared.generated.resources.top_up_failed_date
import vankoo.shared.generated.resources.top_up_failed_retry
import vankoo.shared.generated.resources.top_up_failed_title
import vankoo.shared.generated.resources.wallet_title

/**
 * «No se pudo recargar» — `MK · Recargar · No se pudo`.
 *
 * The notice's wording follows Finance's normalised reason, so a card that expired
 * is not told it was declined — and it reads `Deposit.outcomeReason`, because an
 * expired session comes as CANCELLED with the reason in `cancellationReason`, not in
 * `failureReason`. Two rows of the mockup are missing on purpose: the
 * card («Visa ···· 4242»), which Vankoo never sees, and the second button («Usar
 * otra tarjeta»), because the card is chosen on Stripe's page and both buttons would
 * lead to the same place. One button, one meaning.
 */
@Composable
fun TopUpFailedScreen(
    depositId: String,
    onBack: () -> Unit,
    onRetry: (amountMinor: Long) -> Unit,
    viewModel: TopUpFailedViewModel = koinViewModel { parametersOf(depositId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

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
            title = stringResource(Res.string.wallet_title),
            onBack = onBack,
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
                text = stringResource(Res.string.top_up_failed_title),
                style = type.h1,
                color = colors.textPrimary,
            )

            state.error?.let { error -> VankooNotice(text = error.financeMessage()) }

            if (deposit != null) {
                VankooNotice(
                    title = stringResource(deposit.outcomeReason.titleResource()),
                    text = stringResource(deposit.outcomeReason.bodyResource()),
                )

                VankooCard {
                    DetailRow(
                        label = stringResource(Res.string.top_up_failed_amount),
                        value = MoneyFormat.format(deposit.amount.amountMinor, symbol),
                    )
                    (deposit.updatedAt ?: deposit.createdAt)?.let { at ->
                        DetailRow(
                            label = stringResource(Res.string.top_up_failed_date),
                            value = formatLongDateTime(at),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(VankooSize.borderHairline)
                            .background(colors.borderSubtle),
                    )
                    DetailRow(
                        label = stringResource(Res.string.top_up_failed_balance_unchanged),
                        value = state.balanceMinor?.let { MoneyFormat.format(it, symbol) } ?: "—",
                    )
                }

                VankooButton(
                    text = stringResource(Res.string.top_up_failed_retry),
                    onClick = { onRetry(deposit.amount.amountMinor) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    val colors = VankooTheme.colors
    val type = VankooTheme.typography

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = type.caption, color = colors.textMuted)
        Text(text = value, style = type.captionStrong, color = colors.textPrimary)
    }
}
