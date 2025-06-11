package com.boxocr.simple.ui.batch

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.boxocr.simple.repository.CameraManager
import com.boxocr.simple.ui.camera.CameraPreview
import com.boxocr.simple.ui.enhanced.EnhancedMatchingScreen

/**
 * Batch Scanning Screen for prescription workflow
 * Allows scanning multiple drugs in sequence with session management
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchScanningScreen(
    onNavigateBack: () -> Unit,
    viewModel: BatchScanningViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val scannedDrugs by viewModel.scannedDrugs.collectAsState()
    val serverStatus by viewModel.serverStatus.collectAsState()
    
    var showStartDialog by remember { mutableStateOf(false) }
    var patientInfo by remember { mutableStateOf("") }
    
    val cameraManager = remember { CameraManager(context) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Batch Prescription Scanning")
                        if (uiState.isSessionActive) {
                            Text(
                                text = uiState.sessionInfo,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Server status indicator
                    when (serverStatus) {
                        is com.boxocr.simple.automation.WindowsAutomationServer.ServerStatus.Running -> {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Server Running",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        is com.boxocr.simple.automation.WindowsAutomationServer.ServerStatus.Error -> {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = "Server Error",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                        else -> {
                            Icon(
                                Icons.Default.Circle,
                                contentDescription = "Server Stopped",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState.isSessionActive) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Send to Windows FAB
                    if (scannedDrugs.isNotEmpty()) {
                        FloatingActionButton(
                            onClick = { viewModel.sendToWindows() },
                            containerColor = MaterialTheme.colorScheme.secondary
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send to Windows")
                        }
                    }
                    
                    // Complete session FAB
                    FloatingActionButton(
                        onClick = { viewModel.completePrescriptionSession() },
                        containerColor = MaterialTheme.colorScheme.tertiary
                    ) {
                        Icon(Icons.Default.Done, contentDescription = "Complete Session")
                    }
                }
            } else {
                FloatingActionButton(
                    onClick = { showStartDialog = true }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Start Prescription")
                }
            }
        }
    ) { paddingValues ->
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            
            // Error message
            if (uiState.error.isNotBlank()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.clearError() }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Clear Error",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
            
            // Server status card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Computer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = viewModel.getServerInfo(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            if (uiState.isSessionActive) {
                // Active session UI
                ActiveSessionContent(
                    uiState = uiState,
                    scannedDrugs = scannedDrugs,
                    cameraManager = cameraManager,
                    onImageCaptured = { imageBytes ->
                        viewModel.processScannedImage(imageBytes)
                    },
                    onRemoveDrug = { index ->
                        viewModel.removeDrug(index)
                    }
                )
            } else {
                // No session UI
                NoSessionContent()
            }
        }
    }
    
    // Enhanced matching screen overlay
    if (uiState.showEnhancedMatching) {
        EnhancedMatchingScreen(
            onMatchConfirmed = { drugName ->
                viewModel.confirmDrugFromEnhancedMatching(drugName)
            },
            onNavigateBack = {
                viewModel.closeEnhancedMatching()
            }
        )
    }
    
    // Confirmation dialog for scanned drug
    if (uiState.showConfirmDialog) {
        DrugConfirmationDialog(
            scannedText = uiState.lastScannedText,
            matchedDrug = uiState.lastMatchedDrug,
            confidence = uiState.matchConfidence,
            onConfirm = { viewModel.confirmScannedDrug() },
            onReject = { viewModel.rejectScannedDrug() }
        )
    }
    
    // Start session dialog
    if (showStartDialog) {
        StartSessionDialog(
            patientInfo = patientInfo,
            onPatientInfoChange = { patientInfo = it },
            onConfirm = {
                viewModel.startPrescriptionSession(patientInfo)
                showStartDialog = false
                patientInfo = ""
            },
            onDismiss = { showStartDialog = false }
        )
    }
}

@Composable
private fun ActiveSessionContent(
    uiState: BatchScanningViewModel.BatchScanningUiState,
    scannedDrugs: List<BatchScanningViewModel.ScannedDrug>,
    cameraManager: CameraManager,
    onImageCaptured: (ByteArray) -> Unit,
    onRemoveDrug: (Int) -> Unit
) {
    Column {
        // Session progress
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Scan Progress",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Drug ${uiState.currentScanIndex + 1} - ${scannedDrugs.size} scanned",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (uiState.isProcessing) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Processing image...",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
        
        // Camera or scanned drugs list
        if (scannedDrugs.isEmpty() || uiState.isProcessing) {
            // Camera preview
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(300.dp)
            ) {
                CameraPreview(
                    cameraManager = cameraManager,
                    onImageCaptured = onImageCaptured,
                    isProcessing = uiState.isProcessing
                )
            }
        } else {
            // Scanned drugs list with camera option
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Camera card for next scan
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState.isProcessing) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator()
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Processing...")
                                }
                            } else {
                                CameraPreview(
                                    cameraManager = cameraManager,
                                    onImageCaptured = onImageCaptured,
                                    isProcessing = false
                                )
                            }
                        }
                    }
                }
                
                // Scanned drugs
                itemsIndexed(scannedDrugs) { index, drug ->
                    ScannedDrugCard(
                        drug = drug,
                        onRemove = { onRemoveDrug(index) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NoSessionContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.MedicalServices,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Start New Prescription",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Tap the + button to begin scanning drugs for a prescription",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ScannedDrugCard(
    drug: BatchScanningViewModel.ScannedDrug,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Drug index
            Surface(
                modifier = Modifier.size(32.dp),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primary
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = drug.index.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Drug info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = drug.matchedDrug,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Confidence: ${drug.confidence}% • ${drug.getFormattedTime()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Remove button
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove Drug",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun DrugConfirmationDialog(
    scannedText: String,
    matchedDrug: String,
    confidence: Int,
    onConfirm: () -> Unit,
    onReject: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onReject,
        title = { Text("Confirm Scanned Drug") },
        text = {
            Column {
                Text("Scanned text:")
                Text(
                    text = scannedText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Matched drug:")
                Text(
                    text = matchedDrug,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Confidence: $confidence%")
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Add to Prescription")
            }
        },
        dismissButton = {
            TextButton(onClick = onReject) {
                Text("Scan Again")
            }
        }
    )
}

@Composable
private fun StartSessionDialog(
    patientInfo: String,
    onPatientInfoChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Start Prescription Session") },
        text = {
            Column {
                Text("Patient information (optional):")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = patientInfo,
                    onValueChange = onPatientInfoChange,
                    label = { Text("Patient ID or Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Start Session")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
