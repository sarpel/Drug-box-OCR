package com.boxocr.simple.ui.ai

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.animation.core.*

/**
 * 🧠 PHASE 5: AI ASSISTANT SCREEN
 * 
 * Revolutionary conversational AI interface for Turkish medical assistance.
 * First-ever Turkish medical AI that understands medical context and terminology.
 * 
 * Features:
 * - Natural language Turkish medical conversations
 * - Voice-powered medical consultations
 * - Real-time AI recommendations
 * - Intelligent follow-up suggestions
 * - Learning and adaptation capabilities
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIAssistantScreen(
    onNavigateBack: () -> Unit,
    viewModel: AIAssistantViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var userInput by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        viewModel.initializeAI()
    }
    
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
        // AI Assistant Header
        AIAssistantHeader(
            onNavigateBack = onNavigateBack,
            aiStatus = uiState.aiStatus,
            isProcessing = uiState.isProcessing
        )
        
        // Conversation Area
        Box(
            modifier = Modifier.weight(1f)
        ) {
            if (uiState.conversations.isEmpty() && !uiState.isProcessing) {
                AIWelcomeMessage()
            } else {
                ConversationList(
                    conversations = uiState.conversations,
                    isProcessing = uiState.isProcessing,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        
        // AI Recommendations Panel
        AnimatedVisibility(
            visible = uiState.aiRecommendations.isNotEmpty(),
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            AIRecommendationsPanel(
                recommendations = uiState.aiRecommendations,
                onRecommendationClick = { recommendation ->
                    viewModel.processRecommendation(recommendation)
                }
            )
        }
        
        // Input Area
        AIInputArea(
            userInput = userInput,
            onInputChange = { userInput = it },
            onSendMessage = {
                if (userInput.isNotBlank()) {
                    viewModel.sendMessage(userInput)
                    userInput = ""
                }
            },
            onVoiceToggle = {
                isListening = !isListening
                if (isListening) {
                    viewModel.startVoiceInput()
                } else {
                    viewModel.stopVoiceInput()
                }
            },
            isListening = isListening,
            isProcessing = uiState.isProcessing,
            suggestedQuestions = uiState.suggestedQuestions
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AIAssistantHeader(
    onNavigateBack: () -> Unit,
    aiStatus: AIIntelligenceStatus?,
    isProcessing: Boolean
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column {
                    Text(
                        text = "🧠 AI Tıbbi Asistan",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // AI Status Indicator
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = when {
                                        aiStatus?.overallReady == true -> Color.Green
                                        isProcessing -> Color.Yellow
                                        else -> Color.Red
                                    },
                                    shape = androidx.compose.foundation.shape.CircleShape
                                )
                        )
                        
                        Text(
                            text = when {
                                aiStatus?.overallReady == true && !isProcessing -> "AI Hazır"
                                isProcessing -> "AI Düşünüyor..."
                                else -> "AI Başlatılıyor..."
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Geri"
                )
            }
        },
        actions = {
            IconButton(
                onClick = { /* Open AI settings */ }
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "AI Ayarları"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

