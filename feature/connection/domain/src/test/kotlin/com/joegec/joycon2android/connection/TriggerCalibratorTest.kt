package com.joegec.joycon2android.connection

import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.JoyconInput
import org.junit.Assert.assertEquals
import org.junit.Test

class TriggerCalibratorTest {

    private val calibrator = TriggerCalibrator().apply { setFactoryZeros(33, 32) }

    private fun left(raw: Int, clicked: Boolean = false) = calibrator.calibrate(
        JoyconInput(triggerLeft = raw, pressed = if (clicked) setOf(JoyconButton.L.id) else emptySet()),
    ).triggerLeft

    @Test
    fun `rest a few counts above the factory zero reads as released`() {
        assertEquals(0, left(37))
    }

    @Test
    fun `travel scales up to the seeded first stop, short of full`() {
        assertEquals(250, left(170))
        assertEquals(125, left(104)) // halfway between 38 and 170
    }

    @Test
    fun `a held first stop beyond the seed becomes the top of travel`() {
        repeat(5) { left(194) }
        assertEquals(250, left(194))
        assertEquals(179, left(150))
    }

    @Test
    fun `a quick press through to the click does not move the top of travel`() {
        listOf(120, 190, 220).forEach { left(it) }
        assertEquals(250, left(170))
    }

    @Test
    fun `a value held while clicked does not move the top of travel`() {
        repeat(5) { left(200, clicked = true) }
        assertEquals(250, left(170))
    }

    @Test
    fun `travel past the first stop without the click stays short of full`() {
        assertEquals(250, left(240))
    }

    @Test
    fun `the click reads full whatever the travel`() {
        assertEquals(255, left(220, clicked = true))
        assertEquals(255, left(155, clicked = true))
    }

    @Test
    fun `without a factory zero the lowest value seen stands in`() {
        val fresh = TriggerCalibrator()
        val input = { raw: Int -> fresh.calibrate(JoyconInput(triggerLeft = raw)).triggerLeft }
        input(40)
        assertEquals(0, input(44))
        assertEquals(250, input(170))
    }
}
