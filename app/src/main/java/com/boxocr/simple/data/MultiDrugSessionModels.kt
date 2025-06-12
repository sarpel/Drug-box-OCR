package com.boxocr.simple.data

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Enhanced Data Models for Multi-Drug Session Management
 * Comprehensive data structures for complete multi-drug workflow
 */

// Session Management Data Models

@Entity(tableName = "multi_drug_sessions")
@TypeConverters(MultiDrugSessionConverters::class)
data class MultiDrugSessionEntity(
    @PrimaryKey val sessionId: String,
    val sessionName: String,
    val status: String,
    val patientInfo: String?, // JSON serialized PatientInfo
    val config: String, // JSON serialized MultiDrugSessionConfig
    val scanningPhase: String?, // JSON serialized ScanningPhase
    val verificationPhase: String?, // JSON serialized VerificationPhase
    val batchProcessingPhase: String?, // JSON serialized BatchProcessingPhase
    val sessionSummary: String?, // JSON serialized SessionSummary
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
data class PatientInfo(
    val patientId: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val dateOfBirth: String = "",
    val nationalId: String = "",
    val phoneNumber: String = "",
    val address: String = "",
    val emergencyContact: String = "",
    val medicalNotes: String = "",
    val allergies: List<String> = emptyList(),
    val chronicConditions: List<String> = emptyList()
)

@Serializable
data class MultiDrugSessionConfig(
    val autoVerifyHighConfidence: Boolean = true,
    val confidenceThreshold: Float = 0.9f,
    val requireVisualMatch: Boolean = false,
    val enableDamagedTextRecovery: Boolean = true,
    val autoExport: Boolean = true,
    val exportFormat: ExportFormat = ExportFormat.COMPREHENSIVE,
    val windowsIntegration: Boolean = true,
    val batchProcessing: Boolean = true,
    val maxDrugsPerSession: Int = 50,
    val sessionTimeout: Long = 3600000L, // 1 hour
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

enum class ExportFormat {
    BASIC,
    DETAILED,
    COMPREHENSIVE,
    MEDICAL_REPORT
}

// Scanning Phase Data Models

@Serializable
data class ScanningPhase(
    val sessionId: String,
    val config: ScanningConfig = ScanningConfig(),
    val status: ScanningStatus = ScanningStatus.ACTIVE,
    val results: List<MultiDrugResult> = emptyList(),
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
    val processingSpeed: Float = 0f, // drugs per minute
    val errorRate: Float = 0f
)

// Verification Phase Data Models

@Serializable
data class VerificationPhase(
    val sessionId: String,
    val results: List<MultiDrugResult> = emptyList(),
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
    val confidenceDistribution: Map<String, Int> = emptyMap(), // High, Medium, Low
    val verificationAccuracy: Float = 0f
)

// Batch Processing Phase Data Models

@Serializable
data class BatchProcessingPhase(
    val sessionId: String,
    val verifiedResults: List<MultiDrugResult> = emptyList(),
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

// Session Summary and Export Data Models

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
    val qualityDistribution: Map<String, Int> = emptyMap() // Excellent, Good, Fair, Poor
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
    val scanningResults: List<MultiDrugResult>,
    val verificationResults: List<MultiDrugResult>,
    val batchResults: List<CompletedDrugProcessing>,
    val sessionSummary: SessionSummary?,
    val exportTimestamp: Long = System.currentTimeMillis(),
    val exportFormat: ExportFormat = ExportFormat.COMPREHENSIVE
)

// Live Detection Data Models

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

// Processing Status Data Models

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
    val status: String, // "waiting", "processing", "completed", "error"
    val confidence: Int = 0,
    val processingTime: Long = 0L,
    val error: String? = null
)

// Type Converters for Room Database

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

// Additional Helper Data Classes

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

// Validation and Business Logic Extensions

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
