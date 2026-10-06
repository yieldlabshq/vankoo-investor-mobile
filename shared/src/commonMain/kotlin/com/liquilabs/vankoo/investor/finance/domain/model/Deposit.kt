package com.liquilabs.vankoo.investor.finance.domain.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * A top-up, as Finance projects it.
 *
 * The one the app cares about most is a deposit that is still PENDING but already
 * has an [actionUrl]: that is the moment Stripe's checkout page exists and the person
 * can be sent to it. The status alone does not say so — the URL arrives on the
 * projection a beat after the 202, when the charge has been created — which is why
 * [canProceedToProvider] reads the URL and not the status.
 */
@OptIn(ExperimentalTime::class)
data class Deposit(
    val id: String,
    val accountId: String,
    val amount: Money,
    val status: DepositStatus,
    val actionUrl: String?,
    val failureReason: DepositFailureReason?,
    /** Why a CANCELLED deposit was cancelled; Finance reports it apart from a failure. */
    val cancellationReason: DepositFailureReason?,
    val createdAt: Instant?,
    val updatedAt: Instant?,
) {
    val canProceedToProvider: Boolean
        get() = actionUrl != null && !status.isTerminal

    val succeeded: Boolean
        get() = status == DepositStatus.SUCCEEDED

    /**
     * Why the top-up did not go through, whichever field Finance used.
     *
     * A declined charge arrives as [failureReason] on a FAILED deposit; an expired
     * Stripe session arrives as [cancellationReason] on a CANCELLED one, with
     * [failureReason] null. The failure screen wants the reason regardless of which
     * door it came through, so it reads this and not either field.
     */
    val outcomeReason: DepositFailureReason?
        get() = failureReason ?: cancellationReason
}

enum class DepositStatus(val isTerminal: Boolean) {
    PENDING(false),
    ACTION_REQUIRED(false),
    PROCESSING(false),
    SUCCEEDED(true),
    FAILED(true),
    CANCELLED(true),
    /** A status this build has not been taught. Treated as still in flight. */
    UNKNOWN(false),
}

/** Why a top-up failed, normalised by Finance to these five. */
enum class DepositFailureReason {
    DECLINED,
    EXPIRED,
    INVALID_PAYMENT_METHOD,
    PROVIDER_ERROR,
    UNKNOWN,
}
