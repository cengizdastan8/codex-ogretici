pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ekran-karartma"

// Üç senaryo, üç ayrı uygulama. Her biri kendi APK'sını üretir.
include(":s1-overlay")          // Senaryo 1: "Diğer uygulamaların üzerinde göster" katmanı
include(":s2-erisilebilirlik")  // Senaryo 2: Erişilebilirlik servisi katmanı
include(":s3-birlesik")         // Senaryo 3: Rakiplerden derlenen birleşik ürün
