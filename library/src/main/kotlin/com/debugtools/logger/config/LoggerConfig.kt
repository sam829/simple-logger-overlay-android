package com.debugtools.logger.config

import androidx.compose.ui.graphics.Color

/**
 * Configuration for [in.sammacwan.logger.LoggerOverlay].
 *
 * Pass an instance to [in.sammacwan.logger.LoggerOverlay.init] at app startup.
 *
 * ```kotlin
 * LoggerOverlay.init(
 *     context = this,
 *     config = LoggerConfig(
 *         enabled = BuildConfig.DEBUG,
 *         persistLogs = false,
 *         logToConsole = true,
 *     )
 * )
 * ```
 *
 * @property enabled Master switch. Set to `false` (e.g. `BuildConfig.DEBUG == false`) to
 *   make the library a complete no-op in production builds.
 * @property persistLogs When `true`, logs are stored in a Room database and survive process
 *   death. When `false` (default), logs are kept in memory only.
 * @property retentionDays Number of days to retain persisted logs. `0` clears logs on each
 *   app launch. Ignored when [persistLogs] is `false`.
 * @property maxLogsInMemory Maximum number of log entries held in memory before the oldest
 *   are evicted. Only used when [persistLogs] is `false`.
 * @property seedColor Seed colour used to generate the Material 3 dynamic colour scheme for
 *   the overlay UI. Defaults to Material purple.
 * @property enableShakeToOpen When `true`, shaking the device opens the overlay. Useful
 *   during physical device testing when you cannot trigger the FAB.
 * @property logToConsole When `true`, every log entry is also forwarded to Android Logcat
 *   via the internal logger.
 */
data class LoggerConfig(
    val enabled: Boolean = true,
    val persistLogs: Boolean = false,
    val retentionDays: Int = 0,
    val maxLogsInMemory: Int = 1000,
    val seedColor: Color = Color(0xFF6200EE), // Material Purple (default)
    val enableShakeToOpen: Boolean = true,
    val logToConsole: Boolean = true,
)
