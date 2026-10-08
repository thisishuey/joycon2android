package com.joegec.joycon2android.capture.presentation

import org.junit.Assert.assertEquals
import org.junit.Test

class RevealTapCounterTest {

    @Test
    fun `only the seventh tap reveals`() {
        val counter = RevealTapCounter()
        val reveals = (1..10).map { counter.tap() }
        assertEquals(listOf(7), reveals.indices.filter { reveals[it] }.map { it + 1 })
    }
}
