package com.boxocr.simple.ui.theme

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

/**
 * Enhanced Animation System for Turkish Medical Application
 * Professional, smooth animations optimized for medical workflow
 * Accessibility-aware with reduced motion support
 */

// Animation durations optimized for medical precision
object AnimationDurations {
    const val FAST = 150
    const val MEDIUM = 300
    const val SLOW = 500
    const val EXTRA_SLOW = 750
    
    // Medical workflow specific timings
    const val SCAN_FEEDBACK = 200 // Quick feedback for scanning
    const val CONFIDENCE_ANIMATION = 400 // Confidence level changes
    const val PRESCRIPTION_TRANSITION = 600 // Between prescription steps
    const val SUCCESS_CELEBRATION = 800 // Success animations
}

// Easing curves for medical application
object MedicalEasing {
    // Professional, non-distracting curves
    val Standard = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1f)
    val Decelerate = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1f)
    val Accelerate = CubicBezierEasing(0.4f, 0.0f, 1f, 1f)
    val Emphasized = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1f)
    
    // Medical specific curves
    val Precise = CubicBezierEasing(0.25f, 0.46f, 0.45f, 0.94f) // For precise inputs
    val Gentle = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1f) // For error/success states
    val Confident = CubicBezierEasing(0.68f, -0.55f, 0.265f, 1.55f) // For confidence animations
}

// Screen transition animations
object ScreenTransitions {
    
    @Composable
    fun slideInFromRight(
        durationMillis: Int = AnimationDurations.MEDIUM
    ): EnterTransition {
        return slideInHorizontally(
            initialOffsetX = { fullWidth -> fullWidth },
            animationSpec = tween(
                durationMillis = durationMillis,
                easing = MedicalEasing.Emphasized
            )
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = durationMillis,
                easing = MedicalEasing.Standard
            )
        )
    }
    
    @Composable
    fun slideOutToLeft(
        durationMillis: Int = AnimationDurations.MEDIUM
    ): ExitTransition {
        return slideOutHorizontally(
            targetOffsetX = { fullWidth -> -fullWidth },
            animationSpec = tween(
                durationMillis = durationMillis,
                easing = MedicalEasing.Emphasized
            )
        ) + fadeOut(
            animationSpec = tween(
                durationMillis = durationMillis,
                easing = MedicalEasing.Standard
            )
        )
    }
    
    @Composable
    fun slideInFromBottom(
        durationMillis: Int = AnimationDurations.MEDIUM
    ): EnterTransition {
        return slideInVertically(
            initialOffsetY = { fullHeight -> fullHeight },
            animationSpec = tween(
                durationMillis = durationMillis,
                easing = MedicalEasing.Emphasized
            )
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = durationMillis,
                easing = MedicalEasing.Standard
            )
        )
    }
    
    @Composable
    fun slideOutToBottom(
        durationMillis: Int = AnimationDurations.MEDIUM
    ): ExitTransition {
        return slideOutVertically(
            targetOffsetY = { fullHeight -> fullHeight },
            animationSpec = tween(
                durationMillis = durationMillis,
                easing = MedicalEasing.Emphasized
            )
        ) + fadeOut(
            animationSpec = tween(
                durationMillis = durationMillis,
                easing = MedicalEasing.Standard
            )
        )
    }
    
    // Medical workflow specific transitions
    @Composable
    fun scanningTransition(): EnterTransition {
        return scaleIn(
            initialScale = 0.8f,
            transformOrigin = TransformOrigin.Center,
            animationSpec = tween(
                durationMillis = AnimationDurations.SCAN_FEEDBACK,
                easing = MedicalEasing.Confident
            )
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = AnimationDurations.SCAN_FEEDBACK,
                easing = MedicalEasing.Standard
            )
        )
    }
    
    @Composable
    fun prescriptionStepTransition(): EnterTransition {
        return slideInHorizontally(
            initialOffsetX = { it / 2 },
            animationSpec = tween(
                durationMillis = AnimationDurations.PRESCRIPTION_TRANSITION,
                easing = MedicalEasing.Gentle
            )
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = AnimationDurations.PRESCRIPTION_TRANSITION,
                easing = MedicalEasing.Standard
            )
        )
    }
    
    @Composable
    fun successTransition(): EnterTransition {
        return scaleIn(
            initialScale = 0.3f,
            transformOrigin = TransformOrigin.Center,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = AnimationDurations.SUCCESS_CELEBRATION,
                easing = MedicalEasing.Confident
            )
        )
    }
}

// Component-level animations
object ComponentAnimations {
    
    // Confidence level animation
    @Composable
    fun confidenceAnimation(): InfiniteTransition {
        return rememberInfiniteTransition(label = "confidence")
    }
    
    // Drug scanning pulse animation
    @Composable
    fun scanningPulse(): InfiniteTransition {
        return rememberInfiniteTransition(label = "scanning_pulse")
    }
    
    // Processing indicator animation
    @Composable
    fun processingAnimation(): InfiniteTransition {
        return rememberInfiniteTransition(label = "processing")
    }
    
    // Card appearance animation
    fun cardEnterAnimation(): AnimationSpec<Float> {
        return spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    }
    
    // Button press animation
    fun buttonPressAnimation(): AnimationSpec<Float> {
        return spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessHigh
        )
    }
    
    // Error shake animation
    fun errorShakeAnimation(): AnimationSpec<Float> {
        return keyframes {
            durationMillis = AnimationDurations.MEDIUM
            0f at 0 with MedicalEasing.Standard
            -8f at 50 with MedicalEasing.Standard
            8f at 100 with MedicalEasing.Standard
            -8f at 150 with MedicalEasing.Standard
            8f at 200 with MedicalEasing.Standard
            0f at 250 with MedicalEasing.Standard
        }
    }
    
    // Success check animation
    fun successCheckAnimation(): AnimationSpec<Float> {
        return tween(
            durationMillis = AnimationDurations.SUCCESS_CELEBRATION,
            easing = MedicalEasing.Confident
        )
    }
}

