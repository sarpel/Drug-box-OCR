package com.boxocr.simple.repository

import android.content.Context
import android.content.SharedPreferences
import com.boxocr.simple.data.*
import com.boxocr.simple.ui.history.PrescriptionStatus
import com.boxocr.simple.ui.history.TimePeriod
import com.boxocr.simple.ui.history.ExportFormat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Prescription History Repository - Phase 3 Quality of Life Feature
 * 
 * Manages prescription history storage, statistics, export functionality
 */
@Singleton
class PrescriptionHistoryRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sharedPrefs: SharedPreferences = context.getSharedPreferences(
        "prescription_history", Context.MODE_PRIVATE
    )
    
    private val _historyFlow = MutableStateFlow<List<PrescriptionHistory>>(emptyList())
    
    init {
        loadHistoryFromStorage()
    }
    
    /**
     * Get all prescription history as Flow
     */
    fun getAllHistory(): Flow<List<PrescriptionHistory>> = _historyFlow.asStateFlow()
    
    /**
     * Get prescription statistics
     */
    fun getStatistics(): Flow<PrescriptionStatistics> {
        return _historyFlow.map { history ->
            calculateStatistics(history)
        }
    }
    
    /**
     * Add new prescription to history
     */
    suspend fun addPrescription(prescription: PrescriptionHistory) {
        val currentHistory = _historyFlow.value.toMutableList()
        currentHistory.add(0, prescription) // Add to beginning
        
        // Keep only last 1000 prescriptions
        if (currentHistory.size > 1000) {
            currentHistory.removeAt(currentHistory.size - 1)
        }
        
        _historyFlow.value = currentHistory
        saveHistoryToStorage(currentHistory)
    }
    
    /**
     * Update existing prescription
     */
    suspend fun updatePrescription(prescription: PrescriptionHistory) {
        val currentHistory = _historyFlow.value.toMutableList()
        val index = currentHistory.indexOfFirst { it.id == prescription.id }
        
        if (index != -1) {
            currentHistory[index] = prescription
            _historyFlow.value = currentHistory
            saveHistoryToStorage(currentHistory)
        }
    }
    
    /**
     * Delete prescription from history
     */
    suspend fun deletePrescription(prescriptionId: String) {
        val currentHistory = _historyFlow.value.toMutableList()
        currentHistory.removeAll { it.id == prescriptionId }
        
        _historyFlow.value = currentHistory
        saveHistoryToStorage(currentHistory)
    }
    
    /**
     * Get prescription by ID
     */
    suspend fun getPrescriptionById(id: String): PrescriptionHistory? {
        return _historyFlow.value.find { it.id == id }
    }
    
    /**
     * Export history to CSV format
     */
    suspend fun exportToCSV(history: List<PrescriptionHistory>): File {
        val exportDir = File(context.getExternalFilesDir(null), "exports")
        if (!exportDir.exists()) exportDir.mkdirs()
        
        val fileName = "prescription_history_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.csv"
        val exportFile = File(exportDir, fileName)
        
        FileWriter(exportFile).use { writer ->
            // CSV Header
            writer.appendLine("Date,Patient Name,Patient ID,Drug Count,Status,Drugs")
            
            // CSV Data
            history.forEach { prescription ->
                val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(prescription.timestamp))
                val patientName = prescription.patientInfo?.name ?: ""
                val patientId = prescription.patientInfo?.id ?: ""
                val drugs = prescription.drugs.joinToString("; ")
                
                writer.appendLine("\"$dateStr\",\"$patientName\",\"$patientId\",${prescription.drugCount},${prescription.status},\"$drugs\"")
            }
        }
        
        return exportFile
    }
    
    /**
     * Export history to PDF format (simplified - returns file for external processing)
     */
    suspend fun exportToPDF(history: List<PrescriptionHistory>): File {
        // For now, create a text file that can be converted to PDF
        val exportDir = File(context.getExternalFilesDir(null), "exports")
        if (!exportDir.exists()) exportDir.mkdirs()
        
        val fileName = "prescription_history_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.txt"
        val exportFile = File(exportDir, fileName)
        
        FileWriter(exportFile).use { writer ->
            writer.appendLine("PRESCRIPTION HISTORY REPORT")
            writer.appendLine("Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())}")
            writer.appendLine("Total Prescriptions: ${history.size}")
            writer.appendLine("=" * 50)
            writer.appendLine()
            
            history.forEach { prescription ->
                val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(prescription.timestamp))
                writer.appendLine("Date: $dateStr")
                writer.appendLine("Status: ${prescription.status}")
                
                prescription.patientInfo?.let { patient ->
                    writer.appendLine("Patient: ${patient.name}")
                    if (patient.id.isNotBlank()) {
                        writer.appendLine("Patient ID: ${patient.id}")
                    }
                }
                
                writer.appendLine("Drugs (${prescription.drugCount}):")
                prescription.drugs.forEach { drug ->
                    writer.appendLine("  • $drug")
                }
                writer.appendLine("-" * 30)
            }
        }
        
        return exportFile
    }
    
    /**
     * Search prescriptions by query
     */
    fun searchPrescriptions(query: String): Flow<List<PrescriptionHistory>> {
        return _historyFlow.map { history ->
            if (query.isBlank()) {
                history
            } else {
                history.filter { prescription ->
                    prescription.drugs.any { it.contains(query, ignoreCase = true) } ||
                    prescription.patientInfo?.name?.contains(query, ignoreCase = true) == true ||
                    prescription.id.contains(query, ignoreCase = true)
                }
            }
        }
    }
    
    /**
     * Get prescriptions by patient
     */
    fun getPrescriptionsByPatient(patientName: String): Flow<List<PrescriptionHistory>> {
        return _historyFlow.map { history ->
            history.filter { 
                it.patientInfo?.name?.equals(patientName, ignoreCase = true) == true 
            }
        }
    }
    
    /**
     * Calculate prescription statistics
     */
    private fun calculateStatistics(history: List<PrescriptionHistory>): PrescriptionStatistics {
        if (history.isEmpty()) {
            return PrescriptionStatistics()
        }
        
        val now = System.currentTimeMillis()
        val thisMonth = history.count { 
            (now - it.timestamp) < 30L * 24 * 60 * 60 * 1000 // 30 days
        }
        
        val uniqueDrugs = history.flatMap { it.drugs }.toSet().size
        
        val drugFrequency = history.flatMap { it.drugs }
            .groupingBy { it }
            .eachCount()
        
        val mostCommonDrug = drugFrequency.maxByOrNull { it.value }?.key ?: "None"
        
        val daysSinceFirst = if (history.isNotEmpty()) {
            val oldestTimestamp = history.minByOrNull { it.timestamp }?.timestamp ?: now
            maxOf(1, ((now - oldestTimestamp) / (24 * 60 * 60 * 1000)).toInt())
        } else 1
        
        val averagePerDay = history.size.toFloat() / daysSinceFirst
        
        return PrescriptionStatistics(
            totalPrescriptions = history.size,
            uniqueDrugs = uniqueDrugs,
            thisMonth = thisMonth,
            averagePerDay = averagePerDay,
            mostCommonDrug = mostCommonDrug
        )
    }
    
    /**
     * Load history from SharedPreferences
     */
    private fun loadHistoryFromStorage() {
        try {
            val historyJson = sharedPrefs.getString("history_data", "[]") ?: "[]"
            val history = Json.decodeFromString<List<PrescriptionHistory>>(historyJson)
            _historyFlow.value = history
        } catch (e: Exception) {
            // If loading fails, start with empty history
            _historyFlow.value = emptyList()
        }
    }
    
    /**
     * Save history to SharedPreferences
     */
    private fun saveHistoryToStorage(history: List<PrescriptionHistory>) {
        try {
            val historyJson = Json.encodeToString(history)
            sharedPrefs.edit()
                .putString("history_data", historyJson)
                .apply()
        } catch (e: Exception) {
            // Handle save error
        }
    }
    
    /**
     * Clear all history
     */
    suspend fun clearAllHistory() {
        _historyFlow.value = emptyList()
        sharedPrefs.edit().remove("history_data").apply()
    }
    
    /**
     * Get export directory
     */
    fun getExportDirectory(): File {
        val exportDir = File(context.getExternalFilesDir(null), "exports")
        if (!exportDir.exists()) exportDir.mkdirs()
        return exportDir
    }
}

/**
 * Extension function for String repeat (for formatting)
 */
private operator fun String.times(n: Int): String = this.repeat(n)
