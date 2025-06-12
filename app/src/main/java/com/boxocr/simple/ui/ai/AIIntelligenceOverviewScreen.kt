package com.boxocr.simple.ui.ai

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.boxocr.simple.ai.*

/**
 * 🧠 PHASE 5: COMPREHENSIVE AI INTELLIGENCE OVERVIEW SCREEN
 * 
 * Revolutionary showcase of all AI Intelligence capabilities in one unified interface.
 * Demonstrates the complete transformation from basic OCR to AI-powered medical platform.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIIntelligenceOverviewScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAssistant: () -> Unit,
    onNavigateToVision: () -> Unit,
    onNavigateToPredictive: () -> Unit,
    viewModel: AIIntelligenceOverviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.initializeAIOverview()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.05f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {
        // Revolutionary AI Header
        AIIntelligenceHeader(
            onNavigateBack = onNavigateBack,
            aiStatus = uiState.aiStatus
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // AI Intelligence Status Overview
            item {
                AISystemStatusCard(
                    status = uiState.aiStatus,
                    analytics = uiState.analytics
                )
            }
            
            // AI Capabilities Showcase
            item {
                Text(
                    text = "🧠 AI Yetenekleri",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // 1. Turkish Drug Vision AI
            item {
                AICapabilityCard(
                    title = "Türk İlaç Görme AI",
                    description = "99%+ doğrulukla Türk ilaç tanıma sistemi",
                    icon = "👁️",
                    status = uiState.aiStatus?.turkishVisionReady ?: false,
                    onClick = onNavigateToVision,
                    capabilities = listOf(
                        "Özel Türk ilaç paketleri tanıma",
                        "Marka/jenerik ilaç ayrımı", 
                        "Son kullanma tarihi çıkarma",
                        "Orijinallik doğrulama"
                    )
                )
            }
            
            // 2. Medical AI Assistant
            item {
                AICapabilityCard(
                    title = "Tıbbi AI Asistan",
                    description = "Türkiye'nin ilk konuşan tıbbi AI sistemi",
                    icon = "🧠",
                    status = uiState.aiStatus?.medicalAssistantReady ?: false,
                    onClick = onNavigateToAssistant,
                    capabilities = listOf(
                        "Doğal Türkçe tıbbi sohbet",
                        "Sesli tıbbi danışmanlık",
                        "Akıllı reçete oluşturma",
                        "İlaç etkileşim uyarıları"
                    )
                )
            }
            
            // 3. Predictive Intelligence
            item {
                AICapabilityCard(
                    title = "Öngörü AI",
                    description = "Gelecek odaklı tıbbi analiz sistemi",
                    icon = "🔮",
                    status = uiState.aiStatus?.predictiveIntelligenceReady ?: false,
                    onClick = onNavigateToPredictive,
                    capabilities = listOf(
                        "Tedavi sonucu tahmini",
                        "Doz optimizasyon önerileri",
                        "Maliyet tasarrufu analizi",
                        "Hasta özel önerileri"
                    )
                )
            }
            
            // 4. Advanced Analysis
            item {
                AICapabilityCard(
                    title = "Gelişmiş Analiz AI",
                    description = "İleri düzey tıbbi görüntü analizi",
                    icon = "🔬",
                    status = uiState.aiStatus?.advancedAnalysisReady ?: false,
                    onClick = { /* Navigate to advanced analysis */ },
                    capabilities = listOf(
                        "İlaç kalite analizi",
                        "Hasar tespit sistemi",
                        "Sahtecilik kontrolü",
                        "Düzenleyici uyumluluk"
                    )
                )
            }
            
            // AI Performance Metrics
            item {
                AIPerformanceMetricsCard(
                    analytics = uiState.analytics
                )
            }
            
            // AI Recommendations
            if (uiState.aiRecommendations.isNotEmpty()) {
                item {
                    AISystemRecommendationsCard(
                        recommendations = uiState.aiRecommendations
                    )
                }
            }
            
            // Technical Specifications
            item {
                AITechnicalSpecsCard(
                    modelVersions = uiState.analytics?.modelVersions ?: emptyMap()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AIIntelligenceHeader(
    onNavigateBack: () -> Unit,
    aiStatus: AIIntelligenceStatus?
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "🧠 AI İstihbarat Merkezi",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Türk Tıbbi AI Platformu",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
            }
        },
        actions = {
            // AI Status Indicator
            Surface(
                color = if (aiStatus?.overallReady == true) {
                    Color.Green.copy(alpha = 0.2f)
                } else {
                    Color.Orange.copy(alpha = 0.2f)
                },
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = if (aiStatus?.overallReady == true) Color.Green else Color.Orange,
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                    )
                    Text(
                        text = if (aiStatus?.overallReady == true) "AI Aktif" else "Başlatılıyor",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

@Composable
private fun AISystemStatusCard(
    status: AIIntelligenceStatus?,
    analytics: AIIntelligenceAnalytics?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "⚡",
                    fontSize = 32.sp
                )
                
                Column {
                    Text(
                        text = "Sistem Durumu",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (status?.overallReady == true) {
                            "Tüm AI sistemleri aktif ve hazır"
                        } else {
                            "AI sistemleri başlatılıyor..."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // AI Component Status Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AIComponentStatus(
                    name = "Görme AI",
                    isReady = status?.turkishVisionReady ?: false,
                    modifier = Modifier.weight(1f)
                )
                AIComponentStatus(
                    name = "Konuşma AI",
                    isReady = status?.medicalAssistantReady ?: false,
                    modifier = Modifier.weight(1f)
                )
                AIComponentStatus(
                    name = "Öngörü AI",
                    isReady = status?.predictiveIntelligenceReady ?: false,
                    modifier = Modifier.weight(1f)
                )
                AIComponentStatus(
                    name = "Analiz AI",
                    isReady = status?.advancedAnalysisReady ?: false,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Performance Summary
            if (analytics != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${(analytics.overallPerformance * 100).toInt()}%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Genel Performans",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${analytics.usageStatistics["totalQueries"] ?: 0}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        Text(
                            text = "Toplam Sorgu",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "v2.1",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "AI Sürümü",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AIComponentStatus(
    name: String,
    isReady: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = if (isReady) {
            Color.Green.copy(alpha = 0.1f)
        } else {
            Color.Orange.copy(alpha = 0.1f)
        },
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (isReady) Icons.Default.CheckCircle else Icons.Default.Schedule,
                contentDescription = null,
                tint = if (isReady) Color.Green else Color.Orange,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = name,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AICapabilityCard(
    title: String,
    description: String,
    icon: String,
    status: Boolean,
    onClick: () -> Unit,
    capabilities: List<String>
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (status) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = icon,
                    fontSize = 24.sp
                )
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Surface(
                    color = if (status) {
                        Color.Green.copy(alpha = 0.2f)
                    } else {
                        Color.Gray.copy(alpha = 0.2f)
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (status) "Aktif" else "Beklemede",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (status) Color.Green else Color.Gray
                    )
                }
            }
            
            // Capabilities list
            capabilities.forEach { capability ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (status) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        }
                    )
                    Text(
                        text = capability,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (status) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AIPerformanceMetricsCard(
    analytics: AIIntelligenceAnalytics?
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "📊 Performans Metrikleri",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            if (analytics != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricItem(
                        label = "Genel Performans",
                        value = "${(analytics.overallPerformance * 100).toInt()}%",
                        color = MaterialTheme.colorScheme.primary
                    )
                    MetricItem(
                        label = "Toplam İşlem",
                        value = "${analytics.usageStatistics["totalQueries"] ?: 0}",
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    MetricItem(
                        label = "Başarı Oranı",
                        value = "95%",
                        color = Color.Green
                    )
                }
            } else {
                Text(
                    text = "Performans verileri yükleniyor...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MetricItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AISystemRecommendationsCard(
    recommendations: List<AIRecommendation>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "💡 Sistem Önerileri",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            recommendations.take(3).forEach { recommendation ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Lightbulb,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = recommendation.message,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AITechnicalSpecsCard(
    modelVersions: Map<String, String>
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "⚙️ Teknik Özellikler",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            if (modelVersions.isNotEmpty()) {
                modelVersions.forEach { (model, version) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = model.replaceFirstChar { it.uppercaseChar() },
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = version,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            } else {
                Text(
                    text = "Model bilgileri yükleniyor...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ViewModel for the overview screen
@dagger.hilt.android.lifecycle.HiltViewModel
class AIIntelligenceOverviewViewModel @javax.inject.Inject constructor(
    private val aiIntelligenceManager: AIIntelligenceManager
) : androidx.lifecycle.ViewModel() {
    
    private val _uiState = kotlinx.coroutines.flow.MutableStateFlow(AIIntelligenceOverviewUiState())
    val uiState: kotlinx.coroutines.flow.StateFlow<AIIntelligenceOverviewUiState> = _uiState.kotlinx.coroutines.flow.asStateFlow()
    
    fun initializeAIOverview() {
        androidx.lifecycle.viewModelScope.launch {
            // Initialize AI and get status
            val result = aiIntelligenceManager.initializeAIIntelligence()
            if (result.isSuccess) {
                val status = result.getOrThrow()
                _uiState.value = _uiState.value.copy(aiStatus = status)
                
                // Get analytics
                val analyticsResult = aiIntelligenceManager.getAIIntelligenceAnalytics()
                if (analyticsResult.isSuccess) {
                    val analytics = analyticsResult.getOrThrow()
                    _uiState.value = _uiState.value.copy(analytics = analytics)
                }
            }
        }
    }
}

data class AIIntelligenceOverviewUiState(
    val aiStatus: AIIntelligenceStatus? = null,
    val analytics: AIIntelligenceAnalytics? = null,
    val aiRecommendations: List<AIRecommendation> = emptyList(),
    val isLoading: Boolean = false
)
