package com.joegec.joycon2android.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StickResponseTest {

    private val halfTiltRight = 2048 + 1024

    @Test
    fun `the default leaves every value untouched`() {
        assertEquals(halfTiltRight to 2048, StickResponse().apply(halfTiltRight, 2048))
        assertEquals(4095 to 0, StickResponse().apply(4095, 0))
    }

    @Test
    fun `precision shrinks a half tilt but still reaches full at the rim`() {
        val precise = StickResponse(precision = StickResponse.PRECISION_FULL)

        val (halfX, _) = precise.apply(halfTiltRight, 2048)
        val (fullX, _) = precise.apply(4095, 2048)

        assertTrue("half tilt $halfX", halfX in 2048 + 340..2048 + 390) // 0.5^2.5 ≈ 0.18 of full
        assertEquals(4095, fullX)
    }

    @Test
    fun `max tilt caps full deflection`() {
        val (x, y) = StickResponse(maxTilt = 50).apply(4095, 2048)

        assertTrue("x $x", x in 3071..3072) // half of full deflection
        assertEquals(2048, y)
    }

    @Test
    fun `reshaping keeps the tilt's direction`() {
        val (x, y) = StickResponse(precision = 60).apply(2048 + 800, 2048 - 800)

        assertEquals(x - 2048, 2048 - y)
    }

    @Test
    fun `tuning applies to the gamepad's sticks, main and C separately`() {
        val pro = ConnectedJoycon(
            address = "pro", side = Side.PRO, deviceName = "Pro",
            input = JoyconInput(stickX = 4095, rightStickX = 4095),
        )
        val tuning = StickTuning(right = StickResponse(maxTilt = 50))
        val gamepad = PlayerState(PlayerNumber.P1, left = pro, right = pro, stickTuning = tuning).gamepad

        assertEquals(4095, gamepad.leftStickX)
        assertTrue("right ${gamepad.rightStickX}", gamepad.rightStickX in 3071..3072)
    }
}
