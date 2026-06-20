package in.sammacwan.logger.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import in.sammacwan.logger.LoggerOverlay
import in.sammacwan.logger.core.LogEntry
import in.sammacwan.logger.core.LogLevel
import in.sammacwan.logger.core.NetworkLogEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoggerOverlayUiState(
    val logs: List<LogEntry> = emptyList(),
    val networkLogs: List<NetworkLogEntry> = emptyList(),
    val filteredLogs: List<LogEntry> = emptyList(),
    val filteredNetworkLogs: List<NetworkLogEntry> = emptyList(),
    val logCount: Int = 0,
    val networkLogCount: Int = 0,
    val selectedTab: Int = 0,
    val searchVisible: Boolean = false,
    val searchQuery: String = "",
    val showFilterSheet: Boolean = false,
    val selectedLevels: Set<LogLevel> = LogLevel.values().toSet(),
    val networkFilter: NetworkFilter = NetworkFilter.ALL,
) {
    companion object {
        const val MAX_DISPLAYED_LOGS = 500 // Limit for performance
    }
}

enum class NetworkFilter { ALL, SUCCESS, ERROR }

class LoggerOverlayViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(LoggerOverlayUiState())
    val uiState: StateFlow<LoggerOverlayUiState> = _uiState.asStateFlow()
    
    init {
        // Collect logs from storage (limited for performance)
        viewModelScope.launch {
            LoggerOverlay.getStorage()?.getLogs()?.collect { logs ->
                _uiState.update {
                    it.copy(
                        logs = logs.take(LoggerOverlayUiState.MAX_DISPLAYED_LOGS),
                        logCount = logs.size
                    )
                }
                applyFilters()
            }
        }
        
        // Collect network logs (limited for performance)
        viewModelScope.launch {
            LoggerOverlay.getStorage()?.getNetworkLogs()?.collect { logs ->
                _uiState.update {
                    it.copy(
                        networkLogs = logs.take(LoggerOverlayUiState.MAX_DISPLAYED_LOGS),
                        networkLogCount = logs.size
                    )
                }
                applyFilters()
            }
        }
    }
    
    fun selectTab(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
    }
    
    fun toggleSearch() {
        _uiState.update { it.copy(searchVisible = !it.searchVisible) }
    }
    
    fun updateSearch(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }
    
    fun showFilterSheet() {
        _uiState.update { it.copy(showFilterSheet = true) }
    }
    
    fun hideFilterSheet() {
        _uiState.update { it.copy(showFilterSheet = false) }
    }
    
    fun updateLevelFilters(levels: Set<LogLevel>) {
        _uiState.update { it.copy(selectedLevels = levels) }
        applyFilters()
    }
    
    fun updateNetworkFilter(filter: NetworkFilter) {
        _uiState.update { it.copy(networkFilter = filter) }
        applyFilters()
    }
    
    fun clearLogs() {
        LoggerOverlay.clearLogs()
    }
    
    private fun applyFilters() {
        val state = _uiState.value
        
        // Filter simple logs
        val filteredLogs = state.logs
            .filter { it.level in state.selectedLevels }
            .filter {
                if (state.searchQuery.isEmpty()) true
                else it.message.contains(state.searchQuery, ignoreCase = true) ||
                     it.tag.contains(state.searchQuery, ignoreCase = true)
            }
        
        // Filter network logs
        val filteredNetwork = state.networkLogs
            .filter {
                when (state.networkFilter) {
                    NetworkFilter.ALL -> true
                    NetworkFilter.SUCCESS -> it.responseCode in 200..299
                    NetworkFilter.ERROR -> it.responseCode != null && it.responseCode !in 200..299
                }
            }
            .filter {
                if (state.searchQuery.isEmpty()) true
                else it.url.contains(state.searchQuery, ignoreCase = true) ||
                     it.method.contains(state.searchQuery, ignoreCase = true)
            }
        
        _uiState.update {
            it.copy(
                filteredLogs = filteredLogs,
                filteredNetworkLogs = filteredNetwork
            )
        }
    }
}
