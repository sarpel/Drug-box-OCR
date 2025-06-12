package com.boxocr.simple.repository

import android.content.Context
import android.graphics.Bitmap
import com.boxocr.simple.data.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton
import java.io.ByteArrayOutputStream
import java.util.Base64

/**
 * Enhanced Windows Automation for Multi-Drug Sessions
 * Handles multiple drugs per session with intelligent timing and field prediction
 */

@Singleton
class EnhancedWindowsAutomationRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val httpClient: OkHttpClient,
    private val json: Json
) {

    private val _sessionState = MutableStateFlow(MultiDrugWindowsSession())
    val sessionState: StateFlow<MultiDrugWindowsSession> = _sessionState.asStateFlow()

    private val _automationProgress = MutableStateFlow(WindowsAutomationProgress())
    val automationProgress: StateFlow<WindowsAutomationProgress> = _automationProgress.asStateFlow()

    private var baseUrl = "http://localhost:8000" // Default Windows client URL

    /**
     * Initialize Windows automation for multi-drug session
     */
    suspend fun initializeMultiDrugSession(
        sessionId: String,
        drugResults: List<MultiDrugResult>,
        automationConfig: WindowsAutomationConfig = WindowsAutomationConfig()
    ): MultiDrugWindowsSession {
        try {
            // Prepare session data
            val sessionData = MultiDrugSessionData(
                sessionId = sessionId,
                totalDrugs = drugResults.size,
                drugs = drugResults.map { result ->
                    WindowsDrugInfo(
                        name = result.detectedName,
                        confidence = result.confidence,
                        ocrText = result.ocrText,
                        isVerified = result.isVerified,
                        isCorrected = result.isCorrected,
                        matchedInfo = result.matchedDrugInfo,
                        visualMatch = result.hasVisualMatch,
                        recoveryMethod = result.recoveryMethod,
                        sourceImageBase64 = result.croppedDrugBoxBitmap?.let { 
                            encodeBitmapToBase64(it) 
                        },
                        processingMetrics = WindowsProcessingMetrics(
                            ocrTime = result.processingTimeMs,
                            visualMatchTime = result.visualMatchTimeMs ?: 0,
                            totalTime = result.processingTimeMs
                        )
                    )
                },
                config = automationConfig,
                createdAt = System.currentTimeMillis()
            )

            // Send session initialization to Windows client
            val response = sendSessionInitialization(sessionData)
            
            val windowsSession = MultiDrugWindowsSession(
                sessionId = sessionId,
                sessionData = sessionData,
                isActive = true,
                currentDrugIndex = 0,
                totalDrugs = drugResults.size,
                automationState = WindowsAutomationState.INITIALIZED,
                windowsResponse = response
            )

            _sessionState.value = windowsSession
            
            // Initialize progress tracking
            _automationProgress.value = WindowsAutomationProgress(
                sessionId = sessionId,
                totalDrugs = drugResults.size,
                currentDrugIndex = 0,
                isActive = true,
                startTime = System.currentTimeMillis()
            )

            return windowsSession

        } catch (e: Exception) {
            throw Exception("Failed to initialize Windows session: ${e.message}")
        }
    }

    /**
     * Process single drug in multi-drug session
     */
    suspend fun processMultiDrugItem(
        sessionId: String,
        drugIndex: Int,
        drugData: WindowsDrugInfo
    ): WindowsAutomationResult {
        try {
            val currentSession = _sessionState.value
            if (currentSession.sessionId != sessionId) {
                throw IllegalStateException("Session ID mismatch")
            }

            // Update session state
            _sessionState.value = currentSession.copy(
                currentDrugIndex = drugIndex,
                currentDrug = drugData,
                automationState = WindowsAutomationState.PROCESSING
            )

            // Update progress
            _automationProgress.value = _automationProgress.value.copy(
                currentDrugIndex = drugIndex,
                currentDrugName = drugData.name,
                currentStage = "Processing ${drugData.name}...",
                progress = drugIndex.toFloat() / currentSession.totalDrugs
            )

            // Send drug processing request
            val processingRequest = WindowsDrugProcessingRequest(
                sessionId = sessionId,
                drugIndex = drugIndex,
                drugData = drugData,
                automationSettings = currentSession.sessionData.config.toProcessingSettings()
            )

            val result = sendDrugProcessingRequest(processingRequest)

            // Update session with result
            val updatedCompletedDrugs = currentSession.completedDrugs.toMutableList()
            updatedCompletedDrugs.add(result)

            _sessionState.value = currentSession.copy(
                completedDrugs = updatedCompletedDrugs,
                automationState = if (drugIndex + 1 >= currentSession.totalDrugs) {
                    WindowsAutomationState.COMPLETED
                } else {
                    WindowsAutomationState.READY_FOR_NEXT
                }
            )

            return result

        } catch (e: Exception) {
            // Update session with error
            val currentSession = _sessionState.value
            val failedDrug = WindowsAutomationFailure(
                drugIndex = drugIndex,
                drugName = drugData.name,
                error = e.message ?: "Unknown error",
                failedAt = System.currentTimeMillis()
            )

            _sessionState.value = currentSession.copy(
                failedDrugs = currentSession.failedDrugs + failedDrug,
                automationState = WindowsAutomationState.ERROR
            )

            throw e
        }
    }

    /**
     * Send session initialization to Windows client
     */
    private suspend fun sendSessionInitialization(
        sessionData: MultiDrugSessionData
    ): WindowsResponse {
        val requestBody = json.encodeToString(
            MultiDrugSessionData.serializer(),
            sessionData
        ).toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("$baseUrl/api/multi-drug/session/init")
            .post(requestBody)
            .build()

        val response = httpClient.newCall(request).execute()
        
        if (!response.isSuccessful) {
            throw Exception("Windows session initialization failed: ${response.message}")
        }

        val responseBody = response.body?.string() ?: ""
        return json.decodeFromString(WindowsResponse.serializer(), responseBody)
    }

    /**
     * Send individual drug processing request
     */
    private suspend fun sendDrugProcessingRequest(
        request: WindowsDrugProcessingRequest
    ): WindowsAutomationResult {
        val requestBody = json.encodeToString(
            WindowsDrugProcessingRequest.serializer(),
            request
        ).toRequestBody("application/json".toMediaType())

        val httpRequest = Request.Builder()
            .url("$baseUrl/api/multi-drug/process")
            .post(requestBody)
            .build()

        val response = httpClient.newCall(httpRequest).execute()
        
        if (!response.isSuccessful) {
            throw Exception("Drug processing failed: ${response.message}")
        }

        val responseBody = response.body?.string() ?: ""
        return json.decodeFromString(WindowsAutomationResult.serializer(), responseBody)
    }

    /**
     * Wait for Windows processing completion with timeout
     */
    suspend fun waitForProcessingCompletion(
        sessionId: String,
        drugIndex: Int,
        timeoutMs: Long = 30000
    ): Boolean {
        val startTime = System.currentTimeMillis()
        
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            try {
                val status = checkProcessingStatus(sessionId, drugIndex)
                
                when (status.state) {
                    "completed" -> return true
                    "failed" -> throw Exception("Processing failed: ${status.error}")
                    "processing" -> {
                        // Update progress with Windows feedback
                        _automationProgress.value = _automationProgress.value.copy(
                            currentStage = status.currentStage,
                            windowsProgress = status.progress
                        )
                        delay(1000) // Check every second
                    }
                    else -> delay(500) // Check more frequently for other states
                }
            } catch (e: Exception) {
                if (System.currentTimeMillis() - startTime > timeoutMs - 5000) {
                    // Only throw if we're near timeout
                    throw e
                }
                delay(1000)
            }
        }
        
        throw Exception("Processing timeout after ${timeoutMs}ms")
    }

    /**
     * Check processing status from Windows client
     */
    private suspend fun checkProcessingStatus(
        sessionId: String,
        drugIndex: Int
    ): WindowsProcessingStatus {
        val request = Request.Builder()
            .url("$baseUrl/api/multi-drug/status?sessionId=$sessionId&drugIndex=$drugIndex")
            .get()
            .build()

        val response = httpClient.newCall(request).execute()
        
        if (!response.isSuccessful) {
            throw Exception("Status check failed: ${response.message}")
        }

        val responseBody = response.body?.string() ?: ""
        return json.decodeFromString(WindowsProcessingStatus.serializer(), responseBody)
    }

    /**
     * Finalize multi-drug session
     */
    suspend fun finalizeMultiDrugSession(
        sessionId: String,
        batchSummary: BatchProcessingSummary
    ): WindowsSessionSummary {
        try {
            val finalizationRequest = WindowsSessionFinalization(
                sessionId = sessionId,
                totalDrugs = batchSummary.totalDrugs,
                completedCount = batchSummary.completedCount,
                failedCount = batchSummary.failedCount,
                totalProcessingTime = batchSummary.totalProcessingTime,
                sessionSummary = batchSummary
            )

            val requestBody = json.encodeToString(
                WindowsSessionFinalization.serializer(),
                finalizationRequest
            ).toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("$baseUrl/api/multi-drug/session/finalize")
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            
            if (!response.isSuccessful) {
                throw Exception("Session finalization failed: ${response.message}")
            }

            val responseBody = response.body?.string() ?: ""
            val sessionSummary = json.decodeFromString(WindowsSessionSummary.serializer(), responseBody)

            // Update session state to completed
            _sessionState.value = _sessionState.value.copy(
                isActive = false,
                isCompleted = true,
                automationState = WindowsAutomationState.COMPLETED,
                sessionSummary = sessionSummary,
                completedAt = System.currentTimeMillis()
            )

            // Update progress to completed
            _automationProgress.value = _automationProgress.value.copy(
                isActive = false,
                isCompleted = true,
                progress = 1.0f,
                endTime = System.currentTimeMillis()
            )

            return sessionSummary

        } catch (e: Exception) {
            throw Exception("Failed to finalize session: ${e.message}")
        }
    }

    /**
     * Pause multi-drug session
     */
    suspend fun pauseMultiDrugSession(sessionId: String) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/api/multi-drug/session/pause")
                .post("""{"sessionId": "$sessionId"}""".toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            
            if (response.isSuccessful) {
                _sessionState.value = _sessionState.value.copy(
                    automationState = WindowsAutomationState.PAUSED
                )
                _automationProgress.value = _automationProgress.value.copy(
                    isPaused = true
                )
            }
        } catch (e: Exception) {
            // Handle pause error gracefully
        }
    }

    /**
     * Resume multi-drug session
     */
    suspend fun resumeMultiDrugSession(sessionId: String) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/api/multi-drug/session/resume")
                .post("""{"sessionId": "$sessionId"}""".toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            
            if (response.isSuccessful) {
                _sessionState.value = _sessionState.value.copy(
                    automationState = WindowsAutomationState.PROCESSING
                )
                _automationProgress.value = _automationProgress.value.copy(
                    isPaused = false
                )
            }
        } catch (e: Exception) {
            // Handle resume error gracefully
        }
    }

    /**
     * Cancel multi-drug session
     */
    suspend fun cancelMultiDrugSession(sessionId: String) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/api/multi-drug/session/cancel")
                .post("""{"sessionId": "$sessionId"}""".toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            
            _sessionState.value = _sessionState.value.copy(
                isActive = false,
                isCancelled = true,
                automationState = WindowsAutomationState.CANCELLED
            )
            
            _automationProgress.value = _automationProgress.value.copy(
                isActive = false,
                isCancelled = true
            )
        } catch (e: Exception) {
            // Handle cancellation error gracefully
        }
    }

    /**
     * Get current session state
     */
    fun getCurrentSessionState(): MultiDrugWindowsSession {
        return _sessionState.value
    }

    /**
     * Get current automation progress
     */
    fun getCurrentAutomationProgress(): WindowsAutomationProgress {
        return _automationProgress.value
    }

    /**
     * Encode bitmap to base64 for Windows transmission
     */
    private fun encodeBitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.getEncoder().encodeToString(byteArray)
    }

    /**
     * Clear session state
     */
    fun clearSessionState() {
        _sessionState.value = MultiDrugWindowsSession()
        _automationProgress.value = WindowsAutomationProgress()
    }
}

