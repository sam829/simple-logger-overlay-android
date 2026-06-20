# SessionTelemetry

`object SessionTelemetry` — tracks timed user flows with milestones and metadata. Useful for measuring the duration and outcome of any named sequence of events (checkout, onboarding, multi-step form, etc.).

> Full KDoc: [sam829.github.io/simple-logger-overlay-android](https://sam829.github.io/simple-logger-overlay-android/)

---

## Basic usage

```kotlin
import com.debugtools.logger.telemetry.SessionTelemetry

// 1. Start
SessionTelemetry.startSession(
    sessionId = "checkout-42",
    name = "CheckoutFlow",
    metadata = mapOf("cartItems" to 3)
)

// 2. Log milestones as the user progresses
SessionTelemetry.logMilestone("checkout-42", "address_entered")
SessionTelemetry.logMilestone("checkout-42", "payment_submitted")

// 3. Attach dynamic metadata at any point
SessionTelemetry.addMetadata("checkout-42", "paymentMethod", "card")

// 4. End — logs total duration automatically
SessionTelemetry.endSession("checkout-42", success = true)
```

---

## API

```kotlin
fun startSession(sessionId: String, name: String, metadata: Map<String, Any> = emptyMap())
```
Starts tracking. `sessionId` must be unique per active session.

```kotlin
fun endSession(sessionId: String, success: Boolean = true)
```
Ends the session and logs `durationMs`, `durationSeconds`, and final metadata.

```kotlin
fun logMilestone(sessionId: String, milestone: String, metadata: Map<String, Any> = emptyMap())
```
Logs a named checkpoint with elapsed time since session start.

```kotlin
fun addMetadata(sessionId: String, key: String, value: Any)
```
Attaches or updates a key-value pair on an active session.

```kotlin
fun getActiveSessions(): List<Session>
```
Returns all currently active sessions.

```kotlin
fun clearAllSessions()
```
Clears all in-memory session state (useful in tests).

---

## Session data class

```kotlin
data class Session(
    val sessionId: String,
    val name: String,
    val startTime: Long,
    var endTime: Long?,
    val metadata: MutableMap<String, Any>
) {
    fun duration(): Long?  // null while active
}
```
