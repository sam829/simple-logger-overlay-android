package in.sammacwan.logger.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import in.sammacwan.logger.core.LogLevel
import in.sammacwan.logger.ui.viewmodel.NetworkFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSheet(
    selectedLevels: Set<LogLevel>,
    networkFilter: NetworkFilter,
    selectedTab: Int,
    onLevelFilterChanged: (Set<LogLevel>) -> Unit,
    onNetworkFilterChanged: (NetworkFilter) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Filters",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            HorizontalDivider()
            
            when (selectedTab) {
                0 -> {
                    // Log level filters
                    Text(
                        text = "Log Levels",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    LogLevel.values().forEach { level ->
                        val isSelected = level in selectedLevels
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = isSelected,
                                    onClick = {
                                        val newLevels = if (isSelected) {
                                            selectedLevels - level
                                        } else {
                                            selectedLevels + level
                                        }
                                        onLevelFilterChanged(newLevels)
                                    },
                                    role = Role.Checkbox
                                )
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = level.name,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = null // Handled by Row's selectable
                            )
                        }
                    }
                }
                1 -> {
                    // Network filters
                    Text(
                        text = "Response Status",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Column(Modifier.selectableGroup()) {
                        NetworkFilter.values().forEach { filter ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = networkFilter == filter,
                                        onClick = { onNetworkFilterChanged(filter) },
                                        role = Role.RadioButton
                                    )
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = when (filter) {
                                        NetworkFilter.ALL -> "All Requests"
                                        NetworkFilter.SUCCESS -> "Success (2xx)"
                                        NetworkFilter.ERROR -> "Errors (non-2xx)"
                                    },
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                RadioButton(
                                    selected = networkFilter == filter,
                                    onClick = null // Handled by Row's selectable
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
