package com.liquilabs.vankoo.investor.iam.domain.repository

import com.liquilabs.vankoo.investor.iam.domain.model.Session
import kotlinx.coroutines.flow.Flow

/**
 * Where the current session lives between screens, and eventually between
 * launches. The interface says nothing about how it is stored, which is what
 * lets the token move to the Keychain and the Keystore without touching a
 * single caller.
 */
interface SessionStore {
    val current: Flow<Session?>

    suspend fun save(session: Session)

    suspend fun clear()
}
