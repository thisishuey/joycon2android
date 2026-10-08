package com.joegec.joycon2android.capture

/** In the order the guide walks them: docs/capture.md#steps */
enum class CaptureStep {
    REST,
    FACE_BUTTONS,
    DPAD,
    SHOULDER_BUTTONS,
    CENTRE_BUTTONS,
    STICK_CLICKS,
    BACK_BUTTONS,
    LEFT_STICK,
    RIGHT_STICK,
    LEFT_TRIGGER_SWEEP,
    LEFT_TRIGGER_FIRST_STOP,
    LEFT_TRIGGER_SECOND_STOP,
    RIGHT_TRIGGER_SWEEP,
    RIGHT_TRIGGER_FIRST_STOP,
    RIGHT_TRIGGER_SECOND_STOP,
    ALL_CONTROLLERS;

    val next: CaptureStep? get() = entries.getOrNull(ordinal + 1)
    val number: Int get() = ordinal + 1

    companion object {
        val first: CaptureStep = entries.first()
        val count: Int = entries.size
    }
}
