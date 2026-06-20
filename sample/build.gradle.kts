plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.debugtools.logger.sample"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.debugtools.logger.sample"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    sourceSets {
        getByName("main") { java.srcDirs("src/main/kotlin") }
    }
}

dependencies {
    // Library — debug only, same as real consumers would use
    debugImplementation(project(":library"))

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.foundation)
    implementation(libs.compose.activity)
    implementation(libs.compose.viewmodel)
    debugImplementation(libs.compose.ui.tooling)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // OkHttp + Retrofit (sample uses the network interceptor)
    implementation(libs.okhttp.logging)
    implementation(libs.retrofit.core)

    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Core
    implementation(libs.androidx.core.ktx)
}
