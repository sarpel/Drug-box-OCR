# 🧠 Universal AI Model Configuration System

## 🎯 **COMPREHENSIVE AI PROVIDER SUPPORT**

### **☁️ CLOUD AI PROVIDERS (API-Based)**

#### **1. 🌟 OpenAI (ChatGPT)**
```kotlin
data class OpenAIConfig(
    val apiKey: String,
    val baseUrl: String = "https://api.openai.com/v1",
    val model: String = "gpt-4o", // gpt-4o, gpt-4o-mini, gpt-3.5-turbo
    val maxTokens: Int = 4096,
    val temperature: Float = 0.1f, // For medical accuracy
    val organizationId: String? = null,
    val timeout: Long = 30000L
)

class OpenAIRepository @Inject constructor() {
    suspend fun analyzeDrugImage(image: ByteArray, config: OpenAIConfig): AIAnalysisResult {
        // Vision API for drug analysis
        // Medical knowledge consultation
        // Turkish medical terminology
    }
    
    suspend fun medicalConsultation(query: String, config: OpenAIConfig): MedicalAdvice {
        // Turkish medical consultation
        // Drug interaction analysis
        // Dosage recommendations
    }
}
```

#### **2. 🤖 Anthropic (Claude)**
```kotlin
data class ClaudeConfig(
    val apiKey: String,
    val baseUrl: String = "https://api.anthropic.com/v1",
    val model: String = "claude-3-5-sonnet-20241022", // Latest Claude models
    val maxTokens: Int = 4096,
    val temperature: Float = 0.0f, // Medical precision
    val systemPrompt: String = "Turkish medical AI assistant",
    val timeout: Long = 30000L
)

class ClaudeRepository @Inject constructor() {
    suspend fun medicalAnalysis(text: String, config: ClaudeConfig): MedicalInsight {
        // Advanced medical reasoning
        // Turkish healthcare guidelines
        // Prescription analysis
    }
}
```

#### **3. 🔮 Google (Gemini)**
```kotlin
data class GeminiConfig(
    val apiKey: String,
    val baseUrl: String = "https://generativelanguage.googleapis.com/v1",
    val model: String = "gemini-2.0-flash-exp", // Latest Gemini models
    val safetySettings: List<SafetySetting> = defaultMedicalSafety(),
    val generationConfig: GenerationConfig = medicalOptimized(),
    val timeout: Long = 30000L
)

// Already implemented, enhance for universal config
class GeminiRepository @Inject constructor() {
    suspend fun enhancedOCR(image: ByteArray, config: GeminiConfig): OCRResult {
        // Your existing OCR with configurable models
    }
}
```

#### **4. 🚀 xAI (Grok)**
```kotlin
data class GrokConfig(
    val apiKey: String,
    val baseUrl: String = "https://api.x.ai/v1",
    val model: String = "grok-2-1212", // Latest Grok models
    val maxTokens: Int = 4096,
    val temperature: Float = 0.1f,
    val systemPrompt: String = "Turkish medical expert",
    val timeout: Long = 30000L
)

class GrokRepository @Inject constructor() {
    suspend fun medicalInsight(query: String, config: GrokConfig): MedicalAnalysis {
        // Real-time medical knowledge
        // Up-to-date drug information
        // Turkish medical insights
    }
}
```

#### **5. 🧠 DeepSeek**
```kotlin
data class DeepSeekConfig(
    val apiKey: String,
    val baseUrl: String = "https://api.deepseek.com/v1",
    val model: String = "deepseek-v3", // Latest DeepSeek models
    val maxTokens: Int = 4096,
    val temperature: Float = 0.0f, // Medical precision
    val reasoning: Boolean = true, // Enable reasoning mode
    val timeout: Long = 30000L
)

class DeepSeekRepository @Inject constructor() {
    suspend fun reasoningAnalysis(medicalCase: String, config: DeepSeekConfig): ReasoningResult {
        // Advanced medical reasoning
        // Step-by-step analysis
        // Turkish medical context
    }
}
```

#### **6. 🌐 OpenRouter (Multi-Model Gateway)**
```kotlin
data class OpenRouterConfig(
    val apiKey: String,
    val baseUrl: String = "https://openrouter.ai/api/v1",
    val model: String = "anthropic/claude-3.5-sonnet", // Any supported model
    val maxTokens: Int = 4096,
    val temperature: Float = 0.1f,
    val provider: String? = null, // Specific provider preference
    val fallback: List<String> = emptyList(), // Fallback models
    val timeout: Long = 30000L
)

class OpenRouterRepository @Inject constructor() {
    suspend fun multiModelAnalysis(query: String, config: OpenRouterConfig): MultiModelResult {
        // Access to 200+ models
        // Automatic fallback
        // Cost optimization
    }
}
```

