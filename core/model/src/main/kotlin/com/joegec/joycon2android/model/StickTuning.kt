package com.joegec.joycon2android.model

data class StickTuning(
    val left: StickResponse = StickResponse(),
    val right: StickResponse = StickResponse(),
) {
    fun of(stick: GamepadStick): StickResponse = when (stick) {
        GamepadStick.LEFT -> left
        GamepadStick.RIGHT -> right
    }

    fun with(stick: GamepadStick, response: StickResponse): StickTuning = when (stick) {
        GamepadStick.LEFT -> copy(left = response)
        GamepadStick.RIGHT -> copy(right = response)
    }
}
