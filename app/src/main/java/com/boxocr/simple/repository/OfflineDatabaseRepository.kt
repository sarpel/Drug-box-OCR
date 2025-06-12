package com.boxocr.simple.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import com.boxocr.simple.database.*
import com.boxocr.simple.data.MatchResult
import com.boxocr.simple.data.ScanResult
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max

/**
 * Offline Database Repository - Phase 2 Feature
 * Provides local SQLite storage for drugs and enhanced matching capabilities
 */
@Singleton
class OfflineDatabaseRepository @Inject constructor(
    private val database: BoxOCRDatabase
) {
    
    private val drugDao = database.drugDao()
    private val scanHistoryDao = database.scanHistoryDao()
    private val sessionDao = database.prescriptionSessionDao()
    private val statsDao = database.drugMatchingStatsDao()
    
    // Similarity algorithm implementations
    private val similarityCalculator = StringSimilarityCalculator()
    
    /**
     * Get all active drugs from local database
     */
    fun getAllDrugs(): Flow<List<DrugEntity>> = drugDao.getAllActiveDrugs()
    
    /**
     * Get popular drugs based on usage count
     */
    fun getPopularDrugs(limit: Int = 10): Flow<List<DrugEntity>> = drugDao.getPopularDrugs(limit)
    
    /**
     * Search drugs in local database
     */
    suspend fun searchDrugs(query: String): List<DrugEntity> {
        val searchPattern = "%${query.lowercase()}%"
        return drugDao.searchDrugs(searchPattern)
    }
    
    /**
     * Find best match for scanned text using offline database
     */
    suspend fun findBestMatch(scannedText: String, threshold: Float = 0.7f): MatchResult {
        if (scannedText.isBlank()) {
            return MatchResult(
                originalText = scannedText,
                bestMatch = null,
                confidence = 0f,
                allMatches = emptyList()
            )
        }
        
        val cleanedText = scannedText.trim().lowercase()
        val allDrugs = drugDao.getAllActiveDrugs().first()
        
        if (allDrugs.isEmpty()) {
            return MatchResult(
                originalText = scannedText,
                bestMatch = null,
                confidence = 0f,
                allMatches = emptyList()
            )
        }
        
        val matches = mutableListOf<Pair<String, Float>>()
        
        // Calculate similarity scores for each drug
        for (drug in allDrugs) {
            val scores = calculateDrugSimilarity(cleanedText, drug)
            val maxScore = scores.maxOrNull() ?: 0f
            
            if (maxScore >= threshold) {
                matches.add(drug.name to maxScore)
            }
        }
        
        // Sort by confidence score (descending)
        matches.sortByDescending { it.second }
        
        val bestMatch = matches.firstOrNull()
        val bestMatchName = bestMatch?.first
        val confidence = bestMatch?.second ?: 0f
        
        // Update usage statistics if we have a good match
        if (bestMatchName != null && confidence >= threshold) {
            updateDrugUsageStats(bestMatchName, cleanedText, confidence)
        }
        
        return MatchResult(
            originalText = scannedText,
            bestMatch = bestMatchName,
            confidence = confidence,
            allMatches = matches.take(5) // Top 5 matches
        )
    }
    
    /**
     * Calculate similarity scores between scanned text and drug entity
     */
    private fun calculateDrugSimilarity(scannedText: String, drug: DrugEntity): List<Float> {
        val scores = mutableListOf<Float>()
        
        // Compare with drug name
        scores.add(similarityCalculator.jaccardSimilarity(scannedText, drug.name.lowercase()))
        scores.add(similarityCalculator.levenshteinSimilarity(scannedText, drug.name.lowercase()))
        
        // Compare with generic name
        drug.genericName?.let { genericName ->
            scores.add(similarityCalculator.jaccardSimilarity(scannedText, genericName.lowercase()))
            scores.add(similarityCalculator.levenshteinSimilarity(scannedText, genericName.lowercase()))
        }
        
        // Compare with brand names
        drug.brandNames?.let { brandNamesJson ->
            // Parse JSON array of brand names (simplified)
            val brandNames = brandNamesJson.removeSurrounding("[\"", "\"]")
                .split("\", \"")
            
            for (brandName in brandNames) {
                scores.add(similarityCalculator.jaccardSimilarity(scannedText, brandName.lowercase()))
                scores.add(similarityCalculator.levenshteinSimilarity(scannedText, brandName.lowercase()))
            }
        }
        
        // Compare with search keywords
        drug.searchKeywords?.let { keywords ->
            val keywordList = keywords.split(" ")
            for (keyword in keywordList) {
                if (keyword.length > 3) { // Only check meaningful keywords
                    scores.add(similarityCalculator.jaccardSimilarity(scannedText, keyword.lowercase()))
                }
            }
        }
        
        return scores
    }
    
    /**
     * Update drug usage statistics
     */
    private suspend fun updateDrugUsageStats(drugName: String, ocrPattern: String, confidence: Float) {
        val drug = drugDao.getDrugByName(drugName)
        drug?.let { drugEntity ->
            // Increment drug usage count
            drugDao.incrementUsageCount(drugEntity.id)
            
            // Update or create matching statistics
            val timestamp = System.currentTimeMillis()
            val existingStats = statsDao.getStatsForDrug(drugEntity.id)
            val patternStats = existingStats.find { it.ocrPattern == ocrPattern }
            
            if (patternStats != null) {
                statsDao.updateStats(drugEntity.id, ocrPattern, confidence, timestamp)
            } else {
                statsDao.insertStats(
                    DrugMatchingStatsEntity(
                        drugId = drugEntity.id,
                        ocrPattern = ocrPattern,
                        matchCount = 1,
                        confidenceSum = confidence,
                        lastMatched = timestamp
                    )
                )
            }
        }
    }
    
    /**
     * Add or update drug in database
     */
    suspend fun addDrug(drug: DrugEntity): Long {
        return drugDao.insertDrug(drug)
    }
    
    /**
     * Import drugs from text file or CSV
     */
    suspend fun importDrugsFromText(content: String): Int {
        val lines = content.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
        val drugs = mutableListOf<DrugEntity>()
        
        for (line in lines) {
            // Simple text format: just drug names
            if (line.isNotBlank()) {
                val drug = DrugEntity(
                    name = line,
                    searchKeywords = line.lowercase().replace(" ", " "),
                    addedDate = System.currentTimeMillis()
                )
                drugs.add(drug)
            }
        }
        
        if (drugs.isNotEmpty()) {
            drugDao.insertDrugs(drugs)
        }
        
        return drugs.size
    }
    
    /**
     * Clear all drugs and reimport
     */
    suspend fun clearAndImportDrugs(content: String): Int {
        drugDao.deleteAllDrugs()
        return importDrugsFromText(content)
    }
    
    /**
     * Get drug database statistics
     */
    suspend fun getDatabaseStats(): DatabaseStats {
        val totalDrugs = drugDao.getActiveDrugsCount()
        val totalScans = scanHistoryDao.getScansCount()
        val recentScans = scanHistoryDao.getRecentScans(10).first()
        
        return DatabaseStats(
            totalDrugs = totalDrugs,
            totalScans = totalScans,
            recentScansCount = recentScans.size,
            lastScanDate = recentScans.firstOrNull()?.scannedDate
        )
    }
    
    /**
     * Save scan result to history
     */
    suspend fun saveScanResult(
        scannedText: String,
        matchedDrugName: String?,
        confidence: Float,
        sessionId: String? = null,
        imagePath: String? = null,
        ocrRawText: String? = null
    ): Long {
        val drug = matchedDrugName?.let { drugDao.getDrugByName(it) }
        
        val scanEntity = ScanHistoryEntity(
            scannedText = scannedText,
            matchedDrugName = matchedDrugName,
            drugId = drug?.id,
            confidenceScore = confidence,
            ocrRawText = ocrRawText,
            imagePath = imagePath,
            sessionId = sessionId,
            scannedDate = System.currentTimeMillis()
        )
        
        return scanHistoryDao.insertScan(scanEntity)
    }
    
    /**
     * Get recent scans as ScanResult objects
     */
    fun getRecentScans(limit: Int = 20): Flow<List<ScanResult>> {
        return scanHistoryDao.getRecentScans(limit).map { entities ->
            entities.map { entity ->
                ScanResult(
                    scannedText = entity.scannedText,
                    matchedText = entity.matchedDrugName,
                    timestamp = formatTimestamp(entity.scannedDate),
                    confidence = entity.confidenceScore
                )
            }
        }
    }
    
    /**
     * Clean up old data
     */
    suspend fun cleanupOldData(daysToKeep: Int = 30) {
        val cutoffDate = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L)
        
        // Delete old scans
        scanHistoryDao.deleteOldScans(cutoffDate)
        
        // Delete old matching statistics
        statsDao.deleteOldStats(cutoffDate)
    }
    
    /**
     * Format timestamp for display
     */
    private fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}

