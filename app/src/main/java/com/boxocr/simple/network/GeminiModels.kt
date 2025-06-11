package com.boxocr.simple.network

/**
 * Simple Gemini API models for OCR functionality
 */

data class GeminiRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null
)

data class Content(
    val parts: List<Part>
)

data class Part(
    val text: String? = null,
    val inlineData: InlineData? = null
)

data class InlineData(
    val mimeType: String,
    val data: String // Base64 encoded image
)

data class GenerationConfig(
    val temperature: Float = 0.1f,
    val topK: Int = 32,
    val topP: Float = 1.0f,
    val maxOutputTokens: Int = 4096
)

data class GeminiResponse(
    val candidates: List<Candidate>
)

data class Candidate(
    val content: Content,
    val finishReason: String
)
