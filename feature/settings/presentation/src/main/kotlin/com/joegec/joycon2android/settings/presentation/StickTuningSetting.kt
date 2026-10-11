package com.joegec.joycon2android.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.model.GamepadStick
import com.joegec.joycon2android.model.StickResponse
import com.joegec.joycon2android.model.StickTuning
import com.joegec.joycon2android.ui.theme.Accent
import com.joegec.joycon2android.ui.theme.ButtonOff
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.TextDim
import kotlin.math.roundToInt

private const val SLIDER_STEP = 5

@Composable
internal fun StickTuningSetting(
    tuning: StickTuning,
    onChange: (GamepadStick, StickResponse) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Dimens.elementSpacing)) {
        Text(
            stringResource(R.string.settings_stick_tuning_title),
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            stringResource(R.string.settings_stick_tuning_description),
            color = TextDim,
            style = MaterialTheme.typography.bodySmall,
        )
        GamepadStick.entries.forEach { stick ->
            StickControls(stick, tuning.of(stick), onChange = { onChange(stick, it) })
        }
    }
}

@Composable
private fun StickControls(stick: GamepadStick, response: StickResponse, onChange: (StickResponse) -> Unit) {
    Column {
        Text(stick.label(), color = Color.White, style = MaterialTheme.typography.bodyMedium)
        PercentSlider(
            label = stringResource(R.string.settings_stick_precision),
            value = response.precision,
            range = 0..StickResponse.PRECISION_FULL,
            onChange = { onChange(response.copy(precision = it)) },
        )
        PercentSlider(
            label = stringResource(R.string.settings_stick_max_tilt),
            value = response.maxTilt,
            range = StickResponse.MAX_TILT_LOWEST..StickResponse.MAX_TILT_FULL,
            onChange = { onChange(response.copy(maxTilt = it)) },
        )
    }
}

// Saves once the drag ends, not on every step of it.
@Composable
private fun PercentSlider(label: String, value: Int, range: IntRange, onChange: (Int) -> Unit) {
    var dragged by remember(value) { mutableFloatStateOf(value.toFloat()) }
    Column {
        Row {
            Text(label, color = TextDim, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
            Text(
                stringResource(R.string.settings_percent, dragged.roundToInt()),
                color = TextDim,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Slider(
            value = dragged,
            onValueChange = { dragged = it },
            onValueChangeFinished = { onChange(dragged.roundToInt()) },
            valueRange = range.first.toFloat()..range.last.toFloat(),
            steps = (range.last - range.first) / SLIDER_STEP - 1,
            colors = SliderDefaults.colors(
                thumbColor = Accent,
                activeTrackColor = Accent,
                inactiveTrackColor = ButtonOff,
            ),
        )
    }
}

@Composable
private fun GamepadStick.label(): String = when (this) {
    GamepadStick.LEFT -> stringResource(R.string.settings_stick_left)
    GamepadStick.RIGHT -> stringResource(R.string.settings_stick_right)
}
