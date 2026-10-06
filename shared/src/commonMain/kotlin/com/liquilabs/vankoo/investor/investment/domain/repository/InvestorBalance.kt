package com.liquilabs.vankoo.investor.investment.domain.repository

import com.liquilabs.vankoo.investor.core.result.AppResult
import com.liquilabs.vankoo.investor.investment.domain.model.Currency

/**
 * How much the investor has to invest with.
 *
 * This is the one thing this context cannot answer on its own, and the seam is named
 * rather than left implicit: the balance lives in Finance, and the confirm screen
 * needs it to say «Disponible» and to tell an amount apart from an amount that cannot
 * be paid. Declaring the port here and adapting Finance behind it keeps every other
 * file in the context free of finance's vocabulary — its own `Money`, its own
 * failures — which is the same reason this context spells `Money` for itself.
 *
 * Returns null when the wallet has not been opened yet, which Finance answers with a
 * 404 and means «no ha recargado nunca», not «falló».
 */
fun interface InvestorBalance {
    suspend fun of(accountId: String, currency: Currency): AppResult<Long?>
}
