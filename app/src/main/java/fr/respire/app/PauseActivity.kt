package fr.respire.app

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView

/**
 * Écran de pause : respiration guidée + question d'intention + compte à rebours.
 * « Fermer » est disponible tout de suite, « Ouvrir » seulement à la fin de l'attente.
 * Si le quota du jour est atteint, seule la fermeture est possible.
 */
class PauseActivity : Activity() {

    private lateinit var prefs: Prefs
    private lateinit var pkg: String
    private lateinit var mode: String

    private lateinit var tvCountdown: TextView
    private lateinit var tvHint: TextView
    private lateinit var btnOpen: Button
    private var breath: ObjectAnimator? = null

    private val handler = Handler(Looper.getMainLooper())
    private var remaining = 0
    private var decided = false
    private var blocked = false

    private val tick = object : Runnable {
        override fun run() {
            if (remaining <= 0) {
                tvCountdown.text = ""
                tvHint.text = getString(R.string.pause_ready)
                btnOpen.isEnabled = true
                return
            }
            tvCountdown.text = remaining.toString()
            remaining--
            handler.postDelayed(this, 1000L)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val target = intent.getStringExtra(EXTRA_PKG)
        if (target == null) {
            finish()
            return
        }
        pkg = target
        mode = intent.getStringExtra(EXTRA_MODE) ?: MODE_OPEN
        prefs = Prefs(this)
        setContentView(R.layout.activity_pause)

        val icon = findViewById<ImageView>(R.id.appIcon)
        val name = findViewById<TextView>(R.id.appName)
        val question = findViewById<TextView>(R.id.question)
        val stats = findViewById<TextView>(R.id.stats)
        tvCountdown = findViewById(R.id.countdown)
        tvHint = findViewById(R.id.hint)
        btnOpen = findViewById(R.id.btnOpen)
        val btnClose = findViewById<Button>(R.id.btnClose)

        val label = try {
            val ai = packageManager.getApplicationInfo(pkg, 0)
            icon.setImageDrawable(packageManager.getApplicationIcon(ai))
            packageManager.getApplicationLabel(ai).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            pkg
        }
        name.text = label

        val now = System.currentTimeMillis()
        val opens = prefs.opensToday(pkg)
        val quota = prefs.dailyQuota
        blocked = prefs.quotaReached(pkg)

        stats.text = if (quota > 0) {
            getString(R.string.pause_stats_quota, opens, quota)
        } else {
            getString(R.string.pause_stats, opens)
        }

        when {
            blocked -> {
                question.text = getString(R.string.pause_blocked)
                tvHint.text = getString(R.string.pause_blocked_hint)
                btnOpen.visibility = View.GONE
                remaining = 0
                tvCountdown.text = ""
            }
            mode == MODE_SESSION_END -> {
                question.text = getString(R.string.pause_session_question, prefs.sessionMinutes)
                tvHint.text = getString(R.string.pause_breathe)
                btnOpen.text = getString(R.string.pause_continue)
                remaining = prefs.baseDelay
            }
            else -> {
                question.text = getString(R.string.pause_question)
                tvHint.text = getString(R.string.pause_breathe)
                btnOpen.text = getString(R.string.pause_open)
                remaining = prefs.delayFor(pkg, now)
            }
        }
        btnOpen.isEnabled = false

        btnClose.setOnClickListener { giveUp() }
        btnOpen.setOnClickListener { openAnyway() }

        breath = ObjectAnimator.ofPropertyValuesHolder(
            findViewById<View>(R.id.breath),
            PropertyValuesHolder.ofFloat(View.SCALE_X, 0.55f, 1f),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.55f, 1f),
        ).apply {
            duration = 4000L // 4 s inspiration, 4 s expiration
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    override fun onResume() {
        super.onResume()
        showingFor = pkg
        // Le compte à rebours n'avance que si l'écran est visible.
        if (!blocked) {
            handler.removeCallbacks(tick)
            handler.post(tick)
        }
    }

    override fun onPause() {
        if (showingFor == pkg) showingFor = null
        handler.removeCallbacks(tick)
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        // Parti sans choisir (bouton Accueil, appli récente...) : on ne laisse pas traîner l'écran.
        if (!decided) finish()
    }

    override fun onDestroy() {
        breath?.cancel()
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    @Deprecated("Retour = renoncer")
    override fun onBackPressed() {
        giveUp()
    }

    private fun giveUp() {
        if (decided) return
        decided = true
        prefs.recordGiveUp(System.currentTimeMillis())
        startActivity(
            Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_HOME)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        finish()
    }

    private fun openAnyway() {
        if (decided || blocked || remaining > 0) return
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
