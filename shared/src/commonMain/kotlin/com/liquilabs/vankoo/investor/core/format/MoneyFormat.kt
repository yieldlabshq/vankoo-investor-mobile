package com.liquilabs.vankoo.investor.core.format

import kotlin.math.abs

/**
 * Renders an amount in minor units the way the mockups write money: `S/ 3,200.00`.
 *
 * Written by hand because there is no `NumberFormat` in common Kotlin, and the
 * product has one convention on every surface regardless of the phone's locale — a
 * comma to group thousands, a point before the two decimals, a space after the
 * symbol. The sign, when asked for, goes before the symbol: `+ S/ 5,000.00`.
 */
object MoneyFormat {

    fun format(amountMinor: Long, symbol: String, signed: Boolean = false): String {
        val magnitude = abs(amountMinor)
        val units = magnitude / 100
        val cents = (magnitude % 100).toInt()
        val body = "$symbol ${groupThousands(units)}.${cents.toString().padStart(2, '0')}"
        return when {
            !signed -> body
            amountMinor < 0 -> "− $body"
            else -> "+ $body"
        }
    }

    /** `S/ 1,000` — the whole-unit form the suggestion chips use. */
    fun formatWhole(amountMinor: Long, symbol: String): String =
        "$symbol ${groupThousands(abs(amountMinor) / 100)}"

    /**
     * Reads what someone typed into an amount field back into minor units.
     *
     * Accepts digits with an optional point and up to two decimals; grouping commas
     * are ignored so a pasted `5,000` works. Anything else — a second point, letters,
     * more than two decimals — is null, which the screen treats as "not a number yet".
     */
    fun parseMinor(text: String): Long? {
        val cleaned = text.replace(",", "").trim()
        if (cleaned.isEmpty()) return null
        if (!AMOUNT_PATTERN.matches(cleaned)) return null
        val (whole, fraction) = cleaned.split('.').let { it[0] to it.getOrElse(1) { "" } }
        val units = whole.ifEmpty { "0" }.toLongOrNull() ?: return null
        val cents = fraction.padEnd(2, '0').toLong()
        return units * 100 + cents
    }

    private fun groupThousands(units: Long): String {
        val digits = units.toString()
        val out = StringBuilder()
        digits.forEachIndexed { index, char ->
            if (index > 0 && (digits.length - index) % 3 == 0) out.append(',')
            out.append(char)
        }
        return out.toString()
    }

    private val AMOUNT_PATTERN = Regex("""^\d{0,15}(\.\d{0,2})?$""")
}
