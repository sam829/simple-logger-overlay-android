package com.debugtools.logger

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.room.Room
import com.debugtools.logger.config.LoggerConfig
import com.debugtools.logger.core.InternalLogger
import com.debugtools.logger.core.LogEntry
import com.debugtools.logger.core.LogLevel
import com.debugtools.logger.core.NetworkLogEntry
import com.debugtools.logger.storage.InMemoryLogStorage
import com.debugtools.logger.storage.LogDatabase
import com.debugtools.logger.storage.LogStorage
import com.debugtools.logger.storage.RoomLogStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Main entry point for the Simple Logger Overlay library.
 *
 * Call [init] once in your `Application.onCreate()`, then use the static logging functions
 * anywhere in your app. In production builds, pass `enabled = false` (or use
 * `debugImplementation`) to make this a complete no-op.
 *
 * ```kotlin
 * // Application.onCreate()
 * if (BuildConfig.DEBUG) {
 *     LoggerOverlay.init(
 *         context = this,
 *         config = LoggerConfig(persistLogs = false, logToConsole = true)
 *     )
 * }
 *
 * // Anywhere in your app
 * LoggerOverlay.d("MyTag", "Hello from Simple Logger Overlay")
 * LoggerOverlay.e("MyTag", "Something went wrong", exception)
 * ```
 */
object LoggerOverlay {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var config: LoggerConfig = LoggerConfig()
    private var storage: LogStorage? = null
    private var appContext: Context? = null
    private var shakeDetector: ShakeDetector? = null
    
    private val _hasUnreadLogs = MutableStateFlow(false)
    /**
     * A [StateFlow] that emits `true` whenever new logs arrive while the overlay is closed.
     * Observe this to badge your own debug launcher button.
     */
    val hasUnreadLogs: StateFlow<Boolean> = _hasUnreadLogs.asStateFlow()
    
    @Volatile
    private var isInitialized = false
    
    @Volatile
    private var isActivityVisible = false
    
    /**
     * Optional callback consulted before showing the overlay.
     *
     * Set this from the host app to integrate with your own developer-mode preference:
     * ```kotlin
     * LoggerOverlay.shouldShowOverlay = { prefs.isDevModeEnabled }
     * ```
     * Return `true` to allow, `false` to suppress. `null` (default) always allows.
     */
    var shouldShowOverlay: (() -> Boolean)? = null

    /**
     * Initialises the library. Must be called before any logging function.
     *
     * Safe to call multiple times — subsequent calls are ignored.
     *
     * @param context Any [Context]; the library retains the application context internally.
     * @param config Library configuration. See [LoggerConfig] for all options.
     */
    fun init(context: Context, config: LoggerConfig = LoggerConfig()) {
        if (isInitialized) {
            InternalLogger.w("LoggerOverlay already initialized")
            return
        }
        
        this.config = config
        this.appContext = context.applicationContext
        
        // Initialize storage
        storage = if (config.persistLogs && config.retentionDays > 0) {
            val database = Room.databaseBuilder(
                context.applicationContext,
                LogDatabase::class.java,
                "logger_overlay_db"
            )
                .fallbackToDestructiveMigration() // Recreate DB on schema changes (acceptable for debug logs)
                .build()
            RoomLogStorage(database)
        } else {
            InMemoryLogStorage(config.maxLogsInMemory)
        }
        
        // Clear or cleanup based on retention policy
        scope.launch {
            if (config.retentionDays == 0) {
                storage?.clear()
            } else {
                storage?.deleteOlderThan(config.retentionDays)
            }
        }
        
        // Initialize shake detector if enabled
        if (config.enableShakeToOpen) {
            shakeDetector = ShakeDetector(context.applicationContext) {
                InternalLogger.d("Shake detected - opening overlay")
                show(context)
            }
            shakeDetector?.start()
            InternalLogger.d("Shake-to-open enabled")
        }
        
        isInitialized = true
        InternalLogger.d("LoggerOverlay initialized")
    }
    
