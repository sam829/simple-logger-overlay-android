package com.debugtools.logger.storage

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.debugtools.logger.core.LogEntry
import com.debugtools.logger.core.LogLevel

@Entity(tableName = "logs")
data class LogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    @ColumnInfo(name = "level") val level: String,
    val tag: String,
    val message: String,
    val data: String? = null,
    val throwable: String? = null,
    val metadata: String? = null,
) {
    fun toLogEntry() = LogEntry(
        id = id,
        timestamp = timestamp,
        level = LogLevel.valueOf(level),
        tag = tag,
        message = message,
        data = data,
        throwable = throwable,
        metadata = metadata?.let { parseMetadata(it) },
    )
    
    private fun parseMetadata(json: String): Map<String, String> {
        return try {
            json.trim('{', '}')
                .split(",")
                .mapNotNull { pair ->
                    val parts = pair.split("=", limit = 2)
                    if (parts.size == 2) parts[0].trim() to parts[1].trim()
                    else null
                }
                .toMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }
    
    companion object {
        fun from(log: LogEntry) = LogEntity(
            id = log.id,
            timestamp = log.timestamp,
            level = log.level.name,
            tag = log.tag,
            message = log.message,
            data = log.data,
            throwable = log.throwable,
            metadata = log.metadata?.let { serializeMetadata(it) },
        )
        
        private fun serializeMetadata(map: Map<String, String>): String {
            return map.entries.joinToString(",", "{", "}") { "${it.key}=${it.value}" }
        }
    }
}
