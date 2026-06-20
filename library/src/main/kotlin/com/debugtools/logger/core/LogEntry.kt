package com.debugtools.logger.core

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A single structured log entry captured by [in.sammacwan.logger.LoggerOverlay].
 *
 * @property id Auto-generated primary key (Room). Zero for in-memory entries.
 * @property timestamp Wall-clock time in milliseconds since epoch.
 * @property level Severity of the log message.
 * @property tag Source identifier (e.g. `"LoginViewModel"`, `"PaymentFlow"`).
 * @property message Human-readable log message.
 * @property data Optional JSON-encoded or plain-text supplementary data.
 * @property throwable Optional stack trace string, set when an exception is logged.
 * @property metadata Optional key-value pairs for telemetry or structured logging.
 */
data class LogEntry(
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val level: LogLevel,
    val tag: String,
    val message: String,
    val data: String? = null,
    val throwable: String? = null,
    val metadata: Map<String, String>? = null,
)
