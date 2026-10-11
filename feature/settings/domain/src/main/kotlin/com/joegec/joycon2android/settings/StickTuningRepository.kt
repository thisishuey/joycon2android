package com.joegec.joycon2android.settings

import com.joegec.joycon2android.model.GamepadStick
import com.joegec.joycon2android.model.StickResponse
import com.joegec.joycon2android.model.StickTuning
import kotlinx.coroutines.flow.Flow

interface StickTuningRepository {
    val tuning: Flow<StickTuning>
    suspend fun set(stick: GamepadStick, response: StickResponse)
}
