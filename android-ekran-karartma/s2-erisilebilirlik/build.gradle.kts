plugins {
    id("com.android.application")
}

android {
    namespace = "dev.ekrankarartma.s2"
    compileSdk = 36

    defaultConfig {
        applicationId = "dev.ekrankarartma.s2"
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
        // Robolectric birim testlerinin layout, string ve xml kaynaklarina erisebilmesi icin.
        unitTests.isIncludeAndroidResources = true
    }
}

// Calisma zamaninda sifir dis bagimlilik. Yalnizca test icin:
dependencies {
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.18.1")
}
