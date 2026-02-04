plugins {
    id("com.android.application") version "8.10.1"
    id("org.jetbrains.kotlin.android") version "1.9.22"
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.example.yummyrestaurant"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.yummyrestaurant"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
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


}

dependencies {
//    for payment
    implementation("com.stripe:stripe-android:20.11.0")

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.activity:activity:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")


//for map
    implementation("org.osmdroid:osmdroid-android:6.1.16")
    implementation("org.osmdroid:osmdroid-mapsforge:6.1.16")
    implementation(libs.play.services.location)
    implementation(libs.play.services.maps)

    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation("com.google.android.material:material:1.11.0")

    implementation("androidx.recyclerview:recyclerview:1.3.1")

    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    implementation("androidx.annotation:annotation:1.7.0")

    implementation("androidx.cardview:cardview:1.0.0")

    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // ============================================
    // Staff App Dependencies
    // ============================================
    
    // Volley for staff API calls
    implementation("com.android.volley:volley:1.2.1")
    
    // SwipeRefreshLayout for pull-to-refresh functionality
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // CameraX for QR Code scanning in inventory system
    implementation("androidx.camera:camera-core:1.3.1")
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")

    // ML Kit for barcode scanning
    implementation("com.google.mlkit:barcode-scanning:17.2.0")

    // ViewPager2 for inventory tabs
    implementation("androidx.viewpager2:viewpager2:1.0.0")

    // Background tasks for inventory system
    implementation("androidx.work:work-runtime:2.9.0")


    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
