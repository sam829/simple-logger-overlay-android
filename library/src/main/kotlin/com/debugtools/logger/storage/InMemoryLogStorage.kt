package com.debugtools.logger.storage

import com.debugtools.logger.core.LogEntry
import com.debugtools.logger.core.LogLevel
import com.debugtools.logger.core.NetworkLogEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class InMemoryLogStorage(private val maxLogs: Int = 1000) : LogStorage {
    
    private val logs = MutableStateFlow<List<LogEntry>>(emptyList())
    private val networkLogs = MutableStateFlow<List<NetworkLogEntry>>(emptyList())
    private var nextLogId = 1L
    
    override fun getLogs(): Flow<List<LogEntry>> = logs
    
    override fun getNetworkLogs(): Flow<List<NetworkLogEntry>> = networkLogs
    
    override suspend fun addLog(log: LogEntry) {
        val logWithId = log.copy(id = nextLogId++)
        val currentLogs = logs.value.toMutableList()
        currentLogs.add(0, logWithId)
        if (currentLogs.size > maxLogs) {
            currentLogs.removeAt(currentLogs.size - 1)
        }
        logs.value = currentLogs
    }
    
    override suspend fun addNetworkLog(log: NetworkLogEntry) {
        val currentLogs = networkLogs.value.toMutableList()
        currentLogs.add(0, log)
        if (currentLogs.size > maxLogs) {
            currentLogs.removeAt(currentLogs.size - 1)
        }
        networkLogs.value = currentLogs
    }
    
    override suspend fun getNetworkLogById(id: String): NetworkLogEntry? {
        return networkLogs.value.firstOrNull { it.id == id }
    }
    
    override suspend fun updateNetworkLog(log: NetworkLogEntry) {
        val currentLogs = networkLogs.value.toMutableList()
        val index = currentLogs.indexOfFirst { it.id == log.id }
        if (index != -1) {
            currentLogs[index] = log
            // Force emission by creating a new list reference
            networkLogs.value = currentLogs.toList()
        }
    }
    
    override suspend fun search(query: String): Flow<List<LogEntry>> {
        return logs.map { logList ->
            logList.filter {
                it.message.contains(query, ignoreCase = true) ||
                it.tag.contains(query, ignoreCase = true)
            }
        }
    }
    
    override suspend fun searchNetwork(query: String): Flow<List<NetworkLogEntry>> {
        return networkLogs.map { logList ->
            logList.filter {
                it.url.contains(query, ignoreCase = true) ||
                it.method.contains(query, ignoreCase = true)
            }
        }
    }
    
    override suspend fun filter(levels: List<LogLevel>): Flow<List<LogEntry>> {
        return logs.map { logList ->
            logList.filter { it.level in levels }
        }
    }
    
    override suspend fun filterNetwork(success: Boolean?): Flow<List<NetworkLogEntry>> {
        return networkLogs.map { logList ->
            when (success) {
                true -> logList.filter { it.responseCode in 200..299 }
                false -> logList.filter { it.responseCode != null && it.responseCode !in 200..299 }
                null -> logList
            }
        }
    }
    
    override suspend fun clear() {
        logs.value = emptyList()
        networkLogs.value = emptyList()
    }
    
    override suspend fun deleteOlderThan(days: Int) {
        val cutoffTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        logs.value = logs.value.filter { it.timestamp >= cutoffTime }
        networkLogs.value = networkLogs.value.filter { it.timestamp >= cutoffTime }
    }
}
