plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    // Captures d'écran sans émulateur (./tools/screenshots.sh) ; sans effet sur l'APK.
    id("app.cash.paparazzi")
}

android {
    namespace = "fr.respire.app"
    // 35 et non 36 : Paparazzi 1.3.5 ne sait pas lire les propriétés Build de la plateforme 36 (targetSdk reste 36).
    compileSdk = 35

    defaultConfig {
        applicationId = "fr.respire.app"
        minSdk = 26
        targetSdk = 36
        // En publication, la CI calcule le numéro à partir de l'étiquette (v4.2.0 -> 40200).
        versionCode = System.getenv("RESPIRE_VERSION_CODE")?.toIntOrNull() ?: 40101
        versionName = System.getenv("RESPIRE_VERSION_NAME") ?: "4.1.1-dev"
        manifestPlaceholders["allowAutoBackup"] =
            providers.gradleProperty("pilmil.allowAutoBackup").getOrElse("false")
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
                // Mot de passe de clé distinct si fourni, sinon celui du keystore.
                keyPassword = System.getenv("RELEASE_KEY_PASSWORD")?.takeIf { it.isNotEmpty() } ?: System.getenv("RELEASE_STORE_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // Jamais de repli sur la clé de test (publique) : voir le garde-fou plus bas.
            signingConfig = signingConfigs.findByName("release")
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

// Une variante release sans clé d'envoi échoue au lieu d'être signée avec la clé de test publique.
// Les tests unitaires et les builds debug ne sont pas concernés.
gradle.taskGraph.whenReady {
    val buildsRelease = allTasks.any { t ->
        t.project == project && Regex("^(assemble|bundle|package|sign|validateSigning).*Release$").matches(t.name)
    }
    if (buildsRelease && project.extensions.getByType<com.android.build.api.dsl.ApplicationExtension>()
            .signingConfigs.findByName("release") == null) {
        throw GradleException(
            "Signature release impossible : clé d'envoi absente. Définir les variables d'environnement " +
                "RELEASE_KEYSTORE (chemin d'un fichier existant), RELEASE_STORE_PASSWORD, RELEASE_KEY_ALIAS " +
                "et, si différent, RELEASE_KEY_PASSWORD. Pour un test local, utiliser assembleDebug."
        )
    }
}

dependencies {
    implementation("fr.pilmil.core:core-platform:0.1.0")
    val composeBom = platform("androidx.compose:compose-bom:2024.10.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.work:work-runtime-ktx:2.10.0")

    testImplementation("junit:junit:4.13.2")
}

// Trace complète en cas d'échec d'une capture, lisible directement dans le journal.
tasks.withType<Test>().configureEach {
    testLogging {
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        events("failed")
    }
}
