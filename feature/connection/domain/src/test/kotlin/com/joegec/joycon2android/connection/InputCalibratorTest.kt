package com.joegec.joycon2android.connection

import com.joegec.joycon2android.model.ControllerModel
import com.joegec.joycon2android.model.JoyconInput
import org.junit.Assert.assertEquals
import org.junit.Test

class InputCalibratorTest {

    @Test
    fun `a controller without analog triggers reports none`() {
        val calibrated = InputCalibrator(ControllerModel.PRO_CONTROLLER).calibrate(JoyconInput(triggerLeft = 120, triggerRight = 90))
        assertEquals(0, calibrated.triggerLeft)
        assertEquals(0, calibrated.triggerRight)
    }

    @Test
    fun `a gamecube controller's triggers are calibrated`() {
        val calibrator = InputCalibrator(ControllerModel.GAMECUBE).apply { setTriggerZeros(33, 32) }
        assertEquals(255, calibrator.calibrate(JoyconInput(triggerLeft = 180)).triggerLeft)
    }
}
