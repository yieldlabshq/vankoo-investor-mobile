package com.liquilabs.vankoo.investor.iam.domain.model

import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * A signed-in identity and the credential that proves it.
 *
 * The token is opaque to everything that uses a session: it is replayed to the
 * gateway and never inspected. The one exception is [expiresAt], read once at the
 * boundary so a session restored from storage can be thrown away before it is used.
 * That is a convenience, not a security check — whether a token is valid is the
 * server's answer and only the server holds the key.
 */
data class Session(
    val user: User,
    val token: String,
    /** Seconds since the epoch, from the token's `exp`, or null if it had none. */
    val expiresAt: Long? = null,
) {
    /** A token with no expiry we could read is kept: only the server can refuse it. */
    @OptIn(ExperimentalTime::class)
    fun isExpired(nowEpochSeconds: Long = Clock.System.now().epochSeconds): Boolean =
        expiresAt != null && expiresAt <= nowEpochSeconds
}