#### **7. 🇨🇳 Qwen (Alibaba)**
```kotlin
data class QwenConfig(
    val apiKey: String,
    val baseUrl: String = "https://dashscope.aliyuncs.com/api/v1",
    val model: String = "qwen2.5-72b-instruct", // Latest Qwen models
    val maxTokens: Int = 4096,
    val temperature: Float = 0.1f,
    val topP: Float = 0.8f,
    val seed: Int? = null,
    val timeout: Long = 30000L
)

class QwenRepository @Inject constructor() {
    suspend fun multilingualAnalysis(text: String, config: QwenConfig): MultilingualResult {
        // Strong multilingual capabilities
        // Turkish-Chinese medical knowledge
        // Traditional medicine insights
    }
}
```

### **🖥️ LOCAL AI IMPLEMENTATIONS**

#### **8. 🏠 LM Studio**
```kotlin
data class LMStudioConfig(
    val baseUrl: String = "http://localhost:1234/v1", // Default LM Studio port
    val model: String = "microsoft/DialoGPT-large",
    val maxTokens: Int = 2048,
    val temperature: Float = 0.1f,
    val contextLength: Int = 4096,
    val gpuLayers: Int = -1, // Use all GPU layers
    val timeout: Long = 60000L // Longer for local processing
)

class LMStudioRepository @Inject constructor() {
    suspend fun localMedicalAnalysis(text: String, config: LMStudioConfig): LocalAIResult {
        // Privacy-compliant local processing
        // Offline medical consultation
        // Custom medical models
    }
    
    suspend fun checkAvailability(config: LMStudioConfig): Boolean {
        // Test local server connectivity
        // Model availability check
    }
}
```

#### **9. 🦙 Ollama**
```kotlin
data class OllamaConfig(
    val baseUrl: String = "http://localhost:11434", // Default Ollama port
    val model: String = "llama3.1:8b", // Available Ollama models
    val stream: Boolean = false,
    val contextWindow: Int = 4096,
    val temperature: Float = 0.1f,
    val topK: Int = 40,
    val topP: Float = 0.9f,
    val timeout: Long = 120000L // Longer for local models
)

class OllamaRepository @Inject constructor() {
    suspend fun localInference(prompt: String, config: OllamaConfig): OllamaResult {
        // Local model inference
        // Privacy-first approach
        // Customizable medical models
    }
    
    suspend fun listAvailableModels(config: OllamaConfig): List<String> {
        // Get installed models
        // Model size and capabilities
    }
}
```

#### **10. 🔧 LocalAI**
```kotlin
data class LocalAIConfig(
    val baseUrl: String = "http://localhost:8080/v1", // Default LocalAI port
    val model: String = "gpt-3.5-turbo", // LocalAI model name
    val maxTokens: Int = 2048,
    val temperature: Float = 0.1f,
    val backend: String = "llama-cpp", // Backend engine
    val gpuLayers: Int = 0,
    val timeout: Long = 90000L
)

class LocalAIRepository @Inject constructor() {
    suspend fun localAIProcessing(input: String, config: LocalAIConfig): LocalAIResult {
        // OpenAI-compatible local API
        // Multiple backend support
        // Self-hosted medical AI
    }
}
```

#### **11. 🦙 Llama.cpp**
```kotlin
data class LlamaCppConfig(
    val baseUrl: String = "http://localhost:8080", // llama.cpp server
    val model: String = "llama-3.1-8b-instruct",
    val nPredict: Int = 2048,
    val temperature: Float = 0.1f,
    val topK: Int = 40,
    val topP: Float = 0.9f,
    val repeatPenalty: Float = 1.1f,
    val timeout: Long = 120000L
)

class LlamaCppRepository @Inject constructor() {
    suspend fun llamaInference(prompt: String, config: LlamaCppConfig): LlamaResult {
        // Direct llama.cpp integration
        // High-performance local inference
        // Custom quantized models
    }
}
```

#### **12. 🎯 AnythingLLM**
```kotlin
data class AnythingLLMConfig(
    val baseUrl: String = "http://localhost:3001/api", // AnythingLLM default
    val workspaceSlug: String = "medical-workspace",
    val apiKey: String? = null,
    val model: String = "default",
    val mode: String = "chat", // chat, query
    val timeout: Long = 60000L
)

class AnythingLLMRepository @Inject constructor() {
    suspend fun documentBasedQuery(query: String, config: AnythingLLMConfig): DocumentResult {
        // Document-based AI responses
        // Medical knowledge base queries
        // Turkish medical literature search
    }
}
```

## 🏗️ **UNIFIED AI CONFIGURATION ARCHITECTURE**

