package com.boxocr.simple.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Visual Drug Database Entities - Phase 1 Foundation Enhancement
 * 
 * Stores drug box images and visual features for multi-drug detection
 * and damaged text recovery scenarios.
 */

/**
 * Stores drug box images with metadata
 */
@Entity(
    tableName = "drug_box_images",
    indices = [
        Index(value = ["drugName"]),
        Index(value = ["condition"]),
        Index(value = ["createdAt"])
    ]
)
data class DrugBoxImageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "drug_name")
    val drugName: String,
    
    @ColumnInfo(name = "brand_name")
    val brandName: String = "",
    
    @ColumnInfo(name = "image_path")
    val imagePath: String, // Local file path
    
    @ColumnInfo(name = "image_hash")
    val imageHash: String, // For duplicate detection
    
    @ColumnInfo(name = "condition")
    val condition: DrugBoxCondition = DrugBoxCondition.PERFECT,
    
    @ColumnInfo(name = "angle")
    val angle: DrugBoxAngle = DrugBoxAngle.FRONT,
    
    @ColumnInfo(name = "lighting")
    val lighting: DrugBoxLighting = DrugBoxLighting.NORMAL,
    
    @ColumnInfo(name = "width")
    val width: Int,
    
    @ColumnInfo(name = "height")
    val height: Int,
    
    @ColumnInfo(name = "file_size")
    val fileSize: Long,
    
    @ColumnInfo(name = "source")
    val source: ImageSource = ImageSource.USER_UPLOAD,
    
    @ColumnInfo(name = "verified")
    val verified: Boolean = false,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Stores visual features extracted from drug box images
 */
