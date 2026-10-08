package com.joegec.joycon2android.capture

class RevealCaptureUseCase(private val repository: CaptureRepository) {
    suspend operator fun invoke() = repository.reveal()
}
