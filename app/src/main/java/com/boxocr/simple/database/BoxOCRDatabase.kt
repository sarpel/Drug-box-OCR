package com.boxocr.simple.database

import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Provider
import com.boxocr.simple.repository.TurkishDrugEntity

/**
 * Main Database class for Box OCR App - Phase 2 Feature
 * Provides offline drug storage and enhanced matching capabilities
 */
@Database(
    entities = [
        DrugEntity::class,
        ScanHistoryEntity::class,
        PrescriptionSessionEntity::class,
        DrugMatchingStatsEntity::class,
        TurkishDrugEntity::class,
        // Phase 1 Enhancement: Visual Drug Database
        DrugBoxImageEntity::class,
        DrugBoxFeatureEntity::class,
        VisualSimilarityMatchEntity::class,
        VisualCorrectionEntity::class
    ],
    version = 2, // Updated for Phase 1 Multi-Drug Enhancement
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BoxOCRDatabase : RoomDatabase() {
    
    abstract fun drugDao(): DrugDao
    abstract fun scanHistoryDao(): ScanHistoryDao
    abstract fun prescriptionSessionDao(): PrescriptionSessionDao
    abstract fun drugMatchingStatsDao(): DrugMatchingStatsDao
    abstract fun turkishDrugDao(): com.boxocr.simple.repository.TurkishDrugDao
    
    // Phase 1 Enhancement: Visual Drug Database DAOs
    abstract fun drugBoxImageDao(): DrugBoxImageDao
    abstract fun drugBoxFeatureDao(): DrugBoxFeatureDao
    abstract fun visualSimilarityMatchDao(): VisualSimilarityMatchDao
    abstract fun visualCorrectionDao(): VisualCorrectionDao
    
    companion object {
        const val DATABASE_NAME = "box_ocr_database"
    }
}

/**
 * Type converters for Room database
 */
class Converters {
    
    @TypeConverter
    fun fromDrugBoxCondition(condition: DrugBoxCondition): String {
        return condition.name
    }
    
    @TypeConverter
    fun toDrugBoxCondition(condition: String): DrugBoxCondition {
        return DrugBoxCondition.valueOf(condition)
    }
    
    @TypeConverter
    fun fromDrugBoxAngle(angle: DrugBoxAngle): String {
        return angle.name
    }
    
    @TypeConverter
    fun toDrugBoxAngle(angle: String): DrugBoxAngle {
        return DrugBoxAngle.valueOf(angle)
    }
    
    @TypeConverter
    fun fromDrugBoxLighting(lighting: DrugBoxLighting): String {
        return lighting.name
    }
    
    @TypeConverter
    fun toDrugBoxLighting(lighting: String): DrugBoxLighting {
        return DrugBoxLighting.valueOf(lighting)
    }
    
    @TypeConverter
    fun fromImageSource(source: ImageSource): String {
        return source.name
    }
    
    @TypeConverter
    fun toImageSource(source: String): ImageSource {
        return ImageSource.valueOf(source)
    }
    
    @TypeConverter
    fun fromVisualFeatureType(type: VisualFeatureType): String {
        return type.name
    }
    
    @TypeConverter
    fun toVisualFeatureType(type: String): VisualFeatureType {
        return VisualFeatureType.valueOf(type)
    }
    
    @TypeConverter
    fun fromCorrectionType(type: CorrectionType): String {
        return type.name
    }
    
    @TypeConverter
    fun toCorrectionType(type: String): CorrectionType {
        return CorrectionType.valueOf(type)
    }
}

/**
 * Database callback to populate with initial data
 */
class DatabaseCallback(
    private val database: Provider<BoxOCRDatabase>,
    private val applicationScope: CoroutineScope
) : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        
        // Populate database with initial data in background
        applicationScope.launch {
            populateDatabase()
        }
    }

    /**
     * Populate database with sample drug data
     */
    private suspend fun populateDatabase() {
        val drugDao = database.get().drugDao()
        
        // Add some common drugs for initial testing
        val initialDrugs = getInitialDrugs()
        drugDao.insertDrugs(initialDrugs)
    }

    /**
     * Get initial drug data for population
     */
    private fun getInitialDrugs(): List<DrugEntity> {
        return listOf(
            // Diabetes medications
            DrugEntity(
                name = "Metformin",
                genericName = "Metformin Hydrochloride",
                brandNames = """["Glucophage", "Fortamet", "Glumetza"]""",
                category = "Antidiabetic",
                strength = "500mg, 850mg, 1000mg",
                dosageForm = "Tablet",
                searchKeywords = "metformin glucophage diabetes blood sugar",
                activeIngredients = "Metformin Hydrochloride",
                isPrescriptionOnly = true
            ),
            
            DrugEntity(
                name = "Glimepiride",
                genericName = "Glimepiride",
                brandNames = """["Amaryl", "Glimepiride"]""",
                category = "Antidiabetic",
                strength = "1mg, 2mg, 4mg",
                dosageForm = "Tablet",
                searchKeywords = "glimepiride amaryl diabetes sulfonylurea",
                activeIngredients = "Glimepiride",
                isPrescriptionOnly = true
            ),
            
            // Hypertension medications
            DrugEntity(
                name = "Lisinopril",
                genericName = "Lisinopril",
                brandNames = """["Prinivil", "Zestril"]""",
                category = "ACE Inhibitor",
                strength = "5mg, 10mg, 20mg, 40mg",
                dosageForm = "Tablet",
                searchKeywords = "lisinopril prinivil zestril blood pressure ace inhibitor",
                activeIngredients = "Lisinopril",
                isPrescriptionOnly = true
            ),
            
            DrugEntity(
                name = "Amlodipine",
                genericName = "Amlodipine Besylate",
                brandNames = """["Norvasc", "Amlodipine"]""",
                category = "Calcium Channel Blocker",
                strength = "2.5mg, 5mg, 10mg",
                dosageForm = "Tablet",
                searchKeywords = "amlodipine norvasc blood pressure calcium channel blocker",
                activeIngredients = "Amlodipine Besylate",
                isPrescriptionOnly = true
            ),
            
            // Cardiovascular
            DrugEntity(
                name = "Atorvastatin",
                genericName = "Atorvastatin Calcium",
                brandNames = """["Lipitor", "Atorvastatin"]""",
                category = "Statin",
                strength = "10mg, 20mg, 40mg, 80mg",
                dosageForm = "Tablet",
                searchKeywords = "atorvastatin lipitor cholesterol statin",
                activeIngredients = "Atorvastatin Calcium",
                isPrescriptionOnly = true
            ),
            
            DrugEntity(
                name = "Aspirin",
                genericName = "Acetylsalicylic Acid",
                brandNames = """["Bayer", "Aspirin", "Ecotrin"]""",
                category = "Antiplatelet",
                strength = "81mg, 325mg",
                dosageForm = "Tablet",
                searchKeywords = "aspirin bayer acetylsalicylic acid cardio",
                activeIngredients = "Acetylsalicylic Acid",
                isPrescriptionOnly = false
            ),
            
            // Pain management
            DrugEntity(
                name = "Ibuprofen",
                genericName = "Ibuprofen",
                brandNames = """["Advil", "Motrin", "Brufen"]""",
                category = "NSAID",
                strength = "200mg, 400mg, 600mg",
                dosageForm = "Tablet",
                searchKeywords = "ibuprofen advil motrin pain inflammation nsaid",
                activeIngredients = "Ibuprofen",
                isPrescriptionOnly = false
            ),
            
            DrugEntity(
                name = "Paracetamol",
                genericName = "Acetaminophen",
                brandNames = """["Tylenol", "Panadol", "Calpol"]""",
                category = "Analgesic",
                strength = "500mg, 650mg, 1000mg",
                dosageForm = "Tablet",
                searchKeywords = "paracetamol acetaminophen tylenol panadol pain fever",
                activeIngredients = "Acetaminophen",
                isPrescriptionOnly = false
            ),
            
            // Antibiotics
            DrugEntity(
                name = "Amoxicillin",
                genericName = "Amoxicillin",
                brandNames = """["Amoxil", "Trimox", "Amoxicillin"]""",
                category = "Antibiotic",
                strength = "250mg, 500mg, 875mg",
                dosageForm = "Capsule",
                searchKeywords = "amoxicillin amoxil antibiotic penicillin infection",
                activeIngredients = "Amoxicillin",
                isPrescriptionOnly = true
            ),
            
            DrugEntity(
                name = "Azithromycin",
                genericName = "Azithromycin",
                brandNames = """["Zithromax", "Z-Pak", "Azithromycin"]""",
                category = "Antibiotic",
                strength = "250mg, 500mg",
                dosageForm = "Tablet",
                searchKeywords = "azithromycin zithromax z-pak antibiotic macrolide",
                activeIngredients = "Azithromycin",
                isPrescriptionOnly = true
            ),
            
            // Respiratory
            DrugEntity(
                name = "Salbutamol",
                genericName = "Salbutamol Sulfate",
                brandNames = """["Ventolin", "ProAir", "Albuterol"]""",
                category = "Bronchodilator",
                strength = "100mcg/puff",
                dosageForm = "Inhaler",
                searchKeywords = "salbutamol ventolin albuterol inhaler asthma bronchodilator",
                activeIngredients = "Salbutamol Sulfate",
                isPrescriptionOnly = true
            ),
            
            // Vitamins and supplements
            DrugEntity(
                name = "Vitamin D3",
                genericName = "Cholecalciferol",
                brandNames = """["Vitamin D3", "Cholecalciferol"]""",
                category = "Vitamin",
                strength = "1000IU, 2000IU, 5000IU",
                dosageForm = "Tablet",
                searchKeywords = "vitamin d3 cholecalciferol bone health calcium",
                activeIngredients = "Cholecalciferol",
                isPrescriptionOnly = false
            )
        )
    }
}

/**
 * Migration strategies for database schema changes
 */
object DatabaseMigrations {
    
    // Example migration from version 1 to 2 (when needed)
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add migration logic here when schema changes
            // Example:
            // database.execSQL("ALTER TABLE drugs ADD COLUMN new_field TEXT")
        }
    }
}
