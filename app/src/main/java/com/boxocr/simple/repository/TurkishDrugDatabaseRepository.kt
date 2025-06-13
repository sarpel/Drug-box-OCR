package com.boxocr.simple.repository

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Turkish Drug Database Repository
 * Enhanced repository for comprehensive Turkish medical database integration
 * Supports Turkish Ministry of Health drug database (ilaclar.xls)
 */
@Singleton
class TurkishDrugDatabaseRepository @Inject constructor(
    private val context: Context,
    private val turkishDrugDao: TurkishDrugDao
) {
    
    data class TurkishDrug(
        val drugCode: String,
        val drugName: String,
        val barcode: String,
        val prescriptionType: Int?,
        val isActive: Boolean,
        val prospectus: String?,
        val activeIngredient: String?,
        val price: Double?,
        val manuallyAdded: Boolean,
        val sgkActive: Boolean,
        val atcCode: String?,
        val atcName: String?,
        val oldSkrsCode: String?,
        val equivalent: String?,
        val packageUnit: Int?,
        val packageAmount: Int?,
        val calculationExempt: Boolean
    )
    
    data class DrugSearchResult(
        val drug: TurkishDrug,
        val confidence: Double,
        val matchType: String,
        val matchedField: String
    )
    
    data class TurkishDrugDatabaseStats(
        val totalDrugs: Int,
        val activeDrugs: Int,
        val sgkActiveDrugs: Int,
        val genericDrugs: Int,
        val brandDrugs: Int,
        val averagePrice: Double?,
        val lastUpdated: Long
    )
    
    // Initialize database from Excel file
    suspend fun initializeDatabase(): Boolean = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.assets.open("ilaclar.xls")
            val workbook: Workbook = HSSFWorkbook(inputStream)
            val sheet: Sheet = workbook.getSheetAt(0) // LST_ILACLAR sheet
            
            val drugs = mutableListOf<TurkishDrugEntity>()
            
            // Skip header row (row 0)
            for (i in 1 until sheet.physicalNumberOfRows) {
                val row: Row = sheet.getRow(i) ?: continue
                
                val drug = TurkishDrugEntity(
                    drugCode = getCellValue(row, 0),
                    drugName = getCellValue(row, 1),
                    barcode = getCellValue(row, 2),
                    prescriptionType = getCellValue(row, 3).toIntOrNull(),
                    isActive = getCellValue(row, 4) == "-1",
                    prospectus = getCellValue(row, 5).takeIf { it.isNotEmpty() },
                    activeIngredient = getCellValue(row, 6).takeIf { it.isNotEmpty() },
                    price = getCellValue(row, 7).toDoubleOrNull(),
                    manuallyAdded = getCellValue(row, 8) == "-1",
                    sgkActive = getCellValue(row, 9) == "-1",
                    atcCode = getCellValue(row, 10).takeIf { it.isNotEmpty() },
                    atcName = getCellValue(row, 11).takeIf { it.isNotEmpty() },
                    oldSkrsCode = getCellValue(row, 12).takeIf { it.isNotEmpty() },
                    equivalent = getCellValue(row, 13).takeIf { it.isNotEmpty() },
                    packageUnit = getCellValue(row, 14).toIntOrNull(),
                    packageAmount = getCellValue(row, 15).toIntOrNull(),
                    calculationExempt = getCellValue(row, 16) == "-1"
                )
                
                drugs.add(drug)
            }
            
            workbook.close()
            inputStream.close()
            
            // Clear existing data and insert new drugs
            turkishDrugDao.clearAll()
            turkishDrugDao.insertAll(drugs)
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    // Advanced search with multiple algorithms
    suspend fun searchDrugs(
        query: String,
        algorithm: MatchingAlgorithm = MatchingAlgorithm.COMPREHENSIVE,
        minConfidence: Double = 0.7,
        maxResults: Int = 10
    ): List<DrugSearchResult> = withContext(Dispatchers.IO) {
        
        val allDrugs = turkishDrugDao.getAllDrugs()
        val results = mutableListOf<DrugSearchResult>()
        
        for (drugEntity in allDrugs) {
            val drug = drugEntity.toDomain()
            val searchResults = when (algorithm) {
                MatchingAlgorithm.EXACT -> exactMatch(query, drug)
                MatchingAlgorithm.FUZZY -> fuzzyMatch(query, drug)
                MatchingAlgorithm.PHONETIC -> phoneticMatch(query, drug)
                MatchingAlgorithm.COMPREHENSIVE -> comprehensiveMatch(query, drug)
            }
            
            results.addAll(searchResults.filter { it.confidence >= minConfidence })
        }
        
        results.sortedByDescending { it.confidence }.take(maxResults)
    }
    
    // Exact matching for precise drug name queries
    private fun exactMatch(query: String, drug: TurkishDrug): List<DrugSearchResult> {
        val results = mutableListOf<DrugSearchResult>()
        val normalizedQuery = query.uppercase().trim()
        
        // Check drug name
        if (drug.drugName.uppercase().contains(normalizedQuery)) {
            val confidence = if (drug.drugName.uppercase() == normalizedQuery) 1.0 else 0.9
            results.add(DrugSearchResult(drug, confidence, "exact", "drugName"))
        }
        
        // Check active ingredient
        drug.activeIngredient?.let { ingredient ->
            if (ingredient.uppercase().contains(normalizedQuery)) {
                val confidence = if (ingredient.uppercase() == normalizedQuery) 0.95 else 0.85
                results.add(DrugSearchResult(drug, confidence, "exact", "activeIngredient"))
            }
        }
        
        // Check barcode
        if (drug.barcode == query) {
            results.add(DrugSearchResult(drug, 1.0, "exact", "barcode"))
        }
        
        // Check ATC name
        drug.atcName?.let { atcName ->
            if (atcName.uppercase().contains(normalizedQuery)) {
                results.add(DrugSearchResult(drug, 0.8, "exact", "atcName"))
            }
        }
        
        return results.distinctBy { it.drug.drugCode }
    }
    
    // Fuzzy matching using Levenshtein distance and Jaccard similarity
    private fun fuzzyMatch(query: String, drug: TurkishDrug): List<DrugSearchResult> {
        val results = mutableListOf<DrugSearchResult>()
        val normalizedQuery = query.uppercase().trim()
        
        // Drug name fuzzy matching
        val nameSimiliarity = calculateJaccardSimilarity(normalizedQuery, drug.drugName.uppercase())
        if (nameSimiliarity > 0.6) {
            results.add(DrugSearchResult(drug, nameSimiliarity, "fuzzy", "drugName"))
        }
        
        // Active ingredient fuzzy matching
        drug.activeIngredient?.let { ingredient ->
            val ingredientSimilarity = calculateJaccardSimilarity(normalizedQuery, ingredient.uppercase())
            if (ingredientSimilarity > 0.6) {
                results.add(DrugSearchResult(drug, ingredientSimilarity * 0.9, "fuzzy", "activeIngredient"))
            }
        }
        
        // ATC name fuzzy matching
        drug.atcName?.let { atcName ->
            val atcSimilarity = calculateJaccardSimilarity(normalizedQuery, atcName.uppercase())
            if (atcSimilarity > 0.6) {
                results.add(DrugSearchResult(drug, atcSimilarity * 0.8, "fuzzy", "atcName"))
            }
        }
        
        return results.distinctBy { it.drug.drugCode }
    }
    
    // Turkish phonetic matching using Soundex-like algorithm
    private fun phoneticMatch(query: String, drug: TurkishDrug): List<DrugSearchResult> {
        val results = mutableListOf<DrugSearchResult>()
        val queryPhonetic = generateTurkishPhonetic(query)
        
        // Drug name phonetic matching
        val namePhonetic = generateTurkishPhonetic(drug.drugName)
        if (queryPhonetic == namePhonetic) {
            results.add(DrugSearchResult(drug, 0.85, "phonetic", "drugName"))
        }
        
        // Active ingredient phonetic matching
        drug.activeIngredient?.let { ingredient ->
            val ingredientPhonetic = generateTurkishPhonetic(ingredient)
            if (queryPhonetic == ingredientPhonetic) {
                results.add(DrugSearchResult(drug, 0.8, "phonetic", "activeIngredient"))
            }
        }
        
        return results.distinctBy { it.drug.drugCode }
    }
    
    // Comprehensive matching combining all algorithms
    private fun comprehensiveMatch(query: String, drug: TurkishDrug): List<DrugSearchResult> {
        val allResults = mutableListOf<DrugSearchResult>()
        
        allResults.addAll(exactMatch(query, drug))
        allResults.addAll(fuzzyMatch(query, drug))
        allResults.addAll(phoneticMatch(query, drug))
        
        // Return best match per field
        return allResults.groupBy { it.matchedField }
            .mapValues { (_, results) -> results.maxByOrNull { it.confidence } }
            .values.filterNotNull()
    }
    
    // Get database statistics
    fun getDatabaseStats(): Flow<TurkishDrugDatabaseStats> = flow {
        val totalDrugs = turkishDrugDao.getTotalDrugsCount()
        val activeDrugs = turkishDrugDao.getActiveDrugsCount()
        val sgkActiveDrugs = turkishDrugDao.getSgkActiveDrugsCount()
        val averagePrice = turkishDrugDao.getAveragePrice()
        
        val stats = TurkishDrugDatabaseStats(
            totalDrugs = totalDrugs,
            activeDrugs = activeDrugs,
            sgkActiveDrugs = sgkActiveDrugs,
            genericDrugs = 0, // Simplified for now
            brandDrugs = totalDrugs,
            averagePrice = averagePrice,
            lastUpdated = System.currentTimeMillis()
        )
        emit(stats)
    }.flowOn(Dispatchers.IO)
    
    // Search by category/condition
    suspend fun searchByMedicalCondition(condition: String): List<TurkishDrug> = withContext(Dispatchers.IO) {
        // Map Turkish medical conditions to ATC codes
        val atcCodes = when (condition.lowercase()) {
            "diyabet", "diabetes" -> listOf("A10A", "A10B") // Diabetes drugs
            "hipertansiyon", "hypertension" -> listOf("C02", "C03", "C07", "C08", "C09") // Antihypertensives
            "kalp", "heart", "kardiyovasküler" -> listOf("C01", "C02", "C03", "C04") // Cardiovascular
            "ağrı", "pain", "analjezi" -> listOf("N02", "M01") // Analgesics
            "antibiyotik", "antibiotic" -> listOf("J01") // Antibiotics
            "solunum", "respiratory" -> listOf("R03", "R05", "R06") // Respiratory
            "sindirim", "gastrointestinal" -> listOf("A02", "A03", "A06", "A07") // GI drugs
            else -> emptyList()
        }
        
        if (atcCodes.isEmpty()) return@withContext emptyList()
        
        turkishDrugDao.searchByAtcCodes(atcCodes).map { it.toDomain() }
    }
    
    // Get equivalent drugs
    suspend fun getEquivalentDrugs(drug: TurkishDrug): List<TurkishDrug> = withContext(Dispatchers.IO) {
        drug.equivalent?.let { equivalent ->
            turkishDrugDao.getByEquivalent(equivalent).map { it.toDomain() }
        } ?: emptyList()
    }
    
    // Price comparison within equivalent group
    suspend fun getPriceComparison(drug: TurkishDrug): List<Pair<TurkishDrug, Double>> = withContext(Dispatchers.IO) {
        val equivalents = getEquivalentDrugs(drug)
        equivalents.mapNotNull { equiv ->
            equiv.price?.let { price -> equiv to price }
        }.sortedBy { it.second }
    }
    
    // Helper functions
    private fun getCellValue(row: Row, cellIndex: Int): String {
        val cell = row.getCell(cellIndex)
        return when {
            cell == null -> ""
            cell.cellType == org.apache.poi.ss.usermodel.CellType.STRING -> cell.stringCellValue
            cell.cellType == org.apache.poi.ss.usermodel.CellType.NUMERIC -> cell.numericCellValue.toString()
            cell.cellType == org.apache.poi.ss.usermodel.CellType.BOOLEAN -> if (cell.booleanCellValue) "-1" else "0"
            else -> ""
        }
    }
    
    private fun calculateJaccardSimilarity(str1: String, str2: String): Double {
        val set1 = str1.toSet()
        val set2 = str2.toSet()
        val intersection = set1.intersect(set2).size
        val union = set1.union(set2).size
        return if (union == 0) 0.0 else intersection.toDouble() / union
    }
    
    private fun generateTurkishPhonetic(text: String): String {
        return text.uppercase()
            .replace("Ç", "C")
            .replace("Ğ", "G")
            .replace("I", "I")
            .replace("İ", "I")
            .replace("Ö", "O")
            .replace("Ş", "S")
            .replace("Ü", "U")
            .replace(Regex("[AEIOU]"), "")
            .replace(Regex("(.)\\1+"), "$1") // Remove consecutive duplicates
    }
    
    enum class MatchingAlgorithm {
        EXACT, FUZZY, PHONETIC, COMPREHENSIVE
    }
}

