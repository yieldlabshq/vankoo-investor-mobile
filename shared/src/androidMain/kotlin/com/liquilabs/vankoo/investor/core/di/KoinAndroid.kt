package com.liquilabs.vankoo.investor.core.di

import android.content.Context
import com.liquilabs.vankoo.investor.core.config.AppConfig
import com.liquilabs.vankoo.investor.core.platform.AndroidUrlOpener
import com.liquilabs.vankoo.investor.core.platform.CurrentActivity
import com.liquilabs.vankoo.investor.core.platform.UrlOpener
import com.liquilabs.vankoo.investor.core.storage.DATA_STORE_FILE_NAME
import com.liquilabs.vankoo.investor.core.storage.createVankooDataStore
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single { AppConfig(apiBaseUrl = API_BASE_URL) }

    // One instance, shared: VankooAppHost writes the activity into it while its
    // content is composed and the opener reads it when a Custom Tab is launched. A
    // factory would hand each of them its own and the opener would always read null.
    single { CurrentActivity() }
    single<UrlOpener> { AndroidUrlOpener(androidApplication(), get()) }

    single {
        val context: Context = androidApplication()
        createVankooDataStore { context.filesDir.resolve(DATA_STORE_FILE_NAME).absolutePath }
    }
}

/**
 * How the emulator reaches the gateway running on this machine.
 *
 * 10.0.2.2 is the address the emulator maps the host's loopback to; localhost inside
 * the emulator is the emulated device itself. On a physical phone this has to become
 * the machine's address on the network, which is the moment this constant turns into
 * a build field.
 */
private const val API_BASE_URL = "http://10.0.2.2:8080"

/**
 * Entry point for the Android app module.
 *
 * The signature mentions no Koin type on purpose: androidApp starts the
 * container without depending on Koin itself.
 */
fun startKoinAndroid(context: Context) {
    initKoin { androidContext(context) }
}
