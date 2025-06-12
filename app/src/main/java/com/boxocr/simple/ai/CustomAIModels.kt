package com.boxocr.simple.ai

import android.bluetooth.BluetoothGatt
import kotlinx.serialization.Serializable
import java.util.*

/**
 * 🔧 DATA MODELS FOR CUSTOM AI INTEGRATION
 * 
 * Comprehensive data models for the custom and local AI integration system:
 * - Custom AI Provider configurations
 * - Local AI model definitions
 * - Inference results and analytics
 * - Turkish medical AI marketplace models
 * - Private cloud AI configurations
 * 
 * Enterprise-grade data models with full serialization support
 */

// ===== CUSTOM AI PROVIDER MODELS =====

@Serializable
data class CustomAIConfiguration(
    val id: String,
    val name: String,
    val description: String,
    val providerType: AIProviderType,
    val endpoint: String,
    val apiKey: String,
    val headers: Map<String, String>,
    val requestFormat: RequestFormat,
    val responseFormat: ResponseFormat,
    val modelParameters: Map<String, String>,
    val turkishMedicalOptimized: Boolean,
    val maxTokens: Int,
    val timeout: Long,
    val isActive: Boolean,
    val createdAt: Long,
    val lastTested: Long
)

@Serializable
data class CustomAIProviderConfig(
    val name: String,
    val description: String,
    val endpoint: String,
    val apiKey: String,
    val customHeaders: Map<String, String> = emptyMap(),
    val requestFormat: RequestFormat = RequestFormat.GENERIC_JSON,
    val responseFormat: ResponseFormat = ResponseFormat.GENERIC_JSON,
    val modelParameters: Map<String, String> = emptyMap(),
    val turkishMedicalOptimized: Boolean = false,
    val maxTokens: Int = 4096,
    val timeout: Long = 60L
)

@Serializable
data class CustomAIResponse(
    val content: String,
    val confidence: Float,
    val executionTime: Long,
    val tokens: Int,
    val model: String
)

@Serializable
data class AIProviderStatus(
    val providerId: String,
    val providerName: String,
    val isOnline: Boolean,
    val lastChecked: Long,
    val responseTime: Long,
    val errorCount: Int,
    val successRate: Float
)

// ===== LOCAL AI MODEL MODELS =====

@Serializable
data class LocalAIModel(
    val id: String,
    val name: String,
    val description: String,
    val modelType: LocalModelType,
    val filePath: String,
    val fileSize: Long,
    val version: String,
    val turkishMedicalSpecialized: Boolean,
    val inputShape: List<Int>,
    val outputShape: List<Int>,
    val preprocessingConfig: Map<String, String>,
    val postprocessingConfig: Map<String, String>,
    val performanceMetrics: LocalModelPerformance,
    val isLoaded: Boolean,
    val loadedAt: Long,
    val lastUsed: Long
)

@Serializable
data class LocalAIModelConfig(
    val name: String,
    val description: String,
    val modelType: LocalModelType,
    val version: String,
    val turkishMedicalSpecialized: Boolean,
    val inputShape: List<Int>,
    val outputShape: List<Int>,
    val preprocessingConfig: Map<String, String> = emptyMap(),
    val postprocessingConfig: Map<String, String> = emptyMap()
)

@Serializable
data class LocalModelPerformance(
    val averageInferenceTime: Long = 0L,
    val memoryUsage: Long = 0L,
    val accuracy: Float = 0f,
    val totalInferences: Int = 0,
    val successfulInferences: Int = 0,
    val lastPerformanceUpdate: Long = System.currentTimeMillis()
)

@Serializable
data class LocalInferenceResult(
    val modelId: String,
    val modelName: String,
    val inputSize: Int,
    val outputData: ByteArray,
    val executionTime: Long,
    val memoryUsage: Long,
    val confidence: Float,
    val turkishMedicalContext: Boolean
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LocalInferenceResult

        if (modelId != other.modelId) return false
        if (modelName != other.modelName) return false
        if (inputSize != other.inputSize) return false
        if (!outputData.contentEquals(other.outputData)) return false
        if (executionTime != other.executionTime) return false
        if (memoryUsage != other.memoryUsage) return false
        if (confidence != other.confidence) return false
        if (turkishMedicalContext != other.turkishMedicalContext) return false

        return true
    }

    override fun hashCode(): Int {
        var result = modelId.hashCode()
        result = 31 * result + modelName.hashCode()
        result = 31 * result + inputSize
        result = 31 * result + outputData.contentHashCode()
        result = 31 * result + executionTime.hashCode()
        result = 31 * result + memoryUsage.hashCode()
        result = 31 * result + confidence.hashCode()
        result = 31 * result + turkishMedicalContext.hashCode()
        return result
    }
}

