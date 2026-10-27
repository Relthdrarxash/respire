package fr.respire.app

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.inputmethod.InputMethodManager

/**
 * Service d'accessibilité : il ne lit QUE le nom du paquet de l'appli au premier plan
 * (aucun contenu d'écran, canRetrieveWindowContent = false).
 *
 * Deux déclencheurs :
 *  1. Ouverture : passage d'une autre appli vers une appli ciblée.
 *  2. Fin de session : l'utilisateur est toujours dans l'appli quand la session autorisée expire.
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
    }

    /** Fenêtres qui ne doivent pas compter comme « changer d'appli » (clavier, barre système). */
    private fun computeIgnored(): Set<String> {
        val set = mutableSetOf("com.android.systemui", "android")
        getSystemService(InputMethodManager::class.java)
            ?.enabledInputMethodList
            ?.forEach { set.add(it.packageName) }
        return set
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return
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
    }
}
