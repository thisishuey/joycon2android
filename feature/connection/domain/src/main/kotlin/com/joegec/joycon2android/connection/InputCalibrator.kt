package com.joegec.joycon2android.connection

import com.joegec.joycon2android.model.ControllerModel
import com.joegec.joycon2android.model.JoyconInput

class InputCalibrator(private val model: ControllerModel) {

    private val sticks = StickCalibrator()
    private val triggers = TriggerCalibrator()

    fun setTriggerZeros(left: Int?, right: Int?) = triggers.setFactoryZeros(left, right)

    // Bytes 0x3C/0x3D carry nothing meaningful on a controller without analog triggers.
    fun calibrate(input: JoyconInput): JoyconInput {
        val calibrated = sticks.calibrate(input)
        return if (model.hasAnalogTriggers) triggers.calibrate(calibrated)
        else calibrated.copy(triggerLeft = 0, triggerRight = 0)
    }
}
