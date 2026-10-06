package com.liquilabs.vankoo.investor.investment.application

import com.liquilabs.vankoo.investor.core.result.AppError
import com.liquilabs.vankoo.investor.core.result.AppResult
import com.liquilabs.vankoo.investor.investment.domain.model.DebitedButNotInvested
import com.liquilabs.vankoo.investor.investment.domain.model.Investment
import com.liquilabs.vankoo.investor.investment.domain.model.Money
import com.liquilabs.vankoo.investor.investment.domain.repository.AuctionRepository
import com.liquilabs.vankoo.investor.investment.domain.repository.InvestorWallet

/**
 * Buys a share of an auction, paying for it first.
 *
 * Two calls, in this order: the wallet is debited, and only then is Investment asked
 * for the share, with the debit's reference as its `transactionId`. Neither service
 * talks to the other — Finance never hears of the auction, Investment never hears of
 * the wallet — so this is the one place that knows an investment costs money, which
 * is what the KDoc here promised the day it did not.
 *
 * The order is what makes a retry safe. Both calls are idempotent on what they are
 * given: the same [idempotencyKey] replays the same debit and answers the same
 * reference, and the same reference makes `Auction.addInvestment` replay the share
 * already bought instead of buying another. So a tap that timed out anywhere along
 * the way can be tapped again with the same key and ends up with exactly one debit
 * and one share.
 *
 * What the order cannot make safe is the second call failing for good after the
 * first succeeded. That comes back as [DebitedButNotInvested] rather than as the
 * auction's own error, because the person's balance already went down and a banner
 * saying "the server did not answer as expected" would be hiding that.
 */
class InvestInAuction(
    private val auctions: AuctionRepository,
    private val wallet: InvestorWallet,
) {
    suspend operator fun invoke(
        auctionId: String,
        investorId: String,
        amount: Money,
        idempotencyKey: String,
    ): AppResult<Investment> {
        val debitId = when (val debit = wallet.debit(investorId, amount, idempotencyKey)) {
            is AppResult.Success -> debit.data
            is AppResult.Failure -> return debit
        }

        return when (val invested = auctions.invest(auctionId, investorId, amount, transactionId = debitId)) {
            is AppResult.Success -> invested
            is AppResult.Failure -> AppResult.Failure(
                AppError.Business(DebitedButNotInvested(debitId = debitId, cause = invested.error)),
            )
        }
    }
}
