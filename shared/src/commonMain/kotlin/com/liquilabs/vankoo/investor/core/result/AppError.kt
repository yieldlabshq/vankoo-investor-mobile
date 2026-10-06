package com.liquilabs.vankoo.investor.core.result

/**
 * Why an operation failed.
 *
 * Transport failures are modelled here because they mean the same thing in
 * every context. What a business rule rejected does not: each context declares
 * its own reasons and tags them [BusinessReason], because a sealed hierarchy
 * cannot be extended from another package.
 */
sealed interface AppError {
    /** No usable connection, or the request never reached the server. */
    data object Network : AppError

    /** The server answered, but not with something we can use. */
    data class Server(val status: Int) : AppError

    /** The server rejected the request for a reason the domain understands. */
    data class Business(val reason: BusinessReason) : AppError

    /** A bug, a malformed payload, anything we did not plan for. */
    data class Unexpected(val cause: Throwable? = null) : AppError
}

/**
 * Marker for a context's own failure reasons.
 *
 * Implementations are closed enums, so the screen can branch on them by name.
 * The raw code a service sends never reaches here — infrastructure translates
 * it at the boundary, which is what keeps a rename on the backend from
 * rippling inwards.
 */
interface BusinessReason
