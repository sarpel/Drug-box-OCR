package com.boxocr.simple.ui.verification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.boxocr.simple.data.VerificationData
import com.boxocr.simple.data.VerificationDecision
import com.boxocr.simple.repository.VerificationRepository

/**
 * ViewModel for Drug Verification Preview Screen - Phase 2 Feature
 * Manages verification state and user interactions
 */
@HiltViewModel
class VerificationViewModel @Inject constructor(
    private val verificationRepository: VerificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VerificationUiState())
    val uiState: StateFlow<VerificationUiState> = _uiState.asStateFlow()

    init {
        // Automatically load verification data when ViewModel is created
        loadVerificationData()
    }

    /**
     * Load verification data from repository
     */
    private fun loadVerificationData() {
        viewModelScope.launch {
            verificationRepository.verificationData.collect { data ->
                data?.let {
                    initializeVerification(it)
                }
            }
        }
    }

    /**
     * Initialize verification with data from camera/OCR
     */
    fun initializeVerification(verificationData: VerificationData) {
        _uiState.value = _uiState.value.copy(
            verificationData = verificationData,
            editedDrugName = verificationData.matchResult.bestMatch ?: "",
            isEditing = false
        )
    }

    /**
     * Update the edited drug name
     */
    fun updateEditedName(newName: String) {
        _uiState.value = _uiState.value.copy(
            editedDrugName = newName
        )
    }

    /**
     * Toggle editing mode
     */
    fun toggleEditing() {
        _uiState.value = _uiState.value.copy(
            isEditing = !_uiState.value.isEditing
        )
    }

    /**
     * Reset verification state
     */
    fun resetVerification() {
        _uiState.value = VerificationUiState()
    }

    /**
     * Get the final drug name (edited or original match)
     */
    fun getFinalDrugName(): String {
        return _uiState.value.editedDrugName
    }

    /**
     * Check if the drug name has been edited by user
     */
    fun isDrugNameEdited(): Boolean {
        val originalMatch = _uiState.value.verificationData?.matchResult?.bestMatch ?: ""
        return _uiState.value.editedDrugName != originalMatch
    }

    /**
     * Get verification decision based on current state
     */
    fun getVerificationDecision(): VerificationDecision {
        val finalName = getFinalDrugName()
        return if (isDrugNameEdited()) {
            VerificationDecision.Edited(finalName)
        } else {
            VerificationDecision.Confirmed(finalName)
        }
    }

    /**
     * Validate if the current drug name is acceptable
     */
    fun isValidDrugName(): Boolean {
        return _uiState.value.editedDrugName.isNotBlank() && 
               _uiState.value.editedDrugName.trim().length >= 2
    }

    /**
     * Get confidence level description
     */
    fun getConfidenceDescription(): String {
        val confidence = _uiState.value.verificationData?.matchResult?.confidence ?: 0f
        return when {
            confidence >= 0.9f -> "Excellent match"
            confidence >= 0.8f -> "Good match"
            confidence >= 0.7f -> "Fair match"
            confidence >= 0.6f -> "Poor match"
            else -> "Very poor match"
        }
    }

    /**
     * Determine if enhanced matching should be suggested
     */
    fun shouldSuggestEnhancedMatching(): Boolean {
        val confidence = _uiState.value.verificationData?.matchResult?.confidence ?: 0f
        return confidence < 0.8f
    }

    /**
     * Get formatted timestamp
     */
    fun getFormattedTimestamp(): String {
        val timestamp = _uiState.value.verificationData?.timestamp ?: 0L
        return java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault())
            .format(java.util.Date(timestamp))
    }

    /**
     * Cleanup resources
     */
    override fun onCleared() {
        super.onCleared()
        // Cleanup any temporary files if needed
        cleanupTempFiles()
    }

    private fun cleanupTempFiles() {
        // Clean up temporary image files if needed
        // This could be implemented based on specific requirements
    }
}

/**
 * UI State for the Verification Screen
 */
data class VerificationUiState(
    val verificationData: VerificationData? = null,
    val editedDrugName: String = "",
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)
