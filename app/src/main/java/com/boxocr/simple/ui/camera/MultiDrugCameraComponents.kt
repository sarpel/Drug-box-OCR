package com.boxocr.simple.ui.camera

import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.boxocr.simple.repository.*
import kotlinx.coroutines.delay

/**
 * Multi-Drug Detection Overlay Components - Phase 1 Week 2
 * 
 * Real-time detection overlays and multi-drug processing UI components
 * for the enhanced camera screen.
 */

/**
 * Real-time detection overlay that shows bounding boxes around detected drug boxes
 */
@Composable
fun MultiDrugDetectionOverlay(
    detectedRegions: List<DrugBoxRegion>,
    imageWidth: Int,
    imageHeight: Int,
    previewWidth: Int,
    previewHeight: Int,
    modifier: Modifier = Modifier
) {
    if (detectedRegions.isEmpty()) return
    
    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        val scaleX = size.width / imageWidth
        val scaleY = size.height / imageHeight
        
        detectedRegions.forEach { region ->
            val box = region.boundingBox
            val left = box.left * scaleX
            val top = box.top * scaleY
            val right = box.right * scaleX
            val bottom = box.bottom * scaleY
            
            // Draw bounding box
            drawRect(
                color = when {
                    region.confidence > 0.8f -> Color.Green
                    region.confidence > 0.6f -> Color.Yellow
                    else -> Color.Red
                },
                topLeft = androidx.compose.ui.geometry.Offset(left, top),
                size = androidx.compose.ui.geometry.Size(right - left, bottom - top),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx())
            )
            
            // Draw confidence indicator
            drawCircle(
                color = Color.White,
                radius = 8.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(left + 8.dp.toPx(), top + 8.dp.toPx())
            )
            
            drawCircle(
                color = when {
                    region.confidence > 0.8f -> Color.Green
                    region.confidence > 0.6f -> Color.Yellow
                    else -> Color.Red
                },
                radius = 6.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(left + 8.dp.toPx(), top + 8.dp.toPx())
            )
        }
    }
}

/**
 * Live detection status indicator
 */
