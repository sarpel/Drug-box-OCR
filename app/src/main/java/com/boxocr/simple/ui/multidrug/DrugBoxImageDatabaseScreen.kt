package com.boxocr.simple.ui.multidrug

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.boxocr.simple.R
import com.boxocr.simple.database.*
import com.boxocr.simple.repository.*

/**
 * Drug Box Image Database Screen - Phase 2 Week 4 Enhancement
 * 
 * Advanced interface for managing user-provided drug box images:
 * - Batch image upload with intelligent processing
 * - Visual quality assessment and feedback
 * - Smart categorization and organization
 * - Database optimization tools
 * - Comprehensive drug box image management
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrugBoxImageDatabaseScreen(
    onNavigateBack: () -> Unit,
    viewModel: DrugBoxImageDatabaseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.processSelectedImages(uris)
        }
    }
    
    // Drag and drop state
    var showUploadDialog by remember { mutableStateOf(false) }
    var showOptimizationDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "İlaç Kutusu Görsel Veritabanı",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    IconButton(onClick = { showOptimizationDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Optimizasyon")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showUploadDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Görüntü Ekle") },
                containerColor = MaterialTheme.colorScheme.primary
            )
        }
    ) { paddingValues ->
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            
            // Database statistics
            DatabaseStatisticsCard(
                totalImages = uiState.totalImages,
                totalDrugs = uiState.totalDrugs,
                databaseSize = uiState.databaseSize,
                lastOptimized = uiState.lastOptimized
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Processing status
            if (uiState.isProcessing) {
                ProcessingStatusCard(
                    status = uiState.processingStatus,
                    progress = uiState.processingProgress
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Filter and search
            SearchAndFilterSection(
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = viewModel::updateSearchQuery,
                selectedFilter = uiState.selectedFilter,
                onFilterChange = viewModel::updateFilter
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Image grid
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                uiState.images.isEmpty() -> {
                    EmptyStateCard(
                        onAddImages = { showUploadDialog = true }
                    )
                }
                
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 150.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.images) { image ->
                            DrugBoxImageCard(
                                image = image,
                                onClick = { viewModel.selectImage(image) },
                                onLongClick = { viewModel.showImageOptions(image) }
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Upload dialog
    if (showUploadDialog) {
        ImageUploadDialog(
            onDismiss = { showUploadDialog = false },
            onSelectImages = { imagePickerLauncher.launch("image/*") },
            onBulkUpload = viewModel::startBulkUpload
        )
    }
    
    // Optimization dialog
    if (showOptimizationDialog) {
        DatabaseOptimizationDialog(
            optimization = uiState.optimizationResult,
            onDismiss = { showOptimizationDialog = false },
            onOptimize = viewModel::optimizeDatabase
        )
    }
    
    // Image details bottom sheet
    uiState.selectedImage?.let { image ->
        ImageDetailsBottomSheet(
            image = image,
            onDismiss = viewModel::clearSelectedImage,
            onDelete = viewModel::deleteImage,
            onUpdateMetadata = viewModel::updateImageMetadata
        )
    }
    
    // Error handling
    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            // Show snackbar or toast
        }
    }
}

@Composable
fun DatabaseStatisticsCard(
    totalImages: Int,
    totalDrugs: Int,
    databaseSize: String,
    lastOptimized: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Veritabanı İstatistikleri",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatisticItem(
                    value = totalImages.toString(),
                    label = "Toplam\nGörüntü",
                    icon = Icons.Default.Image
                )
                
                StatisticItem(
                    value = totalDrugs.toString(),
                    label = "Farklı\nİlaç",
                    icon = Icons.Default.Medication
                )
                
                StatisticItem(
                    value = databaseSize,
                    label = "Veritabanı\nBoyutu",
                    icon = Icons.Default.Storage
                )
            }
            
            if (lastOptimized.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Son optimizasyon: $lastOptimized",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun StatisticItem(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ProcessingStatusCard(
    status: String,
    progress: Float
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    progress = progress,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 3.dp
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = status,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            if (progress > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchAndFilterSection(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedFilter: DrugBoxFilter,
    onFilterChange: (DrugBoxFilter) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            label = { Text("İlaç ara...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedFilter.displayName,
                onValueChange = { },
                readOnly = true,
                label = { Text("Filtre") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .menuAnchor()
                    .width(120.dp)
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DrugBoxFilter.values().forEach { filter ->
                    DropdownMenuItem(
                        text = { Text(filter.displayName) },
                        onClick = {
                            onFilterChange(filter)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DrugBoxImageCard(
    image: DrugBoxImageWithMetadata,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(image.imagePath)
                    .crossfade(true)
                    .build(),
                contentDescription = image.drugName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.ic_launcher_foreground),
                error = painterResource(R.drawable.ic_launcher_foreground)
            )
            
            // Quality indicator
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .background(
                        color = getQualityColor(image.qualityScore),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${(image.qualityScore * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            }
            
            // Drug name overlay
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .background(
                        color = Color.Black.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(topEnd = 8.dp)
                    )
                    .padding(8.dp)
            ) {
                Text(
                    text = image.drugName,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    maxLines = 2
                )
            }
            
            // Condition indicator
            Icon(
                imageVector = getConditionIcon(image.condition),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp)
                    .size(16.dp)
            )
        }
    }
}

@Composable
fun EmptyStateCard(
    onAddImages: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.PhotoLibrary,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Henüz görüntü eklenmemiş",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "İlaç kutularının görüntülerini ekleyerek\nhasarlı metin kurtarma sistemini güçlendirin",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onAddImages
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Görüntü Ekle")
            }
        }
    }
}

// Helper functions

fun getQualityColor(qualityScore: Float): Color {
    return when {
        qualityScore >= 0.8f -> Color(0xFF4CAF50) // Green
        qualityScore >= 0.6f -> Color(0xFFFF9800) // Orange
        qualityScore >= 0.4f -> Color(0xFFFF5722) // Red-Orange
        else -> Color(0xFFF44336) // Red
    }
}

fun getConditionIcon(condition: DrugBoxCondition): androidx.compose.ui.graphics.vector.ImageVector {
    return when (condition) {
        DrugBoxCondition.PERFECT -> Icons.Default.CheckCircle
        DrugBoxCondition.GOOD -> Icons.Default.Check
        DrugBoxCondition.WORN -> Icons.Default.Warning
        DrugBoxCondition.DAMAGED -> Icons.Default.Error
        DrugBoxCondition.SEVERELY_DAMAGED -> Icons.Default.ErrorOutline
        else -> Icons.Default.Help
    }
}

// Placeholder composables for dialogs and bottom sheets
@Composable
fun ImageUploadDialog(
    onDismiss: () -> Unit,
    onSelectImages: () -> Unit,
    onBulkUpload: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Görüntü Ekle") },
        text = {
            Column {
                Text("İlaç kutusu görüntülerini nasıl eklemek istiyorsunuz?")
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        onSelectImages()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Galeriden Seç")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedButton(
                    onClick = {
                        onBulkUpload()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Toplu Yükleme")
                }
            }
        },
        confirmButton = { },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}

@Composable
fun DatabaseOptimizationDialog(
    optimization: DatabaseOptimizationResult?,
    onDismiss: () -> Unit,
    onOptimize: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Veritabanı Optimizasyonu") },
        text = {
            if (optimization != null) {
                Column {
                    Text("Optimizasyon tamamlandı:")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• ${optimization.duplicatesRemoved} duplikat kaldırıldı")
                    Text("• ${optimization.featuresUpdated} özellik güncellendi")
                    Text("• ${optimization.totalSpaceSaved} MB alan tasarrufu")
                }
            } else {
                Text("Veritabanını optimize etmek istiyor musunuz? Bu işlem:\n\n• Duplikat görüntüleri kaldırır\n• Benzer görüntüleri birleştirir\n• Özellik vektörlerini günceller")
            }
        },
        confirmButton = {
            if (optimization == null) {
                Button(onClick = onOptimize) {
                    Text("Optimize Et")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (optimization != null) "Tamam" else "İptal")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageDetailsBottomSheet(
    image: DrugBoxImageWithMetadata,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onUpdateMetadata: (DrugBoxImageMetadata) -> Unit
) {
    // Implementation for bottom sheet with image details
    // This would be a modal bottom sheet with detailed information
}

// Data classes and enums

data class DrugBoxImageWithMetadata(
    val id: Long,
    val drugName: String,
    val brandName: String,
    val imagePath: String,
    val condition: DrugBoxCondition,
    val angle: DrugBoxAngle,
    val lighting: DrugBoxLighting,
    val qualityScore: Float,
    val fileSize: Long,
    val createdAt: Long
)

enum class DrugBoxFilter(val displayName: String) {
    ALL("Tümü"),
    PERFECT("Mükemmel"),
    GOOD("İyi"),
    DAMAGED("Hasarlı"),
    RECENT("Son Eklenen"),
    HIGH_QUALITY("Yüksek Kalite"),
    LOW_QUALITY("Düşük Kalite")
}
