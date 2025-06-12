package com.boxocr.simple.repository

import android.content.Context
import com.boxocr.simple.data.WindowsIntegrationSettings
import com.boxocr.simple.data.AutomationResult
import com.boxocr.simple.data.WindowDetectionResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enhanced Windows Integration Repository - Phase 3 Quality of Life Feature
 * 
 * Features:
 * 1. Auto-detect Windows app window focus
 * 2. Smart paste timing with field activation detection
 * 3. Custom keystroke sequences beyond F4
 * 4. Multiple prescription software support
 * 5. Advanced timing and reliability features
 */
@Singleton
class EnhancedWindowsIntegrationRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()
    
    private val _integrationState = MutableStateFlow(WindowsIntegrationState())
    val integrationState: StateFlow<WindowsIntegrationState> = _integrationState.asStateFlow()
    
    private var currentSettings = WindowsIntegrationSettings()
    
    /**
     * Test Windows client connection and capabilities
     */
    suspend fun testConnection(androidServerIp: String): Boolean {
        return try {
            val request = Request.Builder()
                .url("http://$androidServerIp:8080/windows/test")
                .get()
                .build()
            
            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Detect active prescription software on Windows
     */
    suspend fun detectPrescriptionSoftware(androidServerIp: String): List<PrescriptionSoftware> {
        return try {
            val request = Request.Builder()
                .url("http://$androidServerIp:8080/windows/detect-software")
                .get()
                .build()
            
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: ""
                parseSoftwareDetectionResponse(responseBody)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Configure automation settings for detected software
     */
    suspend fun configureAutomation(
        androidServerIp: String, 
        software: PrescriptionSoftware,
        settings: WindowsIntegrationSettings
    ): Boolean {
        return try {
            val jsonBody = JSONObject().apply {
                put("software_id", software.id)
                put("settings", JSONObject().apply {
                    put("paste_delay", settings.pasteDelay)
                    put("field_detection", settings.enableFieldDetection)
                    put("custom_keystrokes", JSONArray(settings.customKeystrokes))
                    put("timing_profile", settings.timingProfile.name)
                    put("retry_attempts", settings.retryAttempts)
                    put("smart_detection", settings.enableSmartDetection)
                })
            }
            
            val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("http://$androidServerIp:8080/windows/configure")
                .post(requestBody)
                .build()
            
            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Execute enhanced prescription automation with smart timing
     */
    suspend fun executeSmartAutomation(
        androidServerIp: String,
        drugs: List<String>,
        settings: WindowsIntegrationSettings
    ): AutomationResult {
        return try {
            _integrationState.value = _integrationState.value.copy(isExecuting = true)
            
            // Step 1: Verify window focus
            val windowCheck = checkWindowFocus(androidServerIp)
            if (!windowCheck.isValid) {
                return AutomationResult.Error("Target window not detected: ${windowCheck.error}")
            }
            
            // Step 2: Execute field detection if enabled
            val fieldDetection = if (settings.enableFieldDetection) {
                detectActiveField(androidServerIp)
            } else null
            
            // Step 3: Execute paste sequence with smart timing
            val pasteResult = executePasteSequence(androidServerIp, drugs, settings, fieldDetection)
            
            // Step 4: Execute custom keystrokes
            val keystrokeResult = executeCustomKeystrokes(androidServerIp, settings.customKeystrokes)
            
            _integrationState.value = _integrationState.value.copy(
                isExecuting = false,
                lastResult = pasteResult
            )
            
            pasteResult
        } catch (e: Exception) {
            _integrationState.value = _integrationState.value.copy(
                isExecuting = false,
                lastResult = AutomationResult.Error("Automation failed: ${e.message}")
            )
            AutomationResult.Error("Automation failed: ${e.message}")
        }
    }
    
    /**
     * Check if target window is focused and ready
     */
    private suspend fun checkWindowFocus(androidServerIp: String): WindowDetectionResult {
        return try {
            val request = Request.Builder()
                .url("http://$androidServerIp:8080/windows/check-focus")
                .get()
                .build()
            
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: ""
                parseWindowDetectionResponse(responseBody)
            } else {
                WindowDetectionResult(false, "Failed to check window focus")
            }
        } catch (e: Exception) {
            WindowDetectionResult(false, "Connection error: ${e.message}")
        }
    }
    
    /**
     * Detect active input field for smart pasting
     */
    private suspend fun detectActiveField(androidServerIp: String): FieldDetectionResult? {
        return try {
            val request = Request.Builder()
                .url("http://$androidServerIp:8080/windows/detect-field")
                .get()
                .build()
            
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: ""
                parseFieldDetectionResponse(responseBody)
            } else null
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Execute paste sequence with intelligent timing
     */
    private suspend fun executePasteSequence(
        androidServerIp: String,
        drugs: List<String>,
        settings: WindowsIntegrationSettings,
        fieldDetection: FieldDetectionResult?
    ): AutomationResult {
        val results = mutableListOf<DrugPasteResult>()
        
        for ((index, drug) in drugs.withIndex()) {
            try {
                // Wait for field readiness if detection is available
                if (fieldDetection?.isTextInput == true) {
                    waitForFieldReadiness(androidServerIp, settings)
                }
                
                // Execute paste with timing profile
                val pasteResult = pasteDrugWithTiming(androidServerIp, drug, settings, index)
                results.add(pasteResult)
                
                // Wait between drugs based on timing profile
                val delayMs = calculateInterDrugDelay(settings.timingProfile, index, drugs.size)
                delay(delayMs)
                
            } catch (e: Exception) {
                results.add(DrugPasteResult(drug, false, "Error: ${e.message}"))
            }
        }
        
        val successCount = results.count { it.success }
        return if (successCount == drugs.size) {
            AutomationResult.Success("All ${drugs.size} drugs pasted successfully")
        } else {
            AutomationResult.Partial(
                successCount = successCount,
                totalCount = drugs.size,
                failures = results.filter { !it.success }
            )
        }
    }
    
    /**
     * Wait for field to be ready for input
     */
    private suspend fun waitForFieldReadiness(
        androidServerIp: String,
        settings: WindowsIntegrationSettings
    ) {
        repeat(settings.retryAttempts) { attempt ->
            try {
                val request = Request.Builder()
                    .url("http://$androidServerIp:8080/windows/field-ready")
                    .get()
                    .build()
                
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: ""
                    val isReady = JSONObject(responseBody).getBoolean("ready")
                    if (isReady) return
                }
                
                delay(settings.pasteDelay / 2)
            } catch (e: Exception) {
                // Continue to next attempt
            }
        }
    }
    
    /**
     * Paste individual drug with timing considerations
     */
    private suspend fun pasteDrugWithTiming(
        androidServerIp: String,
        drug: String,
        settings: WindowsIntegrationSettings,
        index: Int
    ): DrugPasteResult {
        return try {
            val jsonBody = JSONObject().apply {
                put("drug_name", drug)
                put("index", index)
                put("timing_profile", settings.timingProfile.name)
                put("paste_delay", settings.pasteDelay)
            }
            
            val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("http://$androidServerIp:8080/windows/paste-drug")
                .post(requestBody)
                .build()
            
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                DrugPasteResult(drug, true, "Success")
            } else {
                DrugPasteResult(drug, false, "HTTP ${response.code}")
            }
        } catch (e: Exception) {
            DrugPasteResult(drug, false, "Exception: ${e.message}")
        }
    }
    
    /**
     * Execute custom keystroke sequences
     */
    private suspend fun executeCustomKeystrokes(
        androidServerIp: String,
        keystrokes: List<String>
    ): Boolean {
        if (keystrokes.isEmpty()) return true
        
        return try {
            val jsonBody = JSONObject().apply {
                put("keystrokes", JSONArray(keystrokes))
            }
            
            val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("http://$androidServerIp:8080/windows/keystrokes")
                .post(requestBody)
                .build()
            
            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Calculate delay between drug entries based on timing profile
     */
    private fun calculateInterDrugDelay(profile: TimingProfile, index: Int, totalDrugs: Int): Long {
        return when (profile) {
            TimingProfile.CONSERVATIVE -> 2000L // 2 seconds
            TimingProfile.STANDARD -> 1000L     // 1 second
            TimingProfile.AGGRESSIVE -> 500L    // 0.5 seconds
            TimingProfile.ADAPTIVE -> {
                // Adaptive timing - slower at start, faster later
                val progress = index.toFloat() / totalDrugs
                val baseDelay = 1500L
                val reduction = (progress * 1000L).toLong()
                maxOf(500L, baseDelay - reduction)
            }
        }
    }
    
    /**
     * Update integration settings
     */
    fun updateSettings(settings: WindowsIntegrationSettings) {
        currentSettings = settings
        _integrationState.value = _integrationState.value.copy(settings = settings)
    }
    
    /**
     * Get current integration settings
     */
    fun getSettings(): WindowsIntegrationSettings = currentSettings
    
    /**
     * Parse software detection response
     */
    private fun parseSoftwareDetectionResponse(response: String): List<PrescriptionSoftware> {
        return try {
            val jsonArray = JSONArray(response)
            val software = mutableListOf<PrescriptionSoftware>()
            
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                software.add(
                    PrescriptionSoftware(
                        id = item.getString("id"),
                        name = item.getString("name"),
                        version = item.optString("version", ""),
                        isActive = item.getBoolean("active"),
                        confidence = item.getDouble("confidence").toFloat()
                    )
                )
            }
            
            software
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Parse window detection response
     */
    private fun parseWindowDetectionResponse(response: String): WindowDetectionResult {
        return try {
            val json = JSONObject(response)
            WindowDetectionResult(
                isValid = json.getBoolean("focused"),
                error = json.optString("error", "")
            )
        } catch (e: Exception) {
            WindowDetectionResult(false, "Parse error: ${e.message}")
        }
    }
    
    /**
     * Parse field detection response
     */
    private fun parseFieldDetectionResponse(response: String): FieldDetectionResult? {
        return try {
            val json = JSONObject(response)
            FieldDetectionResult(
                isActive = json.getBoolean("active"),
                isTextInput = json.getBoolean("text_input"),
                fieldType = json.optString("type", "unknown"),
                position = json.optJSONObject("position")?.let { pos ->
                    FieldPosition(
                        x = pos.getInt("x"),
                        y = pos.getInt("y"),
                        width = pos.getInt("width"),
                        height = pos.getInt("height")
                    )
                }
            )
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Data classes for enhanced Windows integration
 */
data class WindowsIntegrationState(
    val isExecuting: Boolean = false,
    val settings: WindowsIntegrationSettings = WindowsIntegrationSettings(),
    val detectedSoftware: List<PrescriptionSoftware> = emptyList(),
    val lastResult: AutomationResult = AutomationResult.Success("Ready")
)

data class PrescriptionSoftware(
    val id: String,
    val name: String,
    val version: String,
    val isActive: Boolean,
    val confidence: Float
)

data class WindowDetectionResult(
    val isValid: Boolean,
    val error: String = ""
)

data class FieldDetectionResult(
    val isActive: Boolean,
    val isTextInput: Boolean,
    val fieldType: String,
    val position: FieldPosition?
)

data class FieldPosition(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int
)

data class DrugPasteResult(
    val drugName: String,
    val success: Boolean,
    val message: String
)

enum class TimingProfile {
    CONSERVATIVE,  // Slow, reliable
    STANDARD,      // Balanced
    AGGRESSIVE,    // Fast
    ADAPTIVE       // Smart timing based on context
}
