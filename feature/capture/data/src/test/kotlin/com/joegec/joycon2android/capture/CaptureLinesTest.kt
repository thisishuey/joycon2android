package com.joegec.joycon2android.capture

import com.joegec.joycon2android.model.ControllerTraffic
import com.joegec.joycon2android.model.JoyconInput
import com.joegec.joycon2android.model.Side
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CaptureLinesTest {

    private val lines = CaptureLines(startNanos = 1_000_000_000)

    @Test
    fun `a line carries seconds since start, the step and the address`() {
        val line = lines.traffic(ControllerTraffic.Ready(ADDRESS), CaptureStep.DPAD, 3_500_000_000).single()
        assertEquals("2.500 DPAD $ADDRESS ready", line)
    }

    @Test
    fun `a discovery records the manufacturer data that holds the product ID`() {
        val mfg = byteArrayOf(0x01, 0x00, 0x03, 0x7E, 0x05, 0x73, 0x20)
        val line = lines.traffic(ControllerTraffic.Discovered(ADDRESS, mfg, Side.PRO, "Joy-Con 2"), CaptureStep.REST, 1_000_000_000).single()
        assertTrue(line.endsWith("discovered side=PRO name=\"Joy-Con 2\" mfg=01 00 03 7E 05 73 20"))
    }

    @Test
    fun `an input line decodes the fields before the raw bytes`() {
        val packet = ByteArray(63).apply {
            this[0x04] = 0x08 // A
            this[0x3C] = 0xEA.toByte()
            this[0x3D] = 0x22
        }
        val input = JoyconInput(pressed = setOf("A"))
        val line = lines.traffic(ControllerTraffic.Input(ADDRESS, packet, input), CaptureStep.FACE_BUTTONS, 1_000_000_000).single()
        assertTrue(line, line.contains("input btn=00000800 pad=00 L=0,0 R=0,0 trig=234,34 pressed=A len=63 raw=00 00 00 00 08"))
    }

    @Test
    fun `an unchanged input is not repeated`() {
        val packet = ByteArray(63)
        lines.traffic(ControllerTraffic.Input(ADDRESS, packet, null), CaptureStep.REST, 1_000_000_000)
        assertTrue(lines.traffic(ControllerTraffic.Input(ADDRESS, packet, null), CaptureStep.REST, 1_010_000_000).isEmpty())
    }

    @Test
    fun `controllers are filtered independently`() {
        val packet = ByteArray(63)
        lines.traffic(ControllerTraffic.Input(ADDRESS, packet, null), CaptureStep.REST, 1_000_000_000)
        val other = lines.traffic(ControllerTraffic.Input(OTHER, packet, null), CaptureStep.REST, 1_000_000_000)
        assertEquals(1, other.size)
    }

    @Test
    fun `a step line counts through the guide`() {
        assertEquals("0.000 REST - step 1/${CaptureStep.count}", lines.step(CaptureStep.REST, 1_000_000_000))
    }

    @Test
    fun `an spi read names the address requested`() {
        val line = lines.spiRead(ADDRESS, 0x013140, 64, CaptureStep.REST, 1_000_000_000)
        assertTrue(line.endsWith("spi-read 0x013140 len=64"))
    }

    private companion object {
        const val ADDRESS = "AA:BB:CC:DD:EE:FF"
        const val OTHER = "11:22:33:44:55:66"
    }
}
