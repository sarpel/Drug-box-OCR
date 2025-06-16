package com.boxocr.simple.data

import kotlinx.serialization.Serializable
import java.util.UUID

// ============== CORE OCR & DRUG MODELS ==============

@Serializable
data class DrugInfo(
    val name: String,
    val company: String = "",
    val atcCode: String = "",
    val sgkCode: String = "",
    val price: String = "",
    val confidence: Float = 0f,
    val isGeneric: Boolean = false,
    val activeIngredient: String = "",
    val prescriptionRequired: Boolean = true
)

@Serializable
sealed class OCRResult {
    @Serializable
    data class Success(
        val text: String,
        val confidence: Float,
        val processingTime: Long,
        val source: String = "gemini"
    ) : OCRResult()
    
    @Serializable
    data class Error(
        val message: String,
        val exception: String? = null
    ) : OCRResult()
}

@Serializable
data class MatchResult(
    val drug: DrugInfo,
    val confidence: Float,
    val matchType: MatchType,
    val processingTime: Long
)

@Serializable
enum class MatchType {
    EXACT, FUZZY, PARTIAL, AI_ENHANCED
}

// ============== BATCH PROCESSING MODELS ==============

@Serializable
data class BatchItem(
    val id: String = UUID.randomUUID().toString(),
    val imagePath: String,
    val ocrResult: OCRResult? = null,
    val matchResult: MatchResult? = null,
    val status: BatchStatus = BatchStatus.PENDING,
    val timestamp: Long = System.currentTimeMillis(),
    val error: String? = null
)

@Serializable
enum class BatchStatus {
    PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED
}

@Serializable
data class BatchSession(
    val id: String = UUID.randomUUID().toString(),
    val items: List<BatchItem> = emptyList(),
    val status: BatchStatus = BatchStatus.PENDING,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val totalItems: Int = 0,
    val completedItems: Int = 0,
    val failedItems: Int = 0
)

// ============== AUTOMATION MODELS ==============

@Serializable
sealed class AutomationResult {
    @Serializable
    data class Success(
        val sessionId: String,
        val windowTitle: String,
        val completedCount: Int,
        val message: String = ""
    ) : AutomationResult()
    
    @Serializable
    data class Error(
        val sessionId: String,
        val windowTitle: String,
        val message: String,
        val errorCode: String = ""
    ) : AutomationResult()
    
