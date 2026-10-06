package com.liquilabs.vankoo.investor.finance.domain.model

/**
 * An investor's balance in one currency.
 *
 * One wallet per (account, currency): Finance opens it lazily with the first deposit,
 * so an account that has never topped up has none — the repository answers null, not
 * a wallet at zero. The screen tells them apart on purpose: "recarga para empezar" is
 * a different message from a balance that reads S/ 0.00.
 */
data class Wallet(
    val id: String,
    val accountId: String,
    val balance: Money,
)
