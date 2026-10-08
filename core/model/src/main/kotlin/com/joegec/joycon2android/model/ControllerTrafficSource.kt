package com.joegec.joycon2android.model

/** Attaching replays [ControllerTraffic.Discovered] and [ControllerTraffic.Ready] for controllers already connected. */
interface ControllerTrafficSource {
    fun setTrafficListener(listener: ControllerTrafficListener?)
    fun readSpi(address: String, spiAddress: Int, length: Int)
}
