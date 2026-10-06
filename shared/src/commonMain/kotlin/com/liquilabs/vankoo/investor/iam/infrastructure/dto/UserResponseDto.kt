package com.liquilabs.vankoo.investor.iam.infrastructure.dto

import kotlinx.serialization.Serializable

/** An account, as sign-up and `GET /users/{email}` both return it. */
@Serializable
data class UserResponseDto(
    val id: String,
    val email: String,
    val roles: List<String> = emptyList(),
)