// Shared element transitions for medical workflow
object SharedElementTransitions {
    
    // Drug box image transition from camera to verification
    @Composable
    fun drugImageTransition(): ContentTransform {
        return slideInVertically(
            initialOffsetY = { height -> height },
            animationSpec = tween(
                durationMillis = AnimationDurations.PRESCRIPTION_TRANSITION,
                easing = MedicalEasing.Emphasized
            )
        ).togetherWith(
            slideOutVertically(
                targetOffsetY = { height -> -height },
                animationSpec = tween(
                    durationMillis = AnimationDurations.PRESCRIPTION_TRANSITION,
                    easing = MedicalEasing.Emphasized
                )
            )
        )
    }
    
    // Prescription data transition between screens
    @Composable
    fun prescriptionDataTransition(): ContentTransform {
        return fadeIn(
            animationSpec = tween(
                durationMillis = AnimationDurations.MEDIUM,
                easing = MedicalEasing.Standard
            )
        ).togetherWith(
            fadeOut(
                animationSpec = tween(
                    durationMillis = AnimationDurations.MEDIUM,
                    easing = MedicalEasing.Standard
                )
            )
        )
    }
    
    // Confidence score transition
    @Composable
    fun confidenceTransition(): ContentTransform {
        return slideInVertically(
            initialOffsetY = { height -> height / 2 },
            animationSpec = tween(
                durationMillis = AnimationDurations.CONFIDENCE_ANIMATION,
                easing = MedicalEasing.Confident
            )
        ).togetherWith(
            slideOutVertically(
                targetOffsetY = { height -> -height / 2 },
                animationSpec = tween(
                    durationMillis = AnimationDurations.CONFIDENCE_ANIMATION,
                    easing = MedicalEasing.Confident
                )
            )
        )
    }
}

// Turkish medical specific animations
object TurkishMedicalAnimations {
    
    // SGK status change animation
    @Composable
    fun sgkStatusAnimation(): AnimationSpec<Float> {
        return spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    }
    
    // Price comparison animation
    @Composable
    fun priceComparisonAnimation(): AnimationSpec<Float> {
        return tween(
            durationMillis = AnimationDurations.MEDIUM,
            easing = MedicalEasing.Precise
        )
    }
    
    // E-signature transition animation
    @Composable
    fun eSignatureTransition(): EnterTransition {
        return slideInHorizontally(
            initialOffsetX = { fullWidth -> fullWidth },
            animationSpec = tween(
                durationMillis = AnimationDurations.EXTRA_SLOW,
                easing = MedicalEasing.Gentle
            )
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = AnimationDurations.EXTRA_SLOW,
                easing = MedicalEasing.Standard
            )
        )
    }
    
    // Turkish flag celebration animation (for special occasions)
    @Composable
    fun turkishCelebrationAnimation(): InfiniteTransition {
        return rememberInfiniteTransition(label = "turkish_celebration")
    }
}

// Accessibility-aware animations
object AccessibilityAnimations {
    
    // Reduced motion variants for accessibility
    @Composable
    fun reducedMotionFadeIn(): EnterTransition {
        return fadeIn(
            animationSpec = tween(
                durationMillis = AnimationDurations.FAST,
                easing = LinearEasing
            )
        )
    }
    
    @Composable
    fun reducedMotionFadeOut(): ExitTransition {
        return fadeOut(
            animationSpec = tween(
                durationMillis = AnimationDurations.FAST,
                easing = LinearEasing
            )
        )
    }
    
    // High contrast animation support
    @Composable
    fun highContrastTransition(): ContentTransform {
        return fadeIn(
            animationSpec = snap()
        ).togetherWith(
            fadeOut(
                animationSpec = snap()
            )
        )
    }
}

// Animation utility functions
object AnimationUtils {
    
    // Check if reduced motion is preferred
    @Composable
    fun isReducedMotionPreferred(): Boolean {
        // This would integrate with system accessibility settings
        // For now, return false - would be implemented with accessibility APIs
        return false
    }
    
    // Get appropriate transition based on accessibility preferences
    @Composable
    fun getAccessibleTransition(
        normalTransition: EnterTransition,
        reducedMotionTransition: EnterTransition = AccessibilityAnimations.reducedMotionFadeIn()
    ): EnterTransition {
        return if (isReducedMotionPreferred()) {
            reducedMotionTransition
        } else {
            normalTransition
        }
    }
    
    // Scale animation duration based on accessibility preferences
    fun getAccessibleDuration(baseDuration: Int): Int {
        return if (isReducedMotionPreferred()) {
            baseDuration / 2 // Faster animations for reduced motion
        } else {
            baseDuration
        }
    }
}

// Material 3 enhanced animations
object Material3Animations {
    
    // Enhanced Material 3 motion tokens
    @Composable
    fun emphasizedDecelerate(): AnimationSpec<Float> {
        return tween(
            durationMillis = AnimationDurations.SLOW,
            easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
        )
    }
    
    @Composable
    fun emphasizedAccelerate(): AnimationSpec<Float> {
        return tween(
            durationMillis = AnimationDurations.MEDIUM,
            easing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)
        )
    }
    
    @Composable
    fun emphasizedStandard(): AnimationSpec<Float> {
        return tween(
            durationMillis = AnimationDurations.MEDIUM,
            easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
        )
    }
}
