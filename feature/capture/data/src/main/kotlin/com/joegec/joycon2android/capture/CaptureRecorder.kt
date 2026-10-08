package com.joegec.joycon2android.capture

import com.joegec.joycon2android.model.ControllerTraffic
import com.joegec.joycon2android.model.ControllerTrafficListener
import com.joegec.joycon2android.model.ControllerTrafficSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import java.io.File
import java.util.Date

/** Traffic arrives on BLE binder threads and the main thread; [lock] serialises it into one file. */
class CaptureRecorder(
    private val source: ControllerTrafficSource,
    private val directory: File,
    private val header: (Date) -> List<String>,
    private val shareableUri: (File) -> String,
    private val revealPreferences: CaptureRevealPreferences,
    private val clock: () -> Long = System::nanoTime,
) : CaptureRepository, ControllerTrafficListener {

    private val session = MutableStateFlow(CaptureStatus())
    override val status: Flow<CaptureStatus> =
        combine(revealPreferences.revealed, session) { revealed, status -> status.copy(revealed = revealed) }

    private val lock = Any()
    private var recording: CaptureRecording? = null

    override suspend fun reveal() = revealPreferences.reveal()

    override fun start() {
        synchronized(lock) {
            if (recording != null) return
            val startedAt = Date()
            directory.mkdirs()
            val file = File(directory, CaptureFiles.name(startedAt))
            val now = clock()
            recording = CaptureRecording(file, CaptureLines(now)).apply {
                write(header(startedAt))
                enter(CaptureStep.first, now)
            }
            session.value = session.value.copy(step = CaptureStep.first, controllers = 0)
        }
        source.setTrafficListener(this)
    }

    override fun nextStep() {
        val next = synchronized(lock) {
            val current = recording ?: return
            current.step.next?.also { current.enter(it, clock()) }
        }
        if (next == null) stop() else session.value = session.value.copy(step = next)
    }

    override fun stop() {
        source.setTrafficListener(null)
        synchronized(lock) {
            val finished = recording ?: return
            recording = null
            finished.close()
            session.value = CaptureStatus(lastCapture = shareableUri(finished.file))
        }
    }

    override fun onTraffic(traffic: ControllerTraffic) {
        synchronized(lock) {
            val current = recording ?: return
            val now = clock()
            current.write(current.lines.traffic(traffic, current.step, now))
            when (traffic) {
                is ControllerTraffic.Ready -> readCalibration(current, traffic.address, now)
                is ControllerTraffic.Discovered -> countController(current, traffic.address)
                else -> Unit
            }
        }
    }

    private fun readCalibration(current: CaptureRecording, address: String, now: Long) {
        current.write(listOf(current.lines.spiRead(address, CALIBRATION_ADDRESS, CALIBRATION_LENGTH, current.step, now)))
        source.readSpi(address, CALIBRATION_ADDRESS, CALIBRATION_LENGTH)
    }

    private fun countController(current: CaptureRecording, address: String) {
        if (current.addresses.add(address)) {
            session.value = session.value.copy(controllers = current.addresses.size)
        }
    }

    private companion object {
        // Trigger zero points and the unidentified block after them: docs/capture.md#calibration-read
        const val CALIBRATION_ADDRESS = 0x013140
        const val CALIBRATION_LENGTH = 0x40
    }
}
