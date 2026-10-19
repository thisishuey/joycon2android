package com.joegec.joycon2android.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameCubeButtonsTest {

    @Test
    fun `a gamecube controller has no minus or stick clicks, and offers GL and GR for its trigger clicks`() {
        listOf(JoyconButton.Minus, JoyconButton.LS, JoyconButton.RS).forEach {
            assertFalse("$it", it in GameCubeButtons.present)
        }
        assertTrue(JoyconButton.GL in GameCubeButtons.present && JoyconButton.GR in GameCubeButtons.present)
    }

    @Test
    fun `its ZR is marked Z and every other button keeps its marking`() {
        assertEquals("Z", GameCubeButtons.marking(JoyconButton.ZR))
        assertNull(GameCubeButtons.marking(JoyconButton.ZL))
    }

    @Test
    fun `a trigger's first travel presses L or R`() {
        val input = GameCubeButtons.withTriggerPresses(JoyconInput(triggerLeft = 1, pressed = setOf(JoyconButton.A.id)))
        assertEquals(setOf(JoyconButton.A.id, JoyconButton.L.id), input.pressed)
    }

    @Test
    fun `a trigger's click presses GL or GR as well as L or R`() {
        val input = GameCubeButtons.withTriggerPresses(JoyconInput(pressed = setOf(JoyconButton.R.id)))
        assertEquals(setOf(JoyconButton.R.id, JoyconButton.GR.id), input.pressed)
    }

    @Test
    fun `a trigger at rest presses nothing`() {
        assertEquals(emptySet<String>(), GameCubeButtons.withTriggerPresses(JoyconInput()).pressed)
    }

    @Test
    fun `its trigger clicks ride the stick-click slots over DSU`() {
        assertEquals(JoyconButton.LS, GameCubeButtons.onDsu(JoyconButton.GL))
        assertEquals(JoyconButton.A, GameCubeButtons.onDsu(JoyconButton.A))
        assertEquals(setOf(JoyconButton.RS.id, JoyconButton.L.id), GameCubeButtons.onDsu(setOf(JoyconButton.GR.id, JoyconButton.L.id)))
    }
}
