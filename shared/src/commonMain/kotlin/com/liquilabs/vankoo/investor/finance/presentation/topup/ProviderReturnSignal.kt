package com.liquilabs.vankoo.investor.finance.presentation.topup

/** Which door Stripe sent the person back through. */
enum class ProviderReturn {
    /** `vankoo://deposits/success`: the payment went through on Stripe's side. */
    SUCCESS,

    /** `vankoo://deposits/cancel`: they used Stripe's back arrow without paying. */
    CANCEL,
}

/**
 * The note the platform leaves for the waiting screen when Stripe's page hands back.
 *
 * A plain holder rather than a flow, on purpose. On Android the return link arrives
 * through onNewIntent, which runs *before* the activity restarts and the screen
 * resumes its polling, so by the time the screen asks, the note is already here — no
 * race, no buffering, no subscription to keep alive across a configuration change.
 * Reading it clears it: a link is an event, not a state, and a screen recreated later
 * must not act on a return that already happened.
 *
 * Closing the sheet with its own X leaves no note. Stripe only redirects when the
 * person chooses one of its two exits, so "no note but we were at the provider" is
 * itself an answer: they came back without paying.
 */
class ProviderReturnSignal {

    private var pending: ProviderReturn? = null

    fun post(outcome: ProviderReturn) {
        pending = outcome
    }

    /** The last return, if any, and forgets it. */
    fun consume(): ProviderReturn? = pending.also { pending = null }
}
