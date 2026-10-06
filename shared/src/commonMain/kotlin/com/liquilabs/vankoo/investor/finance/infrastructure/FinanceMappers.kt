package com.liquilabs.vankoo.investor.finance.infrastructure

import com.liquilabs.vankoo.investor.finance.domain.model.Currency
import com.liquilabs.vankoo.investor.finance.domain.model.DebitReason
import com.liquilabs.vankoo.investor.finance.domain.model.Deposit
import com.liquilabs.vankoo.investor.finance.domain.model.DepositFailureReason
import com.liquilabs.vankoo.investor.finance.domain.model.DepositStatus
import com.liquilabs.vankoo.investor.finance.domain.model.Money
import com.liquilabs.vankoo.investor.finance.domain.model.MovementDirection
import com.liquilabs.vankoo.investor.finance.domain.model.MovementKind
import com.liquilabs.vankoo.investor.finance.domain.model.MovementPage
import com.liquilabs.vankoo.investor.finance.domain.model.Wallet
import com.liquilabs.vankoo.investor.finance.domain.model.WalletDebit
import com.liquilabs.vankoo.investor.finance.domain.model.WalletMovement
import com.liquilabs.vankoo.investor.finance.infrastructure.dto.DepositResponseDto
import com.liquilabs.vankoo.investor.finance.infrastructure.dto.WalletDebitResponseDto
import com.liquilabs.vankoo.investor.finance.infrastructure.dto.WalletMovementDto
import com.liquilabs.vankoo.investor.finance.infrastructure.dto.WalletMovementPageDto
import com.liquilabs.vankoo.investor.finance.infrastructure.dto.WalletResponseDto
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Wire shapes into domain ones.
 *
 * Every enum is read leniently: a value this build has not been taught becomes the
 * UNKNOWN member rather than an exception, because Finance adding a status must not
 * crash a phone that has not been updated.
 */

internal fun WalletResponseDto.toDomain(): Wallet = Wallet(
    id = walletId,
    accountId = accountId,
    balance = Money(balanceMinor, currency.toCurrency()),
)

@OptIn(ExperimentalTime::class)
internal fun WalletMovementPageDto.toDomain(): MovementPage = MovementPage(
    items = items.map(WalletMovementDto::toDomain),
    pageNumber = pageNumber,
    pageSize = pageSize,
    totalElements = totalElements,
)

@OptIn(ExperimentalTime::class)
internal fun WalletMovementDto.toDomain(): WalletMovement = WalletMovement(
    kind = MovementKind.entries.firstOrNull { it.name == type } ?: MovementKind.UNKNOWN,
    direction = MovementDirection.entries.firstOrNull { it.name == direction } ?: MovementDirection.CREDIT,
    amount = Money(amountMinor, currency.toCurrency()),
    sourceDepositId = sourceDepositId,
    debitId = debitId,
    occurredAt = Instant.parse(occurredAt),
)

internal fun WalletDebitResponseDto.toDomain(): WalletDebit = WalletDebit(
    id = debitId,
    walletId = walletId,
    accountId = accountId,
    amount = Money(amountMinor, currency.toCurrency()),
    reason = DebitReason.entries.firstOrNull { it.name == reason } ?: DebitReason.UNKNOWN,
)

@OptIn(ExperimentalTime::class)
internal fun DepositResponseDto.toDomain(): Deposit = Deposit(
    id = depositId,
    accountId = accountId,
    amount = Money(amountMinor, currency.toCurrency()),
    status = DepositStatus.entries.firstOrNull { it.name == status } ?: DepositStatus.UNKNOWN,
    actionUrl = actionUrl,
    failureReason = failureReason?.toFailureReason(),
    cancellationReason = cancellationReason?.toFailureReason(),
    createdAt = createdAt?.let(::parseInstantOrNull),
    updatedAt = updatedAt?.let(::parseInstantOrNull),
)

/** Both reason fields share Finance's five normalised values, so one reading serves them. */
private fun String.toFailureReason(): DepositFailureReason =
    DepositFailureReason.entries.firstOrNull { it.name == this } ?: DepositFailureReason.UNKNOWN

/**
 * Finance always sends PEN or USD, so an unknown code here is a bug on one side or
 * the other. PEN is the fallback because it is the product's currency, and because a
 * wrong symbol next to a right number is recoverable where a crash is not.
 */
private fun String.toCurrency(): Currency = Currency.fromIsoCode(this) ?: Currency.PEN

@OptIn(ExperimentalTime::class)
private fun parseInstantOrNull(text: String): Instant? = runCatching { Instant.parse(text) }.getOrNull()
