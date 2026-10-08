package com.joegec.joycon2android.capture

import com.joegec.joycon2android.model.ControllerTraffic
import com.joegec.joycon2android.model.ControllerTrafficListener
import com.joegec.joycon2android.model.ControllerTrafficSource
import com.joegec.joycon2android.model.Side
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class CaptureRecorderTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val source = FakeSource()
    private val reveal = FakeReveal()
    private val recorder by lazy {
        CaptureRecorder(
            source = source,
            directory = folder.root,
            header = { listOf("# header") },
            shareableUri = { "content://captures/${it.name}" },
            revealPreferences = reveal,
            clock = { 0L },
        )
    }

    @Test
    fun `starting attaches to the controllers and stopping detaches`() {
        recorder.start()
        assertSame(recorder, source.listener)
        recorder.stop()
        assertNull(source.listener)
    }

    @Test
    fun `a ready controller gets its calibration block read`() {
        recorder.start()
        recorder.onTraffic(ControllerTraffic.Ready(ADDRESS))
        assertEquals(listOf(Triple(ADDRESS, 0x013140, 0x40)), source.reads)
    }

    @Test
    fun `the file holds the header, each step and the traffic`() {
        recorder.start()
        recorder.onTraffic(ControllerTraffic.Ready(ADDRESS))
        recorder.nextStep()
        recorder.stop()
        val text = folder.root.listFiles()!!.single().readText()
        assertTrue(text.startsWith("# header\n"))
        assertTrue(text.contains("REST - step 1/"))
        assertTrue(text.contains("REST $ADDRESS ready"))
        assertTrue(text.contains("FACE_BUTTONS - step 2/"))
    }

    @Test
    fun `next on the last step finishes the capture and offers it to share`() = runBlocking {
        recorder.start()
        repeat(CaptureStep.count) { recorder.nextStep() }
        val status = recorder.status.first()
        assertTrue(!status.recording)
        assertTrue(status.lastCapture!!.startsWith("content://captures/capture-"))
    }

    @Test
    fun `each discovered controller is counted once`() = runBlocking {
        recorder.start()
        repeat(2) { recorder.onTraffic(ControllerTraffic.Discovered(ADDRESS, ByteArray(0), Side.PRO, "Joy-Con 2")) }
        assertEquals(1, recorder.status.first().controllers)
    }

    @Test
    fun `traffic outside a recording is ignored`() {
        recorder.onTraffic(ControllerTraffic.Ready(ADDRESS))
        assertTrue(source.reads.isEmpty())
        assertTrue(folder.root.listFiles()!!.isEmpty())
    }

    @Test
    fun `the status shows whether the tool was revealed`() = runBlocking {
        reveal.revealed.value = true
        assertTrue(recorder.status.first().revealed)
    }

    private class FakeSource : ControllerTrafficSource {
        var listener: ControllerTrafficListener? = null
        val reads = mutableListOf<Triple<String, Int, Int>>()

        override fun setTrafficListener(listener: ControllerTrafficListener?) {
            this.listener = listener
        }

        override fun readSpi(address: String, spiAddress: Int, length: Int) {
            reads += Triple(address, spiAddress, length)
        }
    }

    private class FakeReveal : CaptureRevealPreferences {
        override val revealed = MutableStateFlow(false)
        override suspend fun reveal() {
            revealed.value = true
        }
    }

    private companion object {
        const val ADDRESS = "AA:BB:CC:DD:EE:FF"
    }
}
