package com.boxocr.simple.data

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.boxocr.simple.ui.history.PrescriptionStatus
import com.boxocr.simple.repository.TimingProfile
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.UUID

/**
 * Centralized data models for the Box OCR app
 * All data classes, enums, and type converters in one place
 */

// ============== CORE OCR MODELS ==============

data class ScanResult(
    val scannedText: String,
    val matchedText: String?,
    val timestamp: String,
    val confidence: Float = 0f
)

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

data class MatchResult(
    val originalText: String,
    val bestMatch: String?,
    val confidence: Float,
    val allMatches: List<Pair<String, Float>> = emptyList()
)

data class AppSettings(
    val apiKey: String = "",
    val similarityThreshold: Float = 0.7f,
    val autoClipboard: Boolean = true
)

// ============== VERIFICATION MODELS ==============

data class VerificationData(
    val capturedImagePath: String,
    val ocrText: String,
    val matchResult: MatchResult,
    val timestamp: Long = System.currentTimeMillis()
)

sealed class VerificationDecision {
    data class Confirmed(val finalDrugName: String) : VerificationDecision()
    data class Edited(val newDrugName: String) : VerificationDecision()
    object Rejected : VerificationDecision()
    object RequiresEnhancedMatching : VerificationDecision()
}

// ============== PATIENT AND PRESCRIPTION MODELS ==============

@Serializable
data class PatientInfo(
    val patientId: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val name: String = "$firstName $lastName".trim(),
    val id: String = patientId,
    val dateOfBirth: String = "",
    val nationalId: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val address: String = "",
    val emergencyContact: String = "",
    val medicalNotes: String = "",
    val allergies: List<String> = emptyList(),
    val chronicConditions: List<String> = emptyList()
)

@Serializable
data class PrescriptionHistory(
    val id: String,
    val timestamp: Long,
    val drugs: List<String>,
    val drugCount: Int = drugs.size,
    val patientInfo: PatientInfo? = null,
    val status: PrescriptionStatus = PrescriptionStatus.COMPLETED,
    val sessionDuration: Long = 0,
    val notes: String = ""
)

data class PrescriptionStatistics(
    val totalPrescriptions: Int = 0,
    val uniqueDrugs: Int = 0,
    val thisMonth: Int = 0,
    val averagePerDay: Float = 0f,
    val mostCommonDrug: String = "None",
    val averageDrugsPerPrescription: Float = 0f,
    val busyestHour: Int = 12,
    val completionRate: Float = 100f
)

// ============== TEMPLATE MODELS ==============

