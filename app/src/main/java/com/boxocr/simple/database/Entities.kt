package com.boxocr.simple.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Drug entity for offline database - Phase 2 Feature
 * Stores drug information locally for faster matching and offline usage
 */
@Entity(
    tableName = "drugs",
    indices = [
        Index(value = ["name"], unique = true),
        Index(value = ["category"]),
        Index(value = ["search_keywords"])
    ]
)
data class DrugEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "generic_name")
    val genericName: String? = null,
    
    @ColumnInfo(name = "brand_names")
    val brandNames: String? = null, // JSON array of brand names
    
    @ColumnInfo(name = "category")
    val category: String? = null,
    
    @ColumnInfo(name = "strength")
    val strength: String? = null,
    
    @ColumnInfo(name = "dosage_form")
    val dosageForm: String? = null, // tablet, capsule, syrup, etc.
    
    @ColumnInfo(name = "manufacturer")
    val manufacturer: String? = null,
    
    @ColumnInfo(name = "search_keywords")
    val searchKeywords: String? = null, // Space-separated keywords for search
    
    @ColumnInfo(name = "active_ingredients")
    val activeIngredients: String? = null,
    
    @ColumnInfo(name = "is_prescription_only")
    val isPrescriptionOnly: Boolean = true,
    
    @ColumnInfo(name = "is_controlled_substance")
    val isControlledSubstance: Boolean = false,
    
    @ColumnInfo(name = "added_date")
    val addedDate: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "last_updated")
    val lastUpdated: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "usage_count")
    val usageCount: Int = 0,
    
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true
)

/**
 * Scan history entity for offline storage
 */
@Entity(
    tableName = "scan_history",
    indices = [
        Index(value = ["scanned_date"]),
        Index(value = ["drug_id"]),
        Index(value = ["session_id"])
    ]
)
data class ScanHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "scanned_text")
    val scannedText: String,
    
    @ColumnInfo(name = "matched_drug_name")
    val matchedDrugName: String?,
    
    @ColumnInfo(name = "drug_id")
    val drugId: Long?,
    
    @ColumnInfo(name = "confidence_score")
    val confidenceScore: Float,
    
    @ColumnInfo(name = "ocr_raw_text")
    val ocrRawText: String? = null,
    
    @ColumnInfo(name = "image_path")
    val imagePath: String? = null,
    
    @ColumnInfo(name = "session_id")
    val sessionId: String? = null,
    
    @ColumnInfo(name = "scanned_date")
    val scannedDate: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "verification_status")
    val verificationStatus: String = "PENDING", // PENDING, CONFIRMED, REJECTED, EDITED
    
    @ColumnInfo(name = "final_drug_name")
    val finalDrugName: String? = null
)

/**
 * Prescription session entity for batch scanning
 */
@Entity(
    tableName = "prescription_sessions",
    indices = [
        Index(value = ["session_id"], unique = true),
        Index(value = ["created_date"]),
        Index(value = ["patient_id"])
    ]
)
data class PrescriptionSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "session_id")
    val sessionId: String,
    
    @ColumnInfo(name = "patient_id")
    val patientId: String? = null,
    
    @ColumnInfo(name = "patient_name")
    val patientName: String? = null,
    
    @ColumnInfo(name = "doctor_name")
    val doctorName: String? = null,
    
    @ColumnInfo(name = "clinic_name")
    val clinicName: String? = null,
    
    @ColumnInfo(name = "session_status")
    val sessionStatus: String = "ACTIVE", // ACTIVE, COMPLETED, CANCELLED
    
    @ColumnInfo(name = "drugs_count")
    val drugsCount: Int = 0,
    
    @ColumnInfo(name = "created_date")
    val createdDate: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "completed_date")
    val completedDate: Long? = null,
    
    @ColumnInfo(name = "windows_automation_sent")
    val windowsAutomationSent: Boolean = false,
    
    @ColumnInfo(name = "notes")
    val notes: String? = null
)

/**
 * Drug matching statistics for improving accuracy
 */
@Entity(
    tableName = "drug_matching_stats",
    indices = [
        Index(value = ["drug_id"]),
        Index(value = ["ocr_pattern"])
    ]
)
data class DrugMatchingStatsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "drug_id")
    val drugId: Long,
    
    @ColumnInfo(name = "ocr_pattern")
    val ocrPattern: String, // Common OCR patterns for this drug
    
    @ColumnInfo(name = "match_count")
    val matchCount: Int = 1,
    
    @ColumnInfo(name = "confidence_sum")
    val confidenceSum: Float = 0f,
    
    @ColumnInfo(name = "last_matched")
    val lastMatched: Long = System.currentTimeMillis()
)

/**
 * Data Access Object for drugs
 */
@Dao
interface DrugDao {
    @Query("SELECT * FROM drugs WHERE is_active = 1 ORDER BY name ASC")
    fun getAllActiveDrugs(): Flow<List<DrugEntity>>
    
    @Query("SELECT * FROM drugs WHERE is_active = 1 ORDER BY usage_count DESC LIMIT :limit")
    fun getPopularDrugs(limit: Int): Flow<List<DrugEntity>>
    
