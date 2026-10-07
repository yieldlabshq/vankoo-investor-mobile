package com.liquilabs.vankoo.investor.iam.infrastructure.dto

import kotlinx.serialization.Serializable

@Serializable
data class ResetPasswordRequestDto(
    val token: String,
    val password: String,
)
