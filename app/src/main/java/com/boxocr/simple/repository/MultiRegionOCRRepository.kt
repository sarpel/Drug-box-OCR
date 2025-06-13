package com.boxocr.simple.repository

import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Multi-Region OCR Repository - Phase 1 Enhancement
 * 
 * Processes multiple detected drug box regions in parallel for enhanced performance.
 */
@Singleton
class MultiRegionOCRRepository @Inject constructor(
    private val ocrRepository: OCRRepository,
    private val turkishDrugDatabase: TurkishDrugDatabaseRepository,
    private val multiDrugObjectDetector: MultiDrugObjectDetector
) {
    
    companion object {
        private const val TAG = "MultiRegionOCRRepository"
        private const val MAX_PARALLEL_OCR = 4
    }
    
    data class RegionOCRResult(
        val regionIndex: Int,
        val boundingBox: Rect,
        val ocrText: String,
        val confidence: Float,
        val drugMatches: List<TurkishDrugDatabaseRepository.DrugSearchResult>,
        val processingTimeMs: Long
    )
    
    data class MultiRegionOCRResult(
        val regions: List<RegionOCRResult>,
        val totalProcessingTimeMs: Long,
        val parallelProcessingUsed: Boolean,
        val totalDrugsDetected: Int
    )
    
    /**
     * Process multiple drug box regions with parallel OCR
     */
    suspend fun processMultipleRegions(
        originalBitmap: Bitmap,
        detections: List<MultiDrugObjectDetector.DrugBoxDetection>
    ): MultiRegionOCRResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        
        try {
            val parallelProcessing = detections.size <= MAX_PARALLEL_OCR
            
            val results = if (parallelProcessing) {
                // Process regions in parallel
                detections.mapIndexed { index, detection ->
                    async {
                        processRegion(index, detection)
                    }
                }.awaitAll()
            } else {
                // Process regions sequentially to avoid resource exhaustion
                detections.mapIndexed { index, detection ->
                    processRegion(index, detection)
                }
            }
            
            val totalDrugsDetected = results.sumOf { it.drugMatches.size }
            val totalProcessingTime = System.currentTimeMillis() - startTime
            
            MultiRegionOCRResult(
                regions = results,
                totalProcessingTimeMs = totalProcessingTime,
                parallelProcessingUsed = parallelProcessing,
                totalDrugsDetected = totalDrugsDetected
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Error processing multiple regions", e)
            MultiRegionOCRResult(
                regions = emptyList(),
                totalProcessingTimeMs = System.currentTimeMillis() - startTime,
                parallelProcessingUsed = false,
                totalDrugsDetected = 0
            )
        }
    }
    
    /**
     * Process individual drug box region
     */
    private suspend fun processRegion(
        regionIndex: Int,
        detection: MultiDrugObjectDetector.DrugBoxDetection
    ): RegionOCRResult {
        val startTime = System.currentTimeMillis()
        
        return try {
            // Perform OCR on cropped image
            val ocrResult = ocrRepository.performEnhancedOCR(detection.croppedImage)
            
            // Search for drug matches
            val drugMatches = if (ocrResult.text.isNotBlank()) {
                turkishDrugDatabase.searchDrugs(
                    query = ocrResult.text,
                    algorithm = TurkishDrugDatabaseRepository.MatchingAlgorithm.COMPREHENSIVE,
                    minConfidence = 0.6,
                    maxResults = 5
                )
            } else {
                emptyList()
            }
            
            val processingTime = System.currentTimeMillis() - startTime
            
            RegionOCRResult(
                regionIndex = regionIndex,
                boundingBox = detection.boundingBox,
                ocrText = ocrResult.text,
                confidence = ocrResult.confidence,
                drugMatches = drugMatches,
                processingTimeMs = processingTime
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Error processing region $regionIndex", e)
            RegionOCRResult(
                regionIndex = regionIndex,
                boundingBox = detection.boundingBox,
                ocrText = "",
                confidence = 0.0f,
                drugMatches = emptyList(),
                processingTimeMs = System.currentTimeMillis() - startTime
            )
        }
    }
    
    /**
     * Filter and rank results by confidence
     */
    suspend fun filterAndRankResults(
        results: MultiRegionOCRResult,
        minConfidence: Float = 0.7f
    ): List<RegionOCRResult> {
        return results.regions
            .filter { it.confidence >= minConfidence }
            .filter { it.drugMatches.isNotEmpty() }
            .sortedByDescending { region ->
                region.drugMatches.maxOfOrNull { it.confidence } ?: 0.0
            }
    }
    
    /**
     * Get processing statistics
     */
    fun getProcessingStats(result: MultiRegionOCRResult): Map<String, Any> {
        return mapOf(
            "totalRegions" to result.regions.size,
            "successfulRegions" to result.regions.count { it.drugMatches.isNotEmpty() },
            "averageConfidence" to (result.regions.map { it.confidence }.average().takeIf { !it.isNaN() } ?: 0.0),
            "totalProcessingTime" to result.totalProcessingTimeMs,
            "averageTimePerRegion" to (result.totalProcessingTimeMs / maxOf(1, result.regions.size)),
            "parallelProcessingUsed" to result.parallelProcessingUsed,
            "totalDrugsDetected" to result.totalDrugsDetected
        )
    }
}
