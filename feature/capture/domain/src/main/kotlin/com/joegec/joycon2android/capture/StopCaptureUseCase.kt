package com.joegec.joycon2android.capture

class StopCaptureUseCase(private val repository: CaptureRepository) {
    operator fun invoke() = repository.stop()
}
