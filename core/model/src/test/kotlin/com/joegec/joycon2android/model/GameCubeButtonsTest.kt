package com.joegec.joycon2android.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class GameCubeButtonsTest {

    @Test
    fun `a gamecube controller has no minus, stick clicks or back paddles`() {
        listOf(JoyconButton.Minus, JoyconButton.LS, JoyconButton.RS, JoyconButton.GL, JoyconButton.GR).forEach {
            assertFalse("$it", it in GameCubeButtons.present)
        }
    }

    @Test
    fun `its ZR is marked Z and every other button keeps its marking`() {
        assertEquals("Z", GameCubeButtons.marking(JoyconButton.ZR))
        assertNull(GameCubeButtons.marking(JoyconButton.ZL))
    }

    @Test
    fun `its trigger clicks press the back-button keys`() {
        assertEquals(
            setOf(JoyconButton.GL.id, JoyconButton.GR.id),
            GameCubeButtons.backButtonsClickedBy(setOf(JoyconButton.L.id, JoyconButton.R.id, JoyconButton.A.id)),
        )
        assertEquals(emptySet<String>(), GameCubeButtons.backButtonsClickedBy(setOf(JoyconButton.ZL.id)))
    }

    @Test
    fun `its triggers count as pressed from the first travel or the click`() {
        assertEquals(setOf(JoyconButton.L.id), GameCubeButtons.triggersPulledBy(JoyconInput(triggerLeft = 1)))
        assertEquals(setOf(JoyconButton.R.id), GameCubeButtons.triggersPulledBy(JoyconInput(pressed = setOf(JoyconButton.R.id))))
        assertEquals(emptySet<String>(), GameCubeButtons.triggersPulledBy(JoyconInput()))
    }
}
