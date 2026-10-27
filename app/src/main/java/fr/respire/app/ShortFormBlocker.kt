package fr.respire.app

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Repère le lecteur plein écran des Reels (Instagram) et des Shorts (YouTube)
 * à partir des identifiants techniques des vues, puis fait « retour ».
 * Le reste de l'appli reste utilisable.
 *
 * Seuls les identifiants de vues sont consultés, uniquement dans ces deux applis,
 * uniquement si l'option est active. Rien n'est enregistré ni envoyé.
 *
 * Identifiants repris du jeu de règles ouvert short-video-rules (MIT) et de appblock.
 * Ils peuvent changer avec les mises à jour d'Instagram ou de YouTube.
 */
object ShortFormBlocker {

    const val INSTAGRAM = "com.instagram.android"
    const val YOUTUBE = "com.google.android.youtube"

    private val reelsIds = listOf("clips_viewer_view_pager", "clips_video_container")

    /** Reel ouvert depuis une conversation : on le laisse passer. */
    private val dmRequiredIds = listOf("sender_username_or_fullname", "reply_bar_edittext")

    private val shortsIds = listOf(
        "reel_recycler",
        "reel_player_page_container",
        "reel_watch_fragment_root",
        "reel_watch_player",
    )

    /** Le lecteur doit couvrir au moins 70 % de la hauteur (évite les vignettes de l'accueil). */
    private const val MIN_COVER = 0.7f

    fun isShortFormOnScreen(root: AccessibilityNodeInfo, pkg: String): Boolean {
        val ids = when (pkg) {
            INSTAGRAM -> reelsIds
            YOUTUBE -> shortsIds
            else -> return false
        }
        val rootRect = Rect().also { root.getBoundsInScreen(it) }
        if (rootRect.height() <= 0) return false

        val playerVisible = ids.any { id -> coversScreen(root, "$pkg:id/$id", rootRect) }
        if (!playerVisible) return false

        if (pkg == INSTAGRAM && dmRequiredIds.all { id -> exists(root, "$pkg:id/$id") }) {
            return false
        }
        return true
    }

    private fun coversScreen(root: AccessibilityNodeInfo, fullId: String, rootRect: Rect): Boolean {
        val nodes = root.findAccessibilityNodeInfosByViewId(fullId) ?: return false
        val rect = Rect()
        return nodes.any { node ->
            if (!node.isVisibleToUser) return@any false
            node.getBoundsInScreen(rect)
            rect.height() >= rootRect.height() * MIN_COVER
        }
    }

    private fun exists(root: AccessibilityNodeInfo, fullId: String): Boolean =
        root.findAccessibilityNodeInfosByViewId(fullId)?.any { it.isVisibleToUser } == true

    fun goBack(service: AccessibilityService) {
        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
    }
}
