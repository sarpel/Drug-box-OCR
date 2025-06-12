package com.boxocr.simple.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import kotlin.math.sqrt

/**
 * Visual Drug Database Repository - Phase 1 Foundation Enhancement
 * 
 * Manages visual drug box database for similarity matching and damaged text recovery.
 * Implements advanced image processing for multi-drug detection scenarios.
 */
@Singleton
class VisualDrugDatabaseRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: BoxOCRDatabase
) {
    
    private val drugBoxImageDao = database.drugBoxImageDao()
    private val drugBoxFeatureDao = database.drugBoxFeatureDao()
    private val visualSimilarityMatchDao = database.visualSimilarityMatchDao()
    private val visualCorrectionDao = database.visualCorrectionDao()
    
    // Processing state management
    private val _processingState = MutableStateFlow<VisualDatabaseState>(
        VisualDatabaseState.Idle
    )
    val processingState: StateFlow<VisualDatabaseState> = _processingState.asStateFlow()
    
    // Image storage directory
    private val imageStorageDir = File(context.filesDir, "drug_box_images").apply {
        mkdirs()
    }
    
    /**
     * Add drug box image to visual database
     */
    suspend fun addDrugBoxImage(
        bitmap: Bitmap,
        drugName: String,
        brandName: String = "",
        condition: DrugBoxCondition = DrugBoxCondition.PERFECT,
        angle: DrugBoxAngle = DrugBoxAngle.FRONT,
        lighting: DrugBoxLighting = DrugBoxLighting.NORMAL,
        source: ImageSource = ImageSource.USER_UPLOAD
    ): Long = withContext(Dispatchers.IO) {
        try {
            _processingState.value = VisualDatabaseState.Processing("İmaj kaydediliyor...")
            
            // Generate image hash for duplicate detection
            val imageHash = generateImageHash(bitmap)
            
            // Check for duplicates
            val existingImage = drugBoxImageDao.getImageByHash(imageHash)
            if (existingImage != null) {
                _processingState.value = VisualDatabaseState.Error("Bu görüntü zaten mevcut")
                return@withContext existingImage.id
            }
            
            // Save image to local storage
            val imagePath = saveImageToStorage(bitmap, drugName, imageHash)
            
            // Create database entity
            val imageEntity = DrugBoxImageEntity(
                drugName = drugName,
                brandName = brandName,
                imagePath = imagePath,
                imageHash = imageHash,
                condition = condition,
                angle = angle,
                lighting = lighting,
                width = bitmap.width,
                height = bitmap.height,
                fileSize = getImageFileSize(imagePath),
                source = source
            )
            
            // Insert to database
            val imageId = drugBoxImageDao.insertImage(imageEntity)
            
            // Extract and save visual features
            extractAndSaveVisualFeatures(imageId, bitmap)
            
            _processingState.value = VisualDatabaseState.Completed("İmaj başarıyla eklendi")
            imageId
            
        } catch (e: Exception) {
            _processingState.value = VisualDatabaseState.Error("İmaj eklenirken hata: ${e.message}")
            throw e
        }
    }
    
    /**
     * Find visually similar drug box images
     */
    suspend fun findVisualSimilarity(
        queryBitmap: Bitmap,
        minSimilarityScore: Float = 0.5f,
        maxResults: Int = 10
    ): VisualSimilarityResult = withContext(Dispatchers.IO) {
        try {
            _processingState.value = VisualDatabaseState.Processing("Görsel benzerlik analizi...")
            val startTime = System.currentTimeMillis()
            
            // Extract features from query image
            val queryFeatures = extractVisualFeatures(queryBitmap)
            
            // Get all images from database
            val allImages = drugBoxImageDao.getAllImages().let { flow ->
                // Convert Flow to List - this is a simplified approach
                // In production, you might want to use a different approach
                val result = mutableListOf<DrugBoxImageEntity>()
                // For now, return empty list - this would need proper Flow handling
                result
            }
            
            // Calculate similarity scores
            val similarImages = mutableListOf<SimilarImage>()
            
            for (image in allImages) {
                val imageFeatures = drugBoxFeatureDao.getFeaturesByImageId(image.id)
                val similarityScore = calculateSimilarityScore(queryFeatures, imageFeatures)
                
                if (similarityScore >= minSimilarityScore) {
                    similarImages.add(
                        SimilarImage(
                            imageEntity = image,
                            similarityScore = similarityScore,
                            matchingFeatures = identifyMatchingFeatures(queryFeatures, imageFeatures)
                        )
                    )
                }
            }
            
            // Sort by similarity score and limit results
            val topSimilar = similarImages
                .sortedByDescending { it.similarityScore }
                .take(maxResults)
            
            val processingTime = System.currentTimeMillis() - startTime
            
            _processingState.value = VisualDatabaseState.Completed(
                "${topSimilar.size} benzer görüntü bulundu"
            )
            
            VisualSimilarityResult(
                queryImageId = 0, // Temporary image, no ID
                similarImages = topSimilar,
                processingTime = processingTime,
                algorithm = "HYBRID_SIMILARITY"
            )
            
        } catch (e: Exception) {
            _processingState.value = VisualDatabaseState.Error("Benzerlik analizi hatası: ${e.message}")
            throw e
        }
    }
    
    /**
     * Recover damaged text using visual similarity
     */
    suspend fun recoverDamagedText(
        damagedBitmap: Bitmap,
        partialText: String,
        context: String = ""
    ): DamagedTextRecoveryResult = withContext(Dispatchers.IO) {
        try {
            _processingState.value = VisualDatabaseState.Processing("Hasarlı metin kurtarılıyor...")
            
            // Find visually similar images
            val similarityResult = findVisualSimilarity(damagedBitmap, minSimilarityScore = 0.6f)
            
            if (similarityResult.similarImages.isEmpty()) {
                return@withContext DamagedTextRecoveryResult(
                    recoveredText = partialText,
                    confidence = 0.1f,
                    method = RecoveryMethod.NO_VISUAL_MATCH,
                    suggestions = emptyList()
                )
            }
            
            // Analyze similar images for text recovery
            val textSuggestions = mutableListOf<TextSuggestion>()
            
            for (similarImage in similarityResult.similarImages.take(5)) {
                // Load the similar image
                val similarBitmap = loadImageFromStorage(similarImage.imageEntity.imagePath)
                
                if (similarBitmap != null) {
                    // Extract text from similar image
                    val extractedText = extractTextFromSimilarImage(similarBitmap, similarImage.imageEntity.drugName)
                    
                    // Calculate text similarity with partial text
                    val textSimilarity = calculateTextSimilarity(partialText.lowercase(), extractedText.lowercase())
                    
                    if (textSimilarity > 0.3f) {
                        textSuggestions.add(
                            TextSuggestion(
                                text = extractedText,
                                confidence = (similarImage.similarityScore + textSimilarity) / 2f,
                                source = "visual_similarity",
                                drugName = similarImage.imageEntity.drugName
                            )
                        )
                    }
                }
            }
            
            // Select best recovery result
            val bestSuggestion = textSuggestions.maxByOrNull { it.confidence }
            
            if (bestSuggestion != null && bestSuggestion.confidence > 0.6f) {
                _processingState.value = VisualDatabaseState.Completed("Metin başarıyla kurtarıldı")
                
                DamagedTextRecoveryResult(
                    recoveredText = bestSuggestion.text,
                    confidence = bestSuggestion.confidence,
                    method = RecoveryMethod.VISUAL_SIMILARITY,
                    suggestions = textSuggestions.sortedByDescending { it.confidence }
                )
            } else {
                // Try partial text completion using drug database
                val completedText = attemptPartialTextCompletion(partialText)
                
                DamagedTextRecoveryResult(
                    recoveredText = completedText,
                    confidence = if (completedText != partialText) 0.7f else 0.3f,
                    method = if (completedText != partialText) RecoveryMethod.PARTIAL_COMPLETION else RecoveryMethod.FAILED,
                    suggestions = textSuggestions
                )
            }
            
        } catch (e: Exception) {
            _processingState.value = VisualDatabaseState.Error("Metin kurtarma hatası: ${e.message}")
            DamagedTextRecoveryResult(
                recoveredText = partialText,
                confidence = 0.1f,
                method = RecoveryMethod.ERROR,
                suggestions = emptyList(),
                errorMessage = e.message
            )
        }
    }
    
    /**
     * Batch import drug box images
     */
    suspend fun batchImportImages(
        images: List<Pair<Bitmap, String>>, // Bitmap and drug name pairs
        progressCallback: (Int, Int) -> Unit = { _, _ -> }
    ): BatchImportResult = withContext(Dispatchers.IO) {
        val results = mutableListOf<Long>()
        val errors = mutableListOf<String>()
        
        images.forEachIndexed { index, (bitmap, drugName) ->
            try {
                _processingState.value = VisualDatabaseState.Processing(
                    "İşleniyor ${index + 1}/${images.size}: $drugName"
                )
                
                val imageId = addDrugBoxImage(
                    bitmap = bitmap,
                    drugName = drugName,
                    source = ImageSource.BATCH_UPLOAD
                )
                results.add(imageId)
                
                progressCallback(index + 1, images.size)
                
            } catch (e: Exception) {
                errors.add("$drugName: ${e.message}")
            }
        }
        
        _processingState.value = VisualDatabaseState.Completed(
            "${results.size} görüntü başarıyla eklendi"
        )
        
        BatchImportResult(
            successCount = results.size,
            errorCount = errors.size,
            totalCount = images.size,
            errors = errors
        )
    }
    
    /**
     * Get statistics about visual database
     */
    suspend fun getDatabaseStatistics(): VisualDatabaseStatistics = withContext(Dispatchers.IO) {
        VisualDatabaseStatistics(
            totalImages = drugBoxImageDao.getImageCount(),
            imagesByCondition = mapOf(
                DrugBoxCondition.PERFECT to drugBoxImageDao.getImagesByCondition(DrugBoxCondition.PERFECT).size,
                DrugBoxCondition.SLIGHTLY_WORN to drugBoxImageDao.getImagesByCondition(DrugBoxCondition.SLIGHTLY_WORN).size,
                DrugBoxCondition.DAMAGED to drugBoxImageDao.getImagesByCondition(DrugBoxCondition.DAMAGED).size,
                DrugBoxCondition.HEAVILY_DAMAGED to drugBoxImageDao.getImagesByCondition(DrugBoxCondition.HEAVILY_DAMAGED).size
            ),
            storageUsedMB = calculateStorageUsage(),
            lastUpdated = System.currentTimeMillis()
        )
    }
    
    // Private helper methods
    
    private fun generateImageHash(bitmap: Bitmap): String {
        val bytes = bitmap.copyPixelsToBuffer(java.nio.ByteBuffer.allocate(bitmap.byteCount))
        val md = MessageDigest.getInstance("MD5")
        return md.digest(bytes.array()).joinToString("") { "%02x".format(it) }
    }
    
    private fun saveImageToStorage(bitmap: Bitmap, drugName: String, hash: String): String {
        val filename = "${drugName.replace(" ", "_")}_${hash.take(8)}.jpg"
        val file = File(imageStorageDir, filename)
        
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }
        
        return file.absolutePath
    }
    
    private fun loadImageFromStorage(imagePath: String): Bitmap? {
        return try {
            BitmapFactory.decodeFile(imagePath)
        } catch (e: Exception) {
            null
        }
    }
    
    private fun getImageFileSize(imagePath: String): Long {
        return File(imagePath).length()
    }
    
    private suspend fun extractAndSaveVisualFeatures(imageId: Long, bitmap: Bitmap) {
        val features = extractVisualFeatures(bitmap)
        
        features.forEach { (type, data) ->
            val feature = DrugBoxFeatureEntity(
                imageId = imageId,
                featureType = type,
                featureData = data.data,
                featureVector = data.vector,
                extractionMethod = data.method,
                confidence = data.confidence
            )
            drugBoxFeatureDao.insertFeature(feature)
        }
    }
    
    private fun extractVisualFeatures(bitmap: Bitmap): Map<VisualFeatureType, FeatureData> {
        return AdvancedVisualFeatureExtractor.extractComprehensiveFeatures(bitmap)
    }
    
    /**
     * Enhanced confidence integration system - Phase 2 Week 4
     */
    private fun combineTextAndVisualConfidence(
        textConfidence: Float,
        visualSimilarity: Float,
        visualConfidence: Float
    ): Float {
        // Weighted combination of text and visual confidence
        val textWeight = 0.6f
        val visualWeight = 0.4f
        
        val combinedScore = (textConfidence * textWeight) + 
                           (visualSimilarity * visualConfidence * visualWeight)
        
        // Apply bonus for high agreement between text and visual
        val agreementBonus = if (abs(textConfidence - visualSimilarity) < 0.2f) 0.1f else 0f
        
        return (combinedScore + agreementBonus).coerceIn(0f, 1f)
    }
    
    /**
     * Multi-algorithm similarity scoring system - Phase 2 Week 4 Enhancement
     */
    private fun calculateSimilarityScore(
        queryFeatures: Map<VisualFeatureType, FeatureData>,
        dbFeatures: List<DrugBoxFeatureEntity>
    ): Float {
        var totalScore = 0f
        var weightSum = 0f
        
        // Feature type weights for drug box matching
        val featureWeights = mapOf(
            VisualFeatureType.SIFT_FEATURES to 0.25f,
            VisualFeatureType.COLOR_HISTOGRAM to 0.20f,
            VisualFeatureType.TEXT_LAYOUT to 0.25f,
            VisualFeatureType.EDGE_FEATURES to 0.15f,
            VisualFeatureType.SHAPE_FEATURES to 0.10f,
            VisualFeatureType.TEXTURE_FEATURES to 0.05f
        )
        
        // Calculate weighted similarity for each feature type
        for (dbFeature in dbFeatures) {
            val queryFeature = queryFeatures[dbFeature.featureType]
            val weight = featureWeights[dbFeature.featureType] ?: 0.1f
            
            if (queryFeature != null) {
                val similarity = calculateAdvancedFeatureSimilarity(
                    queryFeature, 
                    dbFeature.featureVector, 
                    dbFeature.featureType
                )
                
                // Apply confidence weighting
                val confidenceWeightedSimilarity = similarity * 
                    (queryFeature.confidence + dbFeature.confidence) / 2f
                
                totalScore += confidenceWeightedSimilarity * weight
                weightSum += weight
            }
        }
        
        return if (weightSum > 0) totalScore / weightSum else 0f
    }
    
    /**
     * Advanced feature similarity calculation with algorithm-specific methods
     */
    private fun calculateAdvancedFeatureSimilarity(
        queryFeature: FeatureData,
        dbFeatureVector: String,
        featureType: VisualFeatureType
    ): Float {
        return when (featureType) {
            VisualFeatureType.SIFT_FEATURES -> calculateSIFTSimilarity(queryFeature.vector, dbFeatureVector)
            VisualFeatureType.COLOR_HISTOGRAM -> calculateHistogramSimilarity(queryFeature.vector, dbFeatureVector)
            VisualFeatureType.TEXT_LAYOUT -> calculateLayoutSimilarity(queryFeature.vector, dbFeatureVector)
            VisualFeatureType.EDGE_FEATURES -> calculateEdgeSimilarity(queryFeature.vector, dbFeatureVector)
            VisualFeatureType.SHAPE_FEATURES -> calculateShapeSimilarity(queryFeature.vector, dbFeatureVector)
            VisualFeatureType.TEXTURE_FEATURES -> calculateTextureSimilarity(queryFeature.vector, dbFeatureVector)
            else -> calculateCosineSimilarity(queryFeature.vector, dbFeatureVector)
        }
    }
    
    /**
     * SIFT feature similarity using descriptor matching
     */
    private fun calculateSIFTSimilarity(vector1: String, vector2: String): Float {
        try {
            val v1 = vector1.split(",").map { it.toFloat() }
            val v2 = vector2.split(",").map { it.toFloat() }
            
            if (v1.isEmpty() || v2.isEmpty()) return 0f
            
            // Calculate descriptor distance using L2 norm
            val descriptorSize = 16 // 16-dimensional descriptors
            var totalSimilarity = 0f
            var matchCount = 0
            
            for (i in v1.indices step descriptorSize) {
                if (i + descriptorSize <= v1.size) {
                    val desc1 = v1.subList(i, i + descriptorSize)
                    
                    // Find best match in v2
                    var bestMatch = 0f
                    for (j in v2.indices step descriptorSize) {
                        if (j + descriptorSize <= v2.size) {
                            val desc2 = v2.subList(j, j + descriptorSize)
                            val distance = calculateL2Distance(desc1, desc2)
                            val similarity = 1f / (1f + distance)
                            bestMatch = maxOf(bestMatch, similarity)
                        }
                    }
                    
                    if (bestMatch > 0.7f) { // Good match threshold
                        totalSimilarity += bestMatch
                        matchCount++
                    }
                }
            }
            
            return if (matchCount > 0) totalSimilarity / matchCount else 0f
            
        } catch (e: Exception) {
            return 0f
        }
    }
    
    /**
     * Color histogram similarity using Chi-squared distance
     */
    private fun calculateHistogramSimilarity(vector1: String, vector2: String): Float {
        try {
            val v1 = vector1.split(",").map { it.toFloat() }
            val v2 = vector2.split(",").map { it.toFloat() }
            
            if (v1.size != v2.size) return 0f
            
            var chiSquared = 0f
            for (i in v1.indices) {
                val sum = v1[i] + v2[i]
                if (sum > 0) {
                    val diff = v1[i] - v2[i]
                    chiSquared += (diff * diff) / sum
                }
            }
            
            // Convert chi-squared to similarity (0-1)
            return 1f / (1f + chiSquared)
            
        } catch (e: Exception) {
            return calculateCosineSimilarity(vector1, vector2)
        }
    }
    
    /**
     * Text layout similarity using spatial distribution comparison
     */
    private fun calculateLayoutSimilarity(vector1: String, vector2: String): Float {
        try {
            val v1 = vector1.split(",").map { it.toFloat() }
            val v2 = vector2.split(",").map { it.toFloat() }
            
            if (v1.size != v2.size || v1.size < 8) return 0f
            
            // Calculate weighted differences for layout features
            val regionWeights = floatArrayOf(0.15f, 0.25f, 0.15f, 0.15f, 0.15f) // top, center, bottom, left, right
            val densityWeight = 0.10f
            val blockSizeWeight = 0.05f
            
            var totalSimilarity = 0f
            
            // Region similarities
            for (i in 0 until 5) {
                val diff = abs(v1[i] - v2[i])
                val similarity = 1f - diff
                totalSimilarity += similarity * regionWeights[i]
            }
            
            // Text density similarity
            val densityDiff = abs(v1[5] - v2[5])
            totalSimilarity += (1f - densityDiff) * densityWeight
            
            // Block size similarity
            val blockSizeDiff = abs(v1[6] - v2[6]) / maxOf(v1[6], v2[6], 1f)
            totalSimilarity += (1f - blockSizeDiff) * blockSizeWeight
            
            return totalSimilarity.coerceIn(0f, 1f)
            
        } catch (e: Exception) {
            return calculateCosineSimilarity(vector1, vector2)
        }
    }
    
    /**
     * Edge feature similarity using gradient comparison
     */
    private fun calculateEdgeSimilarity(vector1: String, vector2: String): Float {
        try {
            val v1 = vector1.split(",").map { it.toFloat() }
            val v2 = vector2.split(",").map { it.toFloat() }
            
            if (v1.size != v2.size) return 0f
            
            // Edge statistics similarity (first 3 elements)
            var statsSimilarity = 0f
            for (i in 0 until minOf(3, v1.size)) {
                val diff = abs(v1[i] - v2[i]) / maxOf(v1[i], v2[i], 0.1f)
                statsSimilarity += 1f - diff
            }
            statsSimilarity /= 3f
            
            // Edge orientation histogram similarity (remaining elements)
            var histSimilarity = 0f
            if (v1.size > 3) {
                val hist1 = v1.subList(3, v1.size)
                val hist2 = v2.subList(3, v2.size)
                histSimilarity = calculateHistogramIntersection(hist1, hist2)
            }
            
            return (statsSimilarity * 0.6f + histSimilarity * 0.4f).coerceIn(0f, 1f)
            
        } catch (e: Exception) {
            return calculateCosineSimilarity(vector1, vector2)
        }
    }
    
    /**
     * Shape feature similarity using geometric descriptors
     */
    private fun calculateShapeSimilarity(vector1: String, vector2: String): Float {
        try {
            val v1 = vector1.split(",").map { it.toFloat() }
            val v2 = vector2.split(",").map { it.toFloat() }
            
            if (v1.isEmpty() || v2.isEmpty()) return 0f
            
            // Group features by contour (4 features per contour: area, perimeter, circularity, aspect ratio)
            val contourSize = 4
            var totalSimilarity = 0f
            var contourCount = 0
            
            for (i in v1.indices step contourSize) {
                if (i + contourSize <= v1.size) {
                    val contour1 = v1.subList(i, i + contourSize)
                    
                    // Find best matching contour in v2
                    var bestMatch = 0f
                    for (j in v2.indices step contourSize) {
                        if (j + contourSize <= v2.size) {
                            val contour2 = v2.subList(j, j + contourSize)
                            val similarity = calculateContourSimilarity(contour1, contour2)
                            bestMatch = maxOf(bestMatch, similarity)
                        }
                    }
                    
                    totalSimilarity += bestMatch
                    contourCount++
                }
            }
            
            return if (contourCount > 0) totalSimilarity / contourCount else 0f
            
        } catch (e: Exception) {
            return calculateCosineSimilarity(vector1, vector2)
        }
    }
    
    /**
     * Texture feature similarity using LBP histogram comparison
     */
    private fun calculateTextureSimilarity(vector1: String, vector2: String): Float {
        try {
            val v1 = vector1.split(",").map { it.toFloat() }
            val v2 = vector2.split(",").map { it.toFloat() }
            
            if (v1.size != v2.size) return 0f
            
            // Calculate histogram intersection for LBP patterns
            return calculateHistogramIntersection(v1, v2)
            
        } catch (e: Exception) {
            return calculateCosineSimilarity(vector1, vector2)
        }
    }
    
    // Helper methods for similarity calculations
    
    private fun calculateL2Distance(v1: List<Float>, v2: List<Float>): Float {
        var sum = 0f
        for (i in v1.indices) {
            val diff = v1[i] - v2[i]
            sum += diff * diff
        }
        return sqrt(sum)
    }
    
    private fun calculateHistogramIntersection(v1: List<Float>, v2: List<Float>): Float {
        var intersection = 0f
        for (i in v1.indices) {
            intersection += minOf(v1[i], v2[i])
        }
        return intersection
    }
    
    private fun calculateContourSimilarity(contour1: List<Float>, contour2: List<Float>): Float {
        // Weights for different shape features
        val areaWeight = 0.3f
        val perimeterWeight = 0.2f
        val circularityWeight = 0.3f
        val aspectRatioWeight = 0.2f
        
        val areaSim = 1f - abs(contour1[0] - contour2[0]) / maxOf(contour1[0], contour2[0], 1f)
        val perimSim = 1f - abs(contour1[1] - contour2[1]) / maxOf(contour1[1], contour2[1], 1f)
        val circSim = 1f - abs(contour1[2] - contour2[2])
        val aspectSim = 1f - abs(contour1[3] - contour2[3]) / maxOf(contour1[3], contour2[3], 1f)
        
        return (areaSim * areaWeight + perimSim * perimeterWeight + 
                circSim * circularityWeight + aspectSim * aspectRatioWeight).coerceIn(0f, 1f)
    }
    
    private fun calculateCosineSimilarity(vector1: String, vector2: String): Float {
        try {
            val v1 = vector1.split(",").map { it.toFloat() }
            val v2 = vector2.split(",").map { it.toFloat() }
            
            if (v1.size != v2.size) return 0f
            
            var dotProduct = 0f
            var norm1 = 0f
            var norm2 = 0f
            
            for (i in v1.indices) {
                dotProduct += v1[i] * v2[i]
                norm1 += v1[i] * v1[i]
                norm2 += v2[i] * v2[i]
            }
            
            val magnitude = sqrt(norm1) * sqrt(norm2)
            return if (magnitude > 0) dotProduct / magnitude else 0f
            
        } catch (e: Exception) {
            return 0f
        }
    }
    
    private fun identifyMatchingFeatures(
        queryFeatures: Map<VisualFeatureType, FeatureData>,
        dbFeatures: List<DrugBoxFeatureEntity>
    ): List<String> {
        return dbFeatures.mapNotNull { feature ->
            val queryFeature = queryFeatures[feature.featureType]
            if (queryFeature != null) {
                val similarity = calculateFeatureSimilarity(queryFeature.vector, feature.featureVector)
                if (similarity > 0.7f) feature.featureType.name else null
            } else null
        }
    }
    
    private fun extractTextFromSimilarImage(bitmap: Bitmap, drugName: String): String {
        // This would integrate with existing OCR system
        return drugName // Simplified for now
    }
    
    private fun calculateTextSimilarity(text1: String, text2: String): Float {
        // Simplified Levenshtein distance similarity
        val maxLength = maxOf(text1.length, text2.length)
        if (maxLength == 0) return 1f
        
        val distance = levenshteinDistance(text1, text2)
        return 1f - (distance.toFloat() / maxLength)
    }
    
    private fun levenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
        
        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j
        
        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                dp[i][j] = if (s1[i - 1] == s2[j - 1]) {
                    dp[i - 1][j - 1]
                } else {
                    1 + minOf(dp[i - 1][j], dp[i][j - 1], dp[i - 1][j - 1])
                }
            }
        }
        
        return dp[s1.length][s2.length]
    }
    
    private fun attemptPartialTextCompletion(partialText: String): String {
        // This would integrate with Turkish drug database
        return partialText // Simplified for now
    }
    
    private fun calculateStorageUsage(): Float {
        var totalSize = 0L
        imageStorageDir.listFiles()?.forEach { file ->
            totalSize += file.length()
        }
        return totalSize / (1024f * 1024f) // Convert to MB
    }
}

// Data classes for repository results

data class FeatureData(
    val data: String,
    val vector: String,
    val method: String,
    val confidence: Float
)

data class DamagedTextRecoveryResult(
    val recoveredText: String,
    val confidence: Float,
    val method: RecoveryMethod,
    val suggestions: List<TextSuggestion>,
    val errorMessage: String? = null
)

data class TextSuggestion(
    val text: String,
    val confidence: Float,
    val source: String,
    val drugName: String
)

enum class RecoveryMethod {
    VISUAL_SIMILARITY,
    PARTIAL_COMPLETION,
    NO_VISUAL_MATCH,
    FAILED,
    ERROR
}

data class BatchImportResult(
    val successCount: Int,
    val errorCount: Int,
    val totalCount: Int,
    val errors: List<String>
)

data class VisualDatabaseStatistics(
    val totalImages: Int,
    val imagesByCondition: Map<DrugBoxCondition, Int>,
    val storageUsedMB: Float,
    val lastUpdated: Long
)

sealed class VisualDatabaseState {
    object Idle : VisualDatabaseState()
    data class Processing(val message: String) : VisualDatabaseState()
    data class Completed(val message: String) : VisualDatabaseState()
    data class Error(val message: String) : VisualDatabaseState()
}
