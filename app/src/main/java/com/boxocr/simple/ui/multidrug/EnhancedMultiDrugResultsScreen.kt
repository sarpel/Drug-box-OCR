package com.boxocr.simple.ui.multidrug

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.boxocr.simple.data.*
import com.boxocr.simple.ui.theme.Spacing
import kotlinx.coroutines.delay

/**
 * Enhanced Multi-Drug Results Display with advanced verification
 * Comprehensive results screen for multiple drug detections
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedMultiDrugResultsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToBatch: (List<String>) -> Unit,
    viewModel: EnhancedMultiDrugResultsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        viewModel.loadResults()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            text = "Multi-Drug Results",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${uiState.results.size} drugs detected",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Batch processing action
                    IconButton(
                        onClick = {
                            val drugNames = uiState.results
                                .filter { it.isVerified }
                                .map { it.detectedName }
                            if (drugNames.isNotEmpty()) {
                                onNavigateToBatch(drugNames)
                            }
                        },
                        enabled = uiState.results.any { it.isVerified }
                    ) {
                        Badge(
                            badgeContent = { 
                                Text(uiState.results.count { it.isVerified }.toString()) 
                            }
                        ) {
                            Icon(Icons.Default.Batch, contentDescription = "Process Batch")
                        }
                    }
                    
                    // Export action
                    IconButton(onClick = { viewModel.exportResults() }) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Export")
                    }
                    
                    // More options
                    var showMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Select All") },
                            onClick = {
                                viewModel.selectAll()
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.SelectAll, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Clear All") },
                            onClick = {
                                viewModel.clearSelection()
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Clear, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Retry Failed") },
                            onClick = {
                                viewModel.retryFailedDetections()
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null) }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState.results.any { it.isVerified }) {
                ExtendedFloatingActionButton(
                    onClick = {
                        val drugNames = uiState.results
                            .filter { it.isVerified }
                            .map { it.detectedName }
                        onNavigateToBatch(drugNames)
                    },
                    text = { Text("Process ${uiState.results.count { it.isVerified }} Drugs") },
                    icon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                    containerColor = MaterialTheme.colorScheme.primary
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Processing status if still active
            if (uiState.isProcessing) {
                ProcessingStatusCard(
                    processingStatus = uiState.processingStatus,
                    modifier = Modifier.padding(Spacing.medium)
                )
            }
            
            // Results summary
            ResultsSummaryCard(
                results = uiState.results,
                modifier = Modifier.padding(horizontal = Spacing.medium)
            )
            
            Spacer(modifier = Modifier.height(Spacing.medium))
            
            // Results list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = Spacing.medium),
                verticalArrangement = Arrangement.spacedBy(Spacing.medium)
            ) {
                itemsIndexed(uiState.results) { index, result ->
                    EnhancedDrugResultCard(
                        result = result,
                        index = index,
                        onVerify = { viewModel.verifyResult(index) },
                        onCorrect = { correction -> viewModel.correctResult(index, correction) },
                        onRemove = { viewModel.removeResult(index) },
                        onViewDetails = { viewModel.showDetails(index) },
                        modifier = Modifier.animateItemPlacement()
                    )
                }
                
                // Add spacing for FAB
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
    
    // Handle UI events
    LaunchedEffect(uiState.message) {
        uiState.message?.let { message ->
            // Show snackbar or toast
            viewModel.clearMessage()
        }
    }
}

/**
 * Processing status card for active multi-drug processing
 */
@Composable
private fun ProcessingStatusCard(
    processingStatus: MultiDrugProcessingStatus,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                Text(
                    text = processingStatus.currentStage,
                    fontWeight = FontWeight.Medium
                )
            }
            
            LinearProgressIndicator(
                progress = processingStatus.progress,
                modifier = Modifier.fillMaxWidth()
            )
            
            Text(
                text = "Processing ${processingStatus.drugProcessingStates.size} drugs...",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Results summary with statistics
 */
@Composable
private fun ResultsSummaryCard(
    results: List<MultiDrugResult>,
    modifier: Modifier = Modifier
) {
    val verified = results.count { it.isVerified }
    val highConfidence = results.count { it.confidence >= 0.8f }
    val needsReview = results.count { it.confidence < 0.6f || it.hasVisualMatch.not() }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SummaryItem(
                count = results.size,
                label = "Total",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            SummaryItem(
                count = verified,
                label = "Verified",
                color = Color.Green
            )
            SummaryItem(
                count = highConfidence,
                label = "High Conf.",
                color = Color.Blue
            )
            SummaryItem(
                count = needsReview,
                label = "Review",
                color = Color.Orange
            )
        }
    }
}

