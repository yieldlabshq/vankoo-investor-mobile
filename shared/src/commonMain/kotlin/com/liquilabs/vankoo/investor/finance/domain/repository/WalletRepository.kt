package com.liquilabs.vankoo.investor.finance.domain.repository

import com.liquilabs.vankoo.investor.core.result.AppResult
import com.liquilabs.vankoo.investor.finance.domain.model.Currency
import com.liquilabs.vankoo.investor.finance.domain.model.DebitReason
import com.liquilabs.vankoo.investor.finance.domain.model.MovementPage
import com.liquilabs.vankoo.investor.finance.domain.model.Money
import com.liquilabs.vankoo.investor.finance.domain.model.Wallet
import com.liquilabs.vankoo.investor.finance.domain.model.WalletDebit

interface WalletRepository {
    /**
     * The balance, or null when no wallet exists yet in that currency.
     *
     * Null is a success: Finance answers 404 until the first deposit lands, and the
     * screen has its own thing to say about that.
     */
    suspend fun getWallet(accountId: String, currency: Currency): AppResult<Wallet?>

    /** A page of history, newest first. An empty page is 200, never 404. */
    suspend fun getMovements(
        accountId: String,
        currency: Currency,
        page: Int,
        size: Int,
    ): AppResult<MovementPage>

    /**
     * Takes [amount] out of the wallet, synchronously: a success means the balance
     * already went down.
     *
     * The idempotency key is the caller's, as with a deposit: the same key with the
     * same amount and reason replays the debit already applied and answers the same
     * [WalletDebit.id], so a retry never debits twice. A wallet that was never
     * opened is a failure here, not a null — there is nothing to take money from.
     */
    suspend fun debit(
        accountId: String,
        amount: Money,
        reason: DebitReason,
        idempotencyKey: String,
    ): AppResult<WalletDebit>
}
