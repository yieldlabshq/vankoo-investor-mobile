package com.liquilabs.vankoo.investor.investment.domain.repository

import com.liquilabs.vankoo.investor.core.result.AppResult
import com.liquilabs.vankoo.investor.investment.domain.model.Money

/**
 * Where the money for an investment comes out of.
 *
 * Sibling of [InvestorBalance], for the same reason: the wallet lives in Finance, and
 * this context needs exactly one thing from it before it buys a share — that the
 * amount has left the wallet. Naming the seam keeps every other file here free of
 * finance's vocabulary; [com.liquilabs.vankoo.investor.investment.infrastructure.FinanceInvestorWallet]
 * is the one adapter behind it.
 *
 * Answers the debit's reference, which is what Investment is then given as
 * `transactionId`: the same key sent again replays the same debit and answers the
 * same reference, so a retry that reaches Investment replays the same share.
 *
 * A wallet with too little in it, or none at all, is refused by name as
 * [com.liquilabs.vankoo.investor.investment.domain.model.InvestmentFailure.InsufficientBalance];
 * the confirm screen already tried to spare the person that, and this is the race it
 * could not see.
 */
fun interface InvestorWallet {
    suspend fun debit(accountId: String, amount: Money, idempotencyKey: String): AppResult<String>
}
