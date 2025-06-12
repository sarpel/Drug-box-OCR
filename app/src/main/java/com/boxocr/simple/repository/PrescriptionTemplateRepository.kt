package com.boxocr.simple.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import com.boxocr.simple.data.*

/**
 * Repository for managing prescription templates - Phase 2 Feature
 * Provides predefined templates and allows custom template creation
 */
@Singleton
class PrescriptionTemplateRepository @Inject constructor() {
    
    private val _templates = MutableStateFlow(getDefaultTemplates())
    val templates: StateFlow<List<PrescriptionTemplate>> = _templates.asStateFlow()
    
    private val _selectedTemplate = MutableStateFlow<PrescriptionTemplate?>(null)
    val selectedTemplate: StateFlow<PrescriptionTemplate?> = _selectedTemplate.asStateFlow()
    
    /**
     * Get all available templates
     */
    fun getAllTemplates(): List<PrescriptionTemplate> = _templates.value
    
    /**
     * Get templates by category
     */
    fun getTemplatesByCategory(category: TemplateCategory): List<PrescriptionTemplate> {
        return _templates.value.filter { it.category == category }
    }
    
    /**
     * Get most frequently used templates
     */
    fun getPopularTemplates(limit: Int = 5): List<PrescriptionTemplate> {
        return _templates.value
            .sortedByDescending { it.usageCount }
            .take(limit)
    }
    
    /**
     * Get template by ID
     */
    fun getTemplateById(id: String): PrescriptionTemplate? {
        return _templates.value.find { it.id == id }
    }
    
    /**
     * Select a template for use
     */
    fun selectTemplate(template: PrescriptionTemplate) {
        _selectedTemplate.value = template
        incrementUsageCount(template.id)
    }
    
    /**
     * Clear selected template
     */
    fun clearSelectedTemplate() {
        _selectedTemplate.value = null
    }
    
    /**
     * Add a new custom template
     */
    fun addCustomTemplate(template: PrescriptionTemplate) {
        val updatedTemplates = _templates.value.toMutableList()
        updatedTemplates.add(template)
        _templates.value = updatedTemplates
    }
    
    /**
     * Remove a template
     */
    fun removeTemplate(templateId: String) {
        val updatedTemplates = _templates.value.filter { it.id != templateId }
        _templates.value = updatedTemplates
    }
    
    /**
     * Increment usage count for a template
     */
    private fun incrementUsageCount(templateId: String) {
        val updatedTemplates = _templates.value.map { template ->
            if (template.id == templateId) {
                template.copy(usageCount = template.usageCount + 1)
            } else {
                template
            }
        }
        _templates.value = updatedTemplates
    }
    
    /**
     * Search templates by name or drug names
     */
    fun searchTemplates(query: String): List<PrescriptionTemplate> {
        val searchQuery = query.lowercase()
        return _templates.value.filter { template ->
            template.name.lowercase().contains(searchQuery) ||
            template.description.lowercase().contains(searchQuery) ||
            template.drugs.any { drug ->
                drug.name.lowercase().contains(searchQuery)
            }
        }
    }
    
