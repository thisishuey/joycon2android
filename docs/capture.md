# Controller capture

A hidden developer option that records what each connected controller sends, to a text file to
share. It is for mapping a controller the app doesn't fully support yet, on the device alone, with
no adb. Code: `feature/capture`, fed by `TrafficRelay` in `feature/connection/data`.

## Recording one

1. Settings (gear, top right) → tap the **Version** line 7 times. **Controller capture** appears
   above it and stays.
2. Turn the virtual gamepad off, so Home and + don't act on the system while you press them.
3. **Start recording**, then scan and connect. A controller already connected is recorded too: its
   advertisement is replayed when recording starts.
4. Follow the steps (below), tapping **Next** after each; **Finish** on the last. **Stop** ends early.
5. **Share last capture** hands the file to any app. Files stay in the app's private storage
   (`files/captures/`, one per recording), served by `CaptureFileProvider`.

## Steps

| Step | What it settles |
|---|---|
| Hands off | stick and trigger rest values |
| Face buttons, D-pad, shoulder, centre buttons, stick clicks, back buttons | which bit each button sets; pressed in the order the prompt names |
| Left stick, right stick | each axis's travel and centre |
| Each trigger: slow press, first stop, second stop | analog travel, the value at each tactile stop, and where the digital click sets |
| Every controller | report rate per controller with all of them streaming |

A trigger is swept rather than held at "halfway", which can't be hit reliably; only the two stops,
which can be felt, are held.

## Format

```
# Joycon2Android <version> controller capture
# device: <manufacturer> <model>, Android <release> (API <n>)
<seconds> <STEP> <address or -> <event> <fields>
```

| Event | Fields |
|---|---|
| `step <n>/<total>` | the guide moved on |
| `discovered` | side the scanner assigned, name, `mfg=` the full Nintendo manufacturer data (product ID at bytes 5–6, [protocol.md](protocol.md#advertising)) |
| `connected` | negotiated `mtu` |
| `ready` | init sequence complete |
| `spi-read` | the [calibration read](#calibration-read) was requested |
| `reply` | a command-response notification, raw (LED acks, SPI reads) |
| `input` | an input packet whose controls moved (below) |
| `rate` | that controller's packets per second over the last second, counting every packet |
| `disconnected` | GATT `status` |

An `input` line:

```
btn=<uint32 at 0x03> pad=<0x07> L=<x>,<y> R=<x>,<y> trig=<0x3C>,<0x3D> pressed=<names> len=<n> raw=<hex>
```

- Offsets and button masks: [protocol.md](protocol.md#packet-layout). Sticks are raw 12-bit, before
  `StickCalibrator`. `pressed` is `PacketParser`'s decoding.
- Logged only when bytes `0x03..0x09` change, a stick moves more than 24 LSB or a trigger more than 3.
  IMU and counter changes alone are dropped.
- A packet too short to reach `0x3D` is an unknown layout: it is logged with `raw=` only, whenever
  anything past the 3-byte counter changes.

## Calibration read

When a controller is ready, the recorder reads `0x40` bytes from SPI `0x013140`. The reply is a
`reply 02 91 …` line, with data from byte 16 ([protocol.md](protocol.md#spi-reads)).

- `0x013140` `[0]`, `[1]`: left and right trigger zero points, per the Linux and SDL Switch 2 drivers
  (USB). Unconfirmed over BLE.
- `0x013160`: read by ndeadly's `switch2_input_viewer` as "some other calibration?". Unidentified.

## Cost when off

`TrafficRelay.emit` builds nothing without a listener, so the per-packet path copies and allocates
only while a recording runs.
