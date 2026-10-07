package com.liquilabs.vankoo.investor.core.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * An RFC 9457 body, in the shape every Vankoo service returns it.
 *
 * `code` is the extension the platform added and the only field a client may branch
 * on: the status is too coarse — two different failures share a 400 — and `title` and
 * `detail` are English prose meant for whoever reads the log. The contract is written
 * down in vankoo-docs, `docs/architecture/error-handling.md`.
 *
 * Everything is nullable because this also has to survive a body that is not really
 * one of these: a proxy's HTML error page decodes to nothing useful, and the caller
 * has to be able to tell.
 */
@Serializable
data class ProblemDetailDto(
    val type: String? = null,
    val title: String? = null,
    val status: Int? = null,
    val detail: String? = null,
    val instance: String? = null,
    val code: String? = null,
    val errors: List<ProblemViolationDto>? = null,
)

/** One broken rule, named by the field it belongs to and by the rule itself. */
@Serializable
data class ProblemViolationDto(
    @SerialName("field") val field: String? = null,
    val code: String? = null,
    /** English, for debugging. Never rendered. */
    val message: String? = null,
)
