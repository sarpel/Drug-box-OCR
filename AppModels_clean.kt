// CANONICAL AI MODEL DEFINITIONS - NO DUPLICATES

package com.boxocr.simple.data

import kotlinx.serialization.Serializable
import java.util.UUID

// ============== CANONICAL MEDICAL ANALYSIS CLASSES ==============

@Serializable
data class MedicalAnalysisResult(
    val analysisId: String = UUID.randomUUID().toString(),
    val drugName: String,
    val dosageInfo: String?,
    val medicalCategory: String?,
    val riskAssessment: RiskLevel,
    val urgencyLevel: UrgencyLevel,
    val evidenceLevel: EvidenceLevel,
    val recommendations: List<String> = emptyList(),
    val warnings: List<String> = emptyList(),
    val interactions: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val confidence: Float,
    val turkishMedicalInfo: String? = null
)

@Serializable
enum class UrgencyLevel {
    LOW,
    MODERATE,
    HIGH,
    CRITICAL
}

@Serializable
enum class EvidenceLevel {
    LOW,
    MODERATE,
    HIGH,
    VERY_HIGH
}

@Serializable
enum class RiskLevel {
    MINIMAL,
    LOW,
    MODERATE,
    HIGH,
    SEVERE
}

// ============== PATIENT CONTEXT AND PROFILE ==============

@Serializable
data class PatientContext(
    val patientId: String,
    val age: Int?,
    val gender: String?,
    val weight: Double?,
    val medicalHistory: List<String> = emptyList(),
    val currentMedications: List<String> = emptyList(),
    val allergies: List<String> = emptyList(),
    val conditions: List<String> = emptyList(),
    val lastUpdated: Long = System.currentTimeMillis()
)

@Serializable
data class PatientProfile(
    val profileId: String = UUID.randomUUID().toString(),
    val age: Int,
    val weight: Double? = null,
    val allergies: List<String> = emptyList(),
    val conditions: List<String> = emptyList(),
    val medications: List<String> = emptyList(),
    val medicalHistory: List<String> = emptyList(),
    val lastUpdated: Long = System.currentTimeMillis()
)

// ============== AI INTEGRATION CLASSES ==============

