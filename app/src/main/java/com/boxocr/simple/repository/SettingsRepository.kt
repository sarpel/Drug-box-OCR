package com.boxocr.simple.repository

import android.content.Context
import android.content.SharedPreferences
import com.boxocr.simple.data.AppSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SettingsRepository - Manages app settings with SharedPreferences
 */
@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "box_ocr_settings", 
        Context.MODE_PRIVATE
    )
    
    companion object {
        private const val KEY_API_KEY = "api_key"
        private const val KEY_SIMILARITY_THRESHOLD = "similarity_threshold"
        private const val KEY_AUTO_CLIPBOARD = "auto_clipboard"
    }
    
    fun getApiKey(): String {
        return prefs.getString(KEY_API_KEY, "") ?: ""
    }
    
    fun setApiKey(apiKey: String) {
        prefs.edit().putString(KEY_API_KEY, apiKey).apply()
    }
    
    fun getSimilarityThreshold(): Float {
        return prefs.getFloat(KEY_SIMILARITY_THRESHOLD, 0.7f)
    }
    
    fun setSimilarityThreshold(threshold: Float) {
        prefs.edit().putFloat(KEY_SIMILARITY_THRESHOLD, threshold).apply()
    }
    
    fun getAutoClipboard(): Boolean {
        return prefs.getBoolean(KEY_AUTO_CLIPBOARD, true)
    }
    
    fun setAutoClipboard(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_CLIPBOARD, enabled).apply()
    }
    
    fun getAppSettings(): AppSettings {
        return AppSettings(
            apiKey = getApiKey(),
            similarityThreshold = getSimilarityThreshold(),
            autoClipboard = getAutoClipboard()
        )
    }
    
    fun saveAppSettings(settings: AppSettings) {
        prefs.edit()
            .putString(KEY_API_KEY, settings.apiKey)
            .putFloat(KEY_SIMILARITY_THRESHOLD, settings.similarityThreshold)
            .putBoolean(KEY_AUTO_CLIPBOARD, settings.autoClipboard)
            .apply()
    }
}
