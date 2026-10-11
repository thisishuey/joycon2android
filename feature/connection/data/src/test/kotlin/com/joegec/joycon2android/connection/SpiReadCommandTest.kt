package com.joegec.joycon2android.connection

import org.junit.Assert.assertArrayEquals
import org.junit.Test

class SpiReadCommandTest {

    @Test
    fun `the device info read matches the command the controller answers`() {
        assertArrayEquals(
            bytes("02 91 00 04 00 08 00 00 40 7E 00 00 00 30 01 00"),
            SpiReadCommand.build(0x013000, 0x40),
        )
    }

    @Test
    fun `length and little-endian address land in their fields`() {
        assertArrayEquals(
            bytes("02 91 00 04 00 08 00 00 02 7E 00 00 40 31 01 00"),
            SpiReadCommand.build(0x013140, 0x02),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `a read longer than one block is rejected`() {
        SpiReadCommand.build(0x013000, 0x41)
    }

    private fun bytes(hex: String): ByteArray =
        hex.split(" ").map { it.toInt(16).toByte() }.toByteArray()
}
