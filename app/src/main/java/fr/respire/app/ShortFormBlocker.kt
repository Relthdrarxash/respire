package fr.respire.app

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityWindowInfo

/**
 * Repère le lecteur plein écran des Reels (Instagram) et des Shorts (YouTube)
 * à partir des identifiants techniques des vues, puis fait « retour ».
 * Le reste de l'appli reste utilisable.
 *
 * Seuls les identifiants de vues sont consultés, uniquement dans ces deux applis,
 * uniquement si l'option est active. Rien n'est envoyé : le diagnostic reste
 * dans les préférences locales et ne contient que des identifiants techniques.
 *
 * Identifiants connus repris du jeu de règles ouvert short-video-rules (MIT) et de appblock.
 * Comme ils changent avec les mises à jour, une détection de secours accepte tout
 * identifiant contenant « reel » (YouTube) ou « clips_viewer » (Instagram) qui couvre
 * au moins 70 % de la hauteur de la fenêtre.
 */
object ShortFormBlocker {

    const val INSTAGRAM = "com.instagram.android"
    const val YOUTUBE = "com.google.android.youtube"

    private val reelsIds = setOf("clips_viewer_view_pager", "clips_video_container", "clips_viewer_container")
    private val shortsIds = setOf(
        "reel_recycler",
        "reel_player_page_container",
        "reel_watch_fragment_root",
        "reel_watch_player",
        "shorts_player",
    )

    /** Reel ouvert depuis une conversation : on le laisse passer. */
    private val dmIds = setOf("sender_username_or_fullname", "reply_bar_edittext")

    /** Le lecteur doit couvrir au moins 70 % de la hauteur (évite les vignettes de l'accueil). */
    private const val MIN_COVER = 0.7f
    private const val MAX_NODES = 2500

    data class Result(
        val blocked: Boolean,
        val windows: Int,
        val nodes: Int,
        /** Identifiants intéressants vus, avec leur couverture en % : « reel_recycler 98% ». */
        val seen: List<String>,
        val reason: String,
    )

    /** Fenêtres de l'appli visées ; à défaut, la fenêtre active. */
    fun roots(service: AccessibilityService, pkg: String): List<AccessibilityNodeInfo> {
        val fromWindows = try {
            service.windows
                .filter { it.type == AccessibilityWindowInfo.TYPE_APPLICATION }
                .mapNotNull { it.root }
                .filter { it.packageName?.toString() == pkg }
        } catch (e: RuntimeException) {
            emptyList()
        }
        if (fromWindows.isNotEmpty()) return fromWindows
        val active = service.rootInActiveWindow ?: return emptyList()
        return if (active.packageName?.toString() == pkg) listOf(active) else emptyList()
    }

    fun scan(roots: List<AccessibilityNodeInfo>, pkg: String): Result {
        val known = if (pkg == INSTAGRAM) reelsIds else shortsIds
        val fallback = if (pkg == INSTAGRAM) "clips_viewer" else "reel"
        val seen = LinkedHashMap<String, Int>()
        val present = HashSet<String>()
        var match: String? = null
        var count = 0

        for (root in roots) {
            val rootRect = Rect().also { root.getBoundsInScreen(it) }
            val height = rootRect.height().coerceAtLeast(1)
            val queue = ArrayDeque<AccessibilityNodeInfo>()
            queue.add(root)
            val rect = Rect()
            while (queue.isNotEmpty() && count < MAX_NODES) {
                val node = queue.removeFirst()
                count++
                val fullId = node.viewIdResourceName
                if (fullId != null && node.isVisibleToUser) {
                    val id = fullId.substringAfter(":id/")
                    present.add(id)
                    val lower = id.lowercase()
                    if ("reel" in lower || "short" in lower || "clips" in lower) {
                        node.getBoundsInScreen(rect)
                        val cover = rect.height() * 100 / height
                        if ((seen[id] ?: -1) < cover) seen[id] = cover
                        if (match == null && cover >= MIN_COVER * 100 && (id in known || fallback in lower)) {
                            match = id
                        }
                    }
                }
                for (i in 0 until node.childCount) node.getChild(i)?.let { queue.add(it) }
            }
        }

        val dm = pkg == INSTAGRAM && dmIds.all { it in present }
        val blocked = match != null && !dm
        val reason = when {
            roots.isEmpty() -> "aucune fenêtre lisible"
            match == null -> "lecteur non trouvé"
            dm -> "Reel ouvert depuis un message, laissé passer"
            else -> "bloqué via $match"
        }
        return Result(
            blocked = blocked,
            windows = roots.size,
            nodes = count,
            seen = seen.entries.take(25).map { "${it.key} ${it.value}%" },
            reason = reason,
        )
    }

    fun goBack(service: AccessibilityService) {
        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
    }
}
