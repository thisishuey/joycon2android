package com.joegec.joycon2android.connection

import com.joegec.joycon2android.model.ControllerTraffic
import com.joegec.joycon2android.model.ControllerTrafficListener
import com.joegec.joycon2android.model.Side
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrafficRelayTest {

    private val relay = TrafficRelay()
    private val received = mutableListOf<ControllerTraffic>()

    @Test
    fun `nothing is built while no listener is attached`() {
        var built = false
        relay.emit { built = true; ControllerTraffic.Ready(ADDRESS) }
        assertFalse(built)
    }

    @Test
    fun `an attached listener receives what is emitted`() {
        relay.listener = ControllerTrafficListener { received += it }
        relay.emit { ControllerTraffic.Ready(ADDRESS) }
        assertTrue(received.single() is ControllerTraffic.Ready)
    }

    @Test
    fun `a discovery made before attaching is replayed afterwards`() {
        relay.discovered(discovery())
        relay.listener = ControllerTrafficListener { received += it }
        relay.replayDiscovery(ADDRESS)
        assertEquals(ADDRESS, (received.single() as ControllerTraffic.Discovered).address)
    }

    @Test
    fun `a controller no longer connected is not replayed`() {
        relay.discovered(discovery())
        relay.retain(emptySet())
        relay.listener = ControllerTrafficListener { received += it }
        relay.replayDiscovery(ADDRESS)
        assertTrue(received.isEmpty())
    }

    private fun discovery() = ControllerTraffic.Discovered(ADDRESS, ByteArray(16), Side.PRO, "Joy-Con 2")

    private companion object {
        const val ADDRESS = "AA:BB:CC:DD:EE:FF"
    }
}
