package com.boxocr.simple.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Multi-Drug Object Detector - Phase 1 Foundation Enhancement
 * 
 * Detects multiple drug boxes in a single image using ML Kit object detection
 * and combines with text recognition for comprehensive multi-drug processing.
 * 
 * Building on existing SmartCameraManager infrastructure for seamless integration.
 */
@Singleton
class MultiDrugObjectDetector @Inject constructor(
    @ApplicationContext private val context: Context,
    private val smartCameraManager: SmartCameraManager
) {
    
    // Object detector optimized for drug box detection
    private val objectDetector: ObjectDetector by lazy {
        val options = ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .enableMultipleObjects() // Key for multi-drug detection
            .enableClassification() // Helps distinguish drug boxes from other objects
            .build()
        
        ObjectDetection.getClient(options)
    }
    
    // Text recognizer for extracted regions
    private val textRecognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }
    
    // Detection state management
    private val _detectionState = MutableStateFlow<MultiDrugDetectionState>(
        MultiDrugDetectionState.Idle
    )
    val detectionState: StateFlow<MultiDrugDetectionState> = _detectionState.asStateFlow()
    
    /**
     * Detect multiple drug boxes in the provided image
     * 
     * @param image Input image containing potentially multiple drug boxes
     * @return List of detected drug box regions with confidence scores
     */
    suspend fun detectMultipleDrugBoxes(image: Bitmap): List<DrugBoxRegion> {
        return suspendCancellableCoroutine { continuation ->
            _detectionState.value = MultiDrugDetectionState.Processing
            
            val inputImage = InputImage.fromBitmap(image, 0)
            
            objectDetector.process(inputImage)
                .addOnSuccessListener { detectedObjects ->
                    // Filter and process detected objects to identify drug boxes
                    val drugBoxRegions = mutableListOf<DrugBoxRegion>()
                    
                    detectedObjects.forEachIndexed { index, detectedObject ->
                        // Filter objects that are likely drug boxes based on:
                        // 1. Size (reasonable box dimensions)
                        // 2. Aspect ratio (typical drug box proportions)
                        // 3. Position (not too close to edges)
                        if (isLikelyDrugBox(detectedObject, image.width, image.height)) {
                            val region = DrugBoxRegion(
                                id = "drug_box_$index",
                                boundingBox = detectedObject.boundingBox,
                                confidence = detectedObject.trackingId?.let { 0.8f } ?: 0.6f,
                                bitmap = extractRegionBitmap(image, detectedObject.boundingBox),
                                detectionMethod = DetectionMethod.OBJECT_DETECTION
                            )
                            drugBoxRegions.add(region)
                        }
                    }
                    
                    // If no objects detected, fall back to text-based region detection
                    if (drugBoxRegions.isEmpty()) {
                        detectTextBasedRegions(image) { textRegions ->
                            _detectionState.value = MultiDrugDetectionState.Completed(
                                drugBoxRegions + textRegions
                            )
                            continuation.resume(drugBoxRegions + textRegions)
                        }
                    } else {
                        _detectionState.value = MultiDrugDetectionState.Completed(drugBoxRegions)
                        continuation.resume(drugBoxRegions)
                    }
                }
                .addOnFailureListener { exception ->
                    _detectionState.value = MultiDrugDetectionState.Error(
                        "Object detection failed: ${exception.message}"
                    )
                    
                    // Fall back to text-based detection on ML Kit failure
                    detectTextBasedRegions(image) { textRegions ->
                        continuation.resume(textRegions)
                    }
                }
        }
    }
    
    /**
     * Detect drug box regions based on text clustering
     * Fallback method when object detection doesn't find clear boxes
     */
    private fun detectTextBasedRegions(
        image: Bitmap, 
        callback: (List<DrugBoxRegion>) -> Unit
    ) {
        val inputImage = InputImage.fromBitmap(image, 0)
        
        textRecognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                val textRegions = mutableListOf<DrugBoxRegion>()
                
                // Group text blocks that are close together (likely same drug box)
                val textClusters = clusterTextBlocks(visionText.textBlocks)
                
                textClusters.forEachIndexed { index, cluster ->
                    if (cluster.isNotEmpty()) {
                        val boundingRect = calculateClusterBoundingBox(cluster)
                        val region = DrugBoxRegion(
                            id = "text_cluster_$index",
                            boundingBox = boundingRect,
                            confidence = 0.7f, // Slightly lower confidence for text-based detection
                            bitmap = extractRegionBitmap(image, boundingRect),
                            detectionMethod = DetectionMethod.TEXT_CLUSTERING,
                            textBlocks = cluster
                        )
                        textRegions.add(region)
                    }
                }
                
                callback(textRegions)
            }
            .addOnFailureListener { exception ->
                _detectionState.value = MultiDrugDetectionState.Error(
                    "Text detection failed: ${exception.message}"
                )
                callback(emptyList())
            }
    }
    
    /**
     * Determine if detected object is likely a drug box
     */
    private fun isLikelyDrugBox(
        detectedObject: com.google.mlkit.vision.objects.DetectedObject,
        imageWidth: Int,
        imageHeight: Int
    ): Boolean {
        val box = detectedObject.boundingBox
        val boxWidth = box.width()
        val boxHeight = box.height()
        
        // Size constraints (reasonable drug box dimensions)
        val minBoxArea = (imageWidth * imageHeight) * 0.02f // At least 2% of image
        val maxBoxArea = (imageWidth * imageHeight) * 0.4f  // At most 40% of image
        val boxArea = boxWidth * boxHeight
        
        if (boxArea < minBoxArea || boxArea > maxBoxArea) return false
        
        // Aspect ratio constraints (typical drug box proportions)
        val aspectRatio = boxWidth.toFloat() / boxHeight.toFloat()
        if (aspectRatio < 0.3f || aspectRatio > 4.0f) return false
        
        // Position constraints (not too close to edges)
        val edgeMargin = 0.05f
        val leftMargin = box.left / imageWidth.toFloat()
        val topMargin = box.top / imageHeight.toFloat()
        val rightMargin = (imageWidth - box.right) / imageWidth.toFloat()
        val bottomMargin = (imageHeight - box.bottom) / imageHeight.toFloat()
        
        return leftMargin > edgeMargin && topMargin > edgeMargin && 
               rightMargin > edgeMargin && bottomMargin > edgeMargin
    }
    
    /**
     * Extract bitmap region from detected bounding box
     */
    private fun extractRegionBitmap(originalBitmap: Bitmap, boundingBox: Rect): Bitmap {
        return try {
            val x = maxOf(0, boundingBox.left)
            val y = maxOf(0, boundingBox.top)
            val width = minOf(boundingBox.width(), originalBitmap.width - x)
            val height = minOf(boundingBox.height(), originalBitmap.height - y)
            
            Bitmap.createBitmap(originalBitmap, x, y, width, height)
        } catch (e: Exception) {
            // Return a small placeholder bitmap if extraction fails
            Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        }
    }
    
    /**
     * Group text blocks that are spatially close (likely same drug box)
     */
    private fun clusterTextBlocks(
        textBlocks: List<com.google.mlkit.vision.text.Text.TextBlock>
    ): List<List<com.google.mlkit.vision.text.Text.TextBlock>> {
        val clusters = mutableListOf<MutableList<com.google.mlkit.vision.text.Text.TextBlock>>()
        val processed = mutableSetOf<com.google.mlkit.vision.text.Text.TextBlock>()
        
        for (block in textBlocks) {
            if (block in processed) continue
            
            val cluster = mutableListOf<com.google.mlkit.vision.text.Text.TextBlock>()
            cluster.add(block)
            processed.add(block)
            
            // Find nearby text blocks to cluster together
            for (otherBlock in textBlocks) {
                if (otherBlock in processed) continue
                
                if (areTextBlocksNearby(block, otherBlock)) {
                    cluster.add(otherBlock)
                    processed.add(otherBlock)
                }
            }
            
            clusters.add(cluster)
        }
        
        return clusters
    }
    
    /**
     * Check if two text blocks are spatially close enough to be in same drug box
     */
    private fun areTextBlocksNearby(
        block1: com.google.mlkit.vision.text.Text.TextBlock,
        block2: com.google.mlkit.vision.text.Text.TextBlock
    ): Boolean {
        val box1 = block1.boundingBox ?: return false
        val box2 = block2.boundingBox ?: return false
        
        val distance = kotlin.math.sqrt(
            ((box1.centerX() - box2.centerX()).toDouble().pow(2.0) +
             (box1.centerY() - box2.centerY()).toDouble().pow(2.0))
        ).toFloat()
        
        val averageSize = (box1.width() + box1.height() + box2.width() + box2.height()) / 4f
        val maxDistance = averageSize * 2.0f // Blocks can be up to 2x average size apart
        
        return distance <= maxDistance
    }
    
    /**
     * Calculate bounding box that encompasses all text blocks in cluster
     */
    private fun calculateClusterBoundingBox(
        cluster: List<com.google.mlkit.vision.text.Text.TextBlock>
    ): Rect {
        var minX = Int.MAX_VALUE
        var minY = Int.MAX_VALUE
        var maxX = Int.MIN_VALUE
        var maxY = Int.MIN_VALUE
        
        for (block in cluster) {
            block.boundingBox?.let { box ->
                minX = minOf(minX, box.left)
                minY = minOf(minY, box.top)
                maxX = maxOf(maxX, box.right)
                maxY = maxOf(maxY, box.bottom)
            }
        }
        
        // Add some padding around the text cluster
        val padding = 20
        return Rect(
            maxOf(0, minX - padding),
            maxOf(0, minY - padding),
            maxX + padding,
            maxY + padding
        )
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        objectDetector.close()
        textRecognizer.close()
    }
}

