package com.liquilabs.vankoo.investor.iam.infrastructure.dto

import kotlinx.serialization.Serializable

@Serializable
data class SignUpRequestDto(
    val email: String,
    val password: String,
    val roles: List<String>,
)