@Composable
private fun AIWelcomeMessage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // AI Brain Animation
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(60.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🧠",
                fontSize = 60.sp
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Türk Tıbbi AI Asistanı",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Merhaba! Ben Türkiye'nin ilk tıbbi AI asistanıyım. Size nasıl yardımcı olabilirim?",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Sample Questions
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Örnek sorular:",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
            
            val sampleQuestions = listOf(
                "Hipertansiyon için en uygun ilaç kombinasyonu nedir?",
                "Bu ilacın yan etkileri neler?",
                "Diyabet hastası için reçete oluştur",
                "İlaç etkileşimlerini kontrol et"
            )
            
            sampleQuestions.forEach { question ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        text = "💬 $question",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun ConversationList(
    conversations: List<AIConversationItem>,
    isProcessing: Boolean,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        items(conversations) { conversation ->
            when (conversation.type) {
                ConversationType.USER_MESSAGE -> {
                    UserMessageBubble(
                        message = conversation.content,
                        timestamp = conversation.timestamp
                    )
                }
                ConversationType.AI_RESPONSE -> {
                    AIResponseBubble(
                        response = conversation.aiResponse!!,
                        timestamp = conversation.timestamp
                    )
                }
                ConversationType.AI_RECOMMENDATION -> {
                    AIRecommendationBubble(
                        recommendation = conversation.aiRecommendation!!,
                        timestamp = conversation.timestamp
                    )
                }
            }
        }
        
        if (isProcessing) {
            item {
                AIThinkingBubble()
            }
        }
    }
}

@Composable
private fun UserMessageBubble(
    message: String,
    timestamp: Long
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(16.dp, 4.dp, 16.dp, 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = formatTimestamp(timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
private fun AIResponseBubble(
    response: TurkishMedicalResponse,
    timestamp: Long
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 300.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // AI Icon and Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🧠",
                        fontSize = 18.sp
                    )
                    
                    Text(
                        text = "AI Tıbbi Asistan",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Confidence Score
                    Surface(
                        color = when {
                            response.confidence > 0.8f -> Color.Green.copy(alpha = 0.2f)
                            response.confidence > 0.6f -> Color.Yellow.copy(alpha = 0.2f)
                            else -> Color.Red.copy(alpha = 0.2f)
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${(response.confidence * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // AI Response Content
                Text(
                    text = response.turkishResponse,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Action Required Indicator
                if (response.actionRequired != TurkishMedicalAction.PROVIDE_INFORMATION) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        
                        Text(
                            text = getActionText(response.actionRequired),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = formatTimestamp(timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun AIRecommendationBubble(
    recommendation: AIRecommendation,
    timestamp: Long
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (recommendation.severity) {
                AISeverity.CRITICAL -> MaterialTheme.colorScheme.errorContainer
                AISeverity.HIGH -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                AISeverity.MEDIUM -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
                AISeverity.LOW -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            }
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            when (recommendation.severity) {
                AISeverity.CRITICAL -> MaterialTheme.colorScheme.error
                AISeverity.HIGH -> MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                AISeverity.MEDIUM -> MaterialTheme.colorScheme.tertiary
                AISeverity.LOW -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Severity Icon
            Icon(
                imageVector = when (recommendation.severity) {
                    AISeverity.CRITICAL -> Icons.Default.Warning
                    AISeverity.HIGH -> Icons.Default.Info
                    AISeverity.MEDIUM -> Icons.Default.Lightbulb
                    AISeverity.LOW -> Icons.Default.TipsAndUpdates
                },
                contentDescription = null,
                tint = when (recommendation.severity) {
                    AISeverity.CRITICAL -> MaterialTheme.colorScheme.error
                    AISeverity.HIGH -> MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    AISeverity.MEDIUM -> MaterialTheme.colorScheme.tertiary
                    AISeverity.LOW -> MaterialTheme.colorScheme.primary
                }
            )
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "AI Önerisi",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = recommendation.message,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                if (recommendation.actionRequired) {
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Eylem gerekli",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun AIThinkingBubble() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            ),
            shape = RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🧠",
                    fontSize = 18.sp
                )
                
                Text(
                    text = "AI düşünüyor",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Thinking animation dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    repeat(3) { index ->
                        val infiniteTransition = rememberInfiniteTransition(label = "thinking")
                        val alpha by infiniteTransition.animateFloat(
                            initialValue = 0.3f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(600, delayMillis = index * 100),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "thinking_dot_$index"
                        )
                        
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .background(
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
                                    shape = androidx.compose.foundation.shape.CircleShape
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AIRecommendationsPanel(
    recommendations: List<AIRecommendation>,
    onRecommendationClick: (AIRecommendation) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
                
                Text(
                    text = "AI Önerileri",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            recommendations.take(3).forEach { recommendation ->
                ElevatedButton(
                    onClick = { onRecommendationClick(recommendation) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Text(
                        text = recommendation.message,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AIInputArea(
    userInput: String,
    onInputChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onVoiceToggle: () -> Unit,
    isListening: Boolean,
    isProcessing: Boolean,
    suggestedQuestions: List<String>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        // Suggested Questions
        if (suggestedQuestions.isNotEmpty()) {
            Text(
                text = "Önerilen sorular:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 12.dp)
            ) {
                items(suggestedQuestions) { question ->
                    FilterChip(
                        onClick = { onInputChange(question) },
                        label = { 
                            Text(
                                text = question,
                                style = MaterialTheme.typography.labelMedium
                            ) 
                        },
                        selected = false
                    )
                }
            }
        }
        
        // Input Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = userInput,
                onValueChange = onInputChange,
                modifier = Modifier.weight(1f),
                placeholder = { 
                    Text("Tıbbi sorunuzu yazın veya sesli sorun...") 
                },
                maxLines = 3,
                enabled = !isProcessing
            )
            
            // Voice Button
            FloatingActionButton(
                onClick = onVoiceToggle,
                modifier = Modifier.size(48.dp),
                containerColor = if (isListening) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.tertiary
                },
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp)
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = if (isListening) "Dinlemeyi durdur" else "Sesli soru sor",
                    tint = if (isListening) {
                        MaterialTheme.colorScheme.onError
                    } else {
                        MaterialTheme.colorScheme.onTertiary
                    }
                )
            }
            
            // Send Button
            FloatingActionButton(
                onClick = onSendMessage,
                modifier = Modifier.size(48.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp)
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Gönder",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

// Helper functions
private fun formatTimestamp(timestamp: Long): String {
    val currentTime = System.currentTimeMillis()
    val diff = currentTime - timestamp
    
    return when {
        diff < 60_000 -> "Şimdi"
        diff < 3600_000 -> "${diff / 60_000} dk önce"
        diff < 86400_000 -> "${diff / 3600_000} sa önce"
        else -> "${diff / 86400_000} gün önce"
    }
}

private fun getActionText(action: TurkishMedicalAction): String {
    return when (action) {
        TurkishMedicalAction.CREATE_PRESCRIPTION -> "Reçete oluşturuluyor"
        TurkishMedicalAction.CHECK_INTERACTIONS -> "Etkileşimler kontrol ediliyor"
        TurkishMedicalAction.OPTIMIZE_DOSAGE -> "Doz optimizasyonu yapılıyor"
        TurkishMedicalAction.SHOW_DRUG_INFO -> "İlaç bilgileri gösteriliyor"
        TurkishMedicalAction.ANALYZE_SYMPTOMS -> "Semptomlar analiz ediliyor"
        else -> "İşlem devam ediyor"
    }
}

// Data classes for UI state
data class AIConversationItem(
    val type: ConversationType,
    val content: String = "",
    val aiResponse: TurkishMedicalResponse? = null,
    val aiRecommendation: AIRecommendation? = null,
    val timestamp: Long = System.currentTimeMillis()
)

enum class ConversationType {
    USER_MESSAGE, AI_RESPONSE, AI_RECOMMENDATION
}
