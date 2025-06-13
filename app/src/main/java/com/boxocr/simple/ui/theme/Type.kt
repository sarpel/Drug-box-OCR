package com.boxocr.simple.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Enhanced Typography for Turkish Medical Application
 * Optimized for Turkish language characteristics and medical terminology
 * Supports both phone and tablet layouts with accessibility considerations
 */

// Font families optimized for Turkish characters
val TurkishMedicalFontFamily = FontFamily.Default

// Alternative font for better Turkish character support
val TurkishSystemFontFamily = FontFamily.Default

// Main typography for phones/standard screens
val TurkishMedicalTypography = Typography(
    // Display styles for large headers
    displayLarge = TextStyle(
        fontFamily = TurkishMedicalFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = TurkishMedicalFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = TurkishMedicalFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    
    // Headline styles for page titles and sections
    headlineLarge = TextStyle(
        fontFamily = TurkishMedicalFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = TurkishMedicalFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = TurkishMedicalFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    
    // Title styles for cards and components
    titleLarge = TextStyle(
        fontFamily = TurkishMedicalFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = TurkishMedicalFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = TurkishMedicalFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    
    // Body text for content
    bodyLarge = TextStyle(
        fontFamily = TurkishSystemFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = TurkishSystemFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = TurkishSystemFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    
    // Label styles for buttons and small text
    labelLarge = TextStyle(
        fontFamily = TurkishMedicalFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = TurkishMedicalFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = TurkishMedicalFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp
    )
)

// Enhanced typography for tablets with larger screens
val TurkishMedicalTabletTypography = Typography(
    // Display styles - larger for tablets
    displayLarge = TextStyle(
        fontFamily = TurkishMedicalFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 72.sp, // +15sp for tablets
        lineHeight = 80.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = TurkishMedicalFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 56.sp, // +11sp for tablets
        lineHeight = 64.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = TurkishMedicalFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 44.sp, // +8sp for tablets
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    
    // Headline styles - enhanced for tablet reading
    headlineLarge = TextStyle(
        fontFamily = TurkishMedicalFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp, // +8sp for tablets
        lineHeight = 48.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = TurkishMedicalFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp, // +6sp for tablets
        lineHeight = 42.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = TurkishMedicalFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp, // +4sp for tablets
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    
    // Title styles - optimized for tablet cards
    titleLarge = TextStyle(
        fontFamily = TurkishMedicalFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 26.sp, // +4sp for tablets
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = TurkishMedicalFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 21.sp, // +3sp for tablets
        lineHeight = 28.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = TurkishMedicalFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp, // +2sp for tablets
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp
    ),
    
    // Body text - enhanced readability for tablets
    bodyLarge = TextStyle(
        fontFamily = TurkishSystemFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp, // +2sp for tablets
        lineHeight = 28.sp,
        letterSpacing = 0.15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = TurkishSystemFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp, // +2sp for tablets
        lineHeight = 24.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = TurkishSystemFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp, // +2sp for tablets
        lineHeight = 20.sp,
        letterSpacing = 0.4.sp
    ),
    
    // Label styles - larger touch targets for tablets
    labelLarge = TextStyle(
        fontFamily = TurkishMedicalFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp, // +2sp for tablets
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = TurkishMedicalFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp, // +2sp for tablets
        lineHeight = 20.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = TurkishMedicalFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp, // +2sp for tablets
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

// Export the main typography as AppTypography
val AppTypography = TurkishMedicalTypography

// Special typography styles for medical content
object MedicalTypography {
    // Drug name styling - prominent and clear
    val DrugName = TextStyle(
        fontFamily = TurkishMedicalFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    )
    
    // Active ingredient styling - scientific terminology
    val ActiveIngredient = TextStyle(
        fontFamily = TurkishSystemFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    )
    
    // Dosage and quantity styling - precise and readable
    val Dosage = TextStyle(
        fontFamily = TurkishSystemFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    )
    
    // Price styling - monetary values
    val Price = TextStyle(
        fontFamily = TurkishMedicalFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    )
    
    // Confidence score styling - status indicators
    val Confidence = TextStyle(
        fontFamily = TurkishMedicalFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    )
    
    // Barcode styling - monospace-like for codes
    val Barcode = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )
    
    // Error message styling - attention grabbing but not alarming
    val ErrorMessage = TextStyle(
        fontFamily = TurkishSystemFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    )
    
    // Success message styling - positive reinforcement
    val SuccessMessage = TextStyle(
        fontFamily = TurkishSystemFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    )
    
    // Prescription ID styling - unique identifier emphasis
    val PrescriptionId = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp
    )
    
    // Patient information styling - sensitive data
    val PatientInfo = TextStyle(
        fontFamily = TurkishSystemFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    )
}

// Accessibility typography variants
object AccessibilityTypography {
    // Large text for vision accessibility
    fun scaledTypography(scale: Float): Typography {
        return TurkishMedicalTypography.copy(
            displayLarge = TurkishMedicalTypography.displayLarge.copy(fontSize = (57 * scale).sp),
            displayMedium = TurkishMedicalTypography.displayMedium.copy(fontSize = (45 * scale).sp),
            displaySmall = TurkishMedicalTypography.displaySmall.copy(fontSize = (36 * scale).sp),
            headlineLarge = TurkishMedicalTypography.headlineLarge.copy(fontSize = (32 * scale).sp),
            headlineMedium = TurkishMedicalTypography.headlineMedium.copy(fontSize = (28 * scale).sp),
            headlineSmall = TurkishMedicalTypography.headlineSmall.copy(fontSize = (24 * scale).sp),
            titleLarge = TurkishMedicalTypography.titleLarge.copy(fontSize = (22 * scale).sp),
            titleMedium = TurkishMedicalTypography.titleMedium.copy(fontSize = (18 * scale).sp),
            titleSmall = TurkishMedicalTypography.titleSmall.copy(fontSize = (16 * scale).sp),
            bodyLarge = TurkishMedicalTypography.bodyLarge.copy(fontSize = (16 * scale).sp),
            bodyMedium = TurkishMedicalTypography.bodyMedium.copy(fontSize = (14 * scale).sp),
            bodySmall = TurkishMedicalTypography.bodySmall.copy(fontSize = (12 * scale).sp),
            labelLarge = TurkishMedicalTypography.labelLarge.copy(fontSize = (14 * scale).sp),
            labelMedium = TurkishMedicalTypography.labelMedium.copy(fontSize = (12 * scale).sp),
            labelSmall = TurkishMedicalTypography.labelSmall.copy(fontSize = (10 * scale).sp)
        )
    }
    
    // High contrast typography
    val HighContrastTypography = TurkishMedicalTypography.copy(
        // All text becomes bold for better contrast
        bodyLarge = TurkishMedicalTypography.bodyLarge.copy(fontWeight = FontWeight.Bold),
        bodyMedium = TurkishMedicalTypography.bodyMedium.copy(fontWeight = FontWeight.Bold),
        bodySmall = TurkishMedicalTypography.bodySmall.copy(fontWeight = FontWeight.Bold),
        labelLarge = TurkishMedicalTypography.labelLarge.copy(fontWeight = FontWeight.Bold),
        labelMedium = TurkishMedicalTypography.labelMedium.copy(fontWeight = FontWeight.Bold),
        labelSmall = TurkishMedicalTypography.labelSmall.copy(fontWeight = FontWeight.Bold)
    )
}

// Turkish language specific adjustments
object TurkishLanguageSupport {
    // Letter spacing adjustments for Turkish characters (ç, ğ, ı, ö, ş, ü)
    const val TurkishLetterSpacing = 0.05f // Slightly more spacing for Turkish chars
    
    // Line height adjustments for better Turkish text rendering
    const val TurkishLineHeightMultiplier = 1.1f // 10% more line height
    
    // Typography specifically optimized for Turkish medical terminology
    val TurkishMedicalOptimized = TurkishMedicalTypography.copy(
        bodyLarge = TurkishMedicalTypography.bodyLarge.copy(
            letterSpacing = (0.15 + TurkishLetterSpacing).sp,
            lineHeight = (24 * TurkishLineHeightMultiplier).sp
        ),
        bodyMedium = TurkishMedicalTypography.bodyMedium.copy(
            letterSpacing = (0.25 + TurkishLetterSpacing).sp,
            lineHeight = (20 * TurkishLineHeightMultiplier).sp
        ),
        bodySmall = TurkishMedicalTypography.bodySmall.copy(
            letterSpacing = (0.4 + TurkishLetterSpacing).sp,
            lineHeight = (16 * TurkishLineHeightMultiplier).sp
        )
    )
}
