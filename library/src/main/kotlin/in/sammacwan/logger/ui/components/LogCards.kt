package in.sammacwan.logger.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import in.sammacwan.logger.core.LogEntry
import in.sammacwan.logger.core.NetworkLogEntry
import in.sammacwan.logger.ui.theme.LogLevelColors
import in.sammacwan.logger.ui.theme.NetworkStatusColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

@Composable
fun LogCard(
    log: LogEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = LogLevelColors.containerColor(log.level)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = LogLevelColors.icon(log.level),
                contentDescription = log.level.name,
                tint = LogLevelColors.contentColor(log.level),
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = log.tag,
                        style = MaterialTheme.typography.labelMedium,
                        color = LogLevelColors.contentColor(log.level),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = timeFormat.format(Date(log.timestamp)),
                        style = MaterialTheme.typography.labelSmall,
                        color = LogLevelColors.contentColor(log.level).copy(alpha = 0.7f)
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = log.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = LogLevelColors.contentColor(log.level),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Show metadata chips if present
                val metadata = log.metadata
                if (metadata != null && metadata.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Show from/to navigation chips
                        metadata["from"]?.let { from ->
                            MetadataChip(
                                label = "from: $from",
                                contentColor = LogLevelColors.contentColor(log.level)
                            )
                        }
                        metadata["to"]?.let { to ->
                            MetadataChip(
                                label = "to: $to",
                                contentColor = LogLevelColors.contentColor(log.level)
                            )
                        }
                        
                        // Show screen name if present
                        metadata["screen"]?.let { screen ->
                            MetadataChip(
                                label = screen,
                                contentColor = LogLevelColors.contentColor(log.level)
                            )
                        }
                        
                        // Show game metadata
                        metadata["game_type"]?.let { gameType ->
                            MetadataChip(
                                label = gameType,
                                contentColor = LogLevelColors.contentColor(log.level)
                            )
                        }
                        metadata["mode"]?.let { mode ->
                            MetadataChip(
                                label = mode,
                                contentColor = LogLevelColors.contentColor(log.level)
                            )
                        }
                        metadata["locale"]?.let { locale ->
                            MetadataChip(
                                label = locale,
                                contentColor = LogLevelColors.contentColor(log.level)
                            )
                        }
                        
                        // Show elapsed time prominently at the end
                        metadata["elapsed_ms"]?.let { elapsedMs ->
                            MetadataChip(
                                label = "⏱ ${elapsedMs}ms",
                                contentColor = LogLevelColors.contentColor(log.level)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetadataChip(
    label: String,
    contentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = contentColor.copy(alpha = 0.15f),
        shape = MaterialTheme.shapes.small,
        modifier = modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace
            ),
            color = contentColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun NetworkCard(
    networkLog: NetworkLogEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = NetworkStatusColors.badgeColor(networkLog.responseCode)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Method badge
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = networkLog.method,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                
                // Status code badge
                networkLog.responseCode?.let { code ->
                    Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = code.toString(),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = FontFamily.Monospace
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                
                // Duration
                Text(
                    text = "${networkLog.duration}ms",
                    style = MaterialTheme.typography.labelSmall,
                    color = NetworkStatusColors.badgeContentColor(networkLog.responseCode)
                        .copy(alpha = 0.7f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = networkLog.url,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace
                ),
                color = NetworkStatusColors.badgeContentColor(networkLog.responseCode),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            networkLog.error?.let { error ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Error: $error",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = timeFormat.format(Date(networkLog.timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = NetworkStatusColors.badgeContentColor(networkLog.responseCode)
                    .copy(alpha = 0.6f)
            )
        }
    }
}
