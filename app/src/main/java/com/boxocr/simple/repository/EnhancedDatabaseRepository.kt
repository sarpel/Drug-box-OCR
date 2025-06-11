package com.boxocr.simple.repository

import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

/**
 * Enhanced database matching with multiple algorithms and confidence scoring
 * Supports brand vs generic name matching and multiple match suggestions
 */
@Singleton
class EnhancedDatabaseRepository @Inject constructor() {
    
    // Drug categories for different matching strategies
    private val drugCategories = mapOf(
        "antibiotics" to listOf("amoxicillin", "azithromycin", "ciprofloxacin", "doxycycline"),
        "analgesics" to listOf("acetaminophen", "ibuprofen", "aspirin", "naproxen"),
        "diabetes" to listOf("metformin", "insulin", "glipizide", "glyburide"),
        "hypertension" to listOf("lisinopril", "amlodipine", "metoprolol", "hydrochlorothiazide"),
        "cholesterol" to listOf("atorvastatin", "simvastatin", "rosuvastatin"),
        "generic" to emptyList() // Default category
    )
    
    // Brand name to generic mappings
    private val brandToGeneric = mapOf(
        // Diabetes medications
        "glucophage" to "metformin",
        "lantus" to "insulin glargine",
        "humalog" to "insulin lispro",
        "novolog" to "insulin aspart",
        
        // Hypertension medications  
        "prinivil" to "lisinopril",
        "zestril" to "lisinopril",
        "norvasc" to "amlodipine",
        "lopressor" to "metoprolol",
        
        // Antibiotics
        "amoxil" to "amoxicillin",
        "augmentin" to "amoxicillin/clavulanate",
        "zithromax" to "azithromycin",
        "cipro" to "ciprofloxacin",
        
        // Analgesics
        "tylenol" to "acetaminophen",
        "advil" to "ibuprofen",
        "motrin" to "ibuprofen",
        "aleve" to "naproxen",
        
        // Cholesterol medications
        "lipitor" to "atorvastatin",
        "zocor" to "simvastatin",
        "crestor" to "rosuvastatin"
    )
    
    // Current database and settings
    private var databaseItems: List<String> = emptyList()
    private var defaultThreshold: Int = 80
    private var categoryThresholds: Map<String, Int> = mapOf(
        "antibiotics" to 85,
        "analgesics" to 75,
        "diabetes" to 90,
        "hypertension" to 85,
        "cholesterol" to 80,
        "generic" to 80
    )
    
    data class MatchResult(
        val drugName: String,
        val confidence: Int,
        val matchType: MatchType,
        val algorithm: String,
        val category: String,
        val isGenericMatch: Boolean = false,
        val brandName: String? = null
    )
    
    enum class MatchType {
        EXACT,           // 100% exact match
        HIGH_CONFIDENCE, // 90-99% confidence
        MEDIUM_CONFIDENCE, // 70-89% confidence  
        LOW_CONFIDENCE,  // 50-69% confidence
        NO_MATCH         // Below 50%
    }
    
    data class MultipleMatchResult(
        val primaryMatch: MatchResult?,
        val alternativeMatches: List<MatchResult>,
        val totalMatches: Int,
        val recommendedAction: RecommendedAction
    )
    
    enum class RecommendedAction {
        AUTO_SELECT,     // High confidence, auto-select primary
        SHOW_OPTIONS,    // Medium confidence, show alternatives
        MANUAL_ENTRY,    // Low confidence, suggest manual entry
        RESCAN          // Very low confidence, suggest rescan
    }
    
    /**
     * Load database items
     */
    fun loadDatabase(items: List<String>) {
        databaseItems = items.map { it.trim().lowercase() }
    }
    
    /**
     * Set similarity threshold for specific category
     */
    fun setCategoryThreshold(category: String, threshold: Int) {
        categoryThresholds = categoryThresholds.toMutableMap().apply {
            put(category, threshold.coerceIn(10, 100))
        }
    }
    
    /**
     * Set default similarity threshold
     */
    fun setDefaultThreshold(threshold: Int) {
        defaultThreshold = threshold.coerceIn(10, 100)
    }
    
