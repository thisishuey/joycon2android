package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.model.JoyconButton

/**
 * Before a GameCube controller reported its trigger clicks as GL/GR, its L/R were the clicks. On the Wii
 * console they stay L/R, as its layouts bind them. docs/virtual-gamepad.md#analog-triggers
 */
internal fun Map<String, String>.withLegacyGameCubeClicks(console: Console, side: JoyconSide): Map<String, String> =
    if (side == JoyconSide.GAMECUBE && console in PAD_CONSOLES) mapValues { (_, value) -> value.withClickSources() }
    else this

private fun String.withClickSources(): String = sourceIdOf(sourceIdsOf(this).map { CLICKS[it] ?: it })

private val PAD_CONSOLES = setOf(Console.GAMECUBE, Console.SWITCH_PRO)

private val CLICKS = mapOf(
    JoyconButton.L.name to JoyconButton.GL.name,
    JoyconButton.R.name to JoyconButton.GR.name,
)
