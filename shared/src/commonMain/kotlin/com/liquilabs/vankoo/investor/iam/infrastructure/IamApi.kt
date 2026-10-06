package com.liquilabs.vankoo.investor.iam.infrastructure

import com.liquilabs.vankoo.investor.core.network.safeApiCall
import com.liquilabs.vankoo.investor.core.network.safeEmptyApiCall
import com.liquilabs.vankoo.investor.core.result.AppResult
import com.liquilabs.vankoo.investor.iam.domain.model.IamFailure
import com.liquilabs.vankoo.investor.iam.infrastructure.dto.ForgotPasswordRequestDto
import com.liquilabs.vankoo.investor.iam.infrastructure.dto.ResetPasswordRequestDto
import com.liquilabs.vankoo.investor.iam.infrastructure.dto.SignInRequestDto
import com.liquilabs.vankoo.investor.iam.infrastructure.dto.SignInResponseDto
import com.liquilabs.vankoo.investor.iam.infrastructure.dto.SignUpRequestDto
import com.liquilabs.vankoo.investor.iam.infrastructure.dto.UserResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody

/**
 * The IAM service, as this app reaches it.
 *
 * Every call names [IamFailure.Companion.fromCode] as its translator, which is what
 * keeps the wire codes from travelling any further inwards.
 *
 * The paths carry the `/iam` prefix themselves because the gateway routes by service
 * prefix, so one base URL serves every context.
 */
class IamApi(private val client: HttpClient) {

    suspend fun signIn(request: SignInRequestDto): AppResult<SignInResponseDto> =
        safeApiCall(IamFailure::fromCode) {
            client.post(SIGN_IN_PATH) { setBody(request) }
        }

    suspend fun signUp(request: SignUpRequestDto): AppResult<UserResponseDto> =
        safeApiCall(IamFailure::fromCode) {
            client.post(SIGN_UP_PATH) { setBody(request) }
        }

    /**
     * Asks for a recovery link.
     *
     * Answers 202 with no body, the same for an address with an account and one
     * without, so there is nothing to decode and nothing to tell apart.
     */
    suspend fun forgotPassword(request: ForgotPasswordRequestDto): AppResult<Unit> =
        safeEmptyApiCall(IamFailure::fromCode) {
            client.post(FORGOT_PASSWORD_PATH) { setBody(request) }
        }

    /** Spends a recovery link on a new password. Answers 204, or 400 with a code. */
    suspend fun resetPassword(request: ResetPasswordRequestDto): AppResult<Unit> =
        safeEmptyApiCall(IamFailure::fromCode) {
            client.post(RESET_PASSWORD_PATH) { setBody(request) }
        }

    /**
     * Reads an account by its address, which is how this API addresses users.
     *
     * No screen calls it yet — the same as on the web, where it also exists without a
     * caller. It is the protected route, so it is what proves the bearer token is
     * actually travelling.
     */
    suspend fun getUserByEmail(email: String): AppResult<UserResponseDto> =
        safeApiCall(IamFailure::fromCode) {
            client.get("$USERS_PATH/$email")
        }

    private companion object {
        const val SIGN_IN_PATH = "/iam/api/v1/authentication/sign-in"
        const val SIGN_UP_PATH = "/iam/api/v1/authentication/sign-up"
        const val FORGOT_PASSWORD_PATH = "/iam/api/v1/authentication/forgot-password"
        const val RESET_PASSWORD_PATH = "/iam/api/v1/authentication/reset-password"
        const val USERS_PATH = "/iam/api/v1/users"
    }
}
