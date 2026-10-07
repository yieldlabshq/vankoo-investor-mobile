package com.liquilabs.vankoo.investor.iam.application

import com.liquilabs.vankoo.investor.core.result.AppResult
import com.liquilabs.vankoo.investor.iam.domain.repository.AuthRepository

/**
 * Sets a new password from a link received by email.
 *
 * It leaves no session behind, like [SignUp] and for the same reason: the service
 * answers without a token, so the next step is signing in with what was just chosen.
 */
class ResetPassword(private val repository: AuthRepository) {
    suspend operator fun invoke(token: String, password: String): AppResult<Unit> =
        repository.resetPassword(token, password)
}
