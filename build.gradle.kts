// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.3.0" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.25" apply false
    id("com.google.dagger.hilt.android") version "2.48.1" apply false
}
// Project-level build.gradle.kts
buildscript {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
    dependencies {
        // ... other classpath dependencies
        classpath("org.jetbrains.kotlin:kotlin-serialization:2.0.21") // Updated for Kotlin 2.0.21
    }
}
val opencvAndroidVersion by extra("4.8.0")
val glideVersion by extra("4.16.0")

task<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}