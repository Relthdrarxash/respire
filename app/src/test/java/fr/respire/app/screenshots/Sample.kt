package fr.respire.app.screenshots

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import fr.respire.app.Prefs
import fr.respire.app.ui.AppItem
import fr.respire.app.ui.DayDetail
import fr.respire.app.ui.HomeUi
import fr.respire.app.ui.RiskUi
import fr.respire.app.ui.Rule
import fr.respire.app.ui.TargetStat

/** Données FABRIQUÉES pour les captures : aucune donnée d'usage réelle, valeurs déterministes. */
/** Libellés dans la langue demandée (fr par défaut) ; `Sample` est la version française, `SampleEn` l'anglaise. */
open class SampleBase(private val en: Boolean) {

    /** Pastille unie en guise d'icône : les vraies icônes des applis ne sont pas disponibles hors appareil. */
    private fun icone(couleur: Int): ImageBitmap {
        val bitmap = Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888)
        Canvas(bitmap).drawRect(0f, 0f, 96f, 96f, Paint().apply { color = couleur })
        return bitmap.asImageBitmap()
    }

    val flux = AppItem("exemple.flux", if (en) "Feed" else "Flux", icone(0xFFB5654A.toInt()))
    val videos = AppItem("exemple.videos", if (en) "Short videos" else "Vidéos courtes", icone(0xFF5C7C99.toInt()))
    val reseau = AppItem("exemple.reseau", if (en) "Social network" else "Réseau social", icone(0xFF7A6B99.toInt()))
    val jeu = AppItem("exemple.jeu", if (en) "Evening game" else "Jeu du soir", icone(0xFF6A9A6E.toInt()))
    val actualites = AppItem("exemple.actus", if (en) "News" else "Actualités", icone(0xFFC9A24B.toInt()))
    val photos = AppItem("exemple.photos", if (en) "Photos" else "Photos", icone(0xFF4E8A8A.toInt()))
    val messagerie = AppItem("exemple.messagerie", if (en) "Messaging" else "Messagerie", icone(0xFF8A6A7A.toInt()))

    val toutes = listOf(actualites, flux, jeu, messagerie, photos, reseau, videos)

    val cibles = listOf(
        TargetStat(flux, opens = 3),
        TargetStat(videos, opens = 5, blocked = true),
        TargetStat(reseau, opens = 1),
    )

    /** 24 valeurs, avec un pic en soirée. */
    private fun heures(poids: Int, jour: Int): List<Int> =
        (0 until 24).map { h ->
            val base = when (h) { in 21..23 -> 3; in 12..13 -> 2; in 7..8 -> 1; else -> 0 }
            if (base == 0) 0 else (base * poids + (jour + h) % 3) / 3
        }

    /** 8 semaines (56 jours) du plus ancien à aujourd'hui, avec une tendance à la baisse. */
    val jours: List<DayDetail> = (0 until 56).map { i ->
        val poids = 9 - i / 8
        val tentations = heures(poids, i)
        DayDetail(
            title = (if (en) "Day" else "Jour") + " ${i + 1}",
            attempts = tentations,
            giveUps = tentations.map { it * 2 / 3 },
            shorts = List(24) { 0 },
        )
    }

    /** Relief de la dernière semaine (lundi à dimanche) : jours variés, un sans pause, un fort. */
    private val reliefSemaine = listOf(7, 4, 0, 11, 3, 8, 6)

    val semaine: List<Int> = jours.map { it.giveUps.sum() }.let { base ->
        base.dropLast(7) + reliefSemaine
    }

    val etiquettes = (if (en) listOf("M", "T", "W", "T", "F", "S", "S") else listOf("L", "M", "M", "J", "V", "S", "D")).let { s -> List(56) { s[it % 7] } }

    fun accueil(serviceActif: Boolean = true, cibles: List<TargetStat> = this.cibles) = HomeUi(
        serviceOn = serviceActif,
        giveUps = 6,
        pauses = 9,
        week = semaine,
        weekLabels = etiquettes,
        targets = cibles,
        rules = mapOf(Rule.DELAY to 10, Rule.STEP to 3, Rule.SESSION to 10, Rule.QUOTA to 5),
        blockReels = true,
        blockShorts = true,
        blockFacebookReels = false,
        facebookInstalled = true,
        shortsBlockedToday = 4,
        diagnostic = "",
        days = jours,
        risk = RiskUi(enabled = true, start = 21, end = 0, quota = 2, active = false),
        version = "4.1.1",
        suggestions = listOf(flux, videos, reseau),
        canNotify = true,
        showReviewPrompt = false,
    )

    /** Semaine d'une appli : 7 jours, 24 heures. */
    fun semaineAppli() = Prefs.AppWeek(
        attempts = listOf(4, 3, 5, 2, 3, 1, 2),
        opens = listOf(2, 1, 2, 1, 1, 0, 1),
        giveUps = listOf(2, 2, 3, 1, 2, 1, 1),
        shorts = listOf(1, 0, 2, 0, 1, 0, 0),
        hours = heures(3, 0),
    )
}


object Sample : SampleBase(false)

object SampleEn : SampleBase(true)
