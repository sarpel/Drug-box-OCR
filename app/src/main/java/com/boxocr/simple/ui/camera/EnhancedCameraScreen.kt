package com.boxocr.simple.ui.camera

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay

/**
 * Enhanced Camera Screen - Phase 1 Week 2 Implementation
 * 
 * Integrates multi-drug detection capabilities with existing camera functionality.
 * Supports single drug, multi-drug, live scanning, and damaged text recovery modes.
 */
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EnhancedCameraScreen(
    onNavigateBack: () -> Unit,
    onNavigateToMultiDrugResults: (String) -> Unit = {},
    onNavigateToVerification: () -> Unit = {},
    viewModel: EnhancedCameraViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Camera permission
    val cameraPermissionState = rememberPermissionState(
        android.Manifest.permission.CAMERA
    )
    
    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.processGalleryImages(uris)
        }
    }
    
    // Settings state
    var showSettings by remember { mutableStateOf(false) }
    var confidenceThreshold by remember { mutableStateOf(0.7f) }
    var maxDetections by remember { mutableStateOf(5) }
    
    LaunchedEffect(Unit) {
        if (cameraPermissionState.hasPermission) {
            viewModel.initializeCamera(lifecycleOwner)
        }
    }
    
    // Handle navigation based on results
    LaunchedEffect(uiState.multiDrugResult) {
        uiState.multiDrugResult?.let { result ->
            if (result.success && result.drugCount > 1) {
                // Navigate to multi-drug results screen
                onNavigateToMultiDrugResults(result.timestamp.toString())
            } else if (result.success && result.drugCount == 1) {
                // Navigate to single drug verification
                onNavigateToVerification()
            }
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        if (cameraPermissionState.hasPermission) {
            // Main camera content
            Column(modifier = Modifier.fillMaxSize()) {
                // Top bar with mode selector
                TopAppBar(
                    title = { 
                        Text(
                            text = when (uiState.cameraMode) {
                                CameraMode.SINGLE_DRUG -> "Tek İlaç Tarama"
                                CameraMode.MULTI_DRUG -> "Çoklu İlaç Tarama"
                                CameraMode.LIVE_SCAN -> "Canlı Tarama"
                                CameraMode.DAMAGED_RECOVERY -> "Hasar Kurtarma"
                            }
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Geri"
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.switchFlash() }
                        ) {
                            Icon(
                                imageVector = if (uiState.flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                contentDescription = "Flash"
                            )
                        }
                    }
                )
                
                // Camera mode selector
                CameraModeSelector(
                    selectedMode = uiState.cameraMode,
                    onModeChange = { viewModel.setCameraMode(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                // Camera preview with overlays
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    // Camera preview
                    AndroidView(
                        factory = { context ->
                            PreviewView(context).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                        update = { previewView ->
                            viewModel.bindPreview(previewView)
                        }
                    )
                    
                    // Multi-drug detection overlay
                    if (uiState.cameraMode != CameraMode.SINGLE_DRUG) {
                        MultiDrugDetectionOverlay(
                            detectedRegions = uiState.detectedRegions,
                            imageWidth = uiState.imageWidth,
                            imageHeight = uiState.imageHeight,
                            previewWidth = uiState.previewWidth,
                            previewHeight = uiState.previewHeight,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    
                    // Live detection status
                    if (uiState.cameraMode == CameraMode.LIVE_SCAN) {
                        LiveDetectionStatus(
                            isActive = uiState.isLiveScanActive,
                            detectedCount = uiState.detectedRegions.size,
                            frameQuality = uiState.frameQuality,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(16.dp)
                        )
                    }
                    
                    // Batch capture indicator
                    if (uiState.batchCaptureCount > 0) {
                        BatchCaptureIndicator(
                            capturedCount = uiState.batchCaptureCount,
                            totalExpected = uiState.batchTotalExpected,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                        )
                    }
                    
                    // Quick actions
                    CameraQuickActions(
                        onGalleryClick = { galleryLauncher.launch("image/*") },
                        onLiveScanToggle = { viewModel.toggleLiveScanning() },
                        onSettingsClick = { showSettings = true },
                        isLiveScanActive = uiState.isLiveScanActive,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(16.dp)
                    )
                }
                
                // Processing progress
                if (uiState.scannerState !is MultiDrugScannerState.Idle) {
                    MultiDrugProcessingProgress(
                        processingState = uiState.scannerState,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                
                // Bottom controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Gallery shortcut
                    IconButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "Galeri",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    // Main capture button
                    MultiDrugCaptureButton(
                        detectedCount = if (uiState.cameraMode != CameraMode.SINGLE_DRUG) {
                            uiState.detectedRegions.size
                        } else 1,
                        isProcessing = uiState.scannerState is MultiDrugScannerState.Processing,
                        onCapture = { viewModel.captureImage() }
                    )
                    
                    // Switch camera
                    IconButton(
                        onClick = { viewModel.switchCamera() },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cameraswitch,
                            contentDescription = "Kamera Değiştir",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            
            // Settings overlay
            if (showSettings) {
                DetectionSettingsOverlay(
                    isVisible = showSettings,
                    onDismiss = { showSettings = false },
                    onConfidenceThresholdChange = { 
                        confidenceThreshold = it
                        viewModel.updateDetectionSettings(it, maxDetections)
                    },
                    onMaxDetectionsChange = { 
                        maxDetections = it
                        viewModel.updateDetectionSettings(confidenceThreshold, it)
                    },
                    confidenceThreshold = confidenceThreshold,
                    maxDetections = maxDetections,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            
            // Results dialog
            uiState.multiDrugResult?.let { result ->
                if (result.success) {
                    MultiDrugResultDialog(
                        result = result,
                        onDismiss = { viewModel.clearResults() },
                        onNavigateToResults = { 
                            onNavigateToMultiDrugResults(result.timestamp.toString())
                        },
                        onCopyToClipboard = { text ->
                            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Drug Names", text)
                            clipboardManager.setPrimaryClip(clip)
                        }
                    )
                }
            }
            
        } else {
            // Permission request UI
            CameraPermissionRequest(
                onRequestPermission = { cameraPermissionState.launchPermissionRequest() }
            )
        }
    }
}

/**
 * Multi-drug result dialog
 */
@Composable
fun MultiDrugResultDialog(
    result: MultiDrugScanResult,
    onDismiss: () -> Unit,
    onNavigateToResults: () -> Unit,
    onCopyToClipboard: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = Color.Green
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("${result.drugCount} İlaç Tespit Edildi")
            }
        },
        text = {
            Column {
                Text(
                    text = "Tespit edilen ilaçlar:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                result.enhancedResults.forEach { drug ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text(
                                text = drug.finalDrugName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Güven: ${String.format("%.1f", drug.finalConfidence * 100)}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "İşleme süresi: ${result.processingTime}ms",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onNavigateToResults) {
                Text("Detayları Gör")
            }
        },
        dismissButton = {
            Row {
                TextButton(
                    onClick = { 
                        onCopyToClipboard(result.drugNames.joinToString(", "))
                    }
                ) {
                    Text("Kopyala")
                }
                TextButton(onClick = onDismiss) {
                    Text("Kapat")
                }
            }
        }
    )
}

/**
 * Camera permission request UI
 */
@Composable
fun CameraPermissionRequest(
    onRequestPermission: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Camera",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Kamera İzni Gerekli",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "İlaç kutularını taramak için kamera iznine ihtiyacımız var.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onRequestPermission
                ) {
                    Text("İzin Ver")
                }
            }
        }
    }
}
