package com.example

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardUiState(
    val stats: SystemStats? = null,
    val cpuHistory: List<Float> = emptyList(),
    val gpuHistory: List<Float> = emptyList(),
    val downloadHistory: List<Float> = emptyList(),
    val uploadHistory: List<Float> = emptyList()
)

class DashboardViewModel(repository: SystemStatsRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    init {
        viewModelScope.launch {
            repository.getStatsFlow().collect { stats ->
                _uiState.update { currentState ->
                    currentState.copy(
                        stats = stats,
                        cpuHistory = (currentState.cpuHistory + stats.cpuUsagePercent).takeLast(20),
                        gpuHistory = (currentState.gpuHistory + stats.gpuUsagePercent).takeLast(20),
                        downloadHistory = (currentState.downloadHistory + stats.downloadSpeedKbps).takeLast(20),
                        uploadHistory = (currentState.uploadHistory + stats.uploadSpeedKbps).takeLast(20)
                    )
                }
            }
        }
    }

    class Factory(private val repository: SystemStatsRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DashboardViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
