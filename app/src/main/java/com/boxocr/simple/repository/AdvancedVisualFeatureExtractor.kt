package com.boxocr.simple.repository

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.*

/**
 * Advanced Visual Feature Extractor - Phase 2 Week 4 Enhancement
 * 
 * Implements sophisticated visual feature extraction algorithms for drug box matching:
 * - SIFT-inspired feature detection
 * - Color histogram analysis
 * - Text layout detection
 * - Edge feature extraction
 * - Multi-algorithm similarity scoring
 */
object AdvancedVisualFeatureExtractor {
    
    // Configuration constants
    private const val SIFT_OCTAVES = 4
    private const val SIFT_LEVELS = 3
    private const val COLOR_HISTOGRAM_BINS = 64
    private const val EDGE_THRESHOLD = 50
    private const val TEXT_BLOCK_MIN_SIZE = 20
    
    /**
     * Extract comprehensive visual features from drug box image
     */
    fun extractComprehensiveFeatures(bitmap: Bitmap): Map<VisualFeatureType, FeatureData> {
        val features = mutableMapOf<VisualFeatureType, FeatureData>()
        
        try {
            // Extract different types of visual features
            features[VisualFeatureType.SIFT_FEATURES] = extractSIFTFeatures(bitmap)
            features[VisualFeatureType.COLOR_HISTOGRAM] = extractColorHistogram(bitmap)
            features[VisualFeatureType.TEXT_LAYOUT] = extractTextLayoutFeatures(bitmap)
            features[VisualFeatureType.EDGE_FEATURES] = extractEdgeFeatures(bitmap)
            features[VisualFeatureType.SHAPE_FEATURES] = extractShapeFeatures(bitmap)
            features[VisualFeatureType.TEXTURE_FEATURES] = extractTextureFeatures(bitmap)
            
        } catch (e: Exception) {
            // Fallback to basic features if advanced extraction fails
            features[VisualFeatureType.BASIC_FEATURES] = extractBasicFeatures(bitmap)
        }
        
        return features
    }
    
    /**
     * Extract SIFT-inspired keypoint features
     */
    private fun extractSIFTFeatures(bitmap: Bitmap): FeatureData {
        val width = bitmap.width
        val height = bitmap.height
        val grayscale = convertToGrayscale(bitmap)
        
        // Generate scale space
        val scaleSpace = generateScaleSpace(grayscale, SIFT_OCTAVES, SIFT_LEVELS)
        
        // Detect keypoints
        val keypoints = detectKeypoints(scaleSpace)
        
        // Generate descriptors
        val descriptors = generateDescriptors(grayscale, keypoints)
        
        // Convert to feature vector
        val featureVector = descriptors.joinToString(",") { it.toString() }
        
        return FeatureData(
            data = "keypoints:${keypoints.size}",
            vector = featureVector,
            method = "sift_inspired",
            confidence = calculateKeypointConfidence(keypoints, width * height)
        )
    }
    
    /**
     * Extract color histogram features
     */
    private fun extractColorHistogram(bitmap: Bitmap): FeatureData {
        val histogram = IntArray(COLOR_HISTOGRAM_BINS * 3) // RGB channels
        val binSize = 256 / COLOR_HISTOGRAM_BINS
        val totalPixels = bitmap.width * bitmap.height
        
        // Process each pixel
        for (x in 0 until bitmap.width) {
            for (y in 0 until bitmap.height) {
                val pixel = bitmap.getPixel(x, y)
                val r = Color.red(pixel) / binSize
                val g = Color.green(pixel) / binSize
                val b = Color.blue(pixel) / binSize
                
                // Ensure indices are within bounds
                val rIndex = minOf(r, COLOR_HISTOGRAM_BINS - 1)
                val gIndex = minOf(g, COLOR_HISTOGRAM_BINS - 1)
                val bIndex = minOf(b, COLOR_HISTOGRAM_BINS - 1)
                
                histogram[rIndex]++
                histogram[COLOR_HISTOGRAM_BINS + gIndex]++
                histogram[COLOR_HISTOGRAM_BINS * 2 + bIndex]++
            }
        }
        
        // Normalize histogram
        val normalizedHistogram = histogram.map { it.toFloat() / totalPixels }
        val featureVector = normalizedHistogram.joinToString(",")
        
        // Calculate color distribution confidence
        val colorVariance = calculateColorVariance(normalizedHistogram)
        
        return FeatureData(
            data = "color_distribution:$colorVariance",
            vector = featureVector,
            method = "color_histogram",
            confidence = minOf(colorVariance * 2f, 1f)
        )
    }
    
