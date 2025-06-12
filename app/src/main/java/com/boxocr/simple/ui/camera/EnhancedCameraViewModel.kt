package com.boxocr.simple.ui.camera

import android.graphics.Bitmap
import android.net.Uri
import androidx.camera.core.ImageProxy
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boxocr.simple.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Enhanced Camera ViewModel - Phase 1 Week 2 Implementation
 * 
 * Manages multi-drug detection, live scanning, and enhanced camera functionality.
 * Integrates with new multi-drug scanner repository and existing camera systems.
 */
@HiltViewModel
class EnhancedCameraViewModel @Inject constructor(
    private val multiDrugScannerRepository: MultiDrugScannerRepository,
    private val smartCameraManager: SmartCameraManager,
    private val multiDrugObjectDetector: MultiDrugObjectDetector,
    private val batchScanningRepository: BatchScanningRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(EnhancedCameraUiState())
    val uiState: StateFlow<EnhancedCameraUiState> = _uiState.asStateFlow()
    
    private var currentPreviewView: PreviewView? = null
    private var lifecycleOwner: LifecycleOwner? = null
    
    init {
        // Observe scanner state
        viewModelScope.launch {
            multiDrugScannerRepository.scannerState
                .collect { scannerState ->
                    _uiState.update { it.copy(scannerState = scannerState) }
                }
        }
        
        // Observe live scanning state
        viewModelScope.launch {
            multiDrugScannerRepository.liveScanningActive
                .collect { isActive ->
                    _uiState.update { it.copy(isLiveScanActive = isActive) }
                }
        }
        
        // Observe detection state
        viewModelScope.launch {
            multiDrugObjectDetector.detectionState
                .collect { detectionState ->
                    when (detectionState) {
                        is MultiDrugDetectionState.Completed -> {
                            _uiState.update { 
                                it.copy(detectedRegions = detectionState.regions)
                            }
                        }
                        else -> { /* Handle other states if needed */ }
                    }
                }
        }
    }
    
    /**
     * Initialize camera with lifecycle
     */
    fun initializeCamera(lifecycleOwner: LifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner
        viewModelScope.launch {
            try {
                smartCameraManager.initializeCamera(lifecycleOwner)
                _uiState.update { it.copy(cameraInitialized = true) }
                
                // Start live detection if in appropriate mode
                if (_uiState.value.cameraMode != CameraMode.SINGLE_DRUG) {
                    startContinuousDetection()
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        scannerState = MultiDrugScannerState.Error("Kamera başlatılamadı: ${e.message}")
                    )
                }
            }
        }
    }
    
    /**
     * Bind camera preview
     */
    fun bindPreview(previewView: PreviewView) {
        currentPreviewView = previewView
        lifecycleOwner?.let { owner ->
            smartCameraManager.bindPreview(previewView, owner)
        }
    }
    
    /**
     * Set camera mode
     */
    fun setCameraMode(mode: CameraMode) {
        _uiState.update { it.copy(cameraMode = mode) }
        
        when (mode) {
            CameraMode.LIVE_SCAN -> {
                multiDrugScannerRepository.startLiveScanning()
            }
            CameraMode.SINGLE_DRUG -> {
                multiDrugScannerRepository.stopLiveScanning()
                _uiState.update { it.copy(detectedRegions = emptyList()) }
            }
            else -> {
                multiDrugScannerRepository.stopLiveScanning()
                startContinuousDetection()
            }
        }
    }
    
    /**
     * Capture image based on current mode
     */
    fun captureImage() {
        viewModelScope.launch {
            try {
                val bitmap = smartCameraManager.captureImage()
                
                when (_uiState.value.cameraMode) {
                    CameraMode.SINGLE_DRUG -> {
                        // Use existing single drug processing
                        processSingleDrugImage(bitmap)
                    }
                    CameraMode.MULTI_DRUG -> {
                        // Use multi-drug processing
                        processMultiDrugImage(bitmap)
                    }
                    CameraMode.LIVE_SCAN -> {
                        // Capture from live scanning
                        processLiveCaptureImage(bitmap)
                    }
                    CameraMode.DAMAGED_RECOVERY -> {
                        // Use damaged text recovery
                        processDamagedRecoveryImage(bitmap)
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        scannerState = MultiDrugScannerState.Error("Görüntü yakalanamadı: ${e.message}")
                    )
                }
            }
        }
    }
    
    /**
     * Process gallery images
     */
    fun processGalleryImages(uris: List<Uri>) {
        viewModelScope.launch {
            if (uris.size == 1) {
                // Single image processing
                val result = multiDrugScannerRepository.processGalleryImage(uris.first())
                _uiState.update { it.copy(multiDrugResult = result) }
            } else {
                // Batch processing
                val batchResult = multiDrugScannerRepository.processBatchImages(uris) { current, total ->
                    _uiState.update { 
                        it.copy(
                            batchCaptureCount = current,
                            batchTotalExpected = total
                        )
                    }
                }
                // Handle batch results
                handleBatchResults(batchResult)
            }
        }
    }
    
    /**
     * Toggle live scanning
     */
    fun toggleLiveScanning() {
        if (_uiState.value.isLiveScanActive) {
            multiDrugScannerRepository.stopLiveScanning()
        } else {
            multiDrugScannerRepository.startLiveScanning()
            setCameraMode(CameraMode.LIVE_SCAN)
        }
    }
    
    /**
     * Switch camera (front/back)
     */
    fun switchCamera() {
        viewModelScope.launch {
            try {
                smartCameraManager.switchCamera()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        scannerState = MultiDrugScannerState.Error("Kamera değiştirilemedi: ${e.message}")
                    )
                }
            }
        }
    }
    
    /**
     * Switch flash
     */
    fun switchFlash() {
        viewModelScope.launch {
            try {
                val isFlashOn = smartCameraManager.toggleFlash()
                _uiState.update { it.copy(flashEnabled = isFlashOn) }
            } catch (e: Exception) {
                // Handle flash toggle error
            }
        }
    }
    
    /**
     * Update detection settings
     */
    fun updateDetectionSettings(confidenceThreshold: Float, maxDetections: Int) {
        _uiState.update { 
            it.copy(
                confidenceThreshold = confidenceThreshold,
                maxDetections = maxDetections
            )
        }
        // Apply settings to detection system
        // This would be implemented in the detector
    }
    
    /**
     * Clear results
     */
    fun clearResults() {
        _uiState.update { 
            it.copy(
                multiDrugResult = null,
                batchCaptureCount = 0,
                batchTotalExpected = 0
            )
        }
    }
    
    // Private methods
    
    private suspend fun processSingleDrugImage(bitmap: Bitmap) {
        // Use existing single drug logic but wrap in multi-drug result
        val result = multiDrugScannerRepository.processCameraImage(bitmap)
        _uiState.update { it.copy(multiDrugResult = result) }
    }
    
    private suspend fun processMultiDrugImage(bitmap: Bitmap) {
        val result = multiDrugScannerRepository.processCameraImage(bitmap)
        _uiState.update { it.copy(multiDrugResult = result) }
    }
    
    private suspend fun processLiveCaptureImage(bitmap: Bitmap) {
        // Process captured frame from live scanning
        val result = multiDrugScannerRepository.processCameraImage(bitmap)
        _uiState.update { it.copy(multiDrugResult = result) }
    }
    
    private suspend fun processDamagedRecoveryImage(bitmap: Bitmap) {
        // Enhanced processing for damaged text recovery
        val result = multiDrugScannerRepository.processCameraImage(bitmap)
        _uiState.update { it.copy(multiDrugResult = result) }
    }
    
    private fun startContinuousDetection() {
        // This would start continuous object detection for multi-drug modes
        viewModelScope.launch {
            // Implementation would continuously analyze camera frames
            // for object detection without full OCR processing
        }
    }
    
    private suspend fun processLiveFrame(imageProxy: ImageProxy) {
        if (_uiState.value.cameraMode == CameraMode.LIVE_SCAN && _uiState.value.isLiveScanActive) {
            val liveResult = multiDrugScannerRepository.processLiveVideoFrame(imageProxy)
            
            _uiState.update { 
                it.copy(
                    frameQuality = liveResult.frameQuality,
                    detectedRegions = emptyList() // Would be updated with actual regions
                )
            }
            
            // Auto-capture if conditions are met
            if (liveResult.shouldCapture && liveResult.confidence > 0.85f) {
                // Trigger auto-capture
                captureImage()
            }
        }
    }
    
    private fun handleBatchResults(batchResult: BatchMultiDrugResult) {
        // Create a summary result for batch processing
        val successfulResults = batchResult.results.filter { it.success }
        val allDrugs = successfulResults.flatMap { it.enhancedResults }
        
        if (successfulResults.isNotEmpty()) {
            // Create aggregate result
            val aggregateResult = MultiDrugScanResult(
                originalImage = successfulResults.first().originalImage,
                ocrResult = null,
                enhancedResults = allDrugs,
                batchIntegration = null,
                processingTime = batchResult.results.sumOf { it.processingTime },
                success = true,
                timestamp = System.currentTimeMillis(),
                source = ImageSource.BATCH_UPLOAD
            )
            
            _uiState.update { 
                it.copy(
                    multiDrugResult = aggregateResult,
                    batchCaptureCount = 0,
                    batchTotalExpected = 0
                )
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        multiDrugObjectDetector.cleanup()
    }
}

/**
 * UI State for Enhanced Camera Screen
 */
data class EnhancedCameraUiState(
    val cameraMode: CameraMode = CameraMode.MULTI_DRUG,
    val cameraInitialized: Boolean = false,
    val flashEnabled: Boolean = false,
    val isLiveScanActive: Boolean = false,
    val detectedRegions: List<DrugBoxRegion> = emptyList(),
    val scannerState: MultiDrugScannerState = MultiDrugScannerState.Idle,
    val multiDrugResult: MultiDrugScanResult? = null,
    val frameQuality: FrameQuality = FrameQuality.GOOD,
    val imageWidth: Int = 1920,
    val imageHeight: Int = 1080,
    val previewWidth: Int = 1920,
    val previewHeight: Int = 1080,
    val batchCaptureCount: Int = 0,
    val batchTotalExpected: Int = 0,
    val confidenceThreshold: Float = 0.7f,
    val maxDetections: Int = 5
)
