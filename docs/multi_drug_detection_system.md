# 🎯 Multi-Drug Box Detection & Recognition System

## 🚀 **REVOLUTIONARY MULTI-DRUG SCANNING ARCHITECTURE**

### **📱 ENHANCED INPUT SOURCES**

#### **1. 📸 Multi-Source Camera System**
```kotlin
class MultiSourceCameraRepository @Inject constructor(
    private val cameraManager: CameraManager,
    private val mlKitDetector: MLKitObjectDetector,
    private val geminiRepository: OCRRepository
) {
    
    // Live video stream processing
    suspend fun startLiveVideoDetection(): Flow<MultiDrugDetectionResult> {
        return cameraManager.startVideoStream()
            .map { frame -> processVideoFrame(frame) }
            .filter { result -> result.detectedBoxes.isNotEmpty() }
    }
    
    // Single photo capture with multi-drug detection
    suspend fun capturePhotoWithMultiDetection(): MultiDrugDetectionResult {
        val photo = cameraManager.capturePhoto()
        return processImageForMultipleDrugs(photo)
    }
    
    // Gallery image processing
    suspend fun processGalleryImage(uri: Uri): MultiDrugDetectionResult {
        val image = loadImageFromUri(uri)
        return processImageForMultipleDrugs(image)
    }
}

data class MultiDrugDetectionResult(
    val originalImage: Bitmap,
    val detectedBoxes: List<DrugBoxDetection>,
    val processingTime: Long,
    val confidence: Float,
    val totalDrugsFound: Int
)

data class DrugBoxDetection(
    val boundingBox: Rect,
    val croppedImage: Bitmap,
    val ocrResult: OCRResult,
    val drugMatch: DrugMatchResult?,
    val confidence: Float,
    val isDamaged: Boolean,
    val visualSimilarity: VisualSimilarityResult?
)
```

### **🔍 ADVANCED OBJECT DETECTION SYSTEM**

#### **2. 🎯 ML Kit Enhanced Drug Box Detection**
```kotlin
class DrugBoxObjectDetector @Inject constructor(
    private val objectDetector: ObjectDetector,
    private val textRecognizer: TextRecognizer
) {
    
    suspend fun detectMultipleDrugBoxes(image: Bitmap): List<DrugBoxRegion> {
        val inputImage = InputImage.fromBitmap(image, 0)
        
        // Step 1: Generic object detection for rectangular objects
        val objects = objectDetector.process(inputImage).await()
        
        // Step 2: Text region detection to identify boxes with text
        val textBlocks = textRecognizer.process(inputImage).await()
        
        // Step 3: Combine object detection + text detection for drug boxes
        return combineDetections(objects, textBlocks, image)
            .filter { region -> isDrugBoxCandidate(region) }
            .sortedByDescending { it.confidence }
    }
    
    private fun combineDetections(
        objects: List<DetectedObject>,
        textBlocks: Text,
        originalImage: Bitmap
    ): List<DrugBoxRegion> {
        val drugBoxRegions = mutableListOf<DrugBoxRegion>()
        
        // Look for rectangular objects that contain text
        objects.forEach { obj ->
            val overlappingTextBlocks = textBlocks.textBlocks.filter { textBlock ->
                obj.boundingBox.intersects(textBlock.boundingBox)
            }
            
            if (overlappingTextBlocks.isNotEmpty()) {
                drugBoxRegions.add(
                    DrugBoxRegion(
                        boundingBox = obj.boundingBox,
                        textBlocks = overlappingTextBlocks,
                        confidence = obj.trackingId?.let { 0.8f } ?: 0.6f,
                        croppedImage = cropImageRegion(originalImage, obj.boundingBox)
                    )
                )
            }
        }
        
        return drugBoxRegions
    }
    
    private fun isDrugBoxCandidate(region: DrugBoxRegion): Boolean {
        // Validate if region looks like a drug box
        val aspectRatio = region.boundingBox.width().toFloat() / region.boundingBox.height()
        val hasEnoughText = region.textBlocks.sumOf { it.text.length } > 5
        val reasonableSize = region.boundingBox.width() > 100 && region.boundingBox.height() > 50
        
        return aspectRatio in 0.5..3.0 && hasEnoughText && reasonableSize
    }
}

data class DrugBoxRegion(
    val boundingBox: Rect,
    val textBlocks: List<Text.TextBlock>,
    val confidence: Float,
    val croppedImage: Bitmap
)
```

