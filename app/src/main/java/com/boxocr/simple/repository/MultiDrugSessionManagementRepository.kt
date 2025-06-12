package com.boxocr.simple.repository

import android.content.Context
import android.graphics.Bitmap
import com.boxocr.simple.data.*
import com.boxocr.simple.database.BoxOCRDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Multi-Drug Session Management Repository
 * Comprehensive session lifecycle management for multi-drug prescription workflows
 */

@Singleton
class MultiDrugSessionManagementRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: BoxOCRDatabase,
    private val multiDrugScannerRepository: MultiDrugScannerRepository,
    private val batchIntegrationRepository: MultiDrugBatchIntegrationRepository,
    private val windowsAutomationRepository: EnhancedWindowsAutomationRepository,
    private val exportRepository: ExportRepository
) {

    private val _activeSessions = MutableStateFlow<Map<String, MultiDrugSession>>(emptyMap())
    val activeSessions: StateFlow<Map<String, MultiDrugSession>> = _activeSessions.asStateFlow()

    private val _currentSession = MutableStateFlow<MultiDrugSession?>(null)
    val currentSession: StateFlow<MultiDrugSession?> = _currentSession.asStateFlow()

    private val _sessionMetrics = MutableStateFlow(SessionMetrics())
    val sessionMetrics: StateFlow<SessionMetrics> = _sessionMetrics.asStateFlow()

    private val sessionMutex = Mutex()

    /**
     * Create a new multi-drug session
     */
    suspend fun createSession(
        sessionName: String = "Multi-Drug Session ${System.currentTimeMillis()}",
        patientInfo: PatientInfo? = null,
        sessionConfig: MultiDrugSessionConfig = MultiDrugSessionConfig()
    ): MultiDrugSession = sessionMutex.withLock {
        try {
            val sessionId = "session_${System.currentTimeMillis()}"
            
            val session = MultiDrugSession(
                sessionId = sessionId,
                sessionName = sessionName,
                patientInfo = patientInfo,
                config = sessionConfig,
                status = SessionStatus.CREATED,
                createdAt = System.currentTimeMillis(),
                lastUpdated = System.currentTimeMillis()
            )

            // Save to database
            database.multiDrugSessionDao().insertSession(session.toEntity())

            // Update active sessions
            val currentSessions = _activeSessions.value.toMutableMap()
            currentSessions[sessionId] = session
            _activeSessions.value = currentSessions

            // Set as current session
            _currentSession.value = session

            // Update metrics
            updateSessionMetrics()

            return session

        } catch (e: Exception) {
            throw Exception("Failed to create session: ${e.message}")
        }
    }

    /**
     * Start scanning phase of the session
     */
    suspend fun startScanningPhase(
        sessionId: String,
        scanConfig: ScanningConfig = ScanningConfig()
    ): ScanningPhase = sessionMutex.withLock {
        try {
            val session = getSessionById(sessionId)
                ?: throw IllegalArgumentException("Session not found: $sessionId")

            if (session.status != SessionStatus.CREATED) {
                throw IllegalStateException("Session must be in CREATED status to start scanning")
            }

            val scanningPhase = ScanningPhase(
                sessionId = sessionId,
                config = scanConfig,
                startTime = System.currentTimeMillis(),
                status = ScanningStatus.ACTIVE
            )

            // Update session
            val updatedSession = session.copy(
                status = SessionStatus.SCANNING,
                scanningPhase = scanningPhase,
                lastUpdated = System.currentTimeMillis()
            )

            updateSession(updatedSession)

            // Initialize multi-drug scanner for this session
            multiDrugScannerRepository.initializeForSession(sessionId, scanConfig)

            return scanningPhase

        } catch (e: Exception) {
            throw Exception("Failed to start scanning phase: ${e.message}")
        }
    }

    /**
     * Add scanning results to the session
     */
    suspend fun addScanningResults(
        sessionId: String,
        results: List<MultiDrugResult>
    ) = sessionMutex.withLock {
        try {
            val session = getSessionById(sessionId)
                ?: throw IllegalArgumentException("Session not found: $sessionId")

            val scanningPhase = session.scanningPhase
                ?: throw IllegalStateException("No active scanning phase")

            // Add results to scanning phase
            val updatedResults = scanningPhase.results + results
            val updatedScanningPhase = scanningPhase.copy(
                results = updatedResults,
                totalDrugsDetected = updatedResults.size,
                lastScanTime = System.currentTimeMillis()
            )

            // Update session
            val updatedSession = session.copy(
                scanningPhase = updatedScanningPhase,
                lastUpdated = System.currentTimeMillis()
            )

            updateSession(updatedSession)

            // Update session metrics
            updateSessionMetrics()

        } catch (e: Exception) {
            throw Exception("Failed to add scanning results: ${e.message}")
        }
    }

    /**
     * Complete scanning phase and move to verification
     */
    suspend fun completeScanningPhase(sessionId: String): VerificationPhase = sessionMutex.withLock {
        try {
            val session = getSessionById(sessionId)
                ?: throw IllegalArgumentException("Session not found: $sessionId")

            val scanningPhase = session.scanningPhase
                ?: throw IllegalStateException("No active scanning phase")

            if (scanningPhase.results.isEmpty()) {
                throw IllegalStateException("Cannot complete scanning phase with no results")
            }

            // Create verification phase
            val verificationPhase = VerificationPhase(
                sessionId = sessionId,
                results = scanningPhase.results,
                totalDrugs = scanningPhase.results.size,
                verifiedCount = scanningPhase.results.count { it.isVerified },
                unverifiedCount = scanningPhase.results.count { !it.isVerified },
                startTime = System.currentTimeMillis(),
                status = VerificationStatus.ACTIVE
            )

            // Update session
            val updatedSession = session.copy(
                status = SessionStatus.VERIFICATION,
                scanningPhase = scanningPhase.copy(
                    status = ScanningStatus.COMPLETED,
                    endTime = System.currentTimeMillis()
                ),
                verificationPhase = verificationPhase,
                lastUpdated = System.currentTimeMillis()
            )

            updateSession(updatedSession)
            return verificationPhase

        } catch (e: Exception) {
            throw Exception("Failed to complete scanning phase: ${e.message}")
        }
    }

    /**
     * Update verification results
     */
    suspend fun updateVerificationResults(
        sessionId: String,
        updatedResults: List<MultiDrugResult>
    ) = sessionMutex.withLock {
        try {
            val session = getSessionById(sessionId)
                ?: throw IllegalArgumentException("Session not found: $sessionId")

            val verificationPhase = session.verificationPhase
                ?: throw IllegalStateException("No active verification phase")

            val updatedVerificationPhase = verificationPhase.copy(
                results = updatedResults,
                verifiedCount = updatedResults.count { it.isVerified },
                unverifiedCount = updatedResults.count { !it.isVerified },
                lastUpdateTime = System.currentTimeMillis()
            )

            // Update session
            val updatedSession = session.copy(
                verificationPhase = updatedVerificationPhase,
                lastUpdated = System.currentTimeMillis()
            )

            updateSession(updatedSession)

        } catch (e: Exception) {
            throw Exception("Failed to update verification results: ${e.message}")
        }
    }

    /**
     * Complete verification and start batch processing
     */
    suspend fun startBatchProcessing(
        sessionId: String,
        processingConfig: BatchProcessingConfig = BatchProcessingConfig()
    ): BatchProcessingPhase = sessionMutex.withLock {
        try {
            val session = getSessionById(sessionId)
                ?: throw IllegalArgumentException("Session not found: $sessionId")

            val verificationPhase = session.verificationPhase
                ?: throw IllegalStateException("No verification phase found")

            val verifiedResults = verificationPhase.results.filter { it.isVerified }
            if (verifiedResults.isEmpty()) {
                throw IllegalStateException("No verified results to process")
            }

            // Create batch processing phase
            val batchProcessingPhase = BatchProcessingPhase(
                sessionId = sessionId,
                verifiedResults = verifiedResults,
                totalDrugs = verifiedResults.size,
                config = processingConfig,
                startTime = System.currentTimeMillis(),
                status = BatchProcessingStatus.INITIALIZING
            )

            // Update session
            val updatedSession = session.copy(
                status = SessionStatus.BATCH_PROCESSING,
                verificationPhase = verificationPhase.copy(
                    status = VerificationStatus.COMPLETED,
                    endTime = System.currentTimeMillis()
                ),
                batchProcessingPhase = batchProcessingPhase,
                lastUpdated = System.currentTimeMillis()
            )

            updateSession(updatedSession)

            // Initialize batch processing
            initializeBatchProcessing(sessionId, batchProcessingPhase)

            return batchProcessingPhase

        } catch (e: Exception) {
            throw Exception("Failed to start batch processing: ${e.message}")
        }
    }

    /**
     * Initialize batch processing with Windows automation
     */
    private suspend fun initializeBatchProcessing(
        sessionId: String,
        batchPhase: BatchProcessingPhase
    ) {
        try {
            // Create batch session
            val batchSession = batchIntegrationRepository.createBatchFromMultiDrugResults(
                multiDrugResults = batchPhase.verifiedResults,
                sessionName = "Batch for Session $sessionId"
            )

            // Initialize Windows automation if enabled
            if (batchPhase.config.windowsIntegration) {
                windowsAutomationRepository.initializeMultiDrugSession(
                    sessionId = sessionId,
                    drugResults = batchPhase.verifiedResults,
                    automationConfig = batchPhase.config.windowsConfig
                )
            }

            // Update batch phase with initialized data
            val updatedBatchPhase = batchPhase.copy(
                batchSessionId = batchSession.id,
                status = BatchProcessingStatus.READY,
                windowsSessionId = if (batchPhase.config.windowsIntegration) sessionId else null
            )

            // Update session
            val session = getSessionById(sessionId)!!
            val updatedSession = session.copy(
                batchProcessingPhase = updatedBatchPhase,
                lastUpdated = System.currentTimeMillis()
            )

            updateSession(updatedSession)

            // Start batch processing
            if (batchPhase.config.autoStart) {
                startAutomaticBatchProcessing(sessionId)
            }

        } catch (e: Exception) {
            // Update session with error
            val session = getSessionById(sessionId)!!
            val errorBatchPhase = batchPhase.copy(
                status = BatchProcessingStatus.ERROR,
                error = "Initialization failed: ${e.message}"
            )
            
            val updatedSession = session.copy(
                batchProcessingPhase = errorBatchPhase,
                lastUpdated = System.currentTimeMillis()
            )
            
            updateSession(updatedSession)
            throw e
        }
    }

    /**
     * Start automatic batch processing
     */
    private suspend fun startAutomaticBatchProcessing(sessionId: String) {
        try {
            val session = getSessionById(sessionId)!!
            val batchPhase = session.batchProcessingPhase!!

            // Update status to processing
            val processingBatchPhase = batchPhase.copy(
                status = BatchProcessingStatus.PROCESSING,
                processingStartTime = System.currentTimeMillis()
            )

            updateSession(session.copy(
                batchProcessingPhase = processingBatchPhase,
                lastUpdated = System.currentTimeMillis()
            ))

            // Start batch integration processing
            batchIntegrationRepository.startBatchProcessing(
                sessionId = batchPhase.batchSessionId!!,
                windowsIntegration = batchPhase.config.windowsIntegration
            )

            // Monitor processing progress
            monitorBatchProcessing(sessionId)

        } catch (e: Exception) {
            // Handle processing error
            val session = getSessionById(sessionId)!!
            val errorBatchPhase = session.batchProcessingPhase!!.copy(
                status = BatchProcessingStatus.ERROR,
                error = "Processing failed: ${e.message}",
                endTime = System.currentTimeMillis()
            )
            
            updateSession(session.copy(
                batchProcessingPhase = errorBatchPhase,
                lastUpdated = System.currentTimeMillis()
            ))
        }
    }

    /**
     * Monitor batch processing progress
     */
    private suspend fun monitorBatchProcessing(sessionId: String) {
        try {
            // Monitor batch processing state
            batchIntegrationRepository.batchProcessingState.collect { batchState ->
                if (batchState.session?.id?.contains(sessionId) == true) {
                    val session = getSessionById(sessionId) ?: return@collect
                    val batchPhase = session.batchProcessingPhase ?: return@collect

                    val updatedBatchPhase = batchPhase.copy(
                        currentDrugIndex = batchState.currentIndex,
                        completedDrugs = batchState.completedDrugs,
                        failedDrugs = batchState.failedDrugs,
                        isProcessing = batchState.isProcessing,
                        progress = if (batchPhase.totalDrugs > 0) {
                            (batchState.completedDrugs.size + batchState.failedDrugs.size).toFloat() / batchPhase.totalDrugs
                        } else 0f
                    )

                    // Check if processing is complete
                    if (batchState.isCompleted) {
                        val completedBatchPhase = updatedBatchPhase.copy(
                            status = BatchProcessingStatus.COMPLETED,
                            endTime = System.currentTimeMillis(),
                            batchSummary = batchState.batchSummary
                        )

                        // Complete the session
                        completeSession(sessionId, completedBatchPhase)
                    } else {
                        updateSession(session.copy(
                            batchProcessingPhase = updatedBatchPhase,
                            lastUpdated = System.currentTimeMillis()
                        ))
                    }
                }
            }
        } catch (e: Exception) {
            // Handle monitoring error
        }
    }

    /**
     * Complete the entire session
     */
    private suspend fun completeSession(
        sessionId: String,
        completedBatchPhase: BatchProcessingPhase
    ) = sessionMutex.withLock {
        try {
            val session = getSessionById(sessionId)!!
            
            // Generate session summary
            val sessionSummary = generateSessionSummary(session, completedBatchPhase)
            
            val completedSession = session.copy(
                status = SessionStatus.COMPLETED,
                batchProcessingPhase = completedBatchPhase,
                sessionSummary = sessionSummary,
                completedAt = System.currentTimeMillis(),
                lastUpdated = System.currentTimeMillis()
            )

            updateSession(completedSession)

            // Export session data if configured
            if (session.config.autoExport) {
                exportSessionData(sessionId)
            }

            // Clean up resources
            cleanupSessionResources(sessionId)

            // Update metrics
            updateSessionMetrics()

        } catch (e: Exception) {
            throw Exception("Failed to complete session: ${e.message}")
        }
    }

    /**
     * Generate comprehensive session summary
     */
    private fun generateSessionSummary(
        session: MultiDrugSession,
        batchPhase: BatchProcessingPhase
    ): SessionSummary {
        val totalTime = System.currentTimeMillis() - session.createdAt
        val scanningTime = session.scanningPhase?.let { 
            (it.endTime ?: System.currentTimeMillis()) - it.startTime 
        } ?: 0L
        val verificationTime = session.verificationPhase?.let {
            (it.endTime ?: System.currentTimeMillis()) - it.startTime
        } ?: 0L
        val batchProcessingTime = batchPhase.endTime?.let { 
            it - batchPhase.startTime 
        } ?: 0L

        return SessionSummary(
            sessionId = session.sessionId,
            sessionName = session.sessionName,
            totalDrugs = session.scanningPhase?.totalDrugsDetected ?: 0,
            verifiedDrugs = session.verificationPhase?.verifiedCount ?: 0,
            processedDrugs = batchPhase.completedDrugs.size,
            failedDrugs = batchPhase.failedDrugs.size,
            totalProcessingTime = totalTime,
            scanningTime = scanningTime,
            verificationTime = verificationTime,
            batchProcessingTime = batchProcessingTime,
            averageConfidence = session.verificationPhase?.results?.map { it.confidence }?.average()?.toFloat() ?: 0f,
            successRate = if (batchPhase.totalDrugs > 0) {
                batchPhase.completedDrugs.size.toFloat() / batchPhase.totalDrugs
            } else 0f,
            windowsIntegration = batchPhase.config.windowsIntegration,
            completedAt = System.currentTimeMillis()
        )
    }

    /**
     * Export session data
     */
    private suspend fun exportSessionData(sessionId: String) {
        try {
            val session = getSessionById(sessionId) ?: return
            
            val exportData = SessionExportData(
                session = session,
                scanningResults = session.scanningPhase?.results ?: emptyList(),
                verificationResults = session.verificationPhase?.results ?: emptyList(),
                batchResults = session.batchProcessingPhase?.completedDrugs ?: emptyList(),
                sessionSummary = session.sessionSummary
            )

            exportRepository.exportSessionData(exportData)
            
        } catch (e: Exception) {
            // Log export error but don't fail session completion
        }
    }

    /**
     * Clean up session resources
     */
    private suspend fun cleanupSessionResources(sessionId: String) {
        try {
            // Clean up temporary files
            multiDrugScannerRepository.cleanupSession(sessionId)
            
            // Clear Windows automation resources
            windowsAutomationRepository.clearSessionState()
            
            // Clear batch integration resources
            batchIntegrationRepository.clearBatchState()
            
        } catch (e: Exception) {
            // Log cleanup error but don't fail
        }
    }

    /**
     * Get session by ID
     */
    private fun getSessionById(sessionId: String): MultiDrugSession? {
        return _activeSessions.value[sessionId]
    }

    /**
     * Update session in state and database
     */
    private suspend fun updateSession(session: MultiDrugSession) {
        // Update in-memory state
        val currentSessions = _activeSessions.value.toMutableMap()
        currentSessions[session.sessionId] = session
        _activeSessions.value = currentSessions

        // Update current session if it matches
        if (_currentSession.value?.sessionId == session.sessionId) {
            _currentSession.value = session
        }

        // Update database
        database.multiDrugSessionDao().updateSession(session.toEntity())
    }

    /**
     * Update session metrics
     */
    private suspend fun updateSessionMetrics() {
        val sessions = _activeSessions.value.values
        val activeSessions = sessions.filter { it.status != SessionStatus.COMPLETED }
        val completedSessions = sessions.filter { it.status == SessionStatus.COMPLETED }

        _sessionMetrics.value = SessionMetrics(
            totalSessions = sessions.size,
            activeSessions = activeSessions.size,
            completedSessions = completedSessions.size,
            totalDrugsProcessed = completedSessions.sumOf { 
                it.batchProcessingPhase?.completedDrugs?.size ?: 0 
            },
            averageProcessingTime = if (completedSessions.isNotEmpty()) {
                completedSessions.mapNotNull { it.sessionSummary?.totalProcessingTime }.average().toLong()
            } else 0L,
            averageSuccessRate = if (completedSessions.isNotEmpty()) {
                completedSessions.mapNotNull { it.sessionSummary?.successRate }.average().toFloat()
            } else 0f
        )
    }

    /**
     * Get all sessions
     */
    fun getAllSessions(): List<MultiDrugSession> {
        return _activeSessions.value.values.toList()
    }

    /**
     * Get session by ID (public)
     */
    fun getSession(sessionId: String): MultiDrugSession? {
        return _activeSessions.value[sessionId]
    }

    /**
     * Delete session
     */
    suspend fun deleteSession(sessionId: String) = sessionMutex.withLock {
        try {
            // Clean up resources first
            cleanupSessionResources(sessionId)
            
            // Remove from database
            database.multiDrugSessionDao().deleteSession(sessionId)
            
            // Remove from state
            val currentSessions = _activeSessions.value.toMutableMap()
            currentSessions.remove(sessionId)
            _activeSessions.value = currentSessions
            
            // Clear current session if it matches
            if (_currentSession.value?.sessionId == sessionId) {
                _currentSession.value = null
            }
            
            // Update metrics
            updateSessionMetrics()
            
        } catch (e: Exception) {
            throw Exception("Failed to delete session: ${e.message}")
        }
    }

    /**
     * Load sessions from database on initialization
     */
    suspend fun loadSessionsFromDatabase() {
        try {
            val sessionEntities = database.multiDrugSessionDao().getAllSessions()
            val sessions = sessionEntities.map { it.toModel() }
            
            val sessionMap = sessions.associateBy { it.sessionId }
            _activeSessions.value = sessionMap
            
            // Set most recent active session as current
            val activeSession = sessions
                .filter { it.status != SessionStatus.COMPLETED }
                .maxByOrNull { it.lastUpdated }
            
            _currentSession.value = activeSession
            
            // Update metrics
            updateSessionMetrics()
            
        } catch (e: Exception) {
            // Handle database load error gracefully
        }
    }
}

// Extension functions for entity conversion
fun MultiDrugSession.toEntity(): MultiDrugSessionEntity {
    // Implementation for converting session to database entity
    return MultiDrugSessionEntity(
        sessionId = this.sessionId,
        sessionName = this.sessionName,
        status = this.status.name,
        createdAt = this.createdAt,
        lastUpdated = this.lastUpdated,
        completedAt = this.completedAt
        // Additional fields as needed
    )
}

fun MultiDrugSessionEntity.toModel(): MultiDrugSession {
    // Implementation for converting database entity to session model
    return MultiDrugSession(
        sessionId = this.sessionId,
        sessionName = this.sessionName,
        status = SessionStatus.valueOf(this.status),
        createdAt = this.createdAt,
        lastUpdated = this.lastUpdated,
        completedAt = this.completedAt
        // Additional fields as needed
    )
}
