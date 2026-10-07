package com.liquilabs.vankoo.investor.iam.infrastructure

import com.liquilabs.vankoo.investor.core.result.AppResult
import com.liquilabs.vankoo.investor.core.result.map
import com.liquilabs.vankoo.investor.iam.domain.model.Email
import com.liquilabs.vankoo.investor.iam.domain.model.Role
import com.liquilabs.vankoo.investor.iam.domain.model.Session
import com.liquilabs.vankoo.investor.iam.domain.model.User
import com.liquilabs.vankoo.investor.iam.domain.repository.AuthRepository
import com.liquilabs.vankoo.investor.iam.infrastructure.dto.ForgotPasswordRequestDto
import com.liquilabs.vankoo.investor.iam.infrastructure.dto.ResetPasswordRequestDto
import com.liquilabs.vankoo.investor.iam.infrastructure.dto.SignInRequestDto
import com.liquilabs.vankoo.investor.iam.infrastructure.dto.SignUpRequestDto

/** IAM over HTTP. It builds the request, and the mappers do the rest. */
class HttpAuthRepository(private val api: IamApi) : AuthRepository {

    override suspend fun signIn(email: Email, password: String): AppResult<Session> =
        api.signIn(SignInRequestDto(email = email.value, password = password))
            .map { it.toDomain() }

    override suspend fun signUp(email: Email, password: String): AppResult<User> =
        api.signUp(
            SignUpRequestDto(
                email = email.value,
                password = password,
                // Never omitted. See Role for what happens when it is.
                roles = listOf(Role.INVESTOR),
            )
        ).map { it.toDomain() }

    override suspend fun requestPasswordReset(email: Email): AppResult<Unit> =
        api.forgotPassword(ForgotPasswordRequestDto(email = email.value))

    override suspend fun resetPassword(token: String, password: String): AppResult<Unit> =
        api.resetPassword(ResetPasswordRequestDto(token = token, password = password))
}
