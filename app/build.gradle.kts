import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val secrets = Properties().apply {
    val f = rootProject.file("secrets.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

fun secret(name: String, fallback: String = ""): String =
    System.getenv(name)?.takeIf { it.isNotBlank() } ?: secrets.getProperty(name, fallback).trim()

android {
    namespace = "com.tempnumber.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.tempnumber.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "RAPIDAPI_KEY", "\"${secret("RAPIDAPI_KEY")}\"")
        buildConfigField("String", "RAPIDAPI_HOST", "\"${secret("RAPIDAPI_HOST")}\"")
        buildConfigField("String", "PATH_COUNTRIES", "\"${secret("PATH_COUNTRIES", "/countries")}\"")
        buildConfigField("String", "PATH_NUMBERS", "\"${secret("PATH_NUMBERS", "/countryNumbers")}\"")
        buildConfigField("String", "PATH_MESSAGES", "\"${secret("PATH_MESSAGES", "/getMessages")}\"")
        buildConfigField("String", "PARAM_COUNTRY", "\"${secret("PARAM_COUNTRY", "country")}\"")
        buildConfigField("String", "PARAM_NUMBER", "\"${secret("PARAM_NUMBER", "number")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")

    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
}
