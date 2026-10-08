package com.joegec.joycon2android.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.model.AndroidKey
import com.joegec.joycon2android.model.AndroidKeyBindings
import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.ui.components.DropdownOption
import com.joegec.joycon2android.ui.components.OptionDropdown
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.TextDim

private const val NONE_ID = "none"

@Composable
internal fun AndroidKeysSetting(
    bindings: AndroidKeyBindings,
    onBind: (AndroidKey, JoyconButton?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Dimens.elementSpacing)) {
        Text(
            stringResource(R.string.settings_android_keys_title),
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            stringResource(R.string.settings_android_keys_description),
            color = TextDim,
            style = MaterialTheme.typography.bodySmall,
        )
        AndroidKey.entries.forEach { key ->
            AndroidKeyRow(key, bindings.buttons[key], onBind = { button -> onBind(key, button) })
        }
    }
}

@Composable
private fun AndroidKeyRow(key: AndroidKey, button: JoyconButton?, onBind: (JoyconButton?) -> Unit) {
    val none = stringResource(R.string.settings_android_key_none)
    val options = listOf(DropdownOption(NONE_ID, none)) +
        AndroidKeyBindings.CHOICES.map { DropdownOption(it.id, it.label) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.elementSpacing),
    ) {
        Text(
            key.label(),
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        OptionDropdown(
            options = options,
            selectedId = button?.id ?: NONE_ID,
            label = button?.label ?: none,
            onSelect = { id -> onBind(AndroidKeyBindings.CHOICES.firstOrNull { it.id == id }) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun AndroidKey.label(): String = when (this) {
    AndroidKey.HOME -> stringResource(R.string.settings_android_key_home)
    AndroidKey.BACK -> stringResource(R.string.settings_android_key_back)
    AndroidKey.SCREENSHOT -> stringResource(R.string.settings_android_key_screenshot)
}
