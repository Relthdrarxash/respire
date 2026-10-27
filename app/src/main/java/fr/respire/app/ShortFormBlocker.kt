package fr.respire.app

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import android.os.SystemClock
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
 * Deux vitesses :
 *  - recherche rapide : on demande directement à Android les quelques identifiants
 *    connus (un appel par identifiant), quelques millisecondes ;
 *  - exploration complète : on parcourt l'arbre de l'écran pour trouver un identifiant
 *    renommé par une mise à jour (contient « reel » ou « clips_viewer » et couvre
 *    au moins 70 % de la hauteur). Lente, donc rare ; ce qu'elle trouve est mémorisé
 *    et rejoint la recherche rapide.
 *
 * Identifiants connus repris du jeu de règles ouvert short-video-rules (MIT) et de appblock.
 */
object ShortFormBlocker {

    const val INSTAGRAM = "com.instagram.android"
    const val YOUTUBE = "com.google.android.youtube"

    private val reelsIds = mutableSetOf("clips_viewer_view_pager", "clips_video_container", "clips_viewer_container")
    private val shortsIds = mutableSetOf(
        "reel_recycler",
        "reel_player_page_container",
        "reel_watch_fragment_root",
        "reel_watch_player",
        "shorts_player",
    )

    /** Reel ouvert depuis une conversation : on le laisse passer. */
    private val dmIds = listOf("sender_username_or_fullname", "reply_bar_edittext")

    /** Le lecteur doit couvrir au moins 70 % de la hauteur (évite les vignettes de l'accueil). */
    private const val MIN_COVER = 0.7f
    private const val MAX_NODES = 1500

    data class Result(
        val blocked: Boolean,
        val windows: Int,
        val method: String,
        val nodes: Int,
        val durationMs: Long,
        /** Identifiants intéressants vus, avec leur couverture en % : « reel_recycler 98% ». */
        val seen: List<String>,
        val reason: String,
    )

    fun knownIds(pkg: String): Set<String> = if (pkg == INSTAGRAM) reelsIds else shortsIds

    /** Ajoute des identifiants appris (mémorisés dans les préférences). */
    fun learn(pkg: String, ids: Collection<String>) {
        if (pkg == INSTAGRAM) reelsIds.addAll(ids) else shortsIds.addAll(ids)
    }

    /** Fenêtres de l'appli visée ; à défaut, la fenêtre active. */
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

    /** Recherche rapide par identifiants connus. Renvoie l'identifiant trouvé, ou null. */
    fun quickMatch(roots: List<AccessibilityNodeInfo>, pkg: String): String? {
        val rect = Rect()
        for (root in roots) {
            val height = rootHeight(root)
            for (id in knownIds(pkg)) {
                val nodes = root.findAccessibilityNodeInfosByViewId("$pkg:id/$id") ?: continue
                val hit = nodes.any { n ->
                    n.isVisibleToUser && run { n.getBoundsInScreen(rect); rect.height() >= height * MIN_COVER }
                }
                if (hit) return id
            }
        }
        return null
    }

    fun isDirectMessage(roots: List<AccessibilityNodeInfo>, pkg: String): Boolean {
        if (pkg != INSTAGRAM) return false
        return dmIds.all { id ->
            roots.any { root ->
                root.findAccessibilityNodeInfosByViewId("$pkg:id/$id")?.any { it.isVisibleToUser } == true
            }
        }
    }

    /** Exploration complète de l'arbre (lente). Sert au diagnostic et à apprendre les nouveaux identifiants. */
    data class FullScan(val match: String?, val seen: List<String>, val nodes: Int)

    fun fullScan(roots: List<AccessibilityNodeInfo>, pkg: String): FullScan {
        val fallback = if (pkg == INSTAGRAM) "clips_viewer" else "reel"
        val seen = LinkedHashMap<String, Int>()
        var match: String? = null
        var count = 0
        val rect = Rect()
        for (root in roots) {
            val height = rootHeight(root)
            val queue = ArrayDeque<AccessibilityNodeInfo>()
            queue.add(root)
            while (queue.isNotEmpty() && count < MAX_NODES) {
                val node = queue.removeFirst()
                count++
                val fullId = node.viewIdResourceName
                if (fullId != null && node.isVisibleToUser) {
                    val id = fullId.substringAfter(":id/")
                    val lower = id.lowercase()
                    if ("reel" in lower || "short" in lower || "clips" in lower) {
                        node.getBoundsInScreen(rect)
                        val cover = rect.height() * 100 / height
                        if ((seen[id] ?: -1) < cover) seen[id] = cover
                        if (match == null && cover >= MIN_COVER * 100 && fallback in lower) match = id
                    }
                }
                for (i in 0 until node.childCount) node.getChild(i)?.let { queue.add(it) }
            }
        }
        return FullScan(match, seen.entries.take(25).map { "${it.key} ${it.value}%" }, count)
    }

    /** Analyse complète utilisée par le service : rapide d'abord, exploration si demandé. */
    fun scan(roots: List<AccessibilityNodeInfo>, pkg: String, explore: Boolean): Result {
        val start = SystemClock.uptimeMillis()
        if (roots.isEmpty()) {
            return Result(false, 0, "-", 0, 0, emptyList(), "aucune fenêtre lisible")
        }
        var match = quickMatch(roots, pkg)
        var method = "rapide"
        var seen = emptyList<String>()
        var nodes = 0
        if (match == null && explore) {
            val full = fullScan(roots, pkg)
            method = "exploration"
            seen = full.seen
            nodes = full.nodes
            match = full.match
        }
        val dm = match != null && isDirectMessage(roots, pkg)
        val reason = when {
            match == null -> "lecteur non trouvé"
            dm -> "Reel ouvert depuis un message, laissé passer"
            else -> "bloqué via $match"
        }
        return Result(
            blocked = match != null && !dm,
            windows = roots.size,
            method = method,
            nodes = nodes,
            durationMs = SystemClock.uptimeMillis() - start,
            seen = seen,
            reason = reason,
        )
    }

    private fun rootHeight(root: AccessibilityNodeInfo): Int {
        val r = Rect()
        root.getBoundsInScreen(r)
        return r.height().coerceAtLeast(1)
    }

    fun goBack(service: AccessibilityService) {
        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
    }
}
