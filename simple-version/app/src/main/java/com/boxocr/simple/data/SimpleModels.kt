package com.boxocr.simple.data

import kotlinx.serialization.Serializable

/**
 * Simple Data Models for Basic OCR App
 * Clean, focused data structures for core functionality
 */

@Serializable
data class DatabaseItem(
    val name: String,
    val originalLine: String
)

@Serializable
data class ScanResult(
    val id: String,
    val timestamp: Long,
    val extractedText: String,
    val matchedItem: String?,
    val confidence: Float,
    val processingTime: Long
)

@Serializable
data class OCRResponse(
    val text: String,
    val confidence: Float,
    val processingTime: Long
)

@Serializable
data class MatchingResult(
    val bestMatch: String?,
    val confidence: Float,
    val allMatches: List<Pair<String, Float>>
)

@Serializable
data class AppSettings(
    val geminiApiKey: String = "",
    val matchingSensitivity: Float = 0.7f,
    val enableClipboardCopy: Boolean = true,
    val keepScanHistory: Boolean = true,
    val maxHistoryItems: Int = 50
)

enum class ScanStatus {
    IDLE, PROCESSING, SUCCESS, ERROR
}

@Serializable
data class AppState(
    val currentStatus: ScanStatus = ScanStatus.IDLE,
    val lastScanResult: ScanResult? = null,
    val errorMessage: String? = null
)
