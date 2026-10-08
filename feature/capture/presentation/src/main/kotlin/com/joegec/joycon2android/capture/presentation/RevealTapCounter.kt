package com.joegec.joycon2android.capture.presentation

/** Android's own developer-options gesture: seven taps on the version. */
class RevealTapCounter(private val tapsNeeded: Int = 7) {

    private var taps = 0

    fun tap(): Boolean = ++taps == tapsNeeded
}
