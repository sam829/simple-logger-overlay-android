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
import in.sammacwan.logger.core.LogEntry
import in.sammacwan.logger.ui.components.DetailSection
import in.sammacwan.logger.ui.theme.LogLevelColors
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogDetailScreen(
    log: LogEntry,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log Detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { copyLogToClipboard(context, log) }) {
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
            // Level Badge
            Surface(
                shape = MaterialTheme.shapes.small,
                color = LogLevelColors.badgeColor(log.level)
            ) {
                Text(
                    text = log.level.name,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = LogLevelColors.badgeContentColor(log.level)
                )
            }
            
            // Timestamp
            DetailSection(
                title = "Timestamp",
                content = formatTimestamp(log.timestamp)
            )
            
            // Tag
            DetailSection(
                title = "Tag",
                content = log.tag
            )
            
            // Message
            DetailSection(
                title = "Message",
                content = log.message
            )
            
            // Data (if present)
            log.data?.let { data ->
                DetailSection(
                    title = "Data",
                    content = formatJson(data),
                    isCode = true
                )
            }
            
            // Throwable (if present)
            log.throwable?.let { throwable ->
                DetailSection(
                    title = "Stack Trace",
                    content = throwable,
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
        // Try to parse and pretty print JSON
        val trimmed = json.trim()
        if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
            // Already formatted or valid JSON - return as is
            trimmed
        } else {
            json
        }
    } catch (e: Exception) {
        json
    }
}

private fun copyLogToClipboard(context: Context, log: LogEntry) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val text = buildString {
        appendLine("Level: ${log.level.name}")
        appendLine("Timestamp: ${formatTimestamp(log.timestamp)}")
        appendLine("Tag: ${log.tag}")
        appendLine("Message: ${log.message}")
        log.data?.let {
            appendLine("\nData:")
            appendLine(it)
        }
        log.throwable?.let {
            appendLine("\nStack Trace:")
            appendLine(it)
        }
    }
    
    val clip = ClipData.newPlainText("Log Entry", text)
    clipboardManager.setPrimaryClip(clip)
}
