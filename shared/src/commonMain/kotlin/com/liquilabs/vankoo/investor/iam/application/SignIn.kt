package com.liquilabs.vankoo.investor.iam.application

import com.liquilabs.vankoo.investor.core.result.AppResult
import com.liquilabs.vankoo.investor.core.result.onSuccess
import com.liquilabs.vankoo.investor.iam.domain.model.Email
import com.liquilabs.vankoo.investor.iam.domain.model.Session
import com.liquilabs.vankoo.investor.iam.domain.repository.AuthRepository
import com.liquilabs.vankoo.investor.iam.domain.repository.SessionStore

/**
 * Signs the investor in and leaves the session where the rest of the app can
 * find it.
 *
 * Coordination, not rules: it holds no business logic of its own, which is why
 * it lives in application rather than domain.
 */
class SignIn(
    private val repository: AuthRepository,
    private val sessions: SessionStore,
) {
    suspend operator fun invoke(email: Email, password: String): AppResult<Session> =
        repository.signIn(email, password).onSuccess { sessions.save(it) }
}
