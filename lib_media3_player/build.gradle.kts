plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.darcy.lib_media3_player"
    compileSdk = 35

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        // 资源前缀
        resourcePrefix = "lib_media3_player_"
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
    kotlinOptions {
        jvmTarget = "11"
    }
    // lint 配置 run `gradlew updateLintBaseline`
    lint {
        baseline = file("lint-baseline.xml")
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(project(":lib_log_toast"))
    // 添加exoplayer依赖
    api(libs.androidx.media3.exoplayer)
//    api(libs.androidx.media3.exoplayer.dash) // 流媒体解码
    api(libs.androidx.media3.ui)
//    api(libs.androidx.media3.exoplayer.decoder)
    // FFmpeg 扩展解码器需要本地编译 https://github.com/androidx/media/tree/release/libraries/decoder_ffmpeg
    // https://juejin.cn/post/7565815654344785929

}