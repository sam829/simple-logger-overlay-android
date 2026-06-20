package com.debugtools.logger.telemetry

import com.debugtools.logger.LoggerOverlay

/**
 * Generic session-based telemetry utility for tracking timed flows and milestones.
 *
 * Useful for measuring the duration and outcome of any named user flow — onboarding,
 * checkout, a game round, a multi-step form, etc.
 *
 * Usage:
 * ```kotlin
 * SessionTelemetry.startSession("checkout-42", "CheckoutFlow")
 * SessionTelemetry.logMilestone("checkout-42", "payment_submitted")
 * SessionTelemetry.endSession("checkout-42", success = true)
 * ```
 */
object SessionTelemetry {

    private val sessions = mutableMapOf<String, Session>()

    /**
     * Represents a single tracked session with timing and arbitrary metadata.
     *
     * @property sessionId Unique identifier for this session.
     * @property name Human-readable name of the flow being tracked.
     * @property startTime Wall-clock start time in milliseconds since epoch.
     * @property endTime Wall-clock end time, set when [endSession] is called.
     * @property metadata Arbitrary key-value pairs attached to this session.
     */
    data class Session(
        val sessionId: String,
        val name: String,
        val startTime: Long,
        var endTime: Long? = null,
        val metadata: MutableMap<String, Any> = mutableMapOf(),
    ) {
        /** Returns the elapsed duration in milliseconds, or null if the session is still active. */
        fun duration(): Long? = endTime?.let { it - startTime }
    }

    /**
     * Starts a new named session.
     *
     * @param sessionId Unique ID for this session. Use a stable, collision-safe value
     *   (e.g. a UUID or a flow-name + timestamp).
     * @param name Human-readable name of the flow (e.g. `"CheckoutFlow"`).
     * @param metadata Optional initial key-value metadata to attach to the session.
     */
    fun startSession(sessionId: String, name: String, metadata: Map<String, Any> = emptyMap()) {
        val session = Session(
            sessionId = sessionId,
            name = name,
            startTime = System.currentTimeMillis(),
            metadata = metadata.toMutableMap(),
        )
        sessions[sessionId] = session

        LoggerOverlay.i(
            tag = "SessionTelemetry",
            message = "Session started: $name",
            data = mapOf(
                "sessionId" to sessionId,
                "name" to name,
                "metadata" to metadata,
            ),
        )
    }

    /**
     * Ends an active session and logs its total duration.
     *
     * @param sessionId The ID passed to [startSession].
     * @param success Whether the session completed successfully.
     */
    fun endSession(sessionId: String, success: Boolean = true) {
        val session = sessions[sessionId] ?: run {
            LoggerOverlay.w("SessionTelemetry", "Attempted to end non-existent session: $sessionId")
            return
        }

        session.endTime = System.currentTimeMillis()
        val duration = session.duration() ?: 0

        val message = "Session ended: ${session.name}"
        val data = mapOf(
            "sessionId" to sessionId,
            "name" to session.name,
            "durationMs" to duration,
            "durationSeconds" to duration / 1000.0,
            "success" to success,
            "metadata" to session.metadata,
        )

        if (success) {
            LoggerOverlay.i(tag = "SessionTelemetry", message = message, data = data)
        } else {
            LoggerOverlay.w(tag = "SessionTelemetry", message = message)
        }

        sessions.remove(sessionId)
    }

    /**
     * Attaches or updates a single metadata value on an active session.
     *
     * @param sessionId The ID of the active session.
     * @param key Metadata key.
     * @param value Metadata value (any serialisable type).
     */
    fun addMetadata(sessionId: String, key: String, value: Any) {
        sessions[sessionId]?.metadata?.put(key, value)
    }

    /**
     * Logs a named milestone within an active session, including elapsed time since start.
     *
     * @param sessionId The ID of the active session.
     * @param milestone Name of the milestone (e.g. `"form_submitted"`, `"api_responded"`).
     * @param metadata Optional additional data to log with this milestone.
     */
    fun logMilestone(sessionId: String, milestone: String, metadata: Map<String, Any> = emptyMap()) {
        val session = sessions[sessionId] ?: run {
            LoggerOverlay.w("SessionTelemetry", "Milestone logged for non-existent session: $sessionId")
            return
        }

        val elapsed = System.currentTimeMillis() - session.startTime

        LoggerOverlay.d(
            tag = "SessionTelemetry",
            message = "Milestone: $milestone",
            data = mapOf(
                "sessionId" to sessionId,
                "name" to session.name,
                "milestone" to milestone,
                "elapsedMs" to elapsed,
                "elapsedSeconds" to elapsed / 1000.0,
                "metadata" to metadata,
            ),
        )
    }

    /**
     * Returns a snapshot of all currently active (not yet ended) sessions.
     */
    fun getActiveSessions(): List<Session> = sessions.values.toList()

    /**
     * Clears all in-memory session state. Useful in tests or when resetting app state.
     */
    fun clearAllSessions() {
        sessions.clear()
        LoggerOverlay.d("SessionTelemetry", "All sessions cleared")
    }
}
