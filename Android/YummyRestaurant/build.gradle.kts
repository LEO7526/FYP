plugins {
    id("com.android.application") version "8.10.1" apply false
    id("com.google.gms.google-services") version "4.3.15" apply false
    alias(libs.plugins.google.firebase.crashlytics) apply false
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}
