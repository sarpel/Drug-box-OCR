package com.boxocr.simple.ai

import kotlinx.serialization.Serializable

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

@Serializable
data class TurkishMedicalCertification(
    val isValid: Boolean,
    val certificateNumber: String,
    val issuedBy: String,
    val validUntil: Long
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

// ===== IOT INTEGRATION DATA MODELS =====

@Serializable
data class MedicalReading(
    val deviceId: String,
    val readingType: String,
    val value: Float,
    val unit: String,
    val timestamp: Long
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
    CARDIOLOGY,
    NEUROLOGY,
    ENDOCRINOLOGY,
    GASTROENTEROLOGY,
    PULMONOLOGY,
    NEPHROLOGY,
    RHEUMATOLOGY,
    ONCOLOGY,
    PSYCHIATRY,
    DERMATOLOGY,
    OPHTHALMOLOGY,
    OTOLARYNGOLOGY,
    GENERAL,
    ANTIBIOTICS,
    VITAMINS
}

enum class MedicalDeviceType {
    BLOOD_PRESSURE_MONITOR,
    GLUCOSE_METER,
    HEART_RATE_MONITOR,
    OXIMETER,
    THERMOMETER,
    WEIGHT_SCALE
}

// ===== SIMPLIFIED MEDICAL MODELS =====

@Serializable
data class ClinicalCase(
    val id: String,
    val description: String,
    val symptoms: List<String>,
    val patientContext: PatientContext
)

@Serializable
data class PatientContext(
    val symptoms: String,
    val duration: String,
    val severity: String
)

@Serializable
data class PatientProfile(
    val id: String,
    val demographics: String,
    val medicalHistory: String,
    val currentMedications: List<String>,
    val allergies: List<String>
)

@Serializable
data class MedicalAnalysisResult(
    val analysis: String,
    val confidence: Float,
    val urgencyLevel: UrgencyLevel,
    val evidenceLevel: EvidenceLevel,
    val riskLevel: RiskLevel
)

@Serializable
data class DrugInteractionAnalysis(
    val interactions: List<String>,
    val severity: String,
    val recommendations: String,
    val sources: List<String>
)

@Serializable
data class MedicalResearchResult(
    val query: String,
    val results: List<String>,
    val sources: List<String>,
    val reliability: Float,
    val lastUpdated: Long
)

@Serializable
data class TurkishMedicalKnowledge(
    val topic: String,
    val content: String,
    val sources: List<String>,
    val lastVerified: Long,
    val certificationLevel: String
)

@Serializable
data class MedicalConsensus(
    val question: String,
    val consensus: String,
    val agreementLevel: Float,
    val sources: List<String>,
    val lastUpdated: Long
)

@Serializable
data class MedicalConsultation(
    val caseId: String,
    val recommendations: List<String>,
    val followUpRequired: Boolean,
    val urgencyLevel: UrgencyLevel,
    val specialists: List<String>
)

@Serializable
data class RiskLevel(
    val level: String,
    val description: String
)

enum class UrgencyLevel {
    LOW, MODERATE, HIGH, CRITICAL
}

enum class EvidenceLevel {
    LOW, MEDIUM, HIGH, VERY_HIGH
}

// ===== API REQUEST/RESPONSE MODELS =====

@Serializable
data class GPTRequest(
    val model: String,
    val messages: List<GPTMessage>,
    val temperature: Float = 0.7f,
    val maxTokens: Int = 1000
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
    val messages: List<ClaudeMessage>,
    val temperature: Float = 0.7f,
    val maxTokens: Int = 1000
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
    val temperature: Float = 0.7f,
    val maxOutputTokens: Int = 1000
)

@Serializable
data class GeminiResponse(
    val candidates: List<GeminiCandidate>
)

@Serializable
data class GeminiCandidate(
    val content: GeminiContent
)