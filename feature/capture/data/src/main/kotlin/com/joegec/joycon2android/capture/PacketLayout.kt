package com.joegec.joycon2android.capture

/** docs/protocol.md#packet-layout */
internal object PacketLayout {
    const val BUTTONS = 0x03
    const val PADDLES = 0x07
    const val LEFT_STICK = 0x0A
    const val RIGHT_STICK = 0x0D
    const val TRIGGER_LEFT = 0x3C
    const val TRIGGER_RIGHT = 0x3D
    const val SIZE_WITH_TRIGGERS = TRIGGER_RIGHT + 1

    // Buttons, back paddles and the two unmapped bytes before the left stick.
    val INPUT_BYTES = BUTTONS until LEFT_STICK

    fun uint8(data: ByteArray, offset: Int): Int = data[offset].toInt() and 0xFF

    fun buttons(data: ByteArray): Long =
        (0..3).fold(0L) { acc, i -> acc or (uint8(data, BUTTONS + i).toLong() shl (8 * i)) }

    fun stickX(data: ByteArray, offset: Int): Int = packedStick(data, offset) and 0xFFF

    fun stickY(data: ByteArray, offset: Int): Int = (packedStick(data, offset) shr 12) and 0xFFF

    private fun packedStick(data: ByteArray, offset: Int): Int =
        uint8(data, offset) or (uint8(data, offset + 1) shl 8) or (uint8(data, offset + 2) shl 16)
}
