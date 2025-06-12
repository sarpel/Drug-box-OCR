package com.boxocr.simple.ui.camera

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Smart Camera Components - Phase 3 Quality of Life Features
 * 
 * Features:
 * 1. Drug box detection overlay with rectangle
 * 2. Auto-capture countdown when text is stable
 * 3. Barcode detection indicators
 * 4. Text stability monitoring
 * 5. Enhanced camera controls
 */

/**
 * Drug Box Detection Overlay
 * Shows a rectangle overlay when a potential drug box is detected
 */
@Composable
fun DrugBoxDetectionOverlay(
    modifier: Modifier = Modifier,
    detectionState: BoxDetectionState,
    onManualCapture: () -> Unit
) {
    val density = LocalDensity.current
    
    Box(modifier = modifier) {
        // Detection rectangle overlay
        if (detectionState.isBoxDetected) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                drawBoxDetectionOverlay(
                    detectionRect = detectionState.detectedBox,
                    confidence = detectionState.confidence,
                    isTextStable = detectionState.isTextStable
                )
            }
        }
        
        // Auto-capture countdown
        if (detectionState.autoCapture.isCountingDown) {
            AutoCaptureCountdown(
                modifier = Modifier.align(Alignment.Center),
                countdown = detectionState.autoCapture.countdown,
                onCancel = { /* Handle cancel */ }
            )
        }
        
        // Detection status indicator
        DetectionStatusIndicator(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            detectionState = detectionState
        )
        
        // Smart capture button
        SmartCaptureButton(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            detectionState = detectionState,
            onManualCapture = onManualCapture
        )
    }
}

/**
 * Auto-Capture Countdown Component
 * Shows countdown timer when auto-capture is triggered
 */
@Composable
private fun AutoCaptureCountdown(
    modifier: Modifier = Modifier,
    countdown: Int,
    onCancel: () -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.8f)
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Auto-capturing in",
                color = Color.White,
                fontSize = 14.sp
            )
            
            Text(
                text = countdown.toString(),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedButton(
                onClick = onCancel,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                )
            ) {
                Text("Cancel")
            }
        }
    }
}

/**
 * Detection Status Indicator
 * Shows current detection status and confidence
 */
