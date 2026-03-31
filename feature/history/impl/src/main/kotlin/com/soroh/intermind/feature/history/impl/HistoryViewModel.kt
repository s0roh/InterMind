package com.soroh.intermind.feature.history.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soroh.intermind.core.data.dto.history.TrainingHistoryItem
import com.soroh.intermind.core.data.repository.HistoryRepository
import com.soroh.intermind.feature.history.impl.util.formatToDateHeader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            if (!_isRefreshing.value) {
                _uiState.value = HistoryUiState.Loading
            }

            try {
                val history = historyRepository.getTrainingHistory()

                val groupedHistory: Map<String, List<TrainingHistoryItem>> = history
                    .sortedByDescending { it.timestamp }
                    .groupBy { it.timestamp.formatToDateHeader() }

                _uiState.value = HistoryUiState.Success(groupedHistory)

            } catch (e: Exception) {
                _uiState.value = HistoryUiState.Error(e.message ?: "Unknown error")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun onRefresh() {
        _isRefreshing.value = true
        loadHistory()
    }
}

sealed interface HistoryUiState {
    data object Loading : HistoryUiState
    data class Error(val message: String) : HistoryUiState
    data class Success(val groupedHistory: Map<String, List<TrainingHistoryItem>>) : HistoryUiState
}