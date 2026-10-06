package com.liquilabs.vankoo.investor.core.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

/**
 * Builds the app's single preferences store.
 *
 * The path is the platform's business — Android has a files directory, iOS has a
 * document directory — so it arrives as a lambda instead of an expect/actual of its
 * own. The catalog only carries `datastore-preferences-core`, which is the
 * multiplatform half and deliberately does not know where anything lives.
 *
 * Nothing here is encrypted. That is fine for what it holds today and stops being
 * fine the moment more than a bearer token goes in: the Keystore and the Keychain
 * are the next step, and they are the first real expect/actual this app will need.
 */
fun createVankooDataStore(producePath: () -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(produceFile = { producePath().toPath() })

/** DataStore insists on this extension; it is not decoration. */
const val DATA_STORE_FILE_NAME = "vankoo.preferences_pb"
