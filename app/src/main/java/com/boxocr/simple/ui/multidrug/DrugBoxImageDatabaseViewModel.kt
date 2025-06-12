package com.boxocr.simple.ui.multidrug

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.boxocr.simple.repository.*
import com.boxocr.simple.database.*
import javax.inject.Inject

/**
 * Drug Box Image Database ViewModel - Phase 2 Week 4 Enhancement
 * 
 * Manages the state and business logic for the drug box image database interface:
 * - Image upload and processing coordination
 * - Database statistics and monitoring
 * - Search and filtering functionality
 * - Quality assessment and optimization
 */
@HiltViewModel
class DrugBoxImageDatabaseViewModel @Inject constructor(
    private val drugBoxImageManager: DrugBoxImageDatabaseManager,
    private val visualDatabaseRepository: VisualDrugDatabaseRepository
) : ViewModel() {
    
    // UI State
    private val _uiState = MutableStateFlow(DrugBoxImageDatabaseUiState())
    val uiState: StateFlow<DrugBoxImageDatabaseUiState> = _uiState.asStateFlow()
    
    // Search and filter state
    private val _searchQuery = MutableStateFlow("")
    private val _selectedFilter = MutableStateFlow(DrugBoxFilter.ALL)
    
    init {
        loadDatabaseStatistics()
        loadImages()
        observeProcessingState()
    }
    
    /**
     * Load database statistics
     */
    private fun loadDatabaseStatistics() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                // Get statistics from repository
                val stats = getDatabaseStatistics()
                
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        totalImages = stats.totalImages,
                        totalDrugs = stats.totalDrugs,
                        databaseSize = formatDatabaseSize(stats.databaseSizeBytes),
                        lastOptimized = formatLastOptimized(stats.lastOptimizedTimestamp)
                    )
                }
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "İstatistikler yüklenirken hata: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Load images with current filter and search
     */
    private fun loadImages() {
        viewModelScope.launch {
            combine(
                _searchQuery.debounce(300), // Debounce search
                _selectedFilter
            ) { query, filter ->
                Pair(query, filter)
            }.collectLatest { (query, filter) ->
                try {
                    _uiState.update { it.copy(isLoading = true) }
                    
                    val images = getFilteredImages(query, filter)
                    
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            images = images,
                            searchQuery = query,
                            selectedFilter = filter
                        )
                    }
                    
                } catch (e: Exception) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = "Görüntüler yüklenirken hata: ${e.message}"
                        )
                    }
                }
            }
        }
    }
    
    /**
     * Observe processing state from the manager
     */
    private fun observeProcessingState() {
        viewModelScope.launch {
            drugBoxImageManager.processingState.collect { state ->
                when (state) {
                    is DatabaseProcessingState.Idle -> {
                        _uiState.update { 
                            it.copy(
                                isProcessing = false,
                                processingStatus = "",
                                processingProgress = 0f
                            )
                        }
                    }
                    
                    is DatabaseProcessingState.Processing -> {
                        _uiState.update { 
                            it.copy(
                                isProcessing = true,
                                processingStatus = state.message,
                                processingProgress = 0.5f // Indeterminate for now
                            )
                        }
                    }
                    
                    is DatabaseProcessingState.Completed -> {
                        _uiState.update { 
                            it.copy(
                                isProcessing = false,
                                processingStatus = state.message,
                                processingProgress = 1f
                            )
                        }
                        
                        // Reload statistics and images after completion
                        loadDatabaseStatistics()
                        loadImages()
                    }
                    
                    is DatabaseProcessingState.Error -> {
                        _uiState.update { 
                            it.copy(
                                isProcessing = false,
                                errorMessage = state.message
                            )
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Process selected images from user
     */
    fun processSelectedImages(uris: List<Uri>) {
        viewModelScope.launch {
            try {
                // Show image metadata input dialog first
                // For now, use default metadata
                val metadata = uris.map { 
                    DrugBoxImageMetadata(
                        drugName = "Bilinmeyen İlaç", // User should provide this
                        condition = DrugBoxCondition.UNKNOWN,
                        angle = DrugBoxAngle.UNKNOWN,
                        allowDuplicates = false
                    )
                }
                
                val result = drugBoxImageManager.processUserDrugBoxImages(
                    imageUris = uris,
                    metadata = metadata
                ) { current, total, currentDrug ->
                    // Update progress
                    val progress = current.toFloat() / total.toFloat()
                    _uiState.update { 
                        it.copy(
                            processingProgress = progress,
                            processingStatus = "İşleniyor ($current/$total): $currentDrug"
                        )
                    }
                }
                
                // Show result summary
                val message = if (result.errorCount > 0) {
                    "${result.successCount} başarılı, ${result.errorCount} hata"
                } else {
                    "${result.successCount} görüntü başarıyla eklendi"
                }
                
                _uiState.update { 
                    it.copy(processingStatus = message)
                }
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(errorMessage = "Görüntü işleme hatası: ${e.message}")
                }
            }
        }
    }
    
    /**
     * Start bulk upload process
     */
    fun startBulkUpload() {
        // Implementation for bulk upload from folder or cloud
        viewModelScope.launch {
            _uiState.update { 
                it.copy(processingStatus = "Toplu yükleme özelliği yakında...")
            }
        }
    }
    
    /**
     * Update search query
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    /**
     * Update filter
     */
    fun updateFilter(filter: DrugBoxFilter) {
        _selectedFilter.value = filter
    }
    
    /**
     * Select image for detailed view
     */
    fun selectImage(image: DrugBoxImageWithMetadata) {
        _uiState.update { it.copy(selectedImage = image) }
    }
    
    /**
     * Clear selected image
     */
    fun clearSelectedImage() {
        _uiState.update { it.copy(selectedImage = null) }
    }
    
    /**
     * Show image options
     */
    fun showImageOptions(image: DrugBoxImageWithMetadata) {
        // Implementation for image context menu
        selectImage(image)
    }
    
    /**
     * Delete image
     */
    fun deleteImage(image: DrugBoxImageWithMetadata) {
        viewModelScope.launch {
            try {
                // Implementation for image deletion
                _uiState.update { 
                    it.copy(processingStatus = "Görüntü siliniyor...")
                }
                
                // Remove from database
                // deleteImageById(image.id)
                
                // Reload images
                loadImages()
                clearSelectedImage()
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(errorMessage = "Görüntü silinirken hata: ${e.message}")
                }
            }
        }
    }
    
    /**
     * Update image metadata
     */
    fun updateImageMetadata(metadata: DrugBoxImageMetadata) {
        viewModelScope.launch {
            try {
                // Implementation for metadata update
                _uiState.update { 
                    it.copy(processingStatus = "Metadata güncelleniyor...")
                }
                
                // Update in database
                // updateImageMetadata(selectedImage.id, metadata)
                
                // Reload images
                loadImages()
                clearSelectedImage()
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(errorMessage = "Metadata güncellenirken hata: ${e.message}")
                }
            }
        }
    }
    
    /**
     * Optimize database
     */
    fun optimizeDatabase() {
        viewModelScope.launch {
            try {
                _uiState.update { 
                    it.copy(
                        isProcessing = true,
                        processingStatus = "Veritabanı optimize ediliyor..."
                    )
                }
                
                val result = drugBoxImageManager.optimizeDatabase()
                
                _uiState.update { 
                    it.copy(
                        isProcessing = false,
                        optimizationResult = result,
                        processingStatus = "Optimizasyon tamamlandı"
                    )
                }
                
                // Reload statistics after optimization
                loadDatabaseStatistics()
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isProcessing = false,
                        errorMessage = "Optimizasyon hatası: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    // Helper methods
    
    private suspend fun getDatabaseStatistics(): DatabaseStatistics {
        // Implementation to get database statistics
        return DatabaseStatistics(
            totalImages = 0, // Get from database
            totalDrugs = 0, // Get unique drug count
            databaseSizeBytes = 0L, // Calculate database size
            lastOptimizedTimestamp = 0L // Get last optimization time
        )
    }
    
    private suspend fun getFilteredImages(
        query: String, 
        filter: DrugBoxFilter
    ): List<DrugBoxImageWithMetadata> {
        // Implementation to get filtered images
        return emptyList() // Placeholder
    }
    
    private fun formatDatabaseSize(sizeBytes: Long): String {
        return when {
            sizeBytes >= 1024 * 1024 * 1024 -> "${sizeBytes / (1024 * 1024 * 1024)} GB"
            sizeBytes >= 1024 * 1024 -> "${sizeBytes / (1024 * 1024)} MB"
            sizeBytes >= 1024 -> "${sizeBytes / 1024} KB"
            else -> "$sizeBytes B"
        }
    }
    
    private fun formatLastOptimized(timestamp: Long): String {
        if (timestamp == 0L) return ""
        
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        val days = diff / (24 * 60 * 60 * 1000)
        
        return when {
            days == 0L -> "Bugün"
            days == 1L -> "Dün"
            days < 7 -> "$days gün önce"
            days < 30 -> "${days / 7} hafta önce"
            else -> "${days / 30} ay önce"
        }
    }
}

/**
 * UI State for Drug Box Image Database Screen
 */
data class DrugBoxImageDatabaseUiState(
    val isLoading: Boolean = false,
    val isProcessing: Boolean = false,
    val processingStatus: String = "",
    val processingProgress: Float = 0f,
    
    // Database statistics
    val totalImages: Int = 0,
    val totalDrugs: Int = 0,
    val databaseSize: String = "0 MB",
    val lastOptimized: String = "",
    
    // Images and filtering
    val images: List<DrugBoxImageWithMetadata> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: DrugBoxFilter = DrugBoxFilter.ALL,
    val selectedImage: DrugBoxImageWithMetadata? = null,
    
    // Optimization
    val optimizationResult: DatabaseOptimizationResult? = null,
    
    // Error handling
    val errorMessage: String? = null
)

/**
 * Database Statistics Data Class
 */
data class DatabaseStatistics(
    val totalImages: Int,
    val totalDrugs: Int,
    val databaseSizeBytes: Long,
    val lastOptimizedTimestamp: Long
)
