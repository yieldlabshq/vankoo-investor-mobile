package com.liquilabs.vankoo.investor.finance.presentation.topup

import com.liquilabs.vankoo.investor.core.platform.UrlOpener
import com.liquilabs.vankoo.investor.core.result.AppResult
import com.liquilabs.vankoo.investor.finance.application.GetDeposit
import com.liquilabs.vankoo.investor.finance.application.GetWallet
import com.liquilabs.vankoo.investor.finance.domain.model.Currency
import com.liquilabs.vankoo.investor.finance.domain.model.DebitReason
import com.liquilabs.vankoo.investor.finance.domain.model.Deposit
import com.liquilabs.vankoo.investor.finance.domain.model.DepositStatus
import com.liquilabs.vankoo.investor.finance.domain.model.Money
import com.liquilabs.vankoo.investor.finance.domain.model.MovementPage
import com.liquilabs.vankoo.investor.finance.domain.model.Wallet
import com.liquilabs.vankoo.investor.finance.domain.model.WalletDebit
import com.liquilabs.vankoo.investor.finance.domain.repository.DepositRepository
import com.liquilabs.vankoo.investor.finance.domain.repository.WalletRepository
import com.liquilabs.vankoo.investor.iam.application.ObserveSession
import com.liquilabs.vankoo.investor.iam.domain.model.Session
import com.liquilabs.vankoo.investor.iam.domain.model.User
import com.liquilabs.vankoo.investor.iam.domain.repository.SessionStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * The waiting screen's decisions, one per test, with time under control.
 *
 * Every test starts polling the way the screen does — on START — and drives the
 * clock past one poll. The repository fake answers whatever the test put in it, so a
 * deposit can grow an action URL or reach a terminal status between polls exactly as
 * Finance's projection does.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TopUpPendingViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val deposits = FakeDepositRepository()
    private val opener = RecordingUrlOpener()
    private val returns = ProviderReturnSignal()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun opensTheProviderPageByItselfOnceWhenTheUrlAppears() = runTest(dispatcher) {
        deposits.current = pending(actionUrl = null)
        val viewModel = viewModel()

        viewModel.startPolling()
        advanceOnePoll()
        assertEquals(TopUpPhase.PREPARING, viewModel.state.value.phase)
        assertEquals(emptyList(), opener.opened)

        deposits.current = pending(actionUrl = CHECKOUT_URL)
        advanceOnePoll()
        assertEquals(listOf(CHECKOUT_URL), opener.opened)
        assertEquals(TopUpPhase.AT_PROVIDER, viewModel.state.value.phase)

        // Still pending on the next polls: no second sheet.
        advanceOnePoll()
        advanceOnePoll()
        assertEquals(1, opener.opened.size)

        viewModel.stopPolling()
    }

    @Test
    fun comingBackThroughTheSuccessDoorMeansConfirming() = runTest(dispatcher) {
        val viewModel = atProvider()

        returns.post(ProviderReturn.SUCCESS)
        viewModel.startPolling()

        assertEquals(TopUpPhase.CONFIRMING, viewModel.state.value.phase)
        viewModel.stopPolling()
    }

    @Test
    fun comingBackThroughTheCancelDoorMeansUnpaid() = runTest(dispatcher) {
        val viewModel = atProvider()

        returns.post(ProviderReturn.CANCEL)
        viewModel.startPolling()

        assertEquals(TopUpPhase.RETURNED_UNPAID, viewModel.state.value.phase)
        viewModel.stopPolling()
    }

    @Test
    fun comingBackWithNoSignalAlsoMeansUnpaid() = runTest(dispatcher) {
        // The sheet was closed with its own X: Stripe redirects nowhere.
        val viewModel = atProvider()

        viewModel.startPolling()

        assertEquals(TopUpPhase.RETURNED_UNPAID, viewModel.state.value.phase)
        viewModel.stopPolling()
    }

    @Test
    fun aSignalIsConsumedSoALaterStartDoesNotReplayIt() = runTest(dispatcher) {
        val viewModel = atProvider()
        returns.post(ProviderReturn.SUCCESS)
        viewModel.startPolling()
        viewModel.stopPolling()

        viewModel.startPolling()

        assertEquals(TopUpPhase.CONFIRMING, viewModel.state.value.phase)
        viewModel.stopPolling()
    }

    @Test
    fun aSignalLeftOverFromAnEarlierTopUpIsDroppedBeforeThisOneOpens() = runTest(dispatcher) {
        // Seen on a device: the previous top-up ended by polling while the activity was
        // only paused under the sheet, so its SUCCESS was never read. The next top-up
        // must not inherit it — and must still open the sheet.
        returns.post(ProviderReturn.SUCCESS)
        deposits.current = pending(actionUrl = CHECKOUT_URL)
        val viewModel = viewModel()

        viewModel.startPolling()
        advanceOnePoll()

        assertEquals(TopUpPhase.AT_PROVIDER, viewModel.state.value.phase)
        assertEquals(listOf(CHECKOUT_URL), opener.opened)
        viewModel.stopPolling()
    }

    @Test
    fun aSignalIsReadOnTheNextPollWhenTheScreenNeverRestarted() = runTest(dispatcher) {
        // The activity stayed paused under the sheet: polling never stopped, so there is
        // no new start. The return must still be noticed.
        deposits.current = pending(actionUrl = CHECKOUT_URL)
        val viewModel = viewModel()
        viewModel.startPolling()
        advanceOnePoll()
        assertEquals(TopUpPhase.AT_PROVIDER, viewModel.state.value.phase)

        returns.post(ProviderReturn.SUCCESS)
        advanceOnePoll()

        assertEquals(TopUpPhase.CONFIRMING, viewModel.state.value.phase)
        viewModel.stopPolling()
    }

    @Test
    fun stillAtTheProviderBetweenPollsIsNotMistakenForAReturn() = runTest(dispatcher) {
        deposits.current = pending(actionUrl = CHECKOUT_URL)
        val viewModel = viewModel()
        viewModel.startPolling()
        advanceOnePoll()

        advanceOnePoll()
        advanceOnePoll()

        assertEquals(TopUpPhase.AT_PROVIDER, viewModel.state.value.phase)
        viewModel.stopPolling()
    }

    @Test
    fun backToPaymentReopensTheSameUrlByHand() = runTest(dispatcher) {
        val viewModel = atProvider()
        viewModel.startPolling()
        assertEquals(TopUpPhase.RETURNED_UNPAID, viewModel.state.value.phase)

        viewModel.onPay()

        assertEquals(listOf(CHECKOUT_URL, CHECKOUT_URL), opener.opened)
        assertEquals(TopUpPhase.AT_PROVIDER, viewModel.state.value.phase)
        viewModel.stopPolling()
    }

    @Test
    fun whenNothingCanOpenThePageTheScreenSaysSoAndStaysUnpaid() =
        runTest(dispatcher) {
            opener.answer = false
            deposits.current = pending(actionUrl = CHECKOUT_URL)
            val viewModel = viewModel()

            viewModel.startPolling()
            advanceOnePoll()

            assertTrue(viewModel.state.value.couldNotOpenBrowser)
            assertEquals(TopUpPhase.RETURNED_UNPAID, viewModel.state.value.phase)
            viewModel.stopPolling()
        }

    @Test
    fun aSucceededDepositEndsTheWaitAsCompleted() = runTest(dispatcher) {
        val viewModel = atProvider()
        returns.post(ProviderReturn.SUCCESS)
        viewModel.startPolling()

        deposits.current = pending(actionUrl = CHECKOUT_URL).copy(status = DepositStatus.SUCCEEDED)
        val outcomes = mutableListOf<TopUpOutcome>()
        val collecting = backgroundScope.launchCollecting(viewModel, outcomes)
        advanceOnePoll()

        assertEquals(listOf<TopUpOutcome>(TopUpOutcome.Completed), outcomes)
        collecting.cancel()
    }

    @Test
    fun aCancelledDepositEndsTheWaitAsFailed() = runTest(dispatcher) {
        val viewModel = atProvider()
        viewModel.startPolling()

        deposits.current = pending(actionUrl = CHECKOUT_URL).copy(status = DepositStatus.CANCELLED)
        val outcomes = mutableListOf<TopUpOutcome>()
        val collecting = backgroundScope.launchCollecting(viewModel, outcomes)
        advanceOnePoll()

        assertEquals(listOf<TopUpOutcome>(TopUpOutcome.Failed(DEPOSIT_ID)), outcomes)
        collecting.cancel()
    }

    @Test
    fun theBalanceIsReadOnceForTheMoneyPanel() = runTest(dispatcher) {
        val viewModel = viewModel()
        dispatcher.scheduler.runCurrent()

        assertEquals(250_000L, viewModel.state.value.balanceMinor)
        assertFalse(viewModel.state.value.couldNotOpenBrowser)
    }

    // --- Scenery -----------------------------------------------------------------

    /** A view model that has already sent the person to Stripe and stopped polling behind the sheet. */
    private fun TestScope.atProvider(): TopUpPendingViewModel {
        deposits.current = pending(actionUrl = CHECKOUT_URL)
        val viewModel = viewModel()
        viewModel.startPolling()
        advanceOnePoll()
        check(viewModel.state.value.phase == TopUpPhase.AT_PROVIDER)
        viewModel.stopPolling()
        return viewModel
    }

    private fun viewModel() = TopUpPendingViewModel(
        depositId = DEPOSIT_ID,
        observeSession = ObserveSession(FakeSessionStore()),
        getDeposit = GetDeposit(deposits),
        getWallet = GetWallet(FakeWalletRepository()),
        urlOpener = opener,
        providerReturns = returns,
    )

    private fun TestScope.advanceOnePoll() {
        dispatcher.scheduler.runCurrent()
        dispatcher.scheduler.advanceTimeBy(TopUpPendingViewModel.POLL_INTERVAL_MS)
        dispatcher.scheduler.runCurrent()
    }

    private fun CoroutineScope.launchCollecting(
        viewModel: TopUpPendingViewModel,
        into: MutableList<TopUpOutcome>,
    ): Job = launch { viewModel.outcome.collect { into += it } }

    private fun pending(actionUrl: String?) = Deposit(
        id = DEPOSIT_ID,
        accountId = ACCOUNT_ID,
        amount = Money(100_000, Currency.PEN),
        status = DepositStatus.PENDING,
        actionUrl = actionUrl,
        failureReason = null,
        cancellationReason = null,
        createdAt = null,
        updatedAt = null,
    )

    private class FakeDepositRepository : DepositRepository {
        var current: Deposit? = null

        override suspend fun initiate(accountId: String, amount: Money, idempotencyKey: String): AppResult<Deposit> =
            error("not under test")

        override suspend fun get(depositId: String): AppResult<Deposit> =
            AppResult.Success(checkNotNull(current) { "the test did not say what Finance answers" })
    }

    private class FakeWalletRepository : WalletRepository {
        override suspend fun getWallet(accountId: String, currency: Currency): AppResult<Wallet?> =
            AppResult.Success(Wallet(id = "w-1", accountId = accountId, balance = Money(250_000, currency)))

        override suspend fun getMovements(
            accountId: String,
            currency: Currency,
            page: Int,
            size: Int,
        ): AppResult<MovementPage> = error("not under test")

        override suspend fun debit(
            accountId: String,
            amount: Money,
            reason: DebitReason,
            idempotencyKey: String,
        ): AppResult<WalletDebit> = error("not under test")
    }

    private class FakeSessionStore : SessionStore {
        override val current: Flow<Session?> =
            flowOf(Session(user = User(id = ACCOUNT_ID, email = "e2e@vankoo.dev"), token = "jwt"))

        override suspend fun save(session: Session) = Unit
        override suspend fun clear() = Unit
    }

    private class RecordingUrlOpener : UrlOpener {
        val opened = mutableListOf<String>()
        var answer = true

        override fun open(url: String): Boolean {
            opened += url
            return answer
        }
    }

    private companion object {
        const val DEPOSIT_ID = "01a0a7b8-5eba-7308-8fa4-9addeb96a601"
        const val ACCOUNT_ID = "01a0a7b7-107c-7a5b-8ad2-b68d066a294f"
        const val CHECKOUT_URL = "https://checkout.stripe.com/c/pay/cs_test_a1"
    }
}
