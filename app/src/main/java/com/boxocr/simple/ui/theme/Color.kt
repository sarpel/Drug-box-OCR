package com.boxocr.simple.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Enhanced Material 3 Color Scheme for Turkish Medical Application
 * Supports both light and dark modes with Turkish design preferences
 */

// Turkish Medical Colors - Light Theme
private val TurkishMedicalLightColors = lightColorScheme(
    primary = Color(0xFF0D47A1), // Turkish Blue - Professional medical blue
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFBBDEFB), // Light blue container
    onPrimaryContainer = Color(0xFF0D47A1),
    
    secondary = Color(0xFFD32F2F), // Turkish Red - Medical alert red
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFCDD2), // Light red container
    onSecondaryContainer = Color(0xFFD32F2F),
    
    tertiary = Color(0xFF388E3C), // Medical green - Success/active
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFC8E6C9), // Light green container
    onTertiaryContainer = Color(0xFF388E3C),
    
    error = Color(0xFFD32F2F), // Medical error red
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFCDD2),
    onErrorContainer = Color(0xFFD32F2F),
    
    background = Color(0xFFFAFAFA), // Clean medical white
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1C1B1F),
    
    surfaceVariant = Color(0xFFF5F5F5), // Card backgrounds
    onSurfaceVariant = Color(0xFF424242),
    outline = Color(0xFFBDBDBD),
    outlineVariant = Color(0xFFE0E0E0),
    
    scrim = Color(0x80000000),
    inverseSurface = Color(0xFF2E2E2E),
    inverseOnSurface = Color(0xFFF5F5F5),
    inversePrimary = Color(0xFF90CAF9)
)

// Turkish Medical Colors - Dark Theme
private val TurkishMedicalDarkColors = darkColorScheme(
    primary = Color(0xFF90CAF9), // Light blue for dark mode
    onPrimary = Color(0xFF0D47A1),
    primaryContainer = Color(0xFF1565C0), // Darker blue container
    onPrimaryContainer = Color(0xFFBBDEFB),
    
    secondary = Color(0xFFEF5350), // Light red for dark mode
    onSecondary = Color(0xFFD32F2F),
    secondaryContainer = Color(0xFFB71C1C), // Darker red container
    onSecondaryContainer = Color(0xFFFFCDD2),
    
    tertiary = Color(0xFF81C784), // Light green for dark mode
    onTertiary = Color(0xFF388E3C),
    tertiaryContainer = Color(0xFF2E7D32), // Darker green container
    onTertiaryContainer = Color(0xFFC8E6C9),
    
    error = Color(0xFFEF5350),
    onError = Color(0xFFD32F2F),
    errorContainer = Color(0xFFB71C1C),
    onErrorContainer = Color(0xFFFFCDD2),
    
    background = Color(0xFF121212), // Medical dark background
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF1E1E1E), // Card surface in dark
    onSurface = Color(0xFFE0E0E0),
    
    surfaceVariant = Color(0xFF2E2E2E), // Dark card backgrounds
    onSurfaceVariant = Color(0xFFBDBDBD),
    outline = Color(0xFF616161),
    outlineVariant = Color(0xFF424242),
    
    scrim = Color(0x80000000),
    inverseSurface = Color(0xFFE0E0E0),
    inverseOnSurface = Color(0xFF1C1B1F),
    inversePrimary = Color(0xFF0D47A1)
)

// Custom colors for medical context
object TurkishMedicalColors {
    // Confidence levels
    val HighConfidence = Color(0xFF4CAF50) // Green
    val MediumConfidence = Color(0xFFFF9800) // Orange
    val LowConfidence = Color(0xFFF44336) // Red
    
    // SGK (Turkish Social Security) colors
    val SgkActive = Color(0xFF2E7D32) // Dark green
    val SgkInactive = Color(0xFF757575) // Gray
    
    // Prescription status colors
    val PrescriptionActive = Color(0xFF1976D2) // Blue
    val PrescriptionComplete = Color(0xFF388E3C) // Green
    val PrescriptionCancelled = Color(0xFFD32F2F) // Red
    val PrescriptionPending = Color(0xFFFF9800) // Orange
    
