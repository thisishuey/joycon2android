package com.joegec.joycon2android.model

import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.roundToInt

/** Shape and limits: docs/virtual-gamepad.md#stick-response */
data class StickResponse(val precision: Int = 0, val maxTilt: Int = MAX_TILT_FULL) {

    val isDefault: Boolean get() = precision == 0 && maxTilt == MAX_TILT_FULL

    /** Raw 0..4095 axes, centre 2048, reshaped along the tilt's own direction. */
    fun apply(x: Int, y: Int): Pair<Int, Int> {
        if (isDefault) return x to y
        val dx = (x - CENTER) / HALF_RANGE
        val dy = (y - CENTER) / HALF_RANGE
        val tilt = hypot(dx, dy)
        if (tilt == 0.0) return x to y
        val shaped = tilt.coerceAtMost(1.0).pow(exponent) * maxTilt / MAX_TILT_FULL
        val scale = shaped / tilt
        return axis(dx * scale) to axis(dy * scale)
    }

    private val exponent: Double get() = 1.0 + precision / PRECISION_FULL.toDouble() * MAX_EXTRA_EXPONENT

    private fun axis(offset: Double) = (CENTER + offset * HALF_RANGE).roundToInt().coerceIn(0, AXIS_MAX)

    companion object {
        const val PRECISION_FULL = 100
        const val MAX_TILT_FULL = 100
        const val MAX_TILT_LOWEST = 40
        private const val MAX_EXTRA_EXPONENT = 1.5 // full precision: output = tilt^2.5
        private const val CENTER = 2048
        private const val HALF_RANGE = 2047.0 // centre to 4095
        private const val AXIS_MAX = 4095
    }
}
