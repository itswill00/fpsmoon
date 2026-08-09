# 🌙 FPSMoon

> Ultra High-Performance Real-Time System Telemetry & Canvas Overlay Engine for Android (Magisk / KernelSU / APatch)

Developed by **[@itswill00](https://github.com/itswill00)**

---

## ⚡ Highlights & Features

- **Zero-Latency Telemetry Engine (C-Daemon)**:
  - Real-time FPS & Frame Time calculation across Qualcomm Snapdragon, MediaTek (FPSGO / GED), Samsung Exynos, Google Tensor, and UNISOC.
  - Automatic Screen Refresh Rate (Hz) detection (60Hz, 90Hz, 120Hz, 144Hz).
  - CPU & GPU frequency, load %, temperature, and governor policy monitoring.
  - Memory (RAM & ZRAM), Battery power usage (Watt, mA, mV, °C), and Network speed (Download / Upload).

- **Hardware-Accelerated Java Overlay Engine (`app_process`)**:
  - Smooth glassmorphism canvas rendering.
  - Zero-heap allocation during rendering ticks (Zero Garbage Collection micro-stutters).
  - **Smart Auto-Fitting**: Dynamic width and height calculations that adjust to font size, scale, and active metric chips.
  - Drag-and-drop position saving with instant UI sync.

- **Embedded Web Control Panel**:
  - Integrated settings controller for customization: themes, opacity, scale, font size, alignment, and metrics toggles.
  - Real-time **Refresh Interval / Polling Interval** slider (100ms - 1000ms).

---

## 🛠️ Structure

- `src/fpsmoon_daemon.c`: Native C telemetry daemon source code.
- `src/FPSMoonOverlay.java`: Java `app_process` overlay engine source code.
- `bin/fpsmoon_daemon`: Compiled native C daemon binary.
- `bin/fpsmoon.dex`: Compiled DEX overlay binary.
- `webroot/`: Web control panel interface (`index.html`, `styles.css`, `app.js`).
- `service.sh` & `action.sh`: Automatic Magisk / KernelSU startup scripts.

---

## 📦 Installation

1. Download `FPSMoon-v1.0.0.zip` from Releases.
2. Flash via **Magisk**, **KernelSU**, or **APatch**.
3. Reboot device.

---

## 📄 License

Developed & Maintained by **[@itswill00](https://github.com/itswill00)**.