    @Query("SELECT * FROM drugs WHERE name LIKE :query OR generic_name LIKE :query OR brand_names LIKE :query OR search_keywords LIKE :query")
    suspend fun searchDrugs(query: String): List<DrugEntity>
    
    @Query("SELECT * FROM drugs WHERE category = :category AND is_active = 1 ORDER BY name ASC")
    suspend fun getDrugsByCategory(category: String): List<DrugEntity>
    
    @Query("SELECT * FROM drugs WHERE id = :drugId")
    suspend fun getDrugById(drugId: Long): DrugEntity?
    
    @Query("SELECT * FROM drugs WHERE name = :name AND is_active = 1")
    suspend fun getDrugByName(name: String): DrugEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrug(drug: DrugEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrugs(drugs: List<DrugEntity>)
    
    @Update
    suspend fun updateDrug(drug: DrugEntity)
    
    @Query("UPDATE drugs SET usage_count = usage_count + 1 WHERE id = :drugId")
    suspend fun incrementUsageCount(drugId: Long)
    
    @Query("DELETE FROM drugs WHERE id = :drugId")
    suspend fun deleteDrug(drugId: Long)
    
    @Query("DELETE FROM drugs")
    suspend fun deleteAllDrugs()
    
    @Query("SELECT COUNT(*) FROM drugs WHERE is_active = 1")
    suspend fun getActiveDrugsCount(): Int
}

/**
 * Data Access Object for scan history
 */
@Dao
interface ScanHistoryDao {
    @Query("SELECT * FROM scan_history ORDER BY scanned_date DESC LIMIT :limit")
    fun getRecentScans(limit: Int): Flow<List<ScanHistoryEntity>>
    
    @Query("SELECT * FROM scan_history WHERE session_id = :sessionId ORDER BY scanned_date ASC")
    suspend fun getSessionScans(sessionId: String): List<ScanHistoryEntity>
    
    @Query("SELECT * FROM scan_history WHERE scanned_date >= :fromDate ORDER BY scanned_date DESC")
    suspend fun getScansFromDate(fromDate: Long): List<ScanHistoryEntity>
    
    @Insert
    suspend fun insertScan(scan: ScanHistoryEntity): Long
    
    @Update
    suspend fun updateScan(scan: ScanHistoryEntity)
    
    @Query("DELETE FROM scan_history WHERE id = :scanId")
    suspend fun deleteScan(scanId: Long)
    
    @Query("DELETE FROM scan_history WHERE scanned_date < :cutoffDate")
    suspend fun deleteOldScans(cutoffDate: Long)
    
    @Query("SELECT COUNT(*) FROM scan_history")
    suspend fun getScansCount(): Int
}

/**
 * Data Access Object for prescription sessions
 */
@Dao
interface PrescriptionSessionDao {
    @Query("SELECT * FROM prescription_sessions ORDER BY created_date DESC")
    fun getAllSessions(): Flow<List<PrescriptionSessionEntity>>
    
    @Query("SELECT * FROM prescription_sessions WHERE session_status = :status ORDER BY created_date DESC")
    fun getSessionsByStatus(status: String): Flow<List<PrescriptionSessionEntity>>
    
    @Query("SELECT * FROM prescription_sessions WHERE session_id = :sessionId")
    suspend fun getSessionById(sessionId: String): PrescriptionSessionEntity?
    
    @Query("SELECT * FROM prescription_sessions WHERE session_status = 'ACTIVE' ORDER BY created_date DESC LIMIT 1")
    suspend fun getActiveSession(): PrescriptionSessionEntity?
    
    @Insert
    suspend fun insertSession(session: PrescriptionSessionEntity): Long
    
    @Update
    suspend fun updateSession(session: PrescriptionSessionEntity)
    
    @Query("UPDATE prescription_sessions SET session_status = :status, completed_date = :completedDate WHERE session_id = :sessionId")
    suspend fun updateSessionStatus(sessionId: String, status: String, completedDate: Long?)
    
    @Query("UPDATE prescription_sessions SET drugs_count = :drugsCount WHERE session_id = :sessionId")
    suspend fun updateDrugsCount(sessionId: String, drugsCount: Int)
    
    @Query("DELETE FROM prescription_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: Long)
}

/**
 * Data Access Object for drug matching statistics
 */
@Dao
interface DrugMatchingStatsDao {
    @Query("SELECT * FROM drug_matching_stats WHERE drug_id = :drugId ORDER BY match_count DESC")
    suspend fun getStatsForDrug(drugId: Long): List<DrugMatchingStatsEntity>
    
    @Query("SELECT * FROM drug_matching_stats WHERE ocr_pattern LIKE :pattern ORDER BY match_count DESC LIMIT 10")
    suspend fun findSimilarPatterns(pattern: String): List<DrugMatchingStatsEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStats(stats: DrugMatchingStatsEntity)
    
    @Query("UPDATE drug_matching_stats SET match_count = match_count + 1, confidence_sum = confidence_sum + :confidence, last_matched = :timestamp WHERE drug_id = :drugId AND ocr_pattern = :pattern")
    suspend fun updateStats(drugId: Long, pattern: String, confidence: Float, timestamp: Long)
    
    @Query("DELETE FROM drug_matching_stats WHERE last_matched < :cutoffDate")
    suspend fun deleteOldStats(cutoffDate: Long)
}
