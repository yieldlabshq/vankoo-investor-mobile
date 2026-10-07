package com.liquilabs.vankoo.investor.core.format

import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * Renders a rate the way the mockups write one: `4.2`, `12`, `62`.
 *
 * One decimal place, and the place dropped when it is a zero — a grade's TEA is
 * `12.000000` on the wire and reads as "12 %", while a term rate that really is 4.2
 * keeps its digit. Written by hand for the same reason [MoneyFormat] is: common
 * Kotlin has no number formatter, and the product prints the same thing on every
 * surface whatever the phone's locale says.
 *
 * The separator is a point, matching [MoneyFormat] and Peruvian usage. The mockups
 * write «4,2 %» with a comma next to an `S/ 18,400.00` that uses the comma for
 * thousands — two conventions in one card, where the same character means a
 * thousandth in one number and a tenth in the next. This is the deliberate deviation.
 *
 * The percent sign is not added here: it belongs to the sentence around the number
 * («%1$s %% de descuento»), which is what lets a translation move it.
 */
object PercentFormat {

    fun format(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "—"
        val tenths = (abs(value) * 10).roundToLong()
        val whole = tenths / 10
        val decimal = (tenths % 10).toInt()
        val sign = if (value < 0 && tenths != 0L) "−" else ""
        return if (decimal == 0) "$sign$whole" else "$sign$whole.$decimal"
    }
}
