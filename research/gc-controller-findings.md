# NSO GameCube controller — research findings

Answers to the open questions in [gc-controller-android-handoff.md](gc-controller-android-handoff.md),
researched 2026-10-08 against this repo at `6bd7eb5` (identical to upstream `JoeGeC/joycon2android`
main) and the external sources cited inline. Input for the implementation session; no code was
changed. [gamecube-controller.patch](gamecube-controller.patch) still applies cleanly to `6bd7eb5`
(`git apply --check`).

**Labels.** **Confirmed** = code or source that runs against this report format, read at the cited
line. **Inferred** = a different report format, transport or controller, README-only, or reasoning.
**HW** = only a hardware capture can settle it.

Offsets are this app's: the 63-byte `…fd2` packet starts with the uint24 counter at `0x00`, buttons
are a uint32 LE at `0x03`. Converting other projects' offsets:

| Source | Their offset | App offset |
|---|---|---|
| BlueRetro, NS2-Connect, RyanCopley Bumble path (uint32 buttons at byte 4) | bit *n* | mask `1 << (n + 8)` |
| SDL `SDL_hidapi_switch2.c` (USB, report-ID byte first) | byte *k* | `k − 1` |
| Linux `hid-nintendo` Switch 2, RyanCopley "format 3" | different report | — |

## Issue #27 re-check

Re-read 2026-10-08: still open, `enhancement`, no comments, no linked branches or PRs, no
timeline events since it was opened (2026-09-27). The handoff's acceptance table stands unchanged.

## Hardware capture, 2026-10-07

One NSO GameCube controller on a Retroid Pocket Nova (Android 13), recorded with the capture tool
from the `controller-capture` branch through all 16 steps:
[captures/gc-single-20261007.txt](captures/gc-single-20261007.txt). The "every controller" step had
only this one connected, so multi-controller behaviour is still untested.

| Question | Result |
|---|---|
| Product ID | `mfg=01 00 03 7E 05 73 20 …` → `0x2073`. Unpatched app: `side=UNKNOWN`, name falls back to "Joy-Con 2" (no local name) |
| Init commands `0C … FF` (Q5) | Harmless: connects, reaches ready, streams 63-byte packets throughout |
| Trigger bytes (Q1) | **`0x3C` = L, `0x3D` = R**, confirmed |
| Button bits (Q2) | **Exactly the Q2 table.** Bits seen, all steps: `0xCF72CF00`. Z = ZR `0x8000`, Start = + `0x20000`, Capture `0x200000`, C `0x400000`, L `0x40000000`, R `0x4000`, ZL `0x80000000`. Paddle byte `0x07` and bytes `0x08–0x09` always `00` |
| IMU | Present, Joy-Con scale and offsets: accel at rest `(624, 3272, 2467)`, magnitude 4145 (4096 = 1 g) |
| SPI `0x013140` (Q4) | Reply data `21 20 FF…`: **L zero 33, R zero 32**. `0x013142..0x01317F` (including `0x013160`) is all `FF` |
| Report rate | 28–33 packets/s with the gamepad off (balanced priority, 30 ms) |

Triggers (raw `0x3C`/`0x3D`):

| | Left | Right |
|---|---|---|
| Flash zero | 33 | 32 |
| Rest | 35–37 | 25–31 |
| Slow sweep max | 196 | 185 |
| Held at first stop | 191–195 | 182 |
| Second stop (click bit set) | 233–238 (up to 241 in play) | 226–230 (up to 234) |

- **Analog travel ends at the first stop.** Pushing through to the second stop jumps 200 → 233 within
  one packet, and that is where the L/R bit sets.
- **The click bit and the analog value are not in lockstep.** In quick presses the bit can show with
  the analog value as low as 155 (releasing), and values up to 220 can pass without it (pressing).
  The click must come from the bit, never from an analog threshold.
- **The first stop differs per trigger by ~13 counts** (L 195, R 182) on this one unit.

Sticks (raw 12-bit):

| | Rest | X travel | Y travel | Smallest half-span |
|---|---|---|---|---|
| Main stick (`0x0A`) | 2073, 2060 | 835..3297 | 799..3277 | 1217 (up) |
| C-stick (`0x0D`) | 1957, 2080 | 861..3105 | 876..3133 | **1053 (up)**, 1096 (left) |

**C-stick vs `StickCalibrator`:** its spans are seeded at 1150 and only widen, so on this unit the
C-stick tops out at ~92% up and ~95% left. Seed the C-stick lower (≈1000), or read the factory stick
calibration (`0x0130E8`, Q4). The three-controller capture below confirms it on every unit.

### Three controllers, 2026-10-07

All three of the user's controllers connected at once on the same Nova, through all 16 steps on
each: [captures/gc-three-20261007.txt](captures/gc-three-20261007.txt). Balanced priority (gamepad
off).

| | `…5E:6A:A6` | `…5E:C7:8D` | `…60:07:95` |
|---|---|---|---|
| Flash zero L / R (`0x013140`) | 33 / 32 | 35 / 32 | 31 / 31 |
| Rest L / R | 35 / 30 | 36 / 31–32 | 32 / 33–34 |
| First stop L (sweep max) | 192–196 | 188 | 185 |
| First stop R (sweep max) | 182–186 | 178 | 170–179 |
| Click range seen (bit set, pressing) | 227–240 | 221–242 | 209–236 |
| Main stick smallest half-span | 1181 | 1149 | 1233 |
| C-stick smallest half-span | **1022** (up) | **1093** (up) | **1074** (up) |
| Rate, all three streaming | 21–34, mean 32.0 | 24–34, mean 30.9 | 26–34, mean 32.3 |

