# FPSMoon

> High-Performance Real-Time System Telemetry & Canvas Overlay Engine for Android

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Android_Root-brightgreen.svg)](https://github.com/itswill00/fpsmoon)
[![Architecture](https://img.shields.io/badge/Architecture-ARM64-orange.svg)](https://github.com/itswill00/fpsmoon)
[![Author](https://img.shields.io/badge/Author-@itswill00-purple.svg)](https://github.com/itswill00)

FPSMoon is a lightweight, low-overhead system performance monitor and overlay suite designed for rooted Android environments (Magisk, KernelSU, and APatch). It combines a high-speed Native C Telemetry Daemon with a Hardware-Accelerated Java Canvas Overlay (`app_process`) and an embedded Web Control Panel.

---

## System Architecture

```text
+-------------------------------------------------------------------+
|                        Android Kernel / Sysfs                     |
|            (/sys/class/thermal, /sys/class/kgsl, /proc/stat)     |
+-------------------------------------------------------------------+
                                  |
                                  v
+-------------------------------------------------------------------+
|                  Native C Telemetry Engine                        |
|                     (bin/fpsmoon_daemon)                          |
|         Reads Sysfs -> Writes Atomic Atomic State Buffer          |
+-------------------------------------------------------------------+
                  /                                 \
                 /                                   \
                v                                     v
+-------------------------------+         +-------------------------+
|  Java Canvas Overlay Engine   |         |  Embedded Web UI Panel  |
| (com.fpsmoon.FPSMoonOverlay)  |         |  (Webroot Settings API) |
|  Dynamic Glassmorphism HUD    |         |  Instant Real-Time Sync |
+-------------------------------+         +-------------------------+
```

---

## Core Capabilities

### 1. Native C Telemetry Engine
- Zero-latency hardware status polling written in optimized C (`fpsmoon_daemon.c`).
- Zero-disk-read stat caching utilizing `stat()` timestamp verification to eliminate unnecessary flash storage reads.
- Configurable polling intervals ranging from 100ms up to 1000ms.

### 2. Hardware-Accelerated Java Overlay (`app_process`)
- Direct window manager surface attachment using Android `app_process`.
- Zero-heap allocation during active canvas rendering ticks to prevent Garbage Collection (GC) pauses and micro-stutters during high refresh rate gameplay.
- Smart Auto-Fitting layout math that dynamically resizes card dimensions based on active metrics, scale, and font sizes.
- Flexible orientation support (Horizontal pill layout & Vertical stack layout).

### 3. Integrated Web Control Panel
- Embedded Material-inspired control panel accessible via HTTP/KSU IPC.
- Instant real-time synchronization between the web panel controls and the live HUD overlay.

---

## Multi-Vendor Hardware Support Matrix

| Component | Telemetry Sources & Fallback Paths | Supported Chipsets |
| :--- | :--- | :--- |
| **FPS & Frame Time** | MediaTek FPSGO, Snapdragon SDE Measured FPS, DRM/KMS VBlank | Snapdragon, MediaTek, Exynos, Tensor |
| **Screen Refresh Rate** | `/sys/class/graphics/fb0/dynamic_fps`, DRM mode queries | 60Hz, 90Hz, 120Hz, 144Hz displays |
| **CPU Telemetry** | `/proc/stat`, policy & cpu core scaling frequencies, thermal zones | All ARM64 Processors |
| **GPU Telemetry** | MediaTek GED HAL, Qualcomm KGSL Adreno, ARM Mali Devfreq | Adreno, Mali, PowerVR |
| **GPU Governor** | Multi-path Devfreq sysfs queries (`simple_ondemand`, `msm-adreno-tz`, etc.) | Snapdragon, MediaTek, Exynos |
| **Memory & Power** | `/proc/meminfo` (RAM & ZRAM), Power supply BMS (Watt, mA, mV, °C) | All Android Power Supplies |
| **Network Speed** | Network interface device stats (`/proc/net/dev`) | Cellular Data & Wi-Fi |

---

## Repository Directory Structure

```text
fpsmoon/
├── action.sh             # Service lifecycle and manual restart control script
├── customize.sh          # Module installation and initial state generator
├── module.prop           # Magisk/KernelSU module metadata
├── service.sh            # Boot completion trigger daemon script
├── uninstall.sh          # Graceful state cleanup script
├── bin/
│   ├── fpsmoon.dex       # Compiled Java overlay DEX binary
│   └── fpsmoon_daemon    # Compiled Native C telemetry binary
├── src/
│   ├── fpsmoon_daemon.c  # Native C engine source code
│   └── FPSMoonOverlay.java# Java app_process overlay source code
├── state/                # Runtime state buffers (config.json, stats.json)
└── webroot/              # Embedded Web UI control panel
    ├── app.js
    ├── index.html
    └── styles.css
```

---

## Installation & Usage

### Flashing via Root Managers
1. Download the latest `FPSMoon-v1.0.0.zip` release.
2. Open **Magisk**, **KernelSU**, or **APatch**.
3. Select **Install from Storage** and flash `FPSMoon-v1.0.0.zip`.
4. Reboot your device.

### Manual Service Management
Services can be restarted or re-initialized manually at any time via root terminal:

```bash
su -c /data/adb/modules/fps_moon/action.sh
```

---

## Building from Source

### Prerequisites
- `clang` or `gcc` (For C daemon compilation)
- `ecj` (Eclipse Compiler for Java)
- `dx` or `d8` (Android DEX compiler)
- `android.jar`

### Compilation Steps

1. Compile the Native C Daemon:
   ```bash
   clang -O3 src/fpsmoon_daemon.c -o bin/fpsmoon_daemon
   ```

2. Compile the Java Overlay Engine to DEX:
   ```bash
   mkdir -p build
   ecj -cp /path/to/android.jar -d build/ src/FPSMoonOverlay.java
   dx --dex --output=bin/fpsmoon.dex build/
   ```

3. Package into Module Zip:
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

## Credits & Author

Designed, developed, and maintained by **[@itswill00](https://github.com/itswill00)**.
