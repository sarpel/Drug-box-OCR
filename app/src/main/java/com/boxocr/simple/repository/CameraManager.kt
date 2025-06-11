package com.boxocr.simple.repository

import android.content.Context
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * CameraManager - Handles camera operations with CameraX
 */
@Singleton
class CameraManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null
    
    suspend fun initializeCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView
    ): Boolean = withContext(Dispatchers.Main) {
        try {
            cameraProvider = ProcessCameraProvider.getInstance(context).get()
            
            val preview = Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(previewView.display.rotation)
                .build()
            
            preview.setSurfaceProvider(previewView.surfaceProvider)
            
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(previewView.display.rotation)
                .build()
            
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            
            cameraProvider?.unbindAll()
            camera = cameraProvider?.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
            
            true
        } catch (e: Exception) {
            Log.e("CameraManager", "Camera initialization failed", e)
            false
        }
    }
    
    suspend fun capturePhoto(): String? = withContext(Dispatchers.IO) {
        val imageCapture = imageCapture ?: return@withContext null
        
        val outputFile = File(context.cacheDir, "captured_${System.currentTimeMillis()}.jpg")
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()
        
        suspendCancellableCoroutine<String?> { continuation ->
            imageCapture.takePicture(
                outputFileOptions,
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        continuation.resume(outputFile.absolutePath)
                    }
                    
                    override fun onError(exception: ImageCaptureException) {
                        Log.e("CameraManager", "Photo capture failed", exception)
                        continuation.resume(null)
                    }
                }
            )
        }
    }
    
    fun enableTorchlight(enabled: Boolean) {
        camera?.cameraControl?.enableTorch(enabled)
    }
    
    fun release() {
        cameraProvider?.unbindAll()
        cameraProvider = null
    }
}