// ===== CONSENSUS AND MULTI-MODEL RESULTS =====

@Serializable
data class ModelResult(
    val modelId: String,
    val modelName: String,
    val response: String,
    val confidence: Float,
    val executionTime: Long,
    val modelType: ModelInferenceType
)

@Serializable
data class AIConsensusResult(
    val consensusResponse: String,
    val averageConfidence: Float,
    val modelResults: List<ModelResult>,
    val consensusReached: Boolean,
    val totalExecutionTime: Long
)

@Serializable
data class ModelInferenceResult(
    val id: String,
    val configurationId: String,
    val modelName: String,
    val input: String,
    val output: String,
    val executionTime: Long,
    val modelType: ModelInferenceType,
    val timestamp: Long
)

// ===== TURKISH MEDICAL MODEL MARKETPLACE =====

@Serializable
data class TurkishMedicalModelInfo(
    val id: String,
    val name: String,
    val description: String,
    val category: TurkishMedicalCategory,
    val modelType: LocalModelType,
    val version: String,
    val fileSize: Long,
    val downloadUrl: String,
    val checksum: String,
    val inputShape: List<Int>,
    val outputShape: List<Int>,
    val preprocessingConfig: Map<String, String>,
    val postprocessingConfig: Map<String, String>,
    val accuracy: Float,
    val medicalValidation: TurkishMedicalValidation,
    val publisher: String,
    val publishedAt: Long,
    val downloadCount: Int,
    val rating: Float,
    val turkishHealthMinistryApproved: Boolean
)

@Serializable
data class TurkishMedicalValidation(
    val validationType: String,
    val validationAuthority: String,
    val validationDate: Long,
    val certificateNumber: String,
    val expiryDate: Long,
    val validationDetails: String
)

// ===== PRIVATE CLOUD AI INTEGRATION =====

@Serializable
data class PrivateCloudAIConfig(
    val providerName: String,
    val cloudProvider: CloudProvider,
    val endpoint: String,
    val apiKey: String,
    val authHeaders: Map<String, String>,
    val requestFormat: RequestFormat,
    val responseFormat: ResponseFormat,
    val modelParameters: Map<String, String>,
    val turkishMedicalOptimized: Boolean,
    val maxTokens: Int,
    val timeout: Long,
    val encryptionEnabled: Boolean,
    val complianceLevel: ComplianceLevel
)

// ===== IOT INTEGRATION DATA MODELS (Additional) =====

@Serializable
data class WeightScaleDevice(
    val id: String,
    val name: String,
    val manufacturer: String,
    val model: String,
    val maxWeight: Float,
    val bodyCompositionSupport: Boolean,
    val connection: BluetoothGatt?
)

@Serializable
data class VitalSignsSession(
    val sessionId: String,
    val patientId: String?,
    val deviceIds: List<String>,
    val startTime: Long,
    val isActive: Boolean,
    val readings: MutableList<MedicalReading>
)

@Serializable
data class SmartPharmacyConfig(
    val pharmacyId: String,
    val pharmacyName: String,
    val endpoint: String,
    val apiKey: String,
    val sgkEnabled: Boolean,
    val medulaEnabled: Boolean,
    val capabilities: List<String>
)

@Serializable
data class SmartPharmacyConnection(
    val pharmacyId: String,
    val pharmacyName: String,
    val endpoint: String,
    val sgkIntegration: Boolean,
    val medulaIntegration: Boolean,
    val dispensingCapabilities: List<String>
)

@Serializable
data class RemoteMonitoringConfig(
    val doctorId: String,
    val hospitalId: String,
    val requiredDevices: List<MedicalDeviceType>,
    val monitoringPlan: String
)

@Serializable
data class RemotePatientSession(
    val sessionId: String,
    val patientId: String,
    val doctorId: String,
    val hospitalId: String,
    val deviceTypes: List<MedicalDeviceType>,
    val monitoringPlan: String,
    val turkishTelemedicineCompliance: Boolean
)

