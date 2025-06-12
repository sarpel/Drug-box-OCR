package com.boxocr.simple.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.camera.core.ImageProxy
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Multi-Drug Scanner Repository - Phase 1 Foundation Enhancement
 * 
 * Main orchestrator that coordinates multi-drug detection, OCR processing,
 * and visual database matching for the revolutionary multi-drug scanner.
 * 
 * Integrates with existing batch scanning workflow and Windows automation.
 */
@Singleton
class MultiDrugScannerRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val multiDrugObjectDetector: MultiDrugObjectDetector,
    private val multiRegionOCRRepository: MultiRegionOCRRepository,
    private val visualDrugDatabaseRepository: VisualDrugDatabaseRepository,
    private val batchScanningRepository: BatchScanningRepository,
    private val turkishDrugDatabaseRepository: TurkishDrugDatabaseRepository
) {
    
    // Combined processing state
    private val _scannerState = MutableStateFlow<MultiDrugScannerState>(
        MultiDrugScannerState.Idle
    )
    val scannerState: StateFlow<MultiDrugScannerState> = _scannerState.asStateFlow()
    
    // Live scanning state for video processing
    private val _liveScanningActive = MutableStateFlow(false)
    val liveScanningActive: StateFlow<Boolean> = _liveScanningActive.asStateFlow()
    
    // Recent results for continuity
    private val recentResults = mutableListOf<MultiDrugScanResult>()
    
    /**
     * Process image from camera capture
     */
    suspend fun processCameraImage(bitmap: Bitmap): MultiDrugScanResult {
        return processMultiDrugImage(
            ImageSource.CAMERA_CAPTURE,
            bitmap
        )
    }
    
    /**
     * Process image from gallery
     */
    suspend fun processGalleryImage(uri: Uri): MultiDrugScanResult {
        return try {
            val bitmap = loadBitmapFromUri(uri)
            processMultiDrugImage(
                ImageSource.GALLERY_IMPORT,
                bitmap
            )
        } catch (e: Exception) {
            MultiDrugScanResult.error("Galeri resmi yüklenemedi: ${e.message}")
        }
    }
    
    /**
     * Process live video frame
     */
    suspend fun processLiveVideoFrame(imageProxy: ImageProxy): LiveScanResult {
        return try {
            val bitmap = convertImageProxyToBitmap(imageProxy)
            
            // Use multi-region OCR for live processing
            val liveResult = multiRegionOCRRepository.processLiveFrame(
                frame = bitmap,
                previousResults = recentResults.map { it.ocrResult }.filterNotNull()
            )
            
            LiveScanResult(
                detectedDrugs = liveResult.result?.drugNames ?: emptyList(),
                regionCount = liveResult.detectedRegionCount,
                frameQuality = liveResult.frameQuality,
                shouldCapture = liveResult.shouldCapture,
                confidence = liveResult.result?.averageConfidence ?: 0f,
                processingTime = liveResult.result?.processingTime ?: 0L
            )
            
        } catch (e: Exception) {
            LiveScanResult.error("Canlı tarama hatası: ${e.message}")
        }
    }
    
    /**
     * Main multi-drug image processing pipeline
     */
    private suspend fun processMultiDrugImage(
        source: ImageSource,
        bitmap: Bitmap
    ): MultiDrugScanResult {
        return try {
            _scannerState.value = MultiDrugScannerState.Processing(
                "Çoklu ilaç taraması başlatılıyor..."
            )
            
            val startTime = System.currentTimeMillis()
            
            // Step 1: Multi-region OCR processing
            _scannerState.value = MultiDrugScannerState.Processing("İlaç kutuları tespit ediliyor...")
            val ocrResult = multiRegionOCRRepository.processMultiRegionImage(bitmap)
            
            if (!ocrResult.success) {
                return MultiDrugScanResult.error("OCR işlemi başarısız: ${ocrResult.errorMessage}")
            }
            
            // Step 2: Enhanced matching with visual database
            _scannerState.value = MultiDrugScannerState.Processing("Görsel eşleştirme yapılıyor...")
            val enhancedResults = enhanceResultsWithVisualDatabase(ocrResult)
            
            // Step 3: Integrate with batch scanning workflow
            _scannerState.value = MultiDrugScannerState.Processing("Batch işleme entegrasyonu...")
            val batchResults = integratWithBatchScanning(enhancedResults, source)
            
            val totalTime = System.currentTimeMillis() - startTime
            
            val result = MultiDrugScanResult(
                originalImage = bitmap,
                ocrResult = ocrResult,
                enhancedResults = enhancedResults,
                batchIntegration = batchResults,
                processingTime = totalTime,
                success = enhancedResults.isNotEmpty(),
                timestamp = System.currentTimeMillis(),
                source = source
            )
            
            // Store result for continuity
            addToRecentResults(result)
            
            _scannerState.value = MultiDrugScannerState.Completed(
                "${enhancedResults.size} ilaç başarıyla işlendi"
            )
            
            result
            
        } catch (e: Exception) {
            _scannerState.value = MultiDrugScannerState.Error(e.message ?: "Bilinmeyen hata")
            MultiDrugScanResult.error("İşleme hatası: ${e.message}")
        }
    }
    
    /**
     * Enhance OCR results with visual database matching
     */
    private suspend fun enhanceResultsWithVisualDatabase(
        ocrResult: MultiDrugOCRResult
    ): List<EnhancedDrugResult> {
        val enhancedResults = mutableListOf<EnhancedDrugResult>()
        
        for (regionResult in ocrResult.regions) {
            // For damaged or low-confidence results, try visual recovery
            val shouldTryVisualRecovery = regionResult.confidence < 0.7f || 
                                        regionResult.drugMatch == null ||
                                        regionResult.ocrText.length < 3
            
            val enhancedResult = if (shouldTryVisualRecovery) {
                // Attempt visual recovery
                val recoveryResult = visualDrugDatabaseRepository.recoverDamagedText(
                    damagedBitmap = regionResult.region.bitmap,
                    partialText = regionResult.ocrText,
                    context = "multi_drug_scan"
                )
                
                // Create enhanced result with recovery
                EnhancedDrugResult(
                    originalRegion = regionResult,
                    finalDrugName = if (recoveryResult.confidence > regionResult.confidence) {
                        recoveryResult.recoveredText
                    } else {
                        regionResult.drugMatch?.name ?: regionResult.ocrText
                    },
                    finalConfidence = maxOf(regionResult.confidence, recoveryResult.confidence),
                    enhancementMethod = if (recoveryResult.confidence > regionResult.confidence) {
                        EnhancementMethod.VISUAL_RECOVERY
                    } else {
                        EnhancementMethod.ORIGINAL_OCR
                    },
                    visualRecovery = recoveryResult,
                    drugDatabaseMatch = regionResult.drugMatch
                )
            } else {
                // Use original OCR result
                EnhancedDrugResult(
                    originalRegion = regionResult,
                    finalDrugName = regionResult.drugMatch?.name ?: regionResult.ocrText,
                    finalConfidence = regionResult.confidence,
                    enhancementMethod = EnhancementMethod.ORIGINAL_OCR,
                    visualRecovery = null,
                    drugDatabaseMatch = regionResult.drugMatch
                )
            }
            
            enhancedResults.add(enhancedResult)
        }
        
        return enhancedResults
    }
    
    /**
     * Integrate with existing batch scanning workflow
     */
    private suspend fun integratWithBatchScanning(
        enhancedResults: List<EnhancedDrugResult>,
        source: ImageSource
    ): BatchIntegrationResult {
        return try {
            val drugNames = enhancedResults.map { it.finalDrugName }.distinct()
            val averageConfidence = enhancedResults.map { it.finalConfidence }.average().toFloat()
            
            // Create batch scanning session for multi-drug results
            val sessionId = batchScanningRepository.createBatchSession(
                sessionName = "Multi-Drug Scan ${System.currentTimeMillis()}",
                expectedItems = drugNames.size
            )
            
            // Add each drug to batch session
            val batchItems = drugNames.map { drugName ->
                batchScanningRepository.addToBatch(
                    sessionId = sessionId,
                    drugName = drugName,
                    confidence = enhancedResults.find { it.finalDrugName == drugName }?.finalConfidence ?: 0f,
                    source = "multi_drug_scanner"
                )
            }
            
            BatchIntegrationResult(
                sessionId = sessionId,
                batchItems = batchItems,
                totalItems = drugNames.size,
                averageConfidence = averageConfidence,
                readyForWindowsAutomation = averageConfidence > 0.8f
            )
            
        } catch (e: Exception) {
            BatchIntegrationResult.error("Batch entegrasyon hatası: ${e.message}")
        }
    }
    
    /**
     * Start live video scanning
     */
    fun startLiveScanning() {
        _liveScanningActive.value = true
        _scannerState.value = MultiDrugScannerState.LiveScanning
    }
    
    /**
     * Stop live video scanning
     */
    fun stopLiveScanning() {
        _liveScanningActive.value = false
        _scannerState.value = MultiDrugScannerState.Idle
    }
    
    /**
     * Process batch of images
     */
    suspend fun processBatchImages(
        imageUris: List<Uri>,
        progressCallback: (Int, Int) -> Unit = { _, _ -> }
    ): BatchMultiDrugResult {
        val results = mutableListOf<MultiDrugScanResult>()
        val errors = mutableListOf<String>()
        
        imageUris.forEachIndexed { index, uri ->
            try {
                _scannerState.value = MultiDrugScannerState.Processing(
                    "İşleniyor ${index + 1}/${imageUris.size}"
                )
                
                val result = processGalleryImage(uri)
                results.add(result)
                
                progressCallback(index + 1, imageUris.size)
                
            } catch (e: Exception) {
                errors.add("Resim ${index + 1}: ${e.message}")
            }
        }
        
        return BatchMultiDrugResult(
            results = results,
            successCount = results.count { it.success },
            errorCount = errors.size,
            totalCount = imageUris.size,
            errors = errors,
            totalDrugsFound = results.flatMap { it.enhancedResults.map { r -> r.finalDrugName } }.distinct().size
        )
    }
    
    /**
     * Get combined state from all components
     */
    fun getCombinedState(): StateFlow<CombinedScannerState> {
        return combine(
            scannerState,
            multiRegionOCRRepository.processingState,
            visualDrugDatabaseRepository.processingState,
            multiDrugObjectDetector.detectionState
        ) { scanner, ocr, visual, detection ->
            CombinedScannerState(
                mainState = scanner,
                ocrState = ocr,
                visualState = visual,
                detectionState = detection
            )
        }
    }
    
    // Private helper methods
    
    private fun addToRecentResults(result: MultiDrugScanResult) {
        recentResults.add(result)
        // Keep only last 5 results for memory efficiency
        if (recentResults.size > 5) {
            recentResults.removeAt(0)
        }
    }
    
    private fun loadBitmapFromUri(uri: Uri): Bitmap {
        // Implement bitmap loading from URI
        // This would use ContentResolver to load the image
        return Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888) // Placeholder
    }
    
    private fun convertImageProxyToBitmap(imageProxy: ImageProxy): Bitmap {
        // Convert CameraX ImageProxy to Bitmap
        // This would implement the actual conversion
        return Bitmap.createBitmap(imageProxy.width, imageProxy.height, Bitmap.Config.ARGB_8888) // Placeholder
    }
}

