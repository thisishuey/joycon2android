package com.joegec.joycon2android.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.joegec.joycon2android.model.GamepadStick
import com.joegec.joycon2android.model.StickResponse
import com.joegec.joycon2android.model.StickTuning
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.stickTuningDataStore: DataStore<Preferences> by preferencesDataStore(name = "stick_tuning")

class StickTuningDataStore(context: Context) : StickTuningRepository {

    private val dataStore = context.applicationContext.stickTuningDataStore
    private val defaults = StickResponse()

    override val tuning: Flow<StickTuning> = dataStore.data.map { prefs ->
        StickTuning(left = read(prefs, GamepadStick.LEFT), right = read(prefs, GamepadStick.RIGHT))
    }

    override suspend fun set(stick: GamepadStick, response: StickResponse) {
        dataStore.edit {
            it[precisionKey(stick)] = response.precision
            it[maxTiltKey(stick)] = response.maxTilt
        }
    }

    private fun read(prefs: Preferences, stick: GamepadStick) = StickResponse(
        precision = prefs[precisionKey(stick)] ?: defaults.precision,
        maxTilt = prefs[maxTiltKey(stick)] ?: defaults.maxTilt,
    )

    private fun precisionKey(stick: GamepadStick) = intPreferencesKey("${stick.name.lowercase()}_precision")

    private fun maxTiltKey(stick: GamepadStick) = intPreferencesKey("${stick.name.lowercase()}_max_tilt")
}
