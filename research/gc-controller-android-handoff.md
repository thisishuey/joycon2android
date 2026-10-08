# NSO GameCube Controller on Android via a Joycon2Android fork — handoff report

Prepared 2026-10-07 for a Claude Code session in a fork of
[JoeGeC/joycon2android](https://github.com/JoeGeC/joycon2android). Companion file:
`gamecube-controller.patch` (apply with `git am`).

## Goal

Use four Switch 2 NSO GameCube controllers (VID `0x057E`, PID `0x2073`) on an Android device as
players 1–4 in Dolphin, with the C-stick and analog L/R working, the way they would on a GameCube.

## Status in one paragraph

Android has no native support: the controllers use Nintendo's own BLE pairing and protocol, not
standard Bluetooth HID, and over USB-C they stay silent until a vendor init command Android never
sends. SDL supports Switch 2 controllers only wired and only when built with libusb (not Android);
Dolphin is waiting on SDL; the Linux `hid-nintendo` Switch 2 driver (v13, Aug 2026) is USB-only and
unmerged. Joycon2Android is the only working Android route: it speaks the BLE protocol itself and
exposes a virtual gamepad per player through Shizuku (no root). Upstream it doesn't recognise the
GameCube controller, which causes every reported bug (below).

## Root cause in the codebase

The scanner identifies controller type by the product ID's low byte in the advertisement
(`feature/connection/data/.../BleScanner.kt`, `sideFromManufacturerData`): `0x67` left Joy-Con,
`0x66` right, `0x69` Pro. `0x73` (GameCube) falls through to `Side.UNKNOWN`. Consequences:

| Reported bug (issue #27) | Cause |
|---|---|
| Main stick and D-pad rotated 90° | A lone unknown controller is treated as a sideways left Joy-Con: `GamepadState.from` → `SidewaysMapper.rotateStickLeft` / `remapButtonsLeft` |
| C-stick never registers | `PacketParser.parse` decodes the right stick (`0x0D`) only when `side == Side.PRO` |
| L+R doesn't assign it | `ComboAssignmentDetector` only treats L+R as a Pro combo for `Side.PRO` |
| No analog triggers | Trigger bytes `0x3C`/`0x3D` are never parsed; virtual gamepad trigger axes (report bytes 11/12) are driven digitally by ZL/ZR; Dolphin setup writes only `Triggers/L` and `Triggers/R`, never `L-Analog`/`R-Analog` |

Buttons already decode correctly through the shared bitmap at `0x03` (user report: face buttons,
digital L/R, ZL/ZR, Home, Chat all work), so the report format the app requests carries the
GameCube controller's inputs in the standard Switch 2 layout.

## Work already done (branch `gamecube-controller`, commit `b34bc15`)

**Not compiled or run.** The sandbox it was written in couldn't download the Android SDK. CI on the
fork is the first build. Expect possible small compile fixes.

| Change | Files |
|---|---|
| `0x73` → `Side.PRO` in the scanner. Fixes rotation, C-stick and L+R assignment | `feature/connection/data/.../BleScanner.kt` |
| `triggerLeft` / `triggerRight` (uint8, default 0) added to `JoyconInput`, parsed from `0x3C`/`0x3D`; 0 if the packet is too short | `core/model/.../JoyconInput.kt`, `feature/connection/data/.../PacketParser.kt` |
| Opt-in raw capture: logs packets whose input bytes changed (buttons `0x03..0x09`, sticks beyond ±24 LSB, triggers beyond ±3) under tag `J2Raw`, off until `setprop` | `PacketChangeFilter.kt` (new), `RawPacketFormat.kt` (new), `JoyconConnection.kt` |
| Unit tests | `PacketParserTest` (2 new), `PacketChangeFilterTest`, `RawPacketFormatTest` |
| Docs | `docs/protocol.md` (PID, trigger notes, "Capturing raw packets"), `README.md` (experimental GameCube line) |

Capture line format: `[side name] btn=… L=x,y R=x,y trig=l,r pressed=… len=… raw=<hex>` — decoded
values are pre-calibration.

## Requirements from issue #27

[JoeGeC/joycon2android#27](https://github.com/JoeGeC/joycon2android/issues/27), "Ability to manually
assign NSO Gamecube Controller as Pro Controller via touch menu" (open, `enhancement`, opened
2026-09-27 by HotTownJohnny, no maintainer reply or linked PR as of 2026-10-07). Every item is from
the original post. Treat this table as the acceptance checklist; reference the issue in commits and
any upstream PR.

| # | Item | Type | Status |
|---|---|---|---|
| 1 | L+R on the assignment screen doesn't make it a Pro Controller | bug | Fixed by the patch (`0x73` → `Side.PRO`); verify on hardware |
| 2 | Assigned, it acts as a left or right Joy-Con depending on the first button pressed | bug | Fixed by the patch: `SideInference` only runs for `Side.UNKNOWN`; verify |
| 3 | D-pad and left stick rotated 90° counter-clockwise | bug | Fixed by the patch (no sideways remap for Pro); verify |
| 4 | No right stick, so the C-stick doesn't work in apps or emulators | bug | Fixed by the patch (right stick decoded for Pro); verify |
| 5 | A/B/X/Y also rotated 90°, which happened to resemble the GameCube's physical face layout; reporter cautions a special layout for one controller may cause problems | observation | Rotation goes away with the patch. Confirm A/B/X/Y now map by name in Dolphin's GameCube preset (`GameCubeMapping.kt`), and decide whether a GameCube-specific preset is wanted |
| 6 | **Main request:** manually set a controller's type to Pro Controller from the touch UI | feature | **Not done.** The patch makes it automatic for `0x2073`, but a manual override still helps any controller whose advertisement isn't recognised (future PIDs, missing manufacturer data). Likely home: the assignment panel (`feature/assignment/presentation/.../AssignmentPanel.kt`), overriding the connection's `Side` |
| 7 | Analog triggers (reporter: fine if out of scope) | feature | Not done; plan below |

## Remaining work: analog triggers with digital click

Make GameCube L/R behave like a real pad: analog travel on an axis, click at the bottom on a button.
Gate it to controllers with analog triggers so Joy-Con and Pro behaviour is unchanged.

1. **Carry triggers to the gamepad.** `JoyconInput` → `PlayerState` → `GamepadState`
   (`core/model/.../GamepadState.kt` currently has only `pressed` and four stick axes).
2. **Calibrate.** Reference values: rest ~`0x22` (34), full ~`0xEA`–`0xF0` (234–240). Rescale to
   0..255 with rest → 0, by sampling rest like `StickCalibrator` does for sticks, else Dolphin sees a
   partly held trigger.
3. **Drive the HID trigger axes from analog L/R.** `feature/gamepad/data/.../ReportMapper.kt` bytes
   11/12 are now `ZL`/`ZR ? 255 : 0`. For an analog-trigger controller use the calibrated L/R values.
   ZL and Z keep their button bits (8, 9), so nothing is lost. Android exposes these as
   `AXIS_BRAKE` (left) / `AXIS_GAS` (right), aliased to `AXIS_LTRIGGER`/`AXIS_RTRIGGER`
   (`docs/virtual-gamepad.md`).
4. **Dolphin bindings.** `feature/gamepad/domain/.../emulator/DolphinGcpadConfig.kt` writes
   `Triggers/L` and `Triggers/R` from buttons only. Add `Triggers/L-Analog` and `Triggers/R-Analog`
   bound to the trigger axes, keep the digital ones on the L/R button bits (`Button L1`/`Button R1`).
5. **Identify the controller as GameCube**, not just Pro, wherever behaviour must differ (trigger
   source, maybe the UI layout and a GameCube mapping preset). Options: a new `Side`/model enum
   value (ripples through exhaustive `when`s in assignment, mapping and presentation code), or a
   flag on the connection from the advertised PID. Decide after reading `docs/architecture.md`.
6. Update `docs/virtual-gamepad.md`, `docs/protocol.md` and the README in the same commit (the
   repo's `CLAUDE.md` requires it).

## Verification procedure (needs hardware)

1. Build: fork's **Actions** → enable workflows → run **Prerelease** on the branch → release with a
   plain APK URL. Without a `DEBUG_KEYSTORE_BASE64` secret each build gets a throwaway key and must
   be uninstalled before the next install; adding a fixed debug keystore as that secret avoids it.
2. Uninstall the official Joycon2Android (signature mismatch), install the APK, start Shizuku via
   Wireless debugging.
3. Hold SYNC, tap Scan. Expect: listed as a Pro Controller, L+R assigns it, both sticks move
   un-rotated.
4. Capture over adb (`adb pair` / `adb connect` from the Wireless debugging screen):
   ```
   adb shell setprop log.tag.J2Raw DEBUG
   adb logcat -s J2Raw > gc-capture.txt
   ```
   Press each control once, slowly: every button, each stick in four directions, each trigger at
   half then full travel. Also confirm the advertisement log line (`Joycon2` tag, `Adv … mfg=…`)
   shows `73 20` at bytes 5–6.
5. With four controllers: connect and assign all four, run Dolphin **Set up**, check for dropped
   links or lag.

## Open questions for research

- **Trigger bytes in this report format.** The reference layout below puts triggers at `0x0C`/`0x0D`
  in "format 3" (handle `0x000E`); this app reads a different notify characteristic (`…fd2`) after
  `0C 91 01 02` / `0C 91 01 04` init commands, whose layout (`docs/protocol.md`) lists triggers at
  `0x3C`/`0x3D`. Which holds for the GameCube controller here is unconfirmed — the capture decides.
- **Init commands.** The reference guide warns against sending cmd `0x0C` to this controller, yet
  the app sends two and the controller works. Any side effect (report rate, missing fields)?
- **Exact button bits** for Z, the new ZL, Start and Capture on this controller in the app's
  bitmap. User report implies Z → ZR, Start → Plus; Capture unconfirmed.
- **Dolphin Android binding names** for the brake/gas axes in `GCPadNew.ini` (the syntax for an
  analog axis bound to `Triggers/L-Analog`), and Dolphin's trigger threshold/dead-zone defaults.
- **Rumble.** The controller has an eccentric-rotating-mass motor (per the kernel patch). Does the
  app or its virtual gamepad pass rumble at all; what's the BLE rumble command for this controller?
- **Android BLE connection limit** for four controllers at high connection priority; per-chipset
  behaviour and whether the app's "faster updates" setting helps or hurts at four.
- **Reconnection.** The app needs SYNC each session (Android connects from a resolvable private
  address). Any workaround specific to this controller?
- **Manual type override (issue #27 item 6).** How a per-controller `Side` override fits the
  architecture: where it's stored (per MAC address?), whether it survives reconnects, and how the
  assignment flow and `PlayerStateResolver` react when the side changes on a connected controller.
- **Upstream.** Whether to send this as a PR to JoeGeC/joycon2android, referencing issue #27.

## Reference: GameCube controller input report ("format 3")

From RyanCopley/NSO-GameCube-Controller-Pairing-App's `NSO_GC_BLE_PROTOCOL.md` (sources: BlueRetro,
ndeadly's switch2_input_viewer). A different report format from the one this app reads; use it for
cross-checking, not as ground truth for `…fd2` packets.

| Offset | Field |
|---|---|
| `0x00` | counter |
| `0x02`–`0x04` | buttons (below) |
| `0x05`–`0x07` | left stick, 12-bit X/Y packed |
| `0x08`–`0x0A` | C-stick, 12-bit X/Y packed |
| `0x0C` / `0x0D` | left / right trigger, analog |
| `0x0F`–`0x36` | IMU |

Buttons — `0x02`: B 01, A 02, Y 04, X 08, R 10, Z 20, Start 40. `0x03`: Down 01, Right 02, Left 04,
Up 08, L 10, ZL 20. `0x04`: Home 01, Capture 02, GR 04, GL 08, Chat 10. No stick clicks. The
digital L/R bits set at the end of trigger travel.

Other protocol notes from the same guide: SMP legacy "Just Works" pairing with key distribution
init `0x02` / resp `0x01`; MTU ≥ 185 or notifications are silently dropped; SPI `0x013140` holds
trigger calibration (64 bytes) — possibly a better source for rest/full values than sampling.

## Alternatives if this stalls

- Original wired GameCube controllers + a Wii U/Switch-style GameCube adapter over USB-C OTG: Dolphin
  for Android supports it directly, with analog triggers and C-stick.
- Joypad OS firmware on a Pico W (`bt2usb_pico_w`) lists NSO GameCube support and could present the
  controller as a standard USB gamepad; untested on Android.

## Sources

- Joycon2Android: https://github.com/JoeGeC/joycon2android — issue #27:
  https://github.com/JoeGeC/joycon2android/issues/27
- GameCube BLE protocol guide: https://github.com/RyanCopley/NSO-GameCube-Controller-Pairing-App
  (`NSO_GC_BLE_PROTOCOL.md`)
- `hid-nintendo` Switch 2 series v13: https://ratatoskr.run/linux-input/2026/08/17399481/t
- Linux 7.3 HID merge: https://www.phoronix.com/news/Linux-7.3-HID
- SDL 3.4 release: https://gamefromscratch.com/sdl-3-4-released/
- SDL issue #15245: https://github.com/libsdl-org/SDL/issues/15245
- Dolphin progress report 2509: https://dolphin-emu.org/blog/2025/09/16/dolphin-progress-report-release-2509/
- Dolphin SDL 3.4.16 update: https://github.com/dolphin-emu/dolphin/pull/14855
- Dolphin GameCube adapter guide: https://dolphin-emu.org/docs/guides/how-use-official-gc-controller-adapter-wii-u/
- Nintendo FAQ: https://en-americas-support.nintendo.com/app/answers/detail/a_id/68609/
- Joypad OS: https://github.com/joypad-ai/joypad-os
