package com.liquilabs.vankoo.investor.iam.infrastructure

import io.github.aakira.napier.Napier
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * The claims IAM puts in the token.
 *
 * Only `exp` is used, and only to drop a stored session that has already run out
 * before making a request that is certain to be refused. Nothing here is verified:
 * the signing secret never leaves the server, so this reads what the server already
 * said rather than deciding anything. `session.assembler.ts` on the web does the same
 * for the same reason.
 */
@Serializable
data class JwtPayload(
    @SerialName("sub") val subject: String? = null,
    val email: String? = null,
    val roles: List<String> = emptyList(),
    @SerialName("exp") val expiresAt: Long? = null,
) {
    companion object {
        private val json = Json { ignoreUnknownKeys = true; isLenient = true }

        /** Reads the payload segment, or null when the token is not one we can read. */
        @OptIn(ExperimentalEncodingApi::class)
        fun of(token: String): JwtPayload? {
            val segment = token.split('.').getOrNull(1) ?: return null
            return try {
                // Base64url, and JWT segments come unpadded — the decoder wants the
                // padding back before it will look at them.
                val padded = segment.padEnd(segment.length + (4 - segment.length % 4) % 4, '=')
                json.decodeFromString<JwtPayload>(Base64.UrlSafe.decode(padded).decodeToString())
            } catch (failure: Exception) {
                Napier.w("Unreadable token payload: ${failure.message}", tag = "iam")
                null
            }
        }
    }
}