    /**
     * Extract text layout features specific to drug boxes
     */
    private fun extractTextLayoutFeatures(bitmap: Bitmap): FeatureData {
        val grayscale = convertToGrayscale(bitmap)
        val binaryImage = applyAdaptiveThreshold(grayscale)
        
        // Detect text regions using connected components
        val textRegions = detectTextRegions(binaryImage)
        
        // Calculate layout features
        val layoutFeatures = calculateLayoutFeatures(textRegions, bitmap.width, bitmap.height)
        
        val featureVector = listOf(
            layoutFeatures.topTextRatio,
            layoutFeatures.centerTextRatio,
            layoutFeatures.bottomTextRatio,
            layoutFeatures.leftTextRatio,
            layoutFeatures.rightTextRatio,
            layoutFeatures.textDensity,
            layoutFeatures.averageTextBlockSize,
            layoutFeatures.textBlockCount.toFloat()
        ).joinToString(",")
        
        return FeatureData(
            data = "text_blocks:${layoutFeatures.textBlockCount}",
            vector = featureVector,
            method = "text_layout_analysis",
            confidence = calculateTextLayoutConfidence(layoutFeatures)
        )
    }
    
    /**
     * Extract edge features using Sobel operator
     */
    private fun extractEdgeFeatures(bitmap: Bitmap): FeatureData {
        val grayscale = convertToGrayscale(bitmap)
        val edges = applySobelEdgeDetection(grayscale)
        
        // Calculate edge statistics
        val edgeStatistics = calculateEdgeStatistics(edges)
        
        // Generate edge histogram
        val edgeHistogram = generateEdgeHistogram(edges)
        
        val featureVector = (edgeStatistics + edgeHistogram).joinToString(",")
        
        return FeatureData(
            data = "edge_density:${edgeStatistics[0]}",
            vector = featureVector,
            method = "sobel_edge_detection",
            confidence = calculateEdgeConfidence(edgeStatistics)
        )
    }
    
    /**
     * Extract shape features from drug box contours
     */
    private fun extractShapeFeatures(bitmap: Bitmap): FeatureData {
        val grayscale = convertToGrayscale(bitmap)
        val edges = applySobelEdgeDetection(grayscale)
        
        // Find contours
        val contours = findContours(edges)
        
        // Calculate shape descriptors
        val shapeFeatures = calculateShapeDescriptors(contours)
        
        val featureVector = shapeFeatures.joinToString(",")
        
        return FeatureData(
            data = "contours:${contours.size}",
            vector = featureVector,
            method = "shape_analysis",
            confidence = calculateShapeConfidence(contours, bitmap.width * bitmap.height)
        )
    }
    
    /**
     * Extract texture features using Local Binary Patterns (LBP)
     */
    private fun extractTextureFeatures(bitmap: Bitmap): FeatureData {
        val grayscale = convertToGrayscale(bitmap)
        val lbpHistogram = calculateLBPHistogram(grayscale)
        
        val featureVector = lbpHistogram.joinToString(",")
        
        return FeatureData(
            data = "texture_patterns:${lbpHistogram.size}",
            vector = featureVector,
            method = "local_binary_patterns",
            confidence = calculateTextureConfidence(lbpHistogram)
        )
    }
    
