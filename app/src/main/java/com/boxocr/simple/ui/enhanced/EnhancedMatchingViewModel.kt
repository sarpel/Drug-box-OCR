package com.boxocr.simple.ui.enhanced

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boxocr.simple.repository.EnhancedDatabaseRepository
import com.boxocr.simple.repository.OCRRepository
import com.boxocr.simple.repository.ScanHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for enhanced drug matching with multiple algorithms and suggestions
 */
@HiltViewModel
class EnhancedMatchingViewModel @Inject constructor(
    private val enhancedDatabaseRepository: EnhancedDatabaseRepository,
    private val ocrRepository: OCRRepository,
    private val historyRepository: ScanHistoryRepository
) : ViewModel() {
    
    // UI State
    private val _uiState = MutableStateFlow(EnhancedMatchingUiState())
    val uiState: StateFlow<EnhancedMatchingUiState> = _uiState.asStateFlow()
    
    // Current matching result
    private val _matchingResult = MutableStateFlow<EnhancedDatabaseRepository.MultipleMatchResult?>(null)
    val matchingResult: StateFlow<EnhancedDatabaseRepository.MultipleMatchResult?> = _matchingResult.asStateFlow()
    
    // Category settings
    private val _categorySettings = MutableStateFlow<Map<String, Int>>(emptyMap())
    val categorySettings: StateFlow<Map<String, Int>> = _categorySettings.asStateFlow()
    
    data class EnhancedMatchingUiState(
        val isProcessing: Boolean = false,
        val currentQuery: String = "",
        val showMatchResults: Boolean = false,
        val selectedMatch: EnhancedDatabaseRepository.MatchResult? = null,
        val error: String = "",
        val scannedText: String = "",
        val imageProcessed: Boolean = false,
        val showCategorySettings: Boolean = false
    )
    
    init {
        loadCategorySettings()
    }
    
    /**
     * Process scanned image with enhanced OCR and matching
     */
    fun processScannedImage(imageBytes: ByteArray) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true, error = "")
            
            try {
                // Step 1: OCR Processing
                val ocrResult = ocrRepository.processImage(imageBytes)
                
                if (ocrResult.isSuccess && ocrResult.extractedText.isNotBlank()) {
                    val extractedText = ocrResult.extractedText
                    
                    _uiState.value = _uiState.value.copy(
                        scannedText = extractedText,
                        currentQuery = extractedText,
                        imageProcessed = true
                    )
                    
                    // Step 2: Enhanced Database Matching
                    performEnhancedMatching(extractedText)
                    
                } else {
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        error = ocrResult.error ?: "Failed to extract text from image"
                    )
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    error = "Processing failed: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Perform enhanced matching with multiple algorithms
     */
    fun performEnhancedMatching(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true, currentQuery = query)
            
            try {
                val matchResult = enhancedDatabaseRepository.findMultipleMatches(query)
                _matchingResult.value = matchResult
                
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    showMatchResults = true,
                    selectedMatch = matchResult.primaryMatch
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    error = "Matching failed: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Select a specific match result
     */
    fun selectMatch(match: EnhancedDatabaseRepository.MatchResult) {
        _uiState.value = _uiState.value.copy(selectedMatch = match)
    }
    
    /**
     * Confirm selected match and add to history
     */
    fun confirmSelectedMatch(): String? {
        val selectedMatch = _uiState.value.selectedMatch
        return if (selectedMatch != null) {
            // Add to history
            historyRepository.addScanResult(selectedMatch.drugName)
            
            // Reset UI
            _uiState.value = _uiState.value.copy(
                showMatchResults = false,
                selectedMatch = null,
                currentQuery = "",
                scannedText = "",
                imageProcessed = false
            )
            _matchingResult.value = null
            
            selectedMatch.drugName
        } else {
            null
        }
    }
    
    /**
     * Reject current matches and rescan
     */
    fun rejectMatches() {
        _uiState.value = _uiState.value.copy(
            showMatchResults = false,
            selectedMatch = null,
            error = "Please try scanning again or enter manually"
        )
        _matchingResult.value = null
    }
    
    /**
     * Manual entry for when no good matches found
     */
    fun enterManually(drugName: String): String? {
        return if (drugName.isNotBlank()) {
            historyRepository.addScanResult(drugName)
            
            _uiState.value = _uiState.value.copy(
                showMatchResults = false,
                selectedMatch = null,
                currentQuery = "",
                scannedText = "",
                imageProcessed = false
            )
            _matchingResult.value = null
            
            drugName
        } else {
            null
        }
    }
    
    /**
     * Show category threshold settings
     */
    fun showCategorySettings() {
        _uiState.value = _uiState.value.copy(showCategorySettings = true)
        loadCategorySettings()
    }
    
    /**
     * Hide category settings
     */
    fun hideCategorySettings() {
        _uiState.value = _uiState.value.copy(showCategorySettings = false)
    }
    
    /**
     * Update category threshold
     */
    fun updateCategoryThreshold(category: String, threshold: Int) {
        enhancedDatabaseRepository.setCategoryThreshold(category, threshold)
        loadCategorySettings()
    }
    
    /**
     * Load current category settings
     */
    private fun loadCategorySettings() {
        val categories = enhancedDatabaseRepository.getDrugCategories()
        val settings = categories.associateWith { category ->
            enhancedDatabaseRepository.getCategoryThreshold(category)
        }
        _categorySettings.value = settings
    }
    
    /**
     * Load database into enhanced repository
     */
    fun loadDatabase(items: List<String>) {
        enhancedDatabaseRepository.loadDatabase(items)
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = "")
    }
    
    /**
     * Reset UI state
     */
    fun resetState() {
        _uiState.value = EnhancedMatchingUiState()
        _matchingResult.value = null
    }
    
    /**
     * Get recommended action text for UI
     */
    fun getRecommendedActionText(action: EnhancedDatabaseRepository.RecommendedAction): String {
        return when (action) {
            EnhancedDatabaseRepository.RecommendedAction.AUTO_SELECT -> 
                "High confidence match - automatically selected"
            EnhancedDatabaseRepository.RecommendedAction.SHOW_OPTIONS -> 
                "Multiple good matches found - please choose"
            EnhancedDatabaseRepository.RecommendedAction.MANUAL_ENTRY -> 
                "Low confidence matches - consider manual entry"
            EnhancedDatabaseRepository.RecommendedAction.RESCAN -> 
                "No good matches found - try scanning again"
        }
    }
    
    /**
     * Get match type color for UI
     */
    fun getMatchTypeColor(matchType: EnhancedDatabaseRepository.MatchType): androidx.compose.ui.graphics.Color {
        return when (matchType) {
            EnhancedDatabaseRepository.MatchType.EXACT -> 
                androidx.compose.ui.graphics.Color(0xFF4CAF50) // Green
            EnhancedDatabaseRepository.MatchType.HIGH_CONFIDENCE -> 
                androidx.compose.ui.graphics.Color(0xFF2196F3) // Blue
            EnhancedDatabaseRepository.MatchType.MEDIUM_CONFIDENCE -> 
                androidx.compose.ui.graphics.Color(0xFFFF9800) // Orange
            EnhancedDatabaseRepository.MatchType.LOW_CONFIDENCE -> 
                androidx.compose.ui.graphics.Color(0xFFF44336) // Red
            EnhancedDatabaseRepository.MatchType.NO_MATCH -> 
                androidx.compose.ui.graphics.Color(0xFF9E9E9E) // Gray
        }
    }
}
