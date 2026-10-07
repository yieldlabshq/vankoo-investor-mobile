package com.liquilabs.vankoo.investor.iam.application

import com.liquilabs.vankoo.investor.iam.domain.model.Session
import com.liquilabs.vankoo.investor.iam.domain.repository.SessionStore
import kotlinx.coroutines.flow.Flow

/** Emits the current session, or null while nobody is signed in. */
class ObserveSession(private val sessions: SessionStore) {
    operator fun invoke(): Flow<Session?> = sessions.current
}
