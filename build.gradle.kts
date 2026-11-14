plugins {
    id("com.android.application") version "8.10.1" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
    // Captures d'écran sans émulateur (./tools/screenshots.sh) ; sans effet sur l'APK.
    id("app.cash.paparazzi") version "1.3.5" apply false
}
