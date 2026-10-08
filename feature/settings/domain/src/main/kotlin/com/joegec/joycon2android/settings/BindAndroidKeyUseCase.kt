package com.joegec.joycon2android.settings

import com.joegec.joycon2android.model.AndroidKey
import com.joegec.joycon2android.model.JoyconButton

class BindAndroidKeyUseCase(private val repository: AndroidKeyBindingsRepository) {
    suspend operator fun invoke(key: AndroidKey, button: JoyconButton?) = repository.bind(key, button)
}
