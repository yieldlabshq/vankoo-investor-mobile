package com.liquilabs.vankoo.investor.core.result

/**
 * Outcome of an operation that can fail for a reason worth showing.
 *
 * Kotlin's own Result carries a Throwable, which would force "wrong password"
 * to travel as an exception. It is not a programming error — it is an answer.
 */
sealed interface AppResult<out T> {
    data class Success<out T>(val data: T) : AppResult<T>
    data class Failure(val error: AppError) : AppResult<Nothing>
}

inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(data))
    is AppResult.Failure -> this
}

inline fun <T> AppResult<T>.onSuccess(action: (T) -> Unit): AppResult<T> {
    if (this is AppResult.Success) action(data)
    return this
}
