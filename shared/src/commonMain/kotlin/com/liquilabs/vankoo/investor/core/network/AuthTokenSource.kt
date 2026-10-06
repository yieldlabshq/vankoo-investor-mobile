package com.liquilabs.vankoo.investor.core.network

/**
 * Where the HTTP client gets the credential it replays.
 *
 * It is declared here, in core, and implemented by whoever owns the session, so the
 * client never imports a bounded context. Same rule the web follows: the store hands
 * the interceptor to the gateway rather than the gateway reaching into the store.
 */
fun interface AuthTokenSource {
    /** The token to send, or null while nobody is signed in. */
    suspend fun current(): String?
}