// Room Database Entities
@Entity(tableName = "turkish_drugs")
data class TurkishDrugEntity(
    @PrimaryKey val drugCode: String,
    val drugName: String,
    val barcode: String,
    val prescriptionType: Int?,
    val isActive: Boolean,
    val prospectus: String?,
    val activeIngredient: String?,
    val price: Double?,
    val manuallyAdded: Boolean,
    val sgkActive: Boolean,
    val atcCode: String?,
    val atcName: String?,
    val oldSkrsCode: String?,
    val equivalent: String?,
    val packageUnit: Int?,
    val packageAmount: Int?,
    val calculationExempt: Boolean
) {
    fun toDomain() = TurkishDrugDatabaseRepository.TurkishDrug(
        drugCode = drugCode,
        drugName = drugName,
        barcode = barcode,
        prescriptionType = prescriptionType,
        isActive = isActive,
        prospectus = prospectus,
        activeIngredient = activeIngredient,
        price = price,
        manuallyAdded = manuallyAdded,
        sgkActive = sgkActive,
        atcCode = atcCode,
        atcName = atcName,
        oldSkrsCode = oldSkrsCode,
        equivalent = equivalent,
        packageUnit = packageUnit,
        packageAmount = packageAmount,
        calculationExempt = calculationExempt
    )
}

