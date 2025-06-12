package com.boxocr.simple.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.ui.geometry.Rect as ComposeRect
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Smart Camera Manager - Phase 3 Quality of Life Features
 * 
 * Features:
 * 1. Real-time text detection with stability monitoring
 * 2. Drug box detection with rectangle overlay
 * 3. Barcode scanning integration
 * 4. Auto-capture when conditions are met
 * 5. Enhanced camera controls
 */
@Singleton
class SmartCameraManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var cameraProvider: ProcessCameraProvider? = null
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    
    // ML Kit components
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val barcodeScanner = BarcodeScanning.getClient()
    
    // Smart detection state
    private val _detectionState = MutableStateFlow(SmartDetectionState())
    val detectionState: StateFlow<SmartDetectionState> = _detectionState.asStateFlow()
    
    // Text stability tracking
    private var lastDetectedText: String? = null
    private var stableFrameCount = 0
    private val stabilityThreshold = 30 // ~1 second at 30 FPS
    
    // Auto-capture state
    private var autoCaptureCooldown = 0
    private val autoCaptureDelay = 90 // ~3 seconds at 30 FPS
    
    /**
     * Initialize smart camera with real-time detection
     */
    suspend fun initializeSmartCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView
    ): Boolean {
        return try {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProvider = cameraProviderFuture.get()
            
            // Preview
            preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
            
            // Image capture
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .build()
            
            // Real-time image analysis for smart detection
            imageAnalyzer = ImageAnalysis.Builder()
                .setTargetRotation(previewView.display.rotation)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        processImageForSmartDetection(imageProxy)
                    }
                }
            
            // Select camera (prefer back camera)
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            
            // Unbind use cases before rebinding
            cameraProvider?.unbindAll()
            
            // Bind use cases to camera
            camera = cameraProvider?.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture,
                imageAnalyzer
            )
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Process image frame for smart detection features
     */
    private fun processImageForSmartDetection(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            
            // Perform text recognition
            textRecognizer.process(image)
                .addOnSuccessListener { visionText ->
                    processTextDetection(visionText.text, visionText.textBlocks)
                }
                .addOnFailureListener { exception ->
                    // Handle text recognition failure
                }
            
            // Perform barcode scanning
            barcodeScanner.process(image)
                .addOnSuccessListener { barcodes ->
                    processBarcodeDetection(barcodes)
                }
                .addOnFailureListener { exception ->
                    // Handle barcode scanning failure
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
    
    /**
     * Process detected text for stability and drug box detection
     */
    private fun processTextDetection(detectedText: String, textBlocks: List<com.google.mlkit.vision.text.Text.TextBlock>) {
        val currentState = _detectionState.value
        
        // Check text stability
        val isTextStable = checkTextStability(detectedText)
        
        // Detect drug box based on text patterns and layout
        val drugBoxDetection = detectDrugBox(textBlocks)
        
        // Update detection state
        val newState = currentState.copy(
            hasDetectedText = detectedText.isNotBlank(),
            detectedText = detectedText,
            isTextStable = isTextStable,
            drugBox = drugBoxDetection,
            lastUpdateTime = System.currentTimeMillis()
        )
        
        _detectionState.value = newState
        
        // Handle auto-capture logic
        handleAutoCapture(newState)
    }
    
    /**
     * Check if detected text is stable across frames
     */
    private fun checkTextStability(currentText: String): Boolean {
        val cleanText = currentText.trim().lowercase()
        
        return if (cleanText == lastDetectedText) {
            stableFrameCount++
            stableFrameCount >= stabilityThreshold
        } else {
            lastDetectedText = cleanText
            stableFrameCount = 1
            false
        }
    }
    
    /**
     * Detect drug box based on text patterns and layout
     */
    private fun detectDrugBox(textBlocks: List<com.google.mlkit.vision.text.Text.TextBlock>): DrugBoxDetection? {
        if (textBlocks.isEmpty()) return null
        
        // Look for drug-related keywords
        val drugKeywords = listOf(
            "mg", "ml", "tablet", "capsule", "syrup", "drops", "cream", "gel",
            "injection", "dose", "dosage", "pharmaceutical", "medicine", "drug"
        )
        
        var confidence = 0f
        var hasKeywords = false
        var boundingBox: Rect? = null
        
        // Analyze text blocks
        for (block in textBlocks) {
            val blockText = block.text.lowercase()
            
            // Check for drug-related keywords
            for (keyword in drugKeywords) {
                if (blockText.contains(keyword)) {
                    hasKeywords = true
                    confidence += 0.3f
                    break
                }
            }
            
            // Check for numeric patterns (dosage indicators)
            if (blockText.contains(Regex("\\d+\\s*(mg|ml|g)"))) {
                confidence += 0.4f
            }
            
            // Check for structured layout (multiple lines in rectangle)
            if (block.lines.size >= 2) {
                confidence += 0.2f
            }
            
            // Calculate bounding box
            block.boundingBox?.let { rect ->
                boundingBox = if (boundingBox == null) {
                    rect
                } else {
                    Rect().apply {
                        left = minOf(boundingBox!!.left, rect.left)
                        top = minOf(boundingBox!!.top, rect.top)
                        right = maxOf(boundingBox!!.right, rect.right)
                        bottom = maxOf(boundingBox!!.bottom, rect.bottom)
                    }
                }
            }
        }
        
        // Normalize confidence
        confidence = minOf(confidence, 1f)
        
        return if (hasKeywords && confidence > 0.5f && boundingBox != null) {
            DrugBoxDetection(
                isDetected = true,
                confidence = confidence,
                boundingBox = boundingBox.toComposeRect(),
                textBlocks = textBlocks.size
            )
        } else null
    }
    
    /**
     * Process detected barcodes
     */
    private fun processBarcodeDetection(barcodes: List<Barcode>) {
        val currentState = _detectionState.value
        val hasBarcode = barcodes.isNotEmpty()
        
        val barcodeInfo = if (hasBarcode) {
            barcodes.first().let { barcode ->
                BarcodeInfo(
                    value = barcode.rawValue ?: "",
                    format = barcode.format,
                    type = barcode.valueType,
                    boundingBox = barcode.boundingBox?.toComposeRect()
                )
            }
        } else null
        
        _detectionState.value = currentState.copy(
            hasBarcode = hasBarcode,
            barcodeInfo = barcodeInfo
        )
    }
    
    /**
     * Handle auto-capture logic based on detection state
     */
    private fun handleAutoCapture(detectionState: SmartDetectionState) {
        val shouldAutoCapture = detectionState.isReadyForAutoCapture() && 
                               _detectionState.value.autoCapture.isEnabled
        
        if (shouldAutoCapture && autoCaptureCooldown <= 0) {
            // Start auto-capture countdown
            startAutoCapture()
        } else if (autoCaptureCooldown > 0) {
            autoCaptureCooldown--
        }
    }
    
    /**
     * Start auto-capture countdown
     */
    private fun startAutoCapture() {
        autoCaptureCooldown = autoCaptureDelay
        // Implementation would trigger auto-capture after delay
        // This would be coordinated with the ViewModel
    }
    
    /**
     * Capture photo with smart features
     */
    suspend fun capturePhoto(): String? {
        val imageCapture = imageCapture ?: return null
        
        val outputDirectory = getOutputDirectory()
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
                .format(System.currentTimeMillis()) + ".jpg"
        )
        
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(photoFile)
            .build()
        
        return try {
            imageCapture.takePicture(
                outputFileOptions,
                cameraExecutor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        // Image saved successfully
                    }
                    
                    override fun onError(exception: ImageCaptureException) {
                        // Handle error
                    }
                }
            )
            photoFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Toggle auto-capture feature
     */
    fun toggleAutoCapture() {
        val currentState = _detectionState.value
        _detectionState.value = currentState.copy(
            autoCapture = currentState.autoCapture.copy(
                isEnabled = !currentState.autoCapture.isEnabled
            )
        )
    }
    
    /**
     * Enable/disable torch
     */
    fun enableTorchlight(enable: Boolean) {
        camera?.cameraControl?.enableTorch(enable)
    }
    
    /**
     * Get output directory for captured images
     */
    private fun getOutputDirectory(): File {
        val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
            File(it, "BoxOCR").apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else context.filesDir
    }
    
    /**
     * Release camera resources
     */
    fun release() {
        cameraProvider?.unbindAll()
        cameraExecutor.shutdown()
        textRecognizer.close()
        barcodeScanner.close()
    }
    
    /**
     * Extension function to convert Android Rect to Compose Rect
     */
    private fun Rect.toComposeRect(): ComposeRect {
        return ComposeRect(
            offset = androidx.compose.ui.geometry.Offset(left.toFloat(), top.toFloat()),
            size = androidx.compose.ui.geometry.Size(
                width = (right - left).toFloat(),
                height = (bottom - top).toFloat()
            )
        )
    }
}

