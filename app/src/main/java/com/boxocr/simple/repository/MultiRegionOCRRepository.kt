package com.boxocr.simple.repository

import android.content.Context
import android.graphics.Bitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Multi-Region OCR Repository - Phase 1 Foundation Enhancement
 * 
 * Processes multiple drug box regions simultaneously using existing Gemini OCR
 * infrastructure with enhanced Turkish medical context understanding.
 * 
 * Builds on existing OCRRepository for seamless integration.
 */
@Singleton
class MultiRegionOCRRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ocrRepository: OCRRepository,
    private val turkishDrugDatabase: TurkishDrugDatabaseRepository,
    private val multiDrugObjectDetector: MultiDrugObjectDetector
) {
    
    // Processing state management
    private val _processingState = MutableStateFlow<MultiRegionOCRState>(
        MultiRegionOCRState.Idle
    )
    val processingState: StateFlow<MultiRegionOCRState> = _processingState.asStateFlow()
    
    /**
     * Process multiple drug box regions in parallel using Gemini OCR
     * 
     * @param image Original image containing multiple drug boxes
     * @return MultiDrugOCRResult with all processed regions
     */
    suspend fun processMultiRegionImage(image: Bitmap): MultiDrugOCRResult {
        return coroutineScope {
            try {
                _processingState.value = MultiRegionOCRState.DetectingRegions
                
                // Step 1: Detect multiple drug box regions
                val detectedRegions = multiDrugObjectDetector.detectMultipleDrugBoxes(image)
                
                if (detectedRegions.isEmpty()) {
                    return@coroutineScope MultiDrugOCRResult(
                        originalImage = image,
                        regions = emptyList(),
                        processingTime = 0L,
                        success = false,
                        errorMessage = "İlaç kutusu tespit edilemedi"
                    )
                }
                
                _processingState.value = MultiRegionOCRState.ProcessingRegions(detectedRegions.size)
                val startTime = System.currentTimeMillis()
                
                // Step 2: Process each region in parallel using existing OCR
                val regionResults = detectedRegions.mapIndexed { index, region ->
                    async {
                        processIndividualRegion(region, index)
                    }
                }.awaitAll()
                
                val processingTime = System.currentTimeMillis() - startTime
                val successfulResults = regionResults.filter { it.success }
                
                _processingState.value = MultiRegionOCRState.Completed(successfulResults.size)
                
                MultiDrugOCRResult(
                    originalImage = image,
                    regions = regionResults,
                    processingTime = processingTime,
                    success = successfulResults.isNotEmpty(),
                    errorMessage = if (successfulResults.isEmpty()) "Hiçbir ilaç ismi okunamadı" else null
                )
                
            } catch (e: Exception) {
                _processingState.value = MultiRegionOCRState.Error(e.message ?: "Bilinmeyen hata")
                
                MultiDrugOCRResult(
                    originalImage = image,
                    regions = emptyList(),
                    processingTime = 0L,
                    success = false,
                    errorMessage = "İşleme hatası: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Process individual drug box region using enhanced Gemini OCR
     */
    private suspend fun processIndividualRegion(
        region: DrugBoxRegion,
        index: Int
    ): RegionOCRResult {
        return try {
            // Enhanced prompt for multi-drug context
            val enhancedPrompt = createMultiDrugOCRPrompt(region, index)
            
            // Use existing OCR with enhanced context
            val ocrResult = ocrRepository.processImageWithPrompt(
                bitmap = region.bitmap,
                customPrompt = enhancedPrompt
            )
            
            // Enhanced Turkish drug matching
            val matchResult = turkishDrugDatabase.findBestMatch(
                ocrResult.extractedText,
                useAdvancedMatching = true,
                confidence = region.confidence
            )
            
            RegionOCRResult(
                region = region.copy(
                    extractedText = ocrResult.extractedText,
                    drugName = matchResult?.name ?: "",
                    matchConfidence = matchResult?.confidence ?: 0f
                ),
                ocrText = ocrResult.extractedText,
                cleanedText = ocrResult.cleanedText,
                drugMatch = matchResult,
                confidence = calculateCombinedConfidence(region.confidence, matchResult?.confidence ?: 0f),
                processingTimeMs = ocrResult.processingTime,
                success = matchResult != null && matchResult.confidence > 0.3f,
                errorMessage = if (matchResult == null) "İlaç eşleştirilemedi" else null
            )
            
        } catch (e: Exception) {
            RegionOCRResult(
                region = region,
                ocrText = "",
                cleanedText = "",
                drugMatch = null,
                confidence = 0f,
                processingTimeMs = 0L,
                success = false,
                errorMessage = "Bölge işlenemedi: ${e.message}"
            )
        }
    }
    
    /**
     * Create enhanced OCR prompt for multi-drug context
     */
    private fun createMultiDrugOCRPrompt(region: DrugBoxRegion, index: Int): String {
        return """
        Bu görüntü çoklu ilaç kutu tarama işleminin bir parçasıdır.
        Bölge ${index + 1}: ${region.detectionMethod.name} yöntemi ile tespit edildi.
        Güven skoru: ${String.format("%.2f", region.confidence)}
        
        Lütfen bu ilaç kutusu bölgesindeki metni çok dikkatli bir şekilde okuyun:
        
        1. İlaç ismini net bir şekilde belirleyin
        2. Hasarlı veya kısmen görünen harfleri tahmin edin
        3. Türkçe ilaç isimleri için doğru yazımı kullanın
        4. Mümkünse dozaj bilgisini de ekleyin
        
        Çoklu ilaç tespit senaryosunda doğruluk kritiktir.
        Sadece ilaç ismini ve dozajını döndürün, başka açıklama yapmayın.
        
        Hasarlı metin kurtarma modu: AKTIF
        Türkçe medikal terminoloji: AKTIF
        Multi-region bağlam: Bölge ${index + 1}/${region.detectionMethod.name}
        """.trimIndent()
    }
    
    /**
     * Calculate combined confidence from detection and matching
     */
    private fun calculateCombinedConfidence(
        detectionConfidence: Float,
        matchConfidence: Float
    ): Float {
        // Weighted combination: 40% detection + 60% matching
        return (detectionConfidence * 0.4f) + (matchConfidence * 0.6f)
    }
    
    /**
     * Process batch of images for continuous scanning
     */
    suspend fun processBatchImages(images: List<Bitmap>): List<MultiDrugOCRResult> {
        return coroutineScope {
            images.mapIndexed { index, image ->
                async {
                    _processingState.value = MultiRegionOCRState.ProcessingBatch(index + 1, images.size)
                    processMultiRegionImage(image)
                }
            }.awaitAll()
        }
    }
    
    /**
     * Process live video frame for real-time detection
     */
    suspend fun processLiveFrame(
        frame: Bitmap,
        previousResults: List<MultiDrugOCRResult> = emptyList()
    ): LiveFrameResult {
        return try {
            // Quick detection for live processing
            val regions = multiDrugObjectDetector.detectMultipleDrugBoxes(frame)
            
            // Only process if we found new or different regions
            val shouldProcess = shouldProcessLiveFrame(regions, previousResults)
            
            if (shouldProcess) {
                val result = processMultiRegionImage(frame)
                LiveFrameResult(
                    result = result,
                    shouldCapture = result.success && result.regions.any { it.success },
                    frameQuality = assessFrameQuality(frame, regions),
                    detectedRegionCount = regions.size
                )
            } else {
                LiveFrameResult(
                    result = null,
                    shouldCapture = false,
                    frameQuality = FrameQuality.PROCESSING_SKIPPED,
                    detectedRegionCount = regions.size
                )
            }
            
        } catch (e: Exception) {
            LiveFrameResult(
                result = null,
                shouldCapture = false,
                frameQuality = FrameQuality.ERROR,
                detectedRegionCount = 0,
                errorMessage = e.message
            )
        }
    }
    
    /**
     * Determine if live frame should be processed based on previous results
     */
    private fun shouldProcessLiveFrame(
        currentRegions: List<DrugBoxRegion>,
        previousResults: List<MultiDrugOCRResult>
    ): Boolean {
        // Process if no previous results
        if (previousResults.isEmpty()) return true
        
        // Process if significant change in region count
        val previousRegionCount = previousResults.lastOrNull()?.regions?.size ?: 0
        if (kotlin.math.abs(currentRegions.size - previousRegionCount) > 1) return true
        
        // Process every 3rd frame to avoid overloading
        return System.currentTimeMillis() % 3 == 0L
    }
    
    /**
     * Assess frame quality for live processing
     */
    private fun assessFrameQuality(frame: Bitmap, regions: List<DrugBoxRegion>): FrameQuality {
        return when {
            regions.isEmpty() -> FrameQuality.NO_DETECTION
            regions.size > 5 -> FrameQuality.TOO_MANY_OBJECTS
            regions.any { it.confidence > 0.8f } -> FrameQuality.EXCELLENT
            regions.any { it.confidence > 0.6f } -> FrameQuality.GOOD
            regions.any { it.confidence > 0.4f } -> FrameQuality.ACCEPTABLE
            else -> FrameQuality.POOR
        }
    }
}

/**
 * Complete result for multi-drug OCR processing
 */
data class MultiDrugOCRResult(
    val originalImage: Bitmap,
    val regions: List<RegionOCRResult>,
    val processingTime: Long,
    val success: Boolean,
    val errorMessage: String? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    val successfulRegions: List<RegionOCRResult>
        get() = regions.filter { it.success }
    
    val drugNames: List<String>
        get() = successfulRegions.mapNotNull { it.drugMatch?.name }.distinct()
    
    val averageConfidence: Float
        get() = if (successfulRegions.isNotEmpty()) {
            successfulRegions.map { it.confidence }.average().toFloat()
        } else 0f
}

/**
 * Result for individual region OCR processing
 */
data class RegionOCRResult(
    val region: DrugBoxRegion,
    val ocrText: String,
    val cleanedText: String,
    val drugMatch: DrugMatch?, // From existing TurkishDrugDatabaseRepository
    val confidence: Float,
    val processingTimeMs: Long,
    val success: Boolean,
    val errorMessage: String? = null
)

/**
 * Result for live video frame processing
 */
data class LiveFrameResult(
    val result: MultiDrugOCRResult?,
    val shouldCapture: Boolean,
    val frameQuality: FrameQuality,
    val detectedRegionCount: Int,
    val errorMessage: String? = null
)

/**
 * Frame quality assessment for live processing
 */
enum class FrameQuality {
    EXCELLENT,
    GOOD, 
    ACCEPTABLE,
    POOR,
    NO_DETECTION,
    TOO_MANY_OBJECTS,
    PROCESSING_SKIPPED,
    ERROR
}

/**
 * State of multi-region OCR processing
 */
sealed class MultiRegionOCRState {
    object Idle : MultiRegionOCRState()
    object DetectingRegions : MultiRegionOCRState()
    data class ProcessingRegions(val regionCount: Int) : MultiRegionOCRState()
    data class ProcessingBatch(val current: Int, val total: Int) : MultiRegionOCRState()
    data class Completed(val successCount: Int) : MultiRegionOCRState()
    data class Error(val message: String) : MultiRegionOCRState()
}
