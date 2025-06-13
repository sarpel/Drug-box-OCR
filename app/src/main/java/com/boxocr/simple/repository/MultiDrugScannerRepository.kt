package com.boxocr.simple.repository

import android.graphics.Bitmap
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Multi-Drug Scanner Repository - Main Orchestrator for Phase 1 Enhancement
 * 
 * Coordinates all multi-drug detection components for comprehensive scanning workflow.
 */
@Singleton
class MultiDrugScannerRepository @Inject constructor(
    private val multiDrugObjectDetector: MultiDrugObjectDetector,
    private val multiRegionOCRRepository: MultiRegionOCRRepository,
    private val visualDrugDatabaseRepository: VisualDrugDatabaseRepository,
    private val batchScanningRepository: BatchScanningRepository,
    private val turkishDrugDatabaseRepository: TurkishDrugDatabaseRepository
) {
    
    companion object {
        private const val TAG = "MultiDrugScannerRepository"
    }
    
    data class ScanConfiguration(
        val enableObjectDetection: Boolean = true,
        val enableVisualMatching: Boolean = true,
        val enableTextRecovery: Boolean = true,
        val minConfidence: Float = 0.7f,
        val maxResults: Int = 10,
        val parallelProcessing: Boolean = true
    )
    
    data class DrugDetectionResult(
        val drugName: String,
        val brandName: String?,
        val confidence: Float,
        val detectionMethod: String,
        val boundingBox: android.graphics.Rect?,
        val ocrText: String,
        val visualMatch: Boolean,
        val recoveredText: Boolean
    )
    
    data class ComprehensiveScanResult(
        val detectedDrugs: List<DrugDetectionResult>,
        val processingTimeMs: Long,
        val totalRegionsProcessed: Int,
        val objectDetectionUsed: Boolean,
        val visualMatchingUsed: Boolean,
        val textRecoveryUsed: Boolean,
        val scanConfiguration: ScanConfiguration,
        val qualityScore: Float
    )
    
    /**
     * Perform comprehensive multi-drug scanning
     */
    suspend fun performComprehensiveScan(
        bitmap: Bitmap,
        configuration: ScanConfiguration = ScanConfiguration()
    ): ComprehensiveScanResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        
        try {
            val detectedDrugs = mutableListOf<DrugDetectionResult>()
            var totalRegionsProcessed = 0
            var objectDetectionUsed = false
            var visualMatchingUsed = false
            var textRecoveryUsed = false
            
            // Step 1: Object Detection for multiple drug boxes
            val detections = if (configuration.enableObjectDetection) {
                objectDetectionUsed = true
                val detectionResult = multiDrugObjectDetector.detectDrugBoxes(bitmap)
                totalRegionsProcessed = detectionResult.detections.size
                detectionResult.detections
            } else {
                // Treat entire image as single drug box
                totalRegionsProcessed = 1
                listOf(
                    MultiDrugObjectDetector.DrugBoxDetection(
                        boundingBox = android.graphics.Rect(0, 0, bitmap.width, bitmap.height),
                        confidence = 1.0f,
                        trackingId = null,
                        croppedImage = bitmap
                    )
                )
            }
            
            // Step 2: Multi-Region OCR Processing
            val ocrResults = multiRegionOCRRepository.processMultipleRegions(bitmap, detections)
            
            // Step 3: Process each region's results
            for (regionResult in ocrResults.regions) {
                if (regionResult.drugMatches.isNotEmpty()) {
                    // Add results from OCR matches
                    for (drugMatch in regionResult.drugMatches) {
                        detectedDrugs.add(
                            DrugDetectionResult(
                                drugName = drugMatch.drug.drugName,
                                brandName = drugMatch.drug.activeIngredient,
                                confidence = drugMatch.confidence.toFloat(),
                                detectionMethod = "ocr_${drugMatch.matchType}",
                                boundingBox = regionResult.boundingBox,
                                ocrText = regionResult.ocrText,
                                visualMatch = false,
                                recoveredText = false
                            )
                        )
                    }
                }
                
                // Step 4: Visual Similarity Matching (if enabled and no good OCR results)
                if (configuration.enableVisualMatching && regionResult.drugMatches.isEmpty()) {
                    visualMatchingUsed = true
                    val visualMatches = visualDrugDatabaseRepository.findSimilarDrugBoxes(
                        regionResult.boundingBox.let { 
                            // Crop bitmap to region - simplified implementation
                            bitmap
                        }
                    )
                    
                    for (visualMatch in visualMatches.matches) {
                        detectedDrugs.add(
                            DrugDetectionResult(
                                drugName = visualMatch.drugName,
                                brandName = null,
                                confidence = visualMatch.confidence,
                                detectionMethod = "visual_similarity",
                                boundingBox = regionResult.boundingBox,
                                ocrText = regionResult.ocrText,
                                visualMatch = true,
                                recoveredText = false
                            )
                        )
                    }
                }
            }
            
            // Step 5: Remove duplicates and rank results
            val uniqueResults = removeDuplicatesAndRank(detectedDrugs, configuration.minConfidence)
                .take(configuration.maxResults)
            
            // Step 6: Calculate quality score
            val qualityScore = calculateScanQuality(uniqueResults, ocrResults)
            
            val totalProcessingTime = System.currentTimeMillis() - startTime
            
            ComprehensiveScanResult(
                detectedDrugs = uniqueResults,
                processingTimeMs = totalProcessingTime,
                totalRegionsProcessed = totalRegionsProcessed,
                objectDetectionUsed = objectDetectionUsed,
                visualMatchingUsed = visualMatchingUsed,
                textRecoveryUsed = textRecoveryUsed,
                scanConfiguration = configuration,
                qualityScore = qualityScore
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during comprehensive scan", e)
            ComprehensiveScanResult(
                detectedDrugs = emptyList(),
                processingTimeMs = System.currentTimeMillis() - startTime,
                totalRegionsProcessed = 0,
                objectDetectionUsed = false,
                visualMatchingUsed = false,
                textRecoveryUsed = false,
                scanConfiguration = configuration,
                qualityScore = 0.0f
            )
        }
    }
    
    /**
     * Perform real-time scanning for live camera feed
     */
    fun performLiveScan(bitmap: Bitmap): Flow<List<DrugDetectionResult>> = flow {
        try {
            val quickConfig = ScanConfiguration(
                enableObjectDetection = true,
                enableVisualMatching = false, // Disable for performance
                enableTextRecovery = false,
                minConfidence = 0.8f, // Higher threshold for live scanning
                maxResults = 3, // Fewer results for speed
                parallelProcessing = false
            )
            
            val result = performComprehensiveScan(bitmap, quickConfig)
            emit(result.detectedDrugs)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during live scan", e)
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Remove duplicate detections and rank by confidence
     */
    private fun removeDuplicatesAndRank(
        results: List<DrugDetectionResult>,
        minConfidence: Float
    ): List<DrugDetectionResult> {
        return results
            .filter { it.confidence >= minConfidence }
            .groupBy { it.drugName.lowercase() }
            .mapValues { (_, duplicates) ->
                // Take the result with highest confidence for each drug
                duplicates.maxByOrNull { it.confidence }
            }
            .values
            .filterNotNull()
            .sortedByDescending { it.confidence }
    }
    
    /**
     * Calculate overall scan quality score
     */
    private fun calculateScanQuality(
        results: List<DrugDetectionResult>,
        ocrResults: MultiRegionOCRRepository.MultiRegionOCRResult
    ): Float {
        if (results.isEmpty()) return 0.0f
        
        val averageConfidence = results.map { it.confidence }.average().toFloat()
        val successRate = results.size.toFloat() / maxOf(1, ocrResults.regions.size)
        val qualityFactors = listOf(averageConfidence, successRate)
        
        return qualityFactors.average().toFloat()
    }
    
    /**
     * Get scan statistics
     */
    fun getScanStatistics(result: ComprehensiveScanResult): Map<String, Any> {
        return mapOf(
            "drugsDetected" to result.detectedDrugs.size,
            "averageConfidence" to (result.detectedDrugs.map { it.confidence }.average().takeIf { !it.isNaN() } ?: 0.0),
            "processingTime" to result.processingTimeMs,
            "regionsProcessed" to result.totalRegionsProcessed,
            "qualityScore" to result.qualityScore,
            "detectionMethods" to result.detectedDrugs.groupingBy { it.detectionMethod }.eachCount(),
            "configuraton" to mapOf(
                "objectDetection" to result.objectDetectionUsed,
                "visualMatching" to result.visualMatchingUsed,
                "textRecovery" to result.textRecoveryUsed
            )
        )
    }
}