- **Every unit: same bits (`0xCF72CF00` over the guide), same layout, no stray bytes.**
- **Trigger zero:** rest sits within −2..+3 of the flash zero, so flash zero plus a ~5-count dead
  zone covers all three.
- **Trigger full:** the first stop ranges 170–196 across units and sides, so a fixed saturation point
  must be ≤ 170 to reach full on every trigger, or learn it per trigger. The click needs the bit.
- **C-stick up travel is 1022–1093 on every unit**, below the 1150 seed: lower the C-stick seed.
- **Three links at balanced priority held up:** ~31–32 packets/s each, no disconnects, worst second
  21/s. High priority with three is still untested.
- **Address stability:** `3C:A9:AB:5E:6A:A6` matches the first capture, 26 minutes earlier. All three
  share the `3C:A9:AB` prefix and its top two bits are `00`. A non-resolvable private address would
  be random in every byte, so these look like public, vendor-assigned addresses, which don't change.
  Keying a type override by address (Q7) should hold.

## Corrections to the handoff report and patch

Things the handoff missed or got wrong. The first two block the goal even if every protocol answer
below holds.

| # | Finding | Evidence | Consequence |
|---|---|---|---|
| 1 | **Dolphin Set up skips Pro Controller players.** `DolphinGcpadConfig.bodyFor` returns null for `player.hasPro`, and `mergeCore` filters them out of `SIDevice` | `DolphinGcpadConfig.kt:80`, `:107`; test `pro controllers are skipped` (`DolphinGcpadConfigTest.kt:104`). There since the initial import (`8463924`), no reason given in code, docs or commit message | With the patch, all four GameCube controllers become `Side.PRO`, and Set up writes **no `[GCPad1–4]` section and no `SIDevice`**. Remove the skip (Pro → `JoyconSide.DUAL`, as `EdenGamepadConfig` and `PlayerBody.joyconSide()` already do) and invert the test |
| 2 | **The GameCube preset binds `Z → R`**, alongside `TriggerR → R` | `GameCubeMapping.kt:42`, `:45` (`DUAL` branch). Same since `c533bc1` | On a GameCube controller Z sets the ZR bit (Q2), which nothing binds, so **Z does nothing in Dolphin** and digital R fires R and Z together. Needs a GameCube-controller layout: `Z → ZR`, `Start → Plus`, `TriggerL → L`, `TriggerR → R`, C-stick → right stick |
| 3 | The trigger axis numbers are `AXIS_GAS = 22` (right) and `AXIS_BRAKE = 23` (left), not BRAKE first | AOSP `include/android/input.h` | Dolphin tokens are `Axis 23+` (L) and `Axis 22+` (R); see Q3 |
| 4 | `docs/virtual-gamepad.md:62-63` ("Android aliases `AXIS_LTRIGGER` to `AXIS_BRAKE`") is true only on Android 14+ | AOSP `Generic.kl` and `JoystickInputMapper::getCompatAxis`; see Q3 | Fix the doc when Q3 is implemented |
| 5 | The kernel series is at **v16** (patchwork, 2026-10-06, `20261006061619.2005967-2-vi@endrift.com`), not v13. Still USB-only | patchwork | Cite v16 |
| 6 | Trigger values: rest **~30–35**, full **~225–240** per unit; 232 is what the SDL and kernel drivers treat as full | Q1, Q4 | The patch's `protocol.md` row ("~0x22 rest, ~0xEA full, unconfirmed in this report format") can drop "unconfirmed"; see Q1 for what still needs HW |
| 7 | SPI `0x013140` holds **2 bytes** that anyone reads (L and R zero), not a 64-byte block | Q4 | Read 2 bytes |
| 8 | Changing a connected controller's type leaves its `StickCalibrator` with a wrong frozen right-stick centre | Q7 | Reset the calibrator on a type change |
| 9 | This environment reaches `dl.google.com` and `maven.google.com` (HTTP 200 on 2026-10-08), unlike the sandbox the patch was written in | `curl` | The implementation session can likely install the Android SDK and compile locally before relying on fork CI |

---

## Q1. Where the analog triggers are in the `…fd2` report

**Answer: `0x3C` = left, `0x3D` = right, uint8, as `docs/protocol.md` says.** Rest ~30–35, full
~225–240, varying per unit and per side. **Status: confirmed in code across four projects;
the exact combination of Android, this app's init and a GameCube unit is HW.**