### **🧠 DAMAGED TEXT RECOVERY SYSTEM**

#### **3. 🔧 AI-Powered Text Reconstruction**
```kotlin
class DamagedTextRecoveryRepository @Inject constructor(
    private val geminiRepository: OCRRepository,
    private val visualDatabaseRepository: VisualDrugDatabaseRepository,
    private val turkishDrugRepository: TurkishDrugDatabaseRepository
) {
    
    suspend fun recoverDamagedText(
        croppedImage: Bitmap,
        partialOCRText: String
    ): TextRecoveryResult {
        
        // Method 1: Enhanced OCR with context
        val enhancedOCR = tryEnhancedOCR(croppedImage, partialOCRText)
        
        // Method 2: Visual similarity matching
        val visualMatch = tryVisualMatching(croppedImage)
        
        // Method 3: Partial text completion using drug database
        val textCompletion = tryTextCompletion(partialOCRText)
        
        // Method 4: AI-powered text reconstruction
        val aiReconstruction = tryAIReconstruction(croppedImage, partialOCRText)
        
        // Combine all methods for best result
        return combineRecoveryMethods(enhancedOCR, visualMatch, textCompletion, aiReconstruction)
    }
    
    private suspend fun tryEnhancedOCR(image: Bitmap, context: String): OCRResult {
        // Apply image enhancement first
        val enhancedImage = enhanceImageForOCR(image)
        
        // Use Gemini with Turkish medical context
        val prompt = """
        Bu Türk ilaç kutusundan metin çıkarın. Kısmi metin: "$context"
        Türkçe ilaç isimleri ve medikal terimler için optimize edin.
        Hasarlı veya bulanık harfleri tahmin edin.
        """
        
        return geminiRepository.extractTextWithContext(enhancedImage, prompt)
    }
    
    private suspend fun tryVisualMatching(image: Bitmap): VisualSimilarityResult {
        // Compare against database of drug box images
        return visualDatabaseRepository.findVisualSimilarity(image)
    }
    
    private suspend fun tryTextCompletion(partialText: String): List<String> {
        // Use Turkish drug database for partial text completion
        return turkishDrugRepository.findPartialMatches(partialText)
            .filter { it.similarity > 0.6f }
            .map { it.drugName }
    }
    
    private suspend fun tryAIReconstruction(image: Bitmap, partialText: String): AIReconstructionResult {
        val prompt = """
        Analiz et: Türk ilaç kutusu görüntüsü
        Kısmi metin: "$partialText"
        
        Görevler:
        1. Hasarlı/eksik harfleri tamamla
        2. Türkçe ilaç adı kurallarını uygula
        3. Medikal terim kontrolü yap
        4. Güven skoru ver (0-100)
        
        Türk ilaç veritabanı bilgisini kullan.
        """
        
        return geminiRepository.reconstructDamagedText(image, prompt)
    }
    
    private fun enhanceImageForOCR(image: Bitmap): Bitmap {
        // Apply image processing techniques
        return ImageProcessor.Builder()
            .addProcessor(ContrastEnhancer(1.5f))
            .addProcessor(NoiseReducer())
            .addProcessor(SharpnessFilter())
            .addProcessor(TextOptimizer())
            .build()
            .process(image)
    }
}

data class TextRecoveryResult(
    val recoveredText: String,
    val confidence: Float,
    val method: RecoveryMethod,
    val alternatives: List<String>,
    val processingTime: Long
)

enum class RecoveryMethod {
    ENHANCED_OCR,
    VISUAL_MATCHING,
    TEXT_COMPLETION,
    AI_RECONSTRUCTION,
    COMBINED
}
```

### **🖼️ VISUAL DRUG DATABASE SYSTEM**

