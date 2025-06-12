package com.boxocr.simple.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import com.boxocr.simple.database.*
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

/**
 * Enhanced Drug Box Image Database Manager - Phase 2 Week 4 Enhancement
 * 
 * Advanced system for processing user-provided drug box images with:
 * - Intelligent batch processing
 * - Visual quality assessment
 * - Automated categorization
 * - Smart deduplication
 * - Multi-condition image management
 */
@Singleton
class DrugBoxImageDatabaseManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: BoxOCRDatabase,
    private val visualRepository: VisualDrugDatabaseRepository
) {
    
    private val drugBoxImageDao = database.drugBoxImageDao()
    
    // Processing state management
    private val _processingState = MutableStateFlow<DatabaseProcessingState>(
        DatabaseProcessingState.Idle
    )
    val processingState: StateFlow<DatabaseProcessingState> = _processingState.asStateFlow()
    
    // Image quality thresholds
    private val MIN_IMAGE_WIDTH = 200
    private val MIN_IMAGE_HEIGHT = 200
    private val MAX_IMAGE_SIZE = 5 * 1024 * 1024 // 5MB
    private val MIN_QUALITY_SCORE = 0.3f
    
    /**
     * Process user-provided drug box images with intelligent categorization
     */
    suspend fun processUserDrugBoxImages(
        imageUris: List<Uri>,
        metadata: List<DrugBoxImageMetadata>,
        progressCallback: (Int, Int, String) -> Unit = { _, _, _ -> }
    ): BatchProcessingResult = withContext(Dispatchers.IO) {
        
        _processingState.value = DatabaseProcessingState.Processing("Görüntüler analiz ediliyor...")
        
        val results = mutableListOf<ProcessingResult>()
        val errors = mutableListOf<ProcessingError>()
        
        imageUris.forEachIndexed { index, uri ->
            try {
                val imageMetadata = metadata.getOrNull(index) ?: DrugBoxImageMetadata()
                
                progressCallback(index + 1, imageUris.size, "İşleniyor: ${imageMetadata.drugName}")
                
                val bitmap = loadBitmapFromUri(uri)
                if (bitmap != null) {
                    val result = processIndividualDrugBoxImage(bitmap, imageMetadata, uri)
                    results.add(result)
                } else {
                    errors.add(ProcessingError(index, "Görüntü yüklenemedi", uri.toString()))
                }
                
            } catch (e: Exception) {
                errors.add(ProcessingError(index, e.message ?: "Bilinmeyen hata", imageUris[index].toString()))
            }
        }
        
        _processingState.value = DatabaseProcessingState.Completed(
            "${results.size} görüntü başarıyla işlendi"
        )
        
        BatchProcessingResult(
            successCount = results.size,
            errorCount = errors.size,
            totalCount = imageUris.size,
            results = results,
            errors = errors
        )
    }
    
    /**
     * Process individual drug box image with quality assessment
     */
    private suspend fun processIndividualDrugBoxImage(
        bitmap: Bitmap,
        metadata: DrugBoxImageMetadata,
        sourceUri: Uri
    ): ProcessingResult {
        
        // 1. Quality assessment
        val qualityScore = assessImageQuality(bitmap)
        
        // 2. Auto-detect drug box condition if not provided
        val detectedCondition = if (metadata.condition == DrugBoxCondition.UNKNOWN) {
            detectDrugBoxCondition(bitmap)
        } else {
            metadata.condition
        }
        
        // 3. Auto-detect angle if not provided
        val detectedAngle = if (metadata.angle == DrugBoxAngle.UNKNOWN) {
            detectDrugBoxAngle(bitmap)
        } else {
            metadata.angle
        }
        
        // 4. Lighting assessment
        val lightingQuality = assessLightingQuality(bitmap)
        
        // 5. Check for duplicates
        val duplicateCheck = checkForDuplicates(bitmap, metadata.drugName)
        
        if (duplicateCheck.isDuplicate && !metadata.allowDuplicates) {
            return ProcessingResult(
                success = false,
                imageId = null,
                drugName = metadata.drugName,
                qualityScore = qualityScore,
                message = "Benzer görüntü zaten mevcut",
                duplicateInfo = duplicateCheck
            )
        }
        
        // 6. Add to database if quality is acceptable
        if (qualityScore >= MIN_QUALITY_SCORE) {
            val imageId = visualRepository.addDrugBoxImage(
                bitmap = bitmap,
                drugName = metadata.drugName,
                brandName = metadata.brandName,
                condition = detectedCondition,
                angle = detectedAngle,
                lighting = lightingQuality,
                source = ImageSource.USER_UPLOAD
            )
            
            return ProcessingResult(
                success = true,
                imageId = imageId,
                drugName = metadata.drugName,
                qualityScore = qualityScore,
                message = "Başarıyla eklendi",
                detectedCondition = detectedCondition,
                detectedAngle = detectedAngle,
                lightingQuality = lightingQuality
            )
        } else {
            return ProcessingResult(
                success = false,
                imageId = null,
                drugName = metadata.drugName,
                qualityScore = qualityScore,
                message = "Görüntü kalitesi yetersiz (${String.format("%.2f", qualityScore)})",
                suggestions = generateQualityImprovementSuggestions(bitmap, qualityScore)
            )
        }
    }
    
    /**
     * Assess image quality for drug box recognition
     */
    private fun assessImageQuality(bitmap: Bitmap): Float {
        var qualityScore = 0f
        
        // 1. Resolution check (20% weight)
        val resolutionScore = assessResolution(bitmap)
        qualityScore += resolutionScore * 0.2f
        
        // 2. Blur detection (25% weight)
        val sharpnessScore = assessSharpness(bitmap)
        qualityScore += sharpnessScore * 0.25f
        
        // 3. Contrast assessment (20% weight)
        val contrastScore = assessContrast(bitmap)
        qualityScore += contrastScore * 0.2f
        
        // 4. Text region detection (25% weight)
        val textScore = assessTextRegions(bitmap)
        qualityScore += textScore * 0.25f
        
        // 5. Lighting quality (10% weight)
        val lightingScore = assessLighting(bitmap)
        qualityScore += lightingScore * 0.1f
        
        return qualityScore.coerceIn(0f, 1f)
    }
    
    /**
     * Detect drug box condition from image analysis
     */
    private fun detectDrugBoxCondition(bitmap: Bitmap): DrugBoxCondition {
        val features = AdvancedVisualFeatureExtractor.extractComprehensiveFeatures(bitmap)
        
        // Analyze edge quality for damage detection
        val edgeFeature = features[VisualFeatureType.EDGE_FEATURES]
        val edgeQuality = edgeFeature?.confidence ?: 0f
        
        // Analyze color distribution for fading/wear
        val colorFeature = features[VisualFeatureType.COLOR_HISTOGRAM]
        val colorVariance = colorFeature?.confidence ?: 0f
        
        // Analyze text layout for completeness
        val textFeature = features[VisualFeatureType.TEXT_LAYOUT]
        val textCompleteness = textFeature?.confidence ?: 0f
        
        return when {
            edgeQuality > 0.8f && colorVariance > 0.7f && textCompleteness > 0.8f -> DrugBoxCondition.PERFECT
            edgeQuality > 0.6f && colorVariance > 0.5f && textCompleteness > 0.6f -> DrugBoxCondition.GOOD
            edgeQuality > 0.4f && colorVariance > 0.3f && textCompleteness > 0.4f -> DrugBoxCondition.WORN
            edgeQuality > 0.2f && textCompleteness > 0.2f -> DrugBoxCondition.DAMAGED
            else -> DrugBoxCondition.SEVERELY_DAMAGED
        }
    }
    
    /**
     * Detect drug box viewing angle
     */
    private fun detectDrugBoxAngle(bitmap: Bitmap): DrugBoxAngle {
        val features = AdvancedVisualFeatureExtractor.extractComprehensiveFeatures(bitmap)
        
        // Analyze text layout to determine angle
        val textFeature = features[VisualFeatureType.TEXT_LAYOUT]
        if (textFeature != null) {
            val layoutVector = textFeature.vector.split(",").map { it.toFloat() }
            
            if (layoutVector.size >= 8) {
                val topTextRatio = layoutVector[0]
                val centerTextRatio = layoutVector[1]
                val bottomTextRatio = layoutVector[2]
                val leftTextRatio = layoutVector[3]
                val rightTextRatio = layoutVector[4]
                
                return when {
                    centerTextRatio > 0.5f -> DrugBoxAngle.FRONT
                    topTextRatio > 0.4f -> DrugBoxAngle.TOP
                    bottomTextRatio > 0.4f -> DrugBoxAngle.BOTTOM
                    leftTextRatio > 0.4f -> DrugBoxAngle.LEFT_SIDE
                    rightTextRatio > 0.4f -> DrugBoxAngle.RIGHT_SIDE
                    else -> DrugBoxAngle.ANGLED
                }
            }
        }
        
        return DrugBoxAngle.FRONT // Default assumption
    }
    
    /**
     * Assess lighting quality
     */
    private fun assessLightingQuality(bitmap: Bitmap): DrugBoxLighting {
        val avgBrightness = calculateAverageBrightness(bitmap)
        val contrast = calculateContrast(bitmap)
        
        return when {
            avgBrightness > 0.8f -> DrugBoxLighting.OVEREXPOSED
            avgBrightness < 0.2f -> DrugBoxLighting.UNDEREXPOSED
            contrast > 0.7f && avgBrightness in 0.4f..0.7f -> DrugBoxLighting.NORMAL
            contrast < 0.3f -> DrugBoxLighting.LOW_CONTRAST
            else -> DrugBoxLighting.NORMAL
        }
    }
    
    /**
     * Check for duplicate images
     */
    private suspend fun checkForDuplicates(
        bitmap: Bitmap, 
        drugName: String
    ): DuplicateCheckResult {
        val similarityResult = visualRepository.findVisualSimilarity(
            queryBitmap = bitmap,
            minSimilarityScore = 0.8f, // High threshold for duplicates
            maxResults = 5
        )
        
        val duplicates = similarityResult.similarImages.filter { 
            it.imageEntity.drugName.equals(drugName, ignoreCase = true) &&
            it.similarityScore > 0.9f // Very high similarity threshold
        }
        
        return DuplicateCheckResult(
            isDuplicate = duplicates.isNotEmpty(),
            duplicateCount = duplicates.size,
            bestMatch = duplicates.maxByOrNull { it.similarityScore },
            allMatches = duplicates
        )
    }
    
    /**
     * Generate suggestions for quality improvement
     */
    private fun generateQualityImprovementSuggestions(
        bitmap: Bitmap, 
        qualityScore: Float
    ): List<String> {
        val suggestions = mutableListOf<String>()
        
        // Check specific quality issues
        if (bitmap.width < MIN_IMAGE_WIDTH || bitmap.height < MIN_IMAGE_HEIGHT) {
            suggestions.add("Daha yüksek çözünürlükte fotoğraf çekin")
        }
        
        val sharpness = assessSharpness(bitmap)
        if (sharpness < 0.5f) {
            suggestions.add("Daha net fotoğraf için kamerayı sabitleyin")
        }
        
        val contrast = assessContrast(bitmap)
        if (contrast < 0.4f) {
            suggestions.add("Daha iyi aydınlatma kullanın")
        }
        
        val textScore = assessTextRegions(bitmap)
        if (textScore < 0.4f) {
            suggestions.add("İlaç kutusundaki yazıları daha net gösterin")
        }
        
        val lighting = assessLighting(bitmap)
        if (lighting < 0.4f) {
            suggestions.add("Daha dengeli ışık kullanın")
        }
        
        return suggestions
    }
    
    // Image quality assessment helper methods
    
    private fun assessResolution(bitmap: Bitmap): Float {
        val totalPixels = bitmap.width * bitmap.height
        val minPixels = MIN_IMAGE_WIDTH * MIN_IMAGE_HEIGHT
        return minOf(totalPixels.toFloat() / minPixels, 1f)
    }
    
    private fun assessSharpness(bitmap: Bitmap): Float {
        // Simplified edge-based sharpness assessment
        var edgeStrength = 0f
        val samples = 100
        
        for (i in 0 until samples) {
            val x = (Math.random() * (bitmap.width - 2)).toInt() + 1
            val y = (Math.random() * (bitmap.height - 2)).toInt() + 1
            
            val center = getGrayValue(bitmap, x, y)
            val right = getGrayValue(bitmap, x + 1, y)
            val bottom = getGrayValue(bitmap, x, y + 1)
            
            val gradientX = abs(right - center)
            val gradientY = abs(bottom - center)
            edgeStrength += sqrt((gradientX * gradientX + gradientY * gradientY).toDouble()).toFloat()
        }
        
        return minOf(edgeStrength / samples / 100f, 1f)
    }
    
    private fun assessContrast(bitmap: Bitmap): Float {
        val histogram = IntArray(256)
        val totalPixels = bitmap.width * bitmap.height
        
        // Sample pixels for histogram
        for (x in 0 until bitmap.width step 4) {
            for (y in 0 until bitmap.height step 4) {
                val gray = getGrayValue(bitmap, x, y)
                histogram[gray]++
            }
        }
        
        // Calculate histogram spread
        var firstNonZero = -1
        var lastNonZero = -1
        
        for (i in histogram.indices) {
            if (histogram[i] > 0) {
                if (firstNonZero == -1) firstNonZero = i
                lastNonZero = i
            }
        }
        
        return if (firstNonZero != -1 && lastNonZero != -1) {
            (lastNonZero - firstNonZero).toFloat() / 255f
        } else {
            0f
        }
    }
    
    private fun assessTextRegions(bitmap: Bitmap): Float {
        val features = AdvancedVisualFeatureExtractor.extractComprehensiveFeatures(bitmap)
        val textFeature = features[VisualFeatureType.TEXT_LAYOUT]
        return textFeature?.confidence ?: 0f
    }
    
    private fun assessLighting(bitmap: Bitmap): Float {
        val avgBrightness = calculateAverageBrightness(bitmap)
        val optimalBrightness = 0.5f
        val deviation = abs(avgBrightness - optimalBrightness)
        return 1f - deviation * 2f // Convert deviation to quality score
    }
    
    private fun getGrayValue(bitmap: Bitmap, x: Int, y: Int): Int {
        val pixel = bitmap.getPixel(x, y)
        val r = (pixel shr 16) and 0xFF
        val g = (pixel shr 8) and 0xFF
        val b = pixel and 0xFF
        return (r * 0.299 + g * 0.587 + b * 0.114).toInt()
    }
    
    private fun calculateAverageBrightness(bitmap: Bitmap): Float {
        var totalBrightness = 0L
        val totalPixels = bitmap.width * bitmap.height
        
        for (x in 0 until bitmap.width step 2) {
            for (y in 0 until bitmap.height step 2) {
                totalBrightness += getGrayValue(bitmap, x, y)
            }
        }
        
        return totalBrightness.toFloat() / (totalPixels / 4) / 255f
    }
    
    private fun calculateContrast(bitmap: Bitmap): Float {
        val avgBrightness = calculateAverageBrightness(bitmap) * 255f
        var variance = 0.0
        var sampleCount = 0
        
        for (x in 0 until bitmap.width step 4) {
            for (y in 0 until bitmap.height step 4) {
                val brightness = getGrayValue(bitmap, x, y)
                val diff = brightness - avgBrightness
                variance += diff * diff
                sampleCount++
            }
        }
        
        val standardDeviation = sqrt(variance / sampleCount)
        return minOf(standardDeviation.toFloat() / 128f, 1f)
    }
    
    private fun loadBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Smart database optimization
     */
    suspend fun optimizeDatabase(): DatabaseOptimizationResult = withContext(Dispatchers.IO) {
        _processingState.value = DatabaseProcessingState.Processing("Veritabanı optimize ediliyor...")
        
        // 1. Remove exact duplicates
        val duplicatesRemoved = removeDuplicateImages()
        
        // 2. Consolidate similar images
        val similarConsolidated = consolidateSimilarImages()
        
        // 3. Update feature vectors with latest algorithms
        val featuresUpdated = updateFeatureVectors()
        
        _processingState.value = DatabaseProcessingState.Completed("Optimizasyon tamamlandı")
        
        DatabaseOptimizationResult(
            duplicatesRemoved = duplicatesRemoved,
            similarConsolidated = similarConsolidated,
            featuresUpdated = featuresUpdated,
            totalSpaceSaved = calculateSpaceSaved()
        )
    }
    
    private suspend fun removeDuplicateImages(): Int {
        // Implementation for removing exact duplicates
        return 0 // Placeholder
    }
    
    private suspend fun consolidateSimilarImages(): Int {
        // Implementation for consolidating similar images
        return 0 // Placeholder
    }
    
    private suspend fun updateFeatureVectors(): Int {
        // Implementation for updating feature vectors
        return 0 // Placeholder
    }
    
    private fun calculateSpaceSaved(): Long {
        // Implementation for calculating saved space
        return 0L // Placeholder
    }
}

