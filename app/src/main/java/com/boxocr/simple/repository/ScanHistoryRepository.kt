package com.boxocr.simple.repository

import com.boxocr.simple.data.ScanResult
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ScanHistoryRepository - Manages scan history in memory
 * (Can be upgraded to Room database if needed)
 */
@Singleton
class ScanHistoryRepository @Inject constructor() {
    
    private val recentScans = mutableListOf<ScanResult>()
    
    /**
     * Add a new scan result
     */
    fun addScanResult(scannedText: String, matchedText: String?) {
        val timestamp = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
            .format(Date())
        
        val scanResult = ScanResult(
            scannedText = scannedText,
            matchedText = matchedText,
            timestamp = timestamp
        )
        
        recentScans.add(0, scanResult) // Add to beginning
        
        // Keep only last 50 scans
        if (recentScans.size > 50) {
            recentScans.removeAt(recentScans.size - 1)
        }
    }
    
    /**
     * Get recent scans
     */
    fun getRecentScans(): List<ScanResult> = recentScans.toList()
    
    /**
     * Clear all scans
     */
    fun clearScans() {
        recentScans.clear()
    }
}