    @Serializable
    data class BatchComplete(
        val sessionId: String,
        val windowTitle: String,
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

@Serializable
data class PrescriptionSoftware(
    val id: String,
    val name: String,
    val version: String,
    val isActive: Boolean,
    val confidence: Float
)

// ============== AI MEDICAL ANALYSIS MODELS ==============

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
    LOW, MODERATE, HIGH, CRITICAL
}

@Serializable
enum class EvidenceLevel {
    LOW, MODERATE, HIGH, VERY_HIGH
}

@Serializable
enum class RiskLevel {
    MINIMAL, LOW, MODERATE, HIGH, SEVERE
}

@Serializable
data class DrugInteractionAnalysis(
    val interactions: List<String>,
    val severity: InteractionSeverity
)

enum class InteractionSeverity {
    MINOR, MODERATE, MAJOR, SEVERE
}

@Serializable
data class MedicalEvidenceResult(
    val findings: List<String>
)

@Serializable
data class TurkishMedicalKnowledge(
    val content: String,
    val category: String
)

@Serializable
data class AIConsensusResult(
    val recommendation: String,
    val confidence: Float
)

@Serializable
data class AIConsultationResult(
    val question: String,
    val response: String,
    val urgency: UrgencyLevel
)

@Serializable
data class AIIntelligenceStatus(
    val overallReady: Boolean,
    val providersActive: Int,
    val modelsLoaded: Int
)

@Serializable
data class AIIntelligenceAnalytics(
    val totalInferences: Int,
    val averageResponseTime: Float,
    val accuracy: Float
)

@Serializable
data class TurkishMedicalResponse(
    val turkishResponse: String,
    val confidence: Float,
    val actionRequired: TurkishMedicalAction? = null
)

enum class TurkishMedicalAction {
    DOSAGE_CHECK, INTERACTION_WARNING, CONTRAINDICATION_ALERT,
    CONSULTATION_REQUIRED, ALTERNATIVE_SUGGESTION
}

@Serializable
data class AIRecommendation(
    val message: String,
    val severity: AISeverity,
    val actionRequired: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
enum class AISeverity {
    INFO, WARNING, CRITICAL, EMERGENCY
}

// ============== AI PROVIDER MODELS ==============

enum class AIProviderType {
    OPENAI, CLAUDE, GEMINI, GROK, DEEPSEEK, OPENROUTER, QWEN,
    LMSTUDIO, OLLAMA, LOCALAI, LLAMA_CPP, ANYTHINGLLM, CUSTOM_API,
    TENSORFLOW_LITE, ONNX, PYTORCH_MOBILE
}

@Serializable
data class CustomAIProvider(
    val id: String,
    val name: String,
    val description: String,
    val providerType: AIProviderType,
    val endpoint: String,
    val apiKey: String,
    val customHeaders: Map<String, String>,
    val requestFormat: String,
    val responseFormat: String,
    val modelParameters: Map<String, String>,
    val turkishMedicalOptimized: Boolean,
    val maxTokens: Int,
    val timeout: Long,
    val isActive: Boolean = false
)

@Serializable
data class LocalAIModel(
    val modelId: String,
    val name: String,
    val description: String,
    val modelType: AIProviderType,
    val downloadUrl: String,
    val isLoaded: Boolean,
    val version: String,
    val filePath: String,
    val size: Long,
    val inputShape: List<Int>,
    val outputShape: List<Int>,
    val preprocessingConfig: Map<String, String>,
    val postprocessingConfig: Map<String, String>,
    val performanceMetrics: ModelPerformanceMetrics
)

@Serializable
data class ModelPerformanceMetrics(
    val inferenceTime: Float,
    val memoryUsage: Float,
    val accuracy: Float
)

@Serializable
data class AIInferenceResult(
    val modelId: String,
    val output: String,
    val processingTime: Float,
    val memoryUsed: Float
)

@Serializable
data class AIProcessingResult(
    val modelId: String,
    val processingTime: Float,
    val confidence: Float
)

@Serializable
data class AIPerformanceAnalysis(
    val confidence: Float,
    val executionTime: Float
)

@Serializable
data class AIModelSummary(
    val name: String,
    val description: String,
    val version: String,
    val performanceMetrics: ModelPerformanceMetrics
)

@Serializable
data class AIResponse(
    val provider: String,
    val response: String,
    val confidence: Float,
    val processingTime: Float
)

// ============== INTELLIGENCE LAYER STATUS ==============

@Serializable
enum class SystemHealth {
    EXCELLENT, GOOD, FAIR, POOR, CRITICAL
}

@Serializable
data class AIIntegrationStatus(
    val providersConfigured: Int,
    val providersActive: Int,
    val modelsLoaded: Int,
    val systemReady: Boolean,
    val lastUpdate: Long = System.currentTimeMillis()
)

// ============== PRESCRIPTION TEMPLATE MODELS ==============

enum class TemplateCategory {
    COMMON_CONDITIONS, EMERGENCY, CHRONIC_DISEASES, PEDIATRIC,
    GERIATRIC, CARDIOLOGY, DIABETES, CUSTOM
}

@Serializable
data class PrescriptionTemplate(
    val id: String,
    val name: String,
    val description: String,
    val category: TemplateCategory,
    val drugs: List<TemplateDrug>,
    val usageCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val isCustom: Boolean = false
)

@Serializable
data class TemplateDrug(
    val drugName: String,
    val dosage: String,
    val frequency: String,
    val duration: String,
    val instructions: String = ""
)

// ============== PRESCRIPTION HISTORY MODELS ==============

@Serializable
data class PrescriptionHistory(
    val id: String,
    val patientInfo: PatientInfo,
    val drugs: List<String>,
    val timestamp: Long,
    val status: PrescriptionStatus,
    val drugCount: Int
)

@Serializable
data class PatientInfo(
    val name: String,
    val id: String,
    val age: Int? = null,
    val gender: String? = null
)

enum class PrescriptionStatus {
    PENDING, COMPLETED, CANCELLED
}

@Serializable
data class PrescriptionStatistics(
    val totalPrescriptions: Int,
    val avgDrugsPerPrescription: Float,
    val mostCommonDrugs: List<String>,
    val dailyAverage: Float,
    val monthlyAverage: Float
)

// ============== ADDITIONAL BUSINESS MODELS ==============

@Serializable
data class ScanResult(
    val drug: String,
    val matchType: String,
    val processingTime: Long
)

@Serializable
data class AppSettings(
    val apiKey: String,
    val similarityThreshold: Float,
    val autoClipboard: Boolean
)

@Serializable
data class VerificationData(
    val drug: String,
    val matchType: String,
    val processingTime: Long
)

@Serializable
data class WindowsAutomationData(
    val name: String
)

@Serializable
data class WindowsDrugData(
    val name: String
)

@Serializable
data class BatchProcessingSummary(
    val completedCount: Int,
    val failedCount: Int
)

@Serializable
data class MultiDrugSessionEntity(
    val sessionId: String,
    val timestamp: Long,
    val drugCount: Int,
    val successCount: Int,
    val processingTimeMs: Long
)

@Serializable
data class WindowsIntegrationSettings(
    val pasteDelay: Long,
    val enableFieldDetection: Boolean,
    val customKeystrokes: Map<String, String>,
    val timingProfile: String,
    val retryAttempts: Int,
    val enableSmartDetection: Boolean
)

@Serializable
data class WindowDetectionResult(
    val windowTitle: String,
    val message: String,
    val completedCount: Int = 0
)

@Serializable
data class BatchDrugMetadata(
    val drugName: String,
    val expectedPosition: Int,
    val confidence: Float,
    val alternativeNames: List<String> = emptyList()
)


// ============== CUSTOM AI INTEGRATION MODELS ==============

@Serializable
data class CustomAIConfiguration(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val providerType: AIProviderType,
    val endpoint: String,
    val apiKey: String,
    val customHeaders: Map<String, String> = emptyMap(),
    val requestFormat: RequestFormat = RequestFormat.JSON,
    val responseFormat: ResponseFormat = ResponseFormat.JSON,
    val modelParameters: Map<String, String> = emptyMap(),
    val turkishMedicalOptimized: Boolean = false,
    val maxTokens: Int = 4096,
    val timeout: Long = 30000L,
    val isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
enum class RequestFormat {
    JSON, XML, PLAIN_TEXT, FORM_DATA
}

@Serializable
enum class ResponseFormat {
    JSON, XML, PLAIN_TEXT, STREAMING
}

@Serializable
data class AIProviderStatus(
    val providerId: String,
    val providerName: String,
    val status: ProviderConnectionStatus,
    val lastChecked: Long = System.currentTimeMillis(),
    val responseTime: Long = 0L,
    val errorMessage: String? = null,
    val successfulRequests: Int = 0,
    val failedRequests: Int = 0
)

@Serializable
enum class ProviderConnectionStatus {
    CONNECTED, DISCONNECTED, ERROR, TIMEOUT, RATE_LIMITED
}

@Serializable
data class ModelInferenceResult(
    val modelId: String,
    val providerId: String,
    val input: String,
    val output: String,
    val confidence: Float,
    val processingTime: Long,
    val memoryUsed: Float = 0f,
    val timestamp: Long = System.currentTimeMillis(),
    val inferenceType: ModelInferenceType = ModelInferenceType.TEXT_COMPLETION
)

@Serializable
enum class ModelInferenceType {
    TEXT_COMPLETION, TEXT_CLASSIFICATION, QUESTION_ANSWERING, 
    TURKISH_MEDICAL_ANALYSIS, DRUG_INTERACTION_CHECK
}

@Serializable
data class CustomAIResponse(
    val requestId: String,
    val providerId: String,
    val response: String,
    val confidence: Float,
    val processingTime: Long,
    val tokenUsage: TokenUsage? = null,
    val metadata: Map<String, String> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class TokenUsage(
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int,
    val cost: Float = 0f
)

@Serializable
data class LocalInferenceResult(
    val modelPath: String,
    val modelType: LocalModelType,
    val input: String,
    val output: String,
    val confidence: Float,
    val processingTime: Long,
    val memoryUsed: Float,
    val cpuUsage: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
enum class LocalModelType {
    TENSORFLOW_LITE, ONNX, PYTORCH_MOBILE, LLAMA_CPP, 
    OLLAMA, CUSTOM_BINARY, QUANTIZED_MODEL
}

@Serializable
data class LocalModelPerformance(
    val modelId: String,
    val averageInferenceTime: Float,
    val averageMemoryUsage: Float,
    val averageCpuUsage: Float,
    val accuracy: Float,
    val totalInferences: Int,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Serializable
data class ModelResult(
    val modelId: String,
    val result: String,
    val confidence: Float,
    val processingTime: Long,
    val success: Boolean = true,
    val errorMessage: String? = null
)

@Serializable
data class TurkishMedicalModelInfo(
    val modelId: String,
    val name: String,
    val description: String,
    val version: String,
    val turkishMedicalAccuracy: Float,
    val drugDatabaseCompatibility: Boolean,
    val sgkIntegration: Boolean,
    val medulaCompliance: Boolean,
    val specializations: List<String> = emptyList()
)

@Serializable
data class PrivateCloudAIConfig(
    val cloudProvider: String,
    val endpoint: String,
    val apiKey: String,
    val region: String,
    val deploymentId: String? = null,
    val customConfig: Map<String, String> = emptyMap(),
    val securitySettings: SecuritySettings? = null
)

@Serializable
data class SecuritySettings(
    val encryption: Boolean = true,
    val certificateValidation: Boolean = true,
    val allowSelfSigned: Boolean = false,
    val customCertificates: List<String> = emptyList()
)

@Serializable
data class CustomAIProviderConfig(
    val providerId: String,
    val name: String,
    val baseUrl: String,
    val authType: AuthenticationType,
    val credentials: Map<String, String>,
    val defaultModel: String? = null,
    val supportedFeatures: List<String> = emptyList(),
    val rateLimits: RateLimitConfig? = null
)

@Serializable
enum class AuthenticationType {
    API_KEY, BEARER_TOKEN, BASIC_AUTH, OAUTH2, CUSTOM_HEADER
}

@Serializable
data class RateLimitConfig(
    val requestsPerMinute: Int,
    val requestsPerHour: Int,
    val tokensPerMinute: Int? = null,
    val burstAllowance: Int = 5
)

@Serializable
data class LocalAIModelConfig(
    val modelId: String,
    val modelPath: String,
    val modelType: LocalModelType,
    val quantization: QuantizationType = QuantizationType.NONE,
    val maxMemoryMB: Int = 2048,
    val numThreads: Int = 4,
    val optimizationLevel: OptimizationLevel = OptimizationLevel.BALANCED,
    val cacheEnabled: Boolean = true
)

@Serializable
enum class QuantizationType {
    NONE, INT8, INT16, FLOAT16, DYNAMIC
}

@Serializable
enum class OptimizationLevel {
    PERFORMANCE, BALANCED, MEMORY_OPTIMIZED, POWER_EFFICIENT
}
