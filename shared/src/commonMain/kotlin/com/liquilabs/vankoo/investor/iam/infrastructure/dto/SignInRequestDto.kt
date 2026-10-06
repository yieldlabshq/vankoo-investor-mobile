package com.liquilabs.vankoo.investor.iam.infrastructure.dto

import kotlinx.serialization.Serializable

@Serializable
data class SignInRequestDto(
    val email: String,
    val password: String,
)
