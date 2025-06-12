package com.boxocr.simple.data

import com.boxocr.simple.ui.history.PrescriptionStatus
import com.boxocr.simple.repository.TimingProfile
import kotlinx.serialization.Serializable

/**
 * Comprehensive data models for the Box OCR app
 * Updated for Phase 3: Quality of Life Features
 */

// ============== CORE OCR MODELS ==============

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

// ============== VERIFICATION MODELS ==============

/**
 * Drug verification data for preview screen
 */
data class VerificationData(
    val capturedImagePath: String,    // Path to saved image file
    val ocrText: String,              // Raw OCR text extracted
    val matchResult: MatchResult,     // Best match with confidence
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Verification decision from user
 */
sealed class VerificationDecision {
    data class Confirmed(val finalDrugName: String) : VerificationDecision()
    data class Edited(val newDrugName: String) : VerificationDecision()
    object Rejected : VerificationDecision()
    object RequiresEnhancedMatching : VerificationDecision()
}

// ============== PRESCRIPTION HISTORY MODELS ==============

/**
 * Complete prescription history entry
 */
@Serializable
data class PrescriptionHistory(
    val id: String,
    val timestamp: Long,
    val drugs: List<String>,
    val drugCount: Int = drugs.size,
    val patientInfo: PatientInfo? = null,
    val status: PrescriptionStatus = PrescriptionStatus.COMPLETED,
    val sessionDuration: Long = 0, // in milliseconds
    val notes: String = ""
)

/**
 * Patient information
 */
@Serializable
data class PatientInfo(
    val name: String,
    val id: String = "",
    val dateOfBirth: String = "",
    val phoneNumber: String = "",
    val email: String = ""
)

/**
 * Prescription statistics for analytics
 */
data class PrescriptionStatistics(
    val totalPrescriptions: Int = 0,
    val uniqueDrugs: Int = 0,
    val thisMonth: Int = 0,
    val averagePerDay: Float = 0f,
    val mostCommonDrug: String = "None",
    val averageDrugsPerPrescription: Float = 0f,
    val busyestHour: Int = 12, // 24-hour format
    val completionRate: Float = 100f // percentage
)

// ============== SMART CAMERA MODELS ==============

/**
 * Smart detection state for camera
 */
data class SmartDetectionState(
    val hasDetectedText: Boolean = false,
    val detectedText: String = "",
    val isTextStable: Boolean = false,
    val drugBox: DrugBoxDetection? = null,
    val hasBarcode: Boolean = false,
    val barcodeInfo: BarcodeInfo? = null,
    val autoCapture: AutoCaptureSettings = AutoCaptureSettings(),
    val lastUpdateTime: Long = 0
) {
    fun isReadyForAutoCapture(): Boolean {
        return hasDetectedText && 
               isTextStable && 
               drugBox?.isDetected == true && 
               drugBox.confidence > 0.7f
    }
}

/**
 * Drug box detection result
 */
data class DrugBoxDetection(
    val isDetected: Boolean,
    val confidence: Float,
    val boundingBox: androidx.compose.ui.geometry.Rect,
    val textBlocks: Int
)

/**
 * Barcode information
 */
data class BarcodeInfo(
    val value: String,
    val format: Int,
    val type: Int,
    val boundingBox: androidx.compose.ui.geometry.Rect?
)

/**
 * Auto-capture settings
 */
data class AutoCaptureSettings(
    val isEnabled: Boolean = true,
    val stabilityThreshold: Int = 30,
    val confidenceThreshold: Float = 0.7f,
    val countdownSeconds: Int = 3
)

// ============== WINDOWS INTEGRATION MODELS ==============

/**
 * Windows integration settings
 */
@Serializable
data class WindowsIntegrationSettings(
    val pasteDelay: Long = 1000L,
    val enableFieldDetection: Boolean = true,
    val customKeystrokes: List<String> = listOf("F4"),
    val timingProfile: TimingProfile = TimingProfile.STANDARD,
    val retryAttempts: Int = 3,
    val enableSmartDetection: Boolean = true,
    val windowTitle: String = "",
    val autoFocusWindow: Boolean = true
)

/**
 * Automation execution result
 */
sealed class AutomationResult {
    data class Success(val message: String) : AutomationResult()
    data class Error(val message: String) : AutomationResult()
    data class Partial(
        val successCount: Int,
        val totalCount: Int,
        val failures: List<DrugPasteResult>
    ) : AutomationResult()
}

/**
 * Individual drug paste result
 */
@Serializable
data class DrugPasteResult(
    val drugName: String,
    val success: Boolean,
    val message: String
)

/**
 * Windows software detection result
 */
data class PrescriptionSoftware(
    val id: String,
    val name: String,
    val version: String,
    val isActive: Boolean,
    val confidence: Float
)

// ============== SESSION MANAGEMENT MODELS ==============

/**
 * Session backup and recovery state
 */
@Serializable
data class SessionState(
    val sessionId: String,
    val startTime: Long,
    val isActive: Boolean,
    val scannedDrugs: List<String>,
    val patientInfo: String? = null,
    val lastUpdateTime: Long = System.currentTimeMillis(),
    val totalDrugsPlanned: Int = 0,
    val isAutomationPending: Boolean = false
)

/**
 * Incomplete session information
 */
data class IncompleteSession(
    val id: String,
    val startTime: Long,
    val scannedDrugs: List<String>,
    val patientInfo: String = ""
)

// ============== TEMPLATE MODELS ==============

/**
 * Prescription template for common drug combinations
 */
@Serializable
data class PrescriptionTemplate(
    val id: String,
    val name: String,
    val category: TemplateCategory,
    val drugs: List<TemplateDrug>,
    val description: String = "",
    val usageCount: Int = 0,
    val lastUsed: Long = 0,
    val tags: List<String> = emptyList()
)

/**
 * Template drug with dosage information
 */
@Serializable
data class TemplateDrug(
    val name: String,
    val dosage: String = "",
    val frequency: String = "",
    val notes: String = "",
    val isOptional: Boolean = false
)

/**
 * Template categories
 */
enum class TemplateCategory(val displayName: String, val color: String) {
    DIABETES("Diabetes", "#4CAF50"),
    HYPERTENSION("Hypertension", "#2196F3"),
    CARDIOVASCULAR("Cardiovascular", "#F44336"),
    RESPIRATORY("Respiratory", "#FF9800"),
    PAIN_MANAGEMENT("Pain Management", "#9C27B0"),
    ANTIBIOTICS("Antibiotics", "#00BCD4"),
    VITAMINS("Vitamins & Supplements", "#8BC34A"),
    GENERAL("General", "#607D8B")
}

// ============== VOICE COMMAND MODELS ==============

/**
 * Voice command recognition result
 */
data class VoiceCommandResult(
    val command: String,
    val confidence: Float,
    val action: VoiceAction,
    val parameters: Map<String, String> = emptyMap()
)

/**
 * Available voice actions
 */
enum class VoiceAction {
    TAKE_PHOTO,
    NEXT_DRUG,
    CONFIRM,
    CANCEL,
    UNDO,
    COMPLETE_SESSION,
    START_SESSION,
    HELP,
    NAVIGATE_HOME,
    ENHANCED_MATCHING,
    MANUAL_ENTRY,
    REPEAT_LAST,
    VOICE_SETTINGS
}

/**
 * Voice command settings
 */
data class VoiceSettings(
    val isEnabled: Boolean = true,
    val language: String = "en-US",
    val sensitivity: Float = 0.7f,
    val feedbackEnabled: Boolean = true,
    val wakeWordEnabled: Boolean = false,
    val customCommands: Map<String, VoiceAction> = emptyMap()
)

// ============== EXPORT MODELS ==============

/**
 * Export configuration
 */
data class ExportConfig(
    val format: ExportFormat,
    val includePatientInfo: Boolean = true,
    val includeTimestamps: Boolean = true,
    val includeStatistics: Boolean = false,
    val dateRange: DateRange? = null
)

/**
 * Date range for filtering
 */
data class DateRange(
    val startDate: Long,
    val endDate: Long
)

/**
 * Export formats
 */
enum class ExportFormat {
    CSV, PDF, JSON, XLSX
}

// ============== ENHANCED MATCHING MODELS ==============

/**
 * Enhanced matching result with multiple algorithms
 */
data class EnhancedMatchResult(
    val originalText: String,
    val bestMatch: EnhancedMatch?,
    val alternativeMatches: List<EnhancedMatch>,
    val processingTime: Long,
    val algorithmsUsed: List<String>
)

/**
 * Enhanced match with detailed information
 */
data class EnhancedMatch(
    val drugName: String,
    val confidence: Float,
    val algorithm: String,
    val category: DrugCategory,
    val brandName: String? = null,
    val genericName: String? = null,
    val dosageInfo: String? = null,
    val manufacturer: String? = null
)

/**
 * Drug categories for enhanced matching
 */
enum class DrugCategory(val displayName: String, val threshold: Float) {
    EXACT("Exact Match", 0.95f),
    BRAND("Brand Name", 0.85f),
    GENERIC("Generic Name", 0.80f),
    PARTIAL("Partial Match", 0.70f),
    PHONETIC("Phonetic Match", 0.75f),
    ABBREVIATION("Abbreviation", 0.80f),
    UNKNOWN("Unknown", 0.60f)
}

// ============== ANALYTICS MODELS ==============

/**
 * Usage analytics for app improvement
 */
data class UsageAnalytics(
    val sessionCount: Int = 0,
    val totalScans: Int = 0,
    val averageSessionDuration: Long = 0,
    val mostUsedFeatures: Map<String, Int> = emptyMap(),
    val errorFrequency: Map<String, Int> = emptyMap(),
    val performanceMetrics: PerformanceMetrics = PerformanceMetrics()
)

/**
 * Performance metrics
 */
data class PerformanceMetrics(
    val averageOCRTime: Long = 0,
    val averageMatchingTime: Long = 0,
    val cameraInitTime: Long = 0,
    val memoryUsage: Long = 0,
    val networkLatency: Long = 0
)

// ============== UI STATE MODELS ==============

/**
 * Global app state
 */
data class AppState(
    val isOnline: Boolean = true,
    val isDatabaseLoaded: Boolean = false,
    val activeSessionId: String? = null,
    val lastBackupTime: Long = 0,
    val pendingUploads: Int = 0,
    val systemHealth: SystemHealth = SystemHealth()
)

/**
 * System health indicators
 */
data class SystemHealth(
    val cameraStatus: ComponentStatus = ComponentStatus.UNKNOWN,
    val networkStatus: ComponentStatus = ComponentStatus.UNKNOWN,
    val storageStatus: ComponentStatus = ComponentStatus.UNKNOWN,
    val apiStatus: ComponentStatus = ComponentStatus.UNKNOWN
)

/**
 * Component status
 */
enum class ComponentStatus {
    HEALTHY, WARNING, ERROR, UNKNOWN
}
