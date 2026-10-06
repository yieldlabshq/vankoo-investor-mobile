package com.liquilabs.vankoo.investor.iam.application

import com.liquilabs.vankoo.investor.core.result.AppResult
import com.liquilabs.vankoo.investor.iam.domain.model.Email
import com.liquilabs.vankoo.investor.iam.domain.model.User
import com.liquilabs.vankoo.investor.iam.domain.repository.AuthRepository

/**
 * Registers an investor.
 *
 * Unlike [SignIn] it leaves no session behind, because there is none to leave: the
 * service returns the new user without a token. The screen sends the person to sign
 * in with the address already filled, which is what the web does for the same reason.
 */
class SignUp(private val repository: AuthRepository) {
    suspend operator fun invoke(email: Email, password: String): AppResult<User> =
        repository.signUp(email, password)
}
