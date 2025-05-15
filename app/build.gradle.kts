import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.darcy.videocutter"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.darcy.videocutter"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters.addAll(listOf("arm64-v8a"))
        }
    }

    // load key store from local.properties
    val localProperties = gradleLocalProperties(File(project.rootDir.absolutePath))
    signingConfigs {
        create("debugSign") {
            storeFile = file(localProperties.getProperty("keystore_path"))
            keyAlias = localProperties.getProperty("key_alias")
            storePassword = localProperties.getProperty("store_password")
            keyPassword = localProperties.getProperty("key_password")
        }

        create("releaseSign") {
            storeFile = file(localProperties.getProperty("keystore_path"))
            keyAlias = localProperties.getProperty("key_alias")
            storePassword = localProperties.getProperty("store_password")
            keyPassword = localProperties.getProperty("key_password")
        }
    }


    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
            signingConfig = signingConfigs.getByName("debugSign")
        }
        release {
            isMinifyEnabled = true
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("releaseSign")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    // 开启ViewBinding
    buildFeatures {
        viewBinding = true
    }
}

fun gradleLocalProperties(projectRootDir: File): Properties {
    val properties = Properties()
    val localProperties = File(projectRootDir, "local.properties")

    if (localProperties.isFile) {
        InputStreamReader(FileInputStream(localProperties), Charsets.UTF_8).use { reader ->
            properties.load(reader)
        }
    } else {
        println("Gradle local properties file not found at $localProperties")
    }
    return properties
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // viewModel 拓展方法
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    implementation(project(":lib_saf_select"))
    implementation(project(":lib_log_toast"))
    implementation(project(":lib_media3_player"))

    implementation(libs.mobile.ffmpeg.full)
}