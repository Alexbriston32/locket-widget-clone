// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.4.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
}

// Ensure proper configuration for AndroidX across all modules
subprojects {
    configurations.all {
        resolutionStrategy {
            force("androidx.appcompat:appcompat:1.6.1")
            force("androidx.core:core:1.13.1")
        }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
