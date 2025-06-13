package com.boxocr.simple.di

import android.content.Context
import com.boxocr.simple.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 🧠 PHASE 5: AI INTELLIGENCE DEPENDENCY INJECTION MODULE
 * 
 * Provides all AI Intelligence components for the revolutionary Turkish medical AI system.
 * Manages the complete AI ecosystem including vision, conversation, prediction, and analysis.
 */
@Module
@InstallIn(SingletonComponent::class)
object AIIntelligenceModule {
    
    @Provides
    @Singleton
    fun provideTurkishDrugVisionRepository(
        @ApplicationContext context: Context
    ): TurkishDrugVisionRepository {
        return TurkishDrugVisionRepository(context)
    }
    
    @Provides
    @Singleton
    fun provideMedicalAIAssistantRepository(
        @ApplicationContext context: Context
    ): MedicalAIAssistantRepository {
        return MedicalAIAssistantRepository(context)
    }
    
    @Provides
    @Singleton
    fun providePredictiveIntelligenceRepository(): PredictiveIntelligenceRepository {
        return PredictiveIntelligenceRepository()
    }
    
    @Provides
    @Singleton
    fun provideAdvancedAnalysisRepository(): AdvancedAnalysisRepository {
        return AdvancedAnalysisRepository()
    }
    
    @Provides
    @Singleton
    fun provideAIIntelligenceManager(
        @ApplicationContext context: Context,
        turkishDrugVision: TurkishDrugVisionRepository,
        medicalAIAssistant: MedicalAIAssistantRepository,
        predictiveIntelligence: PredictiveIntelligenceRepository,
        advancedAnalysis: AdvancedAnalysisRepository
    ): AIIntelligenceManager {
        return AIIntelligenceManager(
            context = context,
            turkishDrugVision = turkishDrugVision,
            medicalAIAssistant = medicalAIAssistant,
            predictiveIntelligence = predictiveIntelligence,
            advancedAnalysis = advancedAnalysis
        )
    }
}
