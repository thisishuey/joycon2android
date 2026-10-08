package com.joegec.joycon2android.capture

class NextCaptureStepUseCase(private val repository: CaptureRepository) {
    operator fun invoke() = repository.nextStep()
}
