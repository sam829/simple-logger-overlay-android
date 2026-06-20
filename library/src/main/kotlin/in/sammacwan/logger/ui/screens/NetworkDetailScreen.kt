package in.sammacwan.logger.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import in.sammacwan.logger.core.NetworkLogEntry
import in.sammacwan.logger.ui.components.DetailSection
import in.sammacwan.logger.ui.theme.NetworkStatusColors
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkDetailScreen(
    log: NetworkLogEntry,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Network Detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { copyNetworkLogToClipboard(context, log) }) {
                        Icon(Icons.Default.ContentCopy, "Copy")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status Badge
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = log.method,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                log.responseCode?.let { code ->
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = NetworkStatusColors.badgeColor(code)
                    ) {
                        Text(
                            text = code.toString(),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = NetworkStatusColors.badgeContentColor(code)
                        )
                    }
                }
                
                log.duration?.let { duration ->
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = "${duration}ms",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
            
            // Timestamp
            DetailSection(
                title = "Timestamp",
                content = formatTimestamp(log.timestamp)
            )
            
            // URL
            DetailSection(
                title = "URL",
                content = log.url
            )
            
            // Request Headers
            log.requestHeaders?.let { headers ->
                DetailSection(
                    title = "Request Headers",
                    content = formatJson(headers),
                    isCode = true
                )
            }
            
            // Request Body
            log.requestBody?.let { body ->
                DetailSection(
                    title = "Request Body",
                    content = formatJson(body),
                    isCode = true
                )
            }
            
            // Response Headers
            log.responseHeaders?.let { headers ->
                DetailSection(
                    title = "Response Headers",
                    content = formatJson(headers),
                    isCode = true
                )
            }
            
            // Response Body
            log.responseBody?.let { body ->
                DetailSection(
                    title = "Response Body",
                    content = formatJson(body),
                    isCode = true
                )
            }
            
            // Error (if present)
            log.error?.let { error ->
                DetailSection(
                    title = "Error",
                    content = error,
                    isCode = true
                )
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

private fun formatJson(json: String): String {
    return try {
        // Limit JSON size to prevent OOM
        val maxSize = 100_000 // 100KB max
        if (json.length > maxSize) {
            return json.take(maxSize) + "\n\n... (truncated, too large to format)"
        }
        
        val trimmed = json.trim()
        if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
            // Simple formatting without multiple replace calls
            val formatted = StringBuilder(trimmed.length + 1000)
            var indent = 0
            var inString = false
            
            for (i in trimmed.indices) {
                val char = trimmed[i]
                
                when (char) {
                    '"' -> {
                        if (i == 0 || trimmed[i - 1] != '\\') {
                            inString = !inString
                        }
                        formatted.append(char)
                    }
                    '{', '[' -> {
                        formatted.append(char)
                        if (!inString) {
                            indent++
                            formatted.append('\n').append("  ".repeat(indent))
                        }
                    }
                    '}', ']' -> {
                        if (!inString) {
                            indent--
                            formatted.append('\n').append("  ".repeat(indent))
                        }
                        formatted.append(char)
                    }
                    ',' -> {
                        formatted.append(char)
                        if (!inString) {
                            formatted.append('\n').append("  ".repeat(indent))
                        }
                    }
                    else -> formatted.append(char)
                }
            }
            formatted.toString()
        } else {
            json
        }
    } catch (e: Exception) {
        // If formatting fails, return original (might be too large)
        if (json.length > 100_000) {
            json.take(100_000) + "\n\n... (truncated due to size)"
        } else {
            json
        }
    }
}

private fun copyNetworkLogToClipboard(context: Context, log: NetworkLogEntry) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val text = buildString {
        appendLine("${log.method} ${log.url}")
        appendLine("Timestamp: ${formatTimestamp(log.timestamp)}")
        log.responseCode?.let { appendLine("Status: $it") }
        log.duration?.let { appendLine("Duration: ${it}ms") }
        
        log.requestHeaders?.let {
            appendLine("\nRequest Headers:")
            appendLine(it)
        }
        log.requestBody?.let {
            appendLine("\nRequest Body:")
            appendLine(it)
        }
        log.responseHeaders?.let {
            appendLine("\nResponse Headers:")
            appendLine(it)
        }
        log.responseBody?.let {
            appendLine("\nResponse Body:")
            appendLine(it)
        }
        log.error?.let {
            appendLine("\nError:")
            appendLine(it)
        }
    }
    
    val clip = ClipData.newPlainText("Network Log", text)
    clipboardManager.setPrimaryClip(clip)
}
