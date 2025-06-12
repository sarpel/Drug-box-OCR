package com.boxocr.simple.ui.camera

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.boxocr.simple.data.DetectedDrugBox
import com.boxocr.simple.data.LiveDetectionResult
import com.boxocr.simple.data.MultiDrugProcessingStatus
import kotlinx.coroutines.delay

/**
 * Live Video OCR Components for real-time multi-drug detection
 * Provides continuous scanning with visual feedback overlays
 */

/**
 * Real-time detection overlay that draws bounding boxes and confidence indicators
 */
@Composable
fun RealTimeDetectionOverlay(
    detectionResults: List<DetectedDrugBox>,
    previewSize: androidx.compose.ui.geometry.Size,
    modifier: Modifier = Modifier,
    showConfidence: Boolean = true,
    showLabels: Boolean = true
) {
    val density = LocalDensity.current
    
    Canvas(modifier = modifier.fillMaxSize()) {
        detectionResults.forEach { drugBox ->
            drawDrugBoxDetection(
                drugBox = drugBox,
                previewSize = previewSize,
                canvasSize = size,
                showConfidence = showConfidence,
                showLabels = showLabels,
                density = density
            )
        }
    }
}

/**
 * Draws individual drug box detection with bounding box and information
 */
private fun DrawScope.drawDrugBoxDetection(
    drugBox: DetectedDrugBox,
    previewSize: androidx.compose.ui.geometry.Size,
    canvasSize: androidx.compose.ui.geometry.Size,
    showConfidence: Boolean: Boolean,
    showLabels: Boolean,
    density: androidx.compose.ui.units.Density
) {
    // Scale bounding box to canvas size
    val scaleX = canvasSize.width / previewSize.width
    val scaleY = canvasSize.height / previewSize.height
    
    val scaledRect = Rect(
        left = drugBox.boundingBox.left * scaleX,
        top = drugBox.boundingBox.top * scaleY,
        right = drugBox.boundingBox.right * scaleX,
        bottom = drugBox.boundingBox.bottom * scaleY
    )
    
    // Get color based on confidence level
    val confidence = drugBox.confidence
    val color = when {
        confidence >= 0.8f -> Color.Green
        confidence >= 0.6f -> Color.Yellow
        confidence >= 0.4f -> Color.Orange
        else -> Color.Red
    }
    
    // Draw bounding box
    drawRect(
        color = color,
        topLeft = Offset(scaledRect.left, scaledRect.top),
        size = androidx.compose.ui.geometry.Size(
            scaledRect.width,
            scaledRect.height
        ),
        style = Stroke(width = 3.dp.toPx())
    )
    
    // Draw corner indicators for better visibility
    val cornerSize = 15.dp.toPx()
    val corners = listOf(
        // Top-left
        Offset(scaledRect.left, scaledRect.top) to 
        Offset(scaledRect.left + cornerSize, scaledRect.top),
        Offset(scaledRect.left, scaledRect.top) to 
        Offset(scaledRect.left, scaledRect.top + cornerSize),
        
        // Top-right
        Offset(scaledRect.right, scaledRect.top) to 
        Offset(scaledRect.right - cornerSize, scaledRect.top),
        Offset(scaledRect.right, scaledRect.top) to 
        Offset(scaledRect.right, scaledRect.top + cornerSize),
        
        // Bottom-left
        Offset(scaledRect.left, scaledRect.bottom) to 
        Offset(scaledRect.left + cornerSize, scaledRect.bottom),
        Offset(scaledRect.left, scaledRect.bottom) to 
        Offset(scaledRect.left, scaledRect.bottom - cornerSize),
        
        // Bottom-right
        Offset(scaledRect.right, scaledRect.bottom) to 
        Offset(scaledRect.right - cornerSize, scaledRect.bottom),
        Offset(scaledRect.right, scaledRect.bottom) to 
        Offset(scaledRect.right, scaledRect.bottom - cornerSize)
    )
    
    corners.forEach { (start, end) ->
        drawLine(
            color = color,
            start = start,
            end = end,
            strokeWidth = 5.dp.toPx()
        )
    }
    
    // Draw confidence indicator
    if (showConfidence) {
        val confidenceText = "${(confidence * 100).toInt()}%"
        drawContext.canvas.nativeCanvas.drawText(
            confidenceText,
            scaledRect.left,
            scaledRect.top - 5.dp.toPx(),
            android.graphics.Paint().apply {
                color = color.toArgb()
                textSize = 12.sp.toPx()
                isAntiAlias = true
            }
        )
    }
    
    // Draw drug name label if detected
    if (showLabels && drugBox.detectedName.isNotEmpty()) {
        val labelText = drugBox.detectedName
        val labelWidth = 150.dp.toPx()
        val labelHeight = 30.dp.toPx()
        
        // Draw label background
        drawRoundRect(
            color = color.copy(alpha = 0.8f),
            topLeft = Offset(scaledRect.left, scaledRect.bottom + 5.dp.toPx()),
            size = androidx.compose.ui.geometry.Size(labelWidth, labelHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
        )
        
        // Draw label text
        drawContext.canvas.nativeCanvas.drawText(
            labelText,
            scaledRect.left + 5.dp.toPx(),
            scaledRect.bottom + 20.dp.toPx(),
            android.graphics.Paint().apply {
                color = Color.White.toArgb()
                textSize = 10.sp.toPx()
                isAntiAlias = true
            }
        )
    }
}

/**
 * Live video interface controls with scanning modes
 */
@Composable
fun LiveVideoControls(
    isScanning: Boolean,
    scanMode: LiveScanMode,
    onToggleScan: () -> Unit,
    onChangeScanMode: (LiveScanMode) -> Unit,
    onManualCapture: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                Color.Black.copy(alpha = 0.7f),
                RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Scan mode selector
        Text(
            text = "Scan Mode",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LiveScanMode.values().forEach { mode ->
                FilterChip(
                    onClick = { onChangeScanMode(mode) },
                    label = { 
                        Text(
                            text = mode.displayName,
                            fontSize = 12.sp
                        )
                    },
                    selected = scanMode == mode,
                    modifier = Modifier.height(32.dp)
                )
            }
        }
        
        // Control buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Toggle scan button
            Button(
                onClick = onToggleScan,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isScanning) Color.Red else Color.Green
                ),
                modifier = Modifier.size(60.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    imageVector = if (isScanning) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = if (isScanning) "Stop Scanning" else "Start Scanning",
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Manual capture button
            Button(
                onClick = onManualCapture,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Blue
                ),
                modifier = Modifier.size(50.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Manual Capture",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Live detection statistics display
 */
@Composable
fun LiveDetectionStats(
    liveResult: LiveDetectionResult,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .background(
                Color.Black.copy(alpha = 0.8f),
                RoundedCornerShape(8.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Live Detection",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Detected: ${liveResult.detectedBoxes.size} boxes",
                color = Color.White,
                fontSize = 12.sp
            )
            
            Text(
                text = "Recognized: ${liveResult.recognizedDrugs.size} drugs",
                color = Color.Green,
                fontSize = 12.sp
            )
            
            if (liveResult.averageConfidence > 0) {
                Text(
                    text = "Avg Confidence: ${(liveResult.averageConfidence * 100).toInt()}%",
                    color = when {
                        liveResult.averageConfidence >= 0.8f -> Color.Green
                        liveResult.averageConfidence >= 0.6f -> Color.Yellow
                        else -> Color.Orange
                    },
                    fontSize = 12.sp
                )
            }
            
            Text(
                text = "FPS: ${liveResult.currentFPS}",
                color = Color.Cyan,
                fontSize = 12.sp
            )
        }
    }
}

/**
 * Progress indicators for multi-drug processing
 */
@Composable
fun MultiDrugProcessingIndicator(
    processingStatus: MultiDrugProcessingStatus,
    modifier: Modifier = Modifier
) {
    var animatedProgress by remember { mutableStateOf(0f) }
    
    LaunchedEffect(processingStatus.progress) {
        // Animate progress smoothly
        val start = animatedProgress
        val end = processingStatus.progress
        val duration = 500L
        val startTime = System.currentTimeMillis()
        
        while (animatedProgress < end) {
            val elapsed = System.currentTimeMillis() - startTime
            val progress = (elapsed.toFloat() / duration).coerceAtMost(1f)
            animatedProgress = start + (end - start) * progress
            delay(16) // ~60 FPS
        }
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Processing title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (processingStatus.isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                }
                Text(
                    text = processingStatus.currentStage,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Overall progress bar
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Overall Progress",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${(animatedProgress * 100).toInt()}%",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                LinearProgressIndicator(
                    progress = animatedProgress,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Individual drug processing status
            if (processingStatus.drugProcessingStates.isNotEmpty()) {
                Text(
                    text = "Drug Processing Status:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                
                processingStatus.drugProcessingStates.forEach { drugStatus ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = drugStatus.drugName.ifEmpty { "Unknown Drug" },
                            fontSize = 11.sp,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            when (drugStatus.status) {
                                "completed" -> Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Complete",
                                    tint = Color.Green,
                                    modifier = Modifier.size(14.dp)
                                )
                                "processing" -> CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 1.5.dp
                                )
                                "error" -> Icon(
                                    Icons.Default.Error,
                                    contentDescription = "Error",
                                    tint = Color.Red,
                                    modifier = Modifier.size(14.dp)
                                )
                                else -> Icon(
                                    Icons.Default.HourglassEmpty,
                                    contentDescription = "Waiting",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            
                            Text(
                                text = "${drugStatus.confidence}%",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // Estimated time remaining
            if (processingStatus.estimatedTimeRemaining > 0) {
                Text(
                    text = "Est. ${processingStatus.estimatedTimeRemaining}s remaining",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Continuous detection feedback overlay
 */
@Composable
fun ContinuousDetectionFeedback(
    isDetecting: Boolean,
    lastDetectionTime: Long,
    modifier: Modifier = Modifier
) {
    var pulseAlpha by remember { mutableStateOf(0.3f) }
    
    LaunchedEffect(isDetecting) {
        while (isDetecting) {
            // Pulse animation
            pulseAlpha = if (pulseAlpha > 0.8f) 0.3f else 0.8f
            delay(500)
        }
    }
    
    Box(
        modifier = modifier
            .background(
                Color.Blue.copy(alpha = if (isDetecting) pulseAlpha else 0f),
                RoundedCornerShape(50)
            )
            .padding(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (isDetecting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
            }
            
            Text(
                text = if (isDetecting) "Scanning..." else "Ready",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            
            if (!isDetecting && lastDetectionTime > 0) {
                val timeSince = (System.currentTimeMillis() - lastDetectionTime) / 1000
                Text(
                    text = "${timeSince}s ago",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 10.sp
                )
            }
        }
    }
}

/**
 * Live scan modes for different detection scenarios
 */
enum class LiveScanMode(val displayName: String) {
    CONTINUOUS("Continuous"),
    ON_DEMAND("On Demand"),
    AUTO_CAPTURE("Auto Capture"),
    SINGLE_SHOT("Single Shot")
}
