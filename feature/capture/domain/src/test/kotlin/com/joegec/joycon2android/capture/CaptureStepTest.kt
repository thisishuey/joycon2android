package com.joegec.joycon2android.capture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CaptureStepTest {

    @Test
    fun `the guide starts with the controller at rest`() {
        assertEquals(CaptureStep.REST, CaptureStep.first)
    }

    @Test
    fun `each trigger is swept before its stops are held`() {
        assertEquals(CaptureStep.LEFT_TRIGGER_FIRST_STOP, CaptureStep.LEFT_TRIGGER_SWEEP.next)
        assertEquals(CaptureStep.LEFT_TRIGGER_SECOND_STOP, CaptureStep.LEFT_TRIGGER_FIRST_STOP.next)
        assertEquals(CaptureStep.RIGHT_TRIGGER_FIRST_STOP, CaptureStep.RIGHT_TRIGGER_SWEEP.next)
    }

    @Test
    fun `the last step has no next`() {
        assertNull(CaptureStep.ALL_CONTROLLERS.next)
        assertEquals(CaptureStep.count, CaptureStep.ALL_CONTROLLERS.number)
    }
}
