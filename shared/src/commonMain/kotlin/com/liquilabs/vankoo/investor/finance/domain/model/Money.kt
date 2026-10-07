package com.liquilabs.vankoo.investor.finance.domain.model

/**
 * An amount in minor units of one currency, which is how Finance speaks money.
 *
 * Minor units — céntimos — rather than a decimal, so no arithmetic here ever rounds.
 * The wire carries them the same way (`amountMinor`), so nothing is converted at the
 * boundary either.
 */
data class Money(
    val amountMinor: Long,
    val currency: Currency,
)

/**
 * The currencies Finance accepts. Two, and never MXN whatever an old mockup shows.
 *
 * The symbol is what the product prints, not what the locale would: soles are `S/`
 * on every surface.
 */
enum class Currency(val isoCode: String, val symbol: String) {
    PEN("PEN", "S/"),
    USD("USD", "$"),
    ;

    companion object {
        fun fromIsoCode(code: String): Currency? = entries.firstOrNull { it.isoCode == code }
    }
}
