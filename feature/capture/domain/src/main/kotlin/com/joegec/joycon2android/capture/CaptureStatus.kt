package com.joegec.joycon2android.capture

/** [lastCapture] is a content URI another app can be granted to read. */
data class CaptureStatus(
    val revealed: Boolean = false,
    val step: CaptureStep? = null,
    val controllers: Int = 0,
    val lastCapture: String? = null,
) {
    val recording: Boolean get() = step != null
}
