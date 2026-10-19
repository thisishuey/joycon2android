package com.joegec.joycon2android.model

import org.junit.Assert.assertEquals
import org.junit.Test

class LoneCaptureKeyTest {

    @Test
    fun `a lone Joy-Con's Capture takes GL's key on the pad`() {
        assertEquals(setOf(JoyconButton.GL.id, JoyconButton.A.id), LoneCaptureKey.onPad(setOf(JoyconButton.Capture.id, JoyconButton.A.id), lone = true))
        assertEquals(JoyconButton.GL, LoneCaptureKey.onPad(JoyconButton.Capture, lone = true))
    }

    @Test
    fun `a body with its own GL keeps Capture off GL's key`() {
        assertEquals(setOf(JoyconButton.Capture.id), LoneCaptureKey.onPad(setOf(JoyconButton.Capture.id), lone = false))
        assertEquals(JoyconButton.Capture, LoneCaptureKey.onPad(JoyconButton.Capture, lone = false))
    }
}
