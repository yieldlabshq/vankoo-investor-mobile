package com.liquilabs.vankoo.investor.iam.application

import com.liquilabs.vankoo.investor.core.result.AppResult
import com.liquilabs.vankoo.investor.iam.domain.model.Email
import com.liquilabs.vankoo.investor.iam.domain.repository.AuthRepository

/**
 * Starts a password recovery.
 *
 * There is nothing to coordinate and nothing to keep: the answer says only that the
 * request was accepted, never whether a mail followed.
 */
class RequestPasswordReset(private val repository: AuthRepository) {
    suspend operator fun invoke(email: Email): AppResult<Unit> = repository.requestPasswordReset(email)
}
