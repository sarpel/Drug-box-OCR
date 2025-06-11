package com.boxocr.simple.automation

/**
 * Data class representing a prescription session
 * Tracks drugs scanned during a single prescription workflow
 */
data class PrescriptionSession(
    val sessionId: String,
    val patientInfo: String = "",
    val startTime: Long = System.currentTimeMillis(),
    var endTime: Long? = null,
    val drugs: MutableList<String> = mutableListOf()
) {
    /**
     * Get session duration in seconds
     */
    fun getDurationSeconds(): Long {
        val end = endTime ?: System.currentTimeMillis()
        return (end - startTime) / 1000
    }
    
    /**
     * Get formatted duration string
     */
    fun getFormattedDuration(): String {
        val seconds = getDurationSeconds()
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return if (minutes > 0) {
            "${minutes}m ${remainingSeconds}s"
        } else {
            "${seconds}s"
        }
    }
    
    /**
     * Check if session is active (not completed)
     */
    fun isActive(): Boolean = endTime == null
    
    /**
     * Add drug to session
     */
    fun addDrug(drugName: String) {
        if (!drugs.contains(drugName)) {
            drugs.add(drugName)
        }
    }
    
    /**
     * Remove drug from session
     */
    fun removeDrug(drugName: String) {
        drugs.remove(drugName)
    }
    
    /**
     * Get drugs as comma-separated string
     */
    fun getDrugsAsString(): String = drugs.joinToString(", ")
    
    /**
     * Complete the session
     */
    fun complete() {
        if (endTime == null) {
            endTime = System.currentTimeMillis()
        }
    }
    
    /**
     * Get summary for display
     */
    fun getSummary(): String {
        return buildString {
            append("Session: $sessionId\n")
            if (patientInfo.isNotBlank()) {
                append("Patient: $patientInfo\n")
            }
            append("Duration: ${getFormattedDuration()}\n")
            append("Drugs (${drugs.size}): ${getDrugsAsString()}")
        }
    }
}
