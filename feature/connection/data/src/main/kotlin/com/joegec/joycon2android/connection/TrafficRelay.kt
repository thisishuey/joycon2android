package com.joegec.joycon2android.connection

import com.joegec.joycon2android.model.ControllerTraffic
import com.joegec.joycon2android.model.ControllerTrafficListener
import java.util.concurrent.ConcurrentHashMap

/** [emit] builds nothing while no listener is attached: it sits on the per-packet path. */
class TrafficRelay {

    @Volatile
    var listener: ControllerTrafficListener? = null

    private val discoveries = ConcurrentHashMap<String, ControllerTraffic.Discovered>()

    inline fun emit(traffic: () -> ControllerTraffic) {
        listener?.onTraffic(traffic())
    }

    fun discovered(traffic: ControllerTraffic.Discovered) {
        discoveries[traffic.address] = traffic
        listener?.onTraffic(traffic)
    }

    fun replayDiscovery(address: String) {
        val current = listener ?: return
        discoveries[address]?.let(current::onTraffic)
    }

    fun retain(addresses: Set<String>) {
        discoveries.keys.retainAll(addresses)
    }
}
