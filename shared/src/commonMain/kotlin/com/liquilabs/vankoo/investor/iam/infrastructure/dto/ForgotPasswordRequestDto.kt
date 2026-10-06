package com.liquilabs.vankoo.investor.iam.infrastructure.dto

import kotlinx.serialization.Serializable

@Serializable
data class ForgotPasswordRequestDto(
    val email: String,
)
