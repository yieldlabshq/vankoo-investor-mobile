package com.liquilabs.vankoo.investor

import android.content.Intent
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.liquilabs.vankoo.investor.core.platform.CurrentActivity
import com.liquilabs.vankoo.investor.finance.presentation.topup.ProviderReturn
import com.liquilabs.vankoo.investor.finance.presentation.topup.ProviderReturnSignal
import org.koin.compose.koinInject

/**
 * El asa de Android sobre la navegación de la app.
 *
 * Misma idea que startKoinAndroid: androidApp arranca y alimenta la app sin importar
 * Navigation ni conocer un NavHostController — lo único que ve es esta clase.
 *
 * Existe por los enlaces profundos. El primero, el que abre la app, lo resuelve el
 * propio NavHost al componerse; los que llegan con la app ya abierta entran por
 * onNewIntent, donde no hay ninguna composición mirando.
 */
class VankooAppHost {

    private var navController: NavHostController? = null
    private var providerReturns: ProviderReturnSignal? = null

    @Composable
    fun Content() {
        val controller = rememberNavController()
        navController = controller

        // La hoja de Stripe se lanza desde la actividad que está en pantalla, para que
        // caiga en nuestra misma tarea. Ver CurrentActivity.
        val activity = LocalActivity.current
        val currentActivity = koinInject<CurrentActivity>()
        DisposableEffect(activity) {
            currentActivity.value = activity
            onDispose { if (currentActivity.value === activity) currentActivity.value = null }
        }

        providerReturns = koinInject()

        App(navController = controller)
    }

    /** Entrega un enlace que llegó con la app ya abierta. */
    fun onNewIntent(intent: Intent) {
        intent.data?.toProviderReturn()?.let { providerReturns?.post(it) }
        navController?.handleDeepLink(intent)
    }
}

/**
 * `vankoo://deposits/success` y `vankoo://deposits/cancel`, las dos puertas por las
 * que Stripe devuelve a la persona. Cualquier otro enlace no es cosa de Finance.
 */
private fun android.net.Uri.toProviderReturn(): ProviderReturn? {
    if (scheme != DEEP_LINK_SCHEME || host != DEPOSITS_HOST) return null
    return when (lastPathSegment) {
        "success" -> ProviderReturn.SUCCESS
        "cancel" -> ProviderReturn.CANCEL
        else -> null
    }
}

private const val DEEP_LINK_SCHEME = "vankoo"
private const val DEPOSITS_HOST = "deposits"
