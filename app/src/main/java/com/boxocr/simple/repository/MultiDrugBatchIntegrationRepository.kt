package com.boxocr.simple.repository

import android.content.Context
import android.graphics.Bitmap
import com.boxocr.simple.data.*
import com.boxocr.simple.database.BoxOCRDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enhanced Batch Processing Integration for Multi-Drug Workflow
 * Seamlessly connects multi-drug scanning results to existing batch processing
 */

@Singleton
class MultiDrugBatchIntegrationRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: BoxOCRDatabase,
    private val batchScanningRepository: BatchScanningRepository,
    private val multiDrugScannerRepository: MultiDrugScannerRepository,
    private val windowsAutomationRepository: WindowsAutomationRepository
) {

    private val _batchProcessingState = MutableStateFlow(MultiDrugBatchState())
    val batchProcessingState: StateFlow<MultiDrugBatchState> = _batchProcessingState.asStateFlow()

    private val _sessionProgress = MutableStateFlow(BatchSessionProgress())
    val sessionProgress: StateFlow<BatchSessionProgress> = _sessionProgress.asStateFlow()

    /**
     * Create a new batch session from multi-drug results
     */
    suspend fun createBatchFromMultiDrugResults(
        multiDrugResults: List<MultiDrugResult>,
        sessionName: String = "Multi-Drug Batch ${System.currentTimeMillis()}"
    ): BatchSession {
        try {
            // Filter verified results
            val verifiedResults = multiDrugResults.filter { it.isVerified }
            
            if (verifiedResults.isEmpty()) {
                throw IllegalArgumentException("No verified results to process")
            }

            // Create batch session
            val batchSession = BatchSession(
                id = "multi_drug_${System.currentTimeMillis()}",
                name = sessionName,
                drugNames = verifiedResults.map { it.detectedName },
                totalDrugs = verifiedResults.size,
                createdAt = System.currentTimeMillis(),
                source = "multi_drug_scanner",
                multiDrugSourceData = MultiDrugSourceData(
                    originalResults = verifiedResults,
                    processingMetrics = calculateProcessingMetrics(verifiedResults),
                    qualityMetrics = calculateQualityMetrics(verifiedResults)
                )
            )

            // Save to database
            database.batchSessionDao().insertSession(batchSession.toEntity())

            // Initialize batch processing state
            _batchProcessingState.value = MultiDrugBatchState(
                session = batchSession,
                drugResults = verifiedResults,
                currentIndex = 0,
                isProcessing = false,
                completedDrugs = emptyList(),
                failedDrugs = emptyList()
            )

            return batchSession

        } catch (e: Exception) {
            throw Exception("Failed to create batch from multi-drug results: ${e.message}")
        }
    }

    /**
     * Start batch processing for multi-drug session
     */
    suspend fun startBatchProcessing(
        sessionId: String,
        windowsIntegration: Boolean = true
    ) {
        try {
            val currentState = _batchProcessingState.value
            val session = currentState.session
                ?: throw IllegalStateException("No active session")

            if (session.id != sessionId) {
                throw IllegalArgumentException("Session ID mismatch")
            }

            _batchProcessingState.value = currentState.copy(
                isProcessing = true,
                currentIndex = 0,
                processingStartTime = System.currentTimeMillis()
            )

            // Initialize session progress
            _sessionProgress.value = BatchSessionProgress(
                sessionId = sessionId,
                totalDrugs = session.totalDrugs,
                currentDrugIndex = 0,
                completedCount = 0,
                failedCount = 0,
                isActive = true,
                startTime = System.currentTimeMillis()
            )

            // Start Windows automation if enabled
            if (windowsIntegration) {
                windowsAutomationRepository.startSession(sessionId)
            }

            // Process each drug sequentially
            processDrugsSequentially(session, windowsIntegration)

        } catch (e: Exception) {
            _batchProcessingState.value = _batchProcessingState.value.copy(
                isProcessing = false,
                error = "Failed to start batch processing: ${e.message}"
            )
        }
    }

    /**
     * Process drugs sequentially with Windows automation integration
     */
    private suspend fun processDrugsSequentially(
        session: BatchSession,
        windowsIntegration: Boolean
    ) {
        val drugResults = _batchProcessingState.value.drugResults
        val completedDrugs = mutableListOf<CompletedDrugProcessing>()
        val failedDrugs = mutableListOf<FailedDrugProcessing>()

        drugResults.forEachIndexed { index, drugResult ->
            try {
                // Update processing state
                _batchProcessingState.value = _batchProcessingState.value.copy(
                    currentIndex = index,
                    currentDrug = drugResult,
                    processingStage = "Processing ${drugResult.detectedName}..."
                )

                // Update session progress
                _sessionProgress.value = _sessionProgress.value.copy(
                    currentDrugIndex = index,
                    currentDrugName = drugResult.detectedName,
                    currentStage = "Processing drug ${index + 1}/${drugResults.size}"
                )

                // Process individual drug
                val processingResult = processSingleDrug(
                    drugResult = drugResult,
                    sessionId = session.id,
                    drugIndex = index,
                    windowsIntegration = windowsIntegration
                )

                if (processingResult.success) {
                    completedDrugs.add(
                        CompletedDrugProcessing(
                            drugResult = drugResult,
                            processingTime = processingResult.processingTime,
                            windowsData = processingResult.windowsData,
                            completedAt = System.currentTimeMillis()
                        )
                    )
                } else {
                    failedDrugs.add(
                        FailedDrugProcessing(
                            drugResult = drugResult,
                            error = processingResult.error,
                            failedAt = System.currentTimeMillis()
                        )
                    )
                }

                // Update progress
                _sessionProgress.value = _sessionProgress.value.copy(
                    completedCount = completedDrugs.size,
                    failedCount = failedDrugs.size,
                    progress = (index + 1).toFloat() / drugResults.size
                )

                // Small delay for UI feedback
                delay(200)

            } catch (e: Exception) {
                failedDrugs.add(
                    FailedDrugProcessing(
                        drugResult = drugResult,
                        error = e.message ?: "Unknown error",
                        failedAt = System.currentTimeMillis()
                    )
                )
            }
        }

        // Complete batch processing
        completeBatchProcessing(session, completedDrugs, failedDrugs, windowsIntegration)
    }

    /**
     * Process a single drug with Windows automation
     */
    private suspend fun processSingleDrug(
        drugResult: MultiDrugResult,
        sessionId: String,
        drugIndex: Int,
        windowsIntegration: Boolean
    ): SingleDrugProcessingResult {
        val startTime = System.currentTimeMillis()
        
        try {
            // Prepare drug data for Windows automation
            val drugData = WindowsDrugData(
                name = drugResult.detectedName,
                confidence = drugResult.confidence,
                ocrText = drugResult.ocrText,
                isVerified = drugResult.isVerified,
                isCorrected = drugResult.isCorrected,
                matchedInfo = drugResult.matchedDrugInfo,
                visualMatch = drugResult.hasVisualMatch,
                recoveryMethod = drugResult.recoveryMethod,
                sourceImage = drugResult.croppedDrugBoxBitmap,
                processingMetrics = DrugProcessingMetrics(
                    ocrTime = drugResult.processingTimeMs,
                    visualMatchTime = drugResult.visualMatchTimeMs ?: 0,
                    totalProcessingTime = drugResult.processingTimeMs
                )
            )

            var windowsData: WindowsAutomationData? = null

            if (windowsIntegration) {
                // Send to Windows automation
                windowsData = windowsAutomationRepository.processMultiDrugItem(
                    sessionId = sessionId,
                    drugIndex = drugIndex,
                    drugData = drugData
                )

                // Wait for Windows processing completion
                windowsAutomationRepository.waitForProcessingCompletion(
                    sessionId = sessionId,
                    drugIndex = drugIndex,
                    timeoutMs = 30000 // 30 seconds timeout
                )
            }

            // Save to batch repository
            batchScanningRepository.addDrugToBatch(
                sessionId = sessionId,
                drugName = drugResult.detectedName,
                confidence = drugResult.confidence,
                metadata = BatchDrugMetadata(
                    multiDrugSource = true,
                    originalOcrText = drugResult.ocrText,
                    visualMatch = drugResult.hasVisualMatch,
                    isVerified = drugResult.isVerified,
                    isCorrected = drugResult.isCorrected,
                    processingTime = drugResult.processingTimeMs
                )
            )

            val processingTime = System.currentTimeMillis() - startTime

            return SingleDrugProcessingResult(
                success = true,
                processingTime = processingTime,
                windowsData = windowsData,
                drugData = drugData
            )

        } catch (e: Exception) {
            return SingleDrugProcessingResult(
                success = false,
                error = e.message ?: "Unknown error",
                processingTime = System.currentTimeMillis() - startTime
            )
        }
    }

    /**
     * Complete batch processing and generate summary
     */
    private suspend fun completeBatchProcessing(
        session: BatchSession,
        completedDrugs: List<CompletedDrugProcessing>,
        failedDrugs: List<FailedDrugProcessing>,
        windowsIntegration: Boolean
    ) {
        try {
            val endTime = System.currentTimeMillis()
            val totalTime = endTime - (_batchProcessingState.value.processingStartTime ?: endTime)

            // Create batch completion summary
            val batchSummary = BatchProcessingSummary(
                sessionId = session.id,
                totalDrugs = session.totalDrugs,
                completedCount = completedDrugs.size,
                failedCount = failedDrugs.size,
                totalProcessingTime = totalTime,
                averageProcessingTime = if (completedDrugs.isNotEmpty()) {
                    completedDrugs.map { it.processingTime }.average().toLong()
                } else 0L,
                windowsIntegration = windowsIntegration,
                completedDrugs = completedDrugs,
                failedDrugs = failedDrugs,
                completedAt = endTime
            )

            // Update batch processing state
            _batchProcessingState.value = _batchProcessingState.value.copy(
                isProcessing = false,
                isCompleted = true,
                completedDrugs = completedDrugs,
                failedDrugs = failedDrugs,
                batchSummary = batchSummary,
                processingEndTime = endTime
            )

            // Update session progress
            _sessionProgress.value = _sessionProgress.value.copy(
                isActive = false,
                isCompleted = true,
                completedCount = completedDrugs.size,
                failedCount = failedDrugs.size,
                progress = 1.0f,
                endTime = endTime
            )

            // Finalize Windows automation session
            if (windowsIntegration) {
                windowsAutomationRepository.finalizeSession(session.id, batchSummary)
            }

            // Save batch summary to database
            database.batchSessionDao().updateSessionSummary(session.id, batchSummary)

        } catch (e: Exception) {
            _batchProcessingState.value = _batchProcessingState.value.copy(
                isProcessing = false,
                error = "Failed to complete batch processing: ${e.message}"
            )
        }
    }

    /**
     * Pause batch processing
     */
    suspend fun pauseBatchProcessing() {
        val currentState = _batchProcessingState.value
        if (currentState.isProcessing) {
            _batchProcessingState.value = currentState.copy(
                isProcessing = false,
                isPaused = true
            )
            
            // Pause Windows automation if active
            currentState.session?.let { session ->
                windowsAutomationRepository.pauseSession(session.id)
            }
        }
    }

    /**
     * Resume batch processing
     */
    suspend fun resumeBatchProcessing() {
        val currentState = _batchProcessingState.value
        if (currentState.isPaused) {
            _batchProcessingState.value = currentState.copy(
                isProcessing = true,
                isPaused = false
            )
            
            // Resume Windows automation if active
            currentState.session?.let { session ->
                windowsAutomationRepository.resumeSession(session.id)
            }
        }
    }

    /**
     * Cancel batch processing
     */
    suspend fun cancelBatchProcessing() {
        val currentState = _batchProcessingState.value
        _batchProcessingState.value = currentState.copy(
            isProcessing = false,
            isCancelled = true
        )
        
        // Cancel Windows automation if active
        currentState.session?.let { session ->
            windowsAutomationRepository.cancelSession(session.id)
        }
    }

    /**
     * Calculate processing metrics from multi-drug results
     */
    private fun calculateProcessingMetrics(results: List<MultiDrugResult>): MultiDrugProcessingMetrics {
        return MultiDrugProcessingMetrics(
            totalProcessingTime = results.sumOf { it.processingTimeMs.toLong() },
            averageProcessingTime = results.map { it.processingTimeMs }.average().toLong(),
            averageConfidence = results.map { it.confidence }.average().toFloat(),
            visualMatchCount = results.count { it.hasVisualMatch },
            correctionCount = results.count { it.isCorrected },
            verificationCount = results.count { it.isVerified },
            recoveryCount = results.count { it.recoveryMethod.isNotEmpty() }
        )
    }

    /**
     * Calculate quality metrics from multi-drug results
     */
    private fun calculateQualityMetrics(results: List<MultiDrugResult>): MultiDrugQualityMetrics {
        return MultiDrugQualityMetrics(
            averageConfidence = results.map { it.confidence }.average().toFloat(),
            highConfidenceCount = results.count { it.confidence >= 0.8f },
            mediumConfidenceCount = results.count { it.confidence >= 0.6f && it.confidence < 0.8f },
            lowConfidenceCount = results.count { it.confidence < 0.6f },
            visualMatchRate = results.count { it.hasVisualMatch }.toFloat() / results.size,
            correctionRate = results.count { it.isCorrected }.toFloat() / results.size,
            verificationRate = results.count { it.isVerified }.toFloat() / results.size
        )
    }

    /**
     * Get current batch state
     */
    fun getCurrentBatchState(): MultiDrugBatchState {
        return _batchProcessingState.value
    }

    /**
     * Get current session progress
     */
    fun getCurrentSessionProgress(): BatchSessionProgress {
        return _sessionProgress.value
    }

    /**
     * Clear current batch state
     */
    fun clearBatchState() {
        _batchProcessingState.value = MultiDrugBatchState()
        _sessionProgress.value = BatchSessionProgress()
    }
}

