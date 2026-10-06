package com.liquilabs.vankoo.investor.iam

import com.liquilabs.vankoo.investor.core.network.AuthTokenSource
import com.liquilabs.vankoo.investor.iam.application.ObserveSession
import com.liquilabs.vankoo.investor.iam.application.RequestPasswordReset
import com.liquilabs.vankoo.investor.iam.application.ResetPassword
import com.liquilabs.vankoo.investor.iam.application.SignIn
import com.liquilabs.vankoo.investor.iam.application.SignOut
import com.liquilabs.vankoo.investor.iam.application.SignUp
import com.liquilabs.vankoo.investor.iam.domain.repository.AuthRepository
import com.liquilabs.vankoo.investor.iam.domain.repository.SessionStore
import com.liquilabs.vankoo.investor.iam.infrastructure.HttpAuthRepository
import com.liquilabs.vankoo.investor.iam.infrastructure.IamApi
import com.liquilabs.vankoo.investor.iam.infrastructure.DataStoreSessionStore
import com.liquilabs.vankoo.investor.iam.presentation.recover.RecoverPasswordViewModel
import com.liquilabs.vankoo.investor.iam.presentation.reset.ResetPasswordViewModel
import com.liquilabs.vankoo.investor.iam.presentation.signin.SignInViewModel
import com.liquilabs.vankoo.investor.iam.presentation.signup.SignUpViewModel
import kotlinx.coroutines.flow.first
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Everything this context needs to run.
 *
 * It is also where the HTTP client learns about the session: core declares
 * [AuthTokenSource] and iam is the one that can answer it. The dependency points
 * this way round on purpose — the client must not know that a bounded context called
 * iam exists.
 */
val iamModule = module {
    single { IamApi(get()) }
    single<AuthRepository> { HttpAuthRepository(get()) }
    single<SessionStore> { DataStoreSessionStore(get()) }

    single<AuthTokenSource> {
        val sessions = get<SessionStore>()
        AuthTokenSource {
            // An expired token is not worth a round trip: the answer is already known.
            sessions.current.first()?.takeIf { !it.isExpired() }?.token
        }
    }

    factory { SignIn(get(), get()) }
    factory { SignUp(get()) }
    factory { RequestPasswordReset(get()) }
    factory { ResetPassword(get()) }
    factory { SignOut(get()) }
    factory { ObserveSession(get()) }

    viewModelOf(::SignInViewModel)
    viewModelOf(::SignUpViewModel)
    viewModelOf(::RecoverPasswordViewModel)
    viewModelOf(::ResetPasswordViewModel)
}