/**
 * Smart detection state data class
 */
data class SmartDetectionState(
    val hasDetectedText: Boolean = false,
    val detectedText: String = "",
    val isTextStable: Boolean = false,
    val drugBox: DrugBoxDetection? = null,
    val hasBarcode: Boolean = false,
    val barcodeInfo: BarcodeInfo? = null,
    val autoCapture: AutoCaptureSettings = AutoCaptureSettings(),
    val lastUpdateTime: Long = 0
) {
    fun isReadyForAutoCapture(): Boolean {
        return hasDetectedText && 
               isTextStable && 
               drugBox?.isDetected == true && 
               drugBox.confidence > 0.7f
    }
}

/**
 * Drug box detection result
 */
data class DrugBoxDetection(
    val isDetected: Boolean,
    val confidence: Float,
    val boundingBox: ComposeRect,
    val textBlocks: Int
)

/**
 * Barcode information
 */
data class BarcodeInfo(
    val value: String,
    val format: Int,
    val type: Int,
    val boundingBox: ComposeRect?
)

/**
 * Auto-capture settings
 */
data class AutoCaptureSettings(
    val isEnabled: Boolean = true,
    val stabilityThreshold: Int = 30,
    val confidenceThreshold: Float = 0.7f,
    val countdownSeconds: Int = 3
)
