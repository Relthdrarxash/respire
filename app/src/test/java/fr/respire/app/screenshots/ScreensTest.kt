package fr.respire.app.screenshots

import androidx.compose.runtime.Composable
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import fr.respire.app.ui.HomeScreen
import fr.respire.app.ui.HomeUi
import fr.respire.app.ui.PauseScreen
import fr.respire.app.ui.PauseUi
import fr.respire.app.ui.PickerScreen
import fr.respire.app.ui.RespireTab
import fr.respire.app.ui.RespireTheme
import org.junit.Rule
import org.junit.Test

/**
 * Une capture par écran, sans téléphone ni émulateur. Les images sortent dans
 * app/src/test/snapshots/images ; tools/screenshots.sh les copie vers docs/marketing/captures.
 * L'appli n'a qu'un thème (clair « Calme ») : pas de variante sombre.
 * Non rendus : les boîtes de dialogue (fenêtres séparées, que Paparazzi ne capture pas), le service
 * d'accessibilité qui surveille les applis et l'écran de pause tel qu'il s'affiche par-dessus une autre appli
 * (PauseActivity) ; l'écran de pause est rendu ici par son composable seul.
 */
abstract class ScreensBase(locale: String, echantillon: () -> SampleBase) {

    // Paresseux : les bitmaps de Sample exigent l'environnement Android de Paparazzi, pas encore prêt à la construction.
    private val Sample by lazy(echantillon)

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5.copy(locale = locale),
        // Sans cela Paparazzi réduit les images à 1000 px de haut ; on veut les 1080x2340 natifs.
        useDeviceResolution = true,
    )

    private fun shot(name: String, content: @Composable () -> Unit) {
        paparazzi.snapshot(name) { RespireTheme { content() } }
    }

    @Composable
    private fun accueil(ui: HomeUi, onglet: RespireTab) = HomeScreen(
        ui = ui,
        onOpenAccessibility = {},
        onOpenLink = {},
        onPickApps = {},
        onSaveRule = { _, _ -> },
        onSetShortForm = { _, _ -> },
        onSaveRisk = { _, _, _, _ -> },
        appWeek = { Sample.semaineAppli() },
        initialTab = onglet,
    )

    /** Ouverture de l'appli : Respire protège, quelques applis surveillées, filtres de vidéos courtes. */
    @Test fun accueil() = shot("accueil") { accueil(Sample.accueil(), RespireTab.APPS) }

    /** Première utilisation : la liste de démarrage (service coupé, aucune appli choisie). */
    @Test fun demarrage() = shot("demarrage") {
        accueil(Sample.accueil(serviceActif = false, cibles = emptyList()), RespireTab.APPS)
    }

    @Test fun statistiques() = shot("statistiques") { accueil(Sample.accueil(), RespireTab.STATS) }

    @Test fun reglages() = shot("reglages") { accueil(Sample.accueil(), RespireTab.SETTINGS) }

    @Test fun choixApplis() = shot("choix_applis") {
        PickerScreen(
            apps = Sample.toutes,
            selected = setOf(Sample.flux.pkg, Sample.videos.pkg, Sample.reseau.pkg),
            onToggle = { _, _ -> },
            onBack = {},
    )
    }

    /** Le geste principal : la respiration avant l'ouverture d'une appli (compte à rebours en cours). */
    @Test fun respiration() = shot("respiration") {
        PauseScreen(
            ui = PauseUi(
                appLabel = Sample.flux.label,
                appIcon = Sample.flux.icon,
                sessionEnd = false,
                blocked = false,
                opensToday = 3,
                quota = 5,
                sessionMinutes = 10,
            ),
            remaining = 7,
            onClose = {},
            onOpen = {},
    )
    }

    /** Quota du jour atteint : l'appli reste fermée. */
    @Test fun quotaAtteint() = shot("quota_atteint") {
        PauseScreen(
            ui = PauseUi(
                appLabel = Sample.videos.label,
                appIcon = Sample.videos.icon,
                sessionEnd = false,
                blocked = true,
                opensToday = 5,
                quota = 5,
                sessionMinutes = 10,
            ),
            remaining = 0,
            onClose = {},
            onOpen = {},
    )
    }
}

/** Captures en français (dossier docs/marketing/captures/). */
class ScreensTest : ScreensBase("fr", { Sample })

/** Mêmes écrans en anglais, pour les visuels marketing en (docs/marketing/captures/en/). */
class ScreensEnTest : ScreensBase("en", { SampleEn })
