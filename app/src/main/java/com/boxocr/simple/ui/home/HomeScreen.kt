package com.boxocr.simple.ui.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Home Screen - Main entry point
 * 
 * Features:
 * 1. Load database from file
 * 2. View current database items
 * 3. Navigate to camera for OCR
 * 4. Navigate to settings
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCamera: () -> Unit,
    onNavigateToBatchScanning: () -> Unit,
    onNavigateToTemplates: () -> Unit,
    onNavigateToAIAssistant: () -> Unit,
    onNavigateToAdvancedAI: () -> Unit,
    onNavigateToIoTIntegration: () -> Unit,
    onNavigateToCustomAI: () -> Unit,
    onNavigateToDrugBoxDatabase: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // Refresh scans when screen becomes visible
    LaunchedEffect(Unit) {
        viewModel.refreshRecentScans()
    }
    
    // File picker for database loading
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.loadDatabase(context, it) }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Box Name OCR") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToSettings
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Database section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Database",
                            style = MaterialTheme.typography.titleLarge
                        )
                        
                        Text(
                            text = "${uiState.databaseItems.size} items",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Button(
                        onClick = { filePickerLauncher.launch("text/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Load Database File")
                    }
                    
                    if (uiState.databaseError != null) {
                        Text(
                            text = "Error: ${uiState.databaseError}",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            
            // Action buttons section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Scanning Modes",
                        style = MaterialTheme.typography.titleLarge
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Single scan button
                        OutlinedButton(
                            onClick = onNavigateToCamera,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Single Scan")
                            }
                        }
                        
                        // Batch scan button
                        Button(
                            onClick = onNavigateToBatchScanning,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.QueuePlayNext, contentDescription = null)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Prescription Mode")
                            }
                        }
                    }
                    
                    Text(
                        text = "Use Prescription Mode for scanning multiple drugs in sequence",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Templates button - Phase 2 Feature
                    OutlinedButton(
                        onClick = onNavigateToTemplates,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null)
                            Text("Browse Prescription Templates")
                        }
                    }
                    
                    // 🧠 AI Assistant button - Phase 5 Revolutionary Feature
                    Button(
                        onClick = onNavigateToAIAssistant,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "🧠", style = MaterialTheme.typography.titleMedium)
                            Column(
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    text = "AI Tıbbi Asistan",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = "Türkiye'nin ilk tıbbi AI asistanı",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }
            
            // 🚀 PRODUCTION FEATURES - PHASE 6
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "🚀 Üretim Özellikleri",
                        style = MaterialTheme.typography.titleLarge
                    )
                    
                    Text(
                        text = "Gelişmiş AI ve IoT entegrasyonu",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Advanced AI Models Integration
                    Button(
                        onClick = onNavigateToAdvancedAI,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Psychology, contentDescription = null)
                            Column(
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    text = "Gelişmiş AI Modelleri",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = "GPT-4, Claude, Gemini Pro entegrasyonu",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                    
                    // IoT Medical Device Integration
                    Button(
                        onClick = onNavigateToIoTIntegration,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Devices, contentDescription = null)
                            Column(
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    text = "IoT Tıbbi Cihaz Entegrasyonu",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = "Akıllı tıbbi cihazlar ve vital signs",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                    
                    // Custom AI Integration
                    Button(
                        onClick = onNavigateToCustomAI,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null)
                            Column(
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    text = "Özel AI Entegrasyonu",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = "Yerel modeller ve özel AI sağlayıcıları",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                    
                    // Multi-Drug Database Management - Phase 2 Week 4
                    Button(
                        onClick = onNavigateToDrugBoxDatabase,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                            Column(
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    text = "İlaç Kutusu Görsel Veritabanı",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = "Hasarlı metin kurtarma için görsel database",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }
            
            // Recent scans section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Recent Scans",
                        style = MaterialTheme.typography.titleLarge
                    )
                    
                    if (uiState.recentScans.isEmpty()) {
                        Text(
                            text = "No scans yet. Use the camera to scan your first box!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.height(200.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(uiState.recentScans.take(10)) { scan ->
                                ScanResultItem(scan)
                            }
                        }
                    }
                }
            }
            
            // Database preview
            if (uiState.databaseItems.isNotEmpty()) {
                Card(
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Database Preview",
                            style = MaterialTheme.typography.titleLarge
                        )
                        
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(uiState.databaseItems.take(20)) { item ->
                                DatabaseItem(item)
                            }
                            
                            if (uiState.databaseItems.size > 20) {
                                item {
                                    Text(
                                        text = "... and ${uiState.databaseItems.size - 20} more items",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScanResultItem(scan: ScanResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = scan.matchedText ?: scan.scannedText,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1
            )
            Text(
                text = "Scanned: ${scan.timestamp}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DatabaseItem(item: String) {
    Text(
        text = "• $item",
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}