@Serializable
data class DeviceStatus(
    val deviceId: String,
    val isConnected: Boolean,
    val batteryLevel: Int?,
    val signalStrength: Int?,
    val lastReading: MedicalReading?,
    val calibrationStatus: String,
    val turkishComplianceStatus: TurkishMedicalCertification
)

// ===== ENUMS AND CLASSIFICATIONS =====

enum class AIProviderType {
    OPENAI,
    ANTHROPIC,
    GOOGLE,
    MICROSOFT,
    CUSTOM,
    LOCAL,
    PRIVATE_CLOUD
}

enum class LocalModelType {
    TENSORFLOW_LITE,
    ONNX,
    PYTORCH_MOBILE,
    CUSTOM_BINARY,
    OPENVINO,
    NCNN
}

enum class RequestFormat {
    OPENAI_CHAT,
    ANTHROPIC_MESSAGES,
    GOOGLE_PALM,
    GENERIC_JSON,
    CUSTOM,
    REST_API,
    GRAPHQL
}

enum class ResponseFormat {
    OPENAI_CHAT,
    ANTHROPIC_MESSAGES,
    GOOGLE_PALM,
    GENERIC_JSON,
    CUSTOM,
    XML,
    PLAIN_TEXT
}

enum class ModelInferenceType {
    CUSTOM_REMOTE,
    LOCAL,
    CONSENSUS,
    HYBRID,
    CLOUD_PRIVATE,
    FEDERATED
}

enum class CloudProvider {
    AWS,
    AZURE,
    GOOGLE_CLOUD,
    IBM_WATSON,
    ORACLE_CLOUD,
    ALIBABA_CLOUD,
    CUSTOM_PRIVATE
}

enum class ComplianceLevel {
    BASIC,
    HIPAA,
    GDPR,
    KVKV_TURKISH,
    SOC2,
    ISO27001,
    MEDICAL_GRADE
}

enum class TurkishMedicalCategory {
    CARDIOLOGY("Kardiyoloji"),
    DIABETES("Diyabet"),
    HYPERTENSION("Hipertansiyon"),
    RESPIRATORY("Solunum Sistemi"),
    NEUROLOGY("Nöroloji"),
    ONCOLOGY("Onkoloji"),
    PEDIATRICS("Çocuk Hastalıkları"),
    GERIATRICS("Geriatri"),
    EMERGENCY("Acil Tıp"),
    PHARMACY("Eczane"),
    RADIOLOGY("Radyoloji"),
    LABORATORY("Laboratuvar"),
    SURGERY("Cerrahi"),
    PSYCHIATRY("Psikiyatri"),
    DERMATOLOGY("Dermatoloji"),
    OPHTHALMOLOGY("Göz Hastalıkları"),
    ENT("Kulak Burun Boğaz"),
    UROLOGY("Üroloji"),
    GYNECOLOGY("Kadın Hastalıkları"),
    ORTHOPEDICS("Ortopedi");

    constructor(displayName: String) {
        this.displayName = displayName
    }

    val displayName: String
}

// ===== ADVANCED AI MODELS DATA CLASSES =====

@Serializable
data class ClinicalCase(
    val patientContext: PatientContext,
    val symptoms: List<String>,
    val currentMedications: List<String>,
    val medicalHistory: String?,
    val primaryConcern: String,
    val medicalField: String
)

@Serializable
data class PatientContext(
    val profile: PatientProfile,
    val symptoms: List<String>,
    val urgencyLevel: UrgencyLevel
)

@Serializable
data class PatientProfile(
    val age: Int,
    val gender: String,
    val weight: Float,
    val allergies: List<String>,
    val chronicConditions: List<String>
)

@Serializable
data class MedicalAnalysisResult(
    val diagnosis: String,
    val treatment: String,
    val interactions: String,
    val followUp: String,
    val summary: String,
    val confidence: Float
)

@Serializable
data class DrugInteractionAnalysis(
    val interactions: List<String>,
    val severity: String,
    val recommendations: List<String>,
    val monitoring: String,
    val summary: String,
    val riskLevel: RiskLevel
)

@Serializable
data class MedicalResearchResult(
    val findings: List<String>,
    val guidelines: List<String>,
    val evidenceLevel: EvidenceLevel,
    val recommendations: List<String>,
    val sources: List<String>,
    val summary: String
)

@Serializable
data class TurkishMedicalKnowledge(
    val explanation: String,
    val guidelines: List<String>,
    val sgkCoverage: String,
    val medulaInfo: String,
    val culturalFactors: List<String>,
    val reliability: Float
)

