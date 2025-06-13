package com.boxocr.simple.ui.production

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boxocr.simple.ai.*
import com.boxocr.simple.data.LocalModelType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 🔧 PRODUCTION FEATURE UI: CUSTOM AI INTEGRATION
 * 
 * Revolutionary UI for custom and local AI model management:
 * - Custom AI provider configuration (OpenAI, Anthropic, local endpoints)
 * - Local AI model management (TensorFlow Lite, ONNX, PyTorch)
 * - Multi-model consensus building
 * - Turkish medical model marketplace
 * - Private cloud AI integration
 * 
 * Enterprise-grade AI configuration interface with Turkish localization
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomAIIntegrationScreen(
    onNavigateBack: () -> Unit,
    viewModel: CustomAIIntegrationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        "AI Sağlayıcılar" to Icons.Default.Cloud,
        "Yerel Modeller" to Icons.Default.Storage,
        "Konfigürasyon" to Icons.Default.Settings,
        "Marketplace" to Icons.Default.Store,
        "Performans" to Icons.Default.Speed
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {
        // Header with AI Integration Status
        CustomAIHeader(
            onNavigateBack = onNavigateBack,
            integrationStatus = uiState.integrationStatus
        )

        // Tab Navigation
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, (title, icon) ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    icon = { Icon(icon, contentDescription = title, modifier = Modifier.size(20.dp)) },
                    text = { Text(title, fontSize = 12.sp) }
                )
            }
        }

        // Tab Content
        when (selectedTab) {
            0 -> AIProvidersTab(viewModel = viewModel, uiState = uiState)
            1 -> LocalModelsTab(viewModel = viewModel, uiState = uiState)
            2 -> ConfigurationTab(viewModel = viewModel, uiState = uiState)
            3 -> MarketplaceTab(viewModel = viewModel, uiState = uiState)
            4 -> PerformanceTab(viewModel = viewModel, uiState = uiState)
        }
    }
}

