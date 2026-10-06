package com.liquilabs.vankoo.investor.iam.domain.model

import com.liquilabs.vankoo.investor.core.result.BusinessReason

/**
 * Every reason IAM gives for turning a request down.
 *
 * The list is the contract's, not a flow's: `validation-failed` comes back from
 * signing up and from signing in alike, so grouping these by screen would mean
 * writing the same code twice and then disagreeing about it. What a screen does with
 * one is a presentation decision.
 *
 * The wire strings stay at the boundary — [fromCode] is the only place they appear —
 * so a rename on the server reaches exactly one file.
 */
enum class IamFailure(val code: String) : BusinessReason {
    EmailAlreadyInUse("email-already-in-use"),
    InvalidCredentials("invalid-credentials"),
    InvalidPasswordResetToken("invalid-password-reset-token"),
    RoleNotAllowed("role-not-allowed"),
    ValidationFailed("validation-failed"),
    InvalidRequest("invalid-request"),
    UserNotFound("user-not-found"),
    Unauthenticated("unauthenticated"),
    Forbidden("forbidden"),
    ;

    companion object {
        private val BY_CODE = entries.associateBy(IamFailure::code)

        /**
         * The failure a code names, or null when this build has never heard of it.
         *
         * Null is not a bug: it is how a client that has fallen behind the services
         * ends up showing a generic message instead of crashing.
         */
        fun fromCode(code: String): IamFailure? = BY_CODE[code]
    }
}