    /**
     * Find multiple matches with different algorithms and confidence levels
     */
    fun findMultipleMatches(query: String): MultipleMatchResult {
        if (databaseItems.isEmpty()) {
            return MultipleMatchResult(null, emptyList(), 0, RecommendedAction.MANUAL_ENTRY)
        }
        
        val normalizedQuery = query.trim().lowercase()
        val category = detectDrugCategory(normalizedQuery)
        val threshold = categoryThresholds[category] ?: defaultThreshold
        
        // Try different matching strategies
        val matches = mutableListOf<MatchResult>()
        
        // 1. Exact matching
        matches.addAll(findExactMatches(normalizedQuery, category))
        
        // 2. Brand to generic matching
        matches.addAll(findBrandMatches(normalizedQuery, category))
        
        // 3. Fuzzy string matching (multiple algorithms)
        matches.addAll(findFuzzyMatches(normalizedQuery, category, threshold))
        
        // 4. Partial word matching
        matches.addAll(findPartialMatches(normalizedQuery, category, threshold))
        
        // Sort by confidence and remove duplicates
        val uniqueMatches = matches
            .distinctBy { it.drugName }
            .sortedByDescending { it.confidence }
        
        // Determine primary match and alternatives
        val primaryMatch = uniqueMatches.firstOrNull()
        val alternatives = uniqueMatches.drop(1).take(5) // Max 5 alternatives
        
        // Determine recommended action
        val action = when {
            primaryMatch == null -> RecommendedAction.RESCAN
            primaryMatch.confidence >= 90 -> RecommendedAction.AUTO_SELECT
            primaryMatch.confidence >= 70 -> RecommendedAction.SHOW_OPTIONS
            primaryMatch.confidence >= 50 -> RecommendedAction.MANUAL_ENTRY
            else -> RecommendedAction.RESCAN
        }
        
        return MultipleMatchResult(
            primaryMatch = primaryMatch,
            alternativeMatches = alternatives,
            totalMatches = uniqueMatches.size,
            recommendedAction = action
        )
    }
    
    /**
     * Find exact matches
     */
    private fun findExactMatches(query: String, category: String): List<MatchResult> {
        return databaseItems
            .filter { it == query }
            .map { 
                MatchResult(
                    drugName = it,
                    confidence = 100,
                    matchType = MatchType.EXACT,
                    algorithm = "exact",
                    category = category
                )
            }
    }
    
    /**
     * Find brand name to generic matches
     */
    private fun findBrandMatches(query: String, category: String): List<MatchResult> {
        val matches = mutableListOf<MatchResult>()
        
        // Direct brand lookup
        brandToGeneric[query]?.let { genericName ->
            databaseItems.filter { it.contains(genericName) }.forEach { dbItem ->
                matches.add(
                    MatchResult(
                        drugName = dbItem,
                        confidence = 95,
                        matchType = MatchType.HIGH_CONFIDENCE,
                        algorithm = "brand_to_generic",
                        category = category,
                        isGenericMatch = true,
                        brandName = query
                    )
                )
            }
        }
        
        // Partial brand matching
        brandToGeneric.entries.forEach { (brand, generic) ->
            if (query.contains(brand) || brand.contains(query)) {
                databaseItems.filter { it.contains(generic) }.forEach { dbItem ->
                    matches.add(
                        MatchResult(
                            drugName = dbItem,
                            confidence = 85,
                            matchType = MatchType.HIGH_CONFIDENCE,
                            algorithm = "partial_brand",
                            category = category,
                            isGenericMatch = true,
                            brandName = brand
                        )
                    )
                }
            }
        }
        
        return matches
    }
    
    /**
     * Find fuzzy matches using multiple algorithms
     */
    private fun findFuzzyMatches(query: String, category: String, threshold: Int): List<MatchResult> {
        val matches = mutableListOf<MatchResult>()
        
        databaseItems.forEach { dbItem ->
            // Jaccard similarity
            val jaccardScore = calculateJaccardSimilarity(query, dbItem)
            if (jaccardScore >= threshold) {
                matches.add(
                    MatchResult(
                        drugName = dbItem,
                        confidence = jaccardScore,
                        matchType = getMatchType(jaccardScore),
                        algorithm = "jaccard",
                        category = category
                    )
                )
            }
            
            // Levenshtein similarity
            val levenshteinScore = calculateLevenshteinSimilarity(query, dbItem)
            if (levenshteinScore >= threshold) {
                matches.add(
                    MatchResult(
                        drugName = dbItem,
                        confidence = levenshteinScore,
                        matchType = getMatchType(levenshteinScore),
                        algorithm = "levenshtein",
                        category = category
                    )
                )
            }
            
            // Soundex similarity (for phonetic matching)
            val soundexScore = calculateSoundexSimilarity(query, dbItem)
            if (soundexScore >= threshold) {
                matches.add(
                    MatchResult(
                        drugName = dbItem,
                        confidence = soundexScore,
                        matchType = getMatchType(soundexScore),
                        algorithm = "soundex",
                        category = category
                    )
                )
            }
        }
        
        return matches
    }
    
    /**
     * Find partial word matches
     */
    private fun findPartialMatches(query: String, category: String, threshold: Int): List<MatchResult> {
        val matches = mutableListOf<MatchResult>()
        val queryWords = query.split(" ", "-", "/").filter { it.length >= 3 }
        
        databaseItems.forEach { dbItem ->
            val dbWords = dbItem.split(" ", "-", "/")
            
            var totalScore = 0
            var matchedWords = 0
            
            queryWords.forEach { queryWord ->
                val bestWordMatch = dbWords.maxOfOrNull { dbWord ->
                    calculateLevenshteinSimilarity(queryWord, dbWord)
                } ?: 0
                
                if (bestWordMatch >= 70) {
                    totalScore += bestWordMatch
                    matchedWords++
                }
            }
            
            if (matchedWords > 0) {
                val avgScore = (totalScore / matchedWords).coerceAtMost(95) // Cap at 95% for partial
                
                if (avgScore >= threshold) {
                    matches.add(
                        MatchResult(
                            drugName = dbItem,
                            confidence = avgScore,
                            matchType = getMatchType(avgScore),
                            algorithm = "partial_word",
                            category = category
                        )
                    )
                }
            }
        }
        
        return matches
    }
    
