package com.joegec.joycon2android.capture

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PacketChangeFilterTest {

    private fun packet(
        buttons: Int = 0,
        leftStick: Pair<Int, Int> = 2048 to 2048,
        triggerLeft: Int = 0x22,
        gyroNoise: Int = 0,
    ): ByteArray = ByteArray(63).apply {
        this[0x03] = (buttons and 0xFF).toByte()
        this[0x04] = ((buttons shr 8) and 0xFF).toByte()
        val (x, y) = leftStick
        val v = (x and 0xFFF) or ((y and 0xFFF) shl 12)
        this[0x0A] = (v and 0xFF).toByte()
        this[0x0B] = ((v shr 8) and 0xFF).toByte()
        this[0x0C] = ((v shr 16) and 0xFF).toByte()
        this[0x36] = gyroNoise.toByte()
        this[0x3C] = triggerLeft.toByte()
    }

    @Test
    fun `the first packet always passes`() {
        assertTrue(PacketChangeFilter().isNewInput(packet()))
    }

    @Test
    fun `motion and counter changes alone are filtered out`() {
        val filter = PacketChangeFilter()
        filter.isNewInput(packet())
        assertFalse(filter.isNewInput(packet(gyroNoise = 0x55).apply { this[0x00] = 9 }))
    }

    @Test
    fun `a button change passes`() {
        val filter = PacketChangeFilter()
        filter.isNewInput(packet())
        assertTrue(filter.isNewInput(packet(buttons = 0x0800)))
    }

    @Test
    fun `stick jitter within tolerance is filtered out but a real move passes`() {
        val filter = PacketChangeFilter(stickTolerance = 24)
        filter.isNewInput(packet())
        assertFalse(filter.isNewInput(packet(leftStick = 2060 to 2040)))
        assertTrue(filter.isNewInput(packet(leftStick = 3000 to 2048)))
    }

    @Test
    fun `trigger travel beyond tolerance passes`() {
        val filter = PacketChangeFilter(triggerTolerance = 3)
        filter.isNewInput(packet())
        assertFalse(filter.isNewInput(packet(triggerLeft = 0x24)))
        assertTrue(filter.isNewInput(packet(triggerLeft = 0x80)))
    }

    @Test
    fun `a short packet of an unknown layout passes when anything past the counter changes`() {
        val filter = PacketChangeFilter()
        assertTrue(filter.isNewInput(byteArrayOf(1, 0, 0, 7)))
        assertFalse(filter.isNewInput(byteArrayOf(2, 0, 0, 7)))
        assertTrue(filter.isNewInput(byteArrayOf(3, 0, 0, 8)))
    }
}
