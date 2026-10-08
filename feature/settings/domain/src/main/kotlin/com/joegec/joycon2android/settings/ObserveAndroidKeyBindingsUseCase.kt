package com.joegec.joycon2android.settings

import com.joegec.joycon2android.model.AndroidKeyBindings
import kotlinx.coroutines.flow.Flow

class ObserveAndroidKeyBindingsUseCase(private val repository: AndroidKeyBindingsRepository) {
    operator fun invoke(): Flow<AndroidKeyBindings> = repository.bindings
}
