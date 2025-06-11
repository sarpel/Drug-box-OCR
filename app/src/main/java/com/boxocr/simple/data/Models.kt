package com.boxocr.simple.data

/**
 * Simple data models for the Box OCR app
 */

/**
 * Result from OCR scanning
 */
data class ScanResult(
    val scannedText: String,      // Original OCR text
    val matchedText: String?,     // Matched database item (if found)
    val timestamp: String,        // When the scan was performed
    val confidence: Float = 0f    // Matching confidence (0-1)
)

/**
 * OCR processing result
 */
sealed class OCRResult {
    data class Success(
        val extractedText: String,
        val confidence: Float? = null
    ) : OCRResult()
    
    data class Error(
        val message: String,
        val exception: Throwable? = null
    ) : OCRResult()
    
    object Loading : OCRResult()
}

/**
 * Database matching result
 */
data class MatchResult(
    val originalText: String,
    val bestMatch: String?,
    val confidence: Float,
    val allMatches: List<Pair<String, Float>> = emptyList()
)

/**
 * App settings stored in SharedPreferences
 */
data class AppSettings(
    val apiKey: String = "",
    val similarityThreshold: Float = 0.7f,
    val autoClipboard: Boolean = true
)
