package com.liquilabs.vankoo.investor.finance.infrastructure.dto

import kotlinx.serialization.Serializable

/** Body of `POST /api/v1/deposits`. The `Idempotency-Key` travels as a header. */
@Serializable
data class CreateDepositRequestDto(
    val accountId: String,
    val amountMinor: Long,
    val currency: String,
    val provider: String,
    val description: String? = null,
)

/** `DepositResource`, the shape both the 202 and `GET /deposits/{id}` answer with. */
@Serializable
data class DepositResponseDto(
    val depositId: String,
    val accountId: String,
    val amountMinor: Long,
    val currency: String,
    val provider: String,
    val status: String,
    val actionUrl: String? = null,
    val failureReason: String? = null,
    val description: String? = null,
    val cancellationReason: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)
