package com.liquilabs.vankoo.investor.finance.infrastructure

import com.liquilabs.vankoo.investor.core.network.safeApiCall
import com.liquilabs.vankoo.investor.core.result.AppError
import com.liquilabs.vankoo.investor.core.result.AppResult
import com.liquilabs.vankoo.investor.finance.domain.model.FinanceFailure
import com.liquilabs.vankoo.investor.finance.infrastructure.dto.CreateDepositRequestDto
import com.liquilabs.vankoo.investor.finance.infrastructure.dto.CreateWalletDebitRequestDto
import com.liquilabs.vankoo.investor.finance.infrastructure.dto.DepositResponseDto
import com.liquilabs.vankoo.investor.finance.infrastructure.dto.WalletDebitResponseDto
import com.liquilabs.vankoo.investor.finance.infrastructure.dto.WalletMovementPageDto
import com.liquilabs.vankoo.investor.finance.infrastructure.dto.WalletResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode

/**
 * The Finance service, as this app reaches it.
 *
 * The paths are relative, like [com.liquilabs.vankoo.investor.iam.infrastructure.IamApi]'s
 * and [com.liquilabs.vankoo.investor.investment.infrastructure.InvestmentApi]'s: the
 * gateway routes everything under `/finance/api/v1/accounts` and
 * `/finance/api/v1/deposits` (Trello #53), validates the token and hands Finance the
 * caller as `X-User-Id`.
 * Finance compares that with the `{accountId}` in the path and answers `403 forbidden`
 * to anyone else's wallet, which is also why going to the service directly stopped
 * being an option: without the gateway there is no caller to compare.
 */
class FinanceApi(private val client: HttpClient) {

    /**
     * The balance, or null on 404.
     *
     * Finance opens a wallet with the first deposit and answers 404 until then. That
     * is an answer — "nothing here yet" — and not a failure, so it is folded into the
     * success side before it can reach a screen as a server error. Finance now names
     * it `wallet-not-found`, and the bare 404 is kept as a second spelling in case an
     * older build of the service is what answers.
     */
    suspend fun getWallet(accountId: String, currency: String): AppResult<WalletResponseDto?> {
        val result = safeApiCall<WalletResponseDto>(FinanceFailure::fromCode) {
            client.get("$ACCOUNTS_PATH/$accountId/wallets/$currency")
        }
        return when {
            result is AppResult.Failure && result.error.isNoWalletYet() -> AppResult.Success(null)
            result is AppResult.Success -> AppResult.Success(result.data)
            else -> result as AppResult.Failure
        }
    }

    suspend fun getMovements(
        accountId: String,
        currency: String,
        page: Int,
        size: Int,
    ): AppResult<WalletMovementPageDto> =
        safeApiCall(FinanceFailure::fromCode) {
            client.get("$ACCOUNTS_PATH/$accountId/wallets/$currency/movements") {
                parameter("page", page)
                parameter("size", size)
            }
        }

    /**
     * Answers 201 with the debit already applied, or replays it if the key was seen.
     *
     * Synchronous, unlike a deposit: when this returns 2xx the balance has gone down.
     * A `409 insufficient-balance` releases the key on the server, so the same key can
     * be sent again after a top-up.
     */
    suspend fun debitWallet(
        accountId: String,
        currency: String,
        request: CreateWalletDebitRequestDto,
        idempotencyKey: String,
    ): AppResult<WalletDebitResponseDto> =
        safeApiCall(FinanceFailure::fromCode) {
            client.post("$ACCOUNTS_PATH/$accountId/wallets/$currency/debits") {
                header(IDEMPOTENCY_KEY_HEADER, idempotencyKey)
                setBody(request)
            }
        }

    /** Answers 202 with the deposit in PENDING, or replays it if the key was seen. */
    suspend fun createDeposit(
        request: CreateDepositRequestDto,
        idempotencyKey: String,
    ): AppResult<DepositResponseDto> =
        safeApiCall(FinanceFailure::fromCode) {
            client.post(DEPOSITS_PATH) {
                header(IDEMPOTENCY_KEY_HEADER, idempotencyKey)
                setBody(request)
            }
        }

    suspend fun getDeposit(depositId: String): AppResult<DepositResponseDto> =
        safeApiCall(FinanceFailure::fromCode) {
            client.get("$DEPOSITS_PATH/$depositId")
        }

    private fun AppError.isNoWalletYet(): Boolean =
        this == AppError.Server(HttpStatusCode.NotFound.value) ||
            this == AppError.Business(FinanceFailure.WalletNotFound)

    private companion object {
        const val ACCOUNTS_PATH = "/finance/api/v1/accounts"
        const val DEPOSITS_PATH = "/finance/api/v1/deposits"
        const val IDEMPOTENCY_KEY_HEADER = "Idempotency-Key"
    }
}
