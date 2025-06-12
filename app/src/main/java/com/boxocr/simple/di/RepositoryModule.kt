package com.boxocr.simple.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import com.boxocr.simple.database.BoxOCRDatabase
import com.boxocr.simple.database.DatabaseCallback
import com.boxocr.simple.repository.*
import javax.inject.Provider
import javax.inject.Singleton

/**
 * Repository Module - Database and repository bindings
 * Updated for Phase 1 Multi-Drug Enhancement
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    /**
     * Provide application scope for database operations
     */
    @Provides
    @Singleton
    fun provideApplicationScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
    
    /**
     * Provide database callback for initialization
     */
    @Provides
    @Singleton
    fun provideDatabaseCallback(
        database: Provider<BoxOCRDatabase>,
        applicationScope: CoroutineScope
    ): DatabaseCallback {
        return DatabaseCallback(database, applicationScope)
    }
    
    /**
     * Provide Room database instance
     */
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        callback: DatabaseCallback
    ): BoxOCRDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            BoxOCRDatabase::class.java,
            BoxOCRDatabase.DATABASE_NAME
        )
        .addCallback(callback)
        .build()
    }
    
    /**
     * Provide Drug DAO
     */
    @Provides
    fun provideDrugDao(database: BoxOCRDatabase) = database.drugDao()
    
    /**
     * Provide Scan History DAO
     */
    @Provides
    fun provideScanHistoryDao(database: BoxOCRDatabase) = database.scanHistoryDao()
    
    /**
     * Provide Prescription Session DAO
     */
    @Provides
    fun providePrescriptionSessionDao(database: BoxOCRDatabase) = database.prescriptionSessionDao()
    
    /**
     * Provide Drug Matching Stats DAO
     */
    @Provides
    fun provideDrugMatchingStatsDao(database: BoxOCRDatabase) = database.drugMatchingStatsDao()
    
    // Phase 1 Multi-Drug Enhancement - Visual Database DAOs
    
    /**
     * Provide Drug Box Image DAO
     */
    @Provides
    fun provideDrugBoxImageDao(database: BoxOCRDatabase) = database.drugBoxImageDao()
    
    /**
     * Provide Drug Box Feature DAO
     */
    @Provides
    fun provideDrugBoxFeatureDao(database: BoxOCRDatabase) = database.drugBoxFeatureDao()
    
    /**
     * Provide Visual Similarity Match DAO
     */
    @Provides
    fun provideVisualSimilarityMatchDao(database: BoxOCRDatabase) = database.visualSimilarityMatchDao()
    
    /**
     * Provide Visual Correction DAO
     */
    @Provides
    fun provideVisualCorrectionDao(database: BoxOCRDatabase) = database.visualCorrectionDao()
}