#### **4. 📚 Drug Box Image Database**
```kotlin
class VisualDrugDatabaseRepository @Inject constructor(
    private val database: VisualDrugDatabase,
    private val imageProcessor: ImageProcessor
) {
    
    suspend fun buildVisualDatabase(drugBoxImages: List<DrugBoxImage>) {
        drugBoxImages.forEach { drugImage ->
            val features = extractVisualFeatures(drugImage.image)
            val colorHistogram = extractColorHistogram(drugImage.image)
            val textLayout = analyzeTextLayout(drugImage.image)
            
            database.insertDrugVisualData(
                DrugVisualData(
                    drugName = drugImage.drugName,
                    brandName = drugImage.brandName,
                    visualFeatures = features,
                    colorHistogram = colorHistogram,
                    textLayout = textLayout,
                    dominantColors = extractDominantColors(drugImage.image),
                    packageShape = analyzePackageShape(drugImage.image)
                )
            )
        }
    }
    
    suspend fun findVisualSimilarity(queryImage: Bitmap): VisualSimilarityResult {
        val queryFeatures = extractVisualFeatures(queryImage)
        val queryColors = extractColorHistogram(queryImage)
        val queryLayout = analyzeTextLayout(queryImage)
        
        val allDrugVisuals = database.getAllVisualData()
        
        val similarities = allDrugVisuals.map { drugVisual ->
            val featureSimilarity = calculateFeatureSimilarity(queryFeatures, drugVisual.visualFeatures)
            val colorSimilarity = calculateColorSimilarity(queryColors, drugVisual.colorHistogram)
            val layoutSimilarity = calculateLayoutSimilarity(queryLayout, drugVisual.textLayout)
            
            // Weighted combination
            val overallSimilarity = (featureSimilarity * 0.4f + 
                                   colorSimilarity * 0.3f + 
                                   layoutSimilarity * 0.3f)
            
            DrugSimilarityScore(
                drugName = drugVisual.drugName,
                similarity = overallSimilarity,
                featureMatch = featureSimilarity,
                colorMatch = colorSimilarity,
                layoutMatch = layoutSimilarity
            )
        }.sortedByDescending { it.similarity }
        
        return VisualSimilarityResult(
            topMatches = similarities.take(5),
            bestMatch = similarities.firstOrNull(),
            processingTime = System.currentTimeMillis()
        )
    }
    
    private fun extractVisualFeatures(image: Bitmap): VisualFeatures {
        // Extract SIFT/ORB features for image matching
        // Edge detection for package outline
        // Corner detection for box shape
        return VisualFeatures(
            siftFeatures = extractSIFTFeatures(image),
            edges = detectEdges(image),
            corners = detectCorners(image),
            textRegions = detectTextRegions(image)
        )
    }
    
    private fun extractColorHistogram(image: Bitmap): ColorHistogram {
        // RGB histogram for color matching
        // Dominant color extraction
        // Color distribution analysis
        return ColorHistogram(
            rgbHistogram = calculateRGBHistogram(image),
            dominantColors = extractDominantColors(image, 5),
            colorDistribution = analyzeColorDistribution(image)
        )
    }
}

data class DrugBoxImage(
    val drugName: String,
    val brandName: String,
    val image: Bitmap,
    val metadata: Map<String, String>
)

data class VisualSimilarityResult(
    val topMatches: List<DrugSimilarityScore>,
    val bestMatch: DrugSimilarityScore?,
    val processingTime: Long
)

data class DrugSimilarityScore(
    val drugName: String,
    val similarity: Float,
    val featureMatch: Float,
    val colorMatch: Float,
    val layoutMatch: Float
)
```

### **🔄 UNIFIED MULTI-DRUG PROCESSING WORKFLOW**

