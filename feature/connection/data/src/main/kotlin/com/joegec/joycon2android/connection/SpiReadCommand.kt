package com.joegec.joycon2android.connection

/** docs/protocol.md#spi-reads */
object SpiReadCommand {

    private const val MAX_LENGTH = 0x40

    fun build(spiAddress: Int, length: Int): ByteArray {
        require(length in 1..MAX_LENGTH) { "SPI reads are 1..$MAX_LENGTH bytes, got $length" }
        return byteArrayOf(
            0x02, 0x91.toByte(), 0x00, 0x04, 0x00, 0x08, 0x00, 0x00,
            length.toByte(), 0x7E, 0x00, 0x00,
            spiAddress.toByte(), (spiAddress shr 8).toByte(), (spiAddress shr 16).toByte(), (spiAddress shr 24).toByte(),
        )
    }
}
