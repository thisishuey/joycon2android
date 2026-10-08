package com.joegec.joycon2android.capture

import kotlin.math.abs

/** Passes a packet only when its input bytes moved, so a capture isn't IMU and stick noise. */
class PacketChangeFilter(
    private val stickTolerance: Int = DEFAULT_STICK_TOLERANCE,
    private val triggerTolerance: Int = DEFAULT_TRIGGER_TOLERANCE,
) {

    private var last: Snapshot? = null
    private var lastUnknown: ByteArray? = null

    fun isNewInput(data: ByteArray): Boolean =
        if (data.size >= PacketLayout.SIZE_WITH_TRIGGERS) isNewKnownInput(data) else isNewUnknownInput(data)

    private fun isNewKnownInput(data: ByteArray): Boolean {
        val current = Snapshot.of(data)
        val previous = last
        if (previous != null && !current.differsFrom(previous)) return false
        last = current
        return true
    }

    // A layout the app doesn't know yet: any change past the counter is news.
    private fun isNewUnknownInput(data: ByteArray): Boolean {
        val payload = data.copyOfRange(minOf(COUNTER_SIZE, data.size), data.size)
        if (payload.contentEquals(lastUnknown)) return false
        lastUnknown = payload
        return true
    }

    private fun Snapshot.differsFrom(other: Snapshot): Boolean =
        !inputBytes.contentEquals(other.inputBytes) ||
            sticks.indices.any { abs(sticks[it] - other.sticks[it]) > stickTolerance } ||
            triggers.indices.any { abs(triggers[it] - other.triggers[it]) > triggerTolerance }

    private class Snapshot(val inputBytes: ByteArray, val sticks: IntArray, val triggers: IntArray) {
        companion object {
            fun of(data: ByteArray) = Snapshot(
                inputBytes = data.copyOfRange(PacketLayout.INPUT_BYTES.first, PacketLayout.INPUT_BYTES.last + 1),
                sticks = intArrayOf(
                    PacketLayout.stickX(data, PacketLayout.LEFT_STICK),
                    PacketLayout.stickY(data, PacketLayout.LEFT_STICK),
                    PacketLayout.stickX(data, PacketLayout.RIGHT_STICK),
                    PacketLayout.stickY(data, PacketLayout.RIGHT_STICK),
                ),
                triggers = intArrayOf(
                    PacketLayout.uint8(data, PacketLayout.TRIGGER_LEFT),
                    PacketLayout.uint8(data, PacketLayout.TRIGGER_RIGHT),
                ),
            )
        }
    }

    private companion object {
        const val DEFAULT_STICK_TOLERANCE = 24
        const val DEFAULT_TRIGGER_TOLERANCE = 3
        const val COUNTER_SIZE = 3
    }
}