@Serializable
data class CustomAIConfiguration(
    val configId: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val providerType: AIProviderType,
    val endpoint: String,
    val apiKey: String,
    val customHeaders: Map<String, String> = emptyMap(),
    val requestFormat: RequestFormat = RequestFormat.JSON,
    val responseFormat: ResponseFormat = ResponseFormat.JSON,
    val modelParameters: Map<String, String> = emptyMap(),
    val turkishMedicalOptimized: Boolean = false,
    val maxTokens: Int = 1000,
    val timeout: Long = 30000,
    val isEnabled: Boolean = true,
    val priority: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class LocalAIModel(
    val modelId: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val modelType: LocalModelType,
    val filePath: String = "",
    val fileSize: Long = 0,
    val version: String = "1.0.0",
    val turkishMedicalSpecialized: Boolean = false,
    val inputShape: List<Int> = emptyList(),
    val outputShape: List<Int> = emptyList(),
    val isLoaded: Boolean = false,
    val loadTime: Long = 0,
    val averageInferenceTime: Long = 0,
    val accuracy: Float = 0f,
    val capabilities: List<String> = emptyList(),
    val metadata: Map<String, String> = emptyMap(),
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
enum class LocalModelType {
    TENSORFLOW_LITE,
    ONNX,
    CUSTOM_BINARY,
    PYTORCH_MOBILE,
    OPENVINO,
    TENSORRT,
    COREML
}

@Serializable
enum class AIProviderType {
    OPENAI,
    CLAUDE,
    GEMINI,
    GROK,
    DEEPSEEK,
    OPENROUTER,
    QWEN,
    CUSTOM_API,
    LOCAL_MODEL
}

@Serializable
enum class RequestFormat {
    JSON,
    XML,
    FORM_DATA,
    CUSTOM
}

@Serializable
enum class ResponseFormat {
    JSON,
    XML,
    TEXT,
    CUSTOM
}

// ============== MEDICAL ANALYSIS SUPPORT CLASSES ==============

@Serializable
data class DrugInteractionAnalysis(
    val analysisId: String = UUID.randomUUID().toString(),
    val interactions: List<String>,
    val severity: RiskLevel,
    val recommendations: List<String> = emptyList(),
    val confidence: Float = 0f,
    val turkishDrugNames: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class MedicalResearchResult(
    val resultId: String = UUID.randomUUID().toString(),
    val findings: String,
    val sources: List<String> = emptyList(),
    val confidence: Float = 0f,
    val turkishTranslation: String? = null,
    val researchDate: Long = System.currentTimeMillis()
)

@Serializable
data class TurkishMedicalKnowledge(
    val knowledgeId: String = UUID.randomUUID().toString(),
    val category: String,
    val content: String,
    val sources: List<String> = emptyList(),
    val lastVerified: Long = System.currentTimeMillis(),
    val isOfficialGuideline: Boolean = false,
    val medicalAuthorityApproved: Boolean = false
)

@Serializable
data class MedicalConsensus(
    val consensusId: String = UUID.randomUUID().toString(),
    val topic: String,
    val consensus: String,
    val evidenceLevel: EvidenceLevel,
    val sources: List<String> = emptyList(),
    val agreementPercentage: Float = 0f,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Serializable
data class ClinicalCase(
    val caseId: String = UUID.randomUUID().toString(),
    val patientProfile: PatientProfile,
    val symptoms: List<String> = emptyList(),
    val diagnosis: String = "",
    val treatment: String = "",
    val outcome: String = "",
    val caseDate: Long = System.currentTimeMillis()
)

@Serializable
data class MedicalConsultation(
    val consultationId: String = UUID.randomUUID().toString(),
    val patientId: String,
    val question: String,
    val response: String,
    val urgency: UrgencyLevel,
    val confidence: Float = 0f,
    val consultationDate: Long = System.currentTimeMillis(),
    val followUpRequired: Boolean = false
)

// ============== AI PROVIDER STATUS AND RESULTS ==============

@Serializable
data class AIProviderStatus(
    val providerId: String,
    val providerName: String,
    val isOnline: Boolean,
    val latency: Long = 0,
    val lastCheck: Long = System.currentTimeMillis(),
    val errorCount: Int = 0,
    val successCount: Int = 0,
    val averageResponseTime: Long = 0
)

@Serializable
data class ModelInferenceResult(
    val resultId: String = UUID.randomUUID().toString(),
    val modelName: String,
    val result: String,
    val confidence: Float,
    val executionTime: Long,
    val type: ModelInferenceType,
    val metadata: Map<String, String> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
enum class ModelInferenceType {
    TEXT_ANALYSIS,
    IMAGE_PROCESSING,
    HYBRID,
    MEDICAL_ANALYSIS,
    DRUG_RECOGNITION,
    TURKISH_NLP
}

@Serializable
data class CustomAIResponse(
    val responseId: String = UUID.randomUUID().toString(),
    val content: String,
    val confidence: Float = 0f,
    val executionTime: Long = 0,
    val providerId: String,
    val modelUsed: String = "",
    val tokenUsage: Int = 0,
    val metadata: Map<String, String> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class LocalInferenceResult(
    val resultId: String = UUID.randomUUID().toString(),
    val modelId: String,
    val output: String,
    val confidence: Float,
    val processingTime: Long,
    val type: ModelInferenceType,
    val memoryUsage: Long = 0,
    val cpuUsage: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class AIConsensusResult(
    val consensusId: String = UUID.randomUUID().toString(),
    val query: String,
    val consensusResponse: String,
    val averageConfidence: Float,
    val totalExecutionTime: Long,
    val providersUsed: List<String> = emptyList(),
    val type: ModelInferenceType,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class ModelResult(
    val resultId: String = UUID.randomUUID().toString(),
    val modelName: String,
    val providerId: String,
    val response: String,
    val confidence: Float,
    val executionTime: Long,
    val type: ModelInferenceType,
    val success: Boolean = true,
    val errorMessage: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

// ============== ADDITIONAL CONFIGURATION CLASSES ==============

@Serializable
data class TurkishMedicalModelInfo(
    val modelId: String = UUID.randomUUID().toString(),
    val name: String,
    val specialization: String,
    val accuracy: Float = 0f,
    val turkishLanguageSupport: Boolean = true,
    val medicalDomains: List<String> = emptyList(),
    val lastTrained: Long = System.currentTimeMillis()
)

@Serializable
data class PrivateCloudAIConfig(
    val configId: String = UUID.randomUUID().toString(),
    val cloudProvider: String,
    val endpoint: String,
    val authenticationMethod: String,
    val credentials: Map<String, String> = emptyMap(),
    val encryptionEnabled: Boolean = true,
    val isCompliant: Boolean = false,
    val complianceStandards: List<String> = emptyList()
)

@Serializable
data class CustomAIProviderConfig(
    val configId: String = UUID.randomUUID().toString(),
    val name: String,
    val cloudProvider: String,
    val endpoint: String,
    val authentication: Map<String, String> = emptyMap(),
    val healthCheckUrl: String = "",
    val maxConcurrentRequests: Int = 10,
    val rateLimitPerMinute: Int = 60,
    val timeout: Long = 30000,
    val retryAttempts: Int = 3,
    val isEnabled: Boolean = true
)

@Serializable
data class LocalAIModelConfig(
    val configId: String = UUID.randomUUID().toString(),
    val modelPath: String,
    val modelType: LocalModelType,
    val hardwareAcceleration: Boolean = false,
    val maxMemoryUsage: Long = 1073741824, // 1GB default
    val threadCount: Int = 4,
    val batchSize: Int = 1,
    val quantization: Boolean = false,
    val optimizationLevel: Int = 1,
    val isEnabled: Boolean = true
)

@Serializable
data class AIIntegrationStatus(
    val statusId: String = UUID.randomUUID().toString(),
    val totalProviders: Int = 0,
    val activeProviders: Int = 0,
    val totalModels: Int = 0,
    val loadedModels: Int = 0,
    val averageResponseTime: Long = 0,
    val successRate: Float = 0f,
    val lastHealthCheck: Long = System.currentTimeMillis(),
    val systemHealth: SystemHealth = SystemHealth.UNKNOWN
)

@Serializable
enum class SystemHealth {
    EXCELLENT,
    GOOD,
    FAIR,
    POOR,
    CRITICAL,
    UNKNOWN
}

@Serializable
enum class AISeverity {
    INFO,
    WARNING,
    ERROR,
    CRITICAL
}

@Serializable
enum class TurkishMedicalAction {
    DOSAGE_CHECK,
    INTERACTION_WARNING,
    ALLERGY_ALERT,
    PRESCRIPTION_REVIEW,
    MEDICAL_CONSULTATION,
    EMERGENCY_ALERT
}
