package com.liquilabs.vankoo.investor.core.network

import com.liquilabs.vankoo.investor.core.result.AppError
import com.liquilabs.vankoo.investor.core.result.AppResult
import com.liquilabs.vankoo.investor.core.result.BusinessReason
import io.github.aakira.napier.Napier
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.serialization.SerializationException

/**
 * The one place an HTTP response becomes an [AppResult].
 *
 * Every repository funnels through here so the rules below are decided once:
 *
 * - a 2xx is the payload;
 * - a problem+json whose `code` this context recognises is a business failure, and
 *   one it does not recognise is a plain server error. That fallback is the contract:
 *   a client that meets a code it was never taught shows the generic message rather
 *   than breaking;
 * - a 401 with an empty body is a session that ran out. It is the shape the gateway
 *   returns from its own filter, which never learned the format — so the most common
 *   failure a client sees is the one answer with no `code` in it. The web reports it
 *   as "we could not reach Vankoo", which is a lie about the network; here it is read
 *   as if it had arrived with `unauthenticated`;
 * - anything that stopped the request from completing is [AppError.Network], and a
 *   body we could not parse is [AppError.Unexpected]. Those two must not be confused:
 *   one is worth retrying and the other is a bug.
 */
suspend inline fun <reified T> safeApiCall(
    crossinline businessReason: (String) -> BusinessReason?,
    crossinline call: suspend () -> HttpResponse,
): AppResult<T> = try {
    val response = call()
    if (response.status.isSuccess()) {
        AppResult.Success(response.body<T>())
    } else {
        AppResult.Failure(response.toAppError { businessReason(it) })
    }
} catch (failure: SerializationException) {
    Napier.e("Could not read the response body", failure, tag = "http")
    AppResult.Failure(AppError.Unexpected(failure))
} catch (failure: Throwable) {
    if (failure.isTransport()) {
        Napier.w("The request never completed: ${failure.message}", tag = "http")
        AppResult.Failure(AppError.Network)
    } else {
        Napier.e("Unexpected failure", failure, tag = "http")
        AppResult.Failure(AppError.Unexpected(failure))
    }
}

/**
 * The same funnel for a call whose success has no body to read.
 *
 * A 202 or a 204 is an answer, not an empty payload: asking the decoder for a value
 * that the contract says is not there would turn a correct response into a
 * SerializationException. The failure path is identical — that is the whole point of
 * it living beside [safeApiCall] rather than being written again per call site.
 */
suspend fun safeEmptyApiCall(
    businessReason: (String) -> BusinessReason?,
    call: suspend () -> HttpResponse,
): AppResult<Unit> = try {
    val response = call()
    if (response.status.isSuccess()) {
        AppResult.Success(Unit)
    } else {
        AppResult.Failure(response.toAppError(businessReason))
    }
} catch (failure: Throwable) {
    if (failure.isTransport()) {
        Napier.w("The request never completed: ${failure.message}", tag = "http")
        AppResult.Failure(AppError.Network)
    } else {
        Napier.e("Unexpected failure", failure, tag = "http")
        AppResult.Failure(AppError.Unexpected(failure))
    }
}

/** Reads the failure out of a response that already came back non-2xx. */
suspend fun HttpResponse.toAppError(businessReason: (String) -> BusinessReason?): AppError {
    val problem = runCatching { body<ProblemDetailDto>() }.getOrNull()
    val code = problem?.code
        ?: UNAUTHENTICATED_CODE.takeIf { status == HttpStatusCode.Unauthorized }
        ?: return AppError.Server(status.value)

    return businessReason(code)
        ?.let { AppError.Business(it) }
        ?: AppError.Server(status.value)
}

/**
 * Whether the request failed on the way out rather than being answered.
 *
 * Matched by name instead of by type: the transport exceptions live in different
 * packages on each engine, and Ktor's own IO type has moved between releases. What
 * matters to a screen is only that nothing came back.
 */
fun Throwable.isTransport(): Boolean {
    val name = this::class.simpleName.orEmpty()
    return name.endsWith("IOException") ||
        name.endsWith("TimeoutException") ||
        name.endsWith("SocketException") ||
        name.endsWith("UnknownHostException") ||
        name.endsWith("ConnectException") ||
        cause?.takeIf { it !== this }?.isTransport() == true
}

/** What the gateway means when it answers 401 without saying anything. */
const val UNAUTHENTICATED_CODE = "unauthenticated"
