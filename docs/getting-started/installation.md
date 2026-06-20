# Installation

## Requirements

| | Minimum |
|---|---|
| Android SDK | 24 (Android 7.0) |
| Kotlin | 2.1.20+ |
| Jetpack Compose BOM | 2026.05.00+ |

---

## Add the dependency

Add to your **app module** `build.gradle.kts`. Use `debugImplementation` so the library is excluded from release APKs entirely.

```kotlin
dependencies {
    debugImplementation("in.sammacwan:simple-logger-overlay:<version>")
}
```

Check the latest version on [Maven Central](https://central.sonatype.com/artifact/in.sammacwan/simple-logger-overlay).

---

## Sync

Run a Gradle sync after adding the dependency:

```bash
./gradlew :app:dependencies
```

---

## Next step

→ [Initialise the library](initialisation.md)
