package com.liquilabs.vankoo.investor.iam.infrastructure

import com.liquilabs.vankoo.investor.iam.domain.model.Session
import com.liquilabs.vankoo.investor.iam.domain.model.User
import com.liquilabs.vankoo.investor.iam.infrastructure.dto.SignInResponseDto
import com.liquilabs.vankoo.investor.iam.infrastructure.dto.UserResponseDto

/**
 * Where a wire shape becomes a domain one.
 *
 * They live together rather than one per DTO so the whole translation of this context
 * can be read at once — it is small, and the interesting part is that there is only
 * one of them.
 */
fun SignInResponseDto.toDomain(): Session = Session(
    user = User(id = id, email = email),
    token = token,
    expiresAt = JwtPayload.of(token)?.expiresAt,
)

fun UserResponseDto.toDomain(): User = User(id = id, email = email)
