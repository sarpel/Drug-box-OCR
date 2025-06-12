package com.boxocr.simple.data

/**
 * Prescription template data models - Phase 2 Feature
 * For common drug combinations and quick prescription creation
 */

/**
 * Prescription template for common drug combinations
 */
data class PrescriptionTemplate(
    val id: String,
    val name: String,
    val description: String,
    val category: TemplateCategory,
    val drugs: List<TemplateDrug>,
    val isDefault: Boolean = false,
    val usageCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Template categories for organizing prescriptions
 */
enum class TemplateCategory(val displayName: String) {
    DIABETES("Diabetes Management"),
    HYPERTENSION("Hypertension"),
    CARDIOVASCULAR("Cardiovascular"),
    RESPIRATORY("Respiratory"),
    PAIN_MANAGEMENT("Pain Management"),
    ANTIBIOTIC("Antibiotic Treatment"),
    CHRONIC_DISEASE("Chronic Disease"),
    PREVENTIVE("Preventive Care"),
    CUSTOM("Custom Template")
}

/**
 * Drug within a prescription template
 */
data class TemplateDrug(
    val name: String,
    val dosage: String? = null,
    val frequency: String? = null,
    val duration: String? = null,
    val instructions: String? = null,
    val isRequired: Boolean = true,
    val alternatives: List<String> = emptyList()
)

/**
 * Template usage statistics
 */
data class TemplateUsage(
    val templateId: String,
    val usageCount: Int,
    val lastUsed: Long,
    val averageTimeToComplete: Long = 0L
)
