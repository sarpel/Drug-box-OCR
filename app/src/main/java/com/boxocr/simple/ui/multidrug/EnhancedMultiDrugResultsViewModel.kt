package com.boxocr.simple.ui.multidrug

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boxocr.simple.data.*
import com.boxocr.simple.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * Enhanced ViewModel for Multi-Drug Results with comprehensive state management
 * Handles verification, correction, export, and batch processing coordination
 */

@HiltViewModel
class EnhancedMultiDrugResultsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val multiDrugScannerRepository: MultiDrugScannerRepository,
    private val batchScanningRepository: BatchScanningRepository,
    private val visualDrugDatabaseRepository: VisualDrugDatabaseRepository,
    private val damagedTextRecoveryRepository: DamagedTextRecoveryRepository,
    private val exportRepository: ExportRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EnhancedMultiDrugResultsUiState())
    val uiState: StateFlow<EnhancedMultiDrugResultsUiState> = _uiState.asStateFlow()

    private val _processingStatus = MutableStateFlow(MultiDrugProcessingStatus())
    val processingStatus: StateFlow<MultiDrugProcessingStatus> = _processingStatus.asStateFlow()

    init {
        // Observe processing status from repository
        viewModelScope.launch {
            multiDrugScannerRepository.processingStatus.collect { status ->
                _processingStatus.value = status
                _uiState.value = _uiState.value.copy(
                    isProcessing = status.isProcessing,
                    processingStatus = status
                )
            }
        }
    }

    /**
     * Load multi-drug results from the repository
     */
    fun loadResults() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                // Get latest multi-drug scanning results
                val results = multiDrugScannerRepository.getLatestResults()
                
                _uiState.value = _uiState.value.copy(
                    results = results,
                    isLoading = false
                )
                
                // Auto-verify high-confidence results if enabled
                autoVerifyHighConfidenceResults()
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "Failed to load results: ${e.message}"
                )
            }
        }
    }

    /**
     * Verify a specific result at the given index
     */
    fun verifyResult(index: Int) {
        viewModelScope.launch {
            try {
                val currentResults = _uiState.value.results.toMutableList()
                if (index in currentResults.indices) {
                    val result = currentResults[index]
                    val verifiedResult = result.copy(
                        isVerified = true,
                        verificationTimestamp = System.currentTimeMillis()
                    )
                    
                    currentResults[index] = verifiedResult
                    
                    _uiState.value = _uiState.value.copy(results = currentResults)
                    
                    // Save verification to repository
                    multiDrugScannerRepository.updateResult(index, verifiedResult)
                    
                    showMessage("Drug verified successfully")
                }
            } catch (e: Exception) {
                showMessage("Failed to verify: ${e.message}")
            }
        }
    }

    /**
     * Correct a result with user input
     */
    fun correctResult(index: Int, correction: String) {
        viewModelScope.launch {
            try {
                val currentResults = _uiState.value.results.toMutableList()
                if (index in currentResults.indices) {
                    val result = currentResults[index]
                    
                    // Create corrected result
                    val correctedResult = result.copy(
                        detectedName = correction,
                        isCorrected = true,
                        originalDetectedName = result.detectedName,
                        correctionTimestamp = System.currentTimeMillis(),
                        confidence = 1.0f // User correction gets maximum confidence
                    )
                    
                    currentResults[index] = correctedResult
                    
                    _uiState.value = _uiState.value.copy(results = currentResults)
                    
                    // Save correction to repository and learning system
                    multiDrugScannerRepository.updateResult(index, correctedResult)
                    multiDrugScannerRepository.learnFromCorrection(
                        originalText = result.detectedName,
                        correctedText = correction,
                        contextImage = result.croppedDrugBoxBitmap
                    )
                    
                    showMessage("Drug name corrected successfully")
                }
            } catch (e: Exception) {
                showMessage("Failed to correct: ${e.message}")
            }
        }
    }

    /**
     * Remove a result from the list
     */
    fun removeResult(index: Int) {
        viewModelScope.launch {
            try {
                val currentResults = _uiState.value.results.toMutableList()
                if (index in currentResults.indices) {
                    currentResults.removeAt(index)
                    _uiState.value = _uiState.value.copy(results = currentResults)
                    
                    multiDrugScannerRepository.removeResult(index)
                    showMessage("Drug removed successfully")
                }
            } catch (e: Exception) {
                showMessage("Failed to remove: ${e.message}")
            }
        }
    }

    /**
     * Show detailed information for a specific result
     */
    fun showDetails(index: Int) {
        viewModelScope.launch {
            try {
                val currentResults = _uiState.value.results
                if (index in currentResults.indices) {
                    val result = currentResults[index]
                    
                    // Load detailed information including visual matches
                    val detailedInfo = multiDrugScannerRepository.getDetailedInfo(result)
                    
                    _uiState.value = _uiState.value.copy(
                        selectedResultDetails = detailedInfo
                    )
                }
            } catch (e: Exception) {
                showMessage("Failed to load details: ${e.message}")
            }
        }
    }

    /**
     * Select all verified results
     */
    fun selectAll() {
        viewModelScope.launch {
            val currentResults = _uiState.value.results.map { result ->
                result.copy(isVerified = true, verificationTimestamp = System.currentTimeMillis())
            }
            
            _uiState.value = _uiState.value.copy(results = currentResults)
            
            // Save all verifications
            currentResults.forEachIndexed { index, result ->
                multiDrugScannerRepository.updateResult(index, result)
            }
            
            showMessage("All drugs selected")
        }
    }

    /**
     * Clear all selections
     */
    fun clearSelection() {
        viewModelScope.launch {
            val currentResults = _uiState.value.results.map { result ->
                result.copy(isVerified = false, verificationTimestamp = 0L)
            }
            
            _uiState.value = _uiState.value.copy(results = currentResults)
            
            // Save deselections
            currentResults.forEachIndexed { index, result ->
                multiDrugScannerRepository.updateResult(index, result)
            }
            
            showMessage("All selections cleared")
        }
    }

    /**
     * Retry failed detections with enhanced recovery
     */
    fun retryFailedDetections() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isProcessing = true)
                
                val currentResults = _uiState.value.results.toMutableList()
                val failedResults = currentResults.filter { it.confidence < 0.6f }
                
                showMessage("Retrying ${failedResults.size} failed detections...")
                
                failedResults.forEachIndexed { index, result ->
                    try {
                        // Update processing status
                        _processingStatus.value = _processingStatus.value.copy(
                            currentStage = "Retrying detection ${index + 1}/${failedResults.size}",
                            progress = index.toFloat() / failedResults.size
                        )
                        
                        // Retry with enhanced text recovery
                        val enhancedResult = result.croppedDrugBoxBitmap?.let { bitmap ->
                            damagedTextRecoveryRepository.recoverDamagedText(
                                partialText = result.ocrText,
                                image = bitmap
                            )
                        }
                        
                        enhancedResult?.let { recovery ->
                            val resultIndex = currentResults.indexOf(result)
                            if (resultIndex != -1) {
                                currentResults[resultIndex] = result.copy(
                                    detectedName = recovery.recoveredText,
                                    confidence = recovery.confidence,
                                    recoveryMethod = recovery.recoveryMethod,
                                    isRetried = true
                                )
                            }
                        }
                        
                        delay(100) // Small delay for UI feedback
                        
                    } catch (e: Exception) {
                        // Continue with other retries even if one fails
                    }
                }
                
                _uiState.value = _uiState.value.copy(
                    results = currentResults,
                    isProcessing = false
                )
                
                _processingStatus.value = _processingStatus.value.copy(
                    currentStage = "Retry complete",
                    progress = 1.0f,
                    isProcessing = false
                )
                
                showMessage("Retry completed")
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isProcessing = false)
                showMessage("Retry failed: ${e.message}")
            }
        }
    }

    /**
     * Export results in various formats
     */
    fun exportResults() {
        viewModelScope.launch {
            try {
                val results = _uiState.value.results
                val verifiedResults = results.filter { it.isVerified }
                
                if (verifiedResults.isEmpty()) {
                    showMessage("No verified results to export")
                    return@launch
                }
                
                // Export in multiple formats
                val exportData = ExportData(
                    timestamp = System.currentTimeMillis(),
                    drugResults = verifiedResults,
                    totalDrugs = verifiedResults.size,
                    averageConfidence = verifiedResults.map { it.confidence }.average().toFloat(),
                    processingTime = verifiedResults.sumOf { it.processingTimeMs.toLong() }
                )
                
                val exportPath = exportRepository.exportMultiDrugResults(exportData)
                showMessage("Results exported to: $exportPath")
                
            } catch (e: Exception) {
                showMessage("Export failed: ${e.message}")
            }
        }
    }

    /**
     * Auto-verify results with high confidence and visual matches
     */
    private fun autoVerifyHighConfidenceResults() {
        viewModelScope.launch {
            try {
                val currentResults = _uiState.value.results.toMutableList()
                var autoVerifiedCount = 0
                
                currentResults.forEachIndexed { index, result ->
                    // Auto-verify if confidence > 90% and has visual match
                    if (result.confidence >= 0.9f && result.hasVisualMatch && !result.isVerified) {
                        currentResults[index] = result.copy(
                            isVerified = true,
                            verificationTimestamp = System.currentTimeMillis(),
                            isAutoVerified = true
                        )
                        autoVerifiedCount++
                    }
                }
                
                if (autoVerifiedCount > 0) {
                    _uiState.value = _uiState.value.copy(results = currentResults)
                    
                    // Save auto-verifications
                    currentResults.forEachIndexed { index, result ->
                        if (result.isAutoVerified) {
                            multiDrugScannerRepository.updateResult(index, result)
                        }
                    }
                    
                    showMessage("$autoVerifiedCount drugs auto-verified")
                }
                
            } catch (e: Exception) {
                // Silent failure for auto-verification
            }
        }
    }

    /**
     * Process batch workflow with verified results
     */
    fun processBatch(drugNames: List<String>) {
        viewModelScope.launch {
            try {
                if (drugNames.isEmpty()) {
                    showMessage("No drugs selected for batch processing")
                    return@launch
                }
                
                // Create batch session with multi-drug results
                val batchSession = BatchSession(
                    id = "multi_drug_${System.currentTimeMillis()}",
                    drugNames = drugNames,
                    createdAt = System.currentTimeMillis(),
                    source = "multi_drug_scanner"
                )
                
                batchScanningRepository.createSession(batchSession)
                showMessage("Batch processing started for ${drugNames.size} drugs")
                
            } catch (e: Exception) {
                showMessage("Failed to start batch processing: ${e.message}")
            }
        }
    }

    /**
     * Clear any displayed message
     */
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    /**
     * Show a message to the user
     */
    private fun showMessage(message: String) {
        _uiState.value = _uiState.value.copy(message = message)
    }
}