/**
 * Database statistics data class
 */
data class DatabaseStats(
    val totalDrugs: Int,
    val totalScans: Int,
    val recentScansCount: Int,
    val lastScanDate: Long?
)

/**
 * String similarity calculator for drug matching
 */
class StringSimilarityCalculator {
    
    /**
     * Calculate Jaccard similarity between two strings
     */
    fun jaccardSimilarity(str1: String, str2: String): Float {
        if (str1.isEmpty() && str2.isEmpty()) return 1.0f
        if (str1.isEmpty() || str2.isEmpty()) return 0.0f
        
        val set1 = str1.lowercase().toCharArray().toSet()
        val set2 = str2.lowercase().toCharArray().toSet()
        
        val intersection = set1.intersect(set2).size
        val union = set1.union(set2).size
        
        return if (union == 0) 0.0f else intersection.toFloat() / union.toFloat()
    }
    
    /**
     * Calculate Levenshtein similarity between two strings
     */
    fun levenshteinSimilarity(str1: String, str2: String): Float {
        val distance = levenshteinDistance(str1.lowercase(), str2.lowercase())
        val maxLength = max(str1.length, str2.length)
        
        return if (maxLength == 0) 1.0f else 1.0f - (distance.toFloat() / maxLength.toFloat())
    }
    
    /**
     * Calculate Levenshtein distance between two strings
     */
    private fun levenshteinDistance(str1: String, str2: String): Int {
        val len1 = str1.length
        val len2 = str2.length
        
        val dp = Array(len1 + 1) { IntArray(len2 + 1) }
        
        for (i in 0..len1) dp[i][0] = i
        for (j in 0..len2) dp[0][j] = j
        
        for (i in 1..len1) {
            for (j in 1..len2) {
                val cost = if (str1[i - 1] == str2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,      // deletion
                    dp[i][j - 1] + 1,      // insertion
                    dp[i - 1][j - 1] + cost // substitution
                )
            }
        }
        
        return dp[len1][len2]
    }
}
