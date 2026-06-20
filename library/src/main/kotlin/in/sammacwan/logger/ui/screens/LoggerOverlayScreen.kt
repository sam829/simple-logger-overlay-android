package in.sammacwan.logger.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import in.sammacwan.logger.core.LogEntry
import in.sammacwan.logger.core.NetworkLogEntry
import in.sammacwan.logger.ui.components.FilterSheet
import in.sammacwan.logger.ui.components.LogCard
import in.sammacwan.logger.ui.components.NetworkCard
import in.sammacwan.logger.ui.viewmodel.LoggerOverlayViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoggerOverlayScreen(
    onClose: () -> Unit,
    viewModel: LoggerOverlayViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedLogForDetail by remember { mutableStateOf<LogEntry?>(null) }
    var selectedNetworkLogForDetail by remember { mutableStateOf<NetworkLogEntry?>(null) }
    
    // Show detail screens if a log is selected
    when {
        selectedLogForDetail != null -> {
            LogDetailScreen(
                log = selectedLogForDetail!!,
                onBack = { selectedLogForDetail = null }
            )
        }
        selectedNetworkLogForDetail != null -> {
            NetworkDetailScreen(
                log = selectedNetworkLogForDetail!!,
                onBack = { selectedNetworkLogForDetail = null }
            )
        }
        else -> {
            // Main list view
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Logger Overlay") },
                        navigationIcon = {
                            IconButton(onClick = onClose) {
                                Icon(Icons.Default.Close, "Close")
                            }
                        },
                        actions = {
                            IconButton(onClick = { viewModel.toggleSearch() }) {
                                Icon(Icons.Default.Search, "Search")
                            }
                            IconButton(onClick = { viewModel.showFilterSheet() }) {
                                Icon(Icons.Default.FilterList, "Filter")
                            }
                            IconButton(onClick = { viewModel.clearLogs() }) {
                                Icon(Icons.Default.Delete, "Clear Logs")
                            }
                        }
                    )
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Search bar
                    if (uiState.searchVisible) {
                        TextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.updateSearch(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            placeholder = { Text("Search logs...") },
                            leadingIcon = { Icon(Icons.Default.Search, "Search") },
                            trailingIcon = {
                                if (uiState.searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.updateSearch("") }) {
                                        Icon(Icons.Default.Clear, "Clear")
                                    }
                                }
                            },
                            singleLine = true
                        )
                    }
                    
                    // Tabs
                    TabRow(selectedTabIndex = uiState.selectedTab) {
                        Tab(
                            selected = uiState.selectedTab == 0,
                            onClick = { viewModel.selectTab(0) },
                            text = { Text("Logs (${uiState.logCount})") }
                        )
                        Tab(
                            selected = uiState.selectedTab == 1,
                            onClick = { viewModel.selectTab(1) },
                            text = { Text("Network (${uiState.networkLogCount})") }
                        )
                    }
                    
                    // Content
                    when (uiState.selectedTab) {
                        0 -> LogListScreen(
                            viewModel = viewModel,
                            onLogClick = { selectedLogForDetail = it }
                        )
                        1 -> NetworkListScreen(
                            viewModel = viewModel,
                            onNetworkLogClick = { selectedNetworkLogForDetail = it }
                        )
                    }
                }
            }
            
            // Filter bottom sheet
            if (uiState.showFilterSheet) {
                FilterSheet(
                    selectedLevels = uiState.selectedLevels,
                    networkFilter = uiState.networkFilter,
                    selectedTab = uiState.selectedTab,
                    onLevelFilterChanged = { viewModel.updateLevelFilters(it) },
                    onNetworkFilterChanged = { viewModel.updateNetworkFilter(it) },
                    onDismiss = { viewModel.hideFilterSheet() }
                )
            }
        }
    }
}

@Composable
private fun LogListScreen(
    viewModel: LoggerOverlayViewModel,
    onLogClick: (LogEntry) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    
    if (uiState.filteredLogs.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text(
                text = "No logs",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.filteredLogs, key = { it.id }) { log ->
                    LogCard(
                        log = log,
                        onClick = { onLogClick(log) }
                    )
                }
            }
            
            // Custom scrollbar indicator
            if (uiState.filteredLogs.size > 10) {
                ScrollbarIndicator(
                    listState = listState,
                    itemCount = uiState.filteredLogs.size,
                    modifier = Modifier
                        .align(androidx.compose.ui.Alignment.CenterEnd)
                        .fillMaxHeight()
                        .padding(end = 4.dp, top = 16.dp, bottom = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun NetworkListScreen(
    viewModel: LoggerOverlayViewModel,
    onNetworkLogClick: (NetworkLogEntry) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    
    if (uiState.filteredNetworkLogs.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text(
                text = "No network logs",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.filteredNetworkLogs, key = { it.id }) { log ->
                    NetworkCard(
                        networkLog = log,
                        onClick = { onNetworkLogClick(log) }
                    )
                }
            }
            
            // Custom scrollbar indicator
            if (uiState.filteredNetworkLogs.size > 10) {
                ScrollbarIndicator(
                    listState = listState,
                    itemCount = uiState.filteredNetworkLogs.size,
                    modifier = Modifier
                        .align(androidx.compose.ui.Alignment.CenterEnd)
                        .fillMaxHeight()
                        .padding(end = 4.dp, top = 16.dp, bottom = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun ScrollbarIndicator(
    listState: androidx.compose.foundation.lazy.LazyListState,
    itemCount: Int,
    modifier: Modifier = Modifier
) {
    val firstVisibleIndex by remember {
        derivedStateOf { listState.firstVisibleItemIndex }
    }
    
    // Calculate scrollbar thumb position and size
    val scrollbarHeight = with(androidx.compose.ui.platform.LocalDensity.current) { 200.dp.toPx() }
    val thumbHeight = (scrollbarHeight / itemCount.coerceAtLeast(1)).coerceAtLeast(40f)
    val thumbOffset = (firstVisibleIndex.toFloat() / itemCount.coerceAtLeast(1)) * (scrollbarHeight - thumbHeight)
    
    Box(
        modifier = modifier.width(6.dp)
    ) {
        // Track
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(4.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp)
                )
        )
        
        // Thumb
        Box(
            modifier = Modifier
                .offset(y = with(androidx.compose.ui.platform.LocalDensity.current) { thumbOffset.toDp() })
                .height(with(androidx.compose.ui.platform.LocalDensity.current) { thumbHeight.toDp() })
                .width(6.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(3.dp)
                )
        )
    }
}
