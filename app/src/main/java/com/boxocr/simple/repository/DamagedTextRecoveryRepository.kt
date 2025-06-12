package com.boxocr.simple.repository

import android.content.Context
import android.graphics.Bitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.min

/**
 * Damaged Text Recovery Repository - Phase 2 Week 3 Implementation
 * 
 * Advanced AI-powered text reconstruction system for damaged, partial, or 
 * corrupted drug box text using multiple recovery strategies.
 * 
 * Combines Turkish medical context, visual similarity, and pattern matching
 * for superior text recovery in challenging conditions.
 */
@Singleton
class DamagedTextRecoveryRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ocrRepository: OCRRepository,
    private val turkishDrugDatabase: TurkishDrugDatabaseRepository,
    private val visualDrugDatabaseRepository: VisualDrugDatabaseRepository
) {
    
    // Recovery state management
    private val _recoveryState = MutableStateFlow<TextRecoveryState>(
        TextRecoveryState.Idle
    )
    val recoveryState: StateFlow<TextRecoveryState> = _recoveryState.asStateFlow()
    
    /**
     * Main text recovery pipeline - tries multiple strategies
     */
    suspend fun recoverDamagedText(
        damagedBitmap: Bitmap,
        partialText: String,
        context: String = "",
        recoveryMode: RecoveryMode = RecoveryMode.COMPREHENSIVE
    ): DamagedTextRecoveryResult = withContext(Dispatchers.IO) {
        try {
            _recoveryState.value = TextRecoveryState.Processing("Hasarlı metin analiz ediliyor...")
            
            val startTime = System.currentTimeMillis()
            val recoveryStrategies = getRecoveryStrategies(recoveryMode)
            val allSuggestions = mutableListOf<TextSuggestion>()
            
            // Execute recovery strategies in order of priority
            for (strategy in recoveryStrategies) {
                _recoveryState.value = TextRecoveryState.Processing("${strategy.name} stratejisi uygulanıyor...")
                
                val suggestions = executeRecoveryStrategy(
                    strategy = strategy,
                    damagedBitmap = damagedBitmap,
                    partialText = partialText,
                    context = context
                )
                
                allSuggestions.addAll(suggestions)
                
                // Early exit if we have high-confidence results
                val bestSuggestion = suggestions.maxByOrNull { it.confidence }
                if (bestSuggestion != null && bestSuggestion.confidence > 0.9f) {
                    break
                }
            }
            
            // Rank and select best recovery result
            val finalResult = selectBestRecoveryResult(allSuggestions, partialText)
            val processingTime = System.currentTimeMillis() - startTime
            
            _recoveryState.value = TextRecoveryState.Completed(
                "Metin kurtarma tamamlandı: ${finalResult.method.name}"
            )
            
            finalResult.copy(processingTime = processingTime)
            
        } catch (e: Exception) {
            _recoveryState.value = TextRecoveryState.Error("Metin kurtarma hatası: ${e.message}")
            
            DamagedTextRecoveryResult(
                recoveredText = partialText,
                confidence = 0.1f,
                method = RecoveryMethod.ERROR,
                suggestions = emptyList(),
                errorMessage = e.message,
                processingTime = 0L
            )
        }
    }
    
    /**
     * Enhanced OCR with preprocessing for damaged text
     */
    suspend fun enhancedOCRRecovery(
        damagedBitmap: Bitmap,
        partialText: String
    ): List<TextSuggestion> {
        val suggestions = mutableListOf<TextSuggestion>()
        
        // Strategy 1: Image enhancement + OCR
        val enhancedBitmap = enhanceImageForOCR(damagedBitmap)
        val enhancedPrompt = createDamagedTextPrompt(partialText)
        
        val enhancedOCRResult = ocrRepository.processImageWithPrompt(
            bitmap = enhancedBitmap,
            customPrompt = enhancedPrompt
        )
        
        if (enhancedOCRResult.extractedText.isNotBlank()) {
            val similarity = calculateTextSimilarity(partialText, enhancedOCRResult.extractedText)
            suggestions.add(
                TextSuggestion(
                    text = enhancedOCRResult.cleanedText,
                    confidence = similarity * 0.8f, // Moderate confidence for enhanced OCR
                    source = "enhanced_ocr",
                    drugName = enhancedOCRResult.cleanedText,
                    metadata = mapOf(
                        "original_confidence" to enhancedOCRResult.confidence.toString(),
                        "processing_time" to enhancedOCRResult.processingTime.toString()
                    )
                )
            )
        }
        
        // Strategy 2: Multi-pass OCR with different preprocessing
        val preprocessingMethods = listOf("contrast", "sharpen", "denoise", "rotate")
        
        for (method in preprocessingMethods) {
            val preprocessedBitmap = applyPreprocessing(damagedBitmap, method)
            val ocrResult = ocrRepository.processImageWithPrompt(
                bitmap = preprocessedBitmap,
                customPrompt = enhancedPrompt
            )
            
            if (ocrResult.extractedText.isNotBlank()) {
                val similarity = calculateTextSimilarity(partialText, ocrResult.extractedText)
                if (similarity > 0.3f) {
                    suggestions.add(
                        TextSuggestion(
                            text = ocrResult.cleanedText,
                            confidence = similarity * 0.7f,
                            source = "preprocessed_ocr_$method",
                            drugName = ocrResult.cleanedText,
                            metadata = mapOf("preprocessing" to method)
                        )
                    )
                }
            }
        }
        
        return suggestions
    }
    
    /**
     * Pattern-based text completion using Turkish drug database
     */
    suspend fun patternBasedCompletion(partialText: String): List<TextSuggestion> {
        val suggestions = mutableListOf<TextSuggestion>()
        
        // Clean and normalize partial text
        val cleanPartial = cleanPartialText(partialText)
        
        if (cleanPartial.length < 2) {
            return suggestions // Too short to work with
        }
        
        // Strategy 1: Prefix matching
        val prefixMatches = turkishDrugDatabase.findDrugsByPrefix(cleanPartial)
        prefixMatches.forEach { drug ->
            val confidence = calculatePrefixMatchConfidence(cleanPartial, drug.name)
            if (confidence > 0.4f) {
                suggestions.add(
                    TextSuggestion(
                        text = drug.name,
                        confidence = confidence,
                        source = "prefix_match",
                        drugName = drug.name,
                        metadata = mapOf(
                            "atc_code" to (drug.atcCode ?: ""),
                            "brand" to (drug.brand ?: "")
                        )
                    )
                )
            }
        }
        
        // Strategy 2: Fuzzy matching with Turkish-specific rules
        val fuzzyMatches = turkishDrugDatabase.findDrugsByFuzzyMatch(cleanPartial)
        fuzzyMatches.forEach { match ->
            if (match.confidence > 0.3f) {
                suggestions.add(
                    TextSuggestion(
                        text = match.name,
                        confidence = match.confidence * 0.9f, // Slightly lower confidence for fuzzy
                        source = "fuzzy_match",
                        drugName = match.name,
                        metadata = mapOf(
                            "edit_distance" to calculateEditDistance(cleanPartial, match.name).toString(),
                            "match_type" to "fuzzy"
                        )
                    )
                )
            }
        }
        
        // Strategy 3: Phonetic matching for Turkish
        val phoneticMatches = turkishDrugDatabase.findDrugsByPhoneticMatch(cleanPartial)
        phoneticMatches.forEach { match ->
            suggestions.add(
                TextSuggestion(
                    text = match.name,
                    confidence = match.confidence * 0.8f, // Lower confidence for phonetic
                    source = "phonetic_match",
                    drugName = match.name,
                    metadata = mapOf("phonetic_algorithm" to "turkish_metaphone")
                )
            )
        }
        
        // Strategy 4: Partial word reconstruction
        val reconstructedSuggestions = reconstructPartialWords(cleanPartial)
        suggestions.addAll(reconstructedSuggestions)
        
        return suggestions.distinctBy { it.text }.sortedByDescending { it.confidence }
    }
    
    /**
     * Context-aware recovery using medical knowledge
     */
    suspend fun contextAwareRecovery(
        partialText: String,
        medicalContext: String
    ): List<TextSuggestion> {
        val suggestions = mutableListOf<TextSuggestion>()
        
        // Extract context clues
        val contextClues = extractMedicalContextClues(medicalContext)
        
        // Find drugs matching context
        val contextualDrugs = turkishDrugDatabase.findDrugsByMedicalContext(contextClues)
        
        for (drug in contextualDrugs) {
            val textSimilarity = calculateTextSimilarity(partialText, drug.name)
            val contextRelevance = calculateContextRelevance(drug, contextClues)
            val combinedConfidence = (textSimilarity * 0.6f) + (contextRelevance * 0.4f)
            
            if (combinedConfidence > 0.3f) {
                suggestions.add(
                    TextSuggestion(
                        text = drug.name,
                        confidence = combinedConfidence,
                        source = "context_aware",
                        drugName = drug.name,
                        metadata = mapOf(
                            "context_clues" to contextClues.joinToString(","),
                            "text_similarity" to textSimilarity.toString(),
                            "context_relevance" to contextRelevance.toString()
                        )
                    )
                )
            }
        }
        
        return suggestions.sortedByDescending { it.confidence }
    }
    
    /**
     * Machine learning-based character restoration
     */
    suspend fun mlCharacterRestoration(
        damagedBitmap: Bitmap,
        partialText: String
    ): List<TextSuggestion> {
        val suggestions = mutableListOf<TextSuggestion>()
        
        try {
            // Identify damaged/missing character positions
            val damageAnalysis = analyzeDamagePattern(damagedBitmap, partialText)
            
            // For each damaged position, predict likely characters
            val restoredVariants = mutableListOf<String>()
            
            for (damageInfo in damageAnalysis.damagedPositions) {
                val characterPredictions = predictMissingCharacter(
                    context = partialText,
                    position = damageInfo.position,
                    visualContext = damageInfo.visualClues
                )
                
                // Generate text variants with predicted characters
                characterPredictions.forEach { prediction ->
                    val restoredText = insertCharacterAtPosition(
                        partialText, 
                        damageInfo.position, 
                        prediction.character
                    )
                    
                    if (isValidTurkishDrugName(restoredText)) {
                        restoredVariants.add(restoredText)
                    }
                }
            }
            
            // Validate restored variants against drug database
            for (variant in restoredVariants) {
                val drugMatch = turkishDrugDatabase.findExactMatch(variant)
                if (drugMatch != null) {
                    suggestions.add(
                        TextSuggestion(
                            text = variant,
                            confidence = 0.85f, // High confidence for ML restoration + DB match
                            source = "ml_character_restoration",
                            drugName = variant,
                            metadata = mapOf(
                                "restoration_method" to "ml_character_prediction",
                                "damaged_positions" to damageAnalysis.damagedPositions.size.toString()
                            )
                        )
                    )
                }
            }
            
        } catch (e: Exception) {
            // ML restoration failed, return empty suggestions
        }
        
        return suggestions.sortedByDescending { it.confidence }
    }
    
    // Private helper methods
    
    private fun getRecoveryStrategies(mode: RecoveryMode): List<RecoveryStrategy> {
        return when (mode) {
            RecoveryMode.FAST -> listOf(
                RecoveryStrategy.PATTERN_COMPLETION,
                RecoveryStrategy.ENHANCED_OCR
            )
            RecoveryMode.BALANCED -> listOf(
                RecoveryStrategy.ENHANCED_OCR,
                RecoveryStrategy.PATTERN_COMPLETION,
                RecoveryStrategy.VISUAL_SIMILARITY,
                RecoveryStrategy.CONTEXT_AWARE
            )
            RecoveryMode.COMPREHENSIVE -> listOf(
                RecoveryStrategy.ENHANCED_OCR,
                RecoveryStrategy.PATTERN_COMPLETION,
                RecoveryStrategy.VISUAL_SIMILARITY,
                RecoveryStrategy.CONTEXT_AWARE,
                RecoveryStrategy.ML_CHARACTER_RESTORATION
            )
            RecoveryMode.VISUAL_ONLY -> listOf(
                RecoveryStrategy.VISUAL_SIMILARITY,
                RecoveryStrategy.ENHANCED_OCR
            )
        }
    }
    
    private suspend fun executeRecoveryStrategy(
        strategy: RecoveryStrategy,
        damagedBitmap: Bitmap,
        partialText: String,
        context: String
    ): List<TextSuggestion> {
        return when (strategy) {
            RecoveryStrategy.ENHANCED_OCR -> enhancedOCRRecovery(damagedBitmap, partialText)
            RecoveryStrategy.PATTERN_COMPLETION -> patternBasedCompletion(partialText)
            RecoveryStrategy.VISUAL_SIMILARITY -> {
                val visualResult = visualDrugDatabaseRepository.recoverDamagedText(
                    damagedBitmap, partialText, context
                )
                visualResult.suggestions
            }
            RecoveryStrategy.CONTEXT_AWARE -> contextAwareRecovery(partialText, context)
            RecoveryStrategy.ML_CHARACTER_RESTORATION -> mlCharacterRestoration(damagedBitmap, partialText)
        }
    }
    
    private fun selectBestRecoveryResult(
        allSuggestions: List<TextSuggestion>,
        originalPartialText: String
    ): DamagedTextRecoveryResult {
        if (allSuggestions.isEmpty()) {
            return DamagedTextRecoveryResult(
                recoveredText = originalPartialText,
                confidence = 0.1f,
                method = RecoveryMethod.FAILED,
                suggestions = emptyList()
            )
        }
        
        // Group suggestions by text and combine confidences
        val groupedSuggestions = allSuggestions
            .groupBy { it.text }
            .map { (text, suggestions) ->
                val combinedConfidence = combineConfidences(suggestions.map { it.confidence })
                val primarySuggestion = suggestions.maxByOrNull { it.confidence }!!
                
                primarySuggestion.copy(
                    confidence = combinedConfidence,
                    source = suggestions.joinToString("+") { it.source }
                )
            }
            .sortedByDescending { it.confidence }
        
        val bestSuggestion = groupedSuggestions.first()
        val method = determineRecoveryMethod(bestSuggestion.source, bestSuggestion.confidence)
        
        return DamagedTextRecoveryResult(
            recoveredText = bestSuggestion.text,
            confidence = bestSuggestion.confidence,
            method = method,
            suggestions = groupedSuggestions.take(5), // Top 5 suggestions
            processingTime = 0L // Will be set by caller
        )
    }
    
    // Utility methods (simplified implementations)
    
    private fun enhanceImageForOCR(bitmap: Bitmap): Bitmap {
        // Implement image enhancement (contrast, sharpening, noise reduction)
        return bitmap // Placeholder
    }
    
    private fun createDamagedTextPrompt(partialText: String): String {
        return """
        Bu görüntüde hasarlı veya kısmen görünen bir Türkçe ilaç ismi var.
        Kısmi metin: "$partialText"
        
        Lütfen:
        1. Hasarlı/eksik harfleri tahmin edin
        2. Türkçe ilaç isimleri kurallarını kullanın
        3. Tam ilaç ismini yeniden oluşturun
        4. Sadece en olası ilaç ismini döndürün
        
        Hasarlı metin kurtarma modu: AKTIF
        Türkçe medikal terminoloji: AKTIF
        """.trimIndent()
    }
    
    private fun applyPreprocessing(bitmap: Bitmap, method: String): Bitmap {
        // Implement various preprocessing methods
        return bitmap // Placeholder
    }
    
    private fun cleanPartialText(text: String): String {
        return text.trim()
            .replace(Regex("[^a-zA-ZçÇğĞıİöÖşŞüÜ\\s]"), "")
            .replace(Regex("\\s+"), " ")
    }
    
    private fun calculateTextSimilarity(text1: String, text2: String): Float {
        val maxLength = maxOf(text1.length, text2.length)
        if (maxLength == 0) return 1f
        
        val distance = calculateEditDistance(text1, text2)
        return 1f - (distance.toFloat() / maxLength)
    }
    
    private fun calculateEditDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
        
        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j
        
        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                dp[i][j] = if (s1[i - 1] == s2[j - 1]) {
                    dp[i - 1][j - 1]
                } else {
                    1 + minOf(dp[i - 1][j], dp[i][j - 1], dp[i - 1][j - 1])
                }
            }
        }
        
        return dp[s1.length][s2.length]
    }
    
    private fun calculatePrefixMatchConfidence(prefix: String, fullText: String): Float {
        if (prefix.isEmpty() || fullText.isEmpty()) return 0f
        if (!fullText.lowercase().startsWith(prefix.lowercase())) return 0f
        
        return prefix.length.toFloat() / fullText.length.toFloat()
    }
    
    private fun reconstructPartialWords(partialText: String): List<TextSuggestion> {
        // Implement word reconstruction logic
        return emptyList() // Placeholder
    }
    
    private fun extractMedicalContextClues(context: String): List<String> {
        // Extract medical context information
        return emptyList() // Placeholder
    }
    
    private fun calculateContextRelevance(drug: DrugMatch, contextClues: List<String>): Float {
        // Calculate how relevant the drug is to the medical context
        return 0.5f // Placeholder
    }
    
    private fun analyzeDamagePattern(bitmap: Bitmap, partialText: String): DamageAnalysis {
        // Analyze the damage pattern in the image
        return DamageAnalysis(emptyList()) // Placeholder
    }
    
    private fun predictMissingCharacter(
        context: String,
        position: Int,
        visualClues: List<String>
    ): List<CharacterPrediction> {
        // Predict missing characters using ML
        return emptyList() // Placeholder
    }
    
    private fun insertCharacterAtPosition(text: String, position: Int, character: Char): String {
        return if (position <= text.length) {
            text.substring(0, position) + character + text.substring(position)
        } else {
            text + character
        }
    }
    
    private fun isValidTurkishDrugName(text: String): Boolean {
        // Validate if the text could be a valid Turkish drug name
        return text.length >= 3 && text.matches(Regex("[a-zA-ZçÇğĞıİöÖşŞüÜ\\s]+"))
    }
    
    private fun combineConfidences(confidences: List<Float>): Float {
        // Combine multiple confidence scores
        return confidences.maxOrNull() ?: 0f
    }
    
    private fun determineRecoveryMethod(source: String, confidence: Float): RecoveryMethod {
        return when {
            source.contains("visual") -> RecoveryMethod.VISUAL_SIMILARITY
            source.contains("pattern") -> RecoveryMethod.PARTIAL_COMPLETION
            source.contains("ml") -> RecoveryMethod.ML_RESTORATION
            source.contains("context") -> RecoveryMethod.CONTEXT_AWARE
            else -> RecoveryMethod.ENHANCED_OCR
        }
    }
}

