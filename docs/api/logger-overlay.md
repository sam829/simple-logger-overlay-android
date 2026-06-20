# LoggerOverlay

`object LoggerOverlay` — the main entry point for the library. All methods are safe to call from any thread.

> Full KDoc: [sam829.github.io/simple-logger-overlay-android](https://sam829.github.io/simple-logger-overlay-android/)

---

## Initialisation

```kotlin
fun init(context: Context, config: LoggerConfig = LoggerConfig())
```

Must be called before any logging. Subsequent calls are ignored.

---

## Logging

```kotlin
fun v(tag: String, message: String, data: Any? = null)
fun d(tag: String, message: String, data: Any? = null)
fun i(tag: String, message: String, data: Any? = null)
fun w(tag: String, message: String, throwable: Throwable? = null)
fun e(tag: String, message: String, throwable: Throwable? = null)
```

```kotlin
// Log JSON — pretty-printed in the overlay detail view
fun logJson(tag: String, message: String, json: String, level: LogLevel = LogLevel.DEBUG)

// Structured telemetry with typed metadata
fun telemetry(tag: String, message: String, metadata: Map<String, String> = emptyMap())
```

### Example

```kotlin
LoggerOverlay.d("Auth", "Login attempt", mapOf("email" to user.email))
LoggerOverlay.e("Payment", "Charge failed", exception)
LoggerOverlay.logJson("API", "User response", responseJson)
```

---

## Overlay control

```kotlin
fun show(context: Context)   // open the overlay UI
fun hide()                   // no-op — overlay closes itself via Activity.finish()
fun enable()                 // re-enable after disable()
fun disable()                // suppress all logging temporarily
fun isEnabled(): Boolean
```

---

## Log management

```kotlin
fun clearLogs()                            // wipe all logs
suspend fun exportLogs(context: Context): Uri?  // export to JSON, returns shareable URI
```

---

## State

```kotlin
val hasUnreadLogs: StateFlow<Boolean>
```

Emits `true` when new logs arrive while the overlay is closed. Observe this to badge your own launcher button:

```kotlin
lifecycleScope.launch {
    LoggerOverlay.hasUnreadLogs.collect { hasNew ->
        fabBadge.isVisible = hasNew
    }
}
```

---

## Overlay visibility gate

```kotlin
var shouldShowOverlay: (() -> Boolean)? = null
```

```kotlin
// Only show overlay when dev mode is on
LoggerOverlay.shouldShowOverlay = { prefs.isDevModeEnabled }
```
