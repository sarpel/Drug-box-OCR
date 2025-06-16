package com.boxocr.simple.data

import kotlinx.serialization.Serializable

/**
 * Multi-Drug Models - Phase 3 Enhancement
 * Contains models specific to multi-drug scanning functionality
 * Core models like BatchSession, DrugPasteResult are in AppModels.kt
 */

// ============== MULTI-DRUG SCANNING MODELS ==============

@Serializable
data class MultiDrugScanResult(
    val timestamp: Long,
    val drugCount: Int,
    val success: Boolean,
    val detectedDrugs: List<DetectedDrug>,
    val processingTimeMs: Long,
    val confidence: Float,
    val sessionId: String
)

@Serializable
data class DetectedDrug(
    val boundingBox: BoundingBox,
    val extractedText: String,
    val matchedDrugName: String?,
    val confidence: Float,
    val processingOrder: Int,
    val visualSimilarityScore: Float = 0.0f,
    val textRecoveryApplied: Boolean = false
)

@Serializable
data class BoundingBox(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
)

@Serializable
data class MultiDrugSession(
    val sessionId: String,
    val timestamp: Long,
    val drugs: List<DetectedDrug>,
    val totalDrugCount: Int,
    val successfulMatches: Int,
    val processingTimeMs: Long,
    val imageMetadata: ImageMetadata? = null
)

@Serializable
data class ImageMetadata(
    val width: Int,
    val height: Int,
    val sourceType: ImageSourceType,
    val enhancementApplied: EnhancementMethod? = null
)

enum class ImageSourceType {
    CAMERA_CAPTURE,
    GALLERY_IMAGE,
    LIVE_VIDEO_FRAME
}

enum class EnhancementMethod {
    BRIGHTNESS_CONTRAST,
    NOISE_REDUCTION,
    SHARPENING,
    EDGE_ENHANCEMENT,
    TEXT_BINARIZATION
}

// ============== VISUAL SIMILARITY MODELS ==============

@Serializable
data class VisualSimilarityResult(
    val drugBoxImage: DrugBoxImageInfo,
    val similarityScore: Float,
    val matchingFeatures: List<String>,
    val visualConfidence: Float
)

@Serializable
data class DrugBoxImageInfo(
    val drugName: String,
    val imageHash: String,
    val condition: BoxCondition,
    val angle: CaptureAngle,
    val lighting: LightingCondition
)

enum class BoxCondition {
    PERFECT,
    GOOD,
    WORN,
    DAMAGED,
    SEVERELY_DAMAGED
}

enum class CaptureAngle {
    FRONT,
    LEFT_SIDE,
    RIGHT_SIDE,
    TOP,
    ANGLED
}

enum class LightingCondition {
    OPTIMAL,
    OVEREXPOSED,
    UNDEREXPOSED,
    LOW_CONTRAST,
    SHADOW_PRESENT
}

// ============== TEXT RECOVERY MODELS ==============

@Serializable
data class DamagedTextRecoveryResult(
    val originalText: String,
    val recoveredText: String,
    val confidence: Float,
    val recoveryMethod: TextRecoveryMethod,
    val dictionaryMatches: List<String>
)

enum class TextRecoveryMethod {
    DICTIONARY_COMPLETION,
    AI_RECONSTRUCTION,
    PATTERN_MATCHING,
    CONTEXT_INFERENCE,
    HYBRID_APPROACH
}

// ============== REAL-TIME SCANNING MODELS ==============

@Serializable
data class LiveScanningState(
    val isScanning: Boolean,
    val detectedBoxes: List<BoundingBox>,
    val currentDrugCount: Int,
    val frameProcessingRate: Float,
    val lastDetectionTimestamp: Long
)

@Serializable
data class RealtimeDetection(
    val boundingBox: BoundingBox,
    val confidence: Float,
    val stabilityScore: Float,
    val framesSinceDetection: Int,
    val preliminaryText: String? = null
)


// ============== OBJECT DETECTION MODELS ==============

@Serializable
data class DrugBoxRegion(
    val boundingBox: BoundingBox,
    val confidence: Float,
    val regionId: String,
    val extractedText: String? = null,
    val ocrConfidence: Float = 0.0f,
    val matchedDrug: DrugInfo? = null,
    val visualSimilarityScore: Float = 0.0f,
    val textRecoveryApplied: Boolean = false,
    val processingOrder: Int = 0
)

@Serializable
data class MultiDrugResult(
    val sessionId: String,
    val timestamp: Long,
    val imageSource: ImageSourceType,
    val detectedRegions: List<DrugBoxRegion>,
    val totalDrugCount: Int,
    val successfulMatches: Int,
    val failedMatches: Int,
    val averageConfidence: Float,
    val processingTimeMs: Long,
    val visualEnhancementApplied: Boolean = false,
    val textRecoveryUsed: Boolean = false,
    val metadata: Map<String, String> = emptyMap()
)

@Serializable
data class ObjectDetectionResult(
    val detectedObjects: List<DrugBoxRegion>,
    val processingTime: Long,
    val modelUsed: String,
    val imageSize: ImageSize,
    val detectionConfidenceThreshold: Float
)

@Serializable
data class ImageSize(
    val width: Int,
    val height: Int
)

// ============== MULTI-DRUG SCANNING STATE ==============

@Serializable
data class MultiDrugScanningState(
    val isScanning: Boolean,
    val currentMode: ScanningMode,
    val detectedRegions: List<DrugBoxRegion>,
    val lastFrameProcessingTime: Long,
    val frameRate: Float,
    val stabilityMetrics: ScanningStabilityMetrics
)

@Serializable
enum class ScanningMode {
    SINGLE_DRUG,
    MULTI_DRUG,
    LIVE_VIDEO,
    DAMAGED_RECOVERY,
    BATCH_PROCESSING
}

@Serializable
data class ScanningStabilityMetrics(
    val averageConfidence: Float,
    val detectionStability: Float,
    val frameConsistency: Float,
    val processingConsistency: Float
)
