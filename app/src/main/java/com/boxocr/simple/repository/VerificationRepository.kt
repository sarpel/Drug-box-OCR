package com.boxocr.simple.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import com.boxocr.simple.data.VerificationData

/**
 * Repository for managing verification data transfer between screens
 * Phase 2 Feature - Drug Verification Preview
 */
@Singleton
class VerificationRepository @Inject constructor() {
    
    private val _verificationData = MutableStateFlow<VerificationData?>(null)
    val verificationData: StateFlow<VerificationData?> = _verificationData.asStateFlow()
    
    /**
     * Set verification data from camera capture
     */
    fun setVerificationData(data: VerificationData) {
        _verificationData.value = data
    }
    
    /**
     * Get current verification data
     */
    fun getCurrentVerificationData(): VerificationData? {
        return _verificationData.value
    }
    
    /**
     * Clear verification data after processing
     */
    fun clearVerificationData() {
        _verificationData.value = null
    }
    
    /**
     * Check if verification data is available
     */
    fun hasVerificationData(): Boolean {
        return _verificationData.value != null
    }
}
