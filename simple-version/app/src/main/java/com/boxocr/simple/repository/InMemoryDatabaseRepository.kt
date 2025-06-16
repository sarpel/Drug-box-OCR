package com.boxocr.simple.repository

import com.boxocr.simple.data.DatabaseItem
import com.boxocr.simple.data.MatchingResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InMemoryDatabaseRepository @Inject constructor() {
    
    private var databaseItems = mutableListOf<DatabaseItem>()
    
    suspend fun loadDatabaseFromText(content: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val items = content.lines()
                .filter { it.isNotBlank() }
                .map { line ->
                    DatabaseItem(
                        name = line.trim(),
                        originalLine = line
                    )
                }
            
            databaseItems.clear()
            databaseItems.addAll(items)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun findBestMatch(query: String, threshold: Float = 0.7f): MatchingResult = withContext(Dispatchers.Default) {
        if (databaseItems.isEmpty()) {
            return@withContext MatchingResult(
                bestMatch = null,
                confidence = 0f,
                allMatches = emptyList()
            )
        }
        
        val queryLower = query.lowercase().trim()
        val matches = databaseItems.map { item ->
            val similarity = calculateSimilarity(queryLower, item.name.lowercase())
            item.name to similarity
        }.sortedByDescending { it.second }
        
        val bestMatch = matches.firstOrNull()
        val passesThreshold = bestMatch?.second ?: 0f >= threshold
        
        MatchingResult(
            bestMatch = if (passesThreshold) bestMatch?.first else null,
            confidence = bestMatch?.second ?: 0f,
            allMatches = matches.take(5)
        )
    }
    
    private fun calculateSimilarity(text1: String, text2: String): Float {
        if (text1 == text2) return 1f
        if (text1.isEmpty() || text2.isEmpty()) return 0f
        
        // Exact substring match
        if (text1.contains(text2) || text2.contains(text1)) return 0.9f
        
        // Levenshtein distance similarity
        val maxLen = maxOf(text1.length, text2.length)
        val distance = levenshteinDistance(text1, text2)
        return (maxLen - distance).toFloat() / maxLen
    }
    
    private fun levenshteinDistance(str1: String, str2: String): Int {
        val dp = Array(str1.length + 1) { IntArray(str2.length + 1) }
        
        for (i in 0..str1.length) dp[i][0] = i
        for (j in 0..str2.length) dp[0][j] = j
        
        for (i in 1..str1.length) {
            for (j in 1..str2.length) {
                val cost = if (str1[i - 1] == str2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + cost
                )
            }
        }
        
        return dp[str1.length][str2.length]
    }
    
    fun getDatabaseSize(): Int = databaseItems.size
    
    fun getAllItems(): List<DatabaseItem> = databaseItems.toList()
}