/**
 * Represents a detected drug box region in the image
 */
data class DrugBoxRegion(
    val id: String,
    val boundingBox: Rect,
    val confidence: Float,
    val bitmap: Bitmap,
    val detectionMethod: DetectionMethod,
    val textBlocks: List<com.google.mlkit.vision.text.Text.TextBlock> = emptyList(),
    val extractedText: String = "",
    val drugName: String = "",
    val matchConfidence: Float = 0f
)

/**
 * Detection method used to identify the drug box region
 */
enum class DetectionMethod {
    OBJECT_DETECTION,  // ML Kit object detection
    TEXT_CLUSTERING,   // Text-based region clustering
    MANUAL_SELECTION   // User-selected region
}

/**
 * State of multi-drug detection process
 */
sealed class MultiDrugDetectionState {
    object Idle : MultiDrugDetectionState()
    object Processing : MultiDrugDetectionState()
    data class Completed(val regions: List<DrugBoxRegion>) : MultiDrugDetectionState()
    data class Error(val message: String) : MultiDrugDetectionState()
}

// Extension functions for convenience
private fun Rect.centerX(): Int = left + width() / 2
private fun Rect.centerY(): Int = top + height() / 2
private fun Double.pow(n: Double): Double = kotlin.math.pow(this, n)