// Enhanced data classes for Windows automation

@Serializable
data class MultiDrugSessionData(
    val sessionId: String,
    val totalDrugs: Int,
    val drugs: List<WindowsDrugInfo>,
    val config: WindowsAutomationConfig,
    val createdAt: Long
)

@Serializable
data class WindowsDrugInfo(
    val name: String,
    val confidence: Float,
    val ocrText: String,
    val isVerified: Boolean,
    val isCorrected: Boolean,
    val matchedInfo: TurkishDrugInfo?,
    val visualMatch: Boolean,
    val recoveryMethod: String,
    val sourceImageBase64: String?,
    val processingMetrics: WindowsProcessingMetrics
)

@Serializable
data class WindowsProcessingMetrics(
    val ocrTime: Long,
    val visualMatchTime: Long,
    val totalTime: Long
)

@Serializable
data class WindowsAutomationConfig(
    val smartTiming: Boolean = true,
    val fieldPrediction: Boolean = true,
    val autoVerification: Boolean = false,
    val batchMode: Boolean = true,
    val delayBetweenDrugs: Long = 1000,
    val timeoutPerDrug: Long = 30000,
    val retryCount: Int = 3
)

@Serializable
data class WindowsDrugProcessingRequest(
    val sessionId: String,
    val drugIndex: Int,
    val drugData: WindowsDrugInfo,
    val automationSettings: ProcessingSettings
)

