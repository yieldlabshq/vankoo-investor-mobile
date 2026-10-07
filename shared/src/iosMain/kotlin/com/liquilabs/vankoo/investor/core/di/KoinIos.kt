package com.liquilabs.vankoo.investor.core.di

import com.liquilabs.vankoo.investor.core.config.AppConfig
import com.liquilabs.vankoo.investor.core.platform.IosUrlOpener
import com.liquilabs.vankoo.investor.core.platform.UrlOpener
import com.liquilabs.vankoo.investor.core.storage.DATA_STORE_FILE_NAME
import com.liquilabs.vankoo.investor.core.storage.createVankooDataStore
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

actual val platformModule: Module = module {
    single { AppConfig(apiBaseUrl = API_BASE_URL) }
    single<UrlOpener> { IosUrlOpener() }

    single { createVankooDataStore(::dataStorePath) }
}

/**
 * The app's own documents directory, which is where a file that should survive
 * reinstallation of nothing and backup of everything belongs.
 */
@OptIn(ExperimentalForeignApi::class)
private fun dataStorePath(): String {
    val documents: NSURL? = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    return requireNotNull(documents?.path) { "The documents directory is missing" } +
        "/" + DATA_STORE_FILE_NAME
}

/**
 * The simulator shares the host's loopback, so localhost is the gateway.
 *
 * Untested: App Transport Security blocks cleartext HTTP and opening it is a change
 * to the Xcode project, not to this file.
 */
private const val API_BASE_URL = "http://localhost:8080"

/**
 * Entry point for the iOS app, called once from `iOSApp.swift`.
 *
 * Not named `initKoinIos`: Objective-C interop renames anything starting with
 * `init` to avoid clashing with initialisers, so Swift would have to call it
 * `doInitKoinIos()`.
 */
fun startKoinIos() {
    initKoin()
}
