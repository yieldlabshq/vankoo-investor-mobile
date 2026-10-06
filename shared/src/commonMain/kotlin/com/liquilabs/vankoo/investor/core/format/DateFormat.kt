package com.liquilabs.vankoo.investor.core.format

import androidx.compose.runtime.Composable
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource
import vankoo.shared.generated.resources.Res
import vankoo.shared.generated.resources.date_long_date_format
import vankoo.shared.generated.resources.date_long_format
import vankoo.shared.generated.resources.month_long
import vankoo.shared.generated.resources.month_short
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * `6 sep` — the day and a three-letter month, in the phone's zone and the app's
 * language.
 *
 * Composable because the month names are string resources, so a screen already
 * showing dates follows a change of language like every other label on it. The
 * ordinal form the mockups use for a single, prominent date («8 de septiembre de
 * 2026, 10:14») is [formatLongDateTime].
 */
@OptIn(ExperimentalTime::class)
@Composable
fun formatShortDate(instant: Instant): String {
    val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val months = stringArrayResource(Res.array.month_short)
    return "${local.day} ${months[local.month.number - 1]}"
}

@OptIn(ExperimentalTime::class)
@Composable
fun formatLongDateTime(instant: Instant): String {
    val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val months = stringArrayResource(Res.array.month_long)
    val minute = local.minute.toString().padStart(2, '0')
    // The order of day, month and year is the language's business, so it lives in
    // the resource as positional arguments and not here.
    return stringResource(
        Res.string.date_long_format,
        local.day.toString(),
        months[local.month.number - 1],
        local.year.toString(),
        "${local.hour}:$minute",
    )
}

/**
 * `30 de septiembre de 2026` — a calendar day, with no time attached to it.
 *
 * Separate from [formatLongDateTime] because the value it renders is a different
 * kind: an invoice's due date is a day everywhere in the world at once, not an
 * instant, and Investment sends it as one (`dueDate`, an ISO `2026-09-30`). Putting
 * it through a zone to print it would be able to move it by a day.
 */
@Composable
fun formatLongDate(date: LocalDate): String {
    val months = stringArrayResource(Res.array.month_long)
    return stringResource(
        Res.string.date_long_date_format,
        date.day.toString(),
        months[date.month.number - 1],
        date.year.toString(),
    )
}

/**
 * Whole days from now until [instant], never negative.
 *
 * Rounded down and measured from the current moment rather than from midnight: the
 * deadlines this counts are instants themselves — when an auction stops accepting
 * money — so "2 days" means two full days of it being open, not two sunrises. A
 * deadline already past is 0, which the copy reads as closing today.
 */
@OptIn(ExperimentalTime::class)
fun daysUntil(instant: Instant, now: Instant = Clock.System.now()): Int {
    val seconds = (instant - now).inWholeSeconds
    return if (seconds <= 0) 0 else (seconds / SECONDS_PER_DAY).toInt()
}

private const val SECONDS_PER_DAY = 86_400L
