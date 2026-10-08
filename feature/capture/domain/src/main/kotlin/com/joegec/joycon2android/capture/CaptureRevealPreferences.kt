package com.joegec.joycon2android.capture

import kotlinx.coroutines.flow.Flow

interface CaptureRevealPreferences {
    val revealed: Flow<Boolean>
    suspend fun reveal()
}
