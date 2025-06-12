// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("com.google.dagger.hilt.android") version "2.48.1" apply false
}
// Project-level build.gradle.kts
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // ... other classpath dependencies
        classpath("org.jetbrains.kotlin:kotlin-serialization:1.9.22") // Or the version compatible with your Kotlin version
    }
}
val opencvAndroidVersion by extra("4.8.0")
val glideVersion by extra("4.16.0")

task<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
