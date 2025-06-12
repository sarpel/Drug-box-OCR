import com.android.build.api.dsl.Packaging

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("kotlin-parcelize")
    id("kotlinx-serialization")
}

android {
    namespace = "com.boxocr.simple"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.boxocr.simple"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // Turkish localization support
        resourceConfigurations += setOf("tr", "en")

        // Enhanced features build config
        buildConfigField("String", "TURKISH_DRUG_API_BASE_URL", "\"https://api.ilacabak.com/\"")
        buildConfigField("String", "ALTERNATIVE_DRUG_API_URL", "\"https://api.ilacrehberi.com/\"")
        buildConfigField("boolean", "ENABLE_TURKISH_PHONETIC_MATCHING", "true")
        buildConfigField("boolean", "ENABLE_ENHANCED_ACCESSIBILITY", "true")
        buildConfigField("boolean", "ENABLE_TABLET_LAYOUT", "true")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // Production Turkish medical configuration
            buildConfigField("boolean", "DEBUG_MODE", "false")
            buildConfigField("boolean", "ENABLE_ANALYTICS", "true")
        }
        debug {
            isMinifyEnabled = false
            buildConfigField("boolean", "DEBUG_MODE", "true")
            buildConfigField("boolean", "ENABLE_ANALYTICS", "false")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
            "-opt-in=androidx.compose.animation.ExperimentalAnimationApi",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    packaging() {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/gradle/incremental.annotation.processors"
        }
    }
}

dependencies {
    // Core Android dependencies
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // Jetpack Compose BOM - ensures compatible versions
    implementation(platform("androidx.compose:compose-bom:2025.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    
    // Enhanced Compose features for Phase 4
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.animation:animation-graphics")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.foundation:foundation-layout")
    implementation("androidx.compose.runtime:runtime-livedata")
    implementation("androidx.compose.ui:ui-util")
    
    // Navigation with enhanced animations
    implementation("androidx.navigation:navigation-compose:2.7.6")
    implementation("androidx.navigation:navigation-runtime-ktx:2.7.6")
    
    // Hilt Dependency Injection
    implementation("com.google.dagger:hilt-android:2.48.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("androidx.hilt:hilt-work:1.2.0")
    kapt("com.google.dagger:hilt-compiler:2.48.1")
    kapt("androidx.hilt:hilt-compiler:1.2.0")

    // Camera with ML Kit integration
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")
    implementation("androidx.camera:camera-core:1.3.1")
    implementation("androidx.camera:camera-extensions:1.3.1")
    
    // Enhanced ML Kit features
    implementation("com.google.mlkit:text-recognition:16.0.0")
    implementation("com.google.mlkit:barcode-scanning:17.2.0")
    implementation("com.google.mlkit:language-id:17.0.4")
    implementation("com.google.mlkit:translate:17.0.1")
    
    // 🧠 PHASE 5: AI INTELLIGENCE DEPENDENCIES
    // TensorFlow Lite for on-device AI inference
    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
    implementation("org.tensorflow:tensorflow-lite-metadata:0.4.4")
    implementation("org.tensorflow:tensorflow-lite-gpu:2.14.0")
    
    // TensorFlow Lite Task Library for specialized AI tasks
    implementation("org.tensorflow:tensorflow-lite-task-vision:0.4.4")
    implementation("org.tensorflow:tensorflow-lite-task-text:0.4.4")
    implementation("org.tensorflow:tensorflow-lite-task-audio:0.4.4")
    
    // Advanced image processing for AI vision
    implementation("androidx.camera:camera-mlkit-vision:1.4.0-alpha02")
    
    // AI model optimization and quantization
    implementation("org.tensorflow:tensorflow-lite-select-tf-ops:2.14.0")
    
    // Advanced coroutines for AI parallel processing
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-guava:1.7.3")
    
    // Memory-efficient bitmap processing for AI vision
    implementation("com.github.bumptech.glide:glide:4.16.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0")

    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")

    // Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // Serialization and data handling
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    
    // DataStore for enhanced preferences
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.datastore:datastore-core:1.0.0")

    // Background processing
    implementation("androidx.work:work-runtime-ktx:2.10.1")
    implementation("androidx.work:work-hilt:2.10.1")

    // Turkish Excel database support - Apache POI
    implementation("org.apache.poi:poi:5.2.4")
    implementation("org.apache.poi:poi-scratchpad:5.2.4")
    implementation("org.apache.poi:poi-ooxml:5.2.4")

    // PDF generation for Turkish reports
    implementation("com.itextpdf:itext7-core:7.2.5")
    implementation("com.itextpdf:html2pdf:4.0.5")

    // Enhanced permissions handling
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")
    
    // Image handling and processing
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("io.coil-kt:coil-svg:2.5.0")
    
    // Accessibility enhancements
    implementation("androidx.compose.ui:ui-test-manifest")
    implementation("androidx.compose.ui:ui-semantics")
    
    // Voice recognition for Turkish language
    implementation("androidx.speech:speech-ktx:1.2.1")
    
    // Turkish language text processing
    implementation("org.apache.commons:commons-text:1.11.0")
    implementation("org.apache.commons:commons-lang3:3.14.0")
    
    // Enhanced date/time handling for Turkish locale
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
    
    // Lottie animations for enhanced UX
    implementation("com.airbnb.android:lottie-compose:6.3.0")
    
    // Turkish locale support
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.core:core-splashscreen:1.0.1")
    
    // Enhanced window management for tablets
    implementation("androidx.window:window:1.2.0")
    implementation("androidx.window:window-core:1.2.0")
    
    // Adaptive layouts for tablet support
    implementation("androidx.compose.material3:material3-window-size-class:1.1.2")
    implementation("androidx.compose.material3:material3-adaptive:1.0.0-alpha05")
    implementation("androidx.compose.material3:material3-adaptive-navigation-suite:1.0.0-alpha02")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.8.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("androidx.room:room-testing:2.6.1")
    testImplementation("com.google.dagger:hilt-android-testing:2.48.1")
    kaptTest("com.google.dagger:hilt-compiler:2.48.1")
    
    // Android instrumented tests
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.navigation:navigation-testing:2.7.6")
    androidTestImplementation("androidx.work:work-testing:2.10.1")
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.48.1")
    kaptAndroidTest("com.google.dagger:hilt-compiler:2.48.1")
    
    // Debug tools
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
