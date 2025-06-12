package com.boxocr.simple.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.boxocr.simple.repository.*
import javax.inject.Singleton

/**
 * Multi-Drug Module - Phase 1 Enhancement Components
 * 
 * Provides dependency injection bindings for the new multi-drug detection,
 * visual database, and enhanced OCR components.
 */
@Module
@InstallIn(SingletonComponent::class)
object MultiDrugModule {
    
    /**
     * Provide Multi-Drug Object Detector
     */
    @Provides
    @Singleton
    fun provideMultiDrugObjectDetector(
        smartCameraManager: SmartCameraManager
    ): MultiDrugObjectDetector {
        return MultiDrugObjectDetector(
            smartCameraManager = smartCameraManager
        )
    }
    
    /**
     * Provide Multi-Region OCR Repository
     */
    @Provides
    @Singleton
    fun provideMultiRegionOCRRepository(
        ocrRepository: OCRRepository,
        turkishDrugDatabase: TurkishDrugDatabaseRepository,
        multiDrugObjectDetector: MultiDrugObjectDetector
    ): MultiRegionOCRRepository {
        return MultiRegionOCRRepository(
            ocrRepository = ocrRepository,
            turkishDrugDatabase = turkishDrugDatabase,
            multiDrugObjectDetector = multiDrugObjectDetector
        )
    }
    
    /**
     * Provide Visual Drug Database Repository
     */
    @Provides
    @Singleton
    fun provideVisualDrugDatabaseRepository(
        context: android.content.Context,
        database: BoxOCRDatabase
    ): VisualDrugDatabaseRepository {
        return VisualDrugDatabaseRepository(
            context = context,
            database = database
        )
    }
    
    /**
     * Provide Multi-Drug Scanner Repository (Main Orchestrator)
     */
    @Provides
    @Singleton
    fun provideMultiDrugScannerRepository(
        multiDrugObjectDetector: MultiDrugObjectDetector,
        multiRegionOCRRepository: MultiRegionOCRRepository,
        visualDrugDatabaseRepository: VisualDrugDatabaseRepository,
        batchScanningRepository: BatchScanningRepository,
        turkishDrugDatabaseRepository: TurkishDrugDatabaseRepository
    ): MultiDrugScannerRepository {
        return MultiDrugScannerRepository(
            multiDrugObjectDetector = multiDrugObjectDetector,
            multiRegionOCRRepository = multiRegionOCRRepository,
            visualDrugDatabaseRepository = visualDrugDatabaseRepository,
            batchScanningRepository = batchScanningRepository,
            turkishDrugDatabaseRepository = turkishDrugDatabaseRepository
        )
    }
    
    /**
     * Provide Damaged Text Recovery Repository
     */
    @Provides
    @Singleton
    fun provideDamagedTextRecoveryRepository(
        ocrRepository: OCRRepository,
        turkishDrugDatabase: TurkishDrugDatabaseRepository,
        visualDrugDatabaseRepository: VisualDrugDatabaseRepository
    ): DamagedTextRecoveryRepository {
        return DamagedTextRecoveryRepository(
            ocrRepository = ocrRepository,
            turkishDrugDatabase = turkishDrugDatabase,
            visualDrugDatabaseRepository = visualDrugDatabaseRepository
        )
    }
    
    /**
     * Provide Drug Box Image Database Manager - Phase 2 Week 4 Enhancement
     */
    @Provides
    @Singleton
    fun provideDrugBoxImageDatabaseManager(
        context: android.content.Context,
        database: BoxOCRDatabase,
        visualDrugDatabaseRepository: VisualDrugDatabaseRepository
    ): DrugBoxImageDatabaseManager {
        return DrugBoxImageDatabaseManager(
            context = context,
            database = database,
            visualRepository = visualDrugDatabaseRepository
        )
    }
}
