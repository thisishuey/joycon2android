package com.joegec.joycon2android.buttonmapping

import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyGameCubeClicksTest {

    private val stored = mapOf("TriggerL" to "L", "A" to "R|Capture", "MainStick_UP" to "LEFT_STICK_UP")

    @Test
    fun `a gamecube controller's old trigger bindings move to its clicks on the pad's consoles`() {
        val upgraded = stored.withLegacyGameCubeClicks(Console.GAMECUBE, JoyconSide.GAMECUBE)

        assertEquals(mapOf("TriggerL" to "GL", "A" to "GR|Capture", "MainStick_UP" to "LEFT_STICK_UP"), upgraded)
        assertEquals("GL", stored.withLegacyGameCubeClicks(Console.SWITCH_PRO, JoyconSide.GAMECUBE).getValue("TriggerL"))
    }

    @Test
    fun `the wii console and other bodies keep their bindings`() {
        assertEquals(stored, stored.withLegacyGameCubeClicks(Console.WIIMOTE_NUNCHUK, JoyconSide.GAMECUBE))
        assertEquals(stored, stored.withLegacyGameCubeClicks(Console.GAMECUBE, JoyconSide.DUAL))
    }
}
