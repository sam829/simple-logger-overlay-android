import com.vanniktech.maven.publish.SonatypeHost
import java.net.URL

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.dokka)
    alias(libs.plugins.maven.publish)
}

android {
    namespace = "com.debugtools.logger"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    buildFeatures {
        compose = true
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
        getByName("test") { java.srcDirs("src/test/kotlin") }
        getByName("androidTest") { java.srcDirs("src/androidTest/kotlin") }
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    coordinates(
        groupId = providers.gradleProperty("GROUP").get(),
        artifactId = providers.gradleProperty("POM_ARTIFACT_ID").get(),
        version = providers.gradleProperty("VERSION").get()
    )

    pom {
        name.set("Simple Logger Overlay")
        description.set(
            "A production-ready Android logging library with an in-app debug overlay " +
                "for viewing logs, network requests, and more. Built with Jetpack Compose."
        )
        inceptionYear.set("2026")
        url.set("https://github.com/sam829/simple-logger-overlay-android")

        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }

        developers {
            developer {
                id.set("sam829")
                name.set("Saumya Macwan")
                url.set("https://github.com/sam829")
            }
        }

        scm {
            url.set("https://github.com/sam829/simple-logger-overlay-android")
            connection.set("scm:git:git://github.com/sam829/simple-logger-overlay-android.git")
            developerConnection.set(
                "scm:git:ssh://git@github.com/sam829/simple-logger-overlay-android.git"
            )
        }
    }
}

tasks.withType<org.jetbrains.dokka.gradle.DokkaTask>().configureEach {
    dokkaSourceSets {
        named("main") {
            moduleName.set("Simple Logger Overlay")
            reportUndocumented.set(true)
            sourceLink {
                localDirectory.set(file("src/main/kotlin"))
                remoteUrl.set(
                    URL(
                        "https://github.com/sam829/simple-logger-overlay-android/tree/main/library/src/main/kotlin"
                    )
                )
                remoteLineSuffix.set("#L")
            }
        }
    }
}

dependencies {
    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.foundation)
    implementation(libs.compose.viewmodel)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.compose.activity)
    implementation("androidx.compose.material:material-icons-extended")

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // JSON serialization
    implementation(libs.kotlinx.serialization.json)

    // Core Android
    implementation(libs.androidx.core.ktx)

    // Network adapters — host app provides; compileOnly avoids forcing them on consumers
    compileOnly(libs.okhttp.logging)
    compileOnly(libs.retrofit.core)

    // Framework observers — optional; host app provides if needed
    compileOnly(libs.androidx.work.runtime)
    compileOnly(libs.hilt.android)

    // Tests
    testImplementation(libs.junit)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.test.ext)
    androidTestImplementation(libs.compose.ui.test.junit4)
}
