package com.joegec.joycon2android.capture

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.captureDataStore: DataStore<Preferences> by preferencesDataStore(name = "capture_preferences")

class CaptureRevealDataStore(context: Context) : CaptureRevealPreferences {

    private val dataStore = context.applicationContext.captureDataStore

    override val revealed: Flow<Boolean> = dataStore.data.map { it[KEY] ?: false }

    override suspend fun reveal() {
        dataStore.edit { it[KEY] = true }
    }

    private companion object {
        val KEY = booleanPreferencesKey("capture_revealed")
    }
}