    /**
     * Get predefined templates for common medical conditions
     */
    private fun getDefaultTemplates(): List<PrescriptionTemplate> {
        return listOf(
            // Diabetes Management
            PrescriptionTemplate(
                id = "diabetes_type2",
                name = "Type 2 Diabetes - Standard",
                description = "Standard treatment for Type 2 diabetes management",
                category = TemplateCategory.DIABETES,
                drugs = listOf(
                    TemplateDrug("Metformin", "500mg", "2x daily", "Long term", "Take with meals"),
                    TemplateDrug("Glimepiride", "2mg", "1x daily", "Long term", "Take before breakfast"),
                    TemplateDrug("Vitamin B12", "1000mcg", "1x daily", "Long term", "Support for Metformin use")
                ),
                isDefault = true
            ),
            
            // Hypertension
            PrescriptionTemplate(
                id = "hypertension_stage1",
                name = "Hypertension - Stage 1",
                description = "Initial treatment for Stage 1 hypertension",
                category = TemplateCategory.HYPERTENSION,
                drugs = listOf(
                    TemplateDrug("Lisinopril", "10mg", "1x daily", "Long term", "Monitor blood pressure"),
                    TemplateDrug("Amlodipine", "5mg", "1x daily", "Long term", "Take at same time daily")
                ),
                isDefault = true
            ),
            
            // Cardiovascular
            PrescriptionTemplate(
                id = "cardiac_protection",
                name = "Cardiac Protection",
                description = "Cardiovascular protection for high-risk patients",
                category = TemplateCategory.CARDIOVASCULAR,
                drugs = listOf(
                    TemplateDrug("Aspirin", "75mg", "1x daily", "Long term", "Take with food"),
                    TemplateDrug("Atorvastatin", "20mg", "1x evening", "Long term", "Monitor liver function"),
                    TemplateDrug("Metoprolol", "50mg", "2x daily", "Long term", "Monitor heart rate")
                ),
                isDefault = true
            ),
            
            // Respiratory
            PrescriptionTemplate(
                id = "asthma_control",
                name = "Asthma Control",
                description = "Standard asthma control medication",
                category = TemplateCategory.RESPIRATORY,
                drugs = listOf(
                    TemplateDrug("Fluticasone", "125mcg", "2 puffs 2x daily", "Long term", "Rinse mouth after use"),
                    TemplateDrug("Salbutamol", "100mcg", "As needed", "PRN", "For acute symptoms")
                ),
                isDefault = true
            ),
            
            // Pain Management
            PrescriptionTemplate(
                id = "chronic_pain",
                name = "Chronic Pain Management",
                description = "Non-opioid chronic pain management",
                category = TemplateCategory.PAIN_MANAGEMENT,
                drugs = listOf(
                    TemplateDrug("Ibuprofen", "400mg", "3x daily", "7-14 days", "Take with food"),
                    TemplateDrug("Paracetamol", "500mg", "4x daily", "As needed", "Max 4g daily"),
                    TemplateDrug("Gabapentin", "300mg", "3x daily", "Long term", "Gradual dose increase")
                ),
                isDefault = true
            ),
            
            // Antibiotic Treatment
            PrescriptionTemplate(
                id = "respiratory_infection",
                name = "Respiratory Infection",
                description = "Standard antibiotic treatment for respiratory infections",
                category = TemplateCategory.ANTIBIOTIC,
                drugs = listOf(
                    TemplateDrug("Amoxicillin", "500mg", "3x daily", "7 days", "Complete full course"),
                    TemplateDrug("Paracetamol", "500mg", "4x daily", "As needed", "For fever/pain")
                ),
                isDefault = true
            ),
            
            // Chronic Disease
            PrescriptionTemplate(
                id = "rheumatoid_arthritis",
                name = "Rheumatoid Arthritis",
                description = "Disease-modifying treatment for RA",
                category = TemplateCategory.CHRONIC_DISEASE,
                drugs = listOf(
                    TemplateDrug("Methotrexate", "15mg", "1x weekly", "Long term", "Take folic acid"),
                    TemplateDrug("Folic Acid", "5mg", "1x weekly", "Long term", "Day after methotrexate"),
                    TemplateDrug("Sulfasalazine", "1g", "2x daily", "Long term", "Monitor blood counts")
                ),
                isDefault = true
            ),
            
            // Preventive Care
            PrescriptionTemplate(
                id = "vitamin_prevention",
                name = "Preventive Vitamins",
                description = "Vitamin supplementation for prevention",
                category = TemplateCategory.PREVENTIVE,
                drugs = listOf(
                    TemplateDrug("Vitamin D3", "1000IU", "1x daily", "Long term", "Support bone health"),
                    TemplateDrug("Multivitamin", "1 tablet", "1x daily", "Long term", "Take with breakfast"),
                    TemplateDrug("Omega-3", "1000mg", "1x daily", "Long term", "Support heart health")
                ),
                isDefault = true
            )
        )
    }
}