#### **5. 🎯 Complete Multi-Drug Scanner**
```kotlin
class MultiDrugScannerRepository @Inject constructor(
    private val multiSourceCamera: MultiSourceCameraRepository,
    private val objectDetector: DrugBoxObjectDetector,
    private val textRecovery: DamagedTextRecoveryRepository,
    private val visualDatabase: VisualDrugDatabaseRepository,
    private val enhancedMatching: EnhancedDatabaseRepository
) {
    
    suspend fun processMultiDrugImage(
        imageSource: ImageSource,
        sourceData: Any // Bitmap, Uri, or CameraFrame
    ): MultiDrugProcessingResult {
        
        val startTime = System.currentTimeMillis()
        
        // Step 1: Load image from source
        val originalImage = when (imageSource) {
            ImageSource.CAMERA -> sourceData as Bitmap
            ImageSource.GALLERY -> loadFromUri(sourceData as Uri)
            ImageSource.LIVE_VIDEO -> extractFrameBitmap(sourceData as CameraFrame)
        }
        
        // Step 2: Detect multiple drug boxes
        val detectedRegions = objectDetector.detectMultipleDrugBoxes(originalImage)
        
        // Step 3: Process each detected box
        val processedDrugs = detectedRegions.mapIndexed { index, region ->
            processSingleDrugBox(region, index)
        }
        
        // Step 4: Combine results and validate
        val validatedResults = validateAndCombineResults(processedDrugs)
        
        val processingTime = System.currentTimeMillis() - startTime
        
        return MultiDrugProcessingResult(
            originalImage = originalImage,
            detectedDrugs = validatedResults,
            totalDrugsDetected = validatedResults.size,
            processingTimeMs = processingTime,
            imageSource = imageSource,
            timestamp = System.currentTimeMillis()
        )
    }
    
    private suspend fun processSingleDrugBox(
        region: DrugBoxRegion,
        index: Int
    ): ProcessedDrugResult {
        
        // Basic OCR
        val ocrResult = performOCR(region.croppedImage)
        
        // Check if text appears damaged
        val isDamaged = assessTextDamage(ocrResult.text)
        
        // Enhanced processing for damaged text
        val finalText = if (isDamaged) {
            val recovery = textRecovery.recoverDamagedText(region.croppedImage, ocrResult.text)
            recovery.recoveredText
        } else {
            ocrResult.text
        }
        
        // Match against drug database
        val drugMatch = enhancedMatching.findBestMatch(finalText)
        
        // Visual similarity as backup
        val visualMatch = if (drugMatch.confidence < 0.7f) {
            visualDatabase.findVisualSimilarity(region.croppedImage)
        } else null
        
        return ProcessedDrugResult(
            index = index,
            boundingBox = region.boundingBox,
            croppedImage = region.croppedImage,
            originalText = ocrResult.text,
            finalText = finalText,
            drugMatch = drugMatch,
            visualMatch = visualMatch,
            confidence = calculateOverallConfidence(drugMatch, visualMatch),
            isDamaged = isDamaged,
            processingTime = System.currentTimeMillis()
        )
    }
    
    private fun assessTextDamage(text: String): Boolean {
        // Heuristics to detect damaged text
        val hasIrregularSpacing = text.contains(Regex("\\s{3,}"))
        val hasRandomChars = text.contains(Regex("[^a-zA-ZçğıöşüÇĞIİÖŞÜ0-9\\s-]"))
        val tooShort = text.length < 3
        val fragmentedWords = text.split(" ").count { it.length < 2 } > text.split(" ").size / 2
        
        return hasIrregularSpacing || hasRandomChars || tooShort || fragmentedWords
    }
    
    private fun calculateOverallConfidence(
        drugMatch: DrugMatchResult,
        visualMatch: VisualSimilarityResult?
    ): Float {
        var confidence = drugMatch.confidence
        
        // Boost confidence if visual match confirms text match
        visualMatch?.bestMatch?.let { visual ->
            if (visual.drugName.equals(drugMatch.drugName, ignoreCase = true)) {
                confidence = minOf(1.0f, confidence + 0.2f)
            }
        }
        
        return confidence
    }
}

data class MultiDrugProcessingResult(
    val originalImage: Bitmap,
    val detectedDrugs: List<ProcessedDrugResult>,
    val totalDrugsDetected: Int,
    val processingTimeMs: Long,
    val imageSource: ImageSource,
    val timestamp: Long
)

data class ProcessedDrugResult(
    val index: Int,
    val boundingBox: Rect,
    val croppedImage: Bitmap,
    val originalText: String,
    val finalText: String,
    val drugMatch: DrugMatchResult,
    val visualMatch: VisualSimilarityResult?,
    val confidence: Float,
    val isDamaged: Boolean,
    val processingTime: Long
)

enum class ImageSource {
    CAMERA, GALLERY, LIVE_VIDEO
}
```

## 📱 **ENHANCED UI FOR MULTI-DRUG SCANNING**

