package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.buttonmapping.preset.GameCubeMapping
import com.joegec.joycon2android.buttonmapping.target.GameCubeAnalogTrigger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class TriggerSourceTest {

    @Test
    fun `a trigger's travel round-trips through its stored id`() {
        TriggerSource.entries.forEach { trigger ->
            val source = MappingSource.Trigger(trigger)
            assertEquals(source, MappingSource.fromId(source.id))
        }
    }

    @Test
    fun `the gamecube layout drives a gamecube controller's analog triggers from its travel`() {
        val entries = GameCubeMapping.entries(JoyconSide.GAMECUBE)

        assertEquals("LEFT_TRIGGER", entries.getValue(GameCubeAnalogTrigger.TriggerLAnalog.name))
        assertEquals("RIGHT_TRIGGER", entries.getValue(GameCubeAnalogTrigger.TriggerRAnalog.name))
        assertFalse(GameCubeAnalogTrigger.TriggerLAnalog.name in GameCubeMapping.entries(JoyconSide.DUAL))
    }
}
