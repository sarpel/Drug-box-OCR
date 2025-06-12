pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") } // Added JitPack for com.quickbirdstudios:opencv
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") } // Ensure JitPack is also here for dependency resolution
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") } // Potentially needed for some older dependencies
    }
}

rootProject.name = "Box Name OCR"
include(":app")
