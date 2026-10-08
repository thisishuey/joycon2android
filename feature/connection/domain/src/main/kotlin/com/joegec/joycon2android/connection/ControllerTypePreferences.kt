package com.joegec.joycon2android.connection

import com.joegec.joycon2android.model.Side
import kotlinx.coroutines.flow.Flow

/** A type chosen by hand, keyed by BLE address: it outlives the connection. */
interface ControllerTypePreferences {
    val overrides: Flow<Map<String, Side>>
    suspend fun set(address: String, side: Side?)
}
