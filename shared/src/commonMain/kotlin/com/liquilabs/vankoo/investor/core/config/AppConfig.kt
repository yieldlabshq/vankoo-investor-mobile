package com.liquilabs.vankoo.investor.core.config

/**
 * Values that differ between builds and platforms.
 *
 * The base URL is not the same string on both: an Android emulator reaches the
 * host machine at 10.0.2.2, while the iOS simulator shares the host's loopback.
 * This is the seam Android alone would solve with a `buildConfigField`; here it is
 * Koin, which already exists and works on both platforms.
 *
 * There used to be a second address here, `financeBaseUrl`, for the days when the
 * gateway had no route to Finance and the app went to the service itself (Trello
 * #52/#53). The route landed, so every service is now reached the same way — by its
 * prefix under [apiBaseUrl] — and the field went with it.
 */
data class AppConfig(
    /** The gateway. Every service is reached through it by its prefix — `/iam`, `/finance`, `/investment`. */
    val apiBaseUrl: String,
)
