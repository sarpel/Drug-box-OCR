package com.boxocr.simple.ui.multidrug

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.boxocr.simple.repository.*

/**
 * Multi-Drug Results Screen - Phase 1 Week 2 Implementation
 * 
 * Displays detailed results from multi-drug scanning with enhanced analysis,
 * batch integration, and Windows automation capabilities.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiDrugResultsScreen(
    resultId: String,
    onNavigateBack: () -> Unit,
    onNavigateToBatch: (String) -> Unit,
    onNavigateToWindowsAutomation: () -> Unit,
    viewModel: MultiDrugResultsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    LaunchedEffect(resultId) {
        viewModel.loadResult(resultId)
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Top app bar
        TopAppBar(
            title = { 
                Text(
                    text = if (uiState.result?.drugCount == 1) {
                        "İlaç Sonucu"
                    } else {
                        "${uiState.result?.drugCount ?: 0} İlaç Sonucu"
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
                // Share button
                IconButton(
                    onClick = { viewModel.shareResults(context) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Paylaş"
                    )
                }
                
                // More options
                var showMenu by remember { mutableStateOf(false) }
                IconButton(
                    onClick = { showMenu = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Daha Fazla"
                    )
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Panoya Kopyala") },
                        onClick = { 
                            viewModel.copyToClipboard(context)
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(Icons.Default.ContentCopy, contentDescription = null)
                        }
                    )
                    
                    DropdownMenuItem(
                        text = { Text("Batch'e Ekle") },
                        onClick = { 
                            viewModel.addToBatch()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(Icons.Default.QueuePlayNext, contentDescription = null)
                        }
                    )
                    
                    if (uiState.result?.batchIntegration?.readyForWindowsAutomation == true) {
                        DropdownMenuItem(
                            text = { Text("Windows Otomasyonu") },
                            onClick = { 
                                onNavigateToWindowsAutomation()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Computer, contentDescription = null)
                            }
                        )
                    }
                }
            }
        )
        
        uiState.result?.let { result ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                // Summary card
                item {
                    MultiDrugSummaryCard(
                        result = result,
                        onNavigateToBatch = onNavigateToBatch
                    )
                }
                
                // Processing info
                item {
                    ProcessingInfoCard(result = result)
                }
                
                // Individual drug results
                items(result.enhancedResults) { drugResult ->
                    DrugResultCard(
                        drugResult = drugResult,
                        onImageClick = { /* Show full image */ },
                        onCorrectResult = { viewModel.correctResult(drugResult, it) }
                    )
                }
                
                // Visual recovery info (if any)
                result.enhancedResults.firstOrNull { it.visualRecovery != null }?.let { drugWithRecovery ->
                    item {
                        VisualRecoveryInfoCard(
                            visualRecovery = drugWithRecovery.visualRecovery!!
                        )
                    }
                }
                
                // Batch integration info
                result.batchIntegration?.let { batchInfo ->
                    item {
                        BatchIntegrationCard(
                            batchInfo = batchInfo,
                            onNavigateToBatch = onNavigateToBatch
                        )
                    }
                }
            }
        } ?: run {
            // Loading state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator()
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = uiState.errorMessage ?: "Sonuç bulunamadı",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

/**
 * Summary card showing overall results
 */
@Composable
fun MultiDrugSummaryCard(
    result: MultiDrugScanResult,
    onNavigateToBatch: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "${result.drugCount} İlaç Tespit Edildi",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Text(
                        text = "Ortalama güven: ${String.format("%.1f", result.averageConfidence * 100)}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
                
                // Status icon
                Icon(
                    imageVector = if (result.success) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = "Status",
                    tint = if (result.success) Color.Green else Color.Red,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Drug names
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                result.drugNames.forEach { drugName ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalPharmacy,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = drugName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            // Quick actions
            if (result.batchIntegration != null) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onNavigateToBatch(result.batchIntegration.sessionId) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ViewList,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Batch Görüntüle")
                    }
                    
                    if (result.batchIntegration.readyForWindowsAutomation) {
                        FilledTonalButton(
                            onClick = { /* Navigate to Windows automation */ },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Computer,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Otomatik Gir")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Processing information card
 */
@Composable
fun ProcessingInfoCard(result: MultiDrugScanResult) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "İşlem Bilgileri",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItem(
                    label = "İşlem Süresi",
                    value = "${result.processingTime}ms"
                )
                
                InfoItem(
                    label = "Kaynak",
                    value = when (result.source) {
                        ImageSource.CAMERA_CAPTURE -> "Kamera"
                        ImageSource.GALLERY_IMPORT -> "Galeri"
                        ImageSource.BATCH_UPLOAD -> "Toplu Yükleme"
                        ImageSource.USER_UPLOAD -> "Kullanıcı"
                    }
                )
                
                InfoItem(
                    label = "Tarih",
                    value = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                        .format(java.util.Date(result.timestamp))
                )
            }
        }
    }
}

/**
 * Individual drug result card
 */
@Composable
fun DrugResultCard(
    drugResult: EnhancedDrugResult,
    onImageClick: () -> Unit,
    onCorrectResult: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = drugResult.finalDrugName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Text(
                        text = "Güven: ${String.format("%.1f", drugResult.finalConfidence * 100)}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    
                    Text(
                        text = "Yöntem: ${drugResult.enhancementMethod.name.replace("_", " ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                // Confidence indicator
                ConfidenceIndicator(
                    confidence = drugResult.finalConfidence,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Original OCR text if different
            if (drugResult.originalRegion.ocrText != drugResult.finalDrugName) {
                Text(
                    text = "Orijinal OCR: \"${drugResult.originalRegion.ocrText}\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(8.dp)
                )
            }
            
            // Drug database match info
            drugResult.drugDatabaseMatch?.let { match ->
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Matched",
                        tint = Color.Green,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = "Veritabanında eşleşti",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Green
                    )
                }
            }
            
            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onImageClick
                ) {
                    Text("Görüntüyü Gör")
                }
                
                TextButton(
                    onClick = { onCorrectResult(drugResult.finalDrugName) }
                ) {
                    Text("Düzelt")
                }
            }
        }
    }
}

// Helper components

@Composable
fun InfoItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun ConfidenceIndicator(
    confidence: Float,
    modifier: Modifier = Modifier
) {
    val color = when {
        confidence > 0.8f -> Color.Green
        confidence > 0.6f -> Color.Yellow
        else -> Color.Red
    }
    
    Box(
        modifier = modifier
            .size(32.dp)
            .background(color.copy(alpha = 0.2f), androidx.compose.foundation.shape.CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${(confidence * 100).toInt()}%",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun VisualRecoveryInfoCard(visualRecovery: DamagedTextRecoveryResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Healing,
                    contentDescription = "Recovery",
                    tint = MaterialTheme.colorScheme.tertiary
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Hasarlı Metin Kurtarma",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Güven: ${String.format("%.1f", visualRecovery.confidence * 100)}%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            
            Text(
                text = "Yöntem: ${visualRecovery.method.name.replace("_", " ")}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun BatchIntegrationCard(
    batchInfo: BatchIntegrationResult,
    onNavigateToBatch: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Batch Entegrasyonu",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            Text(
                text = "${batchInfo.totalItems} öğe eklendi",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { onNavigateToBatch(batchInfo.sessionId) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Batch Oturumunu Görüntüle")
            }
        }
    }
}
