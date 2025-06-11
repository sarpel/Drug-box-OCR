package com.boxocr.simple.ui.enhanced

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.boxocr.simple.repository.EnhancedDatabaseRepository

/**
 * Enhanced matching results screen with multiple match suggestions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedMatchingScreen(
    onMatchConfirmed: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: EnhancedMatchingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val matchingResult by viewModel.matchingResult.collectAsState()
    val categorySettings by viewModel.categorySettings.collectAsState()
    
    var showManualEntry by remember { mutableStateOf(false) }
    var manualEntryText by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Drug Matching Results") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showCategorySettings() }) {
                        Icon(Icons.Default.Tune, contentDescription = "Settings")
                    }
                }
            )
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
            
            // Processing indicator
            if (uiState.isProcessing) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Finding matches...")
                    }
                }
            }
            
            // Scanned text display
            if (uiState.scannedText.isNotBlank()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Scanned Text",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.scannedText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Matching results
            matchingResult?.let { result ->
                MatchingResultsContent(
                    result = result,
                    selectedMatch = uiState.selectedMatch,
                    onMatchSelected = { viewModel.selectMatch(it) },
                    onConfirmMatch = { 
                        val confirmedDrug = viewModel.confirmSelectedMatch()
                        confirmedDrug?.let { onMatchConfirmed(it) }
                    },
                    onRejectMatches = { viewModel.rejectMatches() },
                    onManualEntry = { showManualEntry = true },
                    viewModel = viewModel
                )
            }
        }
    }
    
    // Manual entry dialog
    if (showManualEntry) {
        ManualEntryDialog(
            currentText = manualEntryText,
            onTextChange = { manualEntryText = it },
            onConfirm = {
                val result = viewModel.enterManually(manualEntryText)
                result?.let { onMatchConfirmed(it) }
                showManualEntry = false
                manualEntryText = ""
            },
            onDismiss = { 
                showManualEntry = false
                manualEntryText = ""
            }
        )
    }
    
    // Category settings dialog
    if (uiState.showCategorySettings) {
        CategorySettingsDialog(
            categorySettings = categorySettings,
            onThresholdChanged = { category, threshold ->
                viewModel.updateCategoryThreshold(category, threshold)
            },
            onDismiss = { viewModel.hideCategorySettings() }
        )
    }
}

@Composable
private fun MatchingResultsContent(
    result: EnhancedDatabaseRepository.MultipleMatchResult,
    selectedMatch: EnhancedDatabaseRepository.MatchResult?,
    onMatchSelected: (EnhancedDatabaseRepository.MatchResult) -> Unit,
    onConfirmMatch: () -> Unit,
    onRejectMatches: () -> Unit,
    onManualEntry: () -> Unit,
    viewModel: EnhancedMatchingViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        
        // Recommended action header
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Recommendation",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = viewModel.getRecommendedActionText(result.recommendedAction),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Found ${result.totalMatches} potential matches",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        
        // Primary match (if exists)
        result.primaryMatch?.let { primaryMatch ->
            item {
                Text(
                    text = "Best Match",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            item {
                MatchResultCard(
                    match = primaryMatch,
                    isSelected = selectedMatch == primaryMatch,
                    onSelected = { onMatchSelected(primaryMatch) },
                    isPrimary = true,
                    viewModel = viewModel
                )
            }
        }
        
        // Alternative matches
        if (result.alternativeMatches.isNotEmpty()) {
            item {
                Text(
                    text = "Alternative Matches",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            items(result.alternativeMatches) { match ->
                MatchResultCard(
                    match = match,
                    isSelected = selectedMatch == match,
                    onSelected = { onMatchSelected(match) },
                    isPrimary = false,
                    viewModel = viewModel
                )
            }
        }
        
        // Action buttons
        item {
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Confirm button
                Button(
                    onClick = onConfirmMatch,
                    enabled = selectedMatch != null,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Confirm")
                }
                
                // Manual entry button
                OutlinedButton(
                    onClick = onManualEntry,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Manual")
                }
                
                // Reject button
                OutlinedButton(
                    onClick = onRejectMatches,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Rescan")
                }
            }
        }
    }
}

@Composable
private fun MatchResultCard(
    match: EnhancedDatabaseRepository.MatchResult,
    isSelected: Boolean,
    onSelected: () -> Unit,
    isPrimary: Boolean,
    viewModel: EnhancedMatchingViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onSelected
            ),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> MaterialTheme.colorScheme.secondaryContainer
                isPrimary -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(
                2.dp, 
                MaterialTheme.colorScheme.primary
            )
        } else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            
            // Drug name and selection indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = match.drugName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (isSelected) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Confidence and match type
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Confidence score
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = viewModel.getMatchTypeColor(match.matchType).copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "${match.confidence}%",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = viewModel.getMatchTypeColor(match.matchType)
                    )
                }
                
                // Match type badge
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = match.matchType.name.replace("_", " "),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Additional details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Algorithm: ${match.algorithm}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "Category: ${match.category}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Brand name indicator
            if (match.isGenericMatch && match.brandName != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.SwapHoriz,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Brand '${match.brandName}' → Generic",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
private fun ManualEntryDialog(
    currentText: String,
    onTextChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manual Drug Entry") },
        text = {
            Column {
                Text("Enter the drug name manually:")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = currentText,
                    onValueChange = onTextChange,
                    label = { Text("Drug Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = currentText.isNotBlank()
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun CategorySettingsDialog(
    categorySettings: Map<String, Int>,
    onThresholdChanged: (String, Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Category Matching Thresholds") },
        text = {
            LazyColumn(
                modifier = Modifier.height(400.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(categorySettings.entries.toList()) { (category, threshold) ->
                    Column {
                        Text(
                            text = category.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${threshold}%",
                                modifier = Modifier.width(48.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            Slider(
                                value = threshold.toFloat(),
                                onValueChange = { newValue ->
                                    onThresholdChanged(category, newValue.toInt())
                                },
                                valueRange = 10f..100f,
                                steps = 17, // 10, 15, 20, ..., 95, 100
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}
