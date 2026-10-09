package com.joegec.joycon2android.model

data class AndroidKeyBindings(val buttons: Map<AndroidKey, JoyconButton> = emptyMap()) {

    /** A button stands in for one key at most, so binding it elsewhere moves it. */
    fun bind(key: AndroidKey, button: JoyconButton?): AndroidKeyBindings {
        val others = buttons.filterValues { it != button } - key
        return AndroidKeyBindings(if (button == null) others else others + (key to button))
    }

    fun pressedKeys(pressed: Set<String>): Set<AndroidKey> = buttons.filterValues { it.id in pressed }.keys

    fun gamepadButtons(pressed: Set<String>): Set<String> = pressed - buttons.values.mapTo(mutableSetOf()) { it.id }

    companion object {
        // SidewaysMapper moves none of these, so each reaches the report under its own id.
        val CHOICES = listOf(
            JoyconButton.Home, JoyconButton.Capture, JoyconButton.Chat,
            JoyconButton.GL, JoyconButton.GR, JoyconButton.Minus, JoyconButton.Plus,
        )
    }
}