// Data classes for multi-drug scanning results

data class MultiDrugScanResult(
    val originalImage: Bitmap,
    val ocrResult: MultiDrugOCRResult?,
    val enhancedResults: List<EnhancedDrugResult>,
    val batchIntegration: BatchIntegrationResult?,
    val processingTime: Long,
    val success: Boolean,
    val errorMessage: String? = null,
    val timestamp: Long,
    val source: ImageSource
) {
    val drugCount: Int get() = enhancedResults.size
    val averageConfidence: Float get() = enhancedResults.map { it.finalConfidence }.average().toFloat()
    val drugNames: List<String> get() = enhancedResults.map { it.finalDrugName }.distinct()
    
    companion object {
        fun error(message: String) = MultiDrugScanResult(
            originalImage = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888),
            ocrResult = null,
            enhancedResults = emptyList(),
            batchIntegration = null,
            processingTime = 0L,
            success = false,
            errorMessage = message,
            timestamp = System.currentTimeMillis(),
            source = ImageSource.USER_UPLOAD
        )
    }
}

data class EnhancedDrugResult(
    val originalRegion: RegionOCRResult,
    val finalDrugName: String,
    val finalConfidence: Float,
    val enhancementMethod: EnhancementMethod,
    val visualRecovery: DamagedTextRecoveryResult?,
    val drugDatabaseMatch: DrugMatch?
)

