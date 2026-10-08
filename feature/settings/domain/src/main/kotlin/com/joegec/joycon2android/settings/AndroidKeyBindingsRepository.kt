package com.joegec.joycon2android.settings

import com.joegec.joycon2android.model.AndroidKey
import com.joegec.joycon2android.model.AndroidKeyBindings
import com.joegec.joycon2android.model.JoyconButton
import kotlinx.coroutines.flow.Flow

interface AndroidKeyBindingsRepository {
    val bindings: Flow<AndroidKeyBindings>
    suspend fun bind(key: AndroidKey, button: JoyconButton?)
}
