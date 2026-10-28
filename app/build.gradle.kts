plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "fr.respire.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "fr.respire.app"
        minSdk = 26
        targetSdk = 36
        // En publication, la CI calcule le numéro à partir de l'étiquette (v4.2.0 -> 40200).
        versionCode = System.getenv("RESPIRE_VERSION_CODE")?.toIntOrNull() ?: 13
        versionName = System.getenv("RESPIRE_VERSION_NAME") ?: "4.1"
    }

    // Clé fixe : chaque compilation est signée pareil, donc les mises à jour
    // s'installent par-dessus la version précédente sans la désinstaller.
    signingConfigs {
        getByName("debug") {
            storeFile = file("respire-debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
        // Clé d'envoi Google Play : fournie par la CI (secrets GitHub), jamais versionnée.
        val keystore = System.getenv("RELEASE_KEYSTORE")
        if (keystore != null && file(keystore).exists()) {
            create("release") {
                storeFile = file(keystore)
                storePassword = System.getenv("RELEASE_STORE_PASSWORD")
                keyAlias = System.getenv("RELEASE_KEY_ALIAS")
                keyPassword = System.getenv("RELEASE_STORE_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // Sans clé d'envoi (compilation locale), on signe avec la clé de test du dépôt.
            signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.10.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.core:core-ktx:1.13.1")

    testImplementation("junit:junit:4.13.2")
}
