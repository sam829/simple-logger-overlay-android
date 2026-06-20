package in.sammacwan.logger.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import in.sammacwan.logger.core.LogLevel

object LogLevelColors {
    @Composable
    fun containerColor(level: LogLevel): Color = when (level) {
        LogLevel.VERBOSE -> MaterialTheme.colorScheme.surfaceVariant
        LogLevel.DEBUG -> MaterialTheme.colorScheme.tertiaryContainer
        LogLevel.INFO -> MaterialTheme.colorScheme.primaryContainer
        LogLevel.WARN -> MaterialTheme.colorScheme.secondaryContainer
        LogLevel.ERROR -> MaterialTheme.colorScheme.errorContainer
    }
    
    @Composable
    fun contentColor(level: LogLevel): Color = when (level) {
        LogLevel.VERBOSE -> MaterialTheme.colorScheme.onSurfaceVariant
        LogLevel.DEBUG -> MaterialTheme.colorScheme.onTertiaryContainer
        LogLevel.INFO -> MaterialTheme.colorScheme.onPrimaryContainer
        LogLevel.WARN -> MaterialTheme.colorScheme.onSecondaryContainer
        LogLevel.ERROR -> MaterialTheme.colorScheme.onErrorContainer
    }
    
    @Composable
    fun badgeColor(level: LogLevel): Color = containerColor(level)
    
    @Composable
    fun badgeContentColor(level: LogLevel): Color = contentColor(level)
    
    @Composable
    fun icon(level: LogLevel): ImageVector = when (level) {
        LogLevel.VERBOSE -> Icons.Default.Info
        LogLevel.DEBUG -> Icons.Default.BugReport
        LogLevel.INFO -> Icons.Default.Info
        LogLevel.WARN -> Icons.Default.Warning
        LogLevel.ERROR -> Icons.Default.Error
    }
}

object NetworkStatusColors {
    @Composable
    fun badgeColor(responseCode: Int?): Color = when {
        responseCode == null -> MaterialTheme.colorScheme.surfaceVariant
        responseCode in 200..299 -> MaterialTheme.colorScheme.primaryContainer
        responseCode in 300..399 -> MaterialTheme.colorScheme.secondaryContainer
        responseCode >= 400 -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    @Composable
    fun badgeContentColor(responseCode: Int?): Color = when {
        responseCode == null -> MaterialTheme.colorScheme.onSurfaceVariant
        responseCode in 200..299 -> MaterialTheme.colorScheme.onPrimaryContainer
        responseCode in 300..399 -> MaterialTheme.colorScheme.onSecondaryContainer
        responseCode >= 400 -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}