/**
 * UI State for Enhanced Multi-Drug Results Screen
 */
data class EnhancedMultiDrugResultsUiState(
    val results: List<MultiDrugResult> = emptyList(),
    val isLoading: Boolean = false,
    val isProcessing: Boolean = false,
    val processingStatus: MultiDrugProcessingStatus = MultiDrugProcessingStatus(),
    val selectedResultDetails: MultiDrugResultDetails? = null,
    val message: String? = null
)

/**
 * Detailed information for a specific multi-drug result
 */
data class MultiDrugResultDetails(
    val result: MultiDrugResult,
    val visualMatches: List<VisualSimilarityMatch>,
    val alternativeNames: List<String>,
    val processingHistory: List<ProcessingStep>,
    val qualityMetrics: ImageQualityMetrics
)

/**
 * Processing step for result history
 */
data class ProcessingStep(
    val step: String,
    val timestamp: Long,
    val result: String,
    val confidence: Float,
    val processingTimeMs: Long
)

/**
 * Image quality metrics for drug box regions
 */
data class ImageQualityMetrics(
    val resolution: Float,
    val sharpness: Float,
    val contrast: Float,
    val textRegionQuality: Float,
    val lightingQuality: Float,
    val overallScore: Float,
    val recommendations: List<String>
)

/**
 * Export data structure for multi-drug results
 */
data class ExportData(
    val timestamp: Long,
    val drugResults: List<MultiDrugResult>,
    val totalDrugs: Int,
    val averageConfidence: Float,
    val processingTime: Long,
    val format: String = "multi_drug_export"
)
