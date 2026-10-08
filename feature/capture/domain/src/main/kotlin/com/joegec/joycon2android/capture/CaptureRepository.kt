package com.joegec.joycon2android.capture

import kotlinx.coroutines.flow.Flow

interface CaptureRepository {
    val status: Flow<CaptureStatus>

    suspend fun reveal()
    fun start()
    fun nextStep()
    fun stop()
}
