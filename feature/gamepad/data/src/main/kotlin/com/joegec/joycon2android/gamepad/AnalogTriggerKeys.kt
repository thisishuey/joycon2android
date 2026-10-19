package com.joegec.joycon2android.gamepad

import com.joegec.joycon2android.model.GameCubeButtons
import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.PlayerState

/** The pad's L2/R2 follow the pull and its back-button keys the click: docs/virtual-gamepad.md#analog-triggers */
internal object AnalogTriggerKeys {

    fun pressedOnPad(state: PlayerState): Set<String> {
        val pulled = GameCubeButtons.triggersPulledBy(state.leftInput)
        return state.gamepad.pressed - TRIGGER_KEYS + GameCubeButtons.backButtonsClickedBy(state.pressed) + setOfNotNull(
            JoyconButton.ZL.id.takeIf { JoyconButton.L.id in pulled },
            JoyconButton.ZR.id.takeIf { JoyconButton.R.id in pulled },
        )
    }

    private val TRIGGER_KEYS = setOf(JoyconButton.ZL.id, JoyconButton.ZR.id)
}
