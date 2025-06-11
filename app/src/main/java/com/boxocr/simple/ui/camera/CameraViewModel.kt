package com.boxocr.simple.ui.camera

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.camera.view.PreviewView
import com.boxocr.simple.data.OCRResult
import com.boxocr.simple.repository.CameraManager
import com.boxocr.simple.repository.OCRRepository
import com.boxocr.simple.repository.InMemoryDatabaseRepository
import com.boxocr.simple.repository.ScanHistoryRepository
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
    private val scanHistoryRepository: ScanHistoryRepository
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
