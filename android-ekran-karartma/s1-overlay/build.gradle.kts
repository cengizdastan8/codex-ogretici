plugins {
    id("com.android.application")
}

android {
    namespace = "dev.ekrankarartma.s1"
    compileSdk = 36

    defaultConfig {
        applicationId = "dev.ekrankarartma.s1"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
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

    testOptions {
        // Robolectric'in layout olcumu icin Android kaynaklarina ihtiyaci var.
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    // Calisma zamani bagimliligi yok; yalnizca birim testleri icin.
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.18.1")
}
