package com.joegec.joycon2android.model

/** Where a GameCube controller's shoulders and C go, and why: docs/virtual-gamepad.md#analog-triggers */
object GameCubeOutputMapper {

    fun remapButtons(pressed: Set<String>): Set<String> =
        pressed.mapTo(mutableSetOf()) { REMAP[it] ?: it }

    /** Null for a stick click: no button of this controller lands on those bits. */
    fun emittedId(id: String): String? = REMAP[id] ?: id.takeUnless { it in STICK_CLICKS }

    // ZL/Z are its bumpers and a pulled trigger its trigger button; GL/GR, its clicks, keep their own keys.
    // It has no −, so C is its Select.
    private val REMAP = mapOf(
        JoyconButton.ZL.id to JoyconButton.L.id,
        JoyconButton.ZR.id to JoyconButton.R.id,
        JoyconButton.L.id to JoyconButton.ZL.id,
        JoyconButton.R.id to JoyconButton.ZR.id,
        JoyconButton.Chat.id to JoyconButton.Minus.id,
    )

    private val STICK_CLICKS = setOf(JoyconButton.LS.id, JoyconButton.RS.id)
}
