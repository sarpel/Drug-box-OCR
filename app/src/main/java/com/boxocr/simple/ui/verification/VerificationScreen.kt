package com.boxocr.simple.ui.verification

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Drug Verification Preview Screen - Phase 2 Feature
 * Shows captured image, OCR text, and matched drug for user confirmation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationScreen(
    onConfirmed: (String) -> Unit,
    onRejected: () -> Unit,
    onEnhancedMatching: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: VerificationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = { 
                Text(
                    "Verify Drug Information",
                    style = MaterialTheme.typography.titleMedium
                ) 
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        
        uiState.verificationData?.let { data ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Captured Image Section
                DrugImageSection(imagePath = data.capturedImagePath)
                
                // OCR Text Section
                OCRTextSection(ocrText = data.ocrText)
                
                // Matched Drug Section
                MatchedDrugSection(
                    matchResult = data.matchResult,
                    editedName = uiState.editedDrugName,
                    onNameChanged = viewModel::updateEditedName
                )
                
                // Confidence Indicator
                ConfidenceIndicator(confidence = data.matchResult.confidence)
                
                // Action Buttons
                ActionButtonsSection(
                    confidence = data.matchResult.confidence,
                    editedName = uiState.editedDrugName,
                    isEditing = uiState.isEditing,
                    onConfirmed = { finalName ->
                        keyboardController?.hide()
                        onConfirmed(finalName)
                    },
                    onRejected = onRejected,
                    onEnhancedMatching = onEnhancedMatching,
                    onEditToggle = viewModel::toggleEditing
                )
            }
        } ?: run {
            // Error state - no verification data
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = "Error",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        "No verification data available",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    Button(onClick = onNavigateBack) {
                        Text("Go Back")
                    }
                }
            }
        }
    }
}

@Composable
private fun DrugImageSection(imagePath: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Captured Image",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            val painter = rememberAsyncImagePainter(
                ImageRequest.Builder(LocalContext.current)
                    .data(File(imagePath))
                    .build()
            )
            
            Image(
                painter = painter,
                contentDescription = "Captured drug box image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun OCRTextSection(ocrText: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Extracted Text (OCR)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = ocrText.ifEmpty { "No text extracted" },
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (ocrText.isEmpty()) 
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MatchedDrugSection(
    matchResult: com.boxocr.simple.data.MatchResult,
    editedName: String,
    onNameChanged: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Matched Drug Name",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            OutlinedTextField(
                value = editedName,
                onValueChange = onNameChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Drug Name") },
                placeholder = { Text("Enter drug name...") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { /* Handle done if needed */ }
                )
            )
            
            if (matchResult.bestMatch != null && matchResult.bestMatch != editedName) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Original match: ${matchResult.bestMatch}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun ConfidenceIndicator(confidence: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Match Confidence",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LinearProgressIndicator(
                    progress = confidence,
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp),
                    color = when {
                        confidence >= 0.8f -> Color(0xFF4CAF50) // Green
                        confidence >= 0.6f -> Color(0xFFFF9800) // Orange
                        else -> Color(0xFFF44336) // Red
                    }
                )
                
                Text(
                    "${(confidence * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        confidence >= 0.8f -> Color(0xFF4CAF50)
                        confidence >= 0.6f -> Color(0xFFFF9800)
                        else -> Color(0xFFF44336)
                    }
                )
            }
            
            Text(
                when {
                    confidence >= 0.8f -> "High confidence match"
                    confidence >= 0.6f -> "Medium confidence - please verify"
                    else -> "Low confidence - consider enhanced matching"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun ActionButtonsSection(
    confidence: Float,
    editedName: String,
    isEditing: Boolean,
    onConfirmed: (String) -> Unit,
    onRejected: () -> Unit,
    onEnhancedMatching: () -> Unit,
    onEditToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Verification Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // Primary action - Confirm
            Button(
                onClick = { onConfirmed(editedName) },
                modifier = Modifier.fillMaxWidth(),
                enabled = editedName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Confirm & Add to Prescription")
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Enhanced matching for low confidence
                if (confidence < 0.8f) {
                    OutlinedButton(
                        onClick = onEnhancedMatching,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Enhanced Match")
                    }
                }
                
                // Reject and rescan
                OutlinedButton(
                    onClick = onRejected,
                    modifier = Modifier.weight(1f),
                    colors = OutlinedButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reject & Rescan")
                }
            }
        }
    }
}