    /** Logs a [LogLevel.VERBOSE] message. */
    fun v(tag: String, message: String, data: Any? = null) {
        log(LogLevel.VERBOSE, tag, message, data, null)
    }
    
    /** Logs a [LogLevel.DEBUG] message with optional supplementary [data]. */
    fun d(tag: String, message: String, data: Any? = null) {
        log(LogLevel.DEBUG, tag, message, data, null)
    }
    
    /** Logs a [LogLevel.INFO] message with optional supplementary [data]. */
    fun i(tag: String, message: String, data: Any? = null) {
        log(LogLevel.INFO, tag, message, data, null)
    }
    
    /** Logs a [LogLevel.WARN] message with an optional [throwable]. */
    fun w(tag: String, message: String, throwable: Throwable? = null) {
        log(LogLevel.WARN, tag, message, null, throwable)
    }
    
    /** Logs a [LogLevel.ERROR] message with an optional [throwable]. */
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        log(LogLevel.ERROR, tag, message, null, throwable)
    }
    
    /**
     * Logs a raw JSON string. The overlay will pretty-print it in the detail view.
     *
     * @param tag Source identifier.
     * @param message Short description of what the JSON represents (e.g. `"API response"`).
     * @param json Raw JSON string to log.
     * @param level Log level to assign; defaults to [LogLevel.DEBUG].
     */
    fun logJson(tag: String, message: String, json: String, level: LogLevel = LogLevel.DEBUG) {
        log(level, tag, message, json, null, null)
    }
    
    /**
     * Logs a structured telemetry event with typed key-value [metadata].
     *
     * For richer session-level telemetry use [in.sammacwan.logger.telemetry.SessionTelemetry].
     */
    fun telemetry(tag: String, message: String, metadata: Map<String, String> = emptyMap()) {
        log(LogLevel.INFO, tag, message, null, null, metadata)
    }
    
    private fun log(
        level: LogLevel, 
        tag: String, 
        message: String, 
        data: Any?, 
        throwable: Throwable?,
        metadata: Map<String, String>? = null
    ) {
        if (!config.enabled || !isInitialized) return
        
        val dataString = when (data) {
            is String -> data
            null -> null
            else -> try {
                Json.encodeToString(data)
            } catch (e: Exception) {
                data.toString()
            }
        }
        
        val throwableString = throwable?.let {
            val sw = StringWriter()
            it.printStackTrace(PrintWriter(sw))
            sw.toString()
        }
        
        val entry = LogEntry(
            timestamp = System.currentTimeMillis(),
            level = level,
            tag = tag,
            message = message,
            data = dataString,
            throwable = throwableString,
            metadata = metadata,
        )
        
        scope.launch {
            storage?.addLog(entry)
        }
        
        if (config.logToConsole) {
            InternalLogger.d("[$level] [$tag] $message")
        }
        
        _hasUnreadLogs.value = true
    }
    
    fun logRequest(
        id: String,
        method: String,
        url: String,
        headers: Map<String, String>?,
        body: String?,
    ) {
        if (!config.enabled || !isInitialized) return
        
        val headersString = headers?.let { 
            try {
                Json.encodeToString(it)
            } catch (e: Exception) {
                it.toString()
            }
        }
        
        val entry = NetworkLogEntry(
            id = id,
            timestamp = System.currentTimeMillis(),
            method = method,
            url = url,
            requestHeaders = headersString,
            requestBody = body,
            responseCode = null,
            responseHeaders = null,
            responseBody = null,
            duration = null,
            error = null
        )
        
        scope.launch {
            storage?.addNetworkLog(entry)
        }
        
        if (config.logToConsole) {
            InternalLogger.d("[NETWORK] $method $url")
        }
    }
    
    fun logResponse(
        requestId: String,
        statusCode: Int,
        headers: Map<String, String>?,
        body: String?,
        duration: Long,
    ) {
        if (!config.enabled || !isInitialized) return
        
        val headersString = headers?.let { 
            try {
                Json.encodeToString(it)
            } catch (e: Exception) {
                it.toString()
            }
        }
        
        scope.launch {
            try {
                // Get the existing request log
                val existingLog = storage?.getNetworkLogById(requestId)
                if (existingLog != null) {
                    // Update with response data
                    val updatedLog = existingLog.copy(
                        responseCode = statusCode,
                        responseHeaders = headersString,
                        responseBody = body,
                        duration = duration
                    )
                    storage?.updateNetworkLog(updatedLog)
                } else {
                    InternalLogger.w("Network log with id $requestId not found for response update")
                }
                
                if (config.logToConsole) {
                    InternalLogger.d("[NETWORK] Response: $statusCode (${duration}ms)")
                }
            } catch (e: Exception) {
                InternalLogger.e("Failed to update network log with response", e)
            }
        }
    }
    
    fun logError(requestId: String, error: Throwable, duration: Long) {
        if (!config.enabled || !isInitialized) return
        
        val errorString = "${error.javaClass.simpleName}: ${error.message}"
        
        scope.launch {
            try {
                val existingLog = storage?.getNetworkLogById(requestId)
                if (existingLog != null) {
                    val updatedLog = existingLog.copy(
                        error = errorString,
                        duration = duration,
                        responseCode = -1 // Indicate error
                    )
                    storage?.updateNetworkLog(updatedLog)
                } else {
                    InternalLogger.w("Network log with id $requestId not found for error update")
                }
                
                if (config.logToConsole) {
                    InternalLogger.e("[NETWORK] Error: $errorString", error)
                }
            } catch (e: Exception) {
                InternalLogger.e("Failed to update network log with error", e)
            }
        }
    }
    
    /**
     * Opens the overlay UI as a new Activity.
     *
     * Respects [shouldShowOverlay] if set, and ignores the call if the overlay is already visible.
     *
     * @param context Any [Context] — the overlay is launched with `FLAG_ACTIVITY_NEW_TASK`.
     */
    fun show(context: Context) {
        // Check app-level preference callback if set
        val shouldShow = shouldShowOverlay?.invoke() ?: true
        if (!shouldShow) {
            InternalLogger.d("LoggerOverlay blocked by app preference")
            return
        }
        
        if (isActivityVisible) {
            InternalLogger.d("LoggerOverlay activity already visible, ignoring launch request")
            return
        }
        
        val intent = android.content.Intent(context, com.debugtools.logger.ui.activity.LoggerOverlayActivity::class.java)
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        _hasUnreadLogs.value = false
    }
    
    fun hide() {
        // Note: Closing the overlay is handled by the Activity's finish() method
        // This is a no-op as activities are closed via their own lifecycle
        InternalLogger.d("Hide called - overlay activities should call finish()")
    }
    
    internal fun setActivityVisible(visible: Boolean) {
        isActivityVisible = visible
        InternalLogger.d("Activity visibility changed to: $visible")
    }
    
    /** Enables logging after it has been disabled via [disable]. */
    fun enable() {
        config = config.copy(enabled = true)
    }
    
    /** Disables logging without reinitialising. Re-enable with [enable]. */
    fun disable() {
        config = config.copy(enabled = false)
    }
    
    /** Returns `true` if logging is currently enabled. */
    fun isEnabled(): Boolean = config.enabled
    
    fun enableStorage(context: Context) {
        if (!isInitialized) {
            InternalLogger.w("Cannot enable storage - LoggerOverlay not initialized")
            return
        }
        
        scope.launch {
            try {
                // Save current logs
                val currentLogs = mutableListOf<LogEntry>()
                val currentNetworkLogs = mutableListOf<NetworkLogEntry>()
                
                storage?.getLogs()?.let { flow ->
                    flow.collect { currentLogs.addAll(it) }
                }
                storage?.getNetworkLogs()?.let { flow ->
                    flow.collect { currentNetworkLogs.addAll(it) }
                }
                
                // Switch to Room storage
                val database = Room.databaseBuilder(
                    context.applicationContext,
                    LogDatabase::class.java,
                    "logger_overlay_db"
                ).build()
                
                storage = RoomLogStorage(database)
                config = config.copy(persistLogs = true, retentionDays = 7)
                
                // Restore logs
                currentLogs.forEach { storage?.addLog(it) }
                currentNetworkLogs.forEach { storage?.addNetworkLog(it) }
                
                InternalLogger.d("Switched to persistent storage")
            } catch (e: Exception) {
                InternalLogger.e("Failed to enable storage", e)
            }
        }
    }
    
    fun disableStorage() {
        if (!isInitialized) {
            InternalLogger.w("Cannot disable storage - LoggerOverlay not initialized")
            return
        }
        
        scope.launch {
            try {
                // Save current logs
                val currentLogs = mutableListOf<LogEntry>()
                val currentNetworkLogs = mutableListOf<NetworkLogEntry>()
                
                storage?.getLogs()?.let { flow ->
                    flow.collect { currentLogs.addAll(it) }
                }
                storage?.getNetworkLogs()?.let { flow ->
                    flow.collect { currentNetworkLogs.addAll(it) }
                }
                
                // Switch to in-memory storage
                storage = InMemoryLogStorage(config.maxLogsInMemory)
                config = config.copy(persistLogs = false, retentionDays = 0)
                
                // Restore logs
                currentLogs.forEach { storage?.addLog(it) }
                currentNetworkLogs.forEach { storage?.addNetworkLog(it) }
                
                InternalLogger.d("Switched to in-memory storage")
            } catch (e: Exception) {
                InternalLogger.e("Failed to disable storage", e)
            }
        }
    }
    
    /** Clears all in-memory or persisted logs. */
    fun clearLogs() = scope.launch {
        storage?.clear()
    }
    
    /**
     * Exports all current logs to a JSON file in the app's cache directory and returns a
     * shareable [Uri] via the library's [FileProvider].
     *
     * Call from a coroutine scope — this is a suspending operation.
     *
     * @return A content [Uri] pointing to the exported file, or `null` if export failed.
     */
    suspend fun exportLogs(context: Context): Uri? {
        return try {
            val logs = mutableListOf<LogEntry>()
            val networkLogs = mutableListOf<NetworkLogEntry>()
            
            // Collect from flows
            storage?.getLogs()?.collect { logs.addAll(it) }
            storage?.getNetworkLogs()?.collect { networkLogs.addAll(it) }
            
            // Create export data
            val exportData = mapOf(
                "timestamp" to System.currentTimeMillis(),
                "appVersion" to try {
                    context.packageManager.getPackageInfo(context.packageName, 0).versionName
                } catch (e: Exception) {
                    "unknown"
                },
                "device" to mapOf(
                    "manufacturer" to android.os.Build.MANUFACTURER,
                    "model" to android.os.Build.MODEL,
                    "sdk" to android.os.Build.VERSION.SDK_INT
                ),
                "logs" to logs.map { log ->
                    mapOf(
                        "timestamp" to log.timestamp,
                        "level" to log.level.name,
                        "tag" to log.tag,
                        "message" to log.message,
                        "data" to log.data,
                        "throwable" to log.throwable
                    )
                },
                "networkLogs" to networkLogs.map { log ->
                    mapOf(
                        "id" to log.id,
                        "timestamp" to log.timestamp,
                        "method" to log.method,
                        "url" to log.url,
                        "requestHeaders" to log.requestHeaders,
                        "requestBody" to log.requestBody,
                        "responseCode" to log.responseCode,
                        "responseHeaders" to log.responseHeaders,
                        "responseBody" to log.responseBody,
                        "duration" to log.duration,
                        "error" to log.error
                    )
                }
            )
            
            val file = File(context.cacheDir, "logger_export_${System.currentTimeMillis()}.json")
            file.writeText(Json.encodeToString(exportData))
            
            // Use the library's own FileProvider authority to avoid clashing with the host app's provider
            FileProvider.getUriForFile(context, "in.sammacwan.logger.provider", file)
        } catch (e: Exception) {
            InternalLogger.e("Failed to export logs", e)
            null
        }
    }
    
    internal fun getStorage(): LogStorage? = storage
    internal fun getConfig(): LoggerConfig = config
}
