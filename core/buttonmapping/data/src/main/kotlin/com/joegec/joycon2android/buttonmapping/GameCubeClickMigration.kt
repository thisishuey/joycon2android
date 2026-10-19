package com.joegec.joycon2android.buttonmapping

import androidx.datastore.core.DataMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

// Outside every body's key prefix, so no mapping ever reads it.
private val MIGRATED = booleanPreferencesKey("gamecube_clicks_migrated")

/** Keeps a GameCube controller's stored bindings on the trigger clicks they were made for. */
internal class GameCubeClickMigration : DataMigration<Preferences> {

    override suspend fun shouldMigrate(currentData: Preferences) = currentData[MIGRATED] != true

    override suspend fun migrate(currentData: Preferences): Preferences {
        val migrated = currentData.toMutablePreferences()
        currentData.asMap().forEach { (key, value) ->
            val (consoleName, _, sideName, target) = segmentsOf(key.name).takeIf { it.size == 4 } ?: return@forEach
            val console = consoleNamed(consoleName) ?: return@forEach
            val side = sideNamed(sideName) ?: return@forEach
            if (value !is String) return@forEach
            val upgraded = mapOf(target to value).withLegacyGameCubeClicks(console, side).getValue(target)
            if (upgraded != value) migrated[stringPreferencesKey(key.name)] = upgraded
        }
        migrated[MIGRATED] = true
        return migrated
    }

    override suspend fun cleanUp() = Unit
}
