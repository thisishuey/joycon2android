package com.joegec.joycon2android.settings.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.model.AndroidKey
import com.joegec.joycon2android.model.ConnectionViewMode
import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.ui.components.SettingSwitch
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.TextDim

@Composable
fun SettingsPanel(
    state: SettingsPanelState,
    onViewModeChange: (ConnectionViewMode) -> Unit,
    onFasterUpdatesToggle: (Boolean) -> Unit,
    onBlockDeviceMotionToggle: (Boolean) -> Unit,
    onVersionTapped: () -> Unit,
    onBindAndroidKey: (AndroidKey, JoyconButton?) -> Unit,
    modifier: Modifier = Modifier,
    developerSection: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier
            .verticalScroll(rememberScrollState())
            .padding(Dimens.cardPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.sectionSpacing),
    ) {
        Text(
            stringResource(R.string.settings_title),
            color = Color.White,
            style = MaterialTheme.typography.headlineSmall,
        )
        SettingsSection { LayoutSetting(state.viewMode, onViewModeChange) }
        SettingsSection {
            SettingSwitch(
                title = stringResource(R.string.settings_faster_updates_title),
                description = stringResource(R.string.settings_faster_updates_description),
                checked = state.outputSettings.fasterUpdates,
                onCheckedChange = onFasterUpdatesToggle,
                titleStyle = MaterialTheme.typography.titleMedium,
                warning = stringResource(R.string.settings_faster_updates_warning),
            )
        }
        SettingsSection {
            SettingSwitch(
                title = stringResource(R.string.settings_block_device_motion_title),
                description = stringResource(R.string.settings_block_device_motion_description),
                checked = state.outputSettings.blockDeviceMotion,
                onCheckedChange = onBlockDeviceMotionToggle,
                titleStyle = MaterialTheme.typography.titleMedium,
                warning = if (state.deviceMotionBlockAvailable) null
                else stringResource(R.string.settings_block_device_motion_needs_shizuku),
            )
        }
        SettingsSection { AndroidKeysSetting(state.androidKeys, onBindAndroidKey) }
        developerSection?.let { SettingsSection { it() } }
        VersionLine(state.version, onVersionTapped)
    }
}

// Also the hidden developer-options gesture, so it carries no ripple: docs/capture.md#recording-one
@Composable
private fun VersionLine(version: String, onTapped: () -> Unit) {
    Text(
        stringResource(R.string.settings_version, version),
        color = TextDim,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.clickable(interactionSource = null, indication = null, onClick = onTapped),
    )
}