@Composable
private fun DetectionStatusIndicator(
    modifier: Modifier = Modifier,
    detectionState: BoxDetectionState
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = when {
                detectionState.isBoxDetected && detectionState.isTextStable -> 
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                detectionState.isBoxDetected -> 
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f)
                else -> 
                    Color.Gray.copy(alpha = 0.7f)
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = when {
                    detectionState.isBoxDetected && detectionState.isTextStable -> Icons.Default.CheckCircle
                    detectionState.isBoxDetected -> Icons.Default.Search
                    else -> Icons.Default.CameraAlt
                },
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            
            Column {
                Text(
                    text = when {
                        detectionState.isBoxDetected && detectionState.isTextStable -> "Ready to Capture"
                        detectionState.isBoxDetected -> "Box Detected"
                        else -> "Searching..."
                    },
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                
                if (detectionState.isBoxDetected) {
                    Text(
                        text = "${(detectionState.confidence * 100).toInt()}% confidence",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

/**
 * Smart Capture Button
 * Enhanced capture button with smart features
 */
@Composable
private fun SmartCaptureButton(
    modifier: Modifier = Modifier,
    detectionState: BoxDetectionState,
    onManualCapture: () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Auto-capture toggle
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (detectionState.autoCapture.isEnabled)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                else
                    Color.Gray.copy(alpha = 0.7f)
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "AUTO",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // Main capture button
        FloatingActionButton(
            onClick = onManualCapture,
            modifier = Modifier.size(72.dp),
            containerColor = when {
                detectionState.isBoxDetected && detectionState.isTextStable ->
                    MaterialTheme.colorScheme.primary
                detectionState.isBoxDetected ->
                    MaterialTheme.colorScheme.secondary
                else ->
                    MaterialTheme.colorScheme.outline
            }
        ) {
            Icon(
                Icons.Default.CameraAlt,
                contentDescription = "Capture",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
        
        // Barcode scan button
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (detectionState.barcodeDetected)
                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.9f)
                else
                    Color.Gray.copy(alpha = 0.7f)
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.QrCode,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "QR",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Draw box detection overlay on canvas
 */
private fun DrawScope.drawBoxDetectionOverlay(
    detectionRect: Rect?,
    confidence: Float,
    isTextStable: Boolean
) {
    detectionRect?.let { rect ->
        // Detection rectangle
        val strokeColor = when {
            isTextStable -> Color.Green
            confidence > 0.7f -> Color.Yellow
            else -> Color.Red
        }
        
        val strokeWidth = 4.dp.toPx()
        val cornerLength = 20.dp.toPx()
        
        // Draw corner brackets instead of full rectangle for better UX
        drawCornerBrackets(
            rect = rect,
            color = strokeColor,
            strokeWidth = strokeWidth,
            cornerLength = cornerLength
        )
        
        // Draw center crosshair
        val centerX = rect.center.x
        val centerY = rect.center.y
        val crosshairSize = 10.dp.toPx()
        
        drawLine(
            color = strokeColor,
            start = Offset(centerX - crosshairSize, centerY),
            end = Offset(centerX + crosshairSize, centerY),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = strokeColor,
            start = Offset(centerX, centerY - crosshairSize),
            end = Offset(centerX, centerY + crosshairSize),
            strokeWidth = 2.dp.toPx()
        )
    }
}

/**
 * Draw corner brackets for better visual feedback
 */
private fun DrawScope.drawCornerBrackets(
    rect: Rect,
    color: Color,
    strokeWidth: Float,
    cornerLength: Float
) {
    // Top-left corner
    drawLine(color, Offset(rect.left, rect.top), Offset(rect.left + cornerLength, rect.top), strokeWidth)
    drawLine(color, Offset(rect.left, rect.top), Offset(rect.left, rect.top + cornerLength), strokeWidth)
    
    // Top-right corner  
    drawLine(color, Offset(rect.right - cornerLength, rect.top), Offset(rect.right, rect.top), strokeWidth)
    drawLine(color, Offset(rect.right, rect.top), Offset(rect.right, rect.top + cornerLength), strokeWidth)
    
    // Bottom-left corner
    drawLine(color, Offset(rect.left, rect.bottom - cornerLength), Offset(rect.left, rect.bottom), strokeWidth)
    drawLine(color, Offset(rect.left, rect.bottom), Offset(rect.left + cornerLength, rect.bottom), strokeWidth)
    
    // Bottom-right corner
    drawLine(color, Offset(rect.right, rect.bottom - cornerLength), Offset(rect.right, rect.bottom), strokeWidth)
    drawLine(color, Offset(rect.right - cornerLength, rect.bottom), Offset(rect.right, rect.bottom), strokeWidth)
}

/**
 * Data classes for smart camera features
 */
data class BoxDetectionState(
    val isBoxDetected: Boolean = false,
    val detectedBox: Rect? = null,
    val confidence: Float = 0f,
    val isTextStable: Boolean = false,
    val barcodeDetected: Boolean = false,
    val autoCapture: AutoCaptureState = AutoCaptureState()
)

data class AutoCaptureState(
    val isEnabled: Boolean = true,
    val isCountingDown: Boolean = false,
    val countdown: Int = 3,
    val stabilityThreshold: Int = 30 // frames
)

/**
 * Text Stability Monitor Composable
 * Monitors if detected text remains stable for auto-capture
 */
@Composable
fun rememberTextStabilityMonitor(
    detectedText: String?,
    onStabilityChanged: (Boolean) -> Unit
): TextStabilityState {
    var stabilityState by remember { mutableStateOf(TextStabilityState()) }
    
    LaunchedEffect(detectedText) {
        if (detectedText != null) {
            if (detectedText == stabilityState.lastText) {
                // Text is the same, increment stability counter
                val newCount = stabilityState.stableFrameCount + 1
                stabilityState = stabilityState.copy(stableFrameCount = newCount)
                
                // Check if text is now stable
                if (newCount >= 30 && !stabilityState.isStable) { // 30 frames = ~1 second
                    stabilityState = stabilityState.copy(isStable = true)
                    onStabilityChanged(true)
                }
            } else {
                // Text changed, reset stability
                stabilityState = TextStabilityState(
                    lastText = detectedText,
                    stableFrameCount = 1,
                    isStable = false
                )
                onStabilityChanged(false)
            }
        } else {
            // No text detected, reset
            stabilityState = TextStabilityState()
            onStabilityChanged(false)
        }
    }
    
    return stabilityState
}

data class TextStabilityState(
    val lastText: String? = null,
    val stableFrameCount: Int = 0,
    val isStable: Boolean = false
)
