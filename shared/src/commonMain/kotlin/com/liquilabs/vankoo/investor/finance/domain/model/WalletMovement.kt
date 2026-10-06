package com.liquilabs.vankoo.investor.finance.domain.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * One line of the wallet's history.
 *
 * [kind] is the vocabulary of the wallet contract — RECARGA credits, the other three
 * debit — and [direction] is carried separately because the contract does, so a kind
 * added later comes with its sign rather than needing this file to know it.
 *
 * [sourceDepositId] is set on a top-up and [debitId] on a debit the app asked for —
 * the same value the confirm screen was answered with, and the `transactionId` of
 * the share it bought. Neither is an invoice number, so the row's subtitle is still
 * its date and nothing more.
 */
@OptIn(ExperimentalTime::class)
data class WalletMovement(
    val kind: MovementKind,
    val direction: MovementDirection,
    val amount: Money,
    val sourceDepositId: String?,
    val debitId: String?,
    val occurredAt: Instant,
) {
    /** Signed for display: a debit is negative. */
    val signedAmountMinor: Long
        get() = if (direction == MovementDirection.DEBIT) -amount.amountMinor else amount.amountMinor
}

enum class MovementKind {
    RECARGA,
    INVERSION,
    RETIRO,
    COMISION,
    /** A kind this build has not been taught. Shown generically rather than dropped. */
    UNKNOWN,
}

enum class MovementDirection { CREDIT, DEBIT }

/** A page of history, with the total so the screen knows whether to offer more. */
data class MovementPage(
    val items: List<WalletMovement>,
    val pageNumber: Int,
    val pageSize: Int,
    val totalElements: Long,
) {
    val hasMore: Boolean
        get() = (pageNumber + 1L) * pageSize < totalElements
}