@Serializable
data class PrescriptionTemplate(
    val id: String,
    val name: String,
    val category: TemplateCategory,
    val drugs: List<TemplateDrug>,
    val description: String = "",
    val usageCount: Int = 0,
    val lastUsed: Long = 0,
    val tags: List<String> = emptyList(),
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class TemplateDrug(
    val name: String,
    val dosage: String = "",
    val frequency: String = "",
    val duration: String = "",
    val instructions: String = "",
    val notes: String = "",
    val isOptional: Boolean = false,
    val isRequired: Boolean = !isOptional,
    val alternatives: List<String> = emptyList()
)

enum class TemplateCategory(val displayName: String, val color: String) {
    DIABETES("Diabetes Management", "#4CAF50"),
    HYPERTENSION("Hypertension", "#2196F3"),
    CARDIOVASCULAR("Cardiovascular", "#F44336"),
    RESPIRATORY("Respiratory", "#FF9800"),
    PAIN_MANAGEMENT("Pain Management", "#9C27B0"),
    ANTIBIOTIC("Antibiotic Treatment", "#00BCD4"),
    CHRONIC_DISEASE("Chronic Disease", "#795548"),
    PREVENTIVE("Preventive Care", "#607D8B"),
    ANTIBIOTICS("Antibiotics", "#00BCD4"),
    VITAMINS("Vitamins & Supplements", "#8BC34A"),
    GENERAL("General", "#607D8B"),
    CUSTOM("Custom Template", "#9E9E9E")
}

data class TemplateUsage(
    val templateId: String,
    val usageCount: Int,
    val lastUsed: Long,
    val averageTimeToComplete: Long = 0L
)

// ============== SMART CAMERA MODELS ==============

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

data class DrugBoxDetection(
    val isDetected: Boolean,
    val confidence: Float,
    val boundingBox: androidx.compose.ui.geometry.Rect,
    val textBlocks: Int
)

data class BarcodeInfo(
    val value: String,
    val format: Int,
    val type: Int,
    val boundingBox: androidx.compose.ui.geometry.Rect?
)

data class AutoCaptureSettings(
    val isEnabled: Boolean = true,
    val stabilityThreshold: Int = 30,
    val confidenceThreshold: Float = 0.7f,
    val countdownSeconds: Int = 3
)

// ============== WINDOWS INTEGRATION MODELS ==============

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

@Serializable
data class WindowsAutomationConfig(
    val pasteDelay: Long = 1000L,
    val enableFieldDetection: Boolean = true,
    val customKeystrokes: List<String> = listOf("F4"),
    val timingProfile: TimingProfile = TimingProfile.STANDARD,
    val retryAttempts: Int = 3,
    val enableSmartDetection: Boolean = true,
    val windowTitle: String = "",
    val autoFocusWindow: Boolean = true
)

sealed class AutomationResult {
    data class Success(val message: String) : AutomationResult()
    data class Error(val message: String) : AutomationResult()
    data class Partial(
        val successCount: Int,
        val totalCount: Int,
        val failures: List<DrugPasteResult>
    ) : AutomationResult()
}

@Serializable
data class DrugPasteResult(
    val drugName: String,
    val success: Boolean,
    val message: String
)

data class PrescriptionSoftware(
    val id: String,
    val name: String,
    val version: String,
    val isActive: Boolean,
    val confidence: Float
)

// ============== SESSION MANAGEMENT MODELS ==============

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

data class IncompleteSession(
    val id: String,
    val startTime: Long,
    val scannedDrugs: List<String>,
    val patientInfo: String = ""
)

// ============== MULTI-DRUG SESSION MODELS ==============

@Entity(tableName = "multi_drug_sessions")
@TypeConverters(MultiDrugSessionConverters::class)
data class MultiDrugSessionEntity(
    @PrimaryKey val sessionId: String,
    val sessionName: String,
    val status: String,
    val patientInfo: String?,
    val config: String,
    val scanningPhase: String?,
    val verificationPhase: String?,
    val batchProcessingPhase: String?,
    val sessionSummary: String?,
    val createdAt: Long,
    val lastUpdated: Long,
    val completedAt: Long? = null
)

data class MultiDrugSession(
    val sessionId: String,
    val sessionName: String,
    val patientInfo: PatientInfo? = null,
    val config: MultiDrugSessionConfig = MultiDrugSessionConfig(),
    val status: SessionStatus = SessionStatus.CREATED,
    val scanningPhase: ScanningPhase? = null,
    val verificationPhase: VerificationPhase? = null,
    val batchProcessingPhase: BatchProcessingPhase? = null,
    val sessionSummary: SessionSummary? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUpdated: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

@Serializable
data class MultiDrugSessionConfig(
    val autoVerifyHighConfidence: Boolean = true,
    val confidenceThreshold: Float = 0.9f,
    val requireVisualMatch: Boolean = false,
    val enableDamagedTextRecovery: Boolean = true,
    val autoExport: Boolean = true,
    val exportFormat: SessionExportFormat = SessionExportFormat.COMPREHENSIVE,
    val windowsIntegration: Boolean = true,
    val batchProcessing: Boolean = true,
    val maxDrugsPerSession: Int = 50,
    val sessionTimeout: Long = 3600000L,
    val retryFailedDetections: Boolean = true,
    val maxRetryAttempts: Int = 3
)

enum class SessionStatus {
    CREATED,
    SCANNING,
    VERIFICATION,
    BATCH_PROCESSING,
    COMPLETED,
    ERROR,
    CANCELLED
}

enum class SessionExportFormat {
    BASIC,
    DETAILED,
    COMPREHENSIVE,
    MEDICAL_REPORT
}

enum class ExportFormat {
    CSV, PDF, JSON, XLSX
}

// ============== SCANNING PHASE MODELS ==============

@Serializable
data class ScanningPhase(
    val sessionId: String,
    val config: ScanningConfig = ScanningConfig(),
    val status: ScanningStatus = ScanningStatus.ACTIVE,
    val results: List<MultiDrug> = emptyList(),
    val totalDrugsDetected: Int = 0,
    val totalScans: Int = 0,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val lastScanTime: Long? = null,
    val scanningMetrics: ScanningMetrics = ScanningMetrics()
)

@Serializable
data class ScanningConfig(
    val mode: ScanningMode = ScanningMode.MULTI_DRUG,
    val maxDrugsPerScan: Int = 10,
    val enableLiveVideo: Boolean = true,
    val enableDamagedRecovery: Boolean = true,
    val confidenceThreshold: Float = 0.6f,
    val visualMatchEnabled: Boolean = true,
    val autoCapture: Boolean = false,
    val captureDelay: Long = 2000L,
    val maxRetries: Int = 3
)

enum class ScanningMode {
    SINGLE_DRUG,
    MULTI_DRUG,
    LIVE_VIDEO,
    BATCH_CAPTURE
}

enum class ScanningStatus {
    ACTIVE,
    PAUSED,
    COMPLETED,
    ERROR
}

@Serializable
data class ScanningMetrics(
    val totalScanTime: Long = 0L,
    val averageDrugsPerScan: Float = 0f,
    val averageConfidence: Float = 0f,
    val visualMatchRate: Float = 0f,
    val recoverySuccessRate: Float = 0f,
    val processingSpeed: Float = 0f,
    val errorRate: Float = 0f
)

// ============== VERIFICATION PHASE MODELS ==============

@Serializable
data class VerificationPhase(
    val sessionId: String,
    val results: List<MultiDrug> = emptyList(),
    val totalDrugs: Int = 0,
    val verifiedCount: Int = 0,
    val unverifiedCount: Int = 0,
    val correctedCount: Int = 0,
    val status: VerificationStatus = VerificationStatus.ACTIVE,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val lastUpdateTime: Long? = null,
    val verificationMetrics: VerificationMetrics = VerificationMetrics()
)

enum class VerificationStatus {
    ACTIVE,
    COMPLETED,
    REVIEW_REQUIRED
}

@Serializable
data class VerificationMetrics(
    val autoVerificationRate: Float = 0f,
    val manualVerificationRate: Float = 0f,
    val correctionRate: Float = 0f,
    val averageVerificationTime: Long = 0L,
    val confidenceDistribution: Map<String, Int> = emptyMap(),
    val verificationAccuracy: Float = 0f
)

// ============== BATCH PROCESSING MODELS ==============

@Serializable
data class BatchProcessingPhase(
    val sessionId: String,
    val verifiedResults: List<MultiDrug> = emptyList(),
    val totalDrugs: Int = 0,
    val currentDrugIndex: Int = 0,
    val config: BatchProcessingConfig = BatchProcessingConfig(),
    val status: BatchProcessingStatus = BatchProcessingStatus.READY,
    val batchSessionId: String? = null,
    val windowsSessionId: String? = null,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val processingStartTime: Long? = null,
    val completedDrugs: List<CompletedDrugProcessing> = emptyList(),
    val failedDrugs: List<FailedDrugProcessing> = emptyList(),
    val isProcessing: Boolean = false,
    val progress: Float = 0f,
    val batchSummary: BatchProcessingSummary? = null,
    val error: String? = null
)

@Serializable
data class BatchProcessingConfig(
    val windowsIntegration: Boolean = true,
    val windowsConfig: WindowsAutomationConfig = WindowsAutomationConfig(),
    val autoStart: Boolean = false,
    val parallelProcessing: Boolean = false,
    val maxConcurrentDrugs: Int = 3,
    val retryFailedDrugs: Boolean = true,
    val maxRetryAttempts: Int = 3,
    val timeoutPerDrug: Long = 30000L,
    val enableProgressReporting: Boolean = true
)

enum class BatchProcessingStatus {
    READY,
    INITIALIZING,
    PROCESSING,
    PAUSED,
    COMPLETED,
    ERROR,
    CANCELLED
}

@Serializable
data class CompletedDrugProcessing(
    val drugName: String,
    val processingTime: Long,
    val success: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class FailedDrugProcessing(
    val drugName: String,
    val error: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class BatchProcessingSummary(
    val totalDrugs: Int,
    val successfulDrugs: Int,
    val failedDrugs: Int,
    val totalProcessingTime: Long,
    val averageProcessingTime: Long
)

// ============== SESSION SUMMARY MODELS ==============

@Serializable
data class SessionSummary(
    val sessionId: String,
    val sessionName: String,
    val totalDrugs: Int,
    val verifiedDrugs: Int,
    val processedDrugs: Int,
    val failedDrugs: Int,
    val totalProcessingTime: Long,
    val scanningTime: Long,
    val verificationTime: Long,
    val batchProcessingTime: Long,
    val averageConfidence: Float,
    val successRate: Float,
    val windowsIntegration: Boolean,
    val qualityMetrics: SessionQualityMetrics = SessionQualityMetrics(),
    val performanceMetrics: SessionPerformanceMetrics = SessionPerformanceMetrics(),
    val completedAt: Long = System.currentTimeMillis()
)

@Serializable
data class SessionQualityMetrics(
    val overallQuality: Float = 0f,
    val ocrAccuracy: Float = 0f,
    val visualMatchAccuracy: Float = 0f,
    val recoverySuccessRate: Float = 0f,
    val verificationAccuracy: Float = 0f,
    val batchProcessingAccuracy: Float = 0f,
    val qualityDistribution: Map<String, Int> = emptyMap()
)

@Serializable
data class SessionPerformanceMetrics(
    val drugsPerMinute: Float = 0f,
    val averageProcessingTimePerDrug: Long = 0L,
    val memoryUsage: Long = 0L,
    val cpuUsage: Float = 0f,
    val networkUsage: Long = 0L,
    val diskUsage: Long = 0L,
    val batteryImpact: Float = 0f
)

@Serializable
data class SessionMetrics(
    val totalSessions: Int = 0,
    val activeSessions: Int = 0,
    val completedSessions: Int = 0,
    val totalDrugsProcessed: Int = 0,
    val averageProcessingTime: Long = 0L,
    val averageSuccessRate: Float = 0f,
    val averageDrugsPerSession: Float = 0f,
    val totalSessionTime: Long = 0L,
    val systemPerformance: SystemPerformanceMetrics = SystemPerformanceMetrics()
)

@Serializable
data class SystemPerformanceMetrics(
    val averageMemoryUsage: Long = 0L,
    val peakMemoryUsage: Long = 0L,
    val averageCpuUsage: Float = 0f,
    val peakCpuUsage: Float = 0f,
    val averageBatteryDrain: Float = 0f,
    val crashCount: Int = 0,
    val errorRate: Float = 0f
)

data class SessionExportData(
    val session: MultiDrugSession,
    val scanningResults: List<MultiDrug>,
    val verificationResults: List<MultiDrug>,
    val batchResults: List<CompletedDrugProcessing>,
    val sessionSummary: SessionSummary?,
    val exportTimestamp: Long = System.currentTimeMillis(),
    val exportFormat: SessionExportFormat = SessionExportFormat.COMPREHENSIVE
)

// ============== MULTI-DRUG CORE MODELS ==============

data class MultiDrug(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val confidence: Float,
    val boundingBox: android.graphics.Rect? = null,
    val croppedImage: Bitmap? = null,
    val ocrText: String = "",
    val matchedDrugInfo: TurkishDrugInfo? = null,
    val visualMatch: Boolean = false,
    val isVerified: Boolean = false,
    val correctedName: String? = null,
    val processingTimeMs: Long = 0L,
    val source: ImageSource = ImageSource.CAMERA,
    val timestamp: Long = System.currentTimeMillis(),
    val enhancedResults: List<EnhancedDrugResult> = emptyList(),
    val visualRecovery: DamagedTextRecoveryResult? = null,
    val batchIntegration: BatchIntegrationResult? = null,
    val drugCount: Int = 1,
    val averageConfidence: Float = confidence,
    val processingTime: Long = processingTimeMs,
    val drugNames: List<String> = listOf(name),
    val success: Boolean = confidence > 0.6f
) {
    val finalDrugName: String get() = correctedName ?: name
    val finalConfidence: Float get() = if (isVerified) 1.0f else confidence
    val enhancementMethod: EnhancementMethod get() = when {
        visualRecovery != null -> EnhancementMethod.VISUAL_RECOVERY
        visualMatch -> EnhancementMethod.VISUAL_MATCH
        else -> EnhancementMethod.OCR_ONLY
    }
    val originalRegion: String get() = "$boundingBox"
    val drugDatabaseMatch: TurkishDrugInfo? get() = matchedDrugInfo
}







enum class ImageSource {
    CAMERA,
    GALLERY,
    FILE_IMPORT,
    BATCH_SCAN
}

// ============== VISUAL DETECTION MODELS ==============

data class LiveDetectionResult(
    val detectedBoxes: List<DetectedDrugBox> = emptyList(),
    val recognizedDrugs: List<RecognizedDrug> = emptyList(),
    val averageConfidence: Float = 0f,
    val currentFPS: Int = 0,
    val detectionTimestamp: Long = System.currentTimeMillis(),
    val processingTime: Long = 0L
)

data class DetectedDrugBox(
    val boundingBox: android.graphics.Rect,
    val confidence: Float,
    val detectedName: String = "",
    val croppedImage: Bitmap? = null,
    val features: List<VisualFeature> = emptyList()
)

data class RecognizedDrug(
    val name: String,
    val confidence: Float,
    val boundingBox: android.graphics.Rect,
    val matchedInfo: TurkishDrugInfo? = null,
    val visualMatch: Boolean = false,
    val recoveryUsed: Boolean = false
)

data class VisualFeature(
    val type: FeatureType,
    val value: FloatArray,
    val confidence: Float
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as VisualFeature
        if (type != other.type) return false
        if (!value.contentEquals(other.value)) return false
        if (confidence != other.confidence) return false
        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + value.contentHashCode()
        result = 31 * result + confidence.hashCode()
        return result
    }
}

enum class FeatureType {
    SIFT,
    COLOR_HISTOGRAM,
    TEXT_LAYOUT,
    EDGE_FEATURES,
    SHAPE_FEATURES,
    TEXTURE_FEATURES
}

enum class VisualFeatureType {
    SIFT,
    COLOR_HISTOGRAM,
    TEXT_LAYOUT,
    EDGE_FEATURES,
    SHAPE_FEATURES,
    TEXTURE_FEATURES
}

data class FeatureData(
    val type: VisualFeatureType,
    val data: FloatArray,
    val confidence: Float
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as FeatureData
        if (type != other.type) return false
        if (!data.contentEquals(other.data)) return false
        if (confidence != other.confidence) return false
        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + data.contentHashCode()
        result = 31 * result + confidence.hashCode()
        return result
    }
}

// ============== PROCESSING STATUS MODELS ==============

data class MultiDrugProcessingStatus(
    val isProcessing: Boolean = false,
    val currentStage: String = "",
    val progress: Float = 0f,
    val drugProcessingStates: List<DrugProcessingState> = emptyList(),
    val estimatedTimeRemaining: Long = 0L,
    val processingStartTime: Long = 0L,
    val totalDrugs: Int = 0,
    val completedDrugs: Int = 0,
    val failedDrugs: Int = 0
)

data class DrugProcessingState(
    val drugName: String,
    val status: String,
    val confidence: Int = 0,
    val processingTime: Long = 0L,
    val error: String? = null
)

// ============== DAMAGED TEXT RECOVERY MODELS ==============

data class DamagedTextRecoveryResult(
    val originalText: String,
    val recoveredText: String,
    val confidence: Float,
    val method: RecoveryMethod,
    val isRetried: Boolean = false
)

enum class RecoveryMethod {
    VISUAL_ENHANCEMENT,
    PATTERN_MATCHING,
    ML_RECONSTRUCTION,
    MANUAL_CORRECTION
}

// ============== BATCH INTEGRATION MODELS ==============

data class BatchIntegrationResult(
    val sessionId: String,
    val totalItems: Int,
    val processedItems: Int,
    val failedItems: Int,
    val processingTime: Long
)

// ============== DRUG DATABASE MODELS ==============

data class TurkishDrugInfo(
    val id: String,
    val name: String,
    val brandName: String?,
    val genericName: String?,
    val manufacturer: String?,
    val activeIngredients: List<String>,
    val dosageForm: String?,
    val strength: String?,
    val packageSize: String?,
    val barcode: String?,
    val atcCode: String?,
    val prescriptionRequired: Boolean,
    val price: Double?,
    val reimbursable: Boolean,
    val category: String?,
    val indications: List<String>,
    val contraindications: List<String>,
    val sideEffects: List<String>,
    val interactions: List<String>
)

// ============== CAMERA AND SCANNER MODELS ==============

enum class FrameQuality {
    EXCELLENT,
    GOOD,
    AVERAGE,
    POOR,
    UNACCEPTABLE
}

data class MultiDrugScannerState(
    val isScanning: Boolean = false,
    val detectedDrugs: List<DetectedDrugBox> = emptyList(),
    val currentFrameQuality: FrameQuality = FrameQuality.AVERAGE,
    val message: String = ""
)

// ============== DRUG BOX CONDITIONS ==============

enum class DrugBoxCondition {
    GOOD,
    WORN,
    DAMAGED,
    SEVERELY_DAMAGED,
    UNKNOWN
}

// ============== VOICE COMMAND MODELS ==============

data class VoiceCommandResult(
    val command: String,
    val confidence: Float,
    val action: VoiceAction,
    val parameters: Map<String, String> = emptyMap()
)

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

data class VoiceSettings(
    val isEnabled: Boolean = true,
    val language: String = "en-US",
    val sensitivity: Float = 0.7f,
    val feedbackEnabled: Boolean = true,
    val wakeWordEnabled: Boolean = false,
    val customCommands: Map<String, VoiceAction> = emptyMap()
)

// ============== EXPORT MODELS ==============

data class ExportConfig(
    val format: ExportFormat,
    val includePatientInfo: Boolean = true,
    val includeTimestamps: Boolean = true,
    val includeStatistics: Boolean = false,
    val dateRange: DateRange? = null
)

data class DateRange(
    val startDate: Long,
    val endDate: Long
)

// ============== ENHANCED MATCHING MODELS ==============

data class EnhancedMatchResult(
    val originalText: String,
    val bestMatch: EnhancedMatch?,
    val alternativeMatches: List<EnhancedMatch>,
    val processingTime: Long,
    val algorithmsUsed: List<String>
)

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

data class UsageAnalytics(
    val sessionCount: Int = 0,
    val totalScans: Int = 0,
    val averageSessionDuration: Long = 0,
    val mostUsedFeatures: Map<String, Int> = emptyMap(),
    val errorFrequency: Map<String, Int> = emptyMap(),
    val performanceMetrics: PerformanceMetrics = PerformanceMetrics()
)

data class PerformanceMetrics(
    val averageOCRTime: Long = 0,
    val averageMatchingTime: Long = 0,
    val cameraInitTime: Long = 0,
    val memoryUsage: Long = 0,
    val networkLatency: Long = 0
)

// ============== UI STATE MODELS ==============

data class AppState(
    val isOnline: Boolean = true,
    val isDatabaseLoaded: Boolean = false,
    val activeSessionId: String? = null,
    val lastBackupTime: Long = 0,
    val pendingUploads: Int = 0,
    val systemHealth: SystemHealth = SystemHealth()
)

data class SystemHealth(
    val cameraStatus: ComponentStatus = ComponentStatus.UNKNOWN,
    val networkStatus: ComponentStatus = ComponentStatus.UNKNOWN,
    val storageStatus: ComponentStatus = ComponentStatus.UNKNOWN,
    val apiStatus: ComponentStatus = ComponentStatus.UNKNOWN
)

enum class ComponentStatus {
    HEALTHY, WARNING, ERROR, UNKNOWN
}

// ============== PROGRESS AND ERROR MODELS ==============

data class ProcessingProgress(
    val currentStep: String,
    val stepProgress: Float,
    val overallProgress: Float,
    val estimatedTimeRemaining: Long,
    val currentDrugName: String? = null,
    val currentDrugIndex: Int = 0,
    val totalDrugs: Int = 0
)

data class ErrorDetails(
    val errorCode: String,
    val errorMessage: String,
    val timestamp: Long = System.currentTimeMillis(),
    val stackTrace: String? = null,
    val context: Map<String, Any> = emptyMap()
)

data class PerformanceReport(
    val memoryUsage: Long,
    val cpuUsage: Float,
    val processingSpeed: Float,
    val errorRate: Float,
    val averageResponseTime: Long,
    val timestamp: Long = System.currentTimeMillis()
)

// ============== TYPE CONVERTERS ==============

class MultiDrugSessionConverters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromPatientInfo(patientInfo: PatientInfo?): String? {
        return patientInfo?.let { json.encodeToString(PatientInfo.serializer(), it) }
    }

    @TypeConverter
    fun toPatientInfo(patientInfoString: String?): PatientInfo? {
        return patientInfoString?.let { json.decodeFromString(PatientInfo.serializer(), it) }
    }

    @TypeConverter
    fun fromMultiDrugSessionConfig(config: MultiDrugSessionConfig): String {
        return json.encodeToString(MultiDrugSessionConfig.serializer(), config)
    }

    @TypeConverter
    fun toMultiDrugSessionConfig(configString: String): MultiDrugSessionConfig {
        return json.decodeFromString(MultiDrugSessionConfig.serializer(), configString)
    }

    @TypeConverter
    fun fromScanningPhase(scanningPhase: ScanningPhase?): String? {
        return scanningPhase?.let { json.encodeToString(ScanningPhase.serializer(), it) }
    }

    @TypeConverter
    fun toScanningPhase(scanningPhaseString: String?): ScanningPhase? {
        return scanningPhaseString?.let { json.decodeFromString(ScanningPhase.serializer(), it) }
    }

    @TypeConverter
    fun fromVerificationPhase(verificationPhase: VerificationPhase?): String? {
        return verificationPhase?.let { json.encodeToString(VerificationPhase.serializer(), it) }
    }

    @TypeConverter
    fun toVerificationPhase(verificationPhaseString: String?): VerificationPhase? {
        return verificationPhaseString?.let { json.decodeFromString(VerificationPhase.serializer(), it) }
    }

    @TypeConverter
    fun fromBatchProcessingPhase(batchProcessingPhase: BatchProcessingPhase?): String? {
        return batchProcessingPhase?.let { json.encodeToString(BatchProcessingPhase.serializer(), it) }
    }

    @TypeConverter
    fun toBatchProcessingPhase(batchProcessingPhaseString: String?): BatchProcessingPhase? {
        return batchProcessingPhaseString?.let { json.decodeFromString(BatchProcessingPhase.serializer(), it) }
    }

    @TypeConverter
    fun fromSessionSummary(sessionSummary: SessionSummary?): String? {
        return sessionSummary?.let { json.encodeToString(SessionSummary.serializer(), it) }
    }

    @TypeConverter
    fun toSessionSummary(sessionSummaryString: String?): SessionSummary? {
        return sessionSummaryString?.let { json.decodeFromString(SessionSummary.serializer(), it) }
    }

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return json.encodeToString(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return try {
            json.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromStringIntMap(value: Map<String, Int>): String {
        return json.encodeToString(value)
    }

    @TypeConverter
    fun toStringIntMap(value: String): Map<String, Int> {
        return try {
            json.decodeFromString(value)
        } catch (e: Exception) {
            emptyMap()
        }
    }
}

// ============== VALIDATION EXTENSIONS ==============

fun MultiDrugSession.isValidForScanning(): Boolean {
    return status == SessionStatus.CREATED
}

fun MultiDrugSession.isValidForVerification(): Boolean {
    return status == SessionStatus.SCANNING && 
           scanningPhase?.status == ScanningStatus.COMPLETED &&
           scanningPhase.results.isNotEmpty()
}

fun MultiDrugSession.isValidForBatchProcessing(): Boolean {
    return status == SessionStatus.VERIFICATION && 
           verificationPhase?.status == VerificationStatus.COMPLETED &&
           verificationPhase.results.any { it.isVerified }
}

fun MultiDrugSession.getProgressPercentage(): Float {
    return when (status) {
        SessionStatus.CREATED -> 0f
        SessionStatus.SCANNING -> 25f
        SessionStatus.VERIFICATION -> 50f
        SessionStatus.BATCH_PROCESSING -> 
            75f + (batchProcessingPhase?.progress?.times(25f) ?: 0f)
        SessionStatus.COMPLETED -> 100f
        else -> 0f
    }
}

fun SessionSummary.getQualityGrade(): String {
    val overallScore = (successRate + averageConfidence + qualityMetrics.overallQuality) / 3
    return when {
        overallScore >= 0.9f -> "A"
        overallScore >= 0.8f -> "B"
        overallScore >= 0.7f -> "C"
        overallScore >= 0.6f -> "D"
        else -> "F"
    }
}

fun BatchProcessingPhase.isReadyForProcessing(): Boolean {
    return status == BatchProcessingStatus.READY && 
           verifiedResults.isNotEmpty() &&
           batchSessionId != null
}

// Missing classes for AI integration
@Serializable
data class MedicalAnalysisResult(
    val analysisId: String,
    val confidence: Float,
    val findings: List<String>,
    val recommendations: List<String>,
    val severity: String,
    val processingTimeMs: Long,
    val metadata: Map<String, String> = emptyMap()
)

@Serializable
data class CustomAIConfiguration(
    val configId: String,
    val name: String,
    val provider: String,
    val apiKey: String = "",
    val baseUrl: String = "",
    val model: String = "",
    val parameters: Map<String, String> = emptyMap(),
    val isEnabled: Boolean = true,
    val priority: Int = 0
)

@Serializable
data class LocalAIModel(
    val modelId: String,
    val name: String,
    val filePath: String,
    val modelType: String,
    val version: String,
    val size: Long,
    val isLoaded: Boolean = false,
    val capabilities: List<String> = emptyList(),
    val metadata: Map<String, String> = emptyMap()
)

@Serializable
data class PatientContext(
    val patientId: String,
    val name: String,
    val age: Int?,
    val medicalHistory: List<String> = emptyList(),
    val currentMedications: List<String> = emptyList(),
    val allergies: List<String> = emptyList(),
    val conditions: List<String> = emptyList(),
    val notes: String = ""
)

enum class VisualFeatureType {
    SIFT_FEATURES,
    COLOR_HISTOGRAM,
    TEXT_LAYOUT,
    EDGE_FEATURES,
    SHAPE_FEATURES,
    TEXTURE_FEATURES
}