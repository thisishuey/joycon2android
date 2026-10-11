package com.joegec.joycon2android.settings

import com.joegec.joycon2android.model.GamepadStick
import com.joegec.joycon2android.model.StickResponse

class SetStickResponseUseCase(private val repository: StickTuningRepository) {
    suspend operator fun invoke(stick: GamepadStick, response: StickResponse) = repository.set(stick, response)
}