| Source | What it shows | Status |
|---|---|---|
| Joycon2forMac `src/Joycon2BLEReceiver.mm` @65fd964 — the app's own reference | Subscribes `…7FD2` (L15), sends **the same two `0C 91 01 02/04 … FF` commands** (L434-436), then `parsed["TriggerL"] = data[0x3C]; parsed["TriggerR"] = data[0x3D];` (L582-583). README L19: "Trigger positions (Can be detected with the NGC controller)" | Confirmed for this app's exact setup; the GameCube evidence is that README line (no GC-specific code) |
| BlueRetro `main/adapter/wireless/sw2.c` @e1a9831 | `struct sw2_map { u8 tbd[4]; u32 buttons; u8 tbd1[2]; u8 axes[6]; u8 tbd2[44]; u8 triggers[2]; … }` (L74-82): triggers at byte 60 = `0x3C`, sticks at `0x0A`. GC trigger meta `{.neutral = 30, .abs_max = 195}` (L64-72); `sw2_gc_to_generic` subtracts neutral (L284-287). Reads handle `0x000A` (`hidp/sw2.h` L121) | Confirmed for the GC over BLE; BlueRetro sends no `0x0C` |
| Nohzockt/Switch2-Controllers `NS2-Connect.py` @7168e2b | `extract_gc_triggers`: `if len(data) >= 62: left = data[60]; right = data[61]` (L232-252); dead zone 35, threshold 209 (L40-43). First notify characteristic of service `…7fd0` | Confirmed for the GC over BLE; no `0x0C` |
| joycon2cpp (TheFrano) @2e410b7 `GenerateNSOGCReport` | Reads `buffer[0x3c]` | Confirmed for the GC over BLE, with `0x0C` mask `0x27` (Q5) |
| SDL `src/joystick/hidapi/SDL_hidapi_switch2.c` main@e4af6f8 | `HandleGameCubeState` reads `data[61]`/`data[62]` (L724-737), i.e. app `0x3C`/`0x3D`; left stick at `data[11]` = app `0x0A`. `MapTriggerAxis` full = `232.f` (L223-231) | Confirmed for the GC, but USB |
| RyanCopley NSO-GameCube-Controller-Pairing-App @962c333 | Bumble path on handle `0x000A` reads `ble_data[60]`/`[61]` (`sw2_protocol.py` L100-102). Its "format 3" (handle `0x000E`, triggers `0x0C`/`0x0D`) is a different characteristic. Its doc says reports arrive on `0x000E` while its own code reads `0x000A` | Confirms the handoff's "format 3" table is the other report, not ours |

`…fd2` = handle `0x000A` is inferred from matching layouts and from NS2-Connect taking the first
notify characteristic, not from a published handle-to-UUID table. Joycon2forMac reading `0x3C` on
`…fd2` makes it moot for our purposes.

**Pro Controller 2 has no analog triggers.** BlueRetro `sw2_pro_to_generic` (L212-247) never reads
`triggers`, SDL `HandleSwitchProState` has no trigger axis, and Linux maps Pro ZL/ZR to digital
`BTN_TL2`/`BTN_TR2`. Inferred; HW to confirm `0x3C`/`0x3D` read 0 or constant on a Pro. Gate the
analog path on the controller model, not on `Side.PRO`.

**HW capture settles:** each trigger's value at rest, across a slow sweep, at the first stop and at the second stop
(Q4); at which stop the L/R digital bits set; that a Pro Controller's `0x3C`/`0x3D` stay flat.

## Q2. Button bits

**Answer: the GameCube controller reuses the Pro bitmap; there is no GameCube-only bit.** Z = ZR's
bit, Start = Plus's bit. **Status: confirmed (BlueRetro and NS2-Connect over BLE, SDL over USB); HW
to confirm no stray bits.**

| GC button | App bit | Mask (uint32 at `0x03`) |
|---|---|---|
| A / B / X / Y | same names | `0x800` / `0x400` / `0x200` / `0x100` |
| **Z** | **ZR** | `0x00008000` |
| ZL (new) | ZL | `0x80000000` |
| L click | L | `0x40000000` |
| R click | R | `0x00004000` |
| **Start** | **Plus** | `0x00020000` |
| Home | Home | `0x00100000` |
| **Capture** | Capture | `0x00200000` |
| C (Chat) | Chat | `0x00400000` |
| D-pad | Up / Down / Left / Right | `0x02000000` / `0x01000000` / `0x08000000` / `0x04000000` |

Unused on this controller: Minus, LS, RS, SL/SR, GL/GR.

