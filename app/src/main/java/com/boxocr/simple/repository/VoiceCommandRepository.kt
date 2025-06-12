package com.boxocr.simple.repository

import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import java.util.*

/**
 * Voice Command Repository - Phase 2 Feature
 * Handles voice recognition for hands-free operation during scanning
 */
@Singleton
class VoiceCommandRepository @Inject constructor(
    private val context: Context
) {
    
    private val _voiceCommandState = MutableStateFlow(VoiceCommandState())
    val voiceCommandState: StateFlow<VoiceCommandState> = _voiceCommandState.asStateFlow()
    
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    
    // Define voice commands and their actions
    private val voiceCommands = mapOf(
        // Navigation commands
        "next drug" to VoiceAction.NEXT_DRUG,
        "next" to VoiceAction.NEXT_DRUG,
        "scan next" to VoiceAction.NEXT_DRUG,
        
        // Confirmation commands
        "confirm" to VoiceAction.CONFIRM,
        "yes" to VoiceAction.CONFIRM,
        "correct" to VoiceAction.CONFIRM,
        "add drug" to VoiceAction.CONFIRM,
        
        // Rejection commands
        "cancel" to VoiceAction.CANCEL,
        "no" to VoiceAction.CANCEL,
        "wrong" to VoiceAction.CANCEL,
        "rescan" to VoiceAction.CANCEL,
        
        // Prescription management
        "finish prescription" to VoiceAction.COMPLETE_PRESCRIPTION,
        "complete prescription" to VoiceAction.COMPLETE_PRESCRIPTION,
        "end session" to VoiceAction.COMPLETE_PRESCRIPTION,
        "done" to VoiceAction.COMPLETE_PRESCRIPTION,
        
        // Enhanced matching
        "show more options" to VoiceAction.ENHANCED_MATCHING,
        "enhanced matching" to VoiceAction.ENHANCED_MATCHING,
        "more matches" to VoiceAction.ENHANCED_MATCHING,
        
        // Help and info
        "help" to VoiceAction.HELP,
        "what can I say" to VoiceAction.HELP,
        "voice commands" to VoiceAction.HELP,
        
        // Camera controls
        "take photo" to VoiceAction.CAPTURE_PHOTO,
        "capture" to VoiceAction.CAPTURE_PHOTO,
        "scan" to VoiceAction.CAPTURE_PHOTO,
        
        // Repeat commands
        "repeat" to VoiceAction.REPEAT_LAST,
        "say again" to VoiceAction.REPEAT_LAST,
        "what was that" to VoiceAction.REPEAT_LAST
    )
    
    /**
     * Check if speech recognition is available
     */
    fun isSpeechRecognitionAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }
    
    /**
     * Start listening for voice commands
     */
    fun startListening() {
        if (!isSpeechRecognitionAvailable()) {
            _voiceCommandState.value = _voiceCommandState.value.copy(
                error = "Speech recognition not available on this device"
            )
            return
        }
        
        if (isListening) {
            return
        }
        
        try {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Say a voice command...")
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }
            
            speechRecognizer?.setRecognitionListener(VoiceRecognitionListener())
            speechRecognizer?.startListening(intent)
            
            isListening = true
            _voiceCommandState.value = _voiceCommandState.value.copy(
                isListening = true,
                error = null
            )
            
        } catch (e: Exception) {
            _voiceCommandState.value = _voiceCommandState.value.copy(
                isListening = false,
                error = "Failed to start voice recognition: ${e.message}"
            )
        }
    }
    
    /**
     * Stop listening for voice commands
     */
    fun stopListening() {
        speechRecognizer?.stopListening()
        isListening = false
        _voiceCommandState.value = _voiceCommandState.value.copy(
            isListening = false
        )
    }
    
    /**
     * Process recognized speech and return voice action
     */
    private fun processRecognizedSpeech(recognizedText: String): VoiceAction? {
        val cleanedText = recognizedText.lowercase().trim()
        
        // Direct match first
        voiceCommands[cleanedText]?.let { return it }
        
        // Partial match - check if recognized text contains any command
        for ((command, action) in voiceCommands) {
            if (cleanedText.contains(command)) {
                return action
            }
        }
        
        // Check for drug name additions (custom drug names)
        if (cleanedText.startsWith("add ") || cleanedText.startsWith("drug ")) {
            val drugName = cleanedText.removePrefix("add ").removePrefix("drug ").trim()
            if (drugName.isNotEmpty()) {
                return VoiceAction.ADD_CUSTOM_DRUG
            }
        }
        
        return null
    }
    
    /**
     * Get available voice commands for help
     */
    fun getAvailableCommands(): Map<String, String> {
        return mapOf(
            "Navigation" to "Next, Next drug, Scan next",
            "Confirmation" to "Confirm, Yes, Correct, Add drug",
            "Rejection" to "Cancel, No, Wrong, Rescan",
            "Prescription" to "Finish prescription, Complete prescription, Done",
            "Matching" to "Show more options, Enhanced matching",
            "Camera" to "Take photo, Capture, Scan",
            "Help" to "Help, What can I say, Voice commands"
        )
    }
    
    /**
     * Clear error state
     */
    fun clearError() {
        _voiceCommandState.value = _voiceCommandState.value.copy(error = null)
    }
    
    /**
     * Cleanup resources
     */
    fun cleanup() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        isListening = false
        _voiceCommandState.value = VoiceCommandState()
    }
    
    /**
     * Voice recognition listener
     */
    private inner class VoiceRecognitionListener : android.speech.RecognitionListener {
        
        override fun onReadyForSpeech(params: Bundle?) {
            _voiceCommandState.value = _voiceCommandState.value.copy(
                isListening = true,
                error = null
            )
        }
        
        override fun onBeginningOfSpeech() {
            _voiceCommandState.value = _voiceCommandState.value.copy(
                isProcessing = true
            )
        }
        
        override fun onRmsChanged(rmsdB: Float) {
            // Update audio level for visual feedback
            _voiceCommandState.value = _voiceCommandState.value.copy(
                audioLevel = rmsdB
            )
        }
        
        override fun onBufferReceived(buffer: ByteArray?) {
            // Not used
        }
        
        override fun onEndOfSpeech() {
            _voiceCommandState.value = _voiceCommandState.value.copy(
                isProcessing = false
            )
        }
        
        override fun onError(error: Int) {
            val errorMessage = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                SpeechRecognizer.ERROR_NETWORK -> "Network error"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                SpeechRecognizer.ERROR_NO_MATCH -> "No speech input matched"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
                SpeechRecognizer.ERROR_SERVER -> "Error from server"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                else -> "Unknown error occurred"
            }
            
            isListening = false
            _voiceCommandState.value = _voiceCommandState.value.copy(
                isListening = false,
                isProcessing = false,
                error = errorMessage
            )
        }
        
        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val recognizedText = matches?.firstOrNull()
            
            isListening = false
            
            if (recognizedText != null) {
                val voiceAction = processRecognizedSpeech(recognizedText)
                
                _voiceCommandState.value = _voiceCommandState.value.copy(
                    isListening = false,
                    isProcessing = false,
                    lastRecognizedText = recognizedText,
                    lastVoiceAction = voiceAction,
                    recognitionResults = matches,
                    error = null
                )
            } else {
                _voiceCommandState.value = _voiceCommandState.value.copy(
                    isListening = false,
                    isProcessing = false,
                    error = "No speech recognized"
                )
            }
        }
        
        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val partialText = matches?.firstOrNull()
            
            _voiceCommandState.value = _voiceCommandState.value.copy(
                partialRecognitionText = partialText
            )
        }
        
        override fun onEvent(eventType: Int, params: Bundle?) {
            // Not used
        }
    }
}

/**
 * Voice command state
 */
data class VoiceCommandState(
    val isListening: Boolean = false,
    val isProcessing: Boolean = false,
    val lastRecognizedText: String? = null,
    val partialRecognitionText: String? = null,
    val lastVoiceAction: VoiceAction? = null,
    val recognitionResults: List<String>? = null,
    val audioLevel: Float = 0f,
    val error: String? = null
)

/**
 * Available voice actions
 */
enum class VoiceAction {
    // Navigation
    NEXT_DRUG,
    
    // Confirmation
    CONFIRM,
    
    // Rejection
    CANCEL,
    
    // Prescription management
    COMPLETE_PRESCRIPTION,
    
    // Enhanced matching
    ENHANCED_MATCHING,
    
    // Camera controls
    CAPTURE_PHOTO,
    
    // Custom drug addition
    ADD_CUSTOM_DRUG,
    
    // Help and info
    HELP,
    
    // Repeat commands
    REPEAT_LAST
}
