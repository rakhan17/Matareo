# Matareo 📊 (v1.0.0)

<div align="center">

  <img src="logo.png" width="120" height="120" alt="Matareo Logo">

  <h1>M A T A R E O</h1>

  <p><b>Advanced System Telemetry & Diagnostic Environment for Android</b></p>
  
  <p><i>Version 3.9.21</i></p>

  <br>

  <a href="https://github.com/rakhan17/Matareo/releases/latest/download/app-debug.apk">
    <img src="https://img.shields.io/badge/DOWNLOAD_LATEST_RELEASE-000000?style=for-the-badge&logo=android&logoColor=white&labelColor=111111" alt="Download Matareo">
  </a>

  <br><br>

  <p>
    Matareo is a meticulously engineered diagnostic utility designed for system administrators, developers, and hardware professionals. It provides unprecedented visibility into device telemetry, network architecture, and kernel-level performance data through a fluid, modern user interface.
  </p>

</div>

---

## Executive Overview

Matareo consolidates over 35 specialized diagnostic instruments into a single, cohesive environment. Engineered to bypass surface-level metrics, it executes low-level system commands to deliver raw, unfiltered data regarding processor states, thermal dynamics, and memory allocation. It is the definitive tool for absolute device management.

## Core Capabilities

### Real-Time Hardware Telemetry
*   **Processor & Memory Analytics:** Live monitoring of CPU clock speeds, per-core activity, and RAM allocation metrics.
*   **Storage Architecture Visualization:** Advanced canvas-rendered graphics displaying exact partition mapping (System, Applications, Media, and Cache).
*   **Deep System Profiling:** Comprehensive extraction of SoC specifications, battery health parameters, and hardware thermal states.

### Professional Diagnostic Suite
A categorized toolkit designed for precision troubleshooting:
*   **Performance Engineering:** System-wide telemetry overlay (HUD), thermal throttling detection, and multi-point touch latency analysis.
*   **System Integrity:** Memory state management, application manifest inspection, SELinux policy status, and raw Logcat extraction.
*   **Network Intelligence:** Enterprise-grade network routing tests (Ping, Traceroute), DNS/IP inspection, and interface signal analysis.
*   **Hardware Calibration:** Display dead-pixel isolation, acoustic speaker cleaning, and raw sensor polling.
*   **Storage & File Management:** Mount point analysis (`df -h`), persistent cache clearing, and localized APK extraction.

### Local Stress Testing & Benchmarking
*   **Performance Validation:** Push device limits utilizing local CPU operations, GPU floating-point mathematics, and RAM I/O velocity tests.
*   **Automated Reporting:** Generate and export comprehensive hardware capability scores as structured PDF documents directly from the device.

### Native Command-Line Interface
*   **Integrated Shell:** A highly responsive terminal emulator embedded within the application.
*   **Direct Execution:** Run fundamental Linux operations (`top`, `ip a`, `dumpsys`) locally without requiring external desktop bridging.

---

## Technical Architecture

Built upon modern Android development standards to ensure maximum stability and minimal overhead:
*   **Primary Language:** Kotlin 1.9+
*   **Interface Framework:** Jetpack Compose (Material Design 3)
*   **Design Pattern:** MVVM (Model-View-ViewModel) utilizing Coroutines and Flow
*   **Build Environment:** Gradle Kotlin DSL
*   **SDK Compatibility:** Minimum API 24 (Android 7.0) — Target API 34 (Android 14)

---

## Deployment & Installation

**Direct Installation:**
1. Retrieve the latest compiled binary (.apk) via the **Download** button above.
2. Ensure device security settings permit installations from unknown sources.
3. Execute the package installer to deploy Matareo.

**Compilation from Source:**
1. Clone the repository: `git clone https://github.com/rakhan17/Matareo.git`
2. Open the project directory within **Android Studio**.
3. Synchronize Gradle configurations and verify SDK dependencies.
4. Compile and deploy via standard IDE execution.

---

## Security & Privacy Protocol

Matareo operates on a strict principle of transparency and local-only data processing. Permissions are only requested at runtime when actively invoking a specific diagnostic tool.

*   `SYSTEM_ALERT_WINDOW`: Required strictly for rendering the real-time telemetry overlay.
*   `MANAGE_EXTERNAL_STORAGE`: Required for comprehensive storage benchmarking and directory mapping.
*   `CAMERA` / `RECORD_AUDIO`: Invoked exclusively for hardware API probing and calibration tests.
*   `ACCESS_WIFI_STATE` / `BLUETOOTH_CONNECT`: Necessary for network routing and local connectivity diagnostics.

---

## License

This software is distributed under the MIT License. See the `LICENSE` document for comprehensive terms and conditions.

<div align="center">
  <br>
  <p><i>Engineered for precision. Built for professionals.</i></p>
</div>