package com.liquilabs.vankoo.investor.iam.infrastructure.dto

import kotlinx.serialization.Serializable

/**
 * What sign-in answers with.
 *
 * No roles: the service puts them in the token instead, which is why the session is
 * assembled by reading the payload rather than by asking `GET /users/{email}` again.
 */
@Serializable
data class SignInResponseDto(
    val id: String,
    val email: String,
    val token: String,
)
