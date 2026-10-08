package com.joegec.joycon2android.capture.presentation

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri

// ClipData carries the read grant through the chooser; EXTRA_STREAM alone doesn't on every version.
internal fun Context.shareCapture(uri: String, chooserTitle: String) {
    val file = Uri.parse(uri)
    val send = Intent(Intent.ACTION_SEND)
        .setType("text/plain")
        .putExtra(Intent.EXTRA_STREAM, file)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    send.clipData = ClipData.newRawUri(null, file)
    startActivity(Intent.createChooser(send, chooserTitle))
}