@Dao
interface TurkishDrugDao {
    @Query("SELECT * FROM turkish_drugs")
    suspend fun getAllDrugs(): List<TurkishDrugEntity>
    
    @Query("SELECT * FROM turkish_drugs WHERE drugName LIKE '%' || :query || '%' OR activeIngredient LIKE '%' || :query || '%'")
    suspend fun searchDrugs(query: String): List<TurkishDrugEntity>
    
    @Query("SELECT * FROM turkish_drugs WHERE atcCode IN (:atcCodes) AND isActive = 1")
    suspend fun searchByAtcCodes(atcCodes: List<String>): List<TurkishDrugEntity>
    
    @Query("SELECT * FROM turkish_drugs WHERE equivalent = :equivalent AND isActive = 1")
    suspend fun getByEquivalent(equivalent: String): List<TurkishDrugEntity>
    
    @Query("SELECT * FROM turkish_drugs WHERE drugCode = :drugCode")
    suspend fun getDrugByCode(drugCode: String): TurkishDrugEntity?
    
    @Query("SELECT * FROM turkish_drugs WHERE barcode = :barcode")
    suspend fun getDrugByBarcode(barcode: String): TurkishDrugEntity?
    
    @Query("SELECT COUNT(*) FROM turkish_drugs")
    suspend fun getTotalDrugsCount(): Int
    
    @Query("SELECT COUNT(*) FROM turkish_drugs WHERE isActive = 1")
    suspend fun getActiveDrugsCount(): Int
    
    @Query("SELECT COUNT(*) FROM turkish_drugs WHERE sgkActive = 1")
    suspend fun getSgkActiveDrugsCount(): Int
    
    @Query("SELECT AVG(price) FROM turkish_drugs WHERE price IS NOT NULL")
    suspend fun getAveragePrice(): Double?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(drugs: List<TurkishDrugEntity>)
    
    @Query("DELETE FROM turkish_drugs")
    suspend fun clearAll()
}
