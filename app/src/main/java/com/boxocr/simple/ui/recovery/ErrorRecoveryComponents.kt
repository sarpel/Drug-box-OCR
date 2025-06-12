package com.boxocr.simple.ui.recovery

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Error Recovery Components - Phase 3 Quality of Life Feature
 * 
 * Features:
 * 1. Undo last scan functionality
 * 2. Resume incomplete prescriptions
 * 3. Auto-save session state
 * 4. Error recovery suggestions
 * 5. Session backup and restore
 */

/**
 * Undo Action Bar - Shows when actions can be undone
 */
@Composable
fun UndoActionBar(
    modifier: Modifier = Modifier,
    undoState: UndoState,
    onUndo: () -> Unit,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = undoState.isVisible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(300)
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(300)
        ) + fadeOut()
    ) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Undo,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Column {
                        Text(
                            text = undoState.actionDescription,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        if (undoState.timeRemaining > 0) {
                            Text(
                                text = "Auto-dismiss in ${undoState.timeRemaining}s",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onUndo,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("UNDO", fontWeight = FontWeight.Bold)
                    }
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

/**
 * Session Recovery Dialog - Shows when incomplete sessions are found
 */
@Composable
fun SessionRecoveryDialog(
    incompleteSession: IncompleteSession?,
    onRestore: () -> Unit,
    onDiscard: () -> Unit,
    onDismiss: () -> Unit
) {
    incompleteSession?.let { session ->
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Icon(
                    Icons.Default.Restore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text("Incomplete Session Found")
            },
            text = {
                Column {
                    Text("An incomplete prescription session was found:")
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "Started: ${formatTimestamp(session.startTime)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Drugs scanned: ${session.scannedDrugs.size}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            if (session.patientInfo.isNotBlank()) {
                                Text(
                                    text = "Patient: ${session.patientInfo}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Would you like to restore this session?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                Button(onClick = onRestore) {
                    Text("Restore Session")
                }
            },
            dismissButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = onDiscard) {
                        Text("Discard")
                    }
                    TextButton(onClick = onDismiss) {
                        Text("Later")
                    }
                }
            }
        )
    }
}

/**
 * Auto-Save Indicator - Shows when session is being auto-saved
 */
@Composable
fun AutoSaveIndicator(
    modifier: Modifier = Modifier,
    saveState: AutoSaveState
) {
    AnimatedVisibility(
        visible = saveState.isVisible,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut()
    ) {
        Card(
            modifier = modifier,
            colors = CardDefaults.cardColors(
                containerColor = when (saveState.status) {
                    SaveStatus.SAVING -> MaterialTheme.colorScheme.secondary
                    SaveStatus.SAVED -> MaterialTheme.colorScheme.primary
                    SaveStatus.ERROR -> MaterialTheme.colorScheme.error
                }
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                when (saveState.status) {
                    SaveStatus.SAVING -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    }
                    SaveStatus.SAVED -> {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                    }
                    SaveStatus.ERROR -> {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                    }
                }
                
                Text(
                    text = when (saveState.status) {
                        SaveStatus.SAVING -> "Saving..."
                        SaveStatus.SAVED -> "Saved"
                        SaveStatus.ERROR -> "Save failed"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }
    }
}

/**
 * Error Recovery Suggestions - Shows helpful suggestions when errors occur
 */
@Composable
fun ErrorRecoverySuggestions(
    modifier: Modifier = Modifier,
    errorType: RecoveryErrorType,
    onSuggestionClick: (RecoverySuggestion) -> Unit
) {
    val suggestions = getSuggestionsForError(errorType)
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = "Recovery Suggestions",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            suggestions.forEach { suggestion ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSuggestionClick(suggestion) },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            suggestion.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = suggestion.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = suggestion.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

/**
 * Session Backup Status - Shows backup/restore status
 */
@Composable
fun SessionBackupStatus(
    modifier: Modifier = Modifier,
    backupState: BackupState,
    onManualBackup: () -> Unit,
    onRestoreBackup: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Session Backup",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = when {
                            backupState.lastBackupTime > 0 -> 
                                "Last backup: ${formatTimestamp(backupState.lastBackupTime)}"
                            else -> "No backup available"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (backupState.hasBackup) {
                        OutlinedButton(
                            onClick = onRestoreBackup,
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Restore", fontSize = 12.sp)
                        }
                    }
                    
                    Button(
                        onClick = onManualBackup,
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Backup", fontSize = 12.sp)
                    }
                }
            }
            
            if (backupState.isWorking) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Get recovery suggestions based on error type
 */
private fun getSuggestionsForError(errorType: RecoveryErrorType): List<RecoverySuggestion> {
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
                icon = Icons.Default.Retry,
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
                icon = Icons.Default.Restore,
                action = RecoveryAction.RESTORE_SESSION
            ),
            RecoverySuggestion(
                title = "Start New Session",
                description = "Begin a fresh prescription session",
                icon = Icons.Default.Add,
                action = RecoveryAction.NEW_SESSION
            )
        )
    }
}

/**
 * Format timestamp for display
 */
private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        else -> "${diff / 86400_000}d ago"
    }
}

/**
 * Data classes for recovery system
 */
data class UndoState(
    val isVisible: Boolean = false,
    val actionDescription: String = "",
    val timeRemaining: Int = 0,
    val actionType: UndoAction = UndoAction.SCAN
)

data class IncompleteSession(
    val id: String,
    val startTime: Long,
    val scannedDrugs: List<String>,
    val patientInfo: String = ""
)

data class AutoSaveState(
    val isVisible: Boolean = false,
    val status: SaveStatus = SaveStatus.SAVED
)

data class BackupState(
    val hasBackup: Boolean = false,
    val lastBackupTime: Long = 0,
    val isWorking: Boolean = false
)

data class RecoverySuggestion(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val action: RecoveryAction
)

enum class UndoAction {
    SCAN, DELETE, EDIT, COMPLETE_SESSION
}

enum class SaveStatus {
    SAVING, SAVED, ERROR
}

enum class RecoveryErrorType {
    CAMERA_ERROR, OCR_ERROR, NETWORK_ERROR, SESSION_ERROR
}

enum class RecoveryAction {
    CHECK_PERMISSIONS, RESTART_CAMERA, RETRY_OCR, MANUAL_ENTRY,
    CHECK_NETWORK, USE_OFFLINE, RESTORE_SESSION, NEW_SESSION
}
