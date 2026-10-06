package com.liquilabs.vankoo.investor.iam.domain.model

import kotlin.jvm.JvmInline

/**
 * An address the app is willing to send to IAM.
 *
 * The check is deliberately shallow — shape only. Whether the account exists is
 * the server's answer, not something the client can know.
 */
@JvmInline
value class Email(val value: String) {
    companion object {
        private val SHAPE = Regex("""^[^@\s]+@[^@\s]+\.[^@\s]+$""")

        fun isValid(raw: String): Boolean = SHAPE.matches(raw.trim())
    }
}
