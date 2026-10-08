package com.joegec.joycon2android.capture

class StartCaptureUseCase(private val repository: CaptureRepository) {
    operator fun invoke() = repository.start()
}