@Composable
fun LiveDetectionStatus(
    isActive: Boolean,
    detectedCount: Int,
    frameQuality: FrameQuality,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(16.dp)
            .clip(RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Live indicator
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = if (isActive) Color.Green else Color.Gray,
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Column {
                Text(
                    text = if (isActive) "Canlı Tarama" else "Durduruldu",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                
                if (isActive) {
                    Text(
                        text = "$detectedCount kutu tespit edildi",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Frame quality indicator
            Icon(
                imageVector = when (frameQuality) {
                    FrameQuality.EXCELLENT -> Icons.Default.CheckCircle
                    FrameQuality.GOOD -> Icons.Default.Check
                    FrameQuality.ACCEPTABLE -> Icons.Default.Warning
                    FrameQuality.POOR -> Icons.Default.Error
                    FrameQuality.NO_DETECTION -> Icons.Default.Search
                    FrameQuality.TOO_MANY_OBJECTS -> Icons.Default.ScatterPlot
                    else -> Icons.Default.Help
                },
                contentDescription = "Frame Quality",
                tint = when (frameQuality) {
                    FrameQuality.EXCELLENT -> Color.Green
                    FrameQuality.GOOD -> Color.Blue
                    FrameQuality.ACCEPTABLE -> Color.Yellow
                    FrameQuality.POOR -> Color.Red
                    else -> Color.Gray
                },
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Multi-drug capture button with detection feedback
 */
@Composable
fun MultiDrugCaptureButton(
    detectedCount: Int,
    isProcessing: Boolean,
    onCapture: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonColor = when {
        isProcessing -> MaterialTheme.colorScheme.secondary
        detectedCount > 0 -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Detection count indicator
        if (detectedCount > 0) {
            Card(
                modifier = Modifier.padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = "$detectedCount İlaç Kutusu",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        // Capture button
        FloatingActionButton(
            onClick = onCapture,
            modifier = Modifier.size(72.dp),
            containerColor = buttonColor,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            if (isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onSecondary
                )
            } else {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Çoklu İlaç Yakala",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        
        // Capture hint
        Text(
            text = when {
                isProcessing -> "İşleniyor..."
                detectedCount > 0 -> "Yakalamaya Hazır"
                else -> "İlaç Kutusu Arayın"
            },
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

/**
 * Multi-drug processing progress indicator
 */
@Composable
fun MultiDrugProcessingProgress(
    processingState: MultiDrugScannerState,
    modifier: Modifier = Modifier
) {
    when (processingState) {
        is MultiDrugScannerState.Processing -> {
            Card(
                modifier = modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = processingState.message,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        
        is MultiDrugScannerState.Completed -> {
            Card(
                modifier = modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Completed",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = processingState.message,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        
        is MultiDrugScannerState.Error -> {
            Card(
                modifier = modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = processingState.message,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
        
        else -> { /* Idle state - no UI needed */ }
    }
}

/**
 * Enhanced camera mode selector
 */
@Composable
fun CameraModeSelector(
    selectedMode: CameraMode,
    onModeChange: (CameraMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        )
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            CameraMode.values().forEach { mode ->
                FilterChip(
                    selected = selectedMode == mode,
                    onClick = { onModeChange(mode) },
                    label = {
                        Text(
                            text = mode.displayName,
                            fontSize = 12.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = mode.icon,
                            contentDescription = mode.displayName,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }
    }
}

/**
 * Camera modes for multi-drug detection
 */
enum class CameraMode(val displayName: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    SINGLE_DRUG("Tek İlaç", Icons.Default.CameraAlt),
    MULTI_DRUG("Çoklu İlaç", Icons.Default.ViewModule),
    LIVE_SCAN("Canlı Tarama", Icons.Default.VideoCall),
    DAMAGED_RECOVERY("Hasar Kurtarma", Icons.Default.Healing)
}

/**
 * Detection settings overlay
 */
@Composable
fun DetectionSettingsOverlay(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onConfidenceThresholdChange: (Float) -> Unit,
    onMaxDetectionsChange: (Int) -> Unit,
    confidenceThreshold: Float,
    maxDetections: Int,
    modifier: Modifier = Modifier
) {
    if (!isVisible) return
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tespit Ayarları",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Kapat"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Confidence threshold slider
            Text(
                text = "Güven Eşiği: ${String.format("%.2f", confidenceThreshold)}",
                fontSize = 14.sp
            )
            
            Slider(
                value = confidenceThreshold,
                onValueChange = onConfidenceThresholdChange,
                valueRange = 0.3f..0.9f,
                steps = 12
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Max detections slider
            Text(
                text = "Maksimum Tespit: $maxDetections",
                fontSize = 14.sp
            )
            
            Slider(
                value = maxDetections.toFloat(),
                onValueChange = { onMaxDetectionsChange(it.toInt()) },
                valueRange = 1f..10f,
                steps = 9
            )
        }
    }
}

/**
 * Quick action buttons for camera
 */
@Composable
fun CameraQuickActions(
    onGalleryClick: () -> Unit,
    onLiveScanToggle: () -> Unit,
    onSettingsClick: () -> Unit,
    isLiveScanActive: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Gallery button
        FloatingActionButton(
            onClick = onGalleryClick,
            modifier = Modifier.size(48.dp),
            containerColor = MaterialTheme.colorScheme.secondary
        ) {
            Icon(
                imageVector = Icons.Default.PhotoLibrary,
                contentDescription = "Galeri",
                modifier = Modifier.size(20.dp)
            )
        }
        
        // Live scan toggle
        FloatingActionButton(
            onClick = onLiveScanToggle,
            modifier = Modifier.size(48.dp),
            containerColor = if (isLiveScanActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
        ) {
            Icon(
                imageVector = if (isLiveScanActive) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = if (isLiveScanActive) "Canlı Taramayı Durdur" else "Canlı Tarama Başlat",
                modifier = Modifier.size(20.dp)
            )
        }
        
        // Settings button
        FloatingActionButton(
            onClick = onSettingsClick,
            modifier = Modifier.size(48.dp),
            containerColor = MaterialTheme.colorScheme.secondary
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Ayarlar",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Batch capture indicator
 */
@Composable
fun BatchCaptureIndicator(
    capturedCount: Int,
    totalExpected: Int,
    modifier: Modifier = Modifier
) {
    if (capturedCount == 0) return
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Collections,
                contentDescription = "Batch",
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(16.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = "Batch: $capturedCount/$totalExpected",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            LinearProgressIndicator(
                progress = if (totalExpected > 0) capturedCount.toFloat() / totalExpected else 0f,
                modifier = Modifier
                    .width(60.dp)
                    .height(4.dp),
                color = MaterialTheme.colorScheme.tertiary,
                trackColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
            )
        }
    }
}
