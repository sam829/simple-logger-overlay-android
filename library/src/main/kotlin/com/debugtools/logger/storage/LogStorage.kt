package com.debugtools.logger.storage

import com.debugtools.logger.core.LogEntry
import com.debugtools.logger.core.LogLevel
import com.debugtools.logger.core.NetworkLogEntry
import kotlinx.coroutines.flow.Flow

/**
 * Storage abstraction for log and network-log entries.
 *
 * Two implementations are provided out of the box:
 * - [InMemoryLogStorage] — fast, cleared on app restart.
 * - [RoomLogStorage] — persistent, backed by a Room database.
 *
 * The active implementation is selected automatically by [in.sammacwan.logger.LoggerOverlay.init]
 * based on [in.sammacwan.logger.config.LoggerConfig.persistLogs].
 */
interface LogStorage {
    /** Returns a [Flow] that emits the current list of log entries and updates on change. */
    fun getLogs(): Flow<List<LogEntry>>

    /** Returns a [Flow] that emits the current list of network log entries and updates on change. */
    fun getNetworkLogs(): Flow<List<NetworkLogEntry>>

    /** Persists a new log entry. */
    suspend fun addLog(log: LogEntry)

    /** Persists a new network log entry (typically just the request at this point). */
    suspend fun addNetworkLog(log: NetworkLogEntry)

    /** Retrieves a network log entry by its [id], or null if not found. */
    suspend fun getNetworkLogById(id: String): NetworkLogEntry?

    /** Updates an existing network log entry (used to attach the response or error). */
    suspend fun updateNetworkLog(log: NetworkLogEntry)

    /** Returns logs whose [LogEntry.tag] or [LogEntry.message] contains [query] (case-insensitive). */
    suspend fun search(query: String): Flow<List<LogEntry>>

    /** Returns network logs whose URL or method contains [query] (case-insensitive). */
    suspend fun searchNetwork(query: String): Flow<List<NetworkLogEntry>>

    /** Returns logs filtered to the given [levels]. */
    suspend fun filter(levels: List<LogLevel>): Flow<List<LogEntry>>

    /**
     * Returns network logs filtered by success state.
     * - `true` → only successful responses (2xx).
     * - `false` → only errors / non-2xx responses.
     * - `null` → all entries.
     */
    suspend fun filterNetwork(success: Boolean?): Flow<List<NetworkLogEntry>>

    /** Deletes all stored log and network log entries. */
    suspend fun clear()

    /** Deletes log entries older than [days] days. */
    suspend fun deleteOlderThan(days: Int)
}
