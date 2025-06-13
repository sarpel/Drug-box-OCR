plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("dagger.hilt.android.plugin")
    id("kotlin-parcelize")
    id("kotlinx-serialization")
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
}

android {
    namespace = "com.boxocr.simple"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.boxocr.simple"
        minSdk = 26
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
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



    packaging() {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/gradle/incremental.annotation.processors"
        }
    }
    ndkVersion = "29.0.13113456 rc1"
    buildToolsVersion = "35.0.0" // Updated to match compileSdk 35
    sourceSets {
        getByName("main") {
            java.srcDirs("src/main/java", "src/main/kotlin")
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
    // Re-adding appcompat dependency for base theme fallback in themes.xml
    implementation("androidx.appcompat:appcompat:1.6.1")

    // Jetpack Compose BOM - ensures compatible versions
    implementation(platform("androidx.compose:compose-bom:2025.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3") // Relying on BOM for Material3 version
    implementation("androidx.compose.material:material-icons-extended")
    implementation("com.google.android.material:material:1.11.0") // Adding Material Components library as a fallback for themes/attributes

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

    // Missing dependencies for compilation fixes
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")
    implementation("io.coil-kt:coil:2.5.0")
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("org.apache.poi:poi:5.2.4")
    implementation("org.apache.poi:poi-ooxml:5.2.4")
    implementation("org.apache.poi:poi-scratchpad:5.2.4")

    // Hilt Dependency Injection
    implementation("com.google.dagger:hilt-android:2.48.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("androidx.hilt:hilt-work:1.2.0")
    ksp("com.google.dagger:hilt-compiler:2.48.1")
    ksp("androidx.hilt:hilt-compiler:1.2.0")

    // Camera with ML Kit integration
    implementation("androidx.camera:camera-camera2:1.4.2")
    implementation("androidx.camera:camera-lifecycle:1.4.2")
    implementation("androidx.camera:camera-view:1.4.2")
    implementation("androidx.camera:camera-core:1.4.2")
    implementation("androidx.camera:camera-extensions:1.4.2")

    // Enhanced ML Kit features
    implementation("com.google.mlkit:text-recognition:16.0.0")
    implementation("com.google.mlkit:barcode-scanning:17.2.0")
    implementation("com.google.mlkit:language-id:17.0.4")
    implementation("com.google.mlkit:translate:17.0.1")
    // implementation("com.google.android.speech:speech:1.2.0") // Removed as it is an old, unresolved dependency

    // Phase 1 Multi-Drug Enhancement: Object Detection
    implementation("com.google.mlkit:object-detection:17.0.1")
    implementation("com.google.mlkit:object-detection-custom:17.0.1")

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
    implementation("androidx.camera:camera-mlkit-vision:1.4.2")

    // AI model optimization and quantization
    implementation("org.tensorflow:tensorflow-lite-select-tf-ops:2.14.0")

    // Advanced coroutines for AI parallel processing
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-guava:1.7.3")

    // Memory-efficient bitmap processing for AI vision
    implementation("jp.wasabeef:glide-transformations:4.3.0")
    ksp("com.github.bumptech.glide:compiler:4.16.0")


    // Phase 1 Multi-Drug Enhancement: Visual Feature Extraction
    // Consider replacing with an official OpenCV release or a more maintained fork if issues persist.
    // implementation("com.quickbirdstudios:opencv:4.6.0")  // Removed as per build error and potentially outdated.
    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")

    // Database
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // Remove duplicate serialization dependencies
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Fix window dependencies
    implementation("androidx.window:window:1.2.0")
    implementation("androidx.window:window-core:1.2.0")

    // Fix Material3 adaptive versions
    implementation("androidx.compose.material3:material3-window-size-class")
    implementation("androidx.compose.material3:material3-adaptive-navigation-suite")
    // Removed specific versions of material3-adaptive and material3-adaptive-navigation-suite, relying on Compose BOM for version management.
    // implementation("androidx.compose.material3:material3-adaptive:1.0.0-alpha05")
    // implementation("androidx.compose.material3:material3-adaptive-navigation-suite:1.0.0-alpha02")

    // Remove duplicate Ktor dependencies
    implementation("io.ktor:ktor-client-android:2.3.7")
    implementation("io.ktor:ktor-client-core:2.3.7")
    implementation("io.ktor:ktor-client-logging:2.3.7")



    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.8.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("androidx.room:room-testing:2.6.1")
    testImplementation("com.google.dagger:hilt-android-testing:2.48.1")
    kspTest("com.google.dagger:hilt-compiler:2.48.1")

    // Android instrumented tests
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2025.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.navigation:navigation-testing:2.7.6")
    androidTestImplementation("androidx.work:work-testing:2.10.1")
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.48.1")
    kspAndroidTest("com.google.dagger:hilt-compiler:2.48.1")

    // Debug tools
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}