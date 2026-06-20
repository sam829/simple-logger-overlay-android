# Production Safety

The library is designed to have **zero impact on production builds** when used correctly.

---

## Option A — `debugImplementation` (recommended)

The cleanest approach: the library is excluded from the release APK entirely. No code, no resources, no overhead.

```kotlin
// app/build.gradle.kts
dependencies {
    debugImplementation("in.sammacwan:simple-logger-overlay:<version>")
}
```

When your release build calls `LoggerOverlay.show()` or any other method, it will call the no-op release stub (if you provide one) or fail to compile — which forces you to guard calls with `BuildConfig.DEBUG`.

### Guarding call sites

```kotlin
if (BuildConfig.DEBUG) {
    LoggerOverlay.show(context)
}
```

Or wrap in a helper:

```kotlin
object DebugLogger {
    fun show(context: Context) {
        if (BuildConfig.DEBUG) LoggerOverlay.show(context)
    }
    fun d(tag: String, msg: String) {
        if (BuildConfig.DEBUG) LoggerOverlay.d(tag, msg)
    }
}
```

---

## Option B — runtime flag

If you need a single dependency declaration (e.g. for internal testing builds), disable via config:

```kotlin
LoggerOverlay.init(
    context = this,
    config = LoggerConfig(enabled = BuildConfig.DEBUG)
)
```

When `enabled = false`, every method returns immediately with no allocations or I/O.

---

## Checklist before release

- [ ] Library declared as `debugImplementation` **or** `enabled = BuildConfig.DEBUG`
- [ ] No `LoggerOverlay.*` calls outside `BuildConfig.DEBUG` guards (if using `debugImplementation`)
- [ ] Shake-to-open disabled in builds delivered to users (`enableShakeToOpen = false`)
- [ ] No sensitive data (tokens, passwords, PII) logged — logs can be exported and shared

---

## What the library does NOT do in release

When `enabled = false`:
- No background threads started
- No Room database created
- No accelerometer listener registered
- No FAB added to the window
- No memory allocated for log storage
