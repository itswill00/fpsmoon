# FPSMoon

> A fast, lightweight performance monitor and overlay for rooted Android devices.

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Android_Root-brightgreen.svg)](https://github.com/itswill00/fpsmoon)
[![Architecture](https://img.shields.io/badge/Architecture-ARM64-orange.svg)](https://github.com/itswill00/fpsmoon)
[![Author](https://img.shields.io/badge/Author-@itswill00-purple.svg)](https://github.com/itswill00)

FPSMoon displays real-time FPS, frame times, CPU, GPU, memory, battery usage, and network speeds in a smooth glassmorphism overlay on your screen. It comes with an embedded web control panel so you can easily adjust colors, sizes, themes, and refresh speeds on the fly.

---

## Features

- **Fast Performance Engine**: Built with native C for minimal CPU and battery usage.
- **Smooth Canvas Overlay**: Runs through Android `app_process` with hardware acceleration to prevent lag or stutters while gaming.
- **Smart Auto-Sizing**: The overlay card automatically resizes itself to fit your active stats and font size without clipping text.
- **Embedded Web Control Panel**: Easily tweak themes, transparency, card height, font size, or refresh rates from a web browser.
- **Broad Device Compatibility**: Works across Snapdragon, MediaTek, Exynos, Tensor, and UNISOC devices.

---

## How It Works

```text
+-------------------------------------------------------------------+
|                        Android Kernel & Sysfs                     |
|           Reads hardware stats (CPU, GPU, RAM, Battery)           |
+-------------------------------------------------------------------+
                                  |
                                  v
+-------------------------------------------------------------------+
|                      C Performance Daemon                         |
|                     (bin/fpsmoon_daemon)                          |
|             Updates stats file at your chosen speed               |
+-------------------------------------------------------------------+
                  /                                 \
                 /                                   \
                v                                     v
+-------------------------------+         +-------------------------+
|      Java Overlay Engine      |         |    Web Control Panel    |
| (com.fpsmoon.FPSMoonOverlay)  |         | (Customize HUD settings |
| Draws floating HUD on screen  |         |    and live preview)    |
+-------------------------------+         +-------------------------+
```

---

## Supported Stats & Hardware

| Stat | Description | Supported Hardware |
| :--- | :--- | :--- |
| **FPS & Frame Time** | Real-time FPS, frame duration (ms), and display refresh rate | Snapdragon, MediaTek, Exynos, Tensor |
| **Display Refresh Rate** | Auto-detects screen refresh rate (60Hz, 90Hz, 120Hz, 144Hz) | All High Refresh Displays |
| **CPU Usage** | CPU load %, average frequencies, governor, and temperatures | All ARM64 Processors |
| **GPU Usage** | GPU load %, current clock frequency, temperature, and governor | Adreno, Mali, PowerVR |
| **Memory & Power** | RAM, ZRAM, battery power draw (Watts, mA, mV, and temperature) | All Android Power Supplies |
| **Network Speed** | Live download and upload speeds | Mobile Data & Wi-Fi |

---

## Project Structure

```text
fpsmoon/
├── action.sh             # Service restart script
├── customize.sh          # Module installer script
├── module.prop           # Module metadata
├── service.sh            # Boot startup script
├── uninstall.sh          # Clean uninstall script
├── bin/
│   ├── fpsmoon.dex       # Compiled Java overlay binary
│   └── fpsmoon_daemon    # Compiled C performance daemon binary
├── src/
│   ├── fpsmoon_daemon.c  # C performance daemon source code
│   └── FPSMoonOverlay.java# Java overlay source code
├── state/                # State directory (config.json, stats.json)
└── webroot/              # Web control panel interface
    ├── app.js
    ├── index.html
    └── styles.css
```

---

## Installation

1. Download the latest `FPSMoon-v1.0.0.zip` from the Releases section.
2. Open **Magisk**, **KernelSU**, or **APatch**.
3. Tap **Install from Storage** and select `FPSMoon-v1.0.0.zip`.
4. Reboot your device.

To manually restart the overlay services at any time, run this command in a root terminal:

```bash
su -c /data/adb/modules/fps_moon/action.sh
```

---

## Building the Project

### Tools Needed
- `clang` or `gcc` (For C daemon compilation)
- `ecj` (Eclipse Compiler for Java)
- `dx` or `d8` (Android DEX compiler)
- `android.jar`

### Build Commands

1. Compile the C performance daemon:
   ```bash
   clang -O3 src/fpsmoon_daemon.c -o bin/fpsmoon_daemon
   ```

2. Compile the Java overlay to DEX:
   ```bash
   mkdir -p build
   ecj -cp /path/to/android.jar -d build/ src/FPSMoonOverlay.java
   dx --dex --output=bin/fpsmoon.dex build/
   ```

3. Create the module zip:
   ```bash
   zip -r FPSMoon-v1.0.0.zip action.sh customize.sh module.prop service.sh uninstall.sh bin webroot
   ```

---

## License

```text
MIT License

Copyright (c) 2026 @itswill00

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

## Author

Created and maintained by **[@itswill00](https://github.com/itswill00)**.
