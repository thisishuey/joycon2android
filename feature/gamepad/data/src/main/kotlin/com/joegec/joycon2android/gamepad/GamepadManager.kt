package com.joegec.joycon2android.gamepad
import com.joegec.joycon2android.gamepad.privileged.PrivilegedShell

import android.content.Context
import android.util.Log
import com.joegec.joycon2android.model.AndroidKey
import com.joegec.joycon2android.model.AndroidKeyBindings
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.PlayerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GamepadManager(
    private val scope: CoroutineScope,
    private val context: Context,
    private val androidKeys: StateFlow<AndroidKeyBindings>,
) {

    private val devices = mutableMapOf<PlayerNumber, UhidRelay>()
    private val reportJobs = mutableMapOf<PlayerNumber, Job>()
    private var shell: PrivilegedShell? = null

    val activeCount: Int get() = devices.size

    suspend fun createGamepad(player: PlayerNumber, shell: PrivilegedShell): Boolean = withContext(Dispatchers.IO) {
        this@GamepadManager.shell = shell
        if (player in devices) return@withContext true

        val device = UhidRelay("Joy-Con Virtual Gamepad", player.index)
        val success = device.create(context, shell)

        if (success) {
            devices[player] = device
            Log.i(TAG, "Created virtual gamepad for ${player.name}")
        } else {
            Log.e(TAG, "Failed to create virtual gamepad for ${player.name}")
        }
        success
    }

    fun startReporting(player: PlayerNumber, stateFlow: StateFlow<PlayerState>) {
        reportJobs[player]?.cancel()
        val device = devices[player] ?: return
        reportJobs[player] = scope.launch(Dispatchers.Default) {
            // EXPERIMENT (test build only): short press = input keyevent 120, held >1s = input keycombination 26 25.
            var screenshotDownAt: Long? = null
            combine(stateFlow, androidKeys) { state, keys -> state to keys }.collect { (state, keys) ->
                device.sendReport(ReportMapper.buildReport(state, keys))
                val down = AndroidKey.SCREENSHOT in keys.pressedKeys(state.gamepad.pressed)
                val now = System.currentTimeMillis()
                val since = screenshotDownAt
                if (down && since == null) screenshotDownAt = now
                if (!down && since != null) {
                    screenshotDownAt = null
                    val command = if (now - since < 1000) "input keyevent 120" else "input keycombination 26 25"
                    Log.i(TAG, "Screenshot experiment: $command")
                    scope.launch(Dispatchers.IO) { shell?.shell(command)?.waitFor() }
                }
            }
        }
    }

    fun destroyGamepad(player: PlayerNumber) {
        reportJobs.remove(player)?.cancel()
        // Teardown writes a shutdown packet; over ADB that's a TLS socket, so off-main
        val device = devices.remove(player) ?: return
        scope.launch(Dispatchers.IO) { device.destroy() }
        Log.i(TAG, "Destroyed virtual gamepad for ${player.name}")
    }

    fun destroyAll() {
        reportJobs.values.forEach { it.cancel() }
        reportJobs.clear()
        val toDestroy = devices.values.toList()
        devices.clear()
        if (toDestroy.isNotEmpty()) scope.launch(Dispatchers.IO) { toDestroy.forEach { it.destroy() } }
        Log.i(TAG, "Destroyed all virtual gamepads")
    }

    companion object {
        private const val TAG = "GamepadManager"
    }
}
