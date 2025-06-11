package com.boxocr.simple.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.boxocr.simple.data.OCRResult
import com.boxocr.simple.network.GeminiApiService
import com.boxocr.simple.network.GeminiRequest
import com.boxocr.simple.network.Content
import com.boxocr.simple.network.Part
import com.boxocr.simple.network.InlineData
import com.boxocr.simple.network.GenerationConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OCRRepository - Handles text extraction using Gemini API
 */
@Singleton
class OCRRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val geminiApiService: GeminiApiService,
    private val settingsRepository: SettingsRepository
) {
    
    suspend fun extractTextFromImage(imagePath: String): OCRResult = withContext(Dispatchers.IO) {
        try {
            val apiKey = settingsRepository.getApiKey()
            if (apiKey.isBlank()) {
                return@withContext OCRResult.Error("API key not configured. Please set it in Settings.")
            }
            
            val processedImageBase64 = processAndEncodeImage(imagePath)
            
            val request = GeminiRequest(
                contents = listOf(
                    Content(
                        parts = listOf(
                            Part(text = "Extract all text from this image. Return only the text content, no additional formatting or commentary."),
                            Part(inlineData = InlineData(
                                mimeType = "image/jpeg",
                                data = processedImageBase64
                            ))
                        )
                    )
                ),
                generationConfig = GenerationConfig(
                    temperature = 0.1f,
                    maxOutputTokens = 1024
                )
            )
            
            val response = geminiApiService.generateContent(apiKey, request)
            
            if (response.isSuccessful) {
                val extractedText = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!extractedText.isNullOrBlank()) {
                    OCRResult.Success(extractedText.trim())
                } else {
                    OCRResult.Error("No text extracted from image")
                }
            } else {
                OCRResult.Error("API call failed: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("OCRRepository", "OCR processing failed", e)
            OCRResult.Error("OCR processing failed: ${e.message}")
        }
    }
    
    private fun processAndEncodeImage(imagePath: String): String {
        val originalBitmap = BitmapFactory.decodeFile(imagePath)
        
        // Resize image if too large (for API efficiency)
        val maxSize = 1024
        val bitmap = if (originalBitmap.width > maxSize || originalBitmap.height > maxSize) {
            val ratio = minOf(
                maxSize.toFloat() / originalBitmap.width,
                maxSize.toFloat() / originalBitmap.height
            )
            Bitmap.createScaledBitmap(
                originalBitmap,
                (originalBitmap.width * ratio).toInt(),
                (originalBitmap.height * ratio).toInt(),
                true
            )
        } else {
            originalBitmap
        }
        
        // Convert to JPEG and encode to Base64
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val imageBytes = outputStream.toByteArray()
        
        // Clean up
        if (bitmap != originalBitmap) {
            bitmap.recycle()
        }
        originalBitmap.recycle()
        
        return Base64.encodeToString(imageBytes, Base64.NO_WRAP)
    }
}
