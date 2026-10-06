package com.liquilabs.vankoo.investor.core.platform

/**
 * Hands a URL to the system browser.
 *
 * The only thing the app ever sends outside itself is Stripe's checkout page, and it
 * must open in the real browser and not a WebView: the card form belongs to Stripe,
 * and a WebView would put our app between the person and the page they are trusting
 * with their card. Each platform binds its own in the platform module.
 */
fun interface UrlOpener {
    /** True if something took the URL; false if nothing on the device could open it. */
    fun open(url: String): Boolean
}
