# LoggerConfig

`data class LoggerConfig` — passed to `LoggerOverlay.init()` to configure library behaviour.

> Full KDoc: [sam829.github.io/simple-logger-overlay-android](https://sam829.github.io/simple-logger-overlay-android/)

---

## Properties

| Property | Type | Default | Description |
|---|---|---|---|
| `enabled` | `Boolean` | `true` | Master switch. Set to `BuildConfig.DEBUG` to disable in production. |
| `persistLogs` | `Boolean` | `false` | Store logs in Room database (survives process death). |
| `retentionDays` | `Int` | `0` | Days to retain persisted logs. `0` = clear on each app launch. |
| `maxLogsInMemory` | `Int` | `1000` | Max in-memory entries before oldest are evicted. |
| `seedColor` | `Color` | Purple | Seed for the overlay's Material 3 dynamic colour scheme. |
| `enableShakeToOpen` | `Boolean` | `true` | Open the overlay by shaking the device. |
| `logToConsole` | `Boolean` | `true` | Mirror every log entry to Android Logcat. |

---

## Storage modes

### In-memory (default)

Fast and lightweight. Logs are cleared when the app process is killed.

```kotlin
LoggerConfig(
    persistLogs = false,
    retentionDays = 0,
    maxLogsInMemory = 500
)
```

### Persistent (Room)

Logs survive restarts. Useful for reproducing bugs that require multiple app sessions.

```kotlin
LoggerConfig(
    persistLogs = true,
    retentionDays = 7   // auto-purge after 7 days
)
```

---

## Theming

The overlay UI adapts to any seed colour via Material 3 dynamic colour:

```kotlin
LoggerConfig(
    seedColor = Color(0xFF00897B)  // teal
)
```
