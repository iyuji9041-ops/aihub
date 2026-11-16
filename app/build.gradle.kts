import org.gradle.kotlin.dsl.implementation
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.foss.aihub"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.zaswix.aihub"
        minSdk = 26
        //noinspection OldTargetApi
        targetSdk = 36
        versionCode = 19
        versionName = "3.2.3"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    //noinspection WrongGradleMethod
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    val keystoreFile = System.getenv("KEYSTORE_FILE")?.let { file(it) }
        ?: file("$rootDir/keystore.jks").takeIf { it.exists() }
        ?: file("$rootDir/../../keystore.jks").takeIf { it.exists() }

    val keystorePassword = System.getenv("KEYSTORE_PASSWORD")
    val keyAlias = System.getenv("KEY_ALIAS")
    val keyPassword = System.getenv("KEY_PASSWORD")

    val keystorePropertiesFile = file("$rootDir/keystore.properties").takeIf { it.exists() }
        ?: file("$rootDir/local.properties").takeIf { it.exists() }
        ?: file("$rootDir/../../local.properties").takeIf { it.exists() }

    val keystoreProperties = Properties()
    if (keystorePropertiesFile != null && keystorePropertiesFile.exists()) {
        keystoreProperties.load(keystorePropertiesFile.inputStream())
        println("Keystore properties loaded successfully.")
    }

    val finalStoreFile = keystoreFile ?: keystoreProperties.getProperty("STORE_FILE")?.let { file(it) }
    val finalStorePassword = keystorePassword ?: keystoreProperties.getProperty("KEYSTORE_PASSWORD")
    val finalKeyAlias = keyAlias ?: keystoreProperties.getProperty("KEY_ALIAS")
    val finalKeyPassword = keyPassword ?: keystoreProperties.getProperty("KEY_PASSWORD")

    val canSignRelease = finalStoreFile != null && finalStoreFile.exists() && !finalStorePassword.isNullOrBlank()

    signingConfigs {
        if (canSignRelease) {
            create("release") {
                storeFile = finalStoreFile
                storePassword = finalStorePassword
                keyAlias = finalKeyAlias ?: ""
                keyPassword = finalKeyPassword ?: finalStorePassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )

            if (canSignRelease) {
                signingConfig = signingConfigs.getByName("release")
            } else {
                signingConfig = signingConfigs.getByName("debug")
            }
        }

        getByName("debug") {
            isDebuggable = true
            applicationIdSuffix = ".debug"
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Manually added dependency
    implementation(libs.androidx.webkit)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.gson)
    implementation(libs.androidx.compose.adaptive)
    implementation(libs.androidx.compose.adaptive.layout)
    implementation(libs.androidx.compose.adaptive.navigation)
    implementation(libs.material)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.jetbrains.kotlinx.serialization.json)
    implementation(libs.androidx.core.splashscreen)
}
