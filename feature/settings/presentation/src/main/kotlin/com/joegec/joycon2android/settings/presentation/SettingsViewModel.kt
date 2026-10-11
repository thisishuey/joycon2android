package com.joegec.joycon2android.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joegec.joycon2android.model.AndroidKey
import com.joegec.joycon2android.model.AndroidKeyBindings
import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.settings.BindAndroidKeyUseCase
import com.joegec.joycon2android.settings.ObserveAndroidKeyBindingsUseCase
import com.joegec.joycon2android.model.GamepadStick
import com.joegec.joycon2android.model.StickResponse
import com.joegec.joycon2android.model.StickTuning
import com.joegec.joycon2android.settings.ObserveOutputSettingsUseCase
import com.joegec.joycon2android.settings.ObserveStickTuningUseCase
import com.joegec.joycon2android.settings.SetStickResponseUseCase
import com.joegec.joycon2android.settings.OutputSettings
import com.joegec.joycon2android.settings.SetBlockDeviceMotionUseCase
import com.joegec.joycon2android.settings.SetFasterUpdatesUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    observeOutputSettings: ObserveOutputSettingsUseCase,
    private val setFasterUpdates: SetFasterUpdatesUseCase,
    private val setBlockDeviceMotion: SetBlockDeviceMotionUseCase,
    observeAndroidKeyBindings: ObserveAndroidKeyBindingsUseCase,
    private val bindAndroidKey: BindAndroidKeyUseCase,
    observeStickTuning: ObserveStickTuningUseCase,
    private val setStickResponse: SetStickResponseUseCase,
) : ViewModel() {

    val outputSettings: StateFlow<OutputSettings> = observeOutputSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), OutputSettings())

    val androidKeys: StateFlow<AndroidKeyBindings> = observeAndroidKeyBindings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), AndroidKeyBindings())

    val stickTuning: StateFlow<StickTuning> = observeStickTuning()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), StickTuning())

    fun toggleFasterUpdates(enabled: Boolean) {
        viewModelScope.launch { setFasterUpdates(enabled) }
    }

    fun toggleBlockDeviceMotion(enabled: Boolean) {
        viewModelScope.launch { setBlockDeviceMotion(enabled) }
    }

    fun assignAndroidKey(key: AndroidKey, button: JoyconButton?) {
        viewModelScope.launch { bindAndroidKey(key, button) }
    }

    fun changeStickResponse(stick: GamepadStick, response: StickResponse) {
        viewModelScope.launch { setStickResponse(stick, response) }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
