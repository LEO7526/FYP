plugins {
    id("com.android.application")
}

android {
    // 這是你的專案包名，千萬別改
    namespace = "com.example.yummyrestaurant"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.yummyrestaurant"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // ============================================
    // 1. Android 基礎元件 (直接寫死版本號，保證不報錯)
    // ============================================
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity:1.8.0")

    // ============================================
    // 2. 員工 App 原本用的 (Volley)
    // ============================================
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // ============================================
    // 3. 庫存系統 (Inventory) 的強大功能
    // ============================================

    // 列表與下拉刷新
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // CameraX (掃描 QR Code 用)
    implementation("androidx.camera:camera-core:1.3.1")
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")

    // ML Kit (條碼辨識)
    implementation("com.google.mlkit:barcode-scanning:17.2.0")

    // Retrofit & Gson (庫存系統用的網路連線)
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // 背景任務
    implementation("androidx.work:work-runtime:2.9.0")

    // ============================================
    // 4. 測試相關
    // ============================================
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}