package com.joegec.joycon2android.model

/** What a capture records: docs/capture.md */
sealed interface ControllerTraffic {
    val address: String

    class Discovered(
        override val address: String,
        val manufacturerData: ByteArray,
        val side: Side,
        val name: String,
    ) : ControllerTraffic

    class Connected(override val address: String, val mtu: Int) : ControllerTraffic

    class Ready(override val address: String) : ControllerTraffic

    class Disconnected(override val address: String, val gattStatus: Int) : ControllerTraffic

    class Input(override val address: String, val packet: ByteArray, val decoded: JoyconInput?) : ControllerTraffic

    class Reply(override val address: String, val packet: ByteArray) : ControllerTraffic
}
