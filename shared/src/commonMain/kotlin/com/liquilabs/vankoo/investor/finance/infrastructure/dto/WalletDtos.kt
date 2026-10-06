package com.liquilabs.vankoo.investor.finance.infrastructure.dto

import kotlinx.serialization.Serializable

/** `GET /api/v1/accounts/{accountId}/wallets/{currency}`, mirroring `WalletResource`. */
@Serializable
data class WalletResponseDto(
    val walletId: String,
    val accountId: String,
    val currency: String,
    val balanceMinor: Long,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

/** `GET .../wallets/{currency}/movements`, mirroring `WalletMovementPageResource`. */
@Serializable
data class WalletMovementPageDto(
    val items: List<WalletMovementDto>,
    val pageNumber: Int,
    val pageSize: Int,
    val totalElements: Long,
)

@Serializable
data class WalletMovementDto(
    val type: String,
    val direction: String,
    val amountMinor: Long,
    val currency: String,
    val sourceDepositId: String? = null,
    val debitId: String? = null,
    val occurredAt: String,
)

/** Body of `POST .../wallets/{currency}/debits`, mirroring `CreateWalletDebitResource`. */
@Serializable
data class CreateWalletDebitRequestDto(
    val amountMinor: Long,
    /** A `WalletMovementType` name. The app only ever sends `INVERSION`. */
    val reason: String,
)

/** The 201 of a debit, mirroring `WalletDebitResource`. */
@Serializable
data class WalletDebitResponseDto(
    val debitId: String,
    val walletId: String,
    val accountId: String,
    val currency: String,
    val amountMinor: Long,
    val reason: String,
)
