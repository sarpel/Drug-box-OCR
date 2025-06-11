package com.boxocr.simple

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Simple Box OCR Application
 * Minimal application class with Hilt dependency injection
 */
@HiltAndroidApp
class BoxOCRApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Initialize any necessary components
    }
}
