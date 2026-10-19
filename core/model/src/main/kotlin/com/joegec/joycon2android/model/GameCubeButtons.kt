package com.joegec.joycon2android.model

// The NSO GameCube controller has no − or stick clicks; GL/GR stand for its trigger clicks.
object GameCubeButtons {
    val present: Set<JoyconButton> = setOf(
        JoyconButton.A, JoyconButton.B, JoyconButton.X, JoyconButton.Y,
        JoyconButton.L, JoyconButton.R, JoyconButton.ZL, JoyconButton.ZR, JoyconButton.Plus,
        JoyconButton.Up, JoyconButton.Down, JoyconButton.Left, JoyconButton.Right,
        JoyconButton.Home, JoyconButton.Capture, JoyconButton.Chat, JoyconButton.GL, JoyconButton.GR,
    )

    /** Null where the controller marks the button as the Switch 2 controllers do. Its Z reports as ZR. */
    fun marking(button: JoyconButton): String? = if (button == JoyconButton.ZR) "Z" else null

    /** L/R pressed from a trigger's first travel and GL/GR on its click: docs/virtual-gamepad.md#analog-triggers */
    fun withTriggerPresses(input: JoyconInput): JoyconInput {
        val clicks = CLICKS.filterKeys { it.id in input.pressed }
        val pulled = setOfNotNull(
            JoyconButton.L.takeIf { input.triggerLeft > 0 || it in clicks },
            JoyconButton.R.takeIf { input.triggerRight > 0 || it in clicks },
        )
        val triggerIds = CLICKS.keys.mapTo(mutableSetOf()) { it.id }
        return input.copy(pressed = input.pressed - triggerIds + (pulled + clicks.values).map { it.id })
    }

    private val CLICKS = mapOf(JoyconButton.L to JoyconButton.GL, JoyconButton.R to JoyconButton.GR)
}
