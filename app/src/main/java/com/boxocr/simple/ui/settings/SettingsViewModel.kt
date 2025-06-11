package com.boxocr.simple.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boxocr.simple.data.AppSettings
import com.boxocr.simple.network.GeminiApiService
import com.boxocr.simple.network.GeminiRequest
import com.boxocr.simple.network.Content
import com.boxocr.simple.network.Part
import com.boxocr.simple.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * SettingsViewModel - Manages app settings and API testing
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val geminiApiService: GeminiApiService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        val settings = settingsRepository.getAppSettings()
        _uiState.value = _uiState.value.copy(
            apiKey = settings.apiKey,
            similarityThreshold = settings.similarityThreshold,
            autoClipboard = settings.autoClipboard
        )
    }
    
    fun updateApiKey(apiKey: String) {
        _uiState.value = _uiState.value.copy(
            apiKey = apiKey,
            testResult = null // Clear previous test result
        )
    }
    
    fun updateSimilarityThreshold(threshold: Float) {
        _uiState.value = _uiState.value.copy(similarityThreshold = threshold)
    }
    
    fun updateAutoClipboard(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(autoClipboard = enabled)
    }
    
    fun saveSettings() {
        val currentState = _uiState.value
        val settings = AppSettings(
            apiKey = currentState.apiKey,
            similarityThreshold = currentState.similarityThreshold,
            autoClipboard = currentState.autoClipboard
        )
        
        settingsRepository.saveAppSettings(settings)
        
        _uiState.value = _uiState.value.copy(
            saveMessage = "Settings saved successfully!"
        )
        
        // Clear save message after 3 seconds
        viewModelScope.launch {
            kotlinx.coroutines.delay(3000)
            _uiState.value = _uiState.value.copy(saveMessage = null)
        }
    }
    
    fun testApiConnection() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isTesting = true,
                testResult = null
            )
            
            try {
                val testRequest = GeminiRequest(
                    contents = listOf(
                        Content(
                            parts = listOf(
                                Part(text = "Hello, this is a test. Please respond with 'API connection successful'.")
                            )
                        )
                    )
                )
                
                val response = geminiApiService.generateContent(
                    apiKey = _uiState.value.apiKey,
                    request = testRequest
                )
                
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isTesting = false,
                        testResult = "✓ API connection successful!"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isTesting = false,
                        testResult = "✗ API test failed: ${response.code()} - ${response.message()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isTesting = false,
                    testResult = "✗ API test failed: ${e.message}"
                )
            }
        }
    }
}

/**
 * UI State for Settings Screen
 */
data class SettingsUiState(
    val apiKey: String = "",
    val similarityThreshold: Float = 0.7f,
    val autoClipboard: Boolean = true,
    val isTesting: Boolean = false,
    val testResult: String? = null,
    val saveMessage: String? = null
)
