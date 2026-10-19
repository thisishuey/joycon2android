package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.model.JoyconButton

/** What drives a mapping target: a physical button, one direction of a physical stick, or a trigger's travel. */
sealed interface MappingSource {
    val id: String

    data class Button(val button: JoyconButton) : MappingSource {
        override val id: String get() = button.name
    }

    data class Stick(val stick: StickSource, val direction: StickDirection) : MappingSource {
        override val id: String get() = "${stick.name}_${direction.name}"
    }

    data class Trigger(val trigger: TriggerSource) : MappingSource {
        override val id: String get() = trigger.name
    }

    companion object {
        /** Null for "None" or an id no longer recognised, so a stale entry binds nothing. */
        fun fromId(id: String): MappingSource? =
            JoyconButton.entries.firstOrNull { it.name == id }?.let(::Button)
                ?: allStickDirections().firstOrNull { it.id == id }
                ?: TriggerSource.entries.firstOrNull { it.name == id }?.let(::Trigger)

        fun directionsOf(stick: StickSource): List<Stick> = StickDirection.entries.map { Stick(stick, it) }

        private fun allStickDirections() = StickSource.entries.flatMap(::directionsOf)
    }
}
