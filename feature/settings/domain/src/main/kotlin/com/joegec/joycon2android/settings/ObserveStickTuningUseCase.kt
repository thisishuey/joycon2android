package com.joegec.joycon2android.settings

import com.joegec.joycon2android.model.StickTuning
import kotlinx.coroutines.flow.Flow

class ObserveStickTuningUseCase(private val repository: StickTuningRepository) {
    operator fun invoke(): Flow<StickTuning> = repository.tuning
}
