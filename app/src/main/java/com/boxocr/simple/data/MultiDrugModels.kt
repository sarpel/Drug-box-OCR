package com.boxocr.simple.data

import android.graphics.Bitmap
import kotlinx.serialization.Serializable

@Serializable
data class MultiDrugResult(
    val id: String,
    val detectedName: String,
    val finalDrugName: String,
    val confidence: Float,
    val finalConfidence: Float,
    val enhancementMethod: EnhancementMethod,
    val originalRegion: DrugRegion,
    val croppedDrugBoxBitmap: String? = null, // Base64 encoded bitmap
    val ocrText: String,
    val drugDatabaseMatch: DrugDatabaseMatch? = null,
    val visualRecovery: DamagedTextRecoveryResult? = null,
    val batchIntegration: BatchIntegrationResult? = null,
    val isVerified: Boolean = false,
    val hasVisualMatch: Boolean = false,
    val isAutoVerified: Boolean = false,
    val processingTime: Long = 0L
)

@Serializable
data class DrugRegion(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int
)

@Serializable
data class DrugDatabaseMatch(
    val drugName: String,
    val confidence: Float,
    val source: String
)

@Serializable
data class DamagedTextRecoveryResult(
    val recoveredText: String,
    val confidence: Float,
    val recoveryMethod: String,
    val method: String
)

@Serializable
data class BatchIntegrationResult(
    val sessionId: String,
    val totalItems: Int,
    val processedItems: Int,
    val successfulItems: Int
)

@Serializable
data class BatchSession(
    val id: String,
    val name: String,
    val createdAt: Long,
    val totalItems: Int = 0,
    val processedItems: Int = 0
)

@Serializable
data class MultiDrugScanResult(
    val timestamp: Long,
    val drugCount: Int,
    val averageConfidence: Float,
    val processingTime: Long,
    val source: ImageSource,
    val enhancedResults: List<EnhancedDrugResult>,
    val drugNames: List<String>,
    val success: Boolean = true,
    val originalImage: String? = null // Base64 encoded
)

@Serializable
data class EnhancedDrugResult(
    val originalRegion: DrugRegion,
    val finalDrugName: String,
    val finalConfidence: Float,
    val enhancementMethod: EnhancementMethod,
    val drugDatabaseMatch: DrugDatabaseMatch? = null,
    val visualRecovery: DamagedTextRecoveryResult? = null
)

@Serializable
data class VisualSimilarityMatch(
    val drugName: String,
    val confidence: Float,
    val similarity: Float
)

enum class EnhancementMethod {
    NONE, OCR_ENHANCEMENT, VISUAL_MATCHING, AI_CORRECTION, MANUAL_CORRECTION
}

enum class ImageSource {
    CAMERA, GALLERY, BATCH_SCAN, FILE_IMPORT
}

enum class DrugBoxCondition {
    EXCELLENT, GOOD, FAIR, POOR, DAMAGED
}

enum class DrugBoxAngle {
    FRONT, SIDE, BACK, ANGLED
}

enum class DrugBoxLighting {
    OPTIMAL, GOOD, POOR, VERY_POOR
}

// Extension functions for convenience
fun List<MultiDrugResult>.toMutableList(): MutableList<MultiDrugResult> = this.toMutableList()