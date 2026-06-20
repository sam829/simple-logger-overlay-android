package com.debugtools.logger.storage

import com.debugtools.logger.core.InternalLogger
import com.debugtools.logger.core.LogEntry
import com.debugtools.logger.core.LogLevel
import com.debugtools.logger.core.NetworkLogEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class RoomLogStorage(private val database: LogDatabase) : LogStorage {
    
    override fun getLogs(): Flow<List<LogEntry>> {
        return database.logDao().getAllPaged(1000)
            .map { entities -> entities.map { it.toLogEntry() } }
            .catch { e ->
                InternalLogger.e("Failed to get logs", e)
                emit(emptyList())
            }
            .flowOn(Dispatchers.IO)
    }
    
    override fun getNetworkLogs(): Flow<List<NetworkLogEntry>> {
        return database.networkLogDao().getAllPaged(1000)
            .map { entities -> entities.map { it.toNetworkLogEntry() } }
            .catch { e ->
                InternalLogger.e("Failed to get network logs", e)
                emit(emptyList())
            }
            .flowOn(Dispatchers.IO)
    }
    
    override suspend fun addLog(log: LogEntry) = withContext(Dispatchers.IO) {
        try {
            database.logDao().insert(LogEntity.from(log))
        } catch (e: Exception) {
            InternalLogger.e("Failed to add log", e)
        }
    }
    
    override suspend fun addNetworkLog(log: NetworkLogEntry) = withContext(Dispatchers.IO) {
        try {
            database.networkLogDao().insert(NetworkLogEntity.from(log))
        } catch (e: Exception) {
            InternalLogger.e("Failed to add network log", e)
        }
    }
    
    override suspend fun getNetworkLogById(id: String): NetworkLogEntry? = withContext(Dispatchers.IO) {
        return@withContext try {
            database.networkLogDao().getById(id)?.toNetworkLogEntry()
        } catch (e: Exception) {
            InternalLogger.e("Failed to get network log by id", e)
            null
        }
    }
    
    override suspend fun updateNetworkLog(log: NetworkLogEntry) = withContext(Dispatchers.IO) {
        try {
            database.networkLogDao().update(NetworkLogEntity.from(log))
        } catch (e: Exception) {
            InternalLogger.e("Failed to update network log", e)
        }
    }
    
    override suspend fun search(query: String): Flow<List<LogEntry>> {
        return database.logDao().search(query)
            .map { entities -> entities.map { it.toLogEntry() } }
            .catch { e ->
                InternalLogger.e("Failed to search logs", e)
                emit(emptyList())
            }
            .flowOn(Dispatchers.IO)
    }
    
    override suspend fun searchNetwork(query: String): Flow<List<NetworkLogEntry>> {
        return database.networkLogDao().search(query)
            .map { entities -> entities.map { it.toNetworkLogEntry() } }
            .catch { e ->
                InternalLogger.e("Failed to search network logs", e)
                emit(emptyList())
            }
            .flowOn(Dispatchers.IO)
    }
    
    override suspend fun filter(levels: List<LogLevel>): Flow<List<LogEntry>> {
        return database.logDao().getByLevels(levels.map { it.name })
            .map { entities -> entities.map { it.toLogEntry() } }
            .catch { e ->
                InternalLogger.e("Failed to filter logs", e)
                emit(emptyList())
            }
            .flowOn(Dispatchers.IO)
    }
    
    override suspend fun filterNetwork(success: Boolean?): Flow<List<NetworkLogEntry>> {
        return when (success) {
            true -> database.networkLogDao().getSuccessful()
            false -> database.networkLogDao().getErrors()
            null -> database.networkLogDao().getAllPaged(1000)
        }.map { entities -> entities.map { it.toNetworkLogEntry() } }
            .catch { e ->
                InternalLogger.e("Failed to filter network logs", e)
                emit(emptyList())
            }
            .flowOn(Dispatchers.IO)
    }
    
    override suspend fun clear() = withContext(Dispatchers.IO) {
        try {
            database.logDao().deleteAll()
            database.networkLogDao().deleteAll()
        } catch (e: Exception) {
            InternalLogger.e("Failed to clear logs", e)
        }
    }
    
    override suspend fun deleteOlderThan(days: Int) = withContext(Dispatchers.IO) {
        try {
            val cutoffTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(days.toLong())
            database.logDao().deleteOlderThan(cutoffTime)
            database.networkLogDao().deleteOlderThan(cutoffTime)
        } catch (e: Exception) {
            InternalLogger.e("Failed to delete old logs", e)
        }
    }
}
