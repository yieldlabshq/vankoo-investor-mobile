package com.liquilabs.vankoo.investor.iam.application

import com.liquilabs.vankoo.investor.iam.domain.repository.SessionStore

class SignOut(private val sessions: SessionStore) {
    suspend operator fun invoke() = sessions.clear()
}
