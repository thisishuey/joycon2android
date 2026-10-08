package com.joegec.joycon2android.capture.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.capture.CaptureStatus
import com.joegec.joycon2android.capture.CaptureStep
import com.joegec.joycon2android.ui.theme.Accent
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.TextBright
import com.joegec.joycon2android.ui.theme.TextDim
import com.joegec.joycon2android.ui.theme.TextOnAccent

@Composable
fun CaptureSection(
    status: CaptureStatus,
    onStart: () -> Unit,
    onNext: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Dimens.elementSpacing)) {
        Text(stringResource(R.string.capture_title), color = Color.White, style = MaterialTheme.typography.titleMedium)
        val step = status.step
        if (step == null) {
            Text(stringResource(R.string.capture_description), color = TextDim, style = MaterialTheme.typography.bodySmall)
            IdleActions(status.lastCapture, onStart)
        } else {
            RecordingStep(step, status.controllers)
            RecordingActions(isLast = step.next == null, onNext, onStop)
        }
    }
}

@Composable
private fun RecordingStep(step: CaptureStep, controllers: Int) {
    val text = step.text()
    Text(
        stringResource(R.string.capture_step_progress, step.number, CaptureStep.count),
        color = Accent,
        style = MaterialTheme.typography.labelMedium,
    )
    Text(stringResource(text.title), color = TextBright, style = MaterialTheme.typography.titleSmall)
    Text(stringResource(text.instruction), color = TextBright, style = MaterialTheme.typography.bodyMedium)
    Text(
        if (controllers == 0) stringResource(R.string.capture_no_controllers)
        else pluralStringResource(R.plurals.capture_controllers, controllers, controllers),
        color = TextDim,
        style = MaterialTheme.typography.bodySmall,
    )
}

@Composable
private fun IdleActions(lastCapture: String?, onStart: () -> Unit) {
    val context = LocalContext.current
    val chooserTitle = stringResource(R.string.capture_share_chooser)
    PrimaryButton(stringResource(R.string.capture_start), onStart, Modifier.fillMaxWidth())
    lastCapture?.let { uri ->
        SecondaryButton(
            stringResource(R.string.capture_share),
            onClick = { context.shareCapture(uri, chooserTitle) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun RecordingActions(isLast: Boolean, onNext: () -> Unit, onStop: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.elementSpacing)) {
        SecondaryButton(stringResource(R.string.capture_stop), onStop, Modifier.weight(1f))
        PrimaryButton(
            stringResource(if (isLast) R.string.capture_finish else R.string.capture_next),
            onNext,
            Modifier.weight(1f),
        )
    }
}

@Composable
private fun PrimaryButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.height(Dimens.buttonHeight),
        shape = RoundedCornerShape(Dimens.buttonCorner),
        colors = ButtonDefaults.buttonColors(containerColor = Accent),
    ) {
        Text(label, color = TextOnAccent, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun SecondaryButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(Dimens.buttonHeight),
        shape = RoundedCornerShape(Dimens.buttonCorner),
    ) {
        Text(label, color = TextDim, style = MaterialTheme.typography.labelLarge)
    }
}