// Data classes for multi-drug batch integration

data class MultiDrugBatchState(
    val session: BatchSession? = null,
    val drugResults: List<MultiDrugResult> = emptyList(),
    val currentIndex: Int = 0,
    val currentDrug: MultiDrugResult? = null,
    val isProcessing: Boolean = false,
    val isPaused: Boolean = false,
    val isCancelled: Boolean = false,
    val isCompleted: Boolean = false,
    val processingStage: String = "",
    val processingStartTime: Long? = null,
    val processingEndTime: Long? = null,
    val completedDrugs: List<CompletedDrugProcessing> = emptyList(),
    val failedDrugs: List<FailedDrugProcessing> = emptyList(),
    val batchSummary: BatchProcessingSummary? = null,
    val error: String? = null
)

data class BatchSessionProgress(
    val sessionId: String = "",
    val totalDrugs: Int = 0,
    val currentDrugIndex: Int = 0,
    val currentDrugName: String = "",
    val currentStage: String = "",
    val completedCount: Int = 0,
    val failedCount: Int = 0,
    val progress: Float = 0f,
    val isActive: Boolean = false,
    val isCompleted: Boolean = false,
    val startTime: Long = 0L,
    val endTime: Long = 0L,
    val estimatedTimeRemaining: Long = 0L
)

