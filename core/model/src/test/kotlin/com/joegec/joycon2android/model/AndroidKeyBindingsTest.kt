package com.joegec.joycon2android.model

import org.junit.Assert.assertEquals
import org.junit.Test

class AndroidKeyBindingsTest {

    private val homeAndBack = AndroidKeyBindings()
        .bind(AndroidKey.HOME, JoyconButton.Home)
        .bind(AndroidKey.BACK, JoyconButton.Chat)

    @Test
    fun `a bound button presses its key and leaves the gamepad`() {
        val pressed = setOf(JoyconButton.Chat.id, JoyconButton.A.id)

        assertEquals(setOf(AndroidKey.BACK), homeAndBack.pressedKeys(pressed))
        assertEquals(setOf(JoyconButton.A.id), homeAndBack.gamepadButtons(pressed))
    }

    @Test
    fun `binding a button to a second key moves it`() {
        val moved = homeAndBack.bind(AndroidKey.SCREENSHOT, JoyconButton.Chat)

        assertEquals(mapOf(AndroidKey.HOME to JoyconButton.Home, AndroidKey.SCREENSHOT to JoyconButton.Chat), moved.buttons)
    }

    @Test
    fun `binding a key to nothing frees its button for the gamepad`() {
        val cleared = homeAndBack.bind(AndroidKey.BACK, null)

        assertEquals(setOf(JoyconButton.Chat.id), cleared.gamepadButtons(setOf(JoyconButton.Chat.id)))
    }

    @Test
    fun `nothing is bound by default`() {
        val pressed = setOf(JoyconButton.Home.id)

        assertEquals(emptySet<AndroidKey>(), AndroidKeyBindings().pressedKeys(pressed))
        assertEquals(pressed, AndroidKeyBindings().gamepadButtons(pressed))
    }
}
