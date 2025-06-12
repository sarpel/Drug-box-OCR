package com.boxocr.simple.ui.accessibility

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.semantics.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import android.view.accessibility.AccessibilityManager
import android.os.Build

/**
 * Enhanced Accessibility Components for Turkish Medical Application
 * Comprehensive accessibility support for medical staff with various needs
 * TalkBack, Switch Control, and voice navigation optimized
 */

// Accessibility state management
@Composable
fun rememberAccessibilityState(): AccessibilityState {
    val context = LocalContext.current
    val accessibilityManager = remember {
        context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    }
    
    return remember {
        AccessibilityState(
            isTalkBackEnabled = accessibilityManager.isEnabled,
            isTouchExplorationEnabled = accessibilityManager.isTouchExplorationEnabled,
            isHighContrastEnabled = false, // Would be detected from system settings
            isLargeTextEnabled = false, // Would be detected from system settings
            isReducedMotionEnabled = false // Would be detected from system settings
        )
    }
}

data class AccessibilityState(
    val isTalkBackEnabled: Boolean,
    val isTouchExplorationEnabled: Boolean,
    val isHighContrastEnabled: Boolean,
    val isLargeTextEnabled: Boolean,
    val isReducedMotionEnabled: Boolean
)

// Turkish medical accessibility announcements
object TurkishMedicalAnnouncements {
    const val DRUG_SCANNED = "İlaç tarandı"
    const val CONFIDENCE_HIGH = "Yüksek güven seviyesi"
    const val CONFIDENCE_MEDIUM = "Orta güven seviyesi"
    const val CONFIDENCE_LOW = "Düşük güven seviyesi"
    const val PRESCRIPTION_STARTED = "Reçete oturumu başlatıldı"
    const val PRESCRIPTION_COMPLETED = "Reçete tamamlandı"
    const val DRUG_ADDED = "İlaç eklendi"
    const val ERROR_OCCURRED = "Hata oluştu"
    const val SUCCESS = "Başarılı"
    const val PROCESSING = "İşleniyor"
    const val SCANNING = "Taranıyor"
    const val READY = "Hazır"
}

// Enhanced accessibility modifier
fun Modifier.enhancedAccessibility(
    contentDescription: String,
    role: Role? = null,
    onClick: (() -> Unit)? = null,
    stateDescription: String? = null,
    liveRegion: LiveRegionMode = LiveRegionMode.Polite
) = this.semantics {
    this.contentDescription = contentDescription
    role?.let { this.role = it }
    onClick?.let { 
        this.onClick {
            it()
            true
        }
    }
    stateDescription?.let { this.stateDescription = it }
    this.liveRegion = liveRegion
}

// Accessible button with enhanced feedback
@Composable
fun AccessibleMedicalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String,
    stateDescription: String? = null,
    hapticFeedback: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val accessibilityState = rememberAccessibilityState()
    
    Button(
        onClick = {
            onClick()
            // Add haptic feedback if enabled
            if (hapticFeedback) {
                // Haptic feedback would be triggered here
            }
        },
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp) // Minimum touch target
            .semantics {
                this.contentDescription = contentDescription
                this.role = Role.Button
                stateDescription?.let { this.stateDescription = it }
                this.liveRegion = LiveRegionMode.Polite
            },
        enabled = enabled,
        content = content
    )
}

// Accessible drug information card
@Composable
fun AccessibleDrugCard(
    drugName: String,
    activeIngredient: String?,
    confidence: Double,
    price: Double?,
    sgkActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accessibilityState = rememberAccessibilityState()
    
    // Build comprehensive content description in Turkish
    val contentDescription = buildString {
        append("İlaç: $drugName")
        activeIngredient?.let { append(", Etken madde: $it") }
        append(", Güven seviyesi: ${(confidence * 100).toInt()} yüzde")
        price?.let { append(", Fiyat: $it Türk Lirası") }
        append(if (sgkActive) ", SGK aktif" else ", SGK pasif")
        append(", Detayları görmek için dokunun")
    }
    
    val stateDescription = when {
        confidence >= 0.9 -> TurkishMedicalAnnouncements.CONFIDENCE_HIGH
        confidence >= 0.7 -> TurkishMedicalAnnouncements.CONFIDENCE_MEDIUM
        else -> TurkishMedicalAnnouncements.CONFIDENCE_LOW
    }
    
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 72.dp) // Larger touch target for accessibility
            .semantics {
                this.contentDescription = contentDescription
                this.stateDescription = stateDescription
                this.role = Role.Button
                this.liveRegion = LiveRegionMode.Polite
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = drugName,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = if (accessibilityState.isHighContrastEnabled) FontWeight.Bold else FontWeight.Medium
                )
            )
            
            activeIngredient?.let { ingredient ->
                Text(
                    text = ingredient,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.semantics {
                        this.contentDescription = "Etken madde: $ingredient"
                    }
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Confidence indicator with accessibility
                ConfidenceIndicator(
                    confidence = confidence,
                    modifier = Modifier.semantics {
                        this.contentDescription = "Güven seviyesi ${(confidence * 100).toInt()} yüzde"
                    }
                )
                
                // SGK status with accessibility
                SGKStatusIndicator(
                    isActive = sgkActive,
                    modifier = Modifier.semantics {
                        this.contentDescription = if (sgkActive) "SGK aktif" else "SGK pasif"
                    }
                )
            }
        }
    }
}

// Accessible confidence indicator
@Composable
fun ConfidenceIndicator(
    confidence: Double,
    modifier: Modifier = Modifier
) {
    val accessibilityState = rememberAccessibilityState()
    
    val color = when {
        confidence >= 0.9 -> Color(0xFF4CAF50)
        confidence >= 0.7 -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }
    
    val textColor = if (accessibilityState.isHighContrastEnabled) {
        Color.Black
    } else {
        Color.White
    }
    
    Surface(
        color = color,
        shape = MaterialTheme.shapes.small,
        modifier = modifier.semantics {
            this.contentDescription = "Güven seviyesi ${(confidence * 100).toInt()} yüzde"
            this.role = Role.Image
        }
    ) {
        Text(
            text = "${(confidence * 100).toInt()}%",
            color = textColor,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (accessibilityState.isHighContrastEnabled) FontWeight.Bold else FontWeight.Medium
            ),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

// Accessible SGK status indicator
@Composable
fun SGKStatusIndicator(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val accessibilityState = rememberAccessibilityState()
    
    val color = if (isActive) Color(0xFF2E7D32) else Color(0xFF757575)
    val text = if (isActive) "SGK" else "SGK ✗"
    
    Surface(
        color = color,
        shape = MaterialTheme.shapes.small,
        modifier = modifier.semantics {
            this.contentDescription = if (isActive) "SGK aktif" else "SGK pasif"
            this.role = Role.Image
        }
    ) {
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (accessibilityState.isHighContrastEnabled) FontWeight.Bold else FontWeight.Medium
            ),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

// Accessible scanning overlay
@Composable
fun AccessibleScanningOverlay(
    isScanning: Boolean,
    boxDetected: Boolean,
    textStable: Boolean,
    readyToCapture: Boolean,
    modifier: Modifier = Modifier
) {
    val announcements = remember { mutableListOf<String>() }
    
    // Build state announcements in Turkish
    LaunchedEffect(isScanning, boxDetected, textStable, readyToCapture) {
        announcements.clear()
        
        when {
            readyToCapture -> announcements.add("Yakalamaya hazır")
            textStable -> announcements.add("Metin kararlı")
            boxDetected -> announcements.add("Kutu algılandı")
            isScanning -> announcements.add("Taranıyor")
        }
    }
    
    Box(
        modifier = modifier.semantics {
            this.liveRegion = LiveRegionMode.Assertive
            this.contentDescription = announcements.joinToString(", ")
        }
    ) {
        // Visual scanning indicator content would go here
        if (isScanning) {
            Text(
                text = "Taranıyor...",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.semantics {
                    this.contentDescription = TurkishMedicalAnnouncements.SCANNING
                }
            )
        }
    }
}

// Accessible prescription progress
@Composable
fun AccessiblePrescriptionProgress(
    currentStep: Int,
    totalSteps: Int,
    stepName: String,
    modifier: Modifier = Modifier
) {
    val progressDescription = "Adım $currentStep / $totalSteps: $stepName"
    
    Column(
        modifier = modifier.semantics {
            this.contentDescription = progressDescription
            this.role = Role.ProgressBar
            this.stateDescription = "İlerleme: $currentStep / $totalSteps"
        }
    ) {
        LinearProgressIndicator(
            progress = currentStep.toFloat() / totalSteps.toFloat(),
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    this.contentDescription = "İlerleme çubuğu: $currentStep / $totalSteps"
                }
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = stepName,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.semantics {
                this.contentDescription = "Mevcut adım: $stepName"
            }
        )
    }
}

// Accessible error message
@Composable
fun AccessibleErrorMessage(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                this.contentDescription = "Hata: $message"
                this.role = Role.AlertDialog
                this.liveRegion = LiveRegionMode.Assertive
            }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Hata",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.semantics {
                    this.contentDescription = "Hata başlığı"
                }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.semantics {
                    this.contentDescription = "Hata mesajı: $message"
                }
            )
            
            onRetry?.let { retry ->
                Spacer(modifier = Modifier.height(16.dp))
                
                AccessibleMedicalButton(
                    onClick = retry,
                    contentDescription = "Tekrar dene",
                    stateDescription = "Hatayı düzeltmek için tekrar dene"
                ) {
                    Text("Tekrar Dene")
                }
            }
        }
    }
}

// Voice navigation support
@Composable
fun VoiceNavigationAnnouncement(
    announcement: String,
    priority: LiveRegionMode = LiveRegionMode.Polite
) {
    // Invisible component that makes announcements for voice navigation
    Box(
        modifier = Modifier
            .size(0.dp)
            .semantics {
                this.contentDescription = announcement
                this.liveRegion = priority
            }
    )
}

// Turkish medical keyboard shortcuts
object TurkishMedicalKeyboardShortcuts {
    const val SCAN_DRUG = "İlaç taramak için boşluk tuşuna basın"
    const val NEXT_STEP = "Sonraki adım için Enter tuşuna basın"
    const val CANCEL = "İptal etmek için Escape tuşuna basın"
    const val HELP = "Yardım için F1 tuşuna basın"
    const val SETTINGS = "Ayarlar için F2 tuşuna basın"
}
