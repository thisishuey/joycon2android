package com.joegec.joycon2android.capture

import com.joegec.joycon2android.model.JoyconInput

/** Decoded fields before stick calibration, then the bytes: docs/capture.md#format */
object RawPacketFormat {

    fun describe(packet: ByteArray, decoded: JoyconInput?): String {
        val pressed = decoded?.pressed?.sorted()?.joinToString("+")?.ifEmpty { "-" } ?: "unparsed"
        return "${fields(packet)}pressed=$pressed len=${packet.size} raw=${hex(packet)}"
    }

    fun hex(bytes: ByteArray): String = bytes.joinToString(" ") { "%02X".format(it) }

    private fun fields(packet: ByteArray): String {
        if (packet.size < PacketLayout.SIZE_WITH_TRIGGERS) return ""
        return "btn=%08X pad=%02X L=%d,%d R=%d,%d trig=%d,%d ".format(
            PacketLayout.buttons(packet),
            PacketLayout.uint8(packet, PacketLayout.PADDLES),
            PacketLayout.stickX(packet, PacketLayout.LEFT_STICK),
            PacketLayout.stickY(packet, PacketLayout.LEFT_STICK),
            PacketLayout.stickX(packet, PacketLayout.RIGHT_STICK),
            PacketLayout.stickY(packet, PacketLayout.RIGHT_STICK),
            PacketLayout.uint8(packet, PacketLayout.TRIGGER_LEFT),
            PacketLayout.uint8(packet, PacketLayout.TRIGGER_RIGHT),
        )
    }
}
