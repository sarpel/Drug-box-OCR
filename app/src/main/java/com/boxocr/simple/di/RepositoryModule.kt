package com.boxocr.simple.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Repository Module - All repositories are already provided via @Inject constructor
 * This module exists for any additional repository bindings if needed
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    // All repositories use @Inject constructor, so no explicit bindings needed
    // Add any interface bindings here if repositories implement interfaces
}
