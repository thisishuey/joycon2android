package com.joegec.joycon2android.model

// The NSO GameCube controller has no −, stick clicks or back paddles.
object GameCubeButtons {
    val present: Set<JoyconButton> = setOf(
        JoyconButton.A, JoyconButton.B, JoyconButton.X, JoyconButton.Y,
        JoyconButton.L, JoyconButton.R, JoyconButton.ZL, JoyconButton.ZR, JoyconButton.Plus,
        JoyconButton.Up, JoyconButton.Down, JoyconButton.Left, JoyconButton.Right,
        JoyconButton.Home, JoyconButton.Capture, JoyconButton.Chat,
    )

    /** Null where the controller marks the button as the Switch 2 controllers do. Its Z reports as ZR. */
    fun marking(button: JoyconButton): String? = if (button == JoyconButton.ZR) "Z" else null

    /** A trigger counts as pressed from its first travel, as the pad's L2/R2 do: docs/virtual-gamepad.md#analog-triggers */
    fun triggersPulledBy(input: JoyconInput): Set<String> = setOfNotNull(
        JoyconButton.L.id.takeIf { input.triggerLeft > 0 || it in input.pressed },
        JoyconButton.R.id.takeIf { input.triggerRight > 0 || it in input.pressed },
    )

    /** The pad presses the back-button keys for its trigger clicks: docs/virtual-gamepad.md#analog-triggers */
    fun backButtonsClickedBy(pressed: Set<String>): Set<String> =
        CLICK_BACK_BUTTONS.filterKeys { it.id in pressed }.values.mapTo(mutableSetOf()) { it.id }

    private val CLICK_BACK_BUTTONS = mapOf(JoyconButton.L to JoyconButton.GL, JoyconButton.R to JoyconButton.GR)
}
