package com.boxocr.simple.ui.multidrug

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boxocr.simple.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Multi-Drug Results ViewModel - Phase 1 Week 2 Implementation
 * 
 * Manages the display and interaction logic for multi-drug scan results,
 * including corrections, sharing, and batch integration.
 */
@HiltViewModel
class MultiDrugResultsViewModel @Inject constructor(
    private val multiDrugScannerRepository: MultiDrugScannerRepository,
    private val batchScanningRepository: BatchScanningRepository,
    private val visualDrugDatabaseRepository: VisualDrugDatabaseRepository,
    private val scanHistoryRepository: ScanHistoryRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MultiDrugResultsUiState())
    val uiState: StateFlow<MultiDrugResultsUiState> = _uiState.asStateFlow()
    
    /**
     * Load result by ID
     */
    fun loadResult(resultId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                
                // In a real implementation, this would load from database
                // For now, we'll simulate loading
                val result = loadResultFromStorage(resultId)
                
                if (result != null) {
                    _uiState.value = _uiState.value.copy(
                        result = result,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Sonuç bulunamadı"
                    )
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Sonuç yüklenirken hata: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Share results via Android share intent
     */
    fun shareResults(context: Context) {
        val result = _uiState.value.result ?: return
        
        val shareText = buildString {
            appendLine("İlaç Tarama Sonuçları")
            appendLine("=" * 25)
            appendLine()
            appendLine("Tespit Edilen İlaçlar: ${result.drugCount}")
            appendLine("Ortalama Güven: ${String.format("%.1f", result.averageConfidence * 100)}%")
            appendLine("İşlem Süresi: ${result.processingTime}ms")
            appendLine()
            
            result.enhancedResults.forEachIndexed { index, drug ->
                appendLine("${index + 1}. ${drug.finalDrugName}")
                appendLine("   Güven: ${String.format("%.1f", drug.finalConfidence * 100)}%")
                appendLine("   Yöntem: ${drug.enhancementMethod.name.replace("_", " ")}")
                
                if (drug.originalRegion.ocrText != drug.finalDrugName) {
                    appendLine("   Orijinal OCR: \"${drug.originalRegion.ocrText}\"")
                }
                appendLine()
            }
            
            appendLine("Tarih: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(result.timestamp))}")
            appendLine()
            appendLine("BoxOCR ile oluşturuldu")
        }
        
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, "İlaç Tarama Sonuçları - ${result.drugCount} İlaç")
        }
        
        val chooser = Intent.createChooser(shareIntent, "Sonuçları Paylaş")
        context.startActivity(chooser)
    }
    
    /**
     * Copy results to clipboard
     */
    fun copyToClipboard(context: Context) {
        val result = _uiState.value.result ?: return
        
        val clipboardText = result.drugNames.joinToString(", ")
        
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("İlaç İsimleri", clipboardText)
        clipboardManager.setPrimaryClip(clip)
        
        // Show success message (you might want to use a SnackBar or Toast)
        _uiState.value = _uiState.value.copy(
            successMessage = "İlaç isimleri panoya kopyalandı"
        )
    }
    
    /**
     * Add results to existing batch session
     */
    fun addToBatch() {
        val result = _uiState.value.result ?: return
        
        viewModelScope.launch {
            try {
                result.batchIntegration?.let { batchInfo ->
                    // Batch integration already exists, just notify user
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Sonuçlar zaten batch oturumunda mevcut"
                    )
                } ?: run {
                    // Create new batch session for this result
                    val sessionId = batchScanningRepository.createBatchSession(
                        sessionName = "Multi-Drug Scan ${System.currentTimeMillis()}",
                        expectedItems = result.drugCount
                    )
                    
                    // Add each drug to the batch
                    result.enhancedResults.forEach { drug ->
                        batchScanningRepository.addToBatch(
                            sessionId = sessionId,
                            drugName = drug.finalDrugName,
                            confidence = drug.finalConfidence,
                            source = "multi_drug_results"
                        )
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Sonuçlar batch oturumuna eklendi",
                        batchSessionId = sessionId
                    )
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Batch'e eklenirken hata: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Correct a drug result
     */
    fun correctResult(drugResult: EnhancedDrugResult, correctedName: String) {
        viewModelScope.launch {
            try {
                // Store correction in visual database for learning
                visualDrugDatabaseRepository.addDrugBoxImage(
                    bitmap = drugResult.originalRegion.region.bitmap,
                    drugName = correctedName,
                    condition = DrugBoxCondition.DAMAGED, // Since it needed correction
                    source = ImageSource.USER_UPLOAD
                )
                
                // Update the current result
                val currentResult = _uiState.value.result ?: return@launch
                val updatedResults = currentResult.enhancedResults.map { result ->
                    if (result == drugResult) {
                        result.copy(
                            finalDrugName = correctedName,
                            enhancementMethod = EnhancementMethod.MANUAL_CORRECTION,
                            finalConfidence = 1.0f // Manual correction is 100% confident
                        )
                    } else {
                        result
                    }
                }
                
                val updatedMainResult = currentResult.copy(
                    enhancedResults = updatedResults
                )
                
                _uiState.value = _uiState.value.copy(
                    result = updatedMainResult,
                    successMessage = "Düzeltme kaydedildi ve öğrenme sistemine eklendi"
                )
                
                // Save correction to scan history
                saveResultToHistory(updatedMainResult)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Düzeltme kaydedilirken hata: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Export results to different formats
     */
    fun exportResults(format: ExportFormat, context: Context) {
        val result = _uiState.value.result ?: return
        
        viewModelScope.launch {
            try {
                val exportData = when (format) {
                    ExportFormat.CSV -> exportToCsv(result)
                    ExportFormat.JSON -> exportToJson(result)
                    ExportFormat.TEXT -> exportToText(result)
                }
                
                // Save to external storage or share
                shareExportedData(context, exportData, format)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Dışa aktarılırken hata: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Clear success/error messages
     */
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            successMessage = null,
            errorMessage = null
        )
    }
    
    /**
     * Retry processing with different settings
     */
    fun retryProcessing(newSettings: ProcessingSettings) {
        val result = _uiState.value.result ?: return
        
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isReprocessing = true)
                
                // Reprocess the original image with new settings
                val reprocessedResult = multiDrugScannerRepository.processCameraImage(
                    result.originalImage
                )
                
                _uiState.value = _uiState.value.copy(
                    result = reprocessedResult,
                    isReprocessing = false,
                    successMessage = "Yeniden işleme tamamlandı"
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isReprocessing = false,
                    errorMessage = "Yeniden işleme hatası: ${e.message}"
                )
            }
        }
    }
    
    // Private helper methods
    
    private suspend fun loadResultFromStorage(resultId: String): MultiDrugScanResult? {
        // In a real implementation, this would load from database
        // For now, return null to indicate not found
        return null
    }
    
    private suspend fun saveResultToHistory(result: MultiDrugScanResult) {
        try {
            // Save each drug result to scan history
            result.enhancedResults.forEach { drugResult ->
                scanHistoryRepository.saveScanResult(
                    drugName = drugResult.finalDrugName,
                    confidence = drugResult.finalConfidence,
                    processingTime = result.processingTime,
                    source = result.source.name,
                    timestamp = result.timestamp
                )
            }
        } catch (e: Exception) {
            // Log error but don't fail the whole operation
        }
    }
    
    private fun exportToCsv(result: MultiDrugScanResult): String {
        return buildString {
            appendLine("Drug Name,Confidence,Enhancement Method,Original OCR,Processing Time")
            
            result.enhancedResults.forEach { drug ->
                appendLine("${drug.finalDrugName},${drug.finalConfidence},${drug.enhancementMethod.name},\"${drug.originalRegion.ocrText}\",${result.processingTime}")
            }
        }
    }
    
    private fun exportToJson(result: MultiDrugScanResult): String {
        // In a real implementation, you'd use a JSON library like Moshi or Gson
        return buildString {
            appendLine("{")
            appendLine("  \"timestamp\": ${result.timestamp},")
            appendLine("  \"drugCount\": ${result.drugCount},")
            appendLine("  \"averageConfidence\": ${result.averageConfidence},")
            appendLine("  \"processingTime\": ${result.processingTime},")
            appendLine("  \"source\": \"${result.source.name}\",")
            appendLine("  \"drugs\": [")
            
            result.enhancedResults.forEachIndexed { index, drug ->
                appendLine("    {")
                appendLine("      \"name\": \"${drug.finalDrugName}\",")
                appendLine("      \"confidence\": ${drug.finalConfidence},")
                appendLine("      \"method\": \"${drug.enhancementMethod.name}\",")
                appendLine("      \"originalOCR\": \"${drug.originalRegion.ocrText}\"")
                append("    }")
                if (index < result.enhancedResults.size - 1) appendLine(",")
                else appendLine()
            }
            
            appendLine("  ]")
            appendLine("}")
        }
    }
    
    private fun exportToText(result: MultiDrugScanResult): String {
        return buildString {
            appendLine("İlaç Tarama Sonuçları")
            appendLine("=" * 50)
            appendLine()
            appendLine("Tarih: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(result.timestamp))}")
            appendLine("Tespit Edilen İlaç Sayısı: ${result.drugCount}")
            appendLine("Ortalama Güven Skoru: ${String.format("%.2f", result.averageConfidence * 100)}%")
            appendLine("İşlem Süresi: ${result.processingTime}ms")
            appendLine("Kaynak: ${result.source.name}")
            appendLine()
            
            appendLine("Detaylı Sonuçlar:")
            appendLine("-" * 30)
            
            result.enhancedResults.forEachIndexed { index, drug ->
                appendLine()
                appendLine("${index + 1}. İlaç: ${drug.finalDrugName}")
                appendLine("   Güven Skoru: ${String.format("%.2f", drug.finalConfidence * 100)}%")
                appendLine("   İyileştirme Yöntemi: ${drug.enhancementMethod.name.replace("_", " ")}")
                
                if (drug.originalRegion.ocrText != drug.finalDrugName) {
                    appendLine("   Orijinal OCR Metni: \"${drug.originalRegion.ocrText}\"")
                }
                
                drug.drugDatabaseMatch?.let { match ->
                    appendLine("   Veritabanı Eşleşmesi: Evet (${String.format("%.2f", match.confidence * 100)}%)")
                }
                
                drug.visualRecovery?.let { recovery ->
                    appendLine("   Görsel Kurtarma: ${recovery.method.name} (${String.format("%.2f", recovery.confidence * 100)}%)")
                }
            }
            
            appendLine()
            appendLine("=" * 50)
            appendLine("BoxOCR Multi-Drug Scanner ile oluşturuldu")
        }
    }
    
    private fun shareExportedData(context: Context, data: String, format: ExportFormat) {
        val mimeType = when (format) {
            ExportFormat.CSV -> "text/csv"
            ExportFormat.JSON -> "application/json"
            ExportFormat.TEXT -> "text/plain"
        }
        
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = mimeType
            putExtra(Intent.EXTRA_TEXT, data)
            putExtra(Intent.EXTRA_SUBJECT, "İlaç Tarama Sonuçları - ${format.name}")
        }
        
        val chooser = Intent.createChooser(shareIntent, "Sonuçları Dışa Aktar")
        context.startActivity(chooser)
    }
}

/**
 * UI State for Multi-Drug Results Screen
 */
data class MultiDrugResultsUiState(
    val result: MultiDrugScanResult? = null,
    val isLoading: Boolean = false,
    val isReprocessing: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val batchSessionId: String? = null
)

/**
 * Export format options
 */
enum class ExportFormat {
    CSV,
    JSON,
    TEXT
}

/**
 * Processing settings for retry
 */
data class ProcessingSettings(
    val confidenceThreshold: Float = 0.7f,
    val useVisualRecovery: Boolean = true,
    val enableDamagedTextRecovery: Boolean = true,
    val maxDetections: Int = 5
)
