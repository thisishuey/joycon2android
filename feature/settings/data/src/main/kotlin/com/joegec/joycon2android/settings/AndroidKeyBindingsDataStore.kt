package com.joegec.joycon2android.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.joegec.joycon2android.model.AndroidKey
import com.joegec.joycon2android.model.AndroidKeyBindings
import com.joegec.joycon2android.model.JoyconButton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.androidKeysDataStore: DataStore<Preferences> by preferencesDataStore(name = "android_keys")

class AndroidKeyBindingsDataStore(context: Context) : AndroidKeyBindingsRepository {

    private val dataStore = context.applicationContext.androidKeysDataStore

    override val bindings: Flow<AndroidKeyBindings> = dataStore.data.map(::read)

    override suspend fun bind(key: AndroidKey, button: JoyconButton?) {
        dataStore.edit { prefs ->
            val updated = read(prefs).bind(key, button)
            AndroidKey.entries.forEach { prefs.remove(preferenceFor(it)) }
            updated.buttons.forEach { (boundKey, boundButton) -> prefs[preferenceFor(boundKey)] = boundButton.id }
        }
    }

    private fun read(prefs: Preferences) = AndroidKeyBindings(
        AndroidKey.entries.mapNotNull { key ->
            val id = prefs[preferenceFor(key)]
            JoyconButton.entries.firstOrNull { it.id == id }?.let { key to it }
        }.toMap(),
    )

    private fun preferenceFor(key: AndroidKey) = stringPreferencesKey(key.name.lowercase())
}
