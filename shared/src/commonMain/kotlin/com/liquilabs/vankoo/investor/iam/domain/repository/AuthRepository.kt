package com.liquilabs.vankoo.investor.iam.domain.repository

import com.liquilabs.vankoo.investor.core.result.AppResult
import com.liquilabs.vankoo.investor.iam.domain.model.Email
import com.liquilabs.vankoo.investor.iam.domain.model.Session
import com.liquilabs.vankoo.investor.iam.domain.model.User

interface AuthRepository {
    suspend fun signIn(email: Email, password: String): AppResult<Session>

    /**
     * Creates the account and returns it — no session.
     *
     * Sign-up answers 201 with the user and no token, so registering does not sign
     * anyone in. That is the service's shape, not a simplification here.
     */
    suspend fun signUp(email: Email, password: String): AppResult<User>

    /**
     * Asks IAM to send a recovery link.
     *
     * Succeeds for any address IAM accepts, whether or not it has an account: the
     * service answers the same either way, and a client that could tell them apart
     * would be a way of finding out who is registered.
     */
    suspend fun requestPasswordReset(email: Email): AppResult<Unit>

    /** Spends a recovery link on a new password. */
    suspend fun resetPassword(token: String, password: String): AppResult<Unit>
}
