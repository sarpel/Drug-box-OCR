package com.boxocr.simple.di

import android.content.Context
import com.boxocr.simple.ai.AdvancedAIModelsRepository
import com.boxocr.simple.ai.CustomAIIntegrationRepository
import com.boxocr.simple.iot.SmartMedicalDeviceRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 🎯 PRODUCTION FEATURES DEPENDENCY INJECTION MODULE
 * 
 * Enhanced dependency injection for production-ready features:
 * - Advanced AI Models Integration (GPT-4, Claude, Gemini)
 * - IoT Smart Medical Device Integration
 * - Custom and Local AI Model Management
 * - Multi-provider AI consensus systems
 * 
 * Medical-grade dependency injection with Turkish healthcare compliance
 */
@Module
@InstallIn(SingletonComponent::class)
object ProductionFeaturesModule {

    /**
     * 🧠 Advanced AI Models Repository
     * Provides multi-model AI integration for medical consultations
     */
    @Provides
    @Singleton
    fun provideAdvancedAIModelsRepository(
        @ApplicationContext context: Context
    ): AdvancedAIModelsRepository {
        return AdvancedAIModelsRepository(context)
    }

    /**
     * 🏥 Smart Medical Device Repository  
     * Provides IoT integration for Turkish medical devices
     */
    @Provides
    @Singleton
    fun provideSmartMedicalDeviceRepository(
        @ApplicationContext context: Context
    ): SmartMedicalDeviceRepository {
        return SmartMedicalDeviceRepository(context)
    }

    /**
     * 🔧 Custom AI Integration Repository
     * Provides custom and local AI model management
     */
    @Provides
    @Singleton
    fun provideCustomAIIntegrationRepository(
        @ApplicationContext context: Context
    ): CustomAIIntegrationRepository {
        return CustomAIIntegrationRepository(context)
    }
}