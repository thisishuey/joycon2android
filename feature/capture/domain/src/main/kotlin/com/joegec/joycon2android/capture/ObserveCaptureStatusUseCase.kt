package com.joegec.joycon2android.capture

import kotlinx.coroutines.flow.Flow

class ObserveCaptureStatusUseCase(private val repository: CaptureRepository) {
    operator fun invoke(): Flow<CaptureStatus> = repository.status
}