### **6. 🎨 Multi-Drug Detection Interface**
```kotlin
@Composable
fun MultiDrugScannerScreen(
    onNavigateBack: () -> Unit,
    onDrugsDetected: (MultiDrugProcessingResult) -> Unit,
    viewModel: MultiDrugScannerViewModel = hiltViewModel()
) {
    val scannerState by viewModel.scannerState.collectAsState()
    val detectionResults by viewModel.detectionResults.collectAsState()
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Enhanced Camera Preview with overlays
        Box(modifier = Modifier.weight(1f)) {
            // Camera preview
            CameraPreview(
                onImageCaptured = viewModel::processImage,
                showDetectionOverlay = true
            )
            
            // Real-time detection overlays
            detectionResults?.let { results ->
                DetectionOverlay(
                    results = results,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            // Live detection status
            LiveDetectionStatus(
                drugsDetected = detectionResults?.totalDrugsDetected ?: 0,
                isProcessing = scannerState.isProcessing,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            )
        }
        
        // Control panel
        MultiDrugControlPanel(
            onCapturePhoto = viewModel::capturePhoto,
            onSelectFromGallery = viewModel::selectFromGallery,
            onToggleLiveVideo = viewModel::toggleLiveVideo,
            isLiveMode = scannerState.isLiveMode,
            modifier = Modifier.padding(16.dp)
        )
        
        // Results preview
        detectionResults?.let { results ->
            DrugDetectionResults(
                results = results,
                onConfirmDrugs = { onDrugsDetected(results) },
                onRetakePhoto = viewModel::retakePhoto,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
fun DetectionOverlay(
    results: MultiDrugProcessingResult,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        results.detectedDrugs.forEach { drug ->
            // Draw bounding boxes around detected drugs
            drawRect(
                color = when {
                    drug.confidence > 0.8f -> Color.Green
                    drug.confidence > 0.6f -> Color.Yellow
                    else -> Color.Red
                },
                topLeft = Offset(
                    drug.boundingBox.left.toFloat(),
                    drug.boundingBox.top.toFloat()
                ),
                size = Size(
                    drug.boundingBox.width().toFloat(),
                    drug.boundingBox.height().toFloat()
                ),
                style = Stroke(width = 4.dp.toPx())
            )
            
            // Draw drug name label
            drawRect(
                color = Color.Black.copy(alpha = 0.7f),
                topLeft = Offset(
                    drug.boundingBox.left.toFloat(),
                    drug.boundingBox.top.toFloat() - 30.dp.toPx()
                ),
                size = Size(
                    drug.drugMatch.drugName.length * 8.dp.toPx(),
                    25.dp.toPx()
                )
            )
        }
    }
}

@Composable
fun DrugDetectionResults(
    results: MultiDrugProcessingResult,
    onConfirmDrugs: () -> Unit,
    onRetakePhoto: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Detected ${results.totalDrugsDetected} drugs",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Drug list
            LazyColumn(modifier = Modifier.height(200.dp)) {
                itemsIndexed(results.detectedDrugs) { index, drug ->
                    DrugDetectionItem(
                        drug = drug,
                        index = index + 1
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(onClick = onRetakePhoto) {
                    Text("Retake")
                }
                Button(
                    onClick = onConfirmDrugs,
                    enabled = results.detectedDrugs.any { it.confidence > 0.6f }
                ) {
                    Text("Confirm ${results.detectedDrugs.count { it.confidence > 0.6f }} drugs")
                }
            }
        }
    }
}
```

## 🎯 **IMPLEMENTATION BENEFITS**

### **🚀 Revolutionary Capabilities**
- ✅ **Multiple Input Sources** - Camera, gallery, live video
- ✅ **Multi-Drug Detection** - Process entire prescription at once
- ✅ **Damaged Text Recovery** - AI-powered text reconstruction
- ✅ **Visual Database Matching** - Image-based drug recognition
- ✅ **Real-time Processing** - Live video OCR with overlays

### **🧠 AI Intelligence Enhancement**
- ✅ **Smart Object Detection** - ML Kit + custom logic
- ✅ **Context-Aware OCR** - Turkish medical optimization
- ✅ **Multi-Method Recovery** - 4 different text recovery approaches
- ✅ **Visual Similarity** - Database of drug box images
- ✅ **Confidence Scoring** - Combined text + visual matching

### **📱 User Experience Revolution**
- ✅ **Instant Multi-Drug Scanning** - One photo, multiple drugs
- ✅ **Real-time Feedback** - Live detection overlays
- ✅ **Damage Tolerance** - Works with partial/damaged text
- ✅ **Visual Confirmation** - See exactly what was detected
- ✅ **Flexible Input** - Choose camera, gallery, or live video

This system transforms your app into a **true multi-drug intelligent scanner** that can handle real-world prescription scenarios with damaged boxes and multiple drugs per image! 🚀📱🎯