// Data classes and enums

enum class RecoveryMode {
    FAST,           // Quick recovery with basic strategies
    BALANCED,       // Good balance of speed and accuracy
    COMPREHENSIVE,  // All strategies for maximum accuracy
    VISUAL_ONLY     // Only visual-based strategies
}

enum class RecoveryStrategy {
    ENHANCED_OCR,
    PATTERN_COMPLETION,
    VISUAL_SIMILARITY,
    CONTEXT_AWARE,
    ML_CHARACTER_RESTORATION
}

enum class RecoveryMethod {
    ENHANCED_OCR,
    VISUAL_SIMILARITY,
    PARTIAL_COMPLETION,
    CONTEXT_AWARE,
    ML_RESTORATION,
    HYBRID_APPROACH,
    NO_VISUAL_MATCH,
    FAILED,
    ERROR
}

data class DamageAnalysis(
    val damagedPositions: List<DamageInfo>
)

data class DamageInfo(
    val position: Int,
    val visualClues: List<String>,
    val damageType: String
)

data class CharacterPrediction(
    val character: Char,
    val confidence: Float
)

sealed class TextRecoveryState {
    object Idle : TextRecoveryState()
    data class Processing(val message: String) : TextRecoveryState()
    data class Completed(val message: String) : TextRecoveryState()
    data class Error(val message: String) : TextRecoveryState()
}
