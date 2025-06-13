package com.boxocr.simple.repository

import android.graphics.Bitmap
import android.graphics.Color
import com.boxocr.simple.database.VisualFeatureType
import com.boxocr.simple.data.FeatureData
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
    private const val SIFT_SCALES_PER_OCTAVE = 3
    private const val GAUSSIAN_SIGMA = 1.6f
    private const val CONTRAST_THRESHOLD = 0.04f
    private const val EDGE_THRESHOLD = 10.0f
    private const val DESCRIPTOR_SIZE = 128
    private const val HISTOGRAM_BINS = 64
    private const val SOBEL_THRESHOLD = 50
    private const val LBP_RADIUS = 1
    private const val LBP_POINTS = 8

    /**
     * Extract all visual features from a drug box image
     * Returns a map of feature types to their corresponding feature data
     */
    fun extractAllFeatures(bitmap: Bitmap): Map<VisualFeatureType, FeatureData> {
        val features = mutableMapOf<VisualFeatureType, FeatureData>()
        
        try {
            features[VisualFeatureType.SIFT_FEATURES] = extractSIFTFeatures(bitmap)
            features[VisualFeatureType.COLOR_HISTOGRAM] = extractColorHistogram(bitmap)
            features[VisualFeatureType.TEXT_LAYOUT] = extractTextLayoutFeatures(bitmap)
            features[VisualFeatureType.EDGE_FEATURES] = extractEdgeFeatures(bitmap)
            features[VisualFeatureType.SHAPE_FEATURES] = extractShapeFeatures(bitmap)
            features[VisualFeatureType.TEXTURE_FEATURES] = extractTextureFeatures(bitmap)
        } catch (e: Exception) {
            // Log error and continue with available features
            android.util.Log.w("VisualFeatureExtractor", "Error extracting features", e)
        }
        
        return features
    }

    /**
     * Extract SIFT-inspired keypoint features
     * Returns 128-dimensional descriptor vector
     */
    fun extractSIFTFeatures(bitmap: Bitmap): FeatureData {
        val grayImage = convertToGrayscale(bitmap)
        val keypoints = detectKeypoints(grayImage)
        val descriptors = computeDescriptors(grayImage, keypoints)
        
        // Aggregate descriptors into fixed-size feature vector
        val featureVector = aggregateDescriptors(descriptors)
        
        return FeatureData(
            data = featureVector,
            type = "SIFT",
            confidence = calculateKeypointConfidence(keypoints)
        )
    }

    /**
     * Extract color histogram features
     * Returns 192-dimensional vector (64 bins per RGB channel)
     */
    fun extractColorHistogram(bitmap: Bitmap): FeatureData {
        val rHist = IntArray(HISTOGRAM_BINS)
        val gHist = IntArray(HISTOGRAM_BINS)
        val bHist = IntArray(HISTOGRAM_BINS)
        
        val width = bitmap.width
        val height = bitmap.height
        val totalPixels = width * height
        
        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixel = bitmap.getPixel(x, y)
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)
                
                rHist[r * HISTOGRAM_BINS / 256]++
                gHist[g * HISTOGRAM_BINS / 256]++
                bHist[b * HISTOGRAM_BINS / 256]++
            }
        }
        
        // Normalize histograms
        val normalizedHist = FloatArray(HISTOGRAM_BINS * 3)
        for (i in 0 until HISTOGRAM_BINS) {
            normalizedHist[i] = rHist[i].toFloat() / totalPixels
            normalizedHist[i + HISTOGRAM_BINS] = gHist[i].toFloat() / totalPixels
            normalizedHist[i + HISTOGRAM_BINS * 2] = bHist[i].toFloat() / totalPixels
        }
        
        return FeatureData(
            data = normalizedHist.toList(),
            type = "COLOR_HISTOGRAM",
            confidence = calculateHistogramConfidence(normalizedHist)
        )
    }

    /**
     * Extract text layout features
     * Returns spatial distribution of text regions
     */
    fun extractTextLayoutFeatures(bitmap: Bitmap): FeatureData {
        val textRegions = detectTextRegions(bitmap)
        val layoutFeatures = computeLayoutStatistics(textRegions, bitmap.width, bitmap.height)
        
        return FeatureData(
            data = layoutFeatures,
            type = "TEXT_LAYOUT",
            confidence = if (textRegions.isNotEmpty()) 0.8f else 0.2f
        )
    }

    /**
     * Extract edge features using Sobel operator
     * Returns edge orientation histogram
     */
    fun extractEdgeFeatures(bitmap: Bitmap): FeatureData {
        val grayImage = convertToGrayscale(bitmap)
        val (gradientX, gradientY) = computeSobelGradients(grayImage)
        val orientationHist = computeOrientationHistogram(gradientX, gradientY)
        
        return FeatureData(
            data = orientationHist,
            type = "EDGE_FEATURES",
            confidence = calculateEdgeConfidence(gradientX, gradientY)
        )
    }

    /**
     * Extract shape features from contours
     * Returns geometric descriptors
     */
    fun extractShapeFeatures(bitmap: Bitmap): FeatureData {
        val contours = detectContours(bitmap)
        val shapeDescriptors = computeShapeDescriptors(contours)
        
        return FeatureData(
            data = shapeDescriptors,
            type = "SHAPE_FEATURES",
            confidence = if (contours.isNotEmpty()) 0.7f else 0.1f
        )
    }

    /**
     * Extract texture features using Local Binary Patterns
     * Returns LBP histogram
     */
    fun extractTextureFeatures(bitmap: Bitmap): FeatureData {
        val grayImage = convertToGrayscale(bitmap)
        val lbpHistogram = computeLBPHistogram(grayImage)
        
        return FeatureData(
            data = lbpHistogram,
            type = "TEXTURE_FEATURES",
            confidence = calculateTextureConfidence(lbpHistogram)
        )
    }

    // ============== PRIVATE HELPER METHODS ==============

    private fun convertToGrayscale(bitmap: Bitmap): Array<FloatArray> {
        val width = bitmap.width
        val height = bitmap.height
        val grayImage = Array(height) { FloatArray(width) }
        
        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixel = bitmap.getPixel(x, y)
                val gray = (Color.red(pixel) * 0.299 + Color.green(pixel) * 0.587 + Color.blue(pixel) * 0.114).toFloat()
                grayImage[y][x] = gray / 255.0f
            }
        }
        
        return grayImage
    }

    private fun detectKeypoints(grayImage: Array<FloatArray>): List<Keypoint> {
        val keypoints = mutableListOf<Keypoint>()
        val height = grayImage.size
        val width = grayImage[0].size
        
        // Simplified corner detection (Harris-like)
        for (y in 2 until height - 2) {
            for (x in 2 until width - 2) {
                val response = computeCornerResponse(grayImage, x, y)
                if (response > CONTRAST_THRESHOLD) {
                    keypoints.add(Keypoint(x.toFloat(), y.toFloat(), response))
                }
            }
        }
        
        return keypoints.sortedByDescending { it.response }.take(100) // Keep top 100 keypoints
    }

    private fun computeCornerResponse(image: Array<FloatArray>, x: Int, y: Int): Float {
        var Ixx = 0.0f
        var Ixy = 0.0f
        var Iyy = 0.0f
        
        for (dy in -1..1) {
            for (dx in -1..1) {
                val ix = (image[y][x + 1] - image[y][x - 1]) / 2.0f
                val iy = (image[y + 1][x] - image[y - 1][x]) / 2.0f
                
                Ixx += ix * ix
                Ixy += ix * iy
                Iyy += iy * iy
            }
        }
        
        val det = Ixx * Iyy - Ixy * Ixy
        val trace = Ixx + Iyy
        
        return det - 0.04f * trace * trace
    }

    private fun computeDescriptors(image: Array<FloatArray>, keypoints: List<Keypoint>): List<FloatArray> {
        val descriptors = mutableListOf<FloatArray>()
        
        for (keypoint in keypoints) {
            val descriptor = computeKeypointDescriptor(image, keypoint)
            if (descriptor != null) {
                descriptors.add(descriptor)
            }
        }
        
        return descriptors
    }

    private fun computeKeypointDescriptor(image: Array<FloatArray>, keypoint: Keypoint): FloatArray? {
        val x = keypoint.x.toInt()
        val y = keypoint.y.toInt()
        val patchSize = 16
        val halfPatch = patchSize / 2
        
        if (x < halfPatch || y < halfPatch || 
            x >= image[0].size - halfPatch || y >= image.size - halfPatch) {
            return null
        }
        
        val descriptor = FloatArray(DESCRIPTOR_SIZE)
        var index = 0
        
        // Simplified 128-dimensional descriptor
        for (dy in -halfPatch until halfPatch step 2) {
            for (dx in -halfPatch until halfPatch step 2) {
                if (index < DESCRIPTOR_SIZE) {
                    val intensity = image[y + dy][x + dx]
                    descriptor[index++] = intensity
                }
            }
        }
        
        // Normalize descriptor
        val norm = sqrt(descriptor.sumOf { it * it }.toFloat())
        if (norm > 0) {
            for (i in descriptor.indices) {
                descriptor[i] /= norm
            }
        }
        
        return descriptor
    }

    private fun aggregateDescriptors(descriptors: List<FloatArray>): List<Float> {
        if (descriptors.isEmpty()) {
            return List(DESCRIPTOR_SIZE) { 0.0f }
        }
        
        val aggregated = FloatArray(DESCRIPTOR_SIZE)
        
        // Average pooling
        for (descriptor in descriptors) {
            for (i in descriptor.indices) {
                if (i < aggregated.size) {
                    aggregated[i] += descriptor[i]
                }
            }
        }
        
        val count = descriptors.size.toFloat()
        return aggregated.map { it / count }
    }

    private fun calculateKeypointConfidence(keypoints: List<Keypoint>): Float {
        if (keypoints.isEmpty()) return 0.0f
        
        val avgResponse = keypoints.map { it.response }.average().toFloat()
        return minOf(1.0f, avgResponse * 10.0f) // Scale to 0-1
    }

    private fun calculateHistogramConfidence(histogram: FloatArray): Float {
        // Calculate entropy as confidence measure
        var entropy = 0.0f
        for (bin in histogram) {
            if (bin > 0) {
                entropy -= bin * ln(bin)
            }
        }
        return minOf(1.0f, entropy / 5.0f) // Normalize to 0-1
    }

    private fun detectTextRegions(bitmap: Bitmap): List<TextRegion> {
        // Simplified text region detection
        val regions = mutableListOf<TextRegion>()
        val width = bitmap.width
        val height = bitmap.height
        
        // Grid-based text detection
        val gridSize = 32
        for (y in 0 until height step gridSize) {
            for (x in 0 until width step gridSize) {
                val endX = minOf(x + gridSize, width)
                val endY = minOf(y + gridSize, height)
                
                if (isTextRegion(bitmap, x, y, endX, endY)) {
                    regions.add(TextRegion(x, y, endX - x, endY - y))
                }
            }
        }
        
        return regions
    }

    private fun isTextRegion(bitmap: Bitmap, startX: Int, startY: Int, endX: Int, endY: Int): Boolean {
        var edgeCount = 0
        var totalPixels = 0
        
        for (y in startY until endY) {
            for (x in startX until endX) {
                if (x > 0 && x < bitmap.width - 1 && y > 0 && y < bitmap.height - 1) {
                    val current = getGrayValue(bitmap.getPixel(x, y))
                    val right = getGrayValue(bitmap.getPixel(x + 1, y))
                    val bottom = getGrayValue(bitmap.getPixel(x, y + 1))
                    
                    val edgeStrength = abs(current - right) + abs(current - bottom)
                    if (edgeStrength > 30) edgeCount++
                    totalPixels++
                }
            }
        }
        
        return totalPixels > 0 && (edgeCount.toFloat() / totalPixels) > 0.15f
    }

    private fun getGrayValue(pixel: Int): Int {
        return (Color.red(pixel) * 0.299 + Color.green(pixel) * 0.587 + Color.blue(pixel) * 0.114).toInt()
    }

    private fun computeLayoutStatistics(regions: List<TextRegion>, width: Int, height: Int): List<Float> {
        if (regions.isEmpty()) {
            return List(8) { 0.0f }
        }
        
        // Compute spatial distribution statistics
        val xPositions = regions.map { it.x.toFloat() / width }
        val yPositions = regions.map { it.y.toFloat() / height }
        val widths = regions.map { it.width.toFloat() / width }
        val heights = regions.map { it.height.toFloat() / height }
        
        return listOf(
            xPositions.average().toFloat(),
            yPositions.average().toFloat(),
            widths.average().toFloat(),
            heights.average().toFloat(),
            xPositions.maxOrNull() ?: 0f,
            yPositions.maxOrNull() ?: 0f,
            xPositions.minOrNull() ?: 0f,
            yPositions.minOrNull() ?: 0f
        )
    }

    private fun computeSobelGradients(image: Array<FloatArray>): Pair<Array<FloatArray>, Array<FloatArray>> {
        val height = image.size
        val width = image[0].size
        val gradientX = Array(height) { FloatArray(width) }
        val gradientY = Array(height) { FloatArray(width) }
        
        // Sobel kernels
        val sobelX = arrayOf(
            floatArrayOf(-1f, 0f, 1f),
            floatArrayOf(-2f, 0f, 2f),
            floatArrayOf(-1f, 0f, 1f)
        )
        val sobelY = arrayOf(
            floatArrayOf(-1f, -2f, -1f),
            floatArrayOf(0f, 0f, 0f),
            floatArrayOf(1f, 2f, 1f)
        )
        
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                var gx = 0f
                var gy = 0f
                
                for (ky in -1..1) {
                    for (kx in -1..1) {
                        val pixel = image[y + ky][x + kx]
                        gx += pixel * sobelX[ky + 1][kx + 1]
                        gy += pixel * sobelY[ky + 1][kx + 1]
                    }
                }
                
                gradientX[y][x] = gx
                gradientY[y][x] = gy
            }
        }
        
        return Pair(gradientX, gradientY)
    }

    private fun computeOrientationHistogram(gradientX: Array<FloatArray>, gradientY: Array<FloatArray>): List<Float> {
        val bins = 8
        val histogram = FloatArray(bins)
        val height = gradientX.size
        val width = gradientX[0].size
        
        for (y in 0 until height) {
            for (x in 0 until width) {
                val gx = gradientX[y][x]
                val gy = gradientY[y][x]
                val magnitude = sqrt(gx * gx + gy * gy)
                
                if (magnitude > SOBEL_THRESHOLD) {
                    val orientation = atan2(gy, gx)
                    val normalizedOrientation = (orientation + PI) / (2 * PI)
                    val bin = (normalizedOrientation * bins).toInt().coerceIn(0, bins - 1)
                    histogram[bin] += magnitude
                }
            }
        }
        
        // Normalize histogram
        val total = histogram.sum()
        return if (total > 0) {
            histogram.map { it / total }
        } else {
            histogram.toList()
        }
    }

    private fun calculateEdgeConfidence(gradientX: Array<FloatArray>, gradientY: Array<FloatArray>): Float {
        var strongEdges = 0
        var totalPixels = 0
        
        for (y in gradientX.indices) {
            for (x in gradientX[0].indices) {
                val magnitude = sqrt(gradientX[y][x] * gradientX[y][x] + gradientY[y][x] * gradientY[y][x])
                if (magnitude > SOBEL_THRESHOLD) strongEdges++
                totalPixels++
            }
        }
        
        return if (totalPixels > 0) {
            (strongEdges.toFloat() / totalPixels).coerceIn(0f, 1f)
        } else 0f
    }

    private fun detectContours(bitmap: Bitmap): List<Contour> {
        // Simplified contour detection
        val contours = mutableListOf<Contour>()
        val width = bitmap.width
        val height = bitmap.height
        
        // Edge detection first
        val edges = Array(height) { BooleanArray(width) }
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val current = getGrayValue(bitmap.getPixel(x, y))
                val neighbors = listOf(
                    getGrayValue(bitmap.getPixel(x - 1, y)),
                    getGrayValue(bitmap.getPixel(x + 1, y)),
                    getGrayValue(bitmap.getPixel(x, y - 1)),
                    getGrayValue(bitmap.getPixel(x, y + 1))
                )
                
                val maxDiff = neighbors.maxOfOrNull { abs(current - it) } ?: 0
                edges[y][x] = maxDiff > 30
            }
        }
        
        // Simple contour following
        val visited = Array(height) { BooleanArray(width) }
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (edges[y][x] && !visited[y][x]) {
                    val contour = traceContour(edges, visited, x, y)
                    if (contour.points.size > 10) { // Minimum contour size
                        contours.add(contour)
                    }
                }
            }
        }
        
        return contours
    }

    private fun traceContour(edges: Array<BooleanArray>, visited: Array<BooleanArray>, startX: Int, startY: Int): Contour {
        val points = mutableListOf<Point>()
        val stack = mutableListOf(Point(startX, startY))
        
        while (stack.isNotEmpty() && points.size < 1000) { // Limit contour size
            val current = stack.removeAt(stack.lastIndex)
            if (current.x < 0 || current.x >= edges[0].size || 
                current.y < 0 || current.y >= edges.size ||
                visited[current.y][current.x] || !edges[current.y][current.x]) {
                continue
            }
            
            visited[current.y][current.x] = true
            points.add(current)
            
            // Add 8-connected neighbors
            for (dy in -1..1) {
                for (dx in -1..1) {
                    if (dx != 0 || dy != 0) {
                        stack.add(Point(current.x + dx, current.y + dy))
                    }
                }
            }
        }
        
        return Contour(points)
    }

    private fun computeShapeDescriptors(contours: List<Contour>): List<Float> {
        if (contours.isEmpty()) {
            return List(4) { 0.0f }
        }
        
        val descriptors = mutableListOf<Float>()
        
        for (contour in contours.take(5)) { // Analyze top 5 contours
            val area = contour.points.size.toFloat()
            val perimeter = estimatePerimeter(contour.points)
            val circularity = if (perimeter > 0) 4 * PI.toFloat() * area / (perimeter * perimeter) else 0f
            val aspectRatio = computeAspectRatio(contour.points)
            
            descriptors.addAll(listOf(area, perimeter, circularity, aspectRatio))
        }
        
        // Pad or truncate to fixed size
        return descriptors.take(20).let { list ->
            list + List(maxOf(0, 20 - list.size)) { 0.0f }
        }
    }

    private fun estimatePerimeter(points: List<Point>): Float {
        if (points.size < 2) return 0f
        
        var perimeter = 0f
        for (i in 0 until points.size - 1) {
            val dx = points[i + 1].x - points[i].x
            val dy = points[i + 1].y - points[i].y
            perimeter += sqrt((dx * dx + dy * dy).toFloat())
        }
        
        return perimeter
    }

    private fun computeAspectRatio(points: List<Point>): Float {
        if (points.isEmpty()) return 1f
        
        val minX = points.minOfOrNull { it.x } ?: 0
        val maxX = points.maxOfOrNull { it.x } ?: 0
        val minY = points.minOfOrNull { it.y } ?: 0
        val maxY = points.maxOfOrNull { it.y } ?: 0
        
        val width = maxX - minX + 1
        val height = maxY - minY + 1
        
        return if (height > 0) width.toFloat() / height.toFloat() else 1f
    }

    private fun computeLBPHistogram(image: Array<FloatArray>): List<Float> {
        val histogram = IntArray(256) // LBP values range from 0-255
        val height = image.size
        val width = image[0].size
        
        for (y in LBP_RADIUS until height - LBP_RADIUS) {
            for (x in LBP_RADIUS until width - LBP_RADIUS) {
                val lbpValue = computeLBPValue(image, x, y)
                histogram[lbpValue]++
            }
        }
        
        // Normalize histogram
        val total = histogram.sum()
        return if (total > 0) {
            histogram.map { it.toFloat() / total }
        } else {
            histogram.map { 0f }
        }
    }

    private fun computeLBPValue(image: Array<FloatArray>, x: Int, y: Int): Int {
        val center = image[y][x]
        var lbpValue = 0
        
        // Sample points around the center
        val angles = (0 until LBP_POINTS).map { it * 2 * PI / LBP_POINTS }
        
        for (i in angles.indices) {
            val angle = angles[i]
            val sampleX = x + (LBP_RADIUS * cos(angle)).roundToInt()
            val sampleY = y + (LBP_RADIUS * sin(angle)).roundToInt()
            
            if (sampleX >= 0 && sampleX < image[0].size && sampleY >= 0 && sampleY < image.size) {
                val sampleValue = image[sampleY][sampleX]
                if (sampleValue >= center) {
                    lbpValue = lbpValue or (1 shl i)
                }
            }
        }
        
        return lbpValue
    }

    private fun calculateTextureConfidence(histogram: List<Float>): Float {
        // Calculate uniformity as confidence measure
        val uniformity = histogram.sumOf { it * it }.toFloat()
        return (1.0f - uniformity).coerceIn(0f, 1f)
    }

    // ============== DATA CLASSES ==============

    data class Keypoint(
        val x: Float,
        val y: Float,
        val response: Float
    )

    data class TextRegion(
        val x: Int,
        val y: Int,
        val width: Int,
        val height: Int
    )

    data class Point(
        val x: Int,
        val y: Int
    )

    data class Contour(
        val points: List<Point>
    )
}