@Serializable
data class MedicalConsensus(
    val recommendation: String,
    val warnings: List<String>,
    val consensusConfidence: Float,
    val modelCount: Int,
    val criticalAlerts: List<String>
)

@Serializable
data class MedicalConsultation(
    val id: String,
    val type: String,
    val timestamp: Long,
    val input: String,
    val output: String,
    val confidence: Float,
    val modelVersion: String
)

@Serializable
data class RiskLevel(
    val level: String,
    val confidence: Float
) {
    companion object {
        fun fromContent(content: String): RiskLevel {
            return RiskLevel("Moderate", 0.7f) // Placeholder implementation
        }
    }
}

enum class UrgencyLevel {
    LOW, MODERATE, HIGH, CRITICAL
}

enum class EvidenceLevel(val confidence: Float) {
    LOW(0.3f),
    MODERATE(0.6f),
    HIGH(0.8f),
    VERY_HIGH(0.95f)
}

// ===== API REQUEST/RESPONSE MODELS =====

@Serializable
data class GPTRequest(
    val model: String,
    val messages: List<GPTMessage>,
    val maxTokens: Int,
    val temperature: Float
)

@Serializable
data class GPTMessage(
    val role: String,
    val content: String
)

@Serializable
data class GPTResponse(
    val choices: List<GPTChoice>
)

@Serializable
data class GPTChoice(
    val message: GPTMessage
)

@Serializable
data class ClaudeRequest(
    val model: String,
    val maxTokens: Int,
    val messages: List<ClaudeMessage>,
    val systemPrompt: String
)

@Serializable
data class ClaudeMessage(
    val role: String,
    val content: String
)

@Serializable
data class ClaudeResponse(
    val content: List<ClaudeContentBlock>
)

@Serializable
data class ClaudeContentBlock(
    val text: String
)

@Serializable
data class GeminiRequest(
    val model: String,
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig
)

@Serializable
data class GeminiContent(
    val parts: List<GeminiPart>
)

@Serializable
data class GeminiPart(
    val text: String
)

@Serializable
data class GeminiGenerationConfig(
    val maxOutputTokens: Int,
    val temperature: Float
)

@Serializable
data class GeminiResponse(
    val candidates: List<GeminiCandidate>
)

@Serializable
data class GeminiCandidate(
    val content: GeminiContent
)

// ===== UTILITY DATA CLASSES =====

@Serializable
data class AIModelCapabilities(
    val textGeneration: Boolean = false,
    val imageAnalysis: Boolean = false,
    val speechRecognition: Boolean = false,
    val turkishLanguageSupport: Boolean = false,
    val medicalKnowledge: Boolean = false,
    val drugInteractionAnalysis: Boolean = false,
    val realTimeInference: Boolean = false,
    val batchProcessing: Boolean = false,
    val customFineTuning: Boolean = false,
    val privacyCompliant: Boolean = false
)

@Serializable
data class AIModelLimitations(
    val maxTokensPerRequest: Int = 4096,
    val requestsPerMinute: Int = 60,
    val requestsPerDay: Int = 1000,
    val maxFileSize: Long = 10 * 1024 * 1024, // 10MB
    val supportedLanguages: List<String> = listOf("en", "tr"),
    val requiresInternet: Boolean = true,
    val requiresApiKey: Boolean = true,
    val dataRetention: String = "No data retention"
)

@Serializable
data class AIModelPricing(
    val costPerToken: Float = 0.0f,
    val costPerRequest: Float = 0.0f,
    val costPerMonth: Float = 0.0f,
    val freeTokensPerMonth: Int = 0,
    val currency: String = "USD",
    val turkishLiraEquivalent: Float = 0.0f
)

@Serializable
data class CustomAIProviderMetadata(
    val providerId: String,
    val providerName: String,
    val version: String,
    val lastUpdated: Long,
    val capabilities: AIModelCapabilities,
    val limitations: AIModelLimitations,
    val pricing: AIModelPricing,
    val turkishMedicalCertification: TurkishMedicalCertification,
    val supportContact: String,
    val documentation: String,
    val termsOfService: String,
    val privacyPolicy: String
)

@Serializable
data class LocalAIModelMetadata(
    val modelId: String,
    val modelName: String,
    val creator: String,
    val license: String,
    val trainingDataset: String,
    val accuracy: Float,
    val modelSize: Long,
    val inferenceSpeed: String,
    val hardwareRequirements: String,
    val turkishMedicalValidation: TurkishMedicalValidation?,
    val lastValidated: Long,
    val validationResults: Map<String, String>,
    val usageStatistics: LocalModelUsageStats
)

