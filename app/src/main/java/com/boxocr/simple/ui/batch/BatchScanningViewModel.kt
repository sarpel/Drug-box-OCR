package com.boxocr.simple.ui.batch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boxocr.simple.automation.PrescriptionSession
import com.boxocr.simple.automation.WindowsAutomationServer
import com.boxocr.simple.repository.EnhancedDatabaseRepository
import com.boxocr.simple.repository.OCRRepository
import com.boxocr.simple.repository.ScanHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for batch scanning workflow
 * Handles multiple drug scanning in sequence for prescription automation
 */
@HiltViewModel
class BatchScanningViewModel @Inject constructor(
    private val ocrRepository: OCRRepository,
    private val enhancedDatabaseRepository: EnhancedDatabaseRepository,
    private val historyRepository: ScanHistoryRepository,
    private val automationServer: WindowsAutomationServer
) : ViewModel() {
    
    // Current scanning state
    private val _uiState = MutableStateFlow(BatchScanningUiState())
    val uiState: StateFlow<BatchScanningUiState> = _uiState.asStateFlow()
    
    // Scanned drugs in current session
    private val _scannedDrugs = MutableStateFlow<List<ScannedDrug>>(emptyList())
    val scannedDrugs: StateFlow<List<ScannedDrug>> = _scannedDrugs.asStateFlow()
    
    // Current prescription session
    val currentSession: StateFlow<PrescriptionSession?> = automationServer.currentSession
    
    // Automation server status
    val serverStatus: StateFlow<WindowsAutomationServer.ServerStatus> = automationServer.serverStatus
    
    data class BatchScanningUiState(
        val isScanning: Boolean = false,
        val currentScanIndex: Int = 0,
        val isProcessing: Boolean = false,
        val showConfirmDialog: Boolean = false,
        val showEnhancedMatching: Boolean = false,
        val lastScannedText: String = "",
        val lastMatchedDrug: String = "",
        val matchConfidence: Int = 0,
        val currentMatchResult: EnhancedDatabaseRepository.MultipleMatchResult? = null,
        val error: String = "",
        val isSessionActive: Boolean = false,
        val sessionInfo: String = ""
    )
    
    data class ScannedDrug(
        val index: Int,
        val scannedText: String,
        val matchedDrug: String,
        val confidence: Int,
        val timestamp: Long = System.currentTimeMillis(),
        val isConfirmed: Boolean = false
    ) {
        fun getFormattedTime(): String {
            val now = System.currentTimeMillis()
            val diff = (now - timestamp) / 1000
            return when {
                diff < 60 -> "${diff}s ago"
                diff < 3600 -> "${diff / 60}m ago"
                else -> "${diff / 3600}h ago"
            }
        }
    }
    
    init {
        // Observe session changes
        viewModelScope.launch {
            currentSession.collect { session ->
                _uiState.value = _uiState.value.copy(
                    isSessionActive = session != null,
                    sessionInfo = session?.let { 
                        "Session ${it.sessionId.takeLast(4)} - ${it.drugs.size} drugs - ${it.getFormattedDuration()}"
                    } ?: ""
                )
            }
        }
    }
    
    /**
     * Start new prescription session
     */
    fun startPrescriptionSession(patientInfo: String = "") {
        viewModelScope.launch {
            try {
                // Start automation server if not running
                if (serverStatus.value is WindowsAutomationServer.ServerStatus.Stopped) {
                    automationServer.startServer()
                }
                
                // Start new session
                val sessionId = automationServer.startPrescriptionSession(patientInfo)
                
                // Clear previous scans
                _scannedDrugs.value = emptyList()
                _uiState.value = _uiState.value.copy(
                    currentScanIndex = 0,
                    error = "",
                    isSessionActive = true
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Failed to start session: ${e.message}")
            }
        }
    }
    
    /**
     * Process scanned image for OCR and enhanced database matching
     */
    fun processScannedImage(imageBytes: ByteArray) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true, error = "")
            
            try {
                // OCR processing
                val ocrResult = ocrRepository.processImage(imageBytes)
                
                if (ocrResult.isSuccess && ocrResult.extractedText.isNotBlank()) {
                    val extractedText = ocrResult.extractedText
                    
                    // Enhanced database matching
                    val matchResult = enhancedDatabaseRepository.findMultipleMatches(extractedText)
                    
                    when (matchResult.recommendedAction) {
                        EnhancedDatabaseRepository.RecommendedAction.AUTO_SELECT -> {
                            // High confidence - auto-confirm primary match
                            val primaryMatch = matchResult.primaryMatch!!
                            _uiState.value = _uiState.value.copy(
                                isProcessing = false,
                                showConfirmDialog = true,
                                lastScannedText = extractedText,
                                lastMatchedDrug = primaryMatch.drugName,
                                matchConfidence = primaryMatch.confidence
                            )
                        }
                        
                        EnhancedDatabaseRepository.RecommendedAction.SHOW_OPTIONS -> {
                            // Medium confidence - show enhanced matching screen
                            _uiState.value = _uiState.value.copy(
                                isProcessing = false,
                                showEnhancedMatching = true,
                                lastScannedText = extractedText,
                                currentMatchResult = matchResult
                            )
                        }
                        
                        EnhancedDatabaseRepository.RecommendedAction.MANUAL_ENTRY,
                        EnhancedDatabaseRepository.RecommendedAction.RESCAN -> {
                            // Low confidence - show enhanced matching for manual review
                            _uiState.value = _uiState.value.copy(
                                isProcessing = false,
                                showEnhancedMatching = true,
                                lastScannedText = extractedText,
                                currentMatchResult = matchResult
                            )
                        }
                    }
                    
                } else {
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        error = ocrResult.error ?: "Failed to extract text from image"
                    )
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    error = "Processing failed: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Confirm scanned drug and add to prescription
     */
    fun confirmScannedDrug() {
        val state = _uiState.value
        if (state.lastMatchedDrug.isNotBlank()) {
            addDrugToPrescription(state.lastMatchedDrug, state.lastScannedText, state.matchConfidence)
            
            // Update UI state
            _uiState.value = _uiState.value.copy(
                showConfirmDialog = false,
                currentScanIndex = state.currentScanIndex + 1,
                lastScannedText = "",
                lastMatchedDrug = "",
                matchConfidence = 0
            )
        }
    }
    
    /**
     * Handle drug confirmed from enhanced matching screen
     */
    fun confirmDrugFromEnhancedMatching(drugName: String) {
        val state = _uiState.value
        addDrugToPrescription(drugName, state.lastScannedText, 100) // Full confidence since manually confirmed
        
        _uiState.value = _uiState.value.copy(
            showEnhancedMatching = false,
            currentScanIndex = state.currentScanIndex + 1,
            lastScannedText = "",
            currentMatchResult = null
        )
    }
    
    /**
     * Close enhanced matching screen
     */
    fun closeEnhancedMatching() {
        _uiState.value = _uiState.value.copy(
            showEnhancedMatching = false,
            lastScannedText = "",
            currentMatchResult = null
        )
    }
    
    /**
     * Add drug to prescription with details
     */
    private fun addDrugToPrescription(drugName: String, scannedText: String, confidence: Int) {
        // Create scanned drug entry
        val scannedDrug = ScannedDrug(
            index = _uiState.value.currentScanIndex + 1,
            scannedText = scannedText,
            matchedDrug = drugName,
            confidence = confidence,
            isConfirmed = true
        )
        
        // Add to scanned drugs list
        val updatedList = _scannedDrugs.value.toMutableList()
        updatedList.add(scannedDrug)
        _scannedDrugs.value = updatedList
        
        // Add to prescription session
        automationServer.addDrugToPrescription(drugName)
        
        // Add to history
        historyRepository.addScanResult(drugName)
    }
    
    /**
     * Load database into enhanced repository
     */
    fun loadDatabase(items: List<String>) {
        enhancedDatabaseRepository.loadDatabase(items)
    }
    
    /**
     * Reject scanned drug and continue scanning
     */
    fun rejectScannedDrug() {
        _uiState.value = _uiState.value.copy(
            showConfirmDialog = false,
            lastScannedText = "",
            lastMatchedDrug = "",
            matchConfidence = 0,
            error = "Drug rejected, scan next item"
        )
    }
    
    /**
     * Remove drug from prescription
     */
    fun removeDrug(index: Int) {
        val updatedList = _scannedDrugs.value.toMutableList()
        if (index < updatedList.size) {
            updatedList.removeAt(index)
            // Reindex remaining items
            updatedList.forEachIndexed { newIndex, drug ->
                updatedList[newIndex] = drug.copy(index = newIndex + 1)
            }
            _scannedDrugs.value = updatedList
            
            // Update current scan index
            _uiState.value = _uiState.value.copy(currentScanIndex = updatedList.size)
        }
    }
    
    /**
     * Complete prescription session and prepare for Windows automation
     */
    fun completePrescriptionSession() {
        viewModelScope.launch {
            try {
                val session = automationServer.completePrescriptionSession()
                if (session != null) {
                    _uiState.value = _uiState.value.copy(
                        isSessionActive = false,
                        sessionInfo = "Session completed with ${session.drugs.size} drugs"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(error = "No active session to complete")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Failed to complete session: ${e.message}")
            }
        }
    }
    
    /**
     * Send drugs to Windows for automation
     */
    fun sendToWindows() {
        if (_scannedDrugs.value.isNotEmpty()) {
            val drugs = _scannedDrugs.value.map { it.matchedDrug }
            // The Windows app will call the HTTP API to get the drugs
            _uiState.value = _uiState.value.copy(
                sessionInfo = "Ready for Windows automation - ${drugs.size} drugs queued"
            )
        }
    }
    
    /**
     * Clear current session and start over
     */
    fun clearSession() {
        viewModelScope.launch {
            automationServer.completePrescriptionSession()
            _scannedDrugs.value = emptyList()
            _uiState.value = BatchScanningUiState()
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = "")
    }
    
    /**
     * Get server IP and port for Windows connection
     */
    fun getServerInfo(): String {
        return when (val status = serverStatus.value) {
            is WindowsAutomationServer.ServerStatus.Running -> 
                "Server: ${status.ipAddress}:${status.port}"
            is WindowsAutomationServer.ServerStatus.Starting -> 
                "Starting server on port ${status.port}..."
            is WindowsAutomationServer.ServerStatus.Error -> 
                "Server error: ${status.message}"
            is WindowsAutomationServer.ServerStatus.Stopped -> 
                "Server stopped"
        }
    }
}
