# Matareo

<div align="center">
  <img src="logo.png" width="128" height="128" alt="Matareo Logo">
  <h3>Comprehensive Local System Monitor, Benchmark, & Diagnostics Tool</h3>
</div>

## Overview
**Matareo** is an advanced, beautifully designed Android application focused on system diagnostics, hardware monitoring, and performance benchmarking. Built with a modern Jetpack Compose UI, Matareo offers a unified dashboard for tracking system health, running local network/hardware tests, and inspecting deeply into Android internals—all seamlessly wrapped into an intuitive Material Design 3 interface.

## 🚀 Features

### 📊 Real-Time Dashboard
- **CPU & RAM Metrics**: Live tracking of processor frequencies, active cores, and memory usage.
- **Advanced Donut Charts**: Visual layout of storage allocations (System, Apps, Media, Cache) using custom-drawn canvas graphics.
- **Hardware Telemetry**: View device specifications including Battery Health, SoC model, and Thermal sensors.

### 🛠️ The 35+ Tools Suite
Matareo includes a massive toolkit organized into categories for every diagnostic need:
- **Gaming Suite**: Floating Game Overlay (Real-time HUD for FPS, CPU, RAM), Thermal Throttle Check, Game Booster (Kill-all), Multi-touch Tester.
- **Tech & System**: Deep RAM Cleaner, App Manifest Inspector, SELinux Status, Logcat Viewer, Kernel Info.
- **Network & Security**: Ping Tester, DNS/IP Inspector, VPN Status, Traceroute, Root Checker, DRM Info.
- **Daily Diagnostics**: Speaker Cleaner, Dead Pixel Test, Wi-Fi Signal Analyzer, Battery Inspector.
- **Files & Storage**: Mount Point Manager (df -h), Cache Cleaner, SD Card Benchmark.
- **Developer Tools**: Native Local Shell (Terminal Emulator), System Properties Dump.

### ⚙️ Benchmark & Testing
- **Local Stress Testing**: Benchmark CPU operations, GPU floating-point math, RAM I/O, and Storage Speeds locally.
- **Exportable PDF Reports**: Generate and download comprehensive benchmark scores and hardware details directly to a PDF on your device.

### 💻 Local Shell Terminal
- Integrated command-line interface directly in the app.
- Execute underlying Linux commands (like `top`, `ping`, `ip a`, `dumpsys`) right from your phone.

## 📱 Tech Stack
- **Language**: Kotlin 1.9+
- **UI Framework**: Jetpack Compose (Material Design 3)
- **Architecture**: MVVM with Coroutines and Flow
- **Build System**: Gradle Kotlin DSL (.gradle.kts)
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)

## 📥 Installation
1. Clone this repository.
2. Open the project in Android Studio.
3. Sync Gradle and ensure the latest SDK tools are installed.
4. Hit **Run** (`Shift + F10`) to deploy to your device or emulator.

## 🔒 Permissions Used
Matareo requires several permissions depending on the tools you use:
- `SYSTEM_ALERT_WINDOW` (Overlay): Required for the Floating HUD (FPS/CPU/RAM).
- `INTERNET`: For Ping & Network tests.
- `ACCESS_NETWORK_STATE` / `ACCESS_WIFI_STATE`: For Wi-Fi diagnostics.
- `READ_EXTERNAL_STORAGE` / `WRITE_EXTERNAL_STORAGE`: For PDF report generation and storage analysis.
- `BLUETOOTH_CONNECT`: For Bluetooth diagnostics (Android 12+).
- `CAMERA` / `RECORD_AUDIO`: For sensor/mic/camera testing.

## 📄 License
This project is licensed under the MIT License - see the LICENSE file for details.

---
*Built with ❤️ for power users, developers, and tinkerers.*
