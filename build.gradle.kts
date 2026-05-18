// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    //id("com.android.application")
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false

    alias(libs.plugins.jetbrains.kotlin.serialization) apply false

    alias(libs.plugins.googleServices) apply false
    alias(libs.plugins.crashlytics) apply false
    alias(libs.plugins.ksp) apply false

    id("com.google.dagger.hilt.android") version "2.57.1" apply false
}
