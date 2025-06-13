package com.boxocr.simple.repository

import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Multi-Drug Object Detector - Phase 1 Enhancement
 * 
 * Uses ML Kit Object Detection to identify multiple drug boxes in a single image.
 * Provides bounding boxes for each detected drug box for individual OCR processing.
 */
@Singleton
class MultiDrugObjectDetector @Inject constructor(
    private val smartCameraManager: SmartCameraManager
) {
    
    companion object {
        private const val TAG = "MultiDrugObjectDetector"
        private const val MIN_CONFIDENCE = 0.7f
        private const val MIN_BOX_SIZE = 100 // Minimum width/height in pixels
    }
    
    data class DrugBoxDetection(
        val boundingBox: Rect,
        val confidence: Float,
        val trackingId: Int?,
        val croppedImage: Bitmap
    )
    
    data class MultiDrugDetectionResult(
        val detections: List<DrugBoxDetection>,
        val processingTimeMs: Long,
        val totalObjectsFound: Int,
        val filteredObjectsCount: Int
    )
    
    private val objectDetector = ObjectDetection.getClient(
        ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
            .enableMultipleObjects()
            .enableClassification()
            .build()
    )
    
    /**
     * Detect multiple drug boxes in the given image
     */
    suspend fun detectDrugBoxes(bitmap: Bitmap): MultiDrugDetectionResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        
        try {
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            val detectedObjects = suspendCancellableCoroutine { continuation ->
                objectDetector.process(inputImage)
                    .addOnSuccessListener { objects ->
                        continuation.resume(objects)
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Object detection failed", exception)
                        continuation.resume(emptyList())
                    }
            }
            
            val drugBoxDetections = mutableListOf<DrugBoxDetection>()
            var filteredCount = 0
            
            detectedObjects.forEachIndexed { index, detectedObject ->
                val boundingBox = detectedObject.boundingBox
                
                // Filter based on confidence and size
                if (isValidDrugBox(boundingBox, bitmap.width, bitmap.height)) {
                    val croppedImage = cropImageToBoundingBox(bitmap, boundingBox)
                    
                    drugBoxDetections.add(
                        DrugBoxDetection(
                            boundingBox = boundingBox,
                            confidence = MIN_CONFIDENCE, // Default confidence for detected objects
                            trackingId = detectedObject.trackingId,
                            croppedImage = croppedImage
                        )
                    )
                } else {
                    filteredCount++
                }
            }
            
            val processingTime = System.currentTimeMillis() - startTime
            
            MultiDrugDetectionResult(
                detections = drugBoxDetections,
                processingTimeMs = processingTime,
                totalObjectsFound = detectedObjects.size,
                filteredObjectsCount = filteredCount
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during drug box detection", e)
            MultiDrugDetectionResult(
                detections = emptyList(),
                processingTimeMs = System.currentTimeMillis() - startTime,
                totalObjectsFound = 0,
                filteredObjectsCount = 0
            )
        }
    }
    
    /**
     * Validate if detected object is likely a drug box
     */
    private fun isValidDrugBox(boundingBox: Rect, imageWidth: Int, imageHeight: Int): Boolean {
        val width = boundingBox.width()
        val height = boundingBox.height()
        
        // Check minimum size
        if (width < MIN_BOX_SIZE || height < MIN_BOX_SIZE) {
            return false
        }
        
        // Check if it's not too large (likely the whole image)
        val imageArea = imageWidth * imageHeight
        val boxArea = width * height
        if (boxArea > imageArea * 0.8) {
            return false
        }
        
        // Check aspect ratio (drug boxes are typically rectangular)
        val aspectRatio = width.toFloat() / height.toFloat()
        if (aspectRatio < 0.3 || aspectRatio > 3.0) {
            return false
        }
        
        return true
    }
    
    /**
     * Crop image to bounding box with padding
     */
    private fun cropImageToBoundingBox(bitmap: Bitmap, boundingBox: Rect): Bitmap {
        val padding = 10 // Add small padding around detected object
        
        val left = maxOf(0, boundingBox.left - padding)
        val top = maxOf(0, boundingBox.top - padding)
        val right = minOf(bitmap.width, boundingBox.right + padding)
        val bottom = minOf(bitmap.height, boundingBox.bottom + padding)
        
        val width = right - left
        val height = bottom - top
        
        return if (width > 0 && height > 0) {
            Bitmap.createBitmap(bitmap, left, top, width, height)
        } else {
            bitmap // Return original if cropping fails
        }
    }
    
    /**
     * Process live camera frame for real-time detection
     */
    suspend fun processLiveFrame(bitmap: Bitmap): List<Rect> = withContext(Dispatchers.IO) {
        try {
            val result = detectDrugBoxes(bitmap)
            result.detections.map { it.boundingBox }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing live frame", e)
            emptyList()
        }
    }
    
    /**
     * Release resources
     */
    fun release() {
        try {
            objectDetector.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing object detector", e)
        }
    }
}
