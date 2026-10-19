package com.joegec.joycon2android.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GameCubePadKeysTest {

    @Test
    fun `its trigger clicks take Capture's and GL's keys`() {
        assertEquals(JoyconButton.Capture, GameCubePadKeys.onPad(JoyconButton.GL))
        assertEquals(JoyconButton.GL, GameCubePadKeys.onPad(JoyconButton.GR))
        assertEquals(
            setOf(JoyconButton.Capture.id, JoyconButton.GL.id, JoyconButton.A.id),
            GameCubePadKeys.onPad(setOf(JoyconButton.GL.id, JoyconButton.GR.id, JoyconButton.A.id)),
        )
    }

    @Test
    fun `its own Capture has no key on the pad`() {
        assertNull(GameCubePadKeys.onPad(JoyconButton.Capture))
        assertEquals(emptySet<String>(), GameCubePadKeys.onPad(setOf(JoyconButton.Capture.id)))
    }
}
