package com.boxocr.simple.repository

import android.content.Context
import android.content.SharedPreferences
import com.boxocr.simple.data.*
import com.boxocr.simple.ui.recovery.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Error Recovery Repository - Phase 3 Quality of Life Feature
 * 
 * Manages undo operations, session recovery, auto-save, and error handling
 */
@Singleton
class ErrorRecoveryRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sharedPrefs: SharedPreferences = context.getSharedPreferences(
        "error_recovery", Context.MODE_PRIVATE
    )
    
    // State flows for recovery features
    private val _undoState = MutableStateFlow(UndoState())
    val undoState: StateFlow<UndoState> = _undoState.asStateFlow()
    
    private val _autoSaveState = MutableStateFlow(AutoSaveState())
    val autoSaveState: StateFlow<AutoSaveState> = _autoSaveState.asStateFlow()
    
    private val _backupState = MutableStateFlow(BackupState())
    val backupState: StateFlow<BackupState> = _backupState.asStateFlow()
    
    // Undo stack for reversible operations
    private val undoStack = mutableListOf<UndoableAction>()
    private val maxUndoStackSize = 10
    
    // Auto-save timer
    private var autoSaveTimer: Timer? = null
    private val autoSaveInterval = 30_000L // 30 seconds
    
    init {
        loadBackupState()
        startAutoSaveTimer()
    }
    
    /**
     * Add an undoable action to the stack
     */
    suspend fun addUndoableAction(action: UndoableAction) {
        undoStack.add(0, action) // Add to beginning
        
        // Limit stack size
        if (undoStack.size > maxUndoStackSize) {
            undoStack.removeAt(undoStack.size - 1)
        }
        
        // Show undo notification
        showUndoNotification(action)
    }
    
    /**
     * Execute undo operation
     */
    suspend fun executeUndo(): Boolean {
        if (undoStack.isEmpty()) return false
        
        val action = undoStack.removeAt(0)
        
        return try {
            action.undo()
            hideUndoNotification()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Check if undo is available
     */
    fun canUndo(): Boolean = undoStack.isNotEmpty()
    
    /**
     * Get the last undoable action description
     */
    fun getLastActionDescription(): String {
        return undoStack.firstOrNull()?.description ?: ""
    }
    
    /**
     * Show undo notification with auto-dismiss timer
     */
    private suspend fun showUndoNotification(action: UndoableAction) {
        _undoState.value = UndoState(
            isVisible = true,
            actionDescription = action.description,
            timeRemaining = 5,
            actionType = action.actionType
        )
        
        // Start countdown timer
        repeat(5) { i ->
            delay(1000)
            _undoState.value = _undoState.value.copy(timeRemaining = 4 - i)
        }
        
        // Auto-hide after 5 seconds
        hideUndoNotification()
    }
    
    /**
     * Hide undo notification
     */
    fun hideUndoNotification() {
        _undoState.value = UndoState()
    }
    
    /**
     * Clear undo stack
     */
    fun clearUndoStack() {
        undoStack.clear()
        hideUndoNotification()
    }
    
    /**
     * Save session state for recovery
     */
    suspend fun saveSessionState(sessionData: SessionState) {
        try {
            _autoSaveState.value = AutoSaveState(isVisible = true, status = SaveStatus.SAVING)
            
            val sessionJson = Json.encodeToString(sessionData)
            sharedPrefs.edit()
                .putString("session_state", sessionJson)
                .putLong("session_save_time", System.currentTimeMillis())
                .apply()
            
            // Show save success briefly
            _autoSaveState.value = AutoSaveState(isVisible = true, status = SaveStatus.SAVED)
            delay(1500)
            _autoSaveState.value = AutoSaveState()
            
            // Update backup state
            _backupState.value = _backupState.value.copy(
                hasBackup = true,
                lastBackupTime = System.currentTimeMillis()
            )
            
        } catch (e: Exception) {
            _autoSaveState.value = AutoSaveState(isVisible = true, status = SaveStatus.ERROR)
            delay(2000)
            _autoSaveState.value = AutoSaveState()
        }
    }
    
    /**
     * Load saved session state
     */
    suspend fun loadSessionState(): SessionState? {
        return try {
            val sessionJson = sharedPrefs.getString("session_state", null)
            sessionJson?.let { Json.decodeFromString<SessionState>(it) }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Check for incomplete sessions
     */
    suspend fun checkForIncompleteSession(): IncompleteSession? {
        val sessionState = loadSessionState() ?: return null
        
        // Check if session is genuinely incomplete (has data but not completed)
        if (sessionState.isActive && sessionState.scannedDrugs.isNotEmpty()) {
            return IncompleteSession(
                id = sessionState.sessionId,
                startTime = sessionState.startTime,
                scannedDrugs = sessionState.scannedDrugs,
                patientInfo = sessionState.patientInfo ?: ""
            )
        }
        
        return null
    }
    
    /**
     * Clear saved session state
     */
    suspend fun clearSessionState() {
        sharedPrefs.edit()
            .remove("session_state")
            .remove("session_save_time")
            .apply()
        
        _backupState.value = _backupState.value.copy(
            hasBackup = false,
            lastBackupTime = 0
        )
    }
    
    /**
     * Create manual backup
     */
    suspend fun createManualBackup(sessionData: SessionState) {
        _backupState.value = _backupState.value.copy(isWorking = true)
        
        try {
            saveSessionState(sessionData)
            
            // Also save to backup file with timestamp
            val backupJson = Json.encodeToString(sessionData)
            val timestamp = System.currentTimeMillis()
            sharedPrefs.edit()
                .putString("manual_backup_$timestamp", backupJson)
                .apply()
            
            _backupState.value = _backupState.value.copy(
                isWorking = false,
                hasBackup = true,
                lastBackupTime = timestamp
            )
            
        } catch (e: Exception) {
            _backupState.value = _backupState.value.copy(isWorking = false)
        }
    }
    
    /**
     * Restore from backup
     */
    suspend fun restoreFromBackup(): SessionState? {
        _backupState.value = _backupState.value.copy(isWorking = true)
        
        val restored = try {
            loadSessionState()
        } catch (e: Exception) {
            null
        }
        
        _backupState.value = _backupState.value.copy(isWorking = false)
        return restored
    }
    
    /**
     * Get recovery suggestions for error type
     */
    fun getRecoverySuggestions(errorType: RecoveryErrorType): List<RecoverySuggestion> {
        return when (errorType) {
            RecoveryErrorType.CAMERA_ERROR -> listOf(
                RecoverySuggestion(
                    title = "Check Camera Permission",
                    description = "Ensure the app has camera access",
                    icon = Icons.Default.CameraAlt,
                    action = RecoveryAction.CHECK_PERMISSIONS
                ),
                RecoverySuggestion(
                    title = "Restart Camera",
                    description = "Reinitialize camera connection",
                    icon = Icons.Default.Refresh,
                    action = RecoveryAction.RESTART_CAMERA
                )
            )
            
            RecoveryErrorType.OCR_ERROR -> listOf(
                RecoverySuggestion(
                    title = "Retry Scan",
                    description = "Try scanning the drug box again",
                    icon = Icons.Default.Refresh,
                    action = RecoveryAction.RETRY_OCR
                ),
                RecoverySuggestion(
                    title = "Manual Entry",
                    description = "Enter drug name manually",
                    icon = Icons.Default.Edit,
                    action = RecoveryAction.MANUAL_ENTRY
                )
            )
            
            RecoveryErrorType.NETWORK_ERROR -> listOf(
                RecoverySuggestion(
                    title = "Check Internet",
                    description = "Verify your internet connection",
                    icon = Icons.Default.Wifi,
                    action = RecoveryAction.CHECK_NETWORK
                ),
                RecoverySuggestion(
                    title = "Offline Mode",
                    description = "Continue with offline database",
                    icon = Icons.Default.CloudOff,
                    action = RecoveryAction.USE_OFFLINE
                )
            )
            
            RecoveryErrorType.SESSION_ERROR -> listOf(
                RecoverySuggestion(
                    title = "Restore Session",
                    description = "Restore from last auto-save",
                    icon = androidx.compose.material.icons.Icons.Default.Restore,
                    action = RecoveryAction.RESTORE_SESSION
                ),
                RecoverySuggestion(
                    title = "Start New Session",
                    description = "Begin a fresh prescription session",
                    icon = androidx.compose.material.icons.Icons.Default.Add,
                    action = RecoveryAction.NEW_SESSION
                )
            )
        }
    }
    
    /**
     * Start auto-save timer
     */
    private fun startAutoSaveTimer() {
        autoSaveTimer?.cancel()
        autoSaveTimer = Timer().apply {
            scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    // Auto-save would be triggered here
                    // Implementation depends on current session state
                }
            }, autoSaveInterval, autoSaveInterval)
        }
    }
    
    /**
     * Load backup state from preferences
     */
    private fun loadBackupState() {
        val hasBackup = sharedPrefs.contains("session_state")
        val lastBackupTime = sharedPrefs.getLong("session_save_time", 0)
        
        _backupState.value = BackupState(
            hasBackup = hasBackup,
            lastBackupTime = lastBackupTime,
            isWorking = false
        )
    }
    
    /**
     * Stop auto-save timer
     */
    fun stopAutoSave() {
        autoSaveTimer?.cancel()
        autoSaveTimer = null
    }
}

/**
 * Data classes for error recovery
 */
data class UndoableAction(
    val description: String,
    val actionType: UndoAction,
    val undo: suspend () -> Unit
)

data class SessionState(
    val sessionId: String,
    val startTime: Long,
    val isActive: Boolean,
    val scannedDrugs: List<String>,
    val patientInfo: String? = null,
    val lastUpdateTime: Long = System.currentTimeMillis()
)
