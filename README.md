<div align="center">

<img src="https://github.com/sam829/simple-logger-overlay-android/blob/main/screenshot/logo.svg?raw=true" width="120" alt="Simple Logger Overlay"/>

# Simple Logger Overlay — Android

**A production-ready in-app debug overlay for Android — Compose, Material 3, zero config.**

### Made with Love & Caffeine by [Sam ♥](https://info.sammacwan.in)

[![CI](https://github.com/sam829/simple-logger-overlay-android/actions/workflows/ci.yml/badge.svg)](https://github.com/sam829/simple-logger-overlay-android/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/in.sammacwan/simple-logger-overlay)](https://central.sonatype.com/artifact/in.sammacwan/simple-logger-overlay)
[![API](https://img.shields.io/badge/min%20SDK-24-brightgreen)](https://developer.android.com/about/versions/nougat)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue)](LICENSE)
[![API Docs](https://img.shields.io/badge/docs-KDoc-orange)](https://sam829.github.io/simple-logger-overlay-android/)

<br/>

*Stop switching to Logcat mid-demo.*
*Logs, network calls, and filters — right inside your app.*
*One dependency. Zero overhead in production.*

</div>

<img src="https://github.com/sam829/simple-logger-overlay-android/blob/main/screenshot/logging-banner.png?raw=true" width="100%" alt="Simple Logger Overlay Banner"/>

---

## ✨ Features

- **📱 In-App Debug Overlay** — view logs without connecting to Logcat
- **🎯 Draggable FAB** — quick access from anywhere in your app
- **🌐 Network Logging** — automatic OkHttp request/response capture
- **🔍 Search & Filter** — by message, tag, level, or HTTP method
- **💾 Flexible Storage** — in-memory or persistent Room storage
- **📤 Export Logs** — share as JSON via Android share sheet
- **🎨 Material 3 UI** — dynamic colour theming + dark mode
- **⚡ Zero Performance Impact** — all I/O on background threads
- **🔒 Production Safe** — single flag disables everything in release
- **📊 JSON Pretty-Printing** — human-readable JSON in log details
- **📳 Shake-to-Open** — open the overlay by shaking the device
- **🕹️ SessionTelemetry** — track timed user flows and milestones

---

## 📸 Screenshots

<table>
  <tr>
    <td align="center"><img src="https://github.com/sam829/simple-logger-overlay-android/blob/main/screenshot/1_log_list.png?raw=true" width="200"/><br/><sub><b>Log List</b></sub></td>
    <td align="center"><img src="https://github.com/sam829/simple-logger-overlay-android/blob/main/screenshot/2_log_detail.png?raw=true" width="200"/><br/><sub><b>Log Detail</b></sub></td>
    <td align="center"><img src="https://github.com/sam829/simple-logger-overlay-android/blob/main/screenshot/3_network_list.png?raw=true" width="200"/><br/><sub><b>Network Logs</b></sub></td>
  </tr>
  <tr>
    <td align="center"><img src="https://github.com/sam829/simple-logger-overlay-android/blob/main/screenshot/4_network_detail.png?raw=true" width="200"/><br/><sub><b>Network Detail</b></sub></td>
    <td align="center"><img src="https://github.com/sam829/simple-logger-overlay-android/blob/main/screenshot/5b_search_results.png?raw=true" width="200"/><br/><sub><b>Search</b></sub></td>
    <td align="center"><img src="https://github.com/sam829/simple-logger-overlay-android/blob/main/screenshot/6_filter.png?raw=true" width="200"/><br/><sub><b>Filter by Level</b></sub></td>
  </tr>
</table>

---

## 🚀 Installation

Add the dependency to your module's `build.gradle.kts`:

```kotlin
dependencies {
    // Only included in debug builds — zero impact on release APK
    debugImplementation("in.sammacwan:simple-logger-overlay:LATEST_VERSION")
}
```

Replace `LATEST_VERSION` with the badge version above, or check [Maven Central](https://central.sonatype.com/artifact/in.sammacwan/simple-logger-overlay).

---

## ⚙️ Initialisation

Call `LoggerOverlay.init()` once in your `Application.onCreate()`:

```kotlin
import com.debugtools.logger.LoggerOverlay
import com.debugtools.logger.config.LoggerConfig

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            LoggerOverlay.init(
                context = this,
                config = LoggerConfig(
                    enabled = true,
                    persistLogs = false,   // in-memory (default)
                    logToConsole = true,
                )
            )
        }
    }
}
```

---

## 📖 Logging

```kotlin
import com.debugtools.logger.LoggerOverlay

LoggerOverlay.v("MyTag", "Verbose message")
LoggerOverlay.d("MyTag", "Debug message", mapOf("key" to "value"))
LoggerOverlay.i("MyTag", "Info message")
LoggerOverlay.w("MyTag", "Warning message")
LoggerOverlay.e("MyTag", "Error message", exception)

// JSON — pretty-printed in the overlay detail view
LoggerOverlay.logJson("API", "User response", """{"id":1,"name":"Ada"}""")

// Structured telemetry
LoggerOverlay.telemetry("Checkout", "payment_submitted", mapOf("amount" to "9.99"))
```

---

## 🌐 Network Logging

Add `OkHttpLoggerInterceptor` to your `OkHttpClient`:

```kotlin
import com.debugtools.logger.network.OkHttpLoggerInterceptor

val client = OkHttpClient.Builder()
    .addInterceptor(OkHttpLoggerInterceptor())
    .build()
```

Network logs appear in the **Network** tab with full request/response details.

> OkHttp is a `compileOnly` dependency — your app must provide it.

---

## 🕹️ SessionTelemetry

Track timed flows across your app:

```kotlin
import com.debugtools.logger.telemetry.SessionTelemetry

// Start a flow
SessionTelemetry.startSession("checkout-42", "CheckoutFlow")

// Log milestones
SessionTelemetry.logMilestone("checkout-42", "payment_submitted")
SessionTelemetry.addMetadata("checkout-42", "amount", "9.99")

// End and log total duration
SessionTelemetry.endSession("checkout-42", success = true)
```

---

## 🔌 Optional Integrations

### Hilt injection observer

```kotlin
import com.debugtools.logger.framework.HiltInjectionObserver

@HiltAndroidApp
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        HiltInjectionObserver.init(this) // call after super.onCreate()
    }
}

// In any @AndroidEntryPoint or @Inject constructor:
HiltInjectionObserver.logInjection(
    componentName = this::class.java.name,
    dependencies = listOf(MyRepository::class.java.name)
)
```

> Hilt is a `compileOnly` dependency — only include if your app uses Hilt.

### WorkManager observer

```kotlin
import com.debugtools.logger.framework.WorkManagerObserver

WorkManagerObserver.init(context) // monitors all queued work automatically
```

> WorkManager is a `compileOnly` dependency — only include if your app uses WorkManager.

---

## ⚙️ Configuration reference

| Property | Type | Default | Description |
|---|---|---|---|
| `enabled` | `Boolean` | `true` | Master switch — set to `BuildConfig.DEBUG` for safety |
| `persistLogs` | `Boolean` | `false` | Store logs in Room (survives process death) |
| `retentionDays` | `Int` | `0` | Days to keep persisted logs; `0` = clear on launch |
| `maxLogsInMemory` | `Int` | `1000` | Max in-memory entries before oldest are evicted |
| `seedColor` | `Color` | Purple | Seed for the overlay's Material 3 colour scheme |
| `enableShakeToOpen` | `Boolean` | `true` | Shake device to open overlay |
| `logToConsole` | `Boolean` | `true` | Mirror logs to Android Logcat |

---

## 📤 Export logs

```kotlin
scope.launch {
    val uri = LoggerOverlay.exportLogs(context)
    if (uri != null) {
        val share = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(share, "Share logs"))
    }
}
```

---

## 🔒 Production safety

```kotlin
// Option A — exclude from release APK entirely (recommended)
debugImplementation("in.sammacwan:simple-logger-overlay:VERSION")

// Option B — disable at runtime
LoggerOverlay.init(context, LoggerConfig(enabled = BuildConfig.DEBUG))
```

---

## 📋 Requirements

| | |
|---|---|
| **Min SDK** | 24 (Android 7.0) |
| **Compile SDK** | 35 |
| **Kotlin** | 2.1.20+ |
| **Jetpack Compose BOM** | 2026.05.00+ |

---

## 📚 API Reference

Full KDoc API reference is published to GitHub Pages on every release:
**https://sam829.github.io/simple-logger-overlay-android/**

---

## 🏗️ Architecture

```
library/src/main/kotlin/in/sammacwan/logger/
├── LoggerOverlay.kt          ← main entry point (object)
├── ShakeDetector.kt          ← accelerometer-based shake detection
├── config/
│   └── LoggerConfig.kt       ← library configuration
├── core/
│   ├── LogEntry.kt           ← log entry model
│   ├── LogLevel.kt           ← severity enum
│   ├── NetworkLogEntry.kt    ← network request/response model
│   └── InternalLogger.kt     ← internal Logcat wrapper
├── framework/
│   ├── HiltInjectionObserver.kt   ← optional Hilt integration
│   └── WorkManagerObserver.kt     ← optional WorkManager integration
├── network/
│   └── OkHttpLoggerInterceptor.kt ← OkHttp interceptor
├── storage/
│   ├── LogStorage.kt         ← storage interface
│   ├── InMemoryLogStorage.kt ← default in-memory implementation
│   ├── RoomLogStorage.kt     ← persistent Room implementation
│   └── ...                   ← Room DAOs and entities
├── telemetry/
│   └── SessionTelemetry.kt   ← timed session tracking
└── ui/                       ← 100% Compose overlay UI
    ├── activity/
    ├── components/
    ├── screens/
    ├── theme/
    └── viewmodel/
```

---

## 📄 License

```
Copyright 2026 Saumya Macwan

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
