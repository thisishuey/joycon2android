package com.joegec.joycon2android.capture

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CaptureFiles {
    private const val DIRECTORY = "captures"

    fun directory(context: Context): File = File(context.filesDir, DIRECTORY)

    fun name(startedAt: Date): String =
        "capture-${SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(startedAt)}.txt"
}
