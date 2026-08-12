# Matareo 📊 (v3.9.21)

<div align="center">
  <img src="logo.png" width="128" height="128" alt="Matareo Logo">
  <h3>The Ultimate Local System Monitor, Benchmark, & Diagnostics Tool for Android</h3>
  <br>
  <br>
  <a href="https://github.com/rakhan17/Matareo/releases/latest/download/app-debug.apk">
    <img src="https://img.shields.io/badge/Download_APK-GitHub-black?style=for-the-badge&logo=github" height="60" alt="Download Matareo APK">
  </a>
  <br>
  <br>
  <p>
    <b>Matareo</b> is a beautifully crafted, comprehensive Android application designed for power users, developers, and tech enthusiasts. 
    It provides an in-depth view of your system's health, hardware telemetry, network diagnostics, and performance benchmarks, all neatly organized within a modern Jetpack Compose UI (Material Design 3).
  </p>
</div>

---

## ✨ Why Matareo?

Whether you are debugging a background process, checking thermal throttling while gaming, or inspecting advanced hardware telemetry, Matareo consolidates 35+ specialized diagnostic tools into one seamless experience. No need for multiple clunky apps—Matareo gives you absolute control over your Android device.

## 🚀 Key Features

### 📈 Real-Time Hardware Dashboard
- **CPU & RAM Telemetry**: Live tracking of processor frequencies, active cores, and memory usage.
- **Interactive Storage Analytics**: Advanced Donut Charts visually map storage allocations (System, Apps, Media, Cache) using custom-drawn canvas graphics.
- **Deep System Info**: Inspect detailed specifications including Battery Health, SoC model, and Thermal sensor states.

### 🛠️ The 35+ Tools Suite
A massive toolkit organized into categories for every diagnostic need, executing real low-level Android commands:
- **🎮 Gaming Suite**: Floating Game Overlay (Real-time HUD for FPS, CPU, RAM), Thermal Throttle Check, Game Booster, Multi-touch Tester.
- **⚙️ Tech & System**: Deep RAM Cleaner, App Manifest Inspector, SELinux Status, Logcat Viewer, Kernel Info.
- **🌐 Network & Security**: Ping Tester, DNS/IP Inspector, VPN Status, Traceroute, Root Checker, DRM Info.
- **🩺 Daily Diagnostics**: Speaker Cleaner, Dead Pixel Test, Wi-Fi Signal Analyzer, Battery Inspector, Sensor Latency Test.
- **📁 Files & Storage**: Mount Point Manager (df -h), Cache Cleaner, SD Card Benchmark, APK Extractor.
- **💻 Developer Tools**: Native Local Shell (Terminal Emulator), System Properties Dump.

### ⏱️ Local Benchmarking
- **Stress Testing**: Push your device to the limit with local CPU operations, GPU floating-point math, RAM I/O, and Storage Speed tests.
- **Exportable PDF Reports**: Generate comprehensive benchmark scores and hardware details directly to a PDF on your device.

### ⌨️ Local Shell Terminal
- **Integrated CLI**: A built-in terminal emulator directly in the app.
- **Linux Power**: Execute underlying Linux commands (like `top`, `ping`, `ip a`, `dumpsys`, `df -h`) right from your phone.

---

## 📱 Tech Stack
Built with modern Android development standards:
- **Language**: Kotlin 1.9+
- **UI Framework**: Jetpack Compose (Material Design 3)
- **Architecture**: MVVM with Coroutines and Flow
- **Build System**: Gradle Kotlin DSL (.gradle.kts)
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)

---

## 📥 Installation & Setup

1. **Download the APK** from the badge above or the [Releases](https://github.com/rakhan17/Matareo/releases) page.
2. Ensure you have "Install from Unknown Sources" enabled on your device.
3. Install the APK and launch **Matareo**.

### Building from Source
1. Clone this repository: `git clone https://github.com/rakhan17/Matareo.git`
2. Open the project in **Android Studio**.
3. Sync Gradle and ensure the latest SDK tools are installed.
4. Hit **Run** (`Shift + F10`) to deploy to your device or emulator.

---

## 🔒 Permissions Breakdown
Matareo only requests permissions when a specific tool needs them. If a tool requires permission, you will be redirected to the App Settings to seamlessly enable it.
- **`SYSTEM_ALERT_WINDOW`**: Required for the Floating HUD overlay.
- **`MANAGE_EXTERNAL_STORAGE` / `READ_EXTERNAL_STORAGE`**: For advanced storage benchmarks and APK extraction.
- **`CAMERA` / `RECORD_AUDIO`**: For hardware sensor and API probing.
- **`BLUETOOTH_CONNECT`**: For local Bluetooth diagnostic tests.
- **`ACCESS_WIFI_STATE`**: For network interface inspection.

---

## 📄 License
This project is licensed under the MIT License - see the LICENSE file for details.

---
<div align="center">
  <i>Built with ❤️ for power users, developers, and tinkerers.</i>
</div>