@Composable
private fun CustomAIHeader(
    onNavigateBack: () -> Unit,
    integrationStatus: AIIntegrationStatus
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Geri",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Custom AI",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "🔧 Özel AI Entegrasyonu",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Çoklu AI Sağlayıcı & Yerel Model Yönetimi",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AI Integration Status Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AIIntegrationStatusCard(
                    title = "Özel Sağlayıcı",
                    value = "${integrationStatus.activeCustomProviders}/${integrationStatus.totalCustomProviders}",
                    icon = Icons.Default.Cloud,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                AIIntegrationStatusCard(
                    title = "Yerel Model",
                    value = "${integrationStatus.loadedLocalModels}/${integrationStatus.totalLocalModels}",
                    icon = Icons.Default.Storage,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
                AIIntegrationStatusCard(
                    title = "Günlük İstek",
                    value = "${integrationStatus.totalInferencesToday}",
                    icon = Icons.Default.Analytics,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
                AIIntegrationStatusCard(
                    title = "Sistem Sağlığı",
                    value = when (integrationStatus.systemHealth) {
                        SystemHealth.EXCELLENT -> "Mükemmel"
                        SystemHealth.GOOD -> "İyi"
                        SystemHealth.FAIR -> "Orta"
                        SystemHealth.POOR -> "Zayıf"
                        SystemHealth.CRITICAL -> "Kritik"
                        else -> "Bilinmiyor"
                    },
                    icon = Icons.Default.HealthAndSafety,
                    color = when (integrationStatus.systemHealth) {
                        SystemHealth.EXCELLENT, SystemHealth.GOOD -> MaterialTheme.colorScheme.primary
                        SystemHealth.FAIR -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.error
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun AIIntegrationStatusCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = color,
                textAlign = TextAlign.Center
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun AIProvidersTab(
    viewModel: CustomAIIntegrationViewModel,
    uiState: CustomAIIntegrationUiState
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Add Custom Provider Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "➕ Yeni AI Sağlayıcı Ekle",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "OpenAI, Anthropic, yerel sunucular veya özel AI hizmetleri için konfigürasyon ekleyin",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { viewModel.showAddProviderDialog() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sağlayıcı Ekle")
                        }
                        
                        OutlinedButton(
                            onClick = { viewModel.testAllProviders() },
                            modifier = Modifier.weight(1f),
                            enabled = !uiState.isTesting
                        ) {
                            if (uiState.isTesting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Tümünü Test Et")
                        }
                    }
                }
            }
        }
        
        // Default Providers
        item {
            Text(
                text = "🌐 Varsayılan AI Sağlayıcıları",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        items(uiState.defaultProviders) { provider ->
            AIProviderCard(
                provider = provider,
                onConfigure = { viewModel.configureProvider(provider.id) },
                onTest = { viewModel.testProvider(provider.id) },
                onToggle = { viewModel.toggleProvider(provider.id) }
            )
        }
        
        // Custom Providers
        if (uiState.customProviders.isNotEmpty()) {
            item {
                Text(
                    text = "⚙️ Özel AI Sağlayıcıları (${uiState.customProviders.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            
            items(uiState.customProviders) { provider ->
                AIProviderCard(
                    provider = provider,
                    onConfigure = { viewModel.configureProvider(provider.id) },
                    onTest = { viewModel.testProvider(provider.id) },
                    onToggle = { viewModel.toggleProvider(provider.id) },
                    onDelete = { viewModel.deleteProvider(provider.id) },
                    isCustom = true
                )
            }
        }
    }
}

@Composable
private fun AIProviderCard(
    provider: CustomAIConfiguration,
    onConfigure: () -> Unit,
    onTest: () -> Unit,
    onToggle: () -> Unit,
    onDelete: (() -> Unit)? = null,
    isCustom: Boolean = false
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (provider.isActive) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            else 
                MaterialTheme.colorScheme.surface
        ),
        border = if (provider.isActive) 
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Provider Icon
                    Icon(
                        when (provider.providerType) {
                            AIProviderType.OPENAI -> Icons.Default.Psychology
                            AIProviderType.ANTHROPIC -> Icons.Default.AutoAwesome
                            AIProviderType.GOOGLE -> Icons.Default.Search
                            AIProviderType.CUSTOM -> Icons.Default.Extension
                            AIProviderType.LOCAL -> Icons.Default.Storage
                            else -> Icons.Default.Cloud
                        },
                        contentDescription = provider.providerType.name,
                        modifier = Modifier.size(24.dp),
                        tint = if (provider.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = provider.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (isCustom) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "ÖZEL",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                            if (provider.turkishMedicalOptimized) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "🇹🇷 TIBBİ",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                        Text(
                            text = provider.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "Endpoint: ${provider.endpoint.take(40)}${if (provider.endpoint.length > 40) "..." else ""}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
                
                // Active Status Switch
                Switch(
                    checked = provider.isActive,
                    onCheckedChange = { onToggle() }
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Provider Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ProviderDetailItem(
                    label = "Max Tokens",
                    value = "${provider.maxTokens}",
                    icon = Icons.Default.Token
                )
                ProviderDetailItem(
                    label = "Timeout",
                    value = "${provider.timeout}s",
                    icon = Icons.Default.Schedule
                )
                ProviderDetailItem(
                    label = "Son Test",
                    value = if (provider.lastTested > 0) "Başarılı" else "Henüz Yok",
                    icon = Icons.Default.CheckCircle
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = onConfigure,
                    label = { Text("Yapılandır") },
                    leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                AssistChip(
                    onClick = onTest,
                    label = { Text("Test Et") },
                    leadingIcon = { Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                if (onDelete != null) {
                    AssistChip(
                        onClick = onDelete,
                        label = { Text("Sil") },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProviderDetailItem(
    label: String,
    value: String,
    icon: ImageVector
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = label,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun LocalModelsTab(
    viewModel: CustomAIIntegrationViewModel,
    uiState: CustomAIIntegrationUiState
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Load Local Model Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📱 Yerel AI Model Yükle",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "TensorFlow Lite, ONNX, PyTorch Mobile modellerini yükleyin ve kullanın",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { viewModel.showLoadModelDialog() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Model Yükle")
                        }
                        
                        OutlinedButton(
                            onClick = { viewModel.optimizeModels() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Optimize Et")
                        }
                    }
                }
            }
        }
        
        // Storage Usage
        item {
            LocalModelStorageCard(
                usedStorage = uiState.integrationStatus.storageUsed,
                availableStorage = uiState.integrationStatus.availableStorage
            )
        }
        
        // Local Models
        if (uiState.localModels.isNotEmpty()) {
            item {
                Text(
                    text = "💾 Yerel Modeller (${uiState.localModels.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            items(uiState.localModels) { model ->
                LocalModelCard(
                    model = model,
                    onRun = { viewModel.runLocalModel(model.id) },
                    onUnload = { viewModel.unloadModel(model.id) },
                    onDelete = { viewModel.deleteLocalModel(model.id) }
                )
            }
        }
    }
}

@Composable
private fun LocalModelStorageCard(
    usedStorage: Long,
    availableStorage: Long
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "💽 Depolama Kullanımı",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val totalStorage = usedStorage + availableStorage
            val usagePercentage = if (totalStorage > 0) (usedStorage.toFloat() / totalStorage * 100) else 0f
            
            // Storage Progress Bar
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Kullanılan: ${formatBytes(usedStorage)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Toplam: ${formatBytes(totalStorage)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LinearProgressIndicator(
                    progress = usagePercentage / 100f,
                    modifier = Modifier.fillMaxWidth(),
                    color = when {
                        usagePercentage > 80 -> MaterialTheme.colorScheme.error
                        usagePercentage > 60 -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "%.1f%% kullanımda".format(usagePercentage),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun LocalModelCard(
    model: LocalAIModel,
    onRun: () -> Unit,
    onUnload: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (model.isLoaded) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            else 
                MaterialTheme.colorScheme.surface
        ),
        border = if (model.isLoaded) 
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        when (model.modelType) {
                            LocalModelType.TENSORFLOW_LITE -> Icons.Default.Memory
                            LocalModelType.ONNX -> Icons.Default.Architecture
                            LocalModelType.PYTORCH_MOBILE -> Icons.Default.DataObject
                            else -> Icons.Default.Storage
                        },
                        contentDescription = model.modelType.name,
                        modifier = Modifier.size(24.dp),
                        tint = if (model.isLoaded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = model.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (model.turkishMedicalSpecialized) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "🇹🇷 TIBBİ",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                        Text(
                            text = model.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "${model.modelType.name} • ${formatBytes(model.fileSize)} • v${model.version}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
                
                // Model Status Badge
                Surface(
                    color = if (model.isLoaded) 
                        MaterialTheme.colorScheme.primaryContainer
                    else 
                        MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (model.isLoaded) "Yüklü" else "Bekleniyor",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (model.isLoaded) 
                            MaterialTheme.colorScheme.primary
                        else 
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Model Performance Metrics
            if (model.performanceMetrics.totalInferences > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ModelMetricItem(
                        label = "Ortalama Süre",
                        value = "${model.performanceMetrics.averageInferenceTime}ms",
                        icon = Icons.Default.Speed
                    )
                    ModelMetricItem(
                        label = "Toplam Çalıştırma",
                        value = "${model.performanceMetrics.totalInferences}",
                        icon = Icons.Default.PlayArrow
                    )
                    ModelMetricItem(
                        label = "Başarı Oranı",
                        value = "${((model.performanceMetrics.successfulInferences.toFloat() / model.performanceMetrics.totalInferences) * 100).toInt()}%",
                        icon = Icons.Default.CheckCircle
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // Action Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (model.isLoaded) {
                    AssistChip(
                        onClick = onRun,
                        label = { Text("Çalıştır") },
                        leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    AssistChip(
                        onClick = onUnload,
                        label = { Text("Bellekten Çıkar") },
                        leadingIcon = { Icon(Icons.Default.Eject, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                } else {
                    AssistChip(
                        onClick = { /* Load model */ },
                        label = { Text("Belleğe Yükle") },
                        leadingIcon = { Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }
                AssistChip(
                    onClick = { /* Model info */ },
                    label = { Text("Bilgi") },
                    leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                AssistChip(
                    onClick = onDelete,
                    label = { Text("Sil") },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }
        }
    }
}

@Composable
private fun ModelMetricItem(
    label: String,
    value: String,
    icon: ImageVector
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = label,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun ConfigurationTab(
    viewModel: CustomAIIntegrationViewModel,
    uiState: CustomAIIntegrationUiState
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("AI Konfigürasyon Ayarları - Geliştiriliyor")
    }
}

@Composable
private fun MarketplaceTab(
    viewModel: CustomAIIntegrationViewModel,
    uiState: CustomAIIntegrationUiState
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Türk Tıbbi Model Marketplace - Geliştiriliyor")
    }
}

@Composable
private fun PerformanceTab(
    viewModel: CustomAIIntegrationViewModel,
    uiState: CustomAIIntegrationUiState
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("AI Performans Analizi - Geliştiriliyor")
    }
}

// Helper function to format bytes
private fun formatBytes(bytes: Long): String {
    return when {
        bytes >= 1024 * 1024 * 1024 -> "%.1f GB".format(bytes / (1024.0 * 1024.0 * 1024.0))
        bytes >= 1024 * 1024 -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
        bytes >= 1024 -> "%.1f KB".format(bytes / 1024.0)
        else -> "$bytes B"
    }
}

// ViewModel for Custom AI Integration Screen
@HiltViewModel
class CustomAIIntegrationViewModel @Inject constructor(
    private val customAIRepository: CustomAIIntegrationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomAIIntegrationUiState())
    val uiState: StateFlow<CustomAIIntegrationUiState> = _uiState.asStateFlow()

    init {
        loadAIConfigurations()
        loadLocalModels()
        loadIntegrationStatus()
    }

    fun showAddProviderDialog() {
        // Implementation for showing add provider dialog
    }

    fun testAllProviders() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTesting = true)
            
            try {
                // Test all providers
                delay(2000) // Simulate testing
                
                _uiState.value = _uiState.value.copy(isTesting = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isTesting = false,
                    error = e.message
                )
            }
        }
    }

    fun configureProvider(providerId: String) {
        // Implementation for configuring provider
    }

    fun testProvider(providerId: String) {
        // Implementation for testing specific provider
    }

    fun toggleProvider(providerId: String) {
        viewModelScope.launch {
            val currentConfigs = _uiState.value.defaultProviders + _uiState.value.customProviders
            val updatedConfigs = currentConfigs.map { config ->
                if (config.id == providerId) {
                    config.copy(isActive = !config.isActive)
                } else config
            }
            
            val (defaultProviders, customProviders) = updatedConfigs.partition { 
                it.id.startsWith("openai") || it.id.startsWith("anthropic") 
            }
            
            _uiState.value = _uiState.value.copy(
                defaultProviders = defaultProviders,
                customProviders = customProviders
            )
        }
    }

    fun deleteProvider(providerId: String) {
        viewModelScope.launch {
            val updatedCustom = _uiState.value.customProviders.filter { it.id != providerId }
            _uiState.value = _uiState.value.copy(customProviders = updatedCustom)
        }
    }

    fun showLoadModelDialog() {
        // Implementation for showing load model dialog
    }

    fun optimizeModels() {
        // Implementation for optimizing models
    }

    fun runLocalModel(modelId: String) {
        // Implementation for running local model
    }

    fun unloadModel(modelId: String) {
        viewModelScope.launch {
            val updatedModels = _uiState.value.localModels.map { model ->
                if (model.id == modelId) {
                    model.copy(isLoaded = false)
                } else model
            }
            _uiState.value = _uiState.value.copy(localModels = updatedModels)
        }
    }

    fun deleteLocalModel(modelId: String) {
        viewModelScope.launch {
            val updatedModels = _uiState.value.localModels.filter { it.id != modelId }
            _uiState.value = _uiState.value.copy(localModels = updatedModels)
        }
    }

    private fun loadAIConfigurations() {
        viewModelScope.launch {
            customAIRepository.customAIConfigurations.collect { configurations ->
                val (defaultProviders, customProviders) = configurations.partition { 
                    it.id.startsWith("openai") || it.id.startsWith("anthropic") 
                }
                
                _uiState.value = _uiState.value.copy(
                    defaultProviders = defaultProviders,
                    customProviders = customProviders
                )
            }
        }
    }

    private fun loadLocalModels() {
        viewModelScope.launch {
            customAIRepository.localAIModels.collect { models ->
                _uiState.value = _uiState.value.copy(localModels = models)
            }
        }
    }

    private fun loadIntegrationStatus() {
        viewModelScope.launch {
            // Load sample integration status
            val status = AIIntegrationStatus(
                totalCustomProviders = 5,
                activeCustomProviders = 3,
                totalLocalModels = 2,
                loadedLocalModels = 1,
                totalInferencesToday = 127L,
                averageResponseTime = 850L,
                systemHealth = SystemHealth.EXCELLENT,
                storageUsed = 256L * 1024 * 1024, // 256 MB
                availableStorage = 768L * 1024 * 1024 // 768 MB
            )
            
            _uiState.value = _uiState.value.copy(integrationStatus = status)
        }
    }
}

// Data classes for Custom AI Integration UI state
data class CustomAIIntegrationUiState(
    val isTesting: Boolean = false,
    val integrationStatus: AIIntegrationStatus = AIIntegrationStatus(),
    val defaultProviders: List<CustomAIConfiguration> = emptyList(),
    val customProviders: List<CustomAIConfiguration> = emptyList(),
    val localModels: List<LocalAIModel> = emptyList(),
    val error: String? = null
)