package fr.respire.app

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import fr.respire.app.ui.PauseScreen
import fr.respire.app.ui.PauseUi
import fr.respire.app.ui.RespireTheme
import fr.respire.app.ui.hourLabel

/**
 * Écran de pause : respiration guidée + question d'intention + compte à rebours.
 * « Fermer » est disponible tout de suite, « Ouvrir » seulement à la fin de l'attente.
 * Si le quota du jour est atteint, seule la fermeture est possible.
 */
class PauseActivity : ComponentActivity() {

    private lateinit var prefs: Prefs
    private var pkg: String = ""
    private var decided = false
    private var blocked = false

    private val remaining = mutableIntStateOf(0)
    private val handler = Handler(Looper.getMainLooper())
    private val tick = object : Runnable {
        override fun run() {
            if (remaining.intValue > 0) {
                remaining.intValue -= 1
                if (remaining.intValue > 0) handler.postDelayed(this, 1000L)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)

        val target = intent.getStringExtra(EXTRA_PKG)
        if (target == null) {
            finish()
            return
        }
        pkg = target
        val mode = intent.getStringExtra(EXTRA_MODE) ?: MODE_OPEN
        prefs = Prefs(this)

        var label = pkg
        var icon: ImageBitmap? = null
        try {
            val ai = packageManager.getApplicationInfo(pkg, 0)
            label = packageManager.getApplicationLabel(ai).toString()
            icon = packageManager.getApplicationIcon(ai).toBitmap(96, 96).asImageBitmap()
        } catch (e: PackageManager.NameNotFoundException) {
            // Appli désinstallée entre-temps : on garde le nom du paquet.
        }

        val now = System.currentTimeMillis()
        val reason = prefs.blockReason(pkg, now)
        blocked = reason != Prefs.Block.NONE
        val riskStart = prefs.riskWindowStart(now)
        remaining.intValue = when {
            blocked -> 0
            mode == MODE_SESSION_END -> prefs.baseDelay * (if (riskStart != null) 2 else 1)
            else -> prefs.delayFor(pkg, now)
        }

        val ui = PauseUi(
            appLabel = label,
            appIcon = icon,
            sessionEnd = mode == MODE_SESSION_END,
            blocked = blocked,
            opensToday = prefs.opensToday(pkg),
            quota = prefs.dailyQuota,
            sessionMinutes = prefs.sessionMinutes,
            riskBlocked = reason == Prefs.Block.RISK,
            riskEnd = hourLabel(prefs.riskEnd),
            riskOpens = riskStart?.let { prefs.opensSince(pkg, it) },
            riskQuota = prefs.riskQuota,
        )

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = giveUp()
        })

        setContent {
            RespireTheme {
                PauseScreen(
                    ui = ui,
                    remaining = remaining.intValue,
                    onClose = ::giveUp,
                    onOpen = ::openAnyway,
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        showingFor = pkg
        // Le compte à rebours n'avance que si l'écran est visible.
        handler.removeCallbacks(tick)
        if (remaining.intValue > 0) handler.postDelayed(tick, 1000L)
    }

    override fun onPause() {
        if (showingFor == pkg) showingFor = null
        handler.removeCallbacks(tick)
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        // Parti sans choisir (Accueil, applis récentes...) : on ne laisse pas traîner l'écran.
        if (!decided) finish()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    private fun giveUp() {
        if (decided) return
        decided = true
        prefs.recordGiveUp(System.currentTimeMillis(), pkg)
        startActivity(
            Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_HOME)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        finish()
    }

    private fun openAnyway() {
        if (decided || blocked || remaining.intValue > 0) return
        decided = true
        val now = System.currentTimeMillis()
        prefs.recordOpen(pkg, now)
        prefs.allow(pkg, now + prefs.sessionMinutes * 60_000L)
        packageManager.getLaunchIntentForPackage(pkg)?.let { launch ->
            launch.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            )
            startActivity(launch)
        }
        finish()
    }

    companion object {
        const val EXTRA_PKG = "pkg"
        const val EXTRA_MODE = "mode"
        const val MODE_OPEN = "open"
        const val MODE_SESSION_END = "session_end"

        /** Paquet pour lequel l'écran de pause est actuellement au premier plan. */
        @Volatile
        var showingFor: String? = null
    }
}
