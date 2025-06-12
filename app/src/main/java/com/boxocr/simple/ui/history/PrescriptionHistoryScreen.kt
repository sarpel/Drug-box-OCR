package com.boxocr.simple.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.boxocr.simple.data.PrescriptionHistory
import com.boxocr.simple.data.PatientInfo
import com.boxocr.simple.data.PrescriptionStatistics
import java.text.SimpleDateFormat
import java.util.*

/**
 * Prescription History Screen - Phase 3 Quality of Life Feature
 * 
 * Features:
 * 1. Complete prescription history with timestamps
 * 2. Patient information integration
 * 3. Prescription statistics and patterns
 * 4. Export capabilities
 * 5. Search and filtering
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrescriptionHistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: PrescriptionHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFilters by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Prescription History") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                    IconButton(onClick = { showExportDialog = true }) {
                        Icon(Icons.Default.Download, contentDescription = "Export")
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
            // Statistics summary
            PrescriptionStatisticsCard(
                modifier = Modifier.padding(16.dp),
                statistics = uiState.statistics
            )
            
            // Filters (if expanded)
            if (showFilters) {
                PrescriptionFilters(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    currentFilter = uiState.currentFilter,
                    onFilterChanged = viewModel::updateFilter
                )
            }
            
            // History list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.filteredHistory) { prescription ->
                    PrescriptionHistoryCard(
                        prescription = prescription,
                        onClick = { viewModel.selectPrescription(prescription) }
                    )
                }
                
                if (uiState.filteredHistory.isEmpty()) {
                    item {
                        EmptyHistoryPlaceholder()
                    }
                }
            }
        }
    }
    
    // Export dialog
    if (showExportDialog) {
        ExportDialog(
            onDismiss = { showExportDialog = false },
            onExport = { format ->
                viewModel.exportHistory(format)
                showExportDialog = false
            }
        )
    }
    
    // Prescription details dialog
    uiState.selectedPrescription?.let { prescription ->
        PrescriptionDetailsDialog(
            prescription = prescription,
            onDismiss = { viewModel.clearSelection() }
        )
    }
}

/**
 * Statistics summary card
 */
@Composable
private fun PrescriptionStatisticsCard(
    modifier: Modifier = Modifier,
    statistics: PrescriptionStatistics
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatisticItem(
                    title = "Total Prescriptions",
                    value = statistics.totalPrescriptions.toString(),
                    icon = Icons.Default.Assignment
                )
                
                StatisticItem(
                    title = "Unique Drugs",
                    value = statistics.uniqueDrugs.toString(),
                    icon = Icons.Default.Medication
                )
                
                StatisticItem(
                    title = "This Month",
                    value = statistics.thisMonth.toString(),
                    icon = Icons.Default.CalendarMonth
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatisticItem(
                    title = "Avg per Day",
                    value = String.format("%.1f", statistics.averagePerDay),
                    icon = Icons.Default.TrendingUp
                )
                
                StatisticItem(
                    title = "Most Common",
                    value = statistics.mostCommonDrug,
                    icon = Icons.Default.Star
                )
            }
        }
    }
}

/**
 * Individual statistic item
 */
@Composable
private fun StatisticItem(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Prescription filters
 */
@Composable
private fun PrescriptionFilters(
    modifier: Modifier = Modifier,
    currentFilter: HistoryFilter,
    onFilterChanged: (HistoryFilter) -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Filters",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Time period filter
            Text("Time Period:", style = MaterialTheme.typography.bodySmall)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = currentFilter.timePeriod == TimePeriod.ALL,
                    onClick = { onFilterChanged(currentFilter.copy(timePeriod = TimePeriod.ALL)) },
                    label = { Text("All") }
                )
                FilterChip(
                    selected = currentFilter.timePeriod == TimePeriod.TODAY,
                    onClick = { onFilterChanged(currentFilter.copy(timePeriod = TimePeriod.TODAY)) },
                    label = { Text("Today") }
                )
                FilterChip(
                    selected = currentFilter.timePeriod == TimePeriod.WEEK,
                    onClick = { onFilterChanged(currentFilter.copy(timePeriod = TimePeriod.WEEK)) },
                    label = { Text("Week") }
                )
                FilterChip(
                    selected = currentFilter.timePeriod == TimePeriod.MONTH,
                    onClick = { onFilterChanged(currentFilter.copy(timePeriod = TimePeriod.MONTH)) },
                    label = { Text("Month") }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Patient filter
            if (currentFilter.patientName.isNotBlank()) {
                Text("Patient: ${currentFilter.patientName}", 
                     style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

/**
 * Prescription history card
 */
@Composable
private fun PrescriptionHistoryCard(
    prescription: PrescriptionHistory,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = formatDate(prescription.timestamp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (prescription.patientInfo != null) {
                        Text(
                            text = "Patient: ${prescription.patientInfo.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "${prescription.drugCount} drugs",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    StatusBadge(status = prescription.status)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Drug preview (first few drugs)
            val previewDrugs = prescription.drugs.take(3)
            previewDrugs.forEach { drug ->
                Text(
                    text = "• $drug",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            if (prescription.drugs.size > 3) {
                Text(
                    text = "... and ${prescription.drugs.size - 3} more",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Status badge for prescription
 */
@Composable
private fun StatusBadge(status: PrescriptionStatus) {
    val (color, text) = when (status) {
        PrescriptionStatus.COMPLETED -> MaterialTheme.colorScheme.primary to "Completed"
        PrescriptionStatus.PENDING -> MaterialTheme.colorScheme.secondary to "Pending"
        PrescriptionStatus.CANCELLED -> MaterialTheme.colorScheme.error to "Cancelled"
    }
    
    Box(
        modifier = Modifier
            .background(
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontSize = 10.sp
        )
    }
}

/**
 * Empty history placeholder
 */
@Composable
private fun EmptyHistoryPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Assignment,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No prescriptions found",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Start scanning drugs to build your prescription history",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Export dialog
 */
@Composable
private fun ExportDialog(
    onDismiss: () -> Unit,
    onExport: (ExportFormat) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Export History") },
        text = {
            Column {
                Text("Choose export format:")
                Spacer(modifier = Modifier.height(16.dp))
                
                Row {
                    Button(
                        onClick = { onExport(ExportFormat.CSV) }
                    ) {
                        Text("CSV")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onExport(ExportFormat.PDF) }
                    ) {
                        Text("PDF")
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Prescription details dialog
 */
@Composable
private fun PrescriptionDetailsDialog(
    prescription: PrescriptionHistory,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Prescription Details") },
        text = {
            LazyColumn {
                item {
                    Text("Date: ${formatDate(prescription.timestamp)}")
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    prescription.patientInfo?.let { patient ->
                        Text("Patient: ${patient.name}")
                        if (patient.id.isNotBlank()) {
                            Text("ID: ${patient.id}")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    Text("Status: ${prescription.status}")
                    Text("Drugs (${prescription.drugCount}):")
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                items(prescription.drugs) { drug ->
                    Text("• $drug", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

/**
 * Format timestamp to readable date
 */
private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

/**
 * Data classes for history feature
 */
enum class TimePeriod { ALL, TODAY, WEEK, MONTH }
enum class PrescriptionStatus { COMPLETED, PENDING, CANCELLED }
enum class ExportFormat { CSV, PDF }

data class HistoryFilter(
    val timePeriod: TimePeriod = TimePeriod.ALL,
    val patientName: String = "",
    val status: PrescriptionStatus? = null
)
