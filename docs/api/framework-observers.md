# Framework Observers

Optional utilities for logging framework-level events. Both are `compileOnly` — only include if your app uses the respective framework.

> Full KDoc: [sam829.github.io/simple-logger-overlay-android](https://sam829.github.io/simple-logger-overlay-android/)

---

## HiltInjectionObserver

Logs Hilt dependency injection events — useful for debugging injection graphs and spotting unexpected re-injections.

### Setup

Call `init` after `super.onCreate()` in your `@HiltAndroidApp` application class:

```kotlin
import com.debugtools.logger.framework.HiltInjectionObserver

@HiltAndroidApp
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()   // Hilt initialises here
        HiltInjectionObserver.init(this)
    }
}
```

### Log an injection

Call from any `@AndroidEntryPoint` screen or injected component:

```kotlin
HiltInjectionObserver.logInjection(
    componentName = this::class.java.name,
    dependencies = listOf(MyRepository::class.java.name)
)
// → DEBUG [Hilt] ✅ Injected: MainActivity [deps: MyRepository]
```

### Log a provision

Call from `@Provides` methods:

```kotlin
HiltInjectionObserver.logProvision(
    moduleName = NetworkModule::class.java.name,
    providedType = OkHttpClient::class.java.name
)
// → DEBUG [Hilt] 📦 Provided: OkHttpClient from NetworkModule
```

---

## WorkManagerObserver

Automatically observes all WorkManager task state changes and logs them.

### Setup

```kotlin
import com.debugtools.logger.framework.WorkManagerObserver

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        WorkManagerObserver.init(this)  // starts observing immediately
    }
}
```

### Manual scheduling log

```kotlin
WorkManagerObserver.logWorkScheduled(
    workerName = "SyncWorker",
    isPeriodic = true
)
// → DEBUG [WorkManager] 📅 Scheduled Periodic: SyncWorker
```

### State log format

State changes are logged automatically with an emoji, worker name, state, and attempt count:

```
▶️ SyncWorker | RUNNING | Attempt: 1
✅ SyncWorker | SUCCEEDED | Attempt: 1
❌ SyncWorker | FAILED | Attempt: 2
```

- `FAILED` → logged at ERROR level
- `CANCELLED` → logged at WARN level
- `SUCCEEDED` → logged at INFO level
- All others → DEBUG
