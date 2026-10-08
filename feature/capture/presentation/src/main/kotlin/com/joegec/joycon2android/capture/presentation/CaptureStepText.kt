package com.joegec.joycon2android.capture.presentation

import androidx.annotation.StringRes
import com.joegec.joycon2android.capture.CaptureStep

internal class CaptureStepText(@StringRes val title: Int, @StringRes val instruction: Int)

internal fun CaptureStep.text(): CaptureStepText = when (this) {
    CaptureStep.REST -> CaptureStepText(R.string.capture_step_rest_title, R.string.capture_step_rest)
    CaptureStep.FACE_BUTTONS -> CaptureStepText(R.string.capture_step_face_title, R.string.capture_step_face)
    CaptureStep.DPAD -> CaptureStepText(R.string.capture_step_dpad_title, R.string.capture_step_dpad)
    CaptureStep.SHOULDER_BUTTONS ->
        CaptureStepText(R.string.capture_step_shoulders_title, R.string.capture_step_shoulders)
    CaptureStep.CENTRE_BUTTONS -> CaptureStepText(R.string.capture_step_centre_title, R.string.capture_step_centre)
    CaptureStep.STICK_CLICKS ->
        CaptureStepText(R.string.capture_step_stick_clicks_title, R.string.capture_step_stick_clicks)
    CaptureStep.BACK_BUTTONS -> CaptureStepText(R.string.capture_step_back_title, R.string.capture_step_back)
    CaptureStep.LEFT_STICK ->
        CaptureStepText(R.string.capture_step_left_stick_title, R.string.capture_step_left_stick)
    CaptureStep.RIGHT_STICK ->
        CaptureStepText(R.string.capture_step_right_stick_title, R.string.capture_step_right_stick)
    CaptureStep.LEFT_TRIGGER_SWEEP ->
        CaptureStepText(R.string.capture_step_left_sweep_title, R.string.capture_step_left_sweep)
    CaptureStep.LEFT_TRIGGER_FIRST_STOP ->
        CaptureStepText(R.string.capture_step_left_first_title, R.string.capture_step_left_first)
    CaptureStep.LEFT_TRIGGER_SECOND_STOP ->
        CaptureStepText(R.string.capture_step_left_second_title, R.string.capture_step_left_second)
    CaptureStep.RIGHT_TRIGGER_SWEEP ->
        CaptureStepText(R.string.capture_step_right_sweep_title, R.string.capture_step_right_sweep)
    CaptureStep.RIGHT_TRIGGER_FIRST_STOP ->
        CaptureStepText(R.string.capture_step_right_first_title, R.string.capture_step_right_first)
    CaptureStep.RIGHT_TRIGGER_SECOND_STOP ->
        CaptureStepText(R.string.capture_step_right_second_title, R.string.capture_step_right_second)
    CaptureStep.ALL_CONTROLLERS -> CaptureStepText(R.string.capture_step_all_title, R.string.capture_step_all)
}
