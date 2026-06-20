package com.debugtools.logger.network

import com.debugtools.logger.LoggerOverlay
import com.debugtools.logger.core.NetworkLogEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import java.util.UUID

/**
 * OkHttp [Interceptor] that automatically captures all HTTP requests and responses and
 * forwards them to [LoggerOverlay] for display in the overlay's **Network** tab.
 *
 * Add it to your [okhttp3.OkHttpClient] during initialisation:
 *
 * ```kotlin
 * val client = OkHttpClient.Builder()
 *     .addInterceptor(OkHttpLoggerInterceptor())
 *     .build()
 * ```
 *
 * **OkHttp is a `compileOnly` dependency** of this library — the host app must provide it.
 * The interceptor is a no-op if [LoggerOverlay] is disabled.
 *
 * Response bodies larger than 1 MB are truncated to protect memory.
 */
class OkHttpLoggerInterceptor : Interceptor {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    companion object {
        // Maximum body size to log (1 MB - reasonable for debug logging)
        private const val MAX_BODY_SIZE = 1 * 1024 * 1024L
        private const val TRUNCATION_MESSAGE = "\n\n... [Response truncated - exceeded 1MB limit]"
    }
    
    override fun intercept(chain: Interceptor.Chain): Response {
        if (!LoggerOverlay.isEnabled()) {
            return chain.proceed(chain.request())
        }
        
        val request = chain.request()
        val requestId = UUID.randomUUID().toString()
        val startTime = System.currentTimeMillis()
        
        val requestBody = try {
            request.body?.let { body ->
                // Limit request body reading to MAX_BODY_SIZE
                if (body.contentLength() > MAX_BODY_SIZE) {
                    "[Request body too large: ${body.contentLength()} bytes - not logged]"
                } else {
                    val buffer = Buffer()
                    body.writeTo(buffer)
                    buffer.readUtf8()
                }
            }
        } catch (e: Exception) {
            "[Body read error: ${e.message}]"
        }
        
        val requestHeaders = request.headers.toMultimap()
            .mapValues { it.value.joinToString(", ") }
        
        // Log the request
        LoggerOverlay.logRequest(
            id = requestId,
            method = request.method,
            url = request.url.toString(),
            headers = requestHeaders,
            body = requestBody
        )
        
        return try {
            val response = chain.proceed(request)
            val duration = System.currentTimeMillis() - startTime
            
            val responseBody = try {
                val bodySource = response.body
                if (bodySource == null) {
                    null
                } else {
                    val contentLength = bodySource.contentLength()
                    
                    // If body is too large, don't log it at all
                    if (contentLength > MAX_BODY_SIZE) {
                        "[Response body too large: $contentLength bytes - not logged for memory safety]"
                    } else {
                        // Safely peek at the body with a hard limit
                        val peekSize = if (contentLength > 0) {
                            minOf(MAX_BODY_SIZE, contentLength)
                        } else {
                            MAX_BODY_SIZE
                        }
                        
                        try {
                            val peeked = response.peekBody(peekSize).string()
                            // Double-check the peeked size
                            if (peeked.length > MAX_BODY_SIZE) {
                                peeked.substring(0, MAX_BODY_SIZE.toInt()) + TRUNCATION_MESSAGE
                            } else {
                                peeked
                            }
                        } catch (oom: OutOfMemoryError) {
                            "[OOM while reading response body - response too large]"
                        }
                    }
                }
            } catch (e: Exception) {
                "[Body read error: ${e.message}]"
            }
            
            val responseHeaders = response.headers.toMultimap()
                .mapValues { it.value.joinToString(", ") }
            
            // Log the response
            LoggerOverlay.logResponse(
                requestId = requestId,
                statusCode = response.code,
                headers = responseHeaders,
                body = responseBody,
                duration = duration
            )
            
            response
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            
            // Log the error
            LoggerOverlay.logError(
                requestId = requestId,
                error = e,
                duration = duration
            )
            
            throw e
        }
    }
}
