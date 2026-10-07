package com.liquilabs.vankoo.investor.iam.infrastructure

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.liquilabs.vankoo.investor.iam.domain.model.Session
import com.liquilabs.vankoo.investor.iam.domain.model.User
import com.liquilabs.vankoo.investor.iam.domain.repository.SessionStore
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * The session, kept across launches.
 *
 * **Only the token is written.** Everything else in a [Session] is already inside it,
 * so storing the id and the address as well would be two copies of one fact that can
 * disagree — and the web keeps only the token in localStorage for the same reason.
 * Reading the claims back is not a security decision: an expired token is dropped
 * here to avoid a request that is certain to be refused, and the server still decides
 * whether the rest of them are any good.
 */
class DataStoreSessionStore(
    private val store: DataStore<Preferences>,
) : SessionStore {

    override val current: Flow<Session?> = store.data
        .catch { failure ->
            // A store that cannot be read is a store with nobody signed in. Throwing
            // here would take down whatever screen happens to be collecting.
            Napier.e("Could not read the session", failure, tag = "iam")
            emit(emptyPreferences())
        }
        .map { preferences -> preferences[TOKEN]?.let(::sessionFrom) }

    override suspend fun save(session: Session) {
        store.edit { it[TOKEN] = session.token }
    }

    override suspend fun clear() {
        store.edit { it.remove(TOKEN) }
    }

    private fun sessionFrom(token: String): Session? {
        val payload = JwtPayload.of(token) ?: return null
        val session = Session(
            user = User(id = payload.subject.orEmpty(), email = payload.email.orEmpty()),
            token = token,
            expiresAt = payload.expiresAt,
        )
        return session.takeIf { !it.isExpired() }
    }

    private companion object {
        val TOKEN = stringPreferencesKey("iam.token")
    }
}