@Serializable
data class LocalModelUsageStats(
    val totalInferences: Long = 0L,
    val successfulInferences: Long = 0L,
    val averageInferenceTime: Long = 0L,
    val totalExecutionTime: Long = 0L,
    val lastUsed: Long = 0L,
    val popularUseCase: String = "Unknown",
    val userRating: Float = 0.0f,
    val performanceTrend: PerformanceTrend = PerformanceTrend.STABLE
)

enum class PerformanceTrend {
    IMPROVING,
    STABLE,
    DEGRADING,
    UNKNOWN
}

// ===== INTEGRATION STATUS AND MONITORING =====

@Serializable
data class AIIntegrationStatus(
    val totalCustomProviders: Int = 0,
    val activeCustomProviders: Int = 0,
    val totalLocalModels: Int = 0,
    val loadedLocalModels: Int = 0,
    val totalInferencesToday: Long = 0L,
    val averageResponseTime: Long = 0L,
    val systemHealth: SystemHealth = SystemHealth.UNKNOWN,
    val lastHealthCheck: Long = System.currentTimeMillis(),
    val turkishMedicalCompliance: Boolean = true,
    val storageUsed: Long = 0L,
    val availableStorage: Long = 0L
)

enum class SystemHealth {
    EXCELLENT,
    GOOD,
    FAIR,
    POOR,
    CRITICAL,
    UNKNOWN
}

@Serializable
data class AIPerformanceMetrics(
    val providerId: String,
    val providerName: String,
    val requestsToday: Long = 0L,
    val successfulRequests: Long = 0L,
    val failedRequests: Long = 0L,
    val averageResponseTime: Long = 0L,
    val p95ResponseTime: Long = 0L,
    val p99ResponseTime: Long = 0L,
    val errorRate: Float = 0.0f,
    val uptime: Float = 100.0f,
    val lastIncident: Long = 0L,
    val costToday: Float = 0.0f,
    val tokensUsedToday: Long = 0L
)

// ===== SECURITY AND COMPLIANCE =====

@Serializable
data class AISecurityConfig(
    val encryptionEnabled: Boolean = true,
    val apiKeyEncrypted: Boolean = true,
    val dataAnonymization: Boolean = true,
    val auditLogging: Boolean = true,
    val accessControl: AccessControlLevel = AccessControlLevel.STRICT,
    val dataRetentionDays: Int = 30,
    val gdprCompliant: Boolean = true,
    val kvkvCompliant: Boolean = true,
    val hipaaCompliant: Boolean = false,
    val medicalDataProtection: Boolean = true
)

enum class AccessControlLevel {
    NONE,
    BASIC,
    STANDARD,
    STRICT,
    ENTERPRISE
}

@Serializable
data class AIAuditLog(
    val id: String,
    val timestamp: Long,
    val userId: String?,
    val action: String,
    val resource: String,
    val success: Boolean,
    val details: String,
    val ipAddress: String?,
    val userAgent: String?,
    val riskLevel: String
)

// ===== ERROR HANDLING AND RESILIENCE =====

@Serializable
data class AIErrorInfo(
    val errorCode: String,
    val errorMessage: String,
    val errorType: AIErrorType,
    val timestamp: Long,
    val providerId: String?,
    val modelId: String?,
    val requestId: String?,
    val retryable: Boolean,
    val suggestedAction: String
)

enum class AIErrorType {
    NETWORK_ERROR,
    AUTHENTICATION_ERROR,
    RATE_LIMIT_ERROR,
    MODEL_ERROR,
    INPUT_ERROR,
    OUTPUT_ERROR,
    TIMEOUT_ERROR,
    QUOTA_EXCEEDED,
    MODEL_NOT_FOUND,
    CONFIGURATION_ERROR,
    SYSTEM_ERROR,
    UNKNOWN_ERROR
}

@Serializable
data class AIFallbackConfig(
    val primaryProviderId: String,
    val fallbackProviders: List<String>,
    val retryAttempts: Int = 3,
    val retryDelay: Long = 1000L,
    val circuitBreakerEnabled: Boolean = true,
    val failureThreshold: Int = 5,
    val recoveryTime: Long = 30000L,
    val timeoutMs: Long = 30000L
)