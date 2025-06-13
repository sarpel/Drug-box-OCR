package com.boxocr.simple.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = TurkishMedicalPrimary,
    onPrimary = TurkishMedicalOnPrimary,
    secondary = TurkishMedicalSecondary,
    onSecondary = TurkishMedicalOnSecondary,
    tertiary = TurkishMedicalTertiary,
    onTertiary = TurkishMedicalOnTertiary,
    background = TurkishMedicalBackground,
    onBackground = TurkishMedicalOnBackground,
    surface = TurkishMedicalSurface,
    onSurface = TurkishMedicalOnSurface,
    error = TurkishMedicalError,
    onError = TurkishMedicalOnError,
)

private val LightColorScheme = lightColorScheme(
    primary = TurkishMedicalPrimary,
    onPrimary = TurkishMedicalOnPrimary,
    secondary = TurkishMedicalSecondary,
    onSecondary = TurkishMedicalOnSecondary,
    tertiary = TurkishMedicalTertiary,
    onTertiary = TurkishMedicalOnTertiary,
    surface = TurkishMedicalSurface,
    onSurface = TurkishMedicalOnSurface,
    background = TurkishMedicalBackground,
    onBackground = TurkishMedicalOnBackground,
    error = TurkishMedicalError,
    onError = TurkishMedicalOnError,
)

@Composable
fun BoxOCRTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
