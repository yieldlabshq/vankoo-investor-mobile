package com.liquilabs.vankoo.investor.investment.infrastructure.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonUnquotedLiteral
import kotlinx.serialization.json.jsonPrimitive

/**
 * Reads Investment's decimal amounts as minor units, exactly.
 *
 * Investment prices in `BigDecimal` and sends the result as a JSON number —
 * `17259.20`. Kotlin has no BigDecimal in common code, and decoding that as a Double
 * is how a total ends up a cent short of the rows above it: 17259.20 is not
 * representable in binary floating point, and neither is most of what this service
 * sends. So the literal is read as text, before any number type touches it, and
 * turned into the integer number of cents the rest of the app already speaks.
 *
 * Anything that is not a plain decimal — scientific notation, a third decimal place
 * that is not zero, letters — raises [SerializationException] rather than being
 * coerced. That surfaces as `AppError.Unexpected`, which is what a contract this app
 * has misread actually is; guessing would put a wrong amount of money on a screen.
 */
object DecimalAsMinorUnits : KSerializer<Long> {

    override val descriptor =
        PrimitiveSerialDescriptor("com.liquilabs.vankoo.DecimalAsMinorUnits", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Long {
        // decodeJsonElement keeps the literal as it arrived on the wire; decodeString
        // on a JSON number would be the lenient path, which is not guaranteed.
        val literal = if (decoder is JsonDecoder) {
            decoder.decodeJsonElement().jsonPrimitive.content
        } else {
            decoder.decodeString()
        }
        return parseMinorUnits(literal)
    }

    /**
     * Writes cents back out as a decimal number, which is what the service expects
     * where the app posts an amount.
     */
    override fun serialize(encoder: Encoder, value: Long) {
        val text = formatDecimal(value)
        if (encoder is JsonEncoder) {
            encoder.encodeJsonElement(JsonUnquotedLiteral(text))
        } else {
            encoder.encodeString(text)
        }
    }

    internal fun parseMinorUnits(literal: String): Long {
        val text = literal.trim()
        val negative = text.startsWith('-')
        val digits = text.removePrefix("-").removePrefix("+")
        if (digits.isEmpty() || !digits.all { it.isDigit() || it == '.' }) {
            throw SerializationException("Not a plain decimal amount: $literal")
        }

        val parts = digits.split('.')
        if (parts.size > 2) throw SerializationException("Not a plain decimal amount: $literal")

        val whole = parts[0].ifEmpty { "0" }
        val fraction = parts.getOrElse(1) { "" }
        // A scale beyond cents is only acceptable when it carries nothing: the
        // service sets scale 2 on money, but a projection that widened it to
        // 17259.2000 is still the same amount.
        if (fraction.length > CENTS_DIGITS && fraction.drop(CENTS_DIGITS).any { it != '0' }) {
            throw SerializationException("Amount has more precision than cents: $literal")
        }

        val units = whole.toLongOrNull() ?: throw SerializationException("Amount is too large: $literal")
        val cents = fraction.take(CENTS_DIGITS).padEnd(CENTS_DIGITS, '0').toLong()
        val magnitude = units * 100 + cents
        return if (negative) -magnitude else magnitude
    }

    internal fun formatDecimal(amountMinor: Long): String {
        val negative = amountMinor < 0
        val magnitude = if (negative) -amountMinor else amountMinor
        val body = "${magnitude / 100}.${(magnitude % 100).toString().padStart(CENTS_DIGITS, '0')}"
        return if (negative) "-$body" else body
    }

    private const val CENTS_DIGITS = 2
}
