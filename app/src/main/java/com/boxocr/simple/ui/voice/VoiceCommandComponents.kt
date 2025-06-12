package com.boxocr.simple.ui.voice

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.boxocr.simple.repository.VoiceAction
import com.boxocr.simple.repository.VoiceCommandState

/**
 * Voice Command UI Component - Phase 2 Feature
 * Floating voice control interface for hands-free operation
 */
@Composable
fun VoiceCommandButton(
    onVoiceAction: (VoiceAction) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VoiceCommandViewModel = hiltViewModel()
) {
    val voiceState by viewModel.voiceState.collectAsState()
    
    // Request microphone permission when needed
    LaunchedEffect(Unit) {
        viewModel.initializeVoiceRecognition()
    }
    
    // Handle voice actions
    LaunchedEffect(voiceState.lastVoiceAction) {
        voiceState.lastVoiceAction?.let { action ->
            onVoiceAction(action)
            viewModel.clearLastAction()
        }
    }
    
    Box(modifier = modifier) {
        // Voice status overlay
        AnimatedVisibility(
            visible = voiceState.isListening || voiceState.isProcessing,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            VoiceStatusOverlay(voiceState = voiceState)
        }
        
        // Main voice button
        FloatingActionButton(
            onClick = {
                if (voiceState.isListening) {
                    viewModel.stopListening()
                } else {
                    viewModel.startListening()
                }
            },
            modifier = Modifier.size(56.dp),
            containerColor = if (voiceState.isListening) {
                Color(0xFFF44336) // Red when listening
            } else {
                MaterialTheme.colorScheme.primary
            }
        ) {
            Icon(
                imageVector = when {
                    voiceState.isProcessing -> Icons.Default.Sync
                    voiceState.isListening -> Icons.Default.MicOff
                    else -> Icons.Default.Mic
                },
                contentDescription = "Voice Command",
                tint = Color.White
            )
        }
        
        // Error snackbar
        voiceState.error?.let { error ->
            LaunchedEffect(error) {
                // Show error and clear after delay
                kotlinx.coroutines.delay(3000)
                viewModel.clearError()
            }
        }
    }
}

@Composable
private fun VoiceStatusOverlay(
    voiceState: VoiceCommandState
) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .wrapContentHeight()
            .offset(x = (-140).dp, y = (-80).dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Status indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(
                            if (voiceState.isListening) Color(0xFF4CAF50) else Color(0xFFFF9800)
                        )
                )
                
                Text(
                    text = when {
                        voiceState.isProcessing -> "Processing..."
                        voiceState.isListening -> "Listening..."
                        else -> "Ready"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Partial recognition text
            voiceState.partialRecognitionText?.let { partialText ->
                Text(
                    text = "\"$partialText\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
            
            // Last recognized text
            voiceState.lastRecognizedText?.let { recognizedText ->
                Text(
                    text = "Heard: \"$recognizedText\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }
            
            // Audio level indicator
            if (voiceState.isListening && voiceState.audioLevel > 0) {
                LinearProgressIndicator(
                    progress = (voiceState.audioLevel + 10f) / 20f, // Normalize audio level
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = Color(0xFF4CAF50)
                )
            }
        }
    }
}

/**
 * Voice Command Help Dialog
 */
@Composable
fun VoiceCommandHelpDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    availableCommands: Map<String, String>
) {
    if (isVisible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.RecordVoiceOver, contentDescription = null)
                    Text("Voice Commands")
                }
            },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(availableCommands.entries.toList()) { (category, commands) ->
                        Column {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = commands,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("Got it")
                }
            }
        )
    }
}

/**
 * Compact voice command indicator for smaller screens
 */
@Composable
fun CompactVoiceIndicator(
    isListening: Boolean,
    onToggleListening: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onToggleListening,
        modifier = modifier
    ) {
        Icon(
            imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
            contentDescription = "Voice Command",
            tint = if (isListening) Color(0xFFF44336) else MaterialTheme.colorScheme.onSurface
        )
    }
}
