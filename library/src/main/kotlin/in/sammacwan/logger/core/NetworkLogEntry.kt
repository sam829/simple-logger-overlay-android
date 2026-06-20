package in.sammacwan.logger.core

/**
 * A captured HTTP request/response pair, logged via [in.sammacwan.logger.network.OkHttpLoggerInterceptor].
 *
 * Requests and responses are matched by [id]. The interceptor writes the request first,
 * then updates the same entry with the response or error when it arrives.
 *
 * @property id UUID that links the outgoing request to its inbound response.
 * @property timestamp Wall-clock time the request was initiated (ms since epoch).
 * @property method HTTP method (`GET`, `POST`, etc.).
 * @property url Full request URL.
 * @property requestHeaders JSON-serialised request headers map, or null if not captured.
 * @property requestBody Raw request body text, or null for bodyless methods.
 * @property responseCode HTTP status code, or -1 on network error, or null if pending.
 * @property responseHeaders JSON-serialised response headers map, or null if not yet received.
 * @property responseBody Raw response body text (capped at 1 MB), or null if not yet received.
 * @property duration Round-trip duration in milliseconds, or null if not yet complete.
 * @property error Error description string if the request failed at the network layer.
 */
data class NetworkLogEntry(
    val id: String,
    val timestamp: Long = System.currentTimeMillis(),
    val method: String,
    val url: String,
    val requestHeaders: String? = null,
    val requestBody: String? = null,
    val responseCode: Int? = null,
    val responseHeaders: String? = null,
    val responseBody: String? = null,
    val duration: Long? = null,
    val error: String? = null,
)