    /**
     * Detect drug category based on query content
     */
    private fun detectDrugCategory(query: String): String {
        drugCategories.forEach { (category, keywords) ->
            if (keywords.any { keyword -> query.contains(keyword) }) {
                return category
            }
        }
        
        // Check brand names
        brandToGeneric.keys.forEach { brand ->
            if (query.contains(brand)) {
                // Find category of the generic equivalent
                val generic = brandToGeneric[brand] ?: ""
                drugCategories.forEach { (category, keywords) ->
                    if (keywords.any { keyword -> generic.contains(keyword) }) {
                        return category
                    }
                }
            }
        }
        
        return "generic"
    }
    
    /**
     * Get match type based on confidence score
     */
    private fun getMatchType(confidence: Int): MatchType {
        return when {
            confidence == 100 -> MatchType.EXACT
            confidence >= 90 -> MatchType.HIGH_CONFIDENCE
            confidence >= 70 -> MatchType.MEDIUM_CONFIDENCE
            confidence >= 50 -> MatchType.LOW_CONFIDENCE
            else -> MatchType.NO_MATCH
        }
    }
    
    /**
     * Calculate Jaccard similarity between two strings
     */
    private fun calculateJaccardSimilarity(str1: String, str2: String): Int {
        val set1 = str1.toCharArray().toSet()
        val set2 = str2.toCharArray().toSet()
        val intersection = set1.intersect(set2).size
        val union = set1.union(set2).size
        return if (union == 0) 0 else (intersection * 100) / union
    }
    
    /**
     * Calculate Levenshtein similarity between two strings
     */
    private fun calculateLevenshteinSimilarity(str1: String, str2: String): Int {
        val distance = levenshteinDistance(str1, str2)
        val maxLength = maxOf(str1.length, str2.length)
        return if (maxLength == 0) 100 else ((maxLength - distance) * 100) / maxLength
    }
    
    /**
     * Calculate Levenshtein distance
     */
    private fun levenshteinDistance(str1: String, str2: String): Int {
        val dp = Array(str1.length + 1) { IntArray(str2.length + 1) }
        
        for (i in 0..str1.length) dp[i][0] = i
        for (j in 0..str2.length) dp[0][j] = j
        
        for (i in 1..str1.length) {
            for (j in 1..str2.length) {
                if (str1[i - 1] == str2[j - 1]) {
                    dp[i][j] = dp[i - 1][j - 1]
                } else {
                    dp[i][j] = 1 + minOf(dp[i - 1][j], dp[i][j - 1], dp[i - 1][j - 1])
                }
            }
        }
        
        return dp[str1.length][str2.length]
    }
    
    /**
     * Calculate Soundex similarity (phonetic matching)
     */
    private fun calculateSoundexSimilarity(str1: String, str2: String): Int {
        val soundex1 = calculateSoundex(str1)
        val soundex2 = calculateSoundex(str2)
        return if (soundex1 == soundex2) 90 else 0
    }
    
    /**
     * Calculate Soundex code for phonetic matching
     */
    private fun calculateSoundex(str: String): String {
        if (str.isEmpty()) return "0000"
        
        val cleaned = str.uppercase().filter { it.isLetter() }
        if (cleaned.isEmpty()) return "0000"
        
        val soundexMap = mapOf(
            'B' to '1', 'F' to '1', 'P' to '1', 'V' to '1',
            'C' to '2', 'G' to '2', 'J' to '2', 'K' to '2', 'Q' to '2', 'S' to '2', 'X' to '2', 'Z' to '2',
            'D' to '3', 'T' to '3',
            'L' to '4',
            'M' to '5', 'N' to '5',
            'R' to '6'
        )
        
        val result = StringBuilder()
        result.append(cleaned[0])
        
        var prevCode = soundexMap[cleaned[0]] ?: '0'
        
        for (i in 1 until cleaned.length) {
            val code = soundexMap[cleaned[i]] ?: '0'
            if (code != '0' && code != prevCode) {
                result.append(code)
                if (result.length >= 4) break
            }
            prevCode = code
        }
        
        while (result.length < 4) {
            result.append('0')
        }
        
        return result.substring(0, 4)
    }
    
    /**
     * Get available drug categories
     */
    fun getDrugCategories(): List<String> = drugCategories.keys.toList()
    
    /**
     * Get category threshold
     */
    fun getCategoryThreshold(category: String): Int = 
        categoryThresholds[category] ?: defaultThreshold
    
    /**
     * Get brand name mappings for category
     */
    fun getBrandMappingsForCategory(category: String): Map<String, String> {
        return brandToGeneric.filter { (_, generic) ->
            drugCategories[category]?.any { keyword -> generic.contains(keyword) } == true
        }
    }
}
