package com.joegec.joycon2android.capture

import java.io.File

internal class CaptureRecording(val file: File, val lines: CaptureLines) {

    private val writer = file.bufferedWriter()
    val addresses = mutableSetOf<String>()

    var step: CaptureStep = CaptureStep.first
        private set

    // Flushed per step, so a capture cut short still holds every finished step.
    fun enter(step: CaptureStep, nanos: Long) {
        this.step = step
        write(listOf(lines.step(step, nanos)))
        writer.flush()
    }

    fun write(lines: List<String>) {
        lines.forEach { writer.write(it); writer.write("\n") }
    }

    fun close() = writer.close()
}
