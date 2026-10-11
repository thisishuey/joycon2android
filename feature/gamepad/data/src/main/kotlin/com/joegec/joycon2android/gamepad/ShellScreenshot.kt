package com.joegec.joycon2android.gamepad

import com.joegec.joycon2android.gamepad.privileged.PrivilegedShell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Why the shell and not the virtual pad: docs/virtual-gamepad.md#android-keys */
class ShellScreenshot(private val scope: CoroutineScope) {

    @Volatile
    private var shell: PrivilegedShell? = null

    fun use(shell: PrivilegedShell) {
        this.shell = shell
    }

    fun take() {
        val active = shell ?: return
        scope.launch(Dispatchers.IO) { active.shell(COMMAND)?.waitFor() }
    }

    private companion object {
        const val COMMAND = "input keyevent 120" // KEYCODE_SYSRQ
    }
}