data class CompletedDrugProcessing(
    val drugResult: MultiDrugResult,
    val processingTime: Long,
    val windowsData: WindowsAutomationData?,
    val completedAt: Long
)

data class FailedDrugProcessing(
    val drugResult: MultiDrugResult,
    val error: String,
    val failedAt: Long
)

data class SingleDrugProcessingResult(
    val success: Boolean,
    val processingTime: Long,
    val windowsData: WindowsAutomationData? = null,
    val drugData: WindowsDrugData? = null,
    val error: String? = null
)

data class WindowsDrugData(
    val name: String,
    val confidence: Float,
    val ocrText: String,
    val isVerified: Boolean,
    val isCorrected: Boolean,
    val matchedInfo: TurkishDrugInfo?,
    val visualMatch: Boolean,
    val recoveryMethod: String,
    val sourceImage: Bitmap?,
    val processingMetrics: DrugProcessingMetrics
)

data class DrugProcessingMetrics(
    val ocrTime: Long,
    val visualMatchTime: Long,
    val totalProcessingTime: Long
)

data class BatchProcessingSummary(
    val sessionId: String,
    val totalDrugs: Int,
    val completedCount: Int,
    val failedCount: Int,
    val totalProcessingTime: Long,
    val averageProcessingTime: Long,
    val windowsIntegration: Boolean,
    val completedDrugs: List<CompletedDrugProcessing>,
    val failedDrugs: List<FailedDrugProcessing>,
    val completedAt: Long
)

data class MultiDrugProcessingMetrics(
    val totalProcessingTime: Long,
    val averageProcessingTime: Long,
    val averageConfidence: Float,
    val visualMatchCount: Int,
    val correctionCount: Int,
    val verificationCount: Int,
    val recoveryCount: Int
)

data class MultiDrugQualityMetrics(
    val averageConfidence: Float,
    val highConfidenceCount: Int,
    val mediumConfidenceCount: Int,
    val lowConfidenceCount: Int,
    val visualMatchRate: Float,
    val correctionRate: Float,
    val verificationRate: Float
)

data class MultiDrugSourceData(
    val originalResults: List<MultiDrugResult>,
    val processingMetrics: MultiDrugProcessingMetrics,
    val qualityMetrics: MultiDrugQualityMetrics
)

data class BatchDrugMetadata(
    val multiDrugSource: Boolean,
    val originalOcrText: String,
    val visualMatch: Boolean,
    val isVerified: Boolean,
    val isCorrected: Boolean,
    val processingTime: Long
)
