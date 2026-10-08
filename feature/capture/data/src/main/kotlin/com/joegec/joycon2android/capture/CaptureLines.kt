package com.joegec.joycon2android.capture

import com.joegec.joycon2android.model.ControllerTraffic
import java.util.Locale

/** Not thread-safe: the recorder serialises calls. Line layout: docs/capture.md#format */
class CaptureLines(private val startNanos: Long) {

    private val filters = mutableMapOf<String, PacketChangeFilter>()
    private val rates = mutableMapOf<String, PacketRateCounter>()

    fun step(step: CaptureStep, nanos: Long): String = line(nanos, step, NO_ADDRESS, "step ${step.number}/${CaptureStep.count}")

    fun spiRead(address: String, spiAddress: Int, length: Int, step: CaptureStep, nanos: Long): String =
        line(nanos, step, address, "spi-read 0x%06X len=%d".format(spiAddress, length))

    fun traffic(traffic: ControllerTraffic, step: CaptureStep, nanos: Long): List<String> {
        val address = traffic.address
        return when (traffic) {
            is ControllerTraffic.Discovered -> listOf(
                line(
                    nanos, step, address,
                    "discovered side=${traffic.side} name=\"${traffic.name}\" " +
                        "mfg=${RawPacketFormat.hex(traffic.manufacturerData)}",
                ),
            )
            is ControllerTraffic.Connected -> listOf(line(nanos, step, address, "connected mtu=${traffic.mtu}"))
            is ControllerTraffic.Ready -> listOf(line(nanos, step, address, "ready"))
            is ControllerTraffic.Disconnected -> listOf(line(nanos, step, address, "disconnected status=${traffic.gattStatus}"))
            is ControllerTraffic.Reply -> listOf(line(nanos, step, address, "reply ${RawPacketFormat.hex(traffic.packet)}"))
            is ControllerTraffic.Input -> input(traffic, step, nanos)
        }
    }

    private fun input(input: ControllerTraffic.Input, step: CaptureStep, nanos: Long): List<String> {
        val address = input.address
        val rate = rates.getOrPut(address) { PacketRateCounter() }.count(nanos)
        val changed = filters.getOrPut(address) { PacketChangeFilter() }.isNewInput(input.packet)
        return listOfNotNull(
            rate?.let { line(nanos, step, address, "rate $it/s") },
            if (changed) line(nanos, step, address, "input ${RawPacketFormat.describe(input.packet, input.decoded)}") else null,
        )
    }

    private fun line(nanos: Long, step: CaptureStep, address: String, text: String): String =
        String.format(Locale.US, "%.3f %s %s %s", (nanos - startNanos) / NANOS_PER_SECOND, step.name, address, text)

    private companion object {
        const val NANOS_PER_SECOND = 1e9
        const val NO_ADDRESS = "-"
    }
}