@Serializable
data class ProcessingSettings(
    val timing: TimingSettings,
    val fieldHandling: FieldHandlingSettings,
    val verification: VerificationSettings
)

@Serializable
data class TimingSettings(
    val smartTiming: Boolean,
    val baseDelay: Long,
    val fieldDelay: Long,
    val verificationDelay: Long
)

@Serializable
data class FieldHandlingSettings(
    val predictiveEntry: Boolean,
    val autoCompletion: Boolean,
    val smartNavigation: Boolean
)

@Serializable
data class VerificationSettings(
    val autoVerify: Boolean,
    val confidenceThreshold: Float,
    val visualMatchRequired: Boolean
)

@Serializable
data class WindowsResponse(
    val success: Boolean,
    val message: String,
    val sessionId: String,
    val timestamp: Long
)

@Serializable
data class WindowsAutomationResult(
    val drugIndex: Int,
    val drugName: String,
    val success: Boolean,
    val processingTime: Long,
    val windowsData: Map<String, String>,
    val error: String? = null,
    val completedAt: Long
)

@Serializable
data class WindowsProcessingStatus(
    val state: String,
    val progress: Float,
    val currentStage: String,
    val error: String? = null
)

@Serializable
data class WindowsSessionFinalization(
    val sessionId: String,
    val totalDrugs: Int,
    val completedCount: Int,
    val failedCount: Int,
    val totalProcessingTime: Long,
    val sessionSummary: BatchProcessingSummary
)

