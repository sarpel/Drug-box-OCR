package com.boxocr.simple.ui.templates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.boxocr.simple.data.*
import com.boxocr.simple.repository.PrescriptionTemplateRepository

/**
 * ViewModel for Prescription Templates Screen - Phase 2 Feature
 * Manages template browsing, searching, and selection
 */
@HiltViewModel
class TemplatesViewModel @Inject constructor(
    private val templateRepository: PrescriptionTemplateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TemplatesUiState())
    val uiState: StateFlow<TemplatesUiState> = _uiState.asStateFlow()

    init {
        loadTemplates()
    }

    /**
     * Load all templates and set up reactive updates
     */
    private fun loadTemplates() {
        viewModelScope.launch {
            combine(
                templateRepository.templates,
                _uiState
            ) { templates, currentState ->
                val popularTemplates = templateRepository.getPopularTemplates(5)
                val filteredTemplates = if (currentState.selectedCategory != null) {
                    templateRepository.getTemplatesByCategory(currentState.selectedCategory)
                } else {
                    templates
                }
                
                currentState.copy(
                    allTemplates = templates,
                    popularTemplates = popularTemplates,
                    filteredTemplates = filteredTemplates,
                    isLoading = false
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    /**
     * Select a category filter
     */
    fun selectCategory(category: TemplateCategory?) {
        val filteredTemplates = if (category != null) {
            templateRepository.getTemplatesByCategory(category)
        } else {
            _uiState.value.allTemplates
        }
        
        _uiState.value = _uiState.value.copy(
            selectedCategory = category,
            filteredTemplates = filteredTemplates
        )
    }

    /**
     * Toggle search mode
     */
    fun toggleSearchMode() {
        val newSearchMode = !_uiState.value.isSearchMode
        _uiState.value = _uiState.value.copy(
            isSearchMode = newSearchMode,
            searchQuery = if (!newSearchMode) "" else _uiState.value.searchQuery,
            searchResults = if (!newSearchMode) emptyList() else _uiState.value.searchResults
        )
    }

    /**
     * Update search query
     */
    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        
        if (query.isNotEmpty()) {
            performSearch(query)
        } else {
            _uiState.value = _uiState.value.copy(searchResults = emptyList())
        }
    }

    /**
     * Clear search
     */
    fun clearSearch() {
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            searchResults = emptyList()
        )
    }

    /**
     * Perform search in templates
     */
    private fun performSearch(query: String) {
        viewModelScope.launch {
            val searchResults = templateRepository.searchTemplates(query)
            _uiState.value = _uiState.value.copy(searchResults = searchResults)
        }
    }

    /**
     * Select a template for use
     */
    fun selectTemplate(template: PrescriptionTemplate) {
        templateRepository.selectTemplate(template)
        
        // Update usage count in UI
        val updatedTemplates = _uiState.value.allTemplates.map { t ->
            if (t.id == template.id) {
                t.copy(usageCount = t.usageCount + 1)
            } else {
                t
            }
        }
        
        _uiState.value = _uiState.value.copy(
            allTemplates = updatedTemplates,
            selectedTemplate = template
        )
    }

    /**
     * Get templates by category for quick access
     */
    fun getTemplatesByCategory(category: TemplateCategory): List<PrescriptionTemplate> {
        return templateRepository.getTemplatesByCategory(category)
    }

    /**
     * Get most popular templates
     */
    fun getPopularTemplates(limit: Int = 5): List<PrescriptionTemplate> {
        return templateRepository.getPopularTemplates(limit)
    }

    /**
     * Add a new custom template
     */
    fun addCustomTemplate(
        name: String,
        description: String,
        category: TemplateCategory,
        drugs: List<TemplateDrug>
    ) {
        val template = PrescriptionTemplate(
            id = "custom_${System.currentTimeMillis()}",
            name = name,
            description = description,
            category = category,
            drugs = drugs,
            isDefault = false
        )
        
        templateRepository.addCustomTemplate(template)
    }

    /**
     * Remove a template
     */
    fun removeTemplate(templateId: String) {
        templateRepository.removeTemplate(templateId)
    }

    /**
     * Get template statistics
     */
    fun getTemplateStats(): TemplateStats {
        val templates = _uiState.value.allTemplates
        return TemplateStats(
            totalTemplates = templates.size,
            customTemplates = templates.count { !it.isDefault },
            mostUsedTemplate = templates.maxByOrNull { it.usageCount },
            categoryCounts = TemplateCategory.values().associateWith { category ->
                templates.count { it.category == category }
            }
        )
    }

    /**
     * Clear selected template
     */
    fun clearSelectedTemplate() {
        templateRepository.clearSelectedTemplate()
        _uiState.value = _uiState.value.copy(selectedTemplate = null)
    }
}

/**
 * UI State for the Templates Screen
 */
data class TemplatesUiState(
    val allTemplates: List<PrescriptionTemplate> = emptyList(),
    val popularTemplates: List<PrescriptionTemplate> = emptyList(),
    val filteredTemplates: List<PrescriptionTemplate> = emptyList(),
    val searchResults: List<PrescriptionTemplate> = emptyList(),
    val selectedTemplate: PrescriptionTemplate? = null,
    val selectedCategory: TemplateCategory? = null,
    val searchQuery: String = "",
    val isSearchMode: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)

/**
 * Template statistics for analytics
 */
data class TemplateStats(
    val totalTemplates: Int,
    val customTemplates: Int,
    val mostUsedTemplate: PrescriptionTemplate?,
    val categoryCounts: Map<TemplateCategory, Int>
)
