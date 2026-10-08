package com.joegec.joycon2android.capture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PacketRateCounterTest {

    @Test
    fun `nothing is reported until a second has passed`() {
        val counter = PacketRateCounter()
        assertNull(counter.count(0))
        assertNull(counter.count(500_000_000))
    }

    @Test
    fun `the packet that closes a second reports that second's rate`() {
        val counter = PacketRateCounter()
        val interval = 15_000_000L // 15 ms connection interval
        val rates = (0..67).map { counter.count(it * interval) }
        assertEquals(67, rates.last())
    }

    @Test
    fun `a silent gap lowers the rate rather than hiding it`() {
        val counter = PacketRateCounter()
        counter.count(0)
        assertEquals(1, counter.count(1_000_000_000))
        assertEquals(0, counter.count(4_000_000_000))
    }
}