// Data classes for drug box image processing

data class DrugBoxImageMetadata(
    val drugName: String = "",
    val brandName: String = "",
    val condition: DrugBoxCondition = DrugBoxCondition.UNKNOWN,
    val angle: DrugBoxAngle = DrugBoxAngle.UNKNOWN,
    val lighting: DrugBoxLighting = DrugBoxLighting.UNKNOWN,
    val allowDuplicates: Boolean = false,
    val category: String = "",
    val notes: String = ""
)

data class ProcessingResult(
    val success: Boolean,
    val imageId: Long?,
    val drugName: String,
    val qualityScore: Float,
    val message: String,
    val detectedCondition: DrugBoxCondition? = null,
    val detectedAngle: DrugBoxAngle? = null,
    val lightingQuality: DrugBoxLighting? = null,
    val duplicateInfo: DuplicateCheckResult? = null,
    val suggestions: List<String> = emptyList()
)

data class BatchProcessingResult(
    val successCount: Int,
    val errorCount: Int,
    val totalCount: Int,
    val results: List<ProcessingResult>,
    val errors: List<ProcessingError>
)

data class ProcessingError(
    val index: Int,
    val message: String,
    val source: String
)

data class DuplicateCheckResult(
    val isDuplicate: Boolean,
    val duplicateCount: Int,
    val bestMatch: SimilarImage?,
    val allMatches: List<SimilarImage>
)

data class DatabaseOptimizationResult(
    val duplicatesRemoved: Int,
    val similarConsolidated: Int,
    val featuresUpdated: Int,
    val totalSpaceSaved: Long
)

sealed class DatabaseProcessingState {
    object Idle : DatabaseProcessingState()
    data class Processing(val message: String) : DatabaseProcessingState()
    data class Completed(val message: String) : DatabaseProcessingState()
    data class Error(val message: String) : DatabaseProcessingState()
}
