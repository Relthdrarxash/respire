package fr.respire.app

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import android.view.inputmethod.InputMethodManager

/**
 * Service d'accessibilité.
 *
 * Pauses : seul le nom du paquet de l'appli au premier plan est utilisé.
 *  1. Ouverture : passage d'une autre appli vers une appli ciblée.
 *  2. Fin de session : l'utilisateur est toujours dans l'appli quand la session autorisée expire.
 *
 * Reels / Shorts (options) : dans Instagram et YouTube uniquement, les identifiants
 * techniques des vues sont consultés pour repérer le lecteur plein écran (voir ShortFormBlocker).
 */
class PauseService : AccessibilityService() {

    private lateinit var prefs: Prefs
    private val handler = Handler(Looper.getMainLooper())

    private var lastPkg: String? = null
    private val lastTrigger = HashMap<String, Long>()
    private var ignored: Set<String> = emptySet()

    private var sessionPkg: String? = null
    private val sessionCheck = Runnable { onSessionTimer() }

    override fun onCreate() {
        super.onCreate()
        prefs = Prefs(this)
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        ignored = computeIgnored()
        for (pkg in listOf(ShortFormBlocker.INSTAGRAM, ShortFormBlocker.YOUTUBE)) {
            ShortFormBlocker.learn(pkg, prefs.learnedShortIds(pkg))
        }
    }

    /** Fenêtres qui ne doivent pas compter comme « changer d'appli » (clavier, barre système). */
    private fun computeIgnored(): Set<String> {
        val set = mutableSetOf("com.android.systemui", "android")
        getSystemService(InputMethodManager::class.java)
            ?.enabledInputMethodList
            ?.forEach { set.add(it.packageName) }
        return set
    }

    // ---- Reels / Shorts ---------------------------------------------------------

    private var shortPkg: String? = null
    private var shortScheduled = false
    private val shortCheck = Runnable { runShortCheck() }

    private fun onShortFormCandidate(pkg: String) {
        val enabled = when (pkg) {
            ShortFormBlocker.INSTAGRAM -> prefs.blockReels
            ShortFormBlocker.YOUTUBE -> prefs.blockShorts
            else -> false
        }
        if (!enabled) return
        shortPkg = pkg
        // Au plus une vérification toutes les 120 ms, même si les événements s'enchaînent.
        if (!shortScheduled) {
            shortScheduled = true
            handler.postDelayed(shortCheck, SHORT_CHECK_MS)
        }
    }

    private var lastDiag = 0L
    private var lastExplore = 0L
    private var cooldownUntil = 0L
    private var consecutiveBacks = 0

    private fun runShortCheck() {
        shortScheduled = false
        val pkg = shortPkg ?: return
        val now = SystemClock.uptimeMillis()

        // Juste après un « retour », l'écran est encore en transition : on attend,
        // puis on vérifie une seule fois si le lecteur est toujours là.
        if (now < cooldownUntil) {
            shortScheduled = true
            handler.postDelayed(shortCheck, cooldownUntil - now)
            return
        }

        val roots = ShortFormBlocker.roots(this, pkg)
        if (roots.isEmpty()) return

        val explore = now - lastExplore > EXPLORE_EVERY_MS
        if (explore) lastExplore = now
        val result = ShortFormBlocker.scan(roots, pkg, explore)

        if (result.method == "exploration" && result.blocked) {
            val id = result.reason.substringAfter("via ", "")
            if (id.isNotEmpty()) {
                ShortFormBlocker.learn(pkg, listOf(id))
                prefs.addLearnedShortId(pkg, id)
            }
        }
        if (result.blocked || result.method == "exploration" || now - lastDiag > DIAG_EVERY_MS) {
            lastDiag = now
            prefs.saveShortDiagnostic(pkg, result)
        }

        if (!result.blocked) {
            consecutiveBacks = 0
            return
        }
        // Garde-fou : jamais plus de quelques « retour » d'affilée, pour ne pas sortir de l'appli.
        if (consecutiveBacks >= MAX_CONSECUTIVE_BACKS) return
        consecutiveBacks++
        cooldownUntil = now + BACK_COOLDOWN_MS
        ShortFormBlocker.goBack(this)
        prefs.recordShortBlocked(System.currentTimeMillis())
        if (consecutiveBacks == 1) {
            val msg = if (pkg == ShortFormBlocker.INSTAGRAM) R.string.toast_reels_blocked else R.string.toast_shorts_blocked
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
        // Vérification de contrôle à la fin de la transition.
        shortScheduled = true
        handler.postDelayed(shortCheck, BACK_COOLDOWN_MS)
    }

    // ---- Pauses -----------------------------------------------------------------

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg == ShortFormBlocker.INSTAGRAM || pkg == ShortFormBlocker.YOUTUBE) {
            onShortFormCandidate(pkg)
        }
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        if (pkg in ignored) return

        if (pkg == packageName) {
            lastPkg = pkg
            return
        }
        // L'appli ciblée qui finit de se charger derrière notre écran de pause : on ignore.
        if (PauseActivity.showingFor == pkg) {
            val until = prefs.allowedUntil(pkg)
            if (System.currentTimeMillis() < until) scheduleSessionCheck(pkg, until)
            return
        }

        val previous = lastPkg
        lastPkg = pkg
        if (pkg == previous) return

        val targets = prefs.targets
        if (pkg !in targets) return

        val now = System.currentTimeMillis()
        val until = prefs.allowedUntil(pkg)
        if (now < until) {
            // Retour dans une session encore valide : on surveille sa fin.
            scheduleSessionCheck(pkg, until)
            return
        }
        if (now - (lastTrigger[pkg] ?: 0L) < DEBOUNCE_MS) return
        lastTrigger[pkg] = now

        prefs.recordAttempt(pkg, now)
        showPause(pkg, PauseActivity.MODE_OPEN)
    }

    private fun scheduleSessionCheck(pkg: String, until: Long) {
        handler.removeCallbacks(sessionCheck)
        sessionPkg = pkg
        val delay = (until - System.currentTimeMillis()).coerceAtLeast(0L) + 500L
        handler.postDelayed(sessionCheck, delay)
    }

    private fun onSessionTimer() {
        val pkg = sessionPkg ?: return
        sessionPkg = null
        // Toujours dans l'appli et session expirée : on interrompt le défilement.
        if (lastPkg == pkg && System.currentTimeMillis() >= prefs.allowedUntil(pkg)) {
            showPause(pkg, PauseActivity.MODE_SESSION_END)
        }
    }

    private fun showPause(pkg: String, mode: String) {
        val intent = Intent(this, PauseActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            putExtra(PauseActivity.EXTRA_PKG, pkg)
            putExtra(PauseActivity.EXTRA_MODE, mode)
        }
        startActivity(intent)
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    companion object {
        private const val DEBOUNCE_MS = 1500L
        private const val SHORT_CHECK_MS = 120L
        private const val DIAG_EVERY_MS = 2000L
        private const val EXPLORE_EVERY_MS = 6000L
        private const val BACK_COOLDOWN_MS = 900L
        private const val MAX_CONSECUTIVE_BACKS = 3
    }
}
