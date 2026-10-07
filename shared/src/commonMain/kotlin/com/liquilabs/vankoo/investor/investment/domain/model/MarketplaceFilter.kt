package com.liquilabs.vankoo.investor.investment.domain.model

/**
 * The three ways the market screen can be asked to sort and narrow itself.
 *
 * Deliberately one exclusive choice rather than a set, which is how the mockup draws
 * the row and how the chips read: they are not three independent switches, they are
 * three answers to "what am I looking at".
 *
 * [Green] narrows, the other two only order, and that is a seam worth naming: the
 * marketplace endpoint filters on `greenCertified`, `currency` and `status`, and
 * orders on four fields. Anything outside those two lists has no query behind it.
 *
 * The mockup has a fourth chip, «Riesgo A», which is not here. There is no risk-grade
 * predicate in `AuctionMarketplaceSpecifications`, and filtering the page the app
 * happens to hold would silently drop grade-A auctions that sit on page two. A chip
 * that lies about what it selected is worse than a chip that is missing, so it waits
 * for the query.
 */
enum class MarketplaceFilter {
    /**
     * Everything on offer, newest first — which is what a market with new arrivals
     * should open on, and what keeps this chip from being a duplicate of
     * [ClosingSoon]: the endpoint's own default sort is already `expiresAt,asc`.
     */
    All,

    /** Only invoices with the green certificate. */
    Green,

    /** Whatever stops accepting money first. */
    ClosingSoon,
    ;

    /** The `sort` the marketplace endpoint understands, as `field,direction`. */
    val sort: String
        get() = when (this) {
            All -> "publishedAt,desc"
            Green -> "publishedAt,desc"
            ClosingSoon -> "expiresAt,asc"
        }

    /** Null means "do not filter on it at all", which is not the same as false. */
    val greenCertified: Boolean?
        get() = if (this == Green) true else null
}