    // Drug categories (ATC code based)
    val CategoryNervous = Color(0xFF9C27B0) // Purple - Nervous system
    val CategoryCardiovascular = Color(0xFFE91E63) // Pink - Cardiovascular
    val CategoryRespiratory = Color(0xFF00BCD4) // Cyan - Respiratory
    val CategoryDigestive = Color(0xFF4CAF50) // Green - Digestive
    val CategoryHormonal = Color(0xFFFF5722) // Deep orange - Hormonal
    val CategoryAntiinfective = Color(0xFF795548) // Brown - Anti-infective
    val CategoryAntineoplastic = Color(0xFF607D8B) // Blue gray - Antineoplastic
    val CategoryMusculoskeletal = Color(0xFFFF9800) // Orange - Musculoskeletal
    
    // Turkish flag inspired accent (for special occasions)
    val TurkishRed = Color(0xFFE30A17)
    val TurkishWhite = Color(0xFFFFFFFF)
}

// Theme state management
enum class ThemeMode {
    SYSTEM, LIGHT, DARK, AUTO
}

// Accessibility colors
object AccessibilityColors {
    val HighContrast = Color(0xFF000000)
    val LowVision = Color(0xFFFFFFFF)
    val ColorBlindFriendlyPrimary = Color(0xFF0173B2)
    val ColorBlindFriendlySecondary = Color(0xFFDE8F05)
    val ColorBlindFriendlyTertiary = Color(0xFF029E73)
}

@Composable
fun TurkishMedicalTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        TurkishMedicalDarkColors
    } else {
        TurkishMedicalLightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = TurkishMedicalTypography,
        shapes = TurkishMedicalShapes,
        content = content
    )
}

// Dynamic theme for tablet support
@Composable
fun TurkishMedicalAdaptiveTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    isTablet: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        TurkishMedicalDarkColors
    } else {
        TurkishMedicalLightColors
    }
    
    val typography = if (isTablet) {
        TurkishMedicalTabletTypography
    } else {
        TurkishMedicalTypography
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = TurkishMedicalShapes,
        content = content
    )
}

// Confidence level color helper
@Composable
fun getConfidenceColor(confidence: Double): Color {
    return when {
        confidence >= 0.9 -> TurkishMedicalColors.HighConfidence
        confidence >= 0.7 -> TurkishMedicalColors.MediumConfidence
        else -> TurkishMedicalColors.LowConfidence
    }
}

// SGK status color helper
@Composable
fun getSgkStatusColor(isActive: Boolean): Color {
    return if (isActive) {
        TurkishMedicalColors.SgkActive
    } else {
        TurkishMedicalColors.SgkInactive
    }
}

// Prescription status color helper
@Composable
fun getPrescriptionStatusColor(status: String): Color {
    return when (status.lowercase()) {
        "active", "aktif" -> TurkishMedicalColors.PrescriptionActive
        "complete", "tamamlandı" -> TurkishMedicalColors.PrescriptionComplete
        "cancelled", "iptal" -> TurkishMedicalColors.PrescriptionCancelled
        "pending", "beklemede" -> TurkishMedicalColors.PrescriptionPending
        else -> MaterialTheme.colorScheme.onSurface
    }
}

// ATC category color helper
@Composable
fun getAtcCategoryColor(atcCode: String?): Color {
    return when (atcCode?.firstOrNull()) {
        'N' -> TurkishMedicalColors.CategoryNervous // Nervous system
        'C' -> TurkishMedicalColors.CategoryCardiovascular // Cardiovascular
        'R' -> TurkishMedicalColors.CategoryRespiratory // Respiratory
        'A' -> TurkishMedicalColors.CategoryDigestive // Alimentary tract
        'G', 'H', 'S' -> TurkishMedicalColors.CategoryHormonal // Hormonal
        'J' -> TurkishMedicalColors.CategoryAntiinfective // Anti-infectives
        'L' -> TurkishMedicalColors.CategoryAntineoplastic // Antineoplastic
        'M' -> TurkishMedicalColors.CategoryMusculoskeletal // Musculoskeletal
        else -> MaterialTheme.colorScheme.primary
    }
}
