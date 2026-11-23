plugins {
    id("com.android.application")
}

android {
    namespace = "dev.ekrankarartma.s3"
    compileSdk = 36

    defaultConfig {
        applicationId = "dev.ekrankarartma.s3"
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
        // Robolectric'in layout ölçümü için Android kaynaklarına ihtiyaç var.
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    // Çalışma zamanı bağımlılığı yok; yalnızca birim testleri için.
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.18.1")
}