Sources:
- BlueRetro `sw2.c` L19-46 (bit enum: `SW2_ZR=7`, `SW2_PLUS=9`, `SW2_HOME=12`, `SW2_CAPTURE=13`,
  `SW2_C=14`, `SW2_L=22`, `SW2_ZL=23`) and `sw2_gc_btns_mask` L99-108 (ZR → Z's generic slot). This
  table orders A/B/X/Y differently from `sw2_pro_btns_mask` because it maps **names to GameCube
  positions**, not because the bits differ.
- NS2-Connect `GC_BUTTON_MAP` L111-116: `SW2.PLUS: "Start"`, `SW2.ZR: "Z"`, `SW2.ZL: "ZL"`,
  `SW2.CAPTURE: "Capture"`, `SW2.C: "C"`.
- SDL `HandleGameCubeState` L684-722: R click `data[5]&0x40`, Z `&0x80`; Start `data[6]&0x02`,
  Home `0x10`, Share `0x20`, C `0x40`; L click `data[7]&0x40`, ZL `&0x80`. Identical after the −1
  shift.
- Linux v16 `ns2_gccon_mappings`: `BTN_TR ← ZR` (Z), `BTN_START ← PLUS`.

Consequences for the app:
- `PacketParser`'s existing masks decode every GameCube button; nothing to add.
- Issue item 5: with the patch, A/B/X/Y are no longer rotated and reach Dolphin by name
  (`Buttons/A = Button A`). Correct for a GameCube pad.
- Z needs `GameCubeButton.Z → ZR` (correction 2). Dolphin's token for ZR is `Button R2`.
- `ReportMapper` puts Capture on `BUTTON_C` and Chat on overflow `BUTTON_2` (189), so neither
  collides with anything a GameCube pad binds.

## Q3. Dolphin for Android bindings and trigger behaviour

**Answer.** Under `[GCPad<n>]`:

```
Triggers/L = `Button L1`
Triggers/R = `Button R1`
Triggers/L-Analog = `Axis 23+`
Triggers/R-Analog = `Axis 22+`
```

`L1`/`R1` because the GameCube click sets the L/R bits (Q2), which `ReportMapper` sends as
`BUTTON_L1`/`BUTTON_R1`, as the handoff's step 4 says. Not `Button L2`/`R2`: those are the
GameCube controller's ZL and Z. **Status: syntax and semantics confirmed in
Dolphin source; device test needed.**

Token syntax (Dolphin master 58967fd, 2026-10-07; identical in 2412–2609, and in 2409 except for
sticks):
- `Source/Core/InputCommon/ControllerInterface/Android/Android.cpp`: joystick axes are named
  `fmt::format("{}{}{}", ConstructAxisNamePrefix(source), axis, sign)`, i.e. `Axis <id>+` / `-` with
  the numeric `MotionEvent` axis id, never `AXIS_BRAKE`.
- `AddAxes()` adds `+` only when `max > 0` and `-` only when `min < 0`. A trigger range is 0..1, so
  only `Axis 23+` exists.
- Device qualifier `Android/<getControllerNumber()>/<name>`, matching what `DolphinGcpadConfig`
  writes today (`Device = Android/$deviceId/Joy-Con Virtual Gamepad $index`).

Which axis ids Android reports for our UHID pad (Brake `0xC5` left, Accelerator `0xC4` right,
`UhidRelay.kt:219-230`; Linux `hid-input.c` maps them to `ABS_BRAKE`/`ABS_GAS`; VID/PID
`0x1234:0x5678` gets `Generic.kl`):

| Android | `Generic.kl` | Left trigger shows as |
|---|---|---|
| 7–13 (API 24–33) | `axis 0x09 GAS`, `axis 0x0a BRAKE` | 23 only |
| 14+ (API 34+) | `axis 0x09 RTRIGGER`, `axis 0x0a LTRIGGER` | 17, plus a compat copy as 23 (`getCompatAxis`) |

So `Axis 23+`/`Axis 22+` exist on every supported Android version; `17`/`18` don't below 14.

Keys and trigger behaviour (`Source/Core/Core/HW/GCPadEmu.h`/`.cpp`,
`InputCommon/ControllerEmu/ControlGroup/MixedTriggers.cpp`):
- Key names: `Triggers/L`, `Triggers/R`, `Triggers/L-Analog`, `Triggers/R-Analog`; optional
  `Triggers/Threshold` (default **90**%, range 1–100; before 2412 range 0–100 and `>`), `Triggers/Dead
  Zone` (default 0, max 25, applies to both digital and analog), and per-input `…/Range` (default
  100).
- `MixedTriggers::GetState`: `if (button_value >= threshold) { analog_value = 1.0; *digital |= bit; }`.
  Pressing the **digital** input forces analog to 1.0, and the analog input never sets the digital
  bit. A real GameCube pad behaves this way when the L/R bits fire only at the bottom of travel (Q1 HW).
- Without a digital binding, games that need the click get nothing. Dolphin's own shipped profile
  (`Data/Sys/Profiles/GCPad/SDL Gamepad.ini`, desktop) instead binds one axis to both keys and lets
  Threshold make the click. That is a fallback if the L/R bits prove unreliable.
- `GCPad::LoadDefaults` under `#ifdef ANDROID` binds only rumble (`Android/0/Device Sensors:Motor 0`):
  there is no Android trigger default to imitate.

Constraints for the implementation:
- **Write the `-Analog` keys only for players whose controller has analog triggers.** For a Joy-Con
  pair or Pro, bytes 11/12 are digital ZL/ZR (`ReportMapper.kt:60`, `:62`), and the preset puts
  `Triggers/L` on L, so an `Axis 23+` binding would make ZL fire the GameCube L trigger.
- `GameCubeButton` targets are buttons only; the mapping model has no analog-trigger source.
  Either emit the two lines from `DolphinGcpadConfig` when the player's controller has analog
  triggers (the smaller change), or add analog trigger sources to the mapping model (larger,
  user-remappable). Decide before coding.
- **Risk (inferred, HW on an AYN-type handheld):** `docs/virtual-gamepad.md` notes that
  re-publishing firmware synthesises `L2`/`R2` from the trigger axes. Once byte 12 carries analog R
  travel, such firmware may emit `BUTTON_R2`, which is the Z binding (`Button R2`), so pulling R
  could press Z. Test on the target device. If it does, consider sending a GameCube controller's Z
  on the `BUTTON_Z` slot instead (bit 5, unused because the GameCube controller has no GL).

**Device test:** Dolphin's mapping UI detects `Axis 23+` on the trigger (one Android ≤13 and one 14+
device if available); a re-publishing handheld keeps the axes; partial presses register (Super Mario
Sunshine's spray).

## Q4. Trigger calibration: SPI `0x013140` vs sampling

**Answer.** `0x013140` holds **2 bytes**: `[0]` left zero, `[1]` right zero, raw uint8; `0xFF`
means absent. **No full-scale value is stored**: the drivers that read it scale to a fixed 232.
**Status: confirmed in source (USB drivers, and ndeadly's viewer over BLE); the values on these
units are HW.**

| Source | Reads | Full scale |
|---|---|---|
| Linux v16 (USB) | `NS2_FLASH_ADDR_FACTORY_TRIGGER_CALIB 0x13140`, size 2; `lt_zero = data[0]; rt_zero = data[1]` | `switch2_report_trigger`: `(data − zero) / (232 − zero)` |
| SDL (USB) | `ReadFlashBlock(ctx, 0x13140, …)` (L474-481), GC only | `232.f` (`MapTriggerAxis` L223-231) |
| ndeadly `switch2_input_viewer.py` (gist `7d27aa63…`, BLE) | `read_spi_memory(0x13140, 0x2)` (L1310); also reads `0x13160` len `0x20`, commented "some other calibration?" | scales to `0xFF` |
| joycon2cpp GC init (BLE) | `0x013140` len 2 and len 0x10 (`testapp.cpp` L587, L590) | — |
| BlueRetro | doesn't read it; hard-codes neutral 30, max 195 | — |
| RyanCopley app | runtime wizard (README: base ~32, bump ~190, max ~230); its doc's 64-byte table (`NSO_GC_BLE_PROTOCOL.md` L328) is an assumption nobody reads | — |

**Recommendation:**
1. Read 2 bytes from `0x013140` with the app's existing SPI read form (byte 2 = `0x00`, see
   `docs/protocol.md#spi-reads`): `02 91 00 04 00 08 00 00 02 7E 00 00 40 31 01 00`. Parse the
   reply with a sibling of `SpiColorParser`. `SpiColorParser` already rejects a reply whose
   echoed address is past `0x01301F` (negative offset → null), so the two parsers can share the
   response stream.
2. Zero = flash value. If it reads `0xFF`, or no reply arrives, sample rest as `StickCalibrator`
   does (a still window at connect).
3. Full = 232, widening to the highest value seen, as `StickCalibrator` widens stick spans.
   Measured full travel exceeds 232 on some units (guide: L 234, R 240), so clamp at 255.
   **Settled by the [capture](#hardware-capture-2026-10-07): full analog is the first stop.**
   Analog travel ends there (L ~195, R ~182 on this unit); the second stop is the click and sets the
   L/R bit. SDL and the kernel scale to 232, the click point, which caps an unclicked trigger at
   ~80%. BlueRetro's 195 is relative to its neutral 30, so it too is ~225 raw, the click point.
   Dolphin forces analog to 1.0 while the digital input is held (Q3), so saturating at the first
   stop loses nothing. Full ≈ 180 saturates both triggers of this unit; widening to "max seen" needs
   care, because values up to 220 pass without the bit during quick presses. Revisit with the other
   two controllers.
4. Output `(raw − zero) × 255 / (full − zero)`, clamped to 0..255, with a small dead zone above zero.
   NS2-Connect uses 35 raw, and BlueRetro's 30 neutral suggests a few counts of noise.

Sampling alone gives the same zero but learns a wrong one if a trigger is held at connect. Flash
alone can't know a unit's full travel. Combining them covers both.

Related, for the C-stick: factory stick calibration is 9 bytes at block + `0x28` (`0x0130A8` left,
`0x0130E8` right): neutral X/Y, +span X/Y, −span X/Y, each 12-bit packed (kernel
`switch2_parse_stick_calibration`, SDL `ParseStickCalibration`, BlueRetro `hidp/sw2.c` L35-42).
`StickCalibrator` learns all of this at runtime today, so nothing is required.

**HW:** dump `0x013140` from each of the four controllers and compare with sampled rest and full.

## Q5. Command `0x0C` side effects; rumble

### `0x0C` is "feature select"

Sub-commands (kernel `enum switch2_subcmd_feature_select`): `1` GET_INFO, `2` SET_MASK,
`3` CLEAR_MASK, `4` ENABLE, `5` DISABLE. So the app's two init commands set the feature mask, then
enable it.

| Mask bit | Kernel | ndeadly |
|---|---|---|
| 0 | buttons | buttons |
| 1 | analog (sticks, triggers) | analog |
| 2 | IMU | IMU |
| 3 | — | unknown |
| 4 | mouse | mouse |
| 5 | rumble | "current reporting" |
| 6 | — | unknown |
| 7 | magnetometer | magnetometer |

What projects send to a GameCube controller:
- **`0x27`** (buttons | analog | IMU | bit 5): SDL USB (L397, L409, every Switch 2 controller),
  procon2tool (USB), joycon2cpp `SendNSOGCOfficialInit` over BLE (`testapp.cpp` L583, L595; mask
  `0x37` for Joy-Con, `0x2F` for Pro).
- **`0xFF`** then enable `0x03`: ndeadly, on any PID including the GC (L1290, L1318).
- **`0xFF`/`0xFF`**: this app and Joycon2forMac.
- **No `0x0C`**: BlueRetro and NS2-Connect.

**The pairing guide's warning** (`NSO_GC_BLE_PROTOCOL.md` L452-458: `0x0C` "appear[s] to confuse the
controller … Do not send cmd 0x0C at all") cites no capture. Its only symptom is "unpredictable
controller behavior or no notifications", and its reasoning is that BlueRetro doesn't send it.
joycon2cpp and ndeadly both send `0x0C` to the GC over BLE, and the issue #27 reporter's buttons
work with this app's `0xFF`. **Status: the warning is inferred and contradicted; no evidence of a
side effect.**

**Recommendation:** keep `0xFF` (known to work, and it includes bit 1, analog, which the triggers
need). If the capture shows triggers stuck at 0, a low report rate or dropped notifications, try
the GameCube-specific `0x27` for this model. **HW:** whether `0xFF` vs `0x27` changes anything, and
what bit 5 is.

The format of the report is set by which characteristic is subscribed (`…fd2` = the common layout),
not by `0x0C`. No `03 91 … 0a` "set report format" command is needed over BLE.

### Rumble

**Joycon2Android has no rumble at all (confirmed).** The only grep hit for
rumble/vibrat/FF_/force feedback is an Eden config test. `uhid_relay.c` never reads `UHID_OUTPUT`.
The HID descriptor (`UhidRelay.kt:165-250`) has no Output items, so `hid-generic` exposes no
`FF_RUMBLE`, and Android can't vibrate the virtual pad (inferred). Dolphin's Android rumble would
therefore need:
1. a pad identity whose kernel driver implements force feedback;
2. the relay reading `UHID_OUTPUT`;
3. a BLE rumble command.

**GameCube controller command (ERM motor, on/off only):**
- **BlueRetro** (confirmed BLE, `hidp/sw2.c:109-110`): 21 bytes, write without response, to handle
  `0x0016`. `[0] = 00`, `[1] = 0x50 | seq` (4-bit counter), `[2] = 01` on / `00` off, then an embedded
  command from `[5]`. Sent only on change.
- **Kernel/SDL (USB):** `[2]` = `0` off / `1` on / `2` stop. Strength is simulated by pulsing it
  (every 4 ms in the kernel, 12 ms in SDL). The kernel also sets feature bit 5 and sends `11 91 …`
  ("needed for rumble to work reliably").
- **Handle `0x0016`'s UUID on the GC** is `af95885e-44b3-4a24-9cf0-483cc129469a`, inferred by lining
  up joycon2cpp's UUID names with BlueRetro's handles.
- Joycon2cpp's README says "Rumble is not supported for the NSOGC Controller".

**Recommendation:** out of scope for issue #27 and the four-player goal. It needs work at both ends
(the pad's kernel-side force feedback and a BLE writer). Record it as a follow-up.

## Q6. Four controllers on Android BLE

**Answer: four links fit Android's limits. High priority on all four should fit in radio time at
15 ms. The practical risks are in how the app connects, not in the steady state.** Status: AOSP
values confirmed; chipset behaviour is inferred. Only a four-controller test settles latency.

Limits (AOSP):
- `GATT_MAX_PHY_CHANNEL 7` through Android 14 (`system/internal_include/bt_target.h`;
  android-13.0.0_r1 L546, android-14.0.0_r1 L535). On current main the floor is 8 and the ceiling
  16, via `bluetooth.core.le.max_number_of_concurrent_connections` (`gatt_utils.cc` L106-113).
  Shared system-wide with watches, earbuds and similar devices.
- `CONNECTION_PRIORITY_HIGH` = interval 9–12 units (11.25–15 ms), latency 0. BALANCED = 30–50 ms.
  DCK (API 34+) = 30 ms, so it doesn't help. Timeout 5 s. Values are in the Bluetooth `config.xml`
  (L27-36, L69-72) and `GattService.java` (L2104-2141). The 15 ms and 30 ms the repo measured are
  the top and bottom of those ranges.
- No limit on how many links hold HIGH. `requestConnectionPriority` returns true as soon as the
  request is queued (`BluetoothGatt.java` L2134-2152), so the app's `accepted=` log proves nothing.
  If the controller later asks for its own parameters, Android accepts them (`l2c_ble.cc`
  L324-361).
- Airtime per 63-byte notification, about 0.9 ms per event on 1M PHY with Data Length Extension
  (DLE): four links at 15 ms use ~25% of airtime, ~53% without DLE. Bandwidth (~17 kB/s total) is
  trivial; Wi-Fi coexistence costs late events, not dropped links.
- No reports found of four Joy-Con 2 or GameCube controllers on Android.

Repo risks (`feature/connection/data/…`):
- **Parallel connects.** Each scan result calls `connectGatt` immediately
  (`Joycon2Manager.kt:133` → `ConnectionPool.connect`). There is one `GattOpQueue` per connection
  and nothing global. Several pending direct connects make the stack cancel and restart the
  controller's connect attempt (`le_impl.h` `disarm_connectability`). Forum reports put status 133
  at 5+ parallel connects. A failed controller is simply dropped, and `protocol.md` gotcha 5
  (connect cooldown) makes retries slow. **Mitigation:** one app-wide connect gate that starts the
  next `connectGatt` only after the previous controller is ready or has failed.
- **Low-latency scan during connects.** The scan runs at 100% duty (`ScanManager.java` L96-97)
  until 15 s after Scan (`BleScanner.kt:22`) or 8 controllers, competing with links that are
  connecting or already streaming. It is bounded, so it affects connecting, not play. **Mitigation:**
  stop scanning once the expected controllers are connected, or let the user stop it.
- **`MAX_CONNECTIONS = 8`** (`Joycon2Manager.kt:23`) exceeds the 7-channel limit on Android ≤14.
  Harmless at four.
- **No check of the granted interval.** `BluetoothGattCallback.onConnectionUpdated` is `@hide` but
  can be overridden; a btsnoop log shows "LE Connection Update Complete". Worth logging before
  judging lag.
- 2M PHY (`setPreferredPhy`, API 26+) would roughly halve airtime if the controllers support it.
  Untested.
- CPU, not radio: every notification rebuilds the whole controller list and re-resolves all eight
  players (`Joycon2Manager.rebuildControllers` → `SessionCoordinator`), ~270 times a second at
  four controllers × 67 Hz.

"Faster controller updates" (HIGH) should help at four. It would hurt only if a chipset gives the
links different intervals and they drift into each other.

**HW (four-controller test):**
- intervals actually granted per link, from btsnoop;
- notifications per second per controller, with Wi-Fi active;
- 133 failures with parallel versus serialised connects;
- whether the controllers support DLE and 2M PHY.

## Q7. Manual type override (issue #27 main request)

**Answer: store it in the connection feature, keyed by BLE address. Apply it in the connection so
the parser sees it. Offer it only on unassigned controllers, and unassign first if it is ever
changed on an assigned one.** Status: design, inferred from the code below. Address stability is
HW.

### Where `Side` is consumed today

| Consumer | Reads | Effect of a runtime change |
|---|---|---|
| `JoyconConnection` | `val side` fixed at construction (`JoyconConnection.kt:28`) → `PacketParser.parse(data, side)` per packet (`:283`) | **The override must reach here.** The right stick is decoded only for `Side.PRO` (`PacketParser.kt:33`), and RIGHT reads its stick from `0x0D` (`:54`). An override held only above the connection layer would never give the C-stick |
| `StickCalibrator` (per connection, `JoyconConnection.kt:84`) | — | For a non-Pro side the right axes see a constant 2048 and freeze their centre there after 30 samples. After a switch to PRO the C-stick keeps that wrong centre (raw rest differs by ~2–5%). A LEFT↔RIGHT switch moves the left axes onto the other stick. **Replace the calibrator on a type change** |
| `Joycon2Manager.rebuildControllers` | `connection.side` → `ConnectedJoycon.side` | Picks up a change on the next emission |
| `ComboAssignmentDetector` | `side` per call; latch keyed by address | Fine: L+R assigns once the side is PRO |
| `SessionCoordinator.assign` (`SessionCoordinator.kt:50`) | live side → `AssignmentRepository.assign(address, side, player)` | — |
| `PlayerAssignmentManager` | **caches** `sides[address]` at assign time (`PlayerAssignmentManager.kt:14`, `:18`), used only by `isSlotTaken` | **Stale after a change.** Example: a P1 controller cached UNKNOWN becomes PRO, and another controller can still be assigned to P1 |
| `PlayerStateResolver` | live side on every emission | Stateless, so it adapts. But if a player holds a PRO plus another controller, the other is silently dropped from `PlayerState` while still assigned, with its LED lit |
| `AssignmentPanel` | `joycon.side` for the label (`AssignmentPanel.kt:93`) and slot check (`:189`) | Fine |
| `PlayerBody` / mappings / emulator configs | `PlayerState.hasPro` etc. | Body changes (e.g. LEFT → DUAL), so a different saved mapping applies, and Dolphin/Eden configs are stale until Set up runs again |

### Recommended design

- **Value:** `Side?` per address, null = auto (advertised). Offer Auto / Left Joy-Con / Right Joy-Con /
  Pro Controller. Pro is what #27 asks for. Left and right cost nothing extra (every `when` over
  `Side` already handles them) and help a Joy-Con whose advertisement lacks manufacturer data. Never
  offer `UNKNOWN`.
- **Storage:** an interface in `:feature:connection:domain`, e.g. `ControllerTypeOverrides`, with a
  DataStore implementation in `:feature:connection:data`, mirroring `ViewModePreferences` /
  `ViewModePreferencesDataStore`. Inject it into `Joycon2Manager` through its constructor; today
  that class constructs its collaborators itself, so this is a small DI step. The connect path
  needs the value synchronously, so cache the DataStore flow in a `StateFlow`.
- **Key: the controller's BLE address** (`ScanResult.device.address`). It survives reconnects
  **only if the controller advertises from a stable address**. That is likely: the Switch 2
  re-finds it from its stored pairing, and `docs/protocol.md` treats the controller side as a fixed
  identity. But it is unverified (HW: compare the `Adv <address>` log line across two SYNC
  sessions). If it isn't stable, nothing else is a usable key: the advertisement has no name and
  no serial. Fall back to per-session only.
- **Applying it:** `Joycon2Manager.onDeviceFound` passes `override ?: advertisedSide` to
  `pool.connect`. For a live change, add `ControllerRepository.setControllerType(address, side?)`.
  `JoyconConnection.side` becomes a `@Volatile var`, written together with a fresh
  `StickCalibrator`. `ConnectedJoycon` gains the advertised side, or an "overridden" flag, so the UI
  can show "Auto (Joy-Con)".
- **Use case and flow:** `AssignmentPanel` lives in `:feature:assignment:presentation`, which can't
  see the connection domain (features never depend on each other). Add a callback parameter,
  `onSetType: (address, Side?) -> Unit`, wired in `:app` (`JoyconScreen`/`Joycon2ViewModel`) to a
  `SetControllerTypeUseCase` in `:core:session` beside `AssignControllerUseCase`.
  `SessionCoordinator.setControllerType` first calls `unassign(address)` if the controller is
  assigned (clears the LED, fires `onPlayerUnassigned`, drops the stale `sides` entry), then calls
  `controllers.setControllerType`. That keeps every assignment invariant without teaching
  `PlayerAssignmentManager` about type changes.
- **UI placement:** `AssignmentPanel` only lists unassigned controllers, so a type menu on
  `JoyconAssignmentRow` (e.g. a chip beside the side label) exposes the override only where it is
  safe. The coordinator's unassign-first rule covers any later entry point. Strings for
  "Auto"/"Type" go in the assignment presentation module; the existing `side_left`/`side_right`/
  `side_pro` strings cover the rest. Check `docs/DESIGN.md` for the control style.
- **Docs:** README (players: how to set a type), `docs/architecture.md` if a new
  interface/use case lands, `docs/protocol.md` for the new PID.

### Telling a GameCube controller apart (handoff step 5)

Keep `Side.PRO` for the GameCube controller: it is held and decoded like a Pro. Carry the model
separately, from the advertised product ID: a model enum, or `hasAnalogTriggers`, on the
connection → `ConnectedJoycon` → `PlayerState`. Analog triggers (`ReportMapper` bytes 11/12, the
calibrator, Dolphin's `-Analog` lines) and the GameCube layout default key off the model, not the
side. A new `Side.GAMECUBE` would ripple through every exhaustive `when` and gain nothing, because
the GameCube controller differs only in the trigger source and default layout.

An override to PRO on an unrecognised controller won't give it analog triggers. That is fine for
#27, whose reporter called analog triggers optional.

## What the hardware capture must settle

Recorded with the [in-app capture tool](#in-app-capture-tool-separate-upstream-pr): one GameCube
controller through the guided steps, then all three of the user's controllers at once (the goal is
four; three exercises the same multi-link behaviour):

1. ~~Triggers at rest, sweep, first and second stop, and where the click bit sets (Q1, Q4).~~ Done
   for one controller, 2026-10-07.
2. ~~No bits outside the Q2 table.~~ Done: none.
3. ~~`Adv` log `73 20`, and the address stable across sessions (Q7).~~ Done: same address in both
   captures, with a shared vendor prefix that marks it as a public address.
4. ~~SPI `0x013140` reply versus the sampled rest values (Q4).~~ Done: zeros 33/32, rest within a
   few counts.
5. Optional A/B: init mask `0xFF` vs `0x27` (Q5).
6. A Pro Controller's `0x3C`/`0x3D` stay flat, if one is available (Q1).
7. Several controllers: ~~packets per second each, connect failures~~ (three at balanced priority:
   ~31–32/s each, no failures). High priority and the granted intervals (btsnoop) are still open
   (Q6).
8. Dolphin: `Axis 23+` detected, partial trigger travel works, pulling R doesn't fire Z on the
   target handheld (Q3).

## In-app capture tool (separate upstream PR)

Decided 2026-10-08: the capture is a **hidden developer option in the app**, sent upstream as its own
PR, independent of the GameCube PR. Test hardware: a Retroid Pocket Nova and three NSO GameCube
controllers, with no PC attached.

**Why not the patch's capture.** The patch logs under `J2Raw`, gated by `Log.isLoggable`. Turning it
on needs `adb shell setprop log.tag.J2Raw DEBUG`, which an app can't run, so it is useless on a
lone handheld. Move `PacketChangeFilter` and `RawPacketFormat` (and their tests) into the capture
PR, and drop the `isLoggable` gate, the `logRawPacket` hook and the patch's
"Capturing raw packets" doc section from the GameCube PR.

**What it must do:**

| Requirement | Detail |
|---|---|
| Hidden entry | Revealed by a deliberate gesture in the settings drawer, e.g. tapping a version line 7 times (Android's convention); remembered once revealed. The drawer has no version line today (`InstalledAppVersion` in `:feature:update:data` reads it) |
| Record without adb or Shizuku | Capture needs only BLE, so it works before Shizuku or the virtual gamepad is set up |
| Header | App version, `Build.MANUFACTURER`/`MODEL`, Android version, start time |
| Per controller | Address, full manufacturer data (product ID at bytes 5–6), detected side, connect/disconnect events with GATT status |
| Command replies | Every reply on the command-response characteristic, as hex |
| Calibration reads | While recording, one SPI read of `0x40` bytes from `0x013140` per controller (covers the trigger zero points at `0x013140` and the unknown block at `0x013160`, Q4), using the existing read form `02 91 00 04 00 08 00 00 40 7E 00 00 40 31 01 00` |
| Input | Changed-input packets (`PacketChangeFilter`), raw hex plus decoded fields (`RawPacketFormat`) |
| Rate | Per controller, packets per second over each second, counting every packet rather than only the changed ones (Q6) |
| Guided steps | A prompt list the user advances: hands off (rest), each button, each stick in four directions and a full circle; then L: a slow press from rest to the bottom and back (~3 s each way, logging every value the change filter passes), hold at the first stop (~2 s), hold at the second stop (~2 s), release; then the same for R. Only the stops are held, because they can be felt; partial travel comes from the sweep, since a target like "halfway" can't be hit reliably. Every line carries the current step |
| Output | A plain-text file in app-specific storage (no permission needed), shared through a `FileProvider` and the share sheet |
| Optional | A developer toggle for the init mask `0xFF` vs `0x27` (Q5 A/B) |

**Built** on the `controller-capture` branch (see its `docs/capture.md`): a
`capture` feature (domain/data/presentation), fed by a `ControllerTrafficSource` in `:core:model`
that `Joycon2Manager` implements through `TrafficRelay`, and wired in `:app`. Nothing is copied
or allocated per packet unless a recording is running. The init-mask A/B toggle was left out.

**Docs:** a short `docs/` page for the format and how to read a capture, linked from
`docs/README.md`. If upstream wants it, add a README troubleshooting line telling players how to
send a capture.

**Test build.** Merge the capture branch and the GameCube branch on the fork and build one APK
from that. Uninstall the official app first, because the signatures differ.

## Suggested order for the implementation session

1. Install the SDK and build locally (correction 9).
2. **Capture PR**, branched from `main`: the tool above. It doesn't depend on the GameCube changes:
   the raw bytes are the same whatever side the scanner assigns.
3. **GameCube PR**, branched from `main`: apply the patch minus its `J2Raw` logging; fix compile
   errors; un-skip Pro players in `DolphinGcpadConfig` (correction 1); add a GameCube-controller
   layout with `Z → ZR` (correction 2). This alone gets the GameCube pads into Dolphin with both
   sticks and digital triggers.
4. Build a fork APK with both branches merged, then capture on the Nova: first one controller
   through the guided steps, then all three connected at once (the list above).
5. Manual type override (Q7), which closes #27.
6. Analog triggers: model flag, calibration, `ReportMapper` bytes 11/12, Dolphin `-Analog` lines.
   Correct `docs/virtual-gamepad.md`'s trigger aliasing note in the same commit.
7. Connect gate and scan stop for several controllers (Q6), if the multi-controller capture shows
   failures.
