package com.joegec.joycon2android.model

/** The pad has no key left for Capture; a lone Joy-Con has no GL, so it lends Capture GL's: docs/virtual-gamepad.md#buttons-and-keycodes */
object LoneCaptureKey {

    fun onPad(pressed: Set<String>, lone: Boolean): Set<String> =
        if (lone && JoyconButton.Capture.id in pressed) pressed - JoyconButton.Capture.id + JoyconButton.GL.id else pressed

    fun onPad(button: JoyconButton, lone: Boolean): JoyconButton =
        if (lone && button == JoyconButton.Capture) JoyconButton.GL else button
}