@Serializable
data class WindowsSessionSummary(
    val sessionId: String,
    val totalProcessingTime: Long,
    val drugsProcessed: Int,
    val successRate: Float,
    val windowsIntegrationStats: Map<String, Any>,
    val completedAt: Long
)

data class MultiDrugWindowsSession(
    val sessionId: String = "",
    val sessionData: MultiDrugSessionData? = null,
    val isActive: Boolean = false,
    val isCompleted: Boolean = false,
    val isCancelled: Boolean = false,
    val currentDrugIndex: Int = 0,
    val currentDrug: WindowsDrugInfo? = null,
    val totalDrugs: Int = 0,
    val automationState: WindowsAutomationState = WindowsAutomationState.IDLE,
    val windowsResponse: WindowsResponse? = null,
    val completedDrugs: List<WindowsAutomationResult> = emptyList(),
    val failedDrugs: List<WindowsAutomationFailure> = emptyList(),
    val sessionSummary: WindowsSessionSummary? = null,
    val startedAt: Long = 0L,
    val completedAt: Long? = null
)

data class WindowsAutomationProgress(
    val sessionId: String = "",
    val totalDrugs: Int = 0,
    val currentDrugIndex: Int = 0,
    val currentDrugName: String = "",
    val currentStage: String = "",
    val progress: Float = 0f,
    val windowsProgress: Float = 0f,
    val isActive: Boolean = false,
    val isCompleted: Boolean = false,
    val isPaused: Boolean = false,
    val isCancelled: Boolean = false,
    val startTime: Long = 0L,
    val endTime: Long? = null,
    val estimatedTimeRemaining: Long = 0L
)

data class WindowsAutomationFailure(
    val drugIndex: Int,
    val drugName: String,
    val error: String,
    val failedAt: Long
)

enum class WindowsAutomationState {
    IDLE,
    INITIALIZED,
    PROCESSING,
    READY_FOR_NEXT,
    PAUSED,
    COMPLETED,
    ERROR,
    CANCELLED
}

fun WindowsAutomationConfig.toProcessingSettings(): ProcessingSettings {
    return ProcessingSettings(
        timing = TimingSettings(
            smartTiming = this.smartTiming,
            baseDelay = this.delayBetweenDrugs,
            fieldDelay = this.delayBetweenDrugs / 2,
            verificationDelay = this.delayBetweenDrugs / 4
        ),
        fieldHandling = FieldHandlingSettings(
            predictiveEntry = this.fieldPrediction,
            autoCompletion = this.fieldPrediction,
            smartNavigation = this.smartTiming
        ),
        verification = VerificationSettings(
            autoVerify = this.autoVerification,
            confidenceThreshold = 0.8f,
            visualMatchRequired = false
        )
    )
}
