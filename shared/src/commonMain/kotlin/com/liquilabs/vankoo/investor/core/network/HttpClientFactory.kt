package com.liquilabs.vankoo.investor.core.network

import com.liquilabs.vankoo.investor.core.config.AppConfig
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * The one client the app talks through.
 *
 * `expectSuccess` stays off: a 401 or a 409 is an answer this app has to read, not an
 * exception to catch. Turning it on would push every rejection through the same
 * throw-and-inspect path and make the problem+json body harder to reach than the
 * status. [safeApiCall] is where a response becomes a result.
 */
fun createVankooHttpClient(
    config: AppConfig,
    tokens: AuthTokenSource,
): HttpClient = HttpClient {
    expectSuccess = false

    install(ContentNegotiation) {
        json(
            Json {
                // The services add fields as they grow; a client that breaks on one it
                // has not been taught is a client that breaks on someone else's deploy.
                ignoreUnknownKeys = true
                isLenient = true
            }
        )
    }

    install(Logging) {
        logger = object : Logger {
            override fun log(message: String) = Napier.d(message, tag = LOG_TAG)
        }
        level = LogLevel.INFO
    }

    install(bearerToken(tokens))

    defaultRequest {
        url(config.apiBaseUrl)
        contentType(ContentType.Application.Json)
    }
}

/**
 * Attaches the bearer token to every outgoing request.
 *
 * A plugin rather than `DefaultRequest` because reading the token suspends — it comes
 * off DataStore — and `DefaultRequest`'s builder cannot wait. This is the equivalent
 * of `iam.interceptor.ts` on the web and of uflex's `AuthInterceptor`, minus the
 * `runBlocking` that OkHttp's synchronous chain forces on the latter.
 */
private fun bearerToken(tokens: AuthTokenSource) =
    createClientPlugin(BEARER_PLUGIN_NAME) {
        onRequest { request, _ ->
            if (request.headers.contains(HttpHeaders.Authorization)) return@onRequest
            val token = tokens.current() ?: return@onRequest
            request.headers.append(HttpHeaders.Authorization, "Bearer $token")
        }
    }

private const val LOG_TAG = "http"
private const val BEARER_PLUGIN_NAME = "VankooBearerToken"
