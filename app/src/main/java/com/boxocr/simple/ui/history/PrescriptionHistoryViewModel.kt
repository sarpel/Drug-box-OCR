package com.boxocr.simple.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boxocr.simple.data.PrescriptionHistory
import com.boxocr.simple.data.PrescriptionStatistics
import com.boxocr.simple.repository.PrescriptionHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * Prescription History ViewModel - Phase 3 Quality of Life Feature
 * 
 * Manages prescription history data, filtering, statistics, and export functionality
 */
@HiltViewModel
class PrescriptionHistoryViewModel @Inject constructor(
    private val historyRepository: PrescriptionHistoryRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PrescriptionHistoryUiState())
    val uiState: StateFlow<PrescriptionHistoryUiState> = _uiState.asStateFlow()
    
    init {
        loadHistoryData()
    }
    
    /**
     * Load prescription history and statistics
     */
    private fun loadHistoryData() {
        viewModelScope.launch {
            combine(
                historyRepository.getAllHistory(),
                historyRepository.getStatistics()
            ) { history, statistics ->
                val filtered = applyFilter(history, _uiState.value.currentFilter)
                _uiState.value.copy(
                    allHistory = history,
                    filteredHistory = filtered,
                    statistics = statistics,
                    isLoading = false
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }
    
    /**
     * Update filter and refresh filtered results
     */
    fun updateFilter(filter: HistoryFilter) {
        val currentState = _uiState.value
        val filteredHistory = applyFilter(currentState.allHistory, filter)
        
        _uiState.value = currentState.copy(
            currentFilter = filter,
            filteredHistory = filteredHistory
        )
    }
    
    /**
     * Apply filter to history list
     */
    private fun applyFilter(history: List<PrescriptionHistory>, filter: HistoryFilter): List<PrescriptionHistory> {
        return history.filter { prescription ->
            // Time period filter
            val timeMatches = when (filter.timePeriod) {
                TimePeriod.ALL -> true
                TimePeriod.TODAY -> isToday(prescription.timestamp)
                TimePeriod.WEEK -> isThisWeek(prescription.timestamp)
                TimePeriod.MONTH -> isThisMonth(prescription.timestamp)
            }
            
            // Patient name filter
            val patientMatches = if (filter.patientName.isBlank()) {
                true
            } else {
                prescription.patientInfo?.name?.contains(filter.patientName, ignoreCase = true) == true
            }
            
            // Status filter
            val statusMatches = filter.status?.let { status ->
                prescription.status == status
            } ?: true
            
            timeMatches && patientMatches && statusMatches
        }.sortedByDescending { it.timestamp }
    }
    
    /**
     * Select prescription for detailed view
     */
    fun selectPrescription(prescription: PrescriptionHistory) {
        _uiState.value = _uiState.value.copy(selectedPrescription = prescription)
    }
    
    /**
     * Clear prescription selection
     */
    fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedPrescription = null)
    }
    
    /**
     * Export history in specified format
     */
    fun exportHistory(format: ExportFormat) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true)
            
            try {
                val exportFile = when (format) {
                    ExportFormat.CSV -> historyRepository.exportToCSV(_uiState.value.filteredHistory)
                    ExportFormat.PDF -> historyRepository.exportToPDF(_uiState.value.filteredHistory)
                }
                
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    exportedFile = exportFile
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    error = "Export failed: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    /**
     * Clear exported file reference
     */
    fun clearExportedFile() {
        _uiState.value = _uiState.value.copy(exportedFile = null)
    }
    
    /**
     * Delete prescription from history
     */
    fun deletePrescription(prescriptionId: String) {
        viewModelScope.launch {
            try {
                historyRepository.deletePrescription(prescriptionId)
                // History will be automatically updated via flow
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to delete prescription: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Search prescriptions by text
     */
    fun searchPrescriptions(query: String) {
        val currentState = _uiState.value
        val filteredHistory = if (query.isBlank()) {
            applyFilter(currentState.allHistory, currentState.currentFilter)
        } else {
            applyFilter(currentState.allHistory, currentState.currentFilter).filter { prescription ->
                // Search in drugs, patient name, or prescription ID
                prescription.drugs.any { it.contains(query, ignoreCase = true) } ||
                prescription.patientInfo?.name?.contains(query, ignoreCase = true) == true ||
                prescription.id.contains(query, ignoreCase = true)
            }
        }
        
        _uiState.value = currentState.copy(
            searchQuery = query,
            filteredHistory = filteredHistory
        )
    }
    
    /**
     * Time period helper functions
     */
    private fun isToday(timestamp: Long): Boolean {
        val now = System.currentTimeMillis()
        val dayInMillis = 24 * 60 * 60 * 1000
        return (now - timestamp) < dayInMillis
    }
    
    private fun isThisWeek(timestamp: Long): Boolean {
        val now = System.currentTimeMillis()
        val weekInMillis = 7 * 24 * 60 * 60 * 1000
        return (now - timestamp) < weekInMillis
    }
    
    private fun isThisMonth(timestamp: Long): Boolean {
        val now = System.currentTimeMillis()
        val monthInMillis = 30L * 24 * 60 * 60 * 1000
        return (now - timestamp) < monthInMillis
    }
}

/**
 * UI State for Prescription History Screen
 */
data class PrescriptionHistoryUiState(
    val isLoading: Boolean = true,
    val allHistory: List<PrescriptionHistory> = emptyList(),
    val filteredHistory: List<PrescriptionHistory> = emptyList(),
    val statistics: PrescriptionStatistics = PrescriptionStatistics(),
    val currentFilter: HistoryFilter = HistoryFilter(),
    val searchQuery: String = "",
    val selectedPrescription: PrescriptionHistory? = null,
    val isExporting: Boolean = false,
    val exportedFile: File? = null,
    val error: String? = null
)
