package com.joegec.joycon2android.capture

import kotlin.math.roundToInt

class PacketRateCounter(private val windowNanos: Long = NANOS_PER_SECOND) {

    private var windowStart = NO_WINDOW
    private var count = 0

    /** Packets per second over the window this packet closes, or null while that window is open. */
    fun count(nanos: Long): Int? {
        if (windowStart == NO_WINDOW) {
            open(nanos)
            return null
        }
        val elapsed = nanos - windowStart
        if (elapsed < windowNanos) {
            count++
            return null
        }
        val rate = (count * NANOS_PER_SECOND.toDouble() / elapsed).roundToInt()
        open(nanos)
        return rate
    }

    private fun open(nanos: Long) {
        windowStart = nanos
        count = 1
    }

    private companion object {
        const val NANOS_PER_SECOND = 1_000_000_000L
        const val NO_WINDOW = Long.MIN_VALUE
    }
}
