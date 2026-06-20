# Initialisation

Call `LoggerOverlay.init()` **once** in your `Application.onCreate()`. The library is safe to guard behind `BuildConfig.DEBUG` — it becomes a complete no-op if `enabled = false`.

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
                    persistLogs = false,     // in-memory (default) — fast, cleared on restart
                    logToConsole = true,     // also mirror to Logcat
                    enableShakeToOpen = true // shake device to open overlay
                )
            )
        }
    }
}
```

Register `MyApp` in your `AndroidManifest.xml`:

```xml
<application
    android:name=".MyApp"
    ...>
```

---

## Persistent storage

To keep logs across app restarts (useful for reproducing bugs):

```kotlin
LoggerConfig(
    persistLogs = true,
    retentionDays = 3   // keep logs for 3 days, then auto-purge
)
```

---

## Opening the overlay

The library adds a **draggable FAB** automatically. Tap it, or call:

```kotlin
LoggerOverlay.show(context)
```

You can also shake the device if `enableShakeToOpen = true`.

---

## Next step

→ [LoggerOverlay API](../api/logger-overlay.md)
