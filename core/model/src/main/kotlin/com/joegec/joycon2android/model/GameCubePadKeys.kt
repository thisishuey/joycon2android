package com.joegec.joycon2android.model

/**
 * On the virtual pad a GameCube controller's R click (GR) takes Capture's key, `BUTTON_C`, and its own
 * Capture has none: docs/virtual-gamepad.md#analog-triggers
 */
object GameCubePadKeys {

    fun onPad(pressed: Set<String>): Set<String> = pressed.mapNotNullTo(mutableSetOf(), ::onPadId)

    /** Null for its Capture, which has no key on the pad. */
    fun onPad(button: JoyconButton): JoyconButton? = if (button in KEYS) KEYS[button] else button

    private fun onPadId(id: String): String? {
        val button = JoyconButton.entries.firstOrNull { it.id == id } ?: return id
        return onPad(button)?.id
    }

    // Each button to the one whose key it takes.
    private val KEYS: Map<JoyconButton, JoyconButton?> = mapOf(
        JoyconButton.Capture to null,
        JoyconButton.GR to JoyconButton.Capture,
    )
}
