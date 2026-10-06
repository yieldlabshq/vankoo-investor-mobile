package com.liquilabs.vankoo.investor.iam.domain.model

/**
 * The roles this client knows about.
 *
 * Sending [INVESTOR] on sign-up is not optional. `roles` may be left out and the
 * service will not complain — it falls back to `ROLE_USER` — but profile-service
 * builds an Investor or a Company from exactly this string and ignores anything
 * else, so the account would be created and then never get a profile. The failure
 * is silent and arrives days later; the web documents the same trap in
 * `sign-up.command.ts`.
 */
object Role {
    const val INVESTOR = "ROLE_INVESTOR"
}
