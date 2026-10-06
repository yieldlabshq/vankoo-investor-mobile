package com.liquilabs.vankoo.investor.investment.presentation.confirm

import com.liquilabs.vankoo.investor.core.result.AppError
import com.liquilabs.vankoo.investor.core.result.AppResult
import com.liquilabs.vankoo.investor.iam.application.ObserveSession
import com.liquilabs.vankoo.investor.iam.domain.model.Session
import com.liquilabs.vankoo.investor.iam.domain.model.User
import com.liquilabs.vankoo.investor.iam.domain.repository.SessionStore
import com.liquilabs.vankoo.investor.investment.application.GetAuction
import com.liquilabs.vankoo.investor.investment.application.InvestInAuction
import com.liquilabs.vankoo.investor.investment.domain.model.AuctionDetail
import com.liquilabs.vankoo.investor.investment.domain.model.AuctionStatus
import com.liquilabs.vankoo.investor.investment.domain.model.Currency
import com.liquilabs.vankoo.investor.investment.domain.model.DebitedButNotInvested
import com.liquilabs.vankoo.investor.investment.domain.model.Investment
import com.liquilabs.vankoo.investor.investment.domain.model.InvestmentFailure
import com.liquilabs.vankoo.investor.investment.domain.model.MarketplaceFilter
import com.liquilabs.vankoo.investor.investment.domain.model.MarketplacePage
import com.liquilabs.vankoo.investor.investment.domain.model.Money
import com.liquilabs.vankoo.investor.investment.domain.model.PartitionStatus
import com.liquilabs.vankoo.investor.investment.domain.model.RiskGrade
import com.liquilabs.vankoo.investor.investment.domain.repository.AuctionRepository
import com.liquilabs.vankoo.investor.investment.domain.repository.InvestorBalance
import com.liquilabs.vankoo.investor.investment.domain.repository.InvestorWallet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * What confirming does to the wallet, one bargain per test.
 *
 * The wallet and the auction are fakes that record every call: what the tests pin
 * down is the order of the two calls, that the debit's reference is what reaches
 * Investment, and that a retry sends the same key — because those are the three
 * things that make paying and buying happen exactly once.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ConfirmInvestmentViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val log = mutableListOf<String>()
    private val wallet = RecordingWallet(log)
    private val auctions = RecordingAuctions(log)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun confirmingDebitsFirstAndInvestsWithTheDebitIdAsTransactionId() = runTest(dispatcher) {
        val viewModel = loadedViewModel(amount = "1000")

        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals(listOf("debit", "invest"), log)
        assertEquals(1, wallet.debits.size)
        assertEquals(100_000L, wallet.debits.single().amountMinor)
        assertEquals(wallet.lastDebitId, auctions.investments.single().transactionId)
        assertNotNull(viewModel.state.value.done)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun aRefusedDebitNeverReachesInvestment() = runTest(dispatcher) {
        wallet.answer = AppResult.Failure(AppError.Business(InvestmentFailure.InsufficientBalance))
        val viewModel = loadedViewModel(amount = "1000")

        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals(emptyList(), auctions.investments)
        assertEquals(AppError.Business(InvestmentFailure.InsufficientBalance), viewModel.state.value.error)
        assertFalse(viewModel.state.value.awaitsRetryOfDebitedAmount)
    }

    @Test
    fun aRetryAfterATimeoutSendsTheSameKeyAndBuysOnce() = runTest(dispatcher) {
        wallet.answer = AppResult.Failure(AppError.Network)
        val viewModel = loadedViewModel(amount = "1000")

        viewModel.onSubmit()
        advanceUntilIdle()
        assertEquals(AppError.Network, viewModel.state.value.error)

        wallet.answer = null // back to answering
        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals(2, wallet.debits.size)
        assertEquals(wallet.debits[0].key, wallet.debits[1].key, "the retry must replay the same debit")
        assertEquals(1, auctions.investments.size)
        assertNotNull(viewModel.state.value.done)
    }

    @Test
    fun theMoneyLeavingWithoutAShareIsSaidByNameAndFreezesTheAmount() = runTest(dispatcher) {
        auctions.investAnswer = AppResult.Failure(AppError.Server(400))
        val viewModel = loadedViewModel(amount = "1000")

        viewModel.onSubmit()
        advanceUntilIdle()

        val reason = assertIs<AppError.Business>(viewModel.state.value.error).reason
        val debited = assertIs<DebitedButNotInvested>(reason)
        assertEquals(wallet.lastDebitId, debited.debitId)
        assertEquals(AppError.Server(400), debited.cause)
        assertTrue(viewModel.state.value.awaitsRetryOfDebitedAmount)

        // The field is spoken for: a new amount would be a second debit.
        viewModel.onAmountChange("2000")
        assertEquals("1000", viewModel.state.value.amountText)

        // A retry replays the debit under the same key and finishes the share.
        auctions.investAnswer = null
        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals(wallet.debits[0].key, wallet.debits[1].key)
        assertEquals(listOf(wallet.lastDebitId, wallet.lastDebitId), auctions.investments.map { it.transactionId })
        assertNotNull(viewModel.state.value.done)
        assertFalse(viewModel.state.value.awaitsRetryOfDebitedAmount)
    }

    @Test
    fun changingTheAmountIsANewAttemptWithANewKey() = runTest(dispatcher) {
        wallet.answer = AppResult.Failure(AppError.Network)
        val viewModel = loadedViewModel(amount = "1000")

        viewModel.onSubmit()
        advanceUntilIdle()

        wallet.answer = null
        viewModel.onAmountChange("1500")
        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals(2, wallet.debits.size)
        assertTrue(wallet.debits[0].key != wallet.debits[1].key, "a different amount must not reuse the key")
        assertEquals(150_000L, wallet.debits[1].amountMinor)
    }

    // --- Scenery ---

    private fun loadedViewModel(amount: String): ConfirmInvestmentViewModel {
        val viewModel = ConfirmInvestmentViewModel(
            auctionId = AUCTION_ID,
            observeSession = ObserveSession(FakeSessionStore()),
            getAuction = GetAuction(auctions),
            investorBalance = InvestorBalance { _, _ -> AppResult.Success(500_000L) },
            investInAuction = InvestInAuction(auctions, wallet),
        )
        viewModel.load()
        dispatcher.scheduler.advanceUntilIdle()
        viewModel.onAmountChange(amount)
        return viewModel
    }

    private class RecordingWallet(private val log: MutableList<String>) : InvestorWallet {
        data class Debit(val accountId: String, val amountMinor: Long, val key: String)

        val debits = mutableListOf<Debit>()
        /** What to answer instead of a fresh debit id; null answers a debit. */
        var answer: AppResult<String>? = null
        var lastDebitId: String? = null
            private set

        override suspend fun debit(accountId: String, amount: Money, idempotencyKey: String): AppResult<String> {
            log += "debit"
            debits += Debit(accountId, amount.amountMinor, idempotencyKey)
            answer?.let { return it }
            // Finance replays the same id for the same key; a new key mints a new one.
            val id = "debit-for-$idempotencyKey"
            lastDebitId = id
            return AppResult.Success(id)
        }
    }

    private class RecordingAuctions(private val log: MutableList<String>) : AuctionRepository {
        data class Bought(val auctionId: String, val amountMinor: Long, val transactionId: String)

        val investments = mutableListOf<Bought>()
        var investAnswer: AppResult<Investment>? = null

        override suspend fun getMarketplace(filter: MarketplaceFilter, page: Int, size: Int): AppResult<MarketplacePage> =
            error("not under test")

        override suspend fun getAuction(auctionId: String): AppResult<AuctionDetail> =
            AppResult.Success(auction())

        override suspend fun invest(
            auctionId: String,
            investorId: String,
            amount: Money,
            transactionId: String,
        ): AppResult<Investment> {
            log += "invest"
            investments += Bought(auctionId, amount.amountMinor, transactionId)
            return investAnswer ?: AppResult.Success(
                Investment(
                    partitionId = "partition-1",
                    auctionId = auctionId,
                    amount = amount,
                    participationPct = 10.0,
                    expectedReturn = amount,
                    expectedProfit = Money(0L, amount.currency),
                    returnRatePct = 12.0,
                    status = PartitionStatus.Active,
                    purchasedAt = null,
                ),
            )
        }
    }

    private class FakeSessionStore : SessionStore {
        override val current: Flow<Session?> =
            flowOf(Session(user = User(id = ACCOUNT_ID, email = "e2e@vankoo.dev"), token = "jwt"))

        override suspend fun save(session: Session) = Unit
        override suspend fun clear() = Unit
    }

    private companion object {
        const val ACCOUNT_ID = "01a0a7b7-107c-7a5b-8ad2-b68d066a294f"
        const val AUCTION_ID = "auction-1"

        fun auction() = AuctionDetail(
            auctionId = AUCTION_ID,
            payerName = "Backus",
            payerRuc = "20100113610",
            status = AuctionStatus.Funding,
            riskGrade = RiskGrade.A,
            invoiceAmount = Money(1_000_000L, Currency.PEN),
            target = Money(900_000L, Currency.PEN),
            currentFunding = Money(0L, Currency.PEN),
            dueDate = LocalDate(2026, 12, 1),
            greenCertified = false,
            expiresAt = null,
            acceptedQuote = null,
        )
    }
}
