package com.joegec.joycon2android.capture

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CaptureHeader(private val context: Context) {

    fun lines(startedAt: Date): List<String> = listOf(
        "# Joycon2Android ${appVersion()} controller capture",
        "# device: ${Build.MANUFACTURER} ${Build.MODEL}, Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
        "# started: ${SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US).format(startedAt)}",
        "# format: docs/capture.md",
    )

    private fun appVersion(): String = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty()
    } catch (_: PackageManager.NameNotFoundException) {
        "?"
    }
}
