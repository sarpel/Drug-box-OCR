package com.boxocr.simple.ui.home

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boxocr.simple.data.ScanResult
import com.boxocr.simple.repository.EnhancedDatabaseRepository
import com.boxocr.simple.repository.InMemoryDatabaseRepository
import com.boxocr.simple.repository.ScanHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * HomeViewModel - Manages database and recent scans
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val databaseRepository: InMemoryDatabaseRepository,
    private val enhancedDatabaseRepository: EnhancedDatabaseRepository,
    private val scanHistoryRepository: ScanHistoryRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        // Combine database and scan history flows
        viewModelScope.launch {
            combine(
                databaseRepository.databaseItems,
                databaseRepository.databaseError
            ) { databaseItems, databaseError ->
                _uiState.value.copy(
                    databaseItems = databaseItems,
                    databaseError = databaseError,
                    recentScans = scanHistoryRepository.getRecentScans()
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }
    
    /**
     * Load database from file URI into both repositories
     */
    fun loadDatabase(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                // Load into original repository
                databaseRepository.loadDatabase(uri)
                
                // Also load into enhanced repository
                val databaseItems = databaseRepository.databaseItems.value
                enhancedDatabaseRepository.loadDatabase(databaseItems)
                
            } catch (e: Exception) {
                // Error handling is done in the original repository
            }
        }
    }
    
    /**
     * Refresh recent scans from repository
     */
    fun refreshRecentScans() {
        _uiState.value = _uiState.value.copy(
            recentScans = scanHistoryRepository.getRecentScans()
        )
    }
}

/**
 * UI State for Home Screen
 */
data class HomeUiState(
    val databaseItems: List<String> = emptyList(),
    val recentScans: List<ScanResult> = emptyList(),
    val databaseError: String? = null
)
