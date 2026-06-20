package com.debugtools.logger.storage

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.debugtools.logger.core.NetworkLogEntry

@Entity(
    tableName = "network_logs",
    indices = [Index(value = ["timestamp"])]
)
data class NetworkLogEntity(
    @PrimaryKey val id: String,
    val timestamp: Long,
    val method: String,
    val url: String,
    @ColumnInfo(name = "request_headers") val requestHeaders: String? = null,
    @ColumnInfo(name = "request_body") val requestBody: String? = null,
    @ColumnInfo(name = "response_code") val responseCode: Int? = null,
    @ColumnInfo(name = "response_headers") val responseHeaders: String? = null,
    @ColumnInfo(name = "response_body") val responseBody: String? = null,
    val duration: Long? = null,
    val error: String? = null,
) {
    fun toNetworkLogEntry() = NetworkLogEntry(
        id = id,
        timestamp = timestamp,
        method = method,
        url = url,
        requestHeaders = requestHeaders,
        requestBody = requestBody,
        responseCode = responseCode,
        responseHeaders = responseHeaders,
        responseBody = responseBody,
        duration = duration,
        error = error,
    )
    
    companion object {
        fun from(log: NetworkLogEntry) = NetworkLogEntity(
            id = log.id,
            timestamp = log.timestamp,
            method = log.method,
            url = log.url,
            requestHeaders = log.requestHeaders,
            requestBody = log.requestBody,
            responseCode = log.responseCode,
            responseHeaders = log.responseHeaders,
            responseBody = log.responseBody,
            duration = log.duration,
            error = log.error,
        )
    }
}
