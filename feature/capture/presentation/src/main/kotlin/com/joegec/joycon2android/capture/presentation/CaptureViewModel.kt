package com.joegec.joycon2android.capture.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joegec.joycon2android.capture.CaptureStatus
import com.joegec.joycon2android.capture.NextCaptureStepUseCase
import com.joegec.joycon2android.capture.ObserveCaptureStatusUseCase
import com.joegec.joycon2android.capture.RevealCaptureUseCase
import com.joegec.joycon2android.capture.StartCaptureUseCase
import com.joegec.joycon2android.capture.StopCaptureUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CaptureViewModel(
    observeCaptureStatus: ObserveCaptureStatusUseCase,
    private val revealCapture: RevealCaptureUseCase,
    private val startCapture: StartCaptureUseCase,
    private val nextCaptureStep: NextCaptureStepUseCase,
    private val stopCapture: StopCaptureUseCase,
) : ViewModel() {

    val status: StateFlow<CaptureStatus> = observeCaptureStatus()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), CaptureStatus())

    private val revealTaps = RevealTapCounter()

    fun onVersionTapped() {
        if (revealTaps.tap()) viewModelScope.launch { revealCapture() }
    }

    fun start() = startCapture()

    fun next() = nextCaptureStep()

    fun stop() = stopCapture()

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