@Entity(
    tableName = "drug_box_features",
    foreignKeys = [
        ForeignKey(
            entity = DrugBoxImageEntity::class,
            parentColumns = ["id"],
            childColumns = ["image_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["image_id"])]
)
data class DrugBoxFeatureEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "image_id")
    val imageId: Long,
    
    @ColumnInfo(name = "feature_type")
    val featureType: VisualFeatureType,
    
    @ColumnInfo(name = "feature_data")
    val featureData: String, // JSON serialized feature data
    
    @ColumnInfo(name = "feature_vector")
    val featureVector: String, // Serialized feature vector for similarity
    
    @ColumnInfo(name = "extraction_method")
    val extractionMethod: String,
    
    @ColumnInfo(name = "confidence")
    val confidence: Float,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Stores similarity matches between images
 */
@Entity(
    tableName = "visual_similarity_matches",
    foreignKeys = [
        ForeignKey(
            entity = DrugBoxImageEntity::class,
            parentColumns = ["id"],
            childColumns = ["query_image_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DrugBoxImageEntity::class,
            parentColumns = ["id"],
            childColumns = ["match_image_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["query_image_id"]),
        Index(value = ["match_image_id"]),
        Index(value = ["similarity_score"])
    ]
)
data class VisualSimilarityMatchEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "query_image_id")
    val queryImageId: Long,
    
    @ColumnInfo(name = "match_image_id")
    val matchImageId: Long,
    
    @ColumnInfo(name = "similarity_score")
    val similarityScore: Float,
    
    @ColumnInfo(name = "matching_algorithm")
    val matchingAlgorithm: String,
    
    @ColumnInfo(name = "feature_weights")
    val featureWeights: String, // JSON weights for different features
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Stores user corrections for improving visual matching
 */
@Entity(
    tableName = "visual_corrections",
    foreignKeys = [
        ForeignKey(
            entity = DrugBoxImageEntity::class,
            parentColumns = ["id"],
            childColumns = ["image_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["image_id"])]
)
data class VisualCorrectionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "image_id")
    val imageId: Long,
    
    @ColumnInfo(name = "original_text")
    val originalText: String,
    
    @ColumnInfo(name = "corrected_text")
    val correctedText: String,
    
    @ColumnInfo(name = "correction_type")
    val correctionType: CorrectionType,
    
    @ColumnInfo(name = "user_feedback")
    val userFeedback: String = "",
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Enums for drug box image classification
 */
enum class DrugBoxCondition {
    PERFECT,        // Clean, undamaged box
    SLIGHTLY_WORN,  // Minor wear, all text readable
    DAMAGED,        // Some damage, partial text readable
    HEAVILY_DAMAGED // Significant damage, hard to read
}

enum class DrugBoxAngle {
    FRONT,          // Front view of box
    SIDE,           // Side view
    TOP,            // Top view
    BOTTOM,         // Bottom view
    ANGLED          // Angled view
}

enum class DrugBoxLighting {
    PERFECT,        // Ideal lighting
    NORMAL,         // Standard lighting
    DIM,            // Low light
    BRIGHT,         // Overexposed
    UNEVEN          // Uneven shadows/highlights
}

enum class ImageSource {
    USER_UPLOAD,    // User provided image
    CAMERA_CAPTURE, // Captured via app camera
    GALLERY_IMPORT, // Imported from gallery
    BATCH_UPLOAD    // Bulk upload
}

enum class VisualFeatureType {
    SIFT_FEATURES,      // SIFT keypoints and descriptors
    COLOR_HISTOGRAM,    // Color distribution
    TEXT_LAYOUT,        // Text positioning and layout
    EDGE_FEATURES,      // Edge detection features
    SHAPE_FEATURES,     // Shape and contour features
    TEXTURE_FEATURES    // Texture analysis features
}

enum class CorrectionType {
    OCR_ERROR,          // OCR misread text
    PARTIAL_TEXT,       // Missing/incomplete text
    WRONG_DRUG_MATCH,   // Matched to wrong drug
    VISUAL_SIMILARITY   // Visual similarity issue
}

/**
 * DAOs for visual drug database
 */
@Dao
interface DrugBoxImageDao {
    
    @Query("SELECT * FROM drug_box_images ORDER BY created_at DESC")
    fun getAllImages(): Flow<List<DrugBoxImageEntity>>
    
    @Query("SELECT * FROM drug_box_images WHERE drug_name = :drugName")
    suspend fun getImagesByDrugName(drugName: String): List<DrugBoxImageEntity>
    
    @Query("SELECT * FROM drug_box_images WHERE condition = :condition")
    suspend fun getImagesByCondition(condition: DrugBoxCondition): List<DrugBoxImageEntity>
    
    @Query("SELECT * FROM drug_box_images WHERE image_hash = :hash")
    suspend fun getImageByHash(hash: String): DrugBoxImageEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: DrugBoxImageEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImages(images: List<DrugBoxImageEntity>): List<Long>
    
    @Update
    suspend fun updateImage(image: DrugBoxImageEntity)
    
    @Delete
    suspend fun deleteImage(image: DrugBoxImageEntity)
    
    @Query("DELETE FROM drug_box_images WHERE id = :id")
    suspend fun deleteImageById(id: Long)
    
    @Query("SELECT COUNT(*) FROM drug_box_images")
    suspend fun getImageCount(): Int
    
    @Query("SELECT COUNT(*) FROM drug_box_images WHERE drug_name = :drugName")
    suspend fun getImageCountByDrug(drugName: String): Int
}

@Dao
interface DrugBoxFeatureDao {
    
    @Query("SELECT * FROM drug_box_features WHERE image_id = :imageId")
    suspend fun getFeaturesByImageId(imageId: Long): List<DrugBoxFeatureEntity>
    
    @Query("SELECT * FROM drug_box_features WHERE feature_type = :type")
    suspend fun getFeaturesByType(type: VisualFeatureType): List<DrugBoxFeatureEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeature(feature: DrugBoxFeatureEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeatures(features: List<DrugBoxFeatureEntity>): List<Long>
    
    @Update
    suspend fun updateFeature(feature: DrugBoxFeatureEntity)
    
    @Delete
    suspend fun deleteFeature(feature: DrugBoxFeatureEntity)
    
    @Query("DELETE FROM drug_box_features WHERE image_id = :imageId")
    suspend fun deleteFeaturesByImageId(imageId: Long)
}

@Dao
interface VisualSimilarityMatchDao {
    
    @Query("""
        SELECT * FROM visual_similarity_matches 
        WHERE query_image_id = :queryImageId 
        ORDER BY similarity_score DESC
        LIMIT :limit
    """)
    suspend fun getSimilarImages(queryImageId: Long, limit: Int = 10): List<VisualSimilarityMatchEntity>
    
    @Query("""
        SELECT * FROM visual_similarity_matches 
        WHERE similarity_score >= :minScore 
        ORDER BY similarity_score DESC
    """)
    suspend fun getHighSimilarityMatches(minScore: Float): List<VisualSimilarityMatchEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: VisualSimilarityMatchEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatches(matches: List<VisualSimilarityMatchEntity>): List<Long>
    
    @Query("DELETE FROM visual_similarity_matches WHERE query_image_id = :imageId OR match_image_id = :imageId")
    suspend fun deleteMatchesForImage(imageId: Long)
}

@Dao
interface VisualCorrectionDao {
    
    @Query("SELECT * FROM visual_corrections WHERE image_id = :imageId")
    suspend fun getCorrectionsByImageId(imageId: Long): List<VisualCorrectionEntity>
    
    @Query("SELECT * FROM visual_corrections WHERE correction_type = :type")
    suspend fun getCorrectionsByType(type: CorrectionType): List<VisualCorrectionEntity>
    
    @Query("SELECT * FROM visual_corrections ORDER BY created_at DESC LIMIT :limit")
    suspend fun getRecentCorrections(limit: Int = 50): List<VisualCorrectionEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCorrection(correction: VisualCorrectionEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCorrections(corrections: List<VisualCorrectionEntity>): List<Long>
    
    @Update
    suspend fun updateCorrection(correction: VisualCorrectionEntity)
    
    @Delete
    suspend fun deleteCorrection(correction: VisualCorrectionEntity)
}

/**
 * Data classes for repository layer
 */
data class DrugBoxImageWithFeatures(
    @Embedded val image: DrugBoxImageEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "image_id"
    )
    val features: List<DrugBoxFeatureEntity>
)

data class VisualSimilarityResult(
    val queryImageId: Long,
    val similarImages: List<SimilarImage>,
    val processingTime: Long,
    val algorithm: String
)

data class SimilarImage(
    val imageEntity: DrugBoxImageEntity,
    val similarityScore: Float,
    val matchingFeatures: List<String>
)