enum class EnhancementMethod {
    ORIGINAL_OCR,        // Used original OCR result
    VISUAL_RECOVERY,     // Enhanced with visual database
    HYBRID_APPROACH,     // Combined OCR + visual
    MANUAL_CORRECTION    // User-corrected result
}

data class BatchIntegrationResult(
    val sessionId: String,
    val batchItems: List<String>, // Batch item IDs
    val totalItems: Int,
    val averageConfidence: Float,
    val readyForWindowsAutomation: Boolean,
    val errorMessage: String? = null
) {
    companion object {
        fun error(message: String) = BatchIntegrationResult(
            sessionId = "",
            batchItems = emptyList(),
            totalItems = 0,
            averageConfidence = 0f,
            readyForWindowsAutomation = false,
            errorMessage = message
        )
    }
}

data class LiveScanResult(
    val detectedDrugs: List<String>,
    val regionCount: Int,
    val frameQuality: FrameQuality,
    val shouldCapture: Boolean,
    val confidence: Float,
    val processingTime: Long,
    val errorMessage: String? = null
) {
    companion object {
        fun error(message: String) = LiveScanResult(
            detectedDrugs = emptyList(),
            regionCount = 0,
            frameQuality = FrameQuality.ERROR,
            shouldCapture = false,
            confidence = 0f,
            processingTime = 0L,
            errorMessage = message
        )
    }
}

data class BatchMultiDrugResult(
    val results: List<MultiDrugScanResult>,
    val successCount: Int,
    val errorCount: Int,
    val totalCount: Int,
    val errors: List<String>,
    val totalDrugsFound: Int
)

data class CombinedScannerState(
    val mainState: MultiDrugScannerState,
    val ocrState: MultiRegionOCRState,
    val visualState: VisualDatabaseState,
    val detectionState: MultiDrugDetectionState
)

sealed class MultiDrugScannerState {
    object Idle : MultiDrugScannerState()
    object LiveScanning : MultiDrugScannerState()
    data class Processing(val message: String) : MultiDrugScannerState()
    data class Completed(val message: String) : MultiDrugScannerState()
    data class Error(val message: String) : MultiDrugScannerState()
}
