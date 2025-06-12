package com.boxocr.simple.ui.camera

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.camera.view.PreviewView
import com.boxocr.simple.data.OCRResult
import com.boxocr.simple.data.VerificationData
import com.boxocr.simple.data.MatchResult
import com.boxocr.simple.repository.CameraManager
import com.boxocr.simple.repository.OCRRepository
import com.boxocr.simple.repository.InMemoryDatabaseRepository
import com.boxocr.simple.repository.ScanHistoryRepository
import com.boxocr.simple.repository.VerificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * CameraViewModel - Handles camera operations and OCR processing
 */
@HiltViewModel
class CameraViewModel @Inject constructor(
    private val cameraManager: CameraManager,
    private val ocrRepository: OCRRepository,
    private val databaseRepository: InMemoryDatabaseRepository,
    private val scanHistoryRepository: ScanHistoryRepository,
    private val verificationRepository: VerificationRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()
    
    fun initializeCamera(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        viewModelScope.launch {
            val success = cameraManager.initializeCamera(lifecycleOwner, previewView)
            _uiState.value = _uiState.value.copy(isCameraReady = success)
        }
    }
    
    fun capturePhoto() {
        if (_uiState.value.isProcessing) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true)
            
            try {
                // Capture photo
                val imagePath = cameraManager.capturePhoto()
                if (imagePath == null) {
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        error = "Failed to capture image"
                    )
                    return@launch
                }
                
                // Process with OCR
                when (val result = ocrRepository.extractTextFromImage(imagePath)) {
                    is OCRResult.Success -> {
                        // Find best match in database
                        val matchResult = databaseRepository.findBestMatch(result.extractedText)
                        
                        val resultData = OCRResultData(
                            scannedText = result.extractedText,
                            matchedText = matchResult.bestMatch,
                            confidence = matchResult.confidence
                        )
                        
                        // Add to recent scans
                        scanHistoryRepository.addScanResult(
                            scannedText = result.extractedText,
                            matchedText = matchResult.bestMatch
                        )
                        
                        _uiState.value = _uiState.value.copy(
                            isProcessing = false,
                            ocrResult = resultData
                        )
                    }
                    
                    is OCRResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isProcessing = false,
                            error = result.message
                        )
                    }
                    
                    is OCRResult.Loading -> {
                        // Continue processing
                    }
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    error = "Camera capture failed: ${e.message}"
                )
            }
        }
    }
    
    fun toggleTorch() {
        val currentState = _uiState.value.isTorchOn
        cameraManager.enableTorchlight(!currentState)
        _uiState.value = _uiState.value.copy(isTorchOn = !currentState)
    }
    
    fun clearOCRResult() {
        _uiState.value = _uiState.value.copy(ocrResult = null)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    /**
     * Capture photo for verification workflow - Phase 2 Feature
     * Saves image and creates verification data for preview screen
     */
    fun captureForVerification() {
        if (_uiState.value.isProcessing) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true)
            
            try {
                // Capture photo
                val imagePath = cameraManager.capturePhoto()
                if (imagePath == null) {
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        error = "Failed to capture image"
                    )
                    return@launch
                }
                
                // Save image to cache for verification
                val savedImagePath = saveImageForVerification(imagePath)
                
                // Process with OCR
                when (val result = ocrRepository.extractTextFromImage(imagePath)) {
                    is OCRResult.Success -> {
                        // Find best match in database
                        val matchResult = databaseRepository.findBestMatch(result.extractedText)
                        
                        // Create verification data
                        val verificationData = VerificationData(
                            capturedImagePath = savedImagePath,
                            ocrText = result.extractedText,
                            matchResult = MatchResult(
                                originalText = result.extractedText,
                                bestMatch = matchResult.bestMatch,
                                confidence = matchResult.confidence
                            ),
                            timestamp = System.currentTimeMillis()
                        )
                        
                        // Store verification data in repository for navigation
                        verificationRepository.setVerificationData(verificationData)
                        
                        _uiState.value = _uiState.value.copy(
                            isProcessing = false,
                            verificationData = verificationData
                        )
                    }
                    
                    is OCRResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isProcessing = false,
                            error = result.message
                        )
                    }
                    
                    is OCRResult.Loading -> {
                        // Continue processing
                    }
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    error = "Camera capture failed: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Save captured image to app cache for verification preview
     */
    private fun saveImageForVerification(originalImagePath: String): String {
        // Implementation would copy the image to app cache directory
        // For now, return the original path (could be enhanced with actual copying)
        return originalImagePath
    }
    
    /**
     * Clear verification data after navigation
     */
    fun clearVerificationData() {
        _uiState.value = _uiState.value.copy(verificationData = null)
    }
    
    override fun onCleared() {
        super.onCleared()
        cameraManager.release()
    }
}

/**
 * UI State for Camera Screen
 */
data class CameraUiState(
    val isCameraReady: Boolean = false,
    val isProcessing: Boolean = false,
    val isTorchOn: Boolean = false,
    val ocrResult: OCRResultData? = null,
    val verificationData: VerificationData? = null,
    val error: String? = null
)

/**
 * OCR Result data for UI display
 */
data class OCRResultData(
    val scannedText: String,
    val matchedText: String?,
    val confidence: Float
)
