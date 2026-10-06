package com.liquilabs.vankoo.investor.finance.infrastructure

import com.liquilabs.vankoo.investor.finance.domain.model.Currency
import com.liquilabs.vankoo.investor.finance.domain.model.DebitReason
import com.liquilabs.vankoo.investor.finance.domain.model.DepositFailureReason
import com.liquilabs.vankoo.investor.finance.domain.model.DepositStatus
import com.liquilabs.vankoo.investor.finance.domain.model.Money
import com.liquilabs.vankoo.investor.finance.infrastructure.dto.DepositResponseDto
import com.liquilabs.vankoo.investor.finance.infrastructure.dto.WalletDebitResponseDto
import com.liquilabs.vankoo.investor.finance.infrastructure.dto.WalletMovementDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * The deposit projection as Finance really sends it, read into the domain.
 *
 * The shapes here are copied from the wire during the end-to-end run of the top-up
 * flow: a declined charge is FAILED with a `failureReason`; an expired Checkout
 * session is CANCELLED with the reason in `cancellationReason` and `failureReason`
 * null. The failure screen must say the right thing for both.
 */
class FinanceMappersTest {

    @Test
    fun aFailedDepositKeepsItsFailureReason() {
        val deposit = dto(status = "FAILED", failureReason = "DECLINED").toDomain()

        assertEquals(DepositStatus.FAILED, deposit.status)
        assertEquals(DepositFailureReason.DECLINED, deposit.failureReason)
        assertNull(deposit.cancellationReason)
        assertEquals(DepositFailureReason.DECLINED, deposit.outcomeReason)
    }

    @Test
    fun anExpiredSessionIsCancelledAndTheOutcomeReasonSaysExpired() {
        val deposit = dto(status = "CANCELLED", cancellationReason = "EXPIRED").toDomain()

        assertEquals(DepositStatus.CANCELLED, deposit.status)
        assertNull(deposit.failureReason)
        assertEquals(DepositFailureReason.EXPIRED, deposit.cancellationReason)
        assertEquals(DepositFailureReason.EXPIRED, deposit.outcomeReason)
    }

    @Test
    fun aFailureReasonWinsOverACancellationReasonWhenBothArrive() {
        val deposit = dto(
            status = "FAILED",
            failureReason = "PROVIDER_ERROR",
            cancellationReason = "EXPIRED",
        ).toDomain()

        assertEquals(DepositFailureReason.PROVIDER_ERROR, deposit.outcomeReason)
    }

    @Test
    fun unknownReasonsBecomeUnknownNotACrash() {
        val deposit = dto(status = "CANCELLED", cancellationReason = "RISK_BLOCKED").toDomain()

        assertEquals(DepositFailureReason.UNKNOWN, deposit.cancellationReason)
        assertEquals(DepositFailureReason.UNKNOWN, deposit.outcomeReason)
    }

    @Test
    fun aPendingDepositHasNoOutcomeReason() {
        val deposit = dto(status = "PENDING").toDomain()

        assertNull(deposit.outcomeReason)
    }

    @Test
    fun anUnknownStatusIsTreatedAsStillInFlight() {
        val deposit = dto(status = "SETTLING").toDomain()

        assertEquals(DepositStatus.UNKNOWN, deposit.status)
    }

    // The 201 of POST .../wallets/PEN/debits, as Finance answered it end to end.
    @Test
    fun aDebitKeepsItsIdAndReason() {
        val debit = WalletDebitResponseDto(
            debitId = "01a0aac5-73f1-70ae-8fa9-7b18e412717a",
            walletId = "a74adfa0-3de7-5757-ae85-14078f1b3a41",
            accountId = "01a0a7b7-107c-7a5b-8ad2-b68d066a294f",
            currency = "PEN",
            amountMinor = 5_000,
            reason = "INVERSION",
        ).toDomain()

        assertEquals("01a0aac5-73f1-70ae-8fa9-7b18e412717a", debit.id)
        assertEquals(Money(5_000, Currency.PEN), debit.amount)
        assertEquals(DebitReason.INVERSION, debit.reason)
    }

    // A movement row for that debit carries the same id; a top-up row carries none.
    @Test
    fun aDebitMovementCarriesItsDebitIdAndATopUpDoesNot() {
        val debit = movementDto(type = "INVERSION", direction = "DEBIT",
            debitId = "01a0aac5-73f1-70ae-8fa9-7b18e412717a").toDomain()
        val topUp = movementDto(type = "RECARGA", direction = "CREDIT",
            sourceDepositId = "01a0a805-1dfc-738e-b99c-e01fd6f0f83a").toDomain()

        assertEquals("01a0aac5-73f1-70ae-8fa9-7b18e412717a", debit.debitId)
        assertEquals(-5_000L, debit.signedAmountMinor)
        assertNull(topUp.debitId)
    }

    private fun movementDto(
        type: String,
        direction: String,
        sourceDepositId: String? = null,
        debitId: String? = null,
    ) = WalletMovementDto(
        type = type,
        direction = direction,
        amountMinor = 5_000,
        currency = "PEN",
        sourceDepositId = sourceDepositId,
        debitId = debitId,
        occurredAt = "2026-09-16T15:11:02.234Z",
    )

    private fun dto(
        status: String,
        failureReason: String? = null,
        cancellationReason: String? = null,
    ) = DepositResponseDto(
        depositId = "01a0a7bb-5540-70a4-8fdc-fe62e9336306",
        accountId = "01a0a7b7-107c-7a5b-8ad2-b68d066a294f",
        amountMinor = 100_000,
        currency = "PEN",
        provider = "STRIPE",
        status = status,
        actionUrl = "https://checkout.stripe.com/c/pay/cs_test_a1",
        failureReason = failureReason,
        cancellationReason = cancellationReason,
        createdAt = "2026-09-16T01:00:24.984Z",
        updatedAt = "2026-09-16T01:04:02.101Z",
    )
}