    /**
     * Extract basic features as fallback
     */
    private fun extractBasicFeatures(bitmap: Bitmap): FeatureData {
        // Basic statistical features
        val avgBrightness = calculateAverageBrightness(bitmap)
        val contrast = calculateContrast(bitmap)
        val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
        
        val featureVector = listOf(avgBrightness, contrast, aspectRatio).joinToString(",")
        
        return FeatureData(
            data = "basic_stats",
            vector = featureVector,
            method = "basic_statistics",
            confidence = 0.5f
        )
    }
    
    // Helper functions for image processing
    
    private fun convertToGrayscale(bitmap: Bitmap): Array<IntArray> {
        val width = bitmap.width
        val height = bitmap.height
        val grayscale = Array(height) { IntArray(width) }
        
        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = bitmap.getPixel(x, y)
                val gray = (Color.red(pixel) * 0.299 + Color.green(pixel) * 0.587 + Color.blue(pixel) * 0.114).toInt()
                grayscale[y][x] = gray
            }
        }
        
        return grayscale
    }
    
    private fun generateScaleSpace(grayscale: Array<IntArray>, octaves: Int, levels: Int): Array<Array<Array<IntArray>>> {
        val height = grayscale.size
        val width = grayscale[0].size
        val scaleSpace = Array(octaves) { Array(levels) { Array(height) { IntArray(width) } } }
        
        // Initialize first octave
        scaleSpace[0][0] = grayscale
        
        // Generate scale space (simplified)
        for (octave in 0 until octaves) {
            for (level in 0 until levels) {
                if (octave == 0 && level == 0) continue
                
                val sourceHeight = if (octave > 0) height / (1 shl octave) else height
                val sourceWidth = if (octave > 0) width / (1 shl octave) else width
                
                scaleSpace[octave][level] = Array(sourceHeight) { IntArray(sourceWidth) }
                
                // Apply Gaussian blur (simplified)
                for (y in 1 until sourceHeight - 1) {
                    for (x in 1 until sourceWidth - 1) {
                        var sum = 0
                        for (dy in -1..1) {
                            for (dx in -1..1) {
                                sum += if (octave == 0) grayscale[y + dy][x + dx] else scaleSpace[octave - 1][levels - 1][y + dy][x + dx]
                            }
                        }
                        scaleSpace[octave][level][y][x] = sum / 9
                    }
                }
            }
        }
        
        return scaleSpace
    }
    
    private fun detectKeypoints(scaleSpace: Array<Array<Array<IntArray>>>): List<Keypoint> {
        val keypoints = mutableListOf<Keypoint>()
        
        // Simplified keypoint detection
        for (octave in 0 until scaleSpace.size) {
            for (level in 1 until scaleSpace[octave].size - 1) {
                val current = scaleSpace[octave][level]
                val above = scaleSpace[octave][level + 1]
                val below = scaleSpace[octave][level - 1]
                
                for (y in 1 until current.size - 1) {
                    for (x in 1 until current[0].size - 1) {
                        val value = current[y][x]
                        
                        // Check if it's a local extremum
                        var isExtremum = true
                        for (dy in -1..1) {
                            for (dx in -1..1) {
                                if (current[y + dy][x + dx] > value || 
                                    above[y + dy][x + dx] > value || 
                                    below[y + dy][x + dx] > value) {
                                    isExtremum = false
                                    break
                                }
                            }
                            if (!isExtremum) break
                        }
                        
                        if (isExtremum && value > EDGE_THRESHOLD) {
                            keypoints.add(Keypoint(x, y, octave, level, value.toFloat()))
                        }
                    }
                }
            }
        }
        
        return keypoints
    }
    
    private fun generateDescriptors(grayscale: Array<IntArray>, keypoints: List<Keypoint>): List<Float> {
        val descriptors = mutableListOf<Float>()
        
        for (keypoint in keypoints) {
            // Generate 16-dimensional descriptor for each keypoint
            val descriptor = generateKeypointDescriptor(grayscale, keypoint)
            descriptors.addAll(descriptor)
        }
        
        return descriptors
    }
    
    private fun generateKeypointDescriptor(grayscale: Array<IntArray>, keypoint: Keypoint): List<Float> {
        val descriptor = mutableListOf<Float>()
        val radius = 8
        
        // Extract 4x4 region around keypoint
        for (dy in -radius until radius step 4) {
            for (dx in -radius until radius step 4) {
                var regionSum = 0f
                var count = 0
                
                for (y in dy until dy + 4) {
                    for (x in dx until dx + 4) {
                        val py = keypoint.y + y
                        val px = keypoint.x + x
                        
                        if (py >= 0 && py < grayscale.size && px >= 0 && px < grayscale[0].size) {
                            regionSum += grayscale[py][px]
                            count++
                        }
                    }
                }
                
                descriptor.add(if (count > 0) regionSum / count else 0f)
            }
        }
        
        return descriptor
    }
    
    private fun applyAdaptiveThreshold(grayscale: Array<IntArray>): Array<BooleanArray> {
        val height = grayscale.size
        val width = grayscale[0].size
        val binary = Array(height) { BooleanArray(width) }
        
        for (y in 0 until height) {
            for (x in 0 until width) {
                val threshold = calculateLocalThreshold(grayscale, x, y, 15)
                binary[y][x] = grayscale[y][x] > threshold
            }
        }
        
        return binary
    }
    
    private fun calculateLocalThreshold(grayscale: Array<IntArray>, x: Int, y: Int, windowSize: Int): Int {
        var sum = 0
        var count = 0
        val halfWindow = windowSize / 2
        
        for (dy in -halfWindow..halfWindow) {
            for (dx in -halfWindow..halfWindow) {
                val ny = y + dy
                val nx = x + dx
                
                if (ny >= 0 && ny < grayscale.size && nx >= 0 && nx < grayscale[0].size) {
                    sum += grayscale[ny][nx]
                    count++
                }
            }
        }
        
        return if (count > 0) sum / count else 128
    }
    
    private fun detectTextRegions(binaryImage: Array<BooleanArray>): List<TextRegion> {
        val regions = mutableListOf<TextRegion>()
        val visited = Array(binaryImage.size) { BooleanArray(binaryImage[0].size) }
        
        for (y in binaryImage.indices) {
            for (x in binaryImage[0].indices) {
                if (binaryImage[y][x] && !visited[y][x]) {
                    val region = floodFillTextRegion(binaryImage, visited, x, y)
                    if (region.width >= TEXT_BLOCK_MIN_SIZE && region.height >= TEXT_BLOCK_MIN_SIZE) {
                        regions.add(region)
                    }
                }
            }
        }
        
        return regions
    }
    
    private fun floodFillTextRegion(binaryImage: Array<BooleanArray>, visited: Array<BooleanArray>, startX: Int, startY: Int): TextRegion {
        val stack = mutableListOf<Pair<Int, Int>>()
        stack.add(Pair(startX, startY))
        
        var minX = startX
        var maxX = startX
        var minY = startY
        var maxY = startY
        var pixelCount = 0
        
        while (stack.isNotEmpty()) {
            val (x, y) = stack.removeAt(stack.size - 1)
            
            if (x < 0 || x >= binaryImage[0].size || y < 0 || y >= binaryImage.size || 
                visited[y][x] || !binaryImage[y][x]) {
                continue
            }
            
            visited[y][x] = true
            pixelCount++
            
            minX = minOf(minX, x)
            maxX = maxOf(maxX, x)
            minY = minOf(minY, y)
            maxY = maxOf(maxY, y)
            
            stack.add(Pair(x + 1, y))
            stack.add(Pair(x - 1, y))
            stack.add(Pair(x, y + 1))
            stack.add(Pair(x, y - 1))
        }
        
        return TextRegion(minX, minY, maxX - minX + 1, maxY - minY + 1, pixelCount)
    }
    
    private fun calculateLayoutFeatures(textRegions: List<TextRegion>, imageWidth: Int, imageHeight: Int): LayoutFeatures {
        val totalArea = imageWidth * imageHeight
        val totalTextArea = textRegions.sumOf { it.area }
        
        // Calculate region distributions
        val topRegions = textRegions.filter { it.y < imageHeight / 3 }
        val centerRegions = textRegions.filter { it.y >= imageHeight / 3 && it.y < 2 * imageHeight / 3 }
        val bottomRegions = textRegions.filter { it.y >= 2 * imageHeight / 3 }
        val leftRegions = textRegions.filter { it.x < imageWidth / 3 }
        val rightRegions = textRegions.filter { it.x >= 2 * imageWidth / 3 }
        
        val avgBlockSize = if (textRegions.isNotEmpty()) textRegions.map { it.area }.average().toFloat() else 0f
        
        return LayoutFeatures(
            topTextRatio = topRegions.sumOf { it.area }.toFloat() / totalArea,
            centerTextRatio = centerRegions.sumOf { it.area }.toFloat() / totalArea,
            bottomTextRatio = bottomRegions.sumOf { it.area }.toFloat() / totalArea,
            leftTextRatio = leftRegions.sumOf { it.area }.toFloat() / totalArea,
            rightTextRatio = rightRegions.sumOf { it.area }.toFloat() / totalArea,
            textDensity = totalTextArea.toFloat() / totalArea,
            averageTextBlockSize = avgBlockSize,
            textBlockCount = textRegions.size
        )
    }
    
    private fun applySobelEdgeDetection(grayscale: Array<IntArray>): Array<IntArray> {
        val height = grayscale.size
        val width = grayscale[0].size
        val edges = Array(height) { IntArray(width) }
        
        val sobelX = arrayOf(
            intArrayOf(-1, 0, 1),
            intArrayOf(-2, 0, 2),
            intArrayOf(-1, 0, 1)
        )
        
        val sobelY = arrayOf(
            intArrayOf(-1, -2, -1),
            intArrayOf(0, 0, 0),
            intArrayOf(1, 2, 1)
        )
        
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                var gx = 0
                var gy = 0
                
                for (dy in -1..1) {
                    for (dx in -1..1) {
                        val pixel = grayscale[y + dy][x + dx]
                        gx += pixel * sobelX[dy + 1][dx + 1]
                        gy += pixel * sobelY[dy + 1][dx + 1]
                    }
                }
                
                val magnitude = sqrt((gx * gx + gy * gy).toDouble()).toInt()
                edges[y][x] = minOf(magnitude, 255)
            }
        }
        
        return edges
    }
    
    private fun calculateEdgeStatistics(edges: Array<IntArray>): List<Float> {
        val flatEdges = edges.flatten()
        val totalPixels = flatEdges.size
        val edgePixels = flatEdges.count { it > EDGE_THRESHOLD }
        
        val edgeDensity = edgePixels.toFloat() / totalPixels
        val averageEdgeMagnitude = flatEdges.filter { it > EDGE_THRESHOLD }.average().toFloat()
        val maxEdgeMagnitude = flatEdges.maxOrNull()?.toFloat() ?: 0f
        
        return listOf(edgeDensity, averageEdgeMagnitude, maxEdgeMagnitude)
    }
    
    private fun generateEdgeHistogram(edges: Array<IntArray>): List<Float> {
        val histogram = IntArray(8) // 8 orientation bins
        val totalEdges = edges.sumOf { row -> row.count { it > EDGE_THRESHOLD } }
        
        // Simplified edge orientation histogram
        for (y in 1 until edges.size - 1) {
            for (x in 1 until edges[0].size - 1) {
                if (edges[y][x] > EDGE_THRESHOLD) {
                    val gx = edges[y][x + 1] - edges[y][x - 1]
                    val gy = edges[y + 1][x] - edges[y - 1][x]
                    val angle = atan2(gy.toDouble(), gx.toDouble())
                    val bin = ((angle + PI) / (2 * PI) * 8).toInt().coerceIn(0, 7)
                    histogram[bin]++
                }
            }
        }
        
        return histogram.map { if (totalEdges > 0) it.toFloat() / totalEdges else 0f }
    }
    
    private fun findContours(edges: Array<IntArray>): List<List<Point>> {
        // Simplified contour detection
        val contours = mutableListOf<List<Point>>()
        val visited = Array(edges.size) { BooleanArray(edges[0].size) }
        
        for (y in edges.indices) {
            for (x in edges[0].indices) {
                if (edges[y][x] > EDGE_THRESHOLD && !visited[y][x]) {
                    val contour = traceContour(edges, visited, x, y)
                    if (contour.size > 10) { // Minimum contour size
                        contours.add(contour)
                    }
                }
            }
        }
        
        return contours
    }
    
    private fun traceContour(edges: Array<IntArray>, visited: Array<BooleanArray>, startX: Int, startY: Int): List<Point> {
        val contour = mutableListOf<Point>()
        val stack = mutableListOf<Point>()
        stack.add(Point(startX, startY))
        
        while (stack.isNotEmpty()) {
            val point = stack.removeAt(stack.size - 1)
            val x = point.x
            val y = point.y
            
            if (x < 0 || x >= edges[0].size || y < 0 || y >= edges.size || 
                visited[y][x] || edges[y][x] <= EDGE_THRESHOLD) {
                continue
            }
            
            visited[y][x] = true
            contour.add(point)
            
            // Add 8-connected neighbors
            for (dy in -1..1) {
                for (dx in -1..1) {
                    if (dx != 0 || dy != 0) {
                        stack.add(Point(x + dx, y + dy))
                    }
                }
            }
        }
        
        return contour
    }
    
    private fun calculateShapeDescriptors(contours: List<List<Point>>): List<Float> {
        val descriptors = mutableListOf<Float>()
        
        for (contour in contours) {
            if (contour.isEmpty()) continue
            
            // Calculate basic shape features
            val area = contour.size.toFloat()
            val perimeter = calculatePerimeter(contour)
            val circularity = if (perimeter > 0) 4 * PI.toFloat() * area / (perimeter * perimeter) else 0f
            
            val boundingBox = calculateBoundingBox(contour)
            val aspectRatio = boundingBox.width.toFloat() / boundingBox.height.toFloat()
            
            descriptors.addAll(listOf(area, perimeter, circularity, aspectRatio))
        }
        
        return descriptors
    }
    
    private fun calculatePerimeter(contour: List<Point>): Float {
        var perimeter = 0f
        for (i in 0 until contour.size - 1) {
            val dx = contour[i + 1].x - contour[i].x
            val dy = contour[i + 1].y - contour[i].y
            perimeter += sqrt((dx * dx + dy * dy).toDouble()).toFloat()
        }
        return perimeter
    }
    
    private fun calculateBoundingBox(contour: List<Point>): Rectangle {
        val minX = contour.minOfOrNull { it.x } ?: 0
        val maxX = contour.maxOfOrNull { it.x } ?: 0
        val minY = contour.minOfOrNull { it.y } ?: 0
        val maxY = contour.maxOfOrNull { it.y } ?: 0
        
        return Rectangle(minX, minY, maxX - minX, maxY - minY)
    }
    
    private fun calculateLBPHistogram(grayscale: Array<IntArray>): List<Float> {
        val histogram = IntArray(256) // LBP values 0-255
        val totalPixels = (grayscale.size - 2) * (grayscale[0].size - 2)
        
        for (y in 1 until grayscale.size - 1) {
            for (x in 1 until grayscale[0].size - 1) {
                val center = grayscale[y][x]
                var lbpValue = 0
                
                // Calculate LBP value for 8 neighbors
                val neighbors = listOf(
                    grayscale[y - 1][x - 1], grayscale[y - 1][x], grayscale[y - 1][x + 1],
                    grayscale[y][x + 1], grayscale[y + 1][x + 1], grayscale[y + 1][x],
                    grayscale[y + 1][x - 1], grayscale[y][x - 1]
                )
                
                for (i in neighbors.indices) {
                    if (neighbors[i] >= center) {
                        lbpValue = lbpValue or (1 shl i)
                    }
                }
                
                histogram[lbpValue]++
            }
        }
        
        return histogram.map { it.toFloat() / totalPixels }
    }
    
    // Confidence calculation functions
    
    private fun calculateKeypointConfidence(keypoints: List<Keypoint>, imageArea: Int): Float {
        val keypointDensity = keypoints.size.toFloat() / imageArea * 10000 // Per 10k pixels
        return minOf(keypointDensity / 10f, 1f) // Normalize to 0-1
    }
    
    private fun calculateColorVariance(histogram: List<Float>): Float {
        val mean = histogram.average().toFloat()
        val variance = histogram.map { (it - mean) * (it - mean) }.average().toFloat()
        return sqrt(variance)
    }
    
    private fun calculateTextLayoutConfidence(layoutFeatures: LayoutFeatures): Float {
        // Higher confidence for more structured text layouts
        val structureScore = layoutFeatures.textDensity * 0.5f +
                           (layoutFeatures.textBlockCount / 10f).coerceAtMost(0.3f) +
                           layoutFeatures.averageTextBlockSize / 1000f * 0.2f
        return minOf(structureScore, 1f)
    }
    
    private fun calculateEdgeConfidence(edgeStatistics: List<Float>): Float {
        // Higher confidence for images with clear edges
        val edgeDensity = edgeStatistics[0]
        val edgeStrength = edgeStatistics[1] / 255f
        return minOf(edgeDensity * 0.7f + edgeStrength * 0.3f, 1f)
    }
    
    private fun calculateShapeConfidence(contours: List<List<Point>>, imageArea: Int): Float {
        val totalContourPoints = contours.sumOf { it.size }
        val shapeComplexity = totalContourPoints.toFloat() / imageArea
        return minOf(shapeComplexity * 1000f, 1f)
    }
    
    private fun calculateTextureConfidence(lbpHistogram: List<Float>): Float {
        // Higher confidence for more varied texture patterns
        val entropy = -lbpHistogram.filter { it > 0 }.sumOf { it * ln(it.toDouble()) }.toFloat()
        return minOf(entropy / 8f, 1f) // Normalize by max entropy
    }
    
    private fun calculateAverageBrightness(bitmap: Bitmap): Float {
        var sum = 0L
        val totalPixels = bitmap.width * bitmap.height
        
        for (x in 0 until bitmap.width) {
            for (y in 0 until bitmap.height) {
                val pixel = bitmap.getPixel(x, y)
                val brightness = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3
                sum += brightness
            }
        }
        
        return sum.toFloat() / totalPixels / 255f
    }
    
    private fun calculateContrast(bitmap: Bitmap): Float {
        val avgBrightness = calculateAverageBrightness(bitmap) * 255f
        var varianceSum = 0.0
        val totalPixels = bitmap.width * bitmap.height
        
        for (x in 0 until bitmap.width) {
            for (y in 0 until bitmap.height) {
                val pixel = bitmap.getPixel(x, y)
                val brightness = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3
                val diff = brightness - avgBrightness
                varianceSum += diff * diff
            }
        }
        
        val variance = varianceSum / totalPixels
        return sqrt(variance).toFloat() / 255f
    }
}

// Data classes for advanced visual features

data class Keypoint(
    val x: Int,
    val y: Int,
    val octave: Int,
    val level: Int,
    val response: Float
)

data class TextRegion(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
    val area: Int
)

data class LayoutFeatures(
    val topTextRatio: Float,
    val centerTextRatio: Float,
    val bottomTextRatio: Float,
    val leftTextRatio: Float,
    val rightTextRatio: Float,
    val textDensity: Float,
    val averageTextBlockSize: Float,
    val textBlockCount: Int
)

data class Point(val x: Int, val y: Int)

data class Rectangle(val x: Int, val y: Int, val width: Int, val height: Int)
