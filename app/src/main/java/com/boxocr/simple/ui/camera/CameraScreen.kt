package com.boxocr.simple.ui.camera

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.ViewGroup
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

/**
 * Camera Screen - Core OCR functionality
 * 
 * Features:
 * 1. Camera preview with CameraX
 * 2. Take photo and process with OCR
 * 3. Show result dialog with match
 * 4. Copy result to clipboard
 */
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    onNavigateBack: () -> Unit,
    onNavigateToVerification: () -> Unit = {},
    viewModel: CameraViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Camera permission
    val cameraPermissionState = rememberPermissionState(
        android.Manifest.permission.CAMERA
    )
    
    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }
    
    // Navigate to verification when verification data is available
    LaunchedEffect(uiState.verificationData) {
        uiState.verificationData?.let {
            onNavigateToVerification()
            viewModel.clearVerificationData()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Box") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                cameraPermissionState.status.isGranted -> {
                    // Camera preview
                    CameraPreview(
                        modifier = Modifier.fillMaxSize(),
                        onCameraInitialized = { previewView ->
                            viewModel.initializeCamera(lifecycleOwner, previewView)
                        }
                    )
                    
                    // Camera controls
                    CameraControls(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(32.dp),
                        uiState = uiState,
                        onCapturePhoto = viewModel::captureForVerification,
                        onToggleTorch = viewModel::toggleTorch
                    )
                    
                    // Processing overlay
                    if (uiState.isProcessing) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Card {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator()
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Processing image...")
                                }
                            }
                        }
                    }
                    
                    // OCR Result Dialog
                    uiState.ocrResult?.let { result ->
                        OCRResultDialog(
                            result = result,
                            onDismiss = viewModel::clearOCRResult,
                            onCopyToClipboard = { text ->
                                copyToClipboard(context, text)
                                viewModel.clearOCRResult()
                            }
                        )
                    }
                    
                    // Error dialog
                    uiState.error?.let { error ->
                        AlertDialog(
                            onDismissRequest = viewModel::clearError,
                            title = { Text("Error") },
                            text = { Text(error) },
                            confirmButton = {
                                TextButton(onClick = viewModel::clearError) {
                                    Text("OK")
                                }
                            }
                        )
                    }
                }
                
                else -> {
                    // Permission denied screen
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Camera Permission Required",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "This app needs camera access to scan box labels",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { cameraPermissionState.launchPermissionRequest() }
                        ) {
                            Text("Grant Permission")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CameraPreview(
    modifier: Modifier = Modifier,
    onCameraInitialized: (PreviewView) -> Unit
) {
    AndroidView(
        factory = { context ->
            PreviewView(context).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                onCameraInitialized(this)
            }
        },
        modifier = modifier
    )
}

@Composable
private fun CameraControls(
    modifier: Modifier = Modifier,
    uiState: CameraUiState,
    onCapturePhoto: () -> Unit,
    onToggleTorch: () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(32.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Torch button
        IconButton(
            onClick = onToggleTorch,
            modifier = Modifier
                .size(56.dp)
                .background(
                    Color.Black.copy(alpha = 0.5f),
                    CircleShape
                )
        ) {
            Icon(
                if (uiState.isTorchOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                contentDescription = "Toggle Flash",
                tint = Color.White
            )
        }
        
        // Capture button
        Button(
            onClick = onCapturePhoto,
            enabled = !uiState.isProcessing,
            modifier = Modifier.size(72.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            if (uiState.isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White
                )
            } else {
                Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = "Capture",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun OCRResultDialog(
    result: OCRResultData,
    onDismiss: () -> Unit,
    onCopyToClipboard: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Scan Result") },
        text = {
            Column {
                Text(
                    text = "Scanned Text:",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = result.scannedText,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                if (result.matchedText != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Database Match:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = result.matchedText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No database match found",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { 
                    onCopyToClipboard(result.matchedText ?: result.scannedText)
                }
            ) {
                Text("Copy to Clipboard")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("OCR Result", text)
    clipboard.setPrimaryClip(clip)
}
