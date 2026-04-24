plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.jetbrains.kotlin.serialization)

    alias(libs.plugins.googleServices)
    alias(libs.plugins.crashlytics)
    alias(libs.plugins.ksp)

    //id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.minedu.gob.pe.encuestasatisfaccinenapres"

    compileSdk = 36

    defaultConfig {
        applicationId = "com.minedu.gob.pe.encuestasatisfaccinenapres"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "SUPABASE_URL", "\"https://vofuwtljegyjajwjzlll.supabase.co\"")
        buildConfigField("String", "SUPABASE_KEY", "\"sb_publishable_wWNTLpcXWobt0Bh7IMeopw_pJbxUGVi\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation("com.google.dagger:hilt-android:2.57.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.3.0")
    implementation(libs.navigation3.ui)
    ksp("com.google.dagger:hilt-android-compiler:2.57.1")

    // Icons
    implementation(libs.icon.source)

    // 🔹 Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // 🔹 Supabase (BOM)
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.realtime)
    implementation(libs.supabase.gotrue)

    // 🔹 Ktor
    implementation(libs.ktor.client.okhttp)

    // 🔹 Serialization
    implementation(libs.kotlinx.serialization.json)

    // 🔹 Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.realtime)
    implementation(libs.firebase.config)
    implementation(libs.firebase.crashlytics)

    // 🔹 Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation)

    // 🔹 Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    // 🔹 Navigation
    implementation(libs.androidx.navigation.compose)

    // 🔹 Images
    implementation(libs.coil)

    // 🔹 Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // 🔹 Maps & Location
    implementation(libs.play.services.location)
    implementation(libs.play.services.maps)
    implementation(libs.maps.compose)

    // 🔹 Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)

    // 🔹 DataStore
    implementation(libs.datastore)

    // 🔹 Permissions
    implementation(libs.accompanist.permissions)

    // 🔹 Gson
    implementation(libs.gson)

    // 🔹 Testing
    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

