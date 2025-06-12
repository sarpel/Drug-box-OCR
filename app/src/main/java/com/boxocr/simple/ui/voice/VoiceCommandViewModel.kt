package com.boxocr.simple.ui.voice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.boxocr.simple.repository.VoiceCommandRepository
import com.boxocr.simple.repository.VoiceCommandState
import com.boxocr.simple.repository.VoiceAction

/**
 * ViewModel for Voice Command functionality - Phase 2 Feature
 */
@HiltViewModel
class VoiceCommandViewModel @Inject constructor(
    private val voiceCommandRepository: VoiceCommandRepository
) : ViewModel() {

    val voiceState: StateFlow<VoiceCommandState> = voiceCommandRepository.voiceCommandState

    /**
     * Initialize voice recognition
     */
    fun initializeVoiceRecognition() {
        // Check if speech recognition is available
        if (!voiceCommandRepository.isSpeechRecognitionAvailable()) {
            // Handle unavailable speech recognition
            return
        }
    }

    /**
     * Start listening for voice commands
     */
    fun startListening() {
        voiceCommandRepository.startListening()
    }

    /**
     * Stop listening for voice commands
     */
    fun stopListening() {
        voiceCommandRepository.stopListening()
    }

    /**
     * Clear error state
     */
    fun clearError() {
        voiceCommandRepository.clearError()
    }

    /**
     * Clear last voice action
     */
    fun clearLastAction() {
        // Implementation would reset the last action in the repository
        // For now, this is handled by the repository state management
    }

    /**
     * Get available voice commands for help
     */
    fun getAvailableCommands(): Map<String, String> {
        return voiceCommandRepository.getAvailableCommands()
    }

    /**
     * Cleanup when ViewModel is cleared
     */
    override fun onCleared() {
        super.onCleared()
        voiceCommandRepository.cleanup()
    }
}
