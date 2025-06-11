package com.boxocr.simple.repository

import android.content.Context
import android.net.Uri
import com.boxocr.simple.data.MatchResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

/**
 * InMemoryDatabaseRepository - Manages database items and matching
 */
@Singleton
class InMemoryDatabaseRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository
) {
    
    private val _databaseItems = MutableStateFlow<List<String>>(emptyList())
    val databaseItems: StateFlow<List<String>> = _databaseItems.asStateFlow()
    
    private val _databaseError = MutableStateFlow<String?>(null)
    val databaseError: StateFlow<String?> = _databaseError.asStateFlow()
    
    /**
     * Load database from file URI
     */
    suspend fun loadDatabase(uri: Uri) {
        try {
            _databaseError.value = null
            
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    val items = mutableListOf<String>()
                    
                    reader.lineSequence()
                        .filter { it.isNotBlank() }
                        .map { it.trim() }
                        .forEach { line ->
                            items.add(line)
                        }
                    
                    _databaseItems.value = items
                }
            }
        } catch (e: Exception) {
            _databaseError.value = "Failed to load database: ${e.message}"
        }
    }
    
    /**
     * Get current database items
     */
    fun getDatabaseItems(): List<String> = _databaseItems.value
    
    /**
     * Find best match for extracted text
     */
    fun findBestMatch(extractedText: String): MatchResult {
        val items = _databaseItems.value
        val threshold = settingsRepository.getSimilarityThreshold()
        
        if (items.isEmpty()) {
            return MatchResult(
                originalText = extractedText,
                bestMatch = null,
                confidence = 0f
            )
        }
        
        val cleanedText = extractedText.lowercase().trim()
        
        // Calculate similarity for each database item
        val matches = items.map { item ->
            val similarity = calculateSimilarity(cleanedText, item.lowercase().trim())
            item to similarity
        }.sortedByDescending { it.second }
        
        val bestMatch = matches.first()
        
        return if (bestMatch.second >= threshold) {
            MatchResult(
                originalText = extractedText,
                bestMatch = bestMatch.first,
                confidence = bestMatch.second,
                allMatches = matches.take(5)
            )
        } else {
            MatchResult(
                originalText = extractedText,
                bestMatch = null,
                confidence = bestMatch.second,
                allMatches = matches.take(5)
            )
        }
    }
    
    /**
     * Calculate similarity between two strings
     */
    private fun calculateSimilarity(text1: String, text2: String): Float {
        if (text1 == text2) return 1.0f
        if (text1.isEmpty() || text2.isEmpty()) return 0.0f
        
        // Exact substring matching (highest priority)
        if (text1.contains(text2) || text2.contains(text1)) {
            return 0.9f
        }
        
        // Word-based Jaccard similarity
        val jaccardSimilarity = calculateJaccardSimilarity(text1, text2)
        
        // Levenshtein distance similarity
        val levenshteinSimilarity = calculateLevenshteinSimilarity(text1, text2)
        
        // Return the maximum similarity
        return maxOf(jaccardSimilarity, levenshteinSimilarity)
    }
    
    private fun calculateJaccardSimilarity(text1: String, text2: String): Float {
        val words1 = text1.split(Regex("\\s+")).filter { it.isNotBlank() }.toSet()
        val words2 = text2.split(Regex("\\s+")).filter { it.isNotBlank() }.toSet()
        
        val intersection = words1.intersect(words2).size
        val union = words1.union(words2).size
        
        return if (union > 0) intersection.toFloat() / union.toFloat() else 0.0f
    }
    
    private fun calculateLevenshteinSimilarity(text1: String, text2: String): Float {
        val distance = levenshteinDistance(text1, text2)
        val maxLength = max(text1.length, text2.length)
        return if (maxLength > 0) 1.0f - (distance.toFloat() / maxLength.toFloat()) else 1.0f
    }
    
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
}