### **🎛️ Universal AI Manager**
```kotlin
@Singleton
class UniversalAIManager @Inject constructor(
    private val openAIRepo: OpenAIRepository,
    private val claudeRepo: ClaudeRepository,
    private val geminiRepo: GeminiRepository,
    private val grokRepo: GrokRepository,
    private val deepSeekRepo: DeepSeekRepository,
    private val openRouterRepo: OpenRouterRepository,
    private val qwenRepo: QwenRepository,
    private val lmStudioRepo: LMStudioRepository,
    private val ollamaRepo: OllamaRepository,
    private val localAIRepo: LocalAIRepository,
    private val llamaCppRepo: LlamaCppRepository,
    private val anythingLLMRepo: AnythingLLMRepository
) {
    suspend fun analyzeWithBestAvailable(
        input: MedicalInput,
        preferredProviders: List<AIProvider>
    ): AIAnalysisResult {
        // Try providers in order of preference
        // Fallback to local models if cloud fails
        // Combine results for consensus
    }
    
    suspend fun testAllConfigurations(): Map<AIProvider, Boolean> {
        // Test connectivity to all configured providers
        // Return availability status
    }
}
```

### **📱 AI Configuration UI Screen**
```kotlin
@Composable
fun AIConfigurationScreen(
    onNavigateBack: () -> Unit,
    viewModel: AIConfigurationViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Cloud APIs", "Local Models", "Advanced")
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Tab Row
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        
        // Content based on selected tab
        when (selectedTab) {
            0 -> CloudAPIConfigScreen(viewModel)
            1 -> LocalModelConfigScreen(viewModel)
            2 -> AdvancedSettingsScreen(viewModel)
        }
    }
}

@Composable
fun CloudAPIConfigScreen(viewModel: AIConfigurationViewModel) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item { OpenAIConfigCard(viewModel) }
        item { ClaudeConfigCard(viewModel) }
        item { GeminiConfigCard(viewModel) }
        item { GrokConfigCard(viewModel) }
        item { DeepSeekConfigCard(viewModel) }
        item { OpenRouterConfigCard(viewModel) }
        item { QwenConfigCard(viewModel) }
    }
}

@Composable
fun LocalModelConfigScreen(viewModel: AIConfigurationViewModel) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item { LMStudioConfigCard(viewModel) }
        item { OllamaConfigCard(viewModel) }
        item { LocalAIConfigCard(viewModel) }
        item { LlamaCppConfigCard(viewModel) }
        item { AnythingLLMConfigCard(viewModel) }
    }
}
```

### **🎛️ Configuration Cards Example (OpenAI)**
```kotlin
@Composable
fun OpenAIConfigCard(viewModel: AIConfigurationViewModel) {
    val config by viewModel.openAIConfig.collectAsState()
    var isEnabled by remember { mutableStateOf(config.enabled) }
    var isExpanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with enable toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_openai),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "OpenAI (ChatGPT)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { 
                        isEnabled = it
                        viewModel.updateOpenAIEnabled(it)
                    }
                )
            }
            
            if (isEnabled) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // API Key Input
                OutlinedTextField(
                    value = config.apiKey,
                    onValueChange = viewModel::updateOpenAIKey,
                    label = { Text("API Key") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { /* Show/hide key */ }) {
                            Icon(Icons.Default.Visibility, contentDescription = null)
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Model Selection
                ExposedDropdownMenuBox(
                    expanded = isExpanded,
                    onExpandedChange = { isExpanded = it }
                ) {
                    OutlinedTextField(
                        value = config.model,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Model") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = isExpanded,
                        onDismissRequest = { isExpanded = false }
                    ) {
                        listOf("gpt-4o", "gpt-4o-mini", "gpt-3.5-turbo").forEach { model ->
                            DropdownMenuItem(
                                text = { Text(model) },
                                onClick = {
                                    viewModel.updateOpenAIModel(model)
                                    isExpanded = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Test Connection Button
                Button(
                    onClick = { viewModel.testOpenAIConnection() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Test Connection")
                }
                
                // Connection Status
                val connectionStatus by viewModel.openAIConnectionStatus.collectAsState()
                connectionStatus?.let { status ->
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (status.isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null,
                            tint = if (status.isSuccess) Color.Green else Color.Red,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = status.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (status.isSuccess) Color.Green else Color.Red
                        )
                    }
                }
            }
        }
    }
}
```

## 🎯 **IMPLEMENTATION BENEFITS**

### **🌟 Universal AI Access**
- ✅ **12 AI Providers** - Complete ecosystem coverage
- ✅ **Cloud + Local** - Online and offline capabilities  
- ✅ **Fallback System** - Automatic provider switching
- ✅ **Cost Optimization** - Choose optimal provider per task

### **🔧 Enhanced Medical AI**
- ✅ **Consensus Analysis** - Multiple AI opinions for accuracy
- ✅ **Specialized Models** - Medical-specific AI selection
- ✅ **Privacy Options** - Local processing for sensitive data
- ✅ **Turkish Optimization** - Best AI for Turkish medical context

### **📱 User Experience**
- ✅ **Simple Configuration** - Easy API key management
- ✅ **Status Monitoring** - Real-time connection testing
- ✅ **Intelligent Routing** - Best AI for each task
- ✅ **Seamless Integration** - Works with existing features

This universal AI configuration system transforms your platform into the **most comprehensive AI-powered medical platform available**, giving users choice, redundancy, and optimization for every medical AI task! 🚀