package com.joegec.joycon2android.capture

import android.content.Context
import androidx.core.content.FileProvider
import com.joegec.joycon2android.capture.data.R
import java.io.File

/** Its own subclass: the app's update provider already claims `FileProvider`'s manifest entry. */
class CaptureFileProvider : FileProvider(R.xml.capture_file_paths) {
    companion object {
        private const val AUTHORITY_SUFFIX = ".captures"

        fun uriFor(context: Context, file: File): String =
            getUriForFile(context, context.packageName + AUTHORITY_SUFFIX, file).toString()
    }
}
