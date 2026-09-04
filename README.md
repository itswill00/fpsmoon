# FPS Moon

> Lightweight, real-time performance monitor HUD and screen overlay for rooted Android devices.

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Android_Root-brightgreen.svg)](https://github.com/itswill00/fpsmoon)
[![Architecture](https://img.shields.io/badge/Architecture-ARM64-orange.svg)](https://github.com/itswill00/fpsmoon)
[![Root Solution](https://img.shields.io/badge/Root-KernelSU%20%7C%20APatch%20%7C%20Magisk-blueviolet.svg)](https://github.com/itswill00/fpsmoon)
[![WebUI](https://img.shields.io/badge/WebUI-Vue%203%20SingleFile-emerald.svg)](https://github.com/itswill00/fpsmoon)

FPS Moon provides an ultra-lightweight, hardware-accelerated on-screen HUD displaying real-time FPS, frame duration, CPU and GPU utilization, active frequencies, scaling governors, temperatures, RAM, ZRAM swap, power dissipation (Watts), battery current, and network throughput.

It features a modern single-file Vue 3 WebUI integrated directly into KernelSU, APatch, and MMRL for instant configuration and live control.

---

## Key Highlights

- **Native C Telemetry Daemon**: Direct `/proc` and `/sys` kernel metrics polling written in optimized C with minimal CPU and battery footprint.
- **Skia CPU Rasterization**: Software canvas rendering attached directly via `WindowManagerProvider`, bypassing SELinux GPU restrictions and preventing frame stutter during intense gaming.
- **Dynamic Screen Refresh Rate**: Auto-queries display refresh rates (60 Hz, 90 Hz, 120 Hz, 144 Hz) directly through the Android display subsystem.
- **Interactive Drag & Drop**: Touch and reposition the floating HUD anywhere on your screen in real time with seamless position persistence.
- **Single-File Vue 3 WebUI**: Built with Vue 3, Vite, Pinia, and Vue Router, bundled into a self-contained offline interface with zero external runtime dependencies.
- **Broad Hardware Compatibility**: Out-of-the-box support for Qualcomm Snapdragon (Adreno), MediaTek (Mali), Samsung Exynos, Google Tensor, and UNISOC platforms.
- **Universal ROM Compatibility**: Engineered for AOSP, LineageOS, PixelOS, Xiaomi HyperOS / MIUI, Samsung OneUI, and BBK ColorOS/OxygenOS.

---

## System Architecture

```text
+-------------------------------------------------------------------+
|                        Linux Kernel & Sysfs                       |
|          Hardware telemetry: CPU, GPU, RAM, ZRAM, Battery         |
+-------------------------------------------------------------------+
                                  |
                                  v
+-------------------------------------------------------------------+
|                      Native C Daemon Process                      |
|                       (bin/fpsmoon_daemon)                        |
|           Atomic non-blocking telemetry updates to state/         |
+-------------------------------------------------------------------+
                  /                                 \
                 /                                   \
                v                                     v
+-------------------------------+         +-------------------------+
|      Java Overlay Engine      |         |    Vue 3 Web Control    |
| (com.fpsmoon.FPSMoonOverlay)  |         | (Single-file WebUI in   |
| Real-time floating canvas HUD |         |  KernelSU / APatch /    |
|  attached to Window Manager   |         |      MMRL Managers)     |
+-------------------------------+         +-------------------------+
```

---

## Monitored Metrics

| Metric | Details | Supported Platforms |
| :--- | :--- | :--- |
| **FPS & Frametime** | Real-time frames per second and instantaneous frame time (ms) | Qualcomm, MediaTek, Exynos, Tensor |
| **Display Hz** | Real-time active screen refresh rate | All dynamic & high-refresh displays |
| **Processor (CPU)** | Load %, clock frequencies, active governor, and package temp | ARM64 multi-cluster CPUs |
| **Graphics (GPU)** | GPU load %, current clock MHz, active governor, and temp | Adreno, Mali, PowerVR |
| **Memory (RAM/Swap)**| Active memory usage, total RAM, and ZRAM compressed swap | All Android devices |
| **Power & Battery** | Wattage (W), current draw (mA), voltage (mV), and battery temp | Standard Android power supply sysfs |
| **Network Speed** | Instantaneous download and upload transfer rates | Wi-Fi and Mobile Data |

---

## Requirements

- **Device Architecture**: ARM64 (`arm64-v8a` / `aarch64`)
- **Android Version**: Android 8.0 (Oreo) up to Android 15+
- **Root Solution**:
  - [KernelSU](https://github.com/tiann/KernelSU) (v0.6.0+)
  - [APatch](https://github.com/bmax121/APatch) (v10763+)
  - [Magisk](https://github.com/topjohnwu/Magisk) (v24.0+)

---

## Installation

1. Download the latest `FPSMoon-v1.0.1.zip` from [Releases](https://github.com/itswill00/fpsmoon/releases).
2. Open your root manager (**KernelSU**, **APatch**, or **Magisk**).
3. Navigate to the **Modules** section, select **Install from storage**, and choose the downloaded zip.
4. Reboot your device to start background services automatically.
5. Open the module's WebUI directly inside KernelSU / APatch / MMRL to configure layout, size, and active metrics.

> **Tip:** Existing position coordinates and user preferences are automatically preserved when updating or reinstalling the module.

---

## Web Control Interface

The built-in WebUI provides instantaneous controls divided into four dedicated workspaces:

- **Dashboard**: High-level overlay state, master visibility switch, quick actions, 2x2 live telemetry cards, layout switcher (Horizontal Pill / Vertical Stack), alignment controls, and quick presets.
- **Customize**: Granular item toggles (FPS, CPU, GPU, Governors, RAM, ZRAM, Battery, Network) and appearance sliders (Width, Height, Scale, Text size, Corner radius, Transparency, Update speed).
- **Activity Log**: Live service diagnostics (Daemon PID, Overlay PID), terminal console log viewer, copy log, clear log, and service restart trigger.
- **About**: Hardware and system specifications, kernel version, display capabilities, community links, issue reporting, and release notes.

---

## Troubleshooting & FAQ

### 1. Overlay is not showing on Xiaomi / HyperOS / MIUI
MIUI and HyperOS restrict background overlay permissions by default:
1. Open **Settings** -> **Apps** -> **Manage apps**.
2. Tap the three dots menu and choose **Show all apps**.
3. Locate **Shell** (or your root manager app like KernelSU / APatch).
4. Tap **Other permissions** and grant **Display pop-up windows while running in the background**.
5. Restart services via `su -c /data/adb/modules/fps_moon/action.sh`.

### 2. How do I reposition the overlay?
Simply touch anywhere on the overlay card on your screen and drag it to your preferred position. Coordinates are saved automatically with sub-pixel precision.

### 3. How do I manually restart or stop the overlay?
Execute the action script from a terminal emulator or Termux:
```bash
# Restart daemon and overlay
su -c /data/adb/modules/fps_moon/action.sh

# Stop services
su -c "killall -9 fpsmoon_daemon; pkill -f com.fpsmoon.FPSMoonOverlay"
```

---

## Building from Source

### Prerequisites
- `clang` (LLVM C compiler)
- `node` & `npm` (For compiling the Vue 3 WebUI)
- `ecj` & `dx` or `d8` (Java DEX toolchain)
- `zip`

### Build Commands
```bash
# Compile WebUI, native C daemon, Java DEX, and generate flashable ZIP:
./build.sh

# Compile and immediately deploy live to connected rooted device:
./build.sh --deploy
```

The compiled release package will be located at `/sdcard/Download/FPSMoon-v1.0.1.zip`.

---

## Contributing & Issues

Contributions, bug reports, and suggestions are welcome!
- **Bug Reports & Features**: [GitHub Issues](https://github.com/itswill00/fpsmoon/issues)
- **Source Code**: [GitHub Repository](https://github.com/itswill00/fpsmoon)

---

## License

This project is licensed under the [MIT License](LICENSE).

Developed and maintained by **[@itswill00](https://github.com/itswill00)**.
