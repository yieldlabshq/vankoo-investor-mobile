package com.liquilabs.vankoo.investor.finance.presentation.topup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liquilabs.vankoo.investor.core.designsystem.VankooSize
import com.liquilabs.vankoo.investor.core.designsystem.VankooSpacing
import com.liquilabs.vankoo.investor.core.designsystem.VankooTheme
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooAmountInput
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooButton
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooCard
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooChip
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooMobileHeader
import com.liquilabs.vankoo.investor.core.designsystem.components.VankooNotice
import com.liquilabs.vankoo.investor.core.format.MoneyFormat
import com.liquilabs.vankoo.investor.finance.presentation.financeMessage
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import vankoo.shared.generated.resources.Res
import vankoo.shared.generated.resources.common_back
import vankoo.shared.generated.resources.top_up_current_balance
import vankoo.shared.generated.resources.top_up_stripe_note
import vankoo.shared.generated.resources.top_up_submit
import vankoo.shared.generated.resources.top_up_submit_amount
import vankoo.shared.generated.resources.top_up_title
import vankoo.shared.generated.resources.wallet_title

/**
 * «¿Cuánto quieres recargar?» — `MK · Recargar`.
 *
 * The mockup's «¿Con qué pagas?» block, with a saved Visa, is not here: Vankoo does
 * not hold cards. The card is entered on Stripe's page, which is where the next
 * screen sends the person, and the note under the button says so. That block stays
 * in Figma as a question for the design, not as something this screen can render.
 */
@Composable
fun TopUpScreen(
    initialAmountMinor: Long?,
    onBack: () -> Unit,
    onDepositInitiated: (depositId: String) -> Unit,
    viewModel: TopUpViewModel = koinViewModel { parametersOf(initialAmountMinor) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.initiated.collect { depositId -> onDepositInitiated(depositId) }
    }

    val colors = VankooTheme.colors
    val type = VankooTheme.typography
    val symbol = TopUpViewModel.CURRENCY.symbol

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
                .imePadding()
                .padding(VankooSpacing.pageMargin),
            verticalArrangement = Arrangement.spacedBy(VankooSpacing.s16),
        ) {
            Text(
                text = stringResource(Res.string.top_up_title),
                style = type.h1,
                color = colors.textPrimary,
            )

            VankooCard(verticalGap = VankooSpacing.s16) {
                VankooAmountInput(
                    value = state.amountText,
                    onValueChange = viewModel::onAmountChange,
                    currencySymbol = symbol,
                    supportingText = state.balanceMinor?.let { balance ->
                        stringResource(Res.string.top_up_current_balance, MoneyFormat.format(balance, symbol))
                    },
                    enabled = !state.submitting,
                )

                Row(horizontalArrangement = Arrangement.spacedBy(VankooSpacing.s8)) {
                    TopUpViewModel.SUGGESTIONS_MINOR.forEach { suggestion ->
                        VankooChip(
                            text = MoneyFormat.formatWhole(suggestion, symbol),
                            onClick = { viewModel.onSuggestion(suggestion) },
                            selected = state.amountMinor == suggestion,
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(VankooSize.borderHairline)
                        .background(colors.borderSubtle),
                )

                Text(
                    text = stringResource(Res.string.top_up_stripe_note),
                    style = type.caption,
                    color = colors.textMuted,
                )

                state.error?.let { error ->
                    VankooNotice(text = error.financeMessage())
                }

                val amount = state.amountMinor
                VankooButton(
                    text = if (amount != null && amount > 0) {
                        stringResource(Res.string.top_up_submit_amount, MoneyFormat.format(amount, symbol))
                    } else {
                        stringResource(Res.string.top_up_submit)
                    },
                    onClick = viewModel::onSubmit,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.canSubmit,
                    loading = state.submitting,
                )
            }
        }
    }
}
