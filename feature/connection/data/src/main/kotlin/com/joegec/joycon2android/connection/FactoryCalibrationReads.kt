package com.joegec.joycon2android.connection

import com.joegec.joycon2android.model.ControllerModel
import com.joegec.joycon2android.model.Side

/** docs/protocol.md#spi-reads */
object FactoryCalibrationReads {

    fun commands(model: ControllerModel): List<ByteArray> = buildList {
        add(SpiReadCommand.build(SpiStickCalibration.MAIN_ADDRESS, SpiStickCalibration.LENGTH))
        if (model.defaultSide != Side.LEFT && model.defaultSide != Side.RIGHT) {
            add(SpiReadCommand.build(SpiStickCalibration.RIGHT_ADDRESS, SpiStickCalibration.LENGTH))
        }
        if (model.hasAnalogTriggers) add(SpiReadCommand.build(SpiTriggerZeros.ADDRESS, SpiTriggerZeros.LENGTH))
    }

    /** Null when [reply] adds nothing: another read, or flash left unset. */
    fun update(factory: FactoryCalibration, reply: ByteArray): FactoryCalibration? {
        SpiStickCalibration.parse(reply, SpiStickCalibration.MAIN_ADDRESS)?.let { return factory.copy(mainStick = it) }
        SpiStickCalibration.parse(reply, SpiStickCalibration.RIGHT_ADDRESS)?.let { return factory.copy(rightStick = it) }
        SpiTriggerZeros.parse(reply)?.let { (left, right) ->
            return factory.copy(triggerZeroLeft = left, triggerZeroRight = right)
        }
        return null
    }
}
