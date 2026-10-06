package com.liquilabs.vankoo.investor.investment.domain.model

/**
 * An amount in minor units of one currency.
 *
 * Deliberately this context's own type and not the finance one, although the two are
 * spelled the same. They are not the same value: Finance sends minor units already
 * (`amountMinor`) while Investment sends a decimal (`17259.20`), and the two services
 * are free to drift. Sharing the class would make either service's next change a
 * problem for both screens; a sibling context is not a library.
 *
 * Minor units on this side too, so nothing here ever rounds and the amounts a screen
 * puts side by side — a balance from Finance, a target from Investment — are the same
 * kind of integer. The conversion happens once, at the boundary, in
 * `DecimalAsMinorUnits`.
 */
data class Money(
    val amountMinor: Long,
    val currency: Currency,
) {
    operator fun minus(other: Money): Money {
        require(currency == other.currency) { "Cannot subtract $other from $this" }
        return copy(amountMinor = amountMinor - other.amountMinor)
    }
}

/** The currencies Investment prices in. The same two Finance holds. */
enum class Currency(val isoCode: String, val symbol: String) {
    PEN("PEN", "S/"),
    USD("USD", "$"),
    ;

    companion object {
        fun fromIsoCode(code: String): Currency? = entries.firstOrNull { it.isoCode == code }
    }
}