@Composable
private fun SummaryItem(
    count: Int,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Enhanced drug result card with comprehensive information
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnhancedDrugResultCard(
    result: MultiDrugResult,
    index: Int,
    onVerify: () -> Unit,
    onCorrect: (String) -> Unit,
    onRemove: () -> Unit,
    onViewDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showCorrectionDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(
            containerColor = when {
                result.isVerified -> MaterialTheme.colorScheme.primaryContainer
                result.confidence >= 0.8f -> MaterialTheme.colorScheme.surface
                result.confidence >= 0.6f -> MaterialTheme.colorScheme.surfaceVariant
                else -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(Spacing.medium)
        ) {
            // Main result row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Drug info
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = result.detectedName.ifEmpty { "Unknown Drug" },
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Confidence indicator
                        ConfidenceChip(confidence = result.confidence)
                        
                        // Visual match indicator
                        if (result.hasVisualMatch) {
                            Chip(
                                onClick = { },
                                label = { Text("Visual Match", fontSize = 10.sp) },
                                colors = ChipDefaults.chipColors(
                                    containerColor = Color.Green.copy(alpha = 0.2f)
                                ),
                                modifier = Modifier.height(24.dp)
                            )
                        }
                        
                        // Recovery method indicator
                        if (result.recoveryMethod.isNotEmpty()) {
                            Chip(
                                onClick = { },
                                label = { Text(result.recoveryMethod, fontSize = 10.sp) },
                                colors = ChipDefaults.chipColors(
                                    containerColor = Color.Blue.copy(alpha = 0.2f)
                                ),
                                modifier = Modifier.height(24.dp)
                            )
                        }
                    }
                }
                
                // Actions
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (result.isVerified) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Verified",
                            tint = Color.Green,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        IconButton(
                            onClick = onVerify,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Verify",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    
                    IconButton(
                        onClick = { showCorrectionDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Correct",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Remove",
                            modifier = Modifier.size(16.dp),
                            tint = Color.Red
                        )
                    }
                }
            }
            
            // Expanded details
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = Spacing.medium),
                    verticalArrangement = Arrangement.spacedBy(Spacing.small)
                ) {
                    Divider()
                    
                    // Detection details
                    DetailRow("OCR Text", result.ocrText)
                    DetailRow("Confidence", "${(result.confidence * 100).toInt()}%")
                    DetailRow("Processing Time", "${result.processingTimeMs}ms")
                    
                    if (result.matchedDrugInfo != null) {
                        DetailRow("ATC Code", result.matchedDrugInfo.atcCode)
                        DetailRow("Manufacturer", result.matchedDrugInfo.manufacturer)
                        DetailRow("Form", result.matchedDrugInfo.form)
                    }
                    
                    // Drug box image if available
                    result.croppedDrugBoxBitmap?.let { bitmap ->
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Detected Region:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Drug box region",
                                modifier = Modifier
                                    .height(80.dp)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                    
                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onViewDetails,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Icon(
                                Icons.Default.Visibility,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Details")
                        }
                        
                        if (!result.isVerified) {
                            Button(
                                onClick = onVerify,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Green
                                )
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Verify")
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Correction dialog
    if (showCorrectionDialog) {
        DrugCorrectionDialog(
            currentName = result.detectedName,
            onConfirm = { correction ->
                onCorrect(correction)
                showCorrectionDialog = false
            },
            onDismiss = { showCorrectionDialog = false }
        )
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun ConfidenceChip(confidence: Float) {
    val color = when {
        confidence >= 0.8f -> Color.Green
        confidence >= 0.6f -> Color.Orange
        else -> Color.Red
    }
    
    Chip(
        onClick = { },
        label = { Text("${(confidence * 100).toInt()}%", fontSize = 10.sp) },
        colors = ChipDefaults.chipColors(
            containerColor = color.copy(alpha = 0.2f),
            labelColor = color
        ),
        modifier = Modifier.height(24.dp)
    )
}

@Composable
private fun DrugCorrectionDialog(
    currentName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var correctedName by remember { mutableStateOf(currentName) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Correct Drug Name") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Current: $currentName")
                OutlinedTextField(
                    value = correctedName,
                    onValueChange = { correctedName = it },
                    label = { Text("Correct Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(correctedName) },
                enabled = correctedName.isNotBlank() && correctedName != currentName
            ) {
                Text("Correct")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
