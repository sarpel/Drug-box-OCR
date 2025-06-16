package com.boxocr.simple.repository

import android.util.Log
import com.boxocr.simple.data.OCRResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OCRRepository @Inject constructor() {
    
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }
    
    companion object {
        private const val TAG = "OCRRepository"
        private const val GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent"
    }
    
    suspend fun extractText(
        imageBase64: String,
        apiKey: String
    ): Result<OCRResponse> = withContext(Dispatchers.IO) {
        try {
            val startTime = System.currentTimeMillis()
            
            val requestBody = buildJsonObject {
                put("contents", buildJsonObject {
                    put("parts", buildJsonObject {
                        put("text", "Extract all visible text from this image. Return only the text, no explanations.")
                    })
                    put("parts", buildJsonObject {
                        put("inline_data", buildJsonObject {
                            put("mime_type", "image/jpeg")
                            put("data", imageBase64)
                        })
                    })
                })
            }
            
            val request = Request.Builder()
                .url("$GEMINI_URL?key=$apiKey")
                .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
                .build()
                
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            
            if (response.isSuccessful && responseBody != null) {
                val jsonResponse = json.parseToJsonElement(responseBody) as JsonObject
                val candidates = jsonResponse["candidates"]?.let { 
                    json.parseToJsonElement(it.toString()) as? JsonObject
                }
                
                val text = candidates?.get("content")?.let {
                    val content = json.parseToJsonElement(it.toString()) as JsonObject
                    content["parts"]?.let { parts ->
                        val partsObj = json.parseToJsonElement(parts.toString()) as JsonObject
                        partsObj["text"]?.toString()?.trim('"') ?: ""
                    }
                } ?: ""
                
                val processingTime = System.currentTimeMillis() - startTime
                
                Result.success(
                    OCRResponse(
                        text = text,
                        confidence = 0.85f, // Gemini doesn't provide confidence
                        processingTime = processingTime
                    )
                )
            } else {
                Log.e(TAG, "OCR failed: ${response.code} - $responseBody")
                Result.failure(Exception("OCR request failed: ${response.code}"))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "OCR error", e)
            Result.failure(e)
        }
    }
}
