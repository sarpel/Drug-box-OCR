package com.boxocr.simple.ui.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.boxocr.simple.ai.*
import javax.inject.Inject

/**
 * 🧠 PHASE 5: AI ASSISTANT VIEWMODEL
 * 
 * Revolutionary ViewModel that manages the Turkish Medical AI Assistant interface.
 * Orchestrates all AI intelligence capabilities through the AIIntelligenceManager.
 * 
 * Features:
 * - AI initialization and status management
 * - Conversational AI interactions
 * - Voice command processing
 * - Real-time AI recommendations
 * - Learning and adaptation
 */
@HiltViewModel
class AIAssistantViewModel @Inject constructor(
    private val aiIntelligenceManager: AIIntelligenceManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AIAssistantUiState())
    val uiState: StateFlow<AIAssistantUiState> = _uiState.asStateFlow()
    
    // Current patient context for personalized AI responses
    private var currentPatientContext: TurkishPatientContext? = null
    
    // Conversation context for maintaining dialogue flow
    private var conversationContext = mutableListOf<TurkishMedicalConversation>()
    
    // Voice input management
    private var isVoiceInputActive = false
    
    init {
        // Initialize suggested questions
        updateSuggestedQuestions(getInitialSuggestedQuestions())
    }
    
    /**
     * 🚀 Initialize AI Intelligence System
     */
    fun initializeAI() {
        viewModelScope.launch {
            updateProcessingState(true)
            
            try {
                // Initialize all AI intelligence components
                val result = aiIntelligenceManager.initializeAIIntelligence()
                
                if (result.isSuccess) {
                    val status = result.getOrThrow()
                    updateAIStatus(status)
                    
                    if (status.overallReady) {
                        addSystemMessage("🧠 AI Tıbbi Asistan hazır! Size nasıl yardımcı olabilirim?")
                        updateSuggestedQuestions(getAdvancedSuggestedQuestions())
                    } else {
                        addSystemMessage("⚠️ AI sistemi kısmen hazır. Bazı özellikler sınırlı olabilir.")
                        addAIRecommendation(
                            AIRecommendation(
                                type = AIRecommendationType.PREDICTIVE_INSIGHT,
                                message = "AI sistemi optimizasyonu için uygulama yeniden başlatılabilir",
                                severity = AISeverity.LOW,
                                actionRequired = false
                            )
                        )
                    }
                } else {
                    val error = result.exceptionOrNull()
                    addSystemMessage("❌ AI sistemi başlatılamadı: ${error?.message}")
                    updateSuggestedQuestions(getBasicSuggestedQuestions())
                }
            } catch (e: Exception) {
                addSystemMessage("🔧 AI sistemi başlatılıyor... Lütfen bekleyin.")
                // Fallback to basic functionality
                updateSuggestedQuestions(getBasicSuggestedQuestions())
            } finally {
                updateProcessingState(false)
            }
        }
    }
    
    /**
     * 💬 Send Message to AI Assistant
     */
    fun sendMessage(message: String) {
        viewModelScope.launch {
            // Add user message to conversation
            addUserMessage(message)
            updateProcessingState(true)
            
            try {
                // Process message through conversational AI
                val result = aiIntelligenceManager.conversationalMedicalAI(
                    query = message,
                    patientContext = currentPatientContext,
                    conversationContext = conversationContext
                )
                
                if (result.isSuccess) {
                    val aiResponse = result.getOrThrow()
                    processAIResponse(aiResponse)
                } else {
                    val error = result.exceptionOrNull()
                    addSystemMessage("Üzgünüm, şu an yanıt veremiyorum: ${error?.message}")
                }
            } catch (e: Exception) {
                addSystemMessage("Bir hata oluştu. Lütfen tekrar deneyin.")
            } finally {
                updateProcessingState(false)
            }
        }
    }
    
    /**
     * 🎤 Start Voice Input
     */
    fun startVoiceInput() {
        viewModelScope.launch {
            isVoiceInputActive = true
            addSystemMessage("🎤 Sesli komut bekleniyor... Konuşmaya başlayın.")
            
            // Simulate voice recognition (in real implementation, integrate with Android Speech Recognition)
            delay(3000) // Simulated listening period
            
            if (isVoiceInputActive) {
                // Simulate voice input processing
                processVoiceInput("Hipertansiyon için en uygun ilaç nedir?") // Simulated voice input
            }
        }
    }
    
    /**
     * 🛑 Stop Voice Input
     */
    fun stopVoiceInput() {
        isVoiceInputActive = false
        addSystemMessage("🔇 Sesli komut durduruldu.")
    }
    
    /**
     * 🎙️ Process Voice Input
     */
    private fun processVoiceInput(voiceText: String) {
        viewModelScope.launch {
            if (!isVoiceInputActive) return@launch
            
            addUserMessage("🎤 $voiceText")
            updateProcessingState(true)
            
            try {
                // Create medical context for voice processing
                val medicalContext = TurkishMedicalContext(
                    currentScreen = "ai_assistant",
                    activeSession = true,
                    patientInfo = currentPatientContext
                )
                
                // Process voice command through AI
                val result = aiIntelligenceManager.voiceAIMedicalAssistant(
                    voiceInput = voiceText,
                    currentContext = medicalContext,
                    patientContext = currentPatientContext
                )
                
                if (result.isSuccess) {
                    val voiceResponse = result.getOrThrow()
                    processVoiceAIResponse(voiceResponse)
                } else {
                    addSystemMessage("Sesli komut işlenemedi. Lütfen tekrar deneyin.")
                }
            } catch (e: Exception) {
                addSystemMessage("Sesli komut hatası: ${e.message}")
            } finally {
                updateProcessingState(false)
                isVoiceInputActive = false
            }
        }
    }
    
    /**
     * 💡 Process AI Recommendation
     */
    fun processRecommendation(recommendation: AIRecommendation) {
        viewModelScope.launch {
            // Remove processed recommendation
            removeAIRecommendation(recommendation)
            
            // Process the recommendation based on type
            when (recommendation.type) {
                AIRecommendationType.DRUG_INTERACTION -> {
                    sendMessage("Bu ilaçların etkileşimlerini detaylı analiz eder misiniz?")
                }
                AIRecommendationType.DOSAGE_OPTIMIZATION -> {
                    sendMessage("Doz optimizasyonu önerilerinizi gösterir misiniz?")
                }
                AIRecommendationType.AUTHENTICITY_WARNING -> {
                    sendMessage("Bu ilacın orijinalliğini nasıl doğrulayabilirim?")
                }
                AIRecommendationType.EXPIRY_WARNING -> {
                    sendMessage("Son kullanma tarihi geçen ilaçlar için ne yapmalıyım?")
                }
                AIRecommendationType.QUALITY_CONCERN -> {
                    sendMessage("İlaç kalitesi konusunda ne önerirsiniz?")
                }
                AIRecommendationType.PREDICTIVE_INSIGHT -> {
                    sendMessage("Bu öngörü hakkında daha fazla bilgi verir misiniz?")
                }
            }
        }
    }
    
    /**
     * 👤 Set Patient Context
     */
    fun setPatientContext(patientContext: TurkishPatientContext) {
        currentPatientContext = patientContext
        addSystemMessage("👤 Hasta bilgileri güncellendi. Kişiselleştirilmiş öneriler sağlanacak.")
        
        // Update suggestions based on patient context
        updateSuggestedQuestions(getPersonalizedSuggestedQuestions(patientContext))
    }
    
    // PRIVATE HELPER METHODS
    
    private fun processAIResponse(aiResponse: ConversationalAIResponse) {
        // Add AI response to conversation
        addAIResponse(aiResponse.medicalResponse)
        
        // Update conversation context
        conversationContext.add(
            TurkishMedicalConversation(
                query = _uiState.value.conversations.lastOrNull { it.type == ConversationType.USER_MESSAGE }?.content ?: "",
                response = aiResponse.medicalResponse,
                timestamp = System.currentTimeMillis(),
                patientContext = currentPatientContext
            )
        )
        
        // Update suggested follow-ups
        updateSuggestedQuestions(aiResponse.suggestedFollowUps)
        
        // Add any AI recommendations
        if (aiResponse.medicalResponse.interactionData != null) {
            addAIRecommendation(
                AIRecommendation(
                    type = AIRecommendationType.DRUG_INTERACTION,
                    message = "İlaç etkileşimi tespit edildi. Detayları inceleyin.",
                    severity = AISeverity.MEDIUM,
                    actionRequired = true
                )
            )
        }
        
        // Check for predictive insights
        if (aiResponse.medicalResponse.prescriptionData != null) {
            addAIRecommendation(
                AIRecommendation(
                    type = AIRecommendationType.PREDICTIVE_INSIGHT,
                    message = "Bu reçete için doz optimizasyonu mümkün.",
                    severity = AISeverity.LOW,
                    actionRequired = false
                )
            )
        }
    }
    
    private fun processVoiceAIResponse(voiceResponse: VoiceAIMedicalResponse) {
        // Add AI response to conversation
        addSystemMessage("🎤 ${voiceResponse.aiEnhancedSpokenResponse}")
        
        // Add intelligent suggestions as recommendations
        voiceResponse.intelligentSuggestions.forEach { suggestion ->
            addAIRecommendation(
                AIRecommendation(
                    type = AIRecommendationType.PREDICTIVE_INSIGHT,
                    message = suggestion,
                    severity = AISeverity.LOW,
                    actionRequired = false
                )
            )
        }
        
        // Update suggested questions with contextual actions
        updateSuggestedQuestions(voiceResponse.contextualActions)
    }
    
    // UI STATE UPDATE METHODS
    
    private fun updateAIStatus(status: AIIntelligenceStatus) {
        _uiState.value = _uiState.value.copy(aiStatus = status)
    }
    
    private fun updateProcessingState(isProcessing: Boolean) {
        _uiState.value = _uiState.value.copy(isProcessing = isProcessing)
    }
    
    private fun addUserMessage(message: String) {
        val conversation = AIConversationItem(
            type = ConversationType.USER_MESSAGE,
            content = message,
            timestamp = System.currentTimeMillis()
        )
        
        _uiState.value = _uiState.value.copy(
            conversations = _uiState.value.conversations + conversation
        )
    }
    
    private fun addAIResponse(response: TurkishMedicalResponse) {
        val conversation = AIConversationItem(
            type = ConversationType.AI_RESPONSE,
            aiResponse = response,
            timestamp = System.currentTimeMillis()
        )
        
        _uiState.value = _uiState.value.copy(
            conversations = _uiState.value.conversations + conversation
        )
    }
    
    private fun addSystemMessage(message: String) {
        val systemResponse = TurkishMedicalResponse(
            turkishResponse = message,
            englishResponse = message,
            actionRequired = TurkishMedicalAction.PROVIDE_INFORMATION,
            confidence = 1.0f
        )
        
        addAIResponse(systemResponse)
    }
    
    private fun addAIRecommendation(recommendation: AIRecommendation) {
        val currentRecommendations = _uiState.value.aiRecommendations.toMutableList()
        
        // Avoid duplicate recommendations
        if (!currentRecommendations.any { it.message == recommendation.message }) {
            currentRecommendations.add(recommendation)
            _uiState.value = _uiState.value.copy(aiRecommendations = currentRecommendations)
        }
    }
    
    private fun removeAIRecommendation(recommendation: AIRecommendation) {
        val currentRecommendations = _uiState.value.aiRecommendations.toMutableList()
        currentRecommendations.remove(recommendation)
        _uiState.value = _uiState.value.copy(aiRecommendations = currentRecommendations)
    }
    
    private fun updateSuggestedQuestions(questions: List<String>) {
        _uiState.value = _uiState.value.copy(suggestedQuestions = questions)
    }
    
    // SUGGESTED QUESTIONS GENERATORS
    
    private fun getInitialSuggestedQuestions(): List<String> {
        return listOf(
            "AI sistemi hakkında bilgi ver",
            "Nasıl çalıştığını anlat",
            "Hangi konularda yardım edebilirsin?"
        )
    }
    
    private fun getBasicSuggestedQuestions(): List<String> {
        return listOf(
            "Hipertansiyon nedir?",
            "Diyabet ilaçları hangileri?",
            "İlaç etkileşimleri nelerdir?",
            "Doktor ne zaman görülmeli?"
        )
    }
    
    private fun getAdvancedSuggestedQuestions(): List<String> {
        return listOf(
            "Hipertansiyon için en uygun ilaç kombinasyonu nedir?",
            "Diyabet hastası için reçete oluştur",
            "Bu ilaçların etkileşimini kontrol et",
            "Yaşlı hasta için doz optimizasyonu yap",
            "Bu semptomlara göre tanı öner"
        )
    }
    
    private fun getPersonalizedSuggestedQuestions(patientContext: TurkishPatientContext): List<String> {
        val suggestions = mutableListOf<String>()
        
        // Age-based suggestions
        when {
            patientContext.age > 65 -> {
                suggestions.add("Yaşlı hasta için ilaç dozajı nasıl ayarlanır?")
                suggestions.add("Yaşlılık döneminde dikkat edilmesi gerekenler")
            }
            patientContext.age < 18 -> {
                suggestions.add("Çocuk hastaları için dozaj hesaplaması")
                suggestions.add("Çocuklarda güvenli ilaç kullanımı")
            }
            else -> {
                suggestions.add("Yetişkin hasta için standart dozaj")
            }
        }
        
        // Medical history-based suggestions
        if (patientContext.medicalHistory.isNotEmpty()) {
            suggestions.add("Mevcut hastalıklarıma uygun ilaç öner")
            suggestions.add("Kronik hastalık yönetimi")
        }
        
        // Allergy-based suggestions
        if (patientContext.allergies.isNotEmpty()) {
            suggestions.add("Alerjik reaksiyonlara alternatif ilaçlar")
            suggestions.add("İlaç alerjisi nasıl kontrol edilir?")
        }
        
        return suggestions.take(4) // Limit to 4 suggestions
    }
    
    override fun onCleared() {
        super.onCleared()
        // Cleanup AI resources
        aiIntelligenceManager.cleanup()
    }
}

/**
 * UI State for AI Assistant Screen
 */
data class AIAssistantUiState(
    val conversations: List<AIConversationItem> = emptyList(),
    val aiStatus: AIIntelligenceStatus? = null,
    val isProcessing: Boolean = false,
    val aiRecommendations: List<AIRecommendation> = emptyList(),
    val suggestedQuestions: List<String> = emptyList(),
    val isVoiceInputActive: Boolean = false,
    val currentPatientContext: TurkishPatientContext? = null
)
