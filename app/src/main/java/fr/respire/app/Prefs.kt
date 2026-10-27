package fr.respire.app

import android.content.Context
import android.content.SharedPreferences
import java.util.Calendar

/**
 * Stockage local (SharedPreferences). Aucune donnée ne quitte le téléphone :
 * l'appli ne demande même pas la permission INTERNET.
 */
class Prefs(context: Context) {

    private val sp: SharedPreferences =
        context.applicationContext.getSharedPreferences("respire", Context.MODE_PRIVATE)

    // ---- Réglages ----------------------------------------------------------

    var targets: Set<String>
        get() = sp.getStringSet(K_TARGETS, emptySet())?.toSet() ?: emptySet()
        set(value) = sp.edit().putStringSet(K_TARGETS, HashSet(value)).apply()

    /** Attente de base avant de pouvoir ouvrir, en secondes. */
    var baseDelay: Int
        get() = sp.getInt(K_BASE, 10)
        set(v) = sp.edit().putInt(K_BASE, v).apply()

    /** Secondes ajoutées pour chaque tentative déjà faite dans l'heure. */
    var stepDelay: Int
        get() = sp.getInt(K_STEP, 5)
        set(v) = sp.edit().putInt(K_STEP, v).apply()

    /** Durée d'une session autorisée avant que la pause ne revienne, en minutes. */
    var sessionMinutes: Int
        get() = sp.getInt(K_SESSION, 10)
        set(v) = sp.edit().putInt(K_SESSION, v).apply()

    /** Nombre d'ouvertures (ou prolongations) autorisées par appli et par jour. 0 = illimité. */
    var dailyQuota: Int
        get() = sp.getInt(K_QUOTA, 6)
        set(v) = sp.edit().putInt(K_QUOTA, v).apply()

    /** Masquer les Reels d'Instagram. */
    var blockReels: Boolean
        get() = sp.getBoolean(K_REELS, false)
        set(v) = sp.edit().putBoolean(K_REELS, v).apply()

    /** Masquer les Shorts de YouTube. */
    var blockShorts: Boolean
        get() = sp.getBoolean(K_SHORTS, false)
        set(v) = sp.edit().putBoolean(K_SHORTS, v).apply()

    // ---- Autorisations temporaires -----------------------------------------

    fun allowedUntil(pkg: String): Long = sp.getLong("allow_$pkg", 0L)

    fun allow(pkg: String, until: Long) {
        sp.edit().putLong("allow_$pkg", until).apply()
    }

    // ---- Journaux (horodatages des dernières 24 h) --------------------------

    private fun readLog(key: String): List<Long> =
        sp.getString(key, "").orEmpty()
            .split(',')
            .mapNotNull { it.toLongOrNull() }

    private fun appendLog(key: String, now: Long) {
        val kept = readLog(key).filter { now - it < DAY_MS } + now
        sp.edit().putString(key, kept.joinToString(",")).apply()
    }

    /** Tentative d'ouverture (la pause s'est affichée). */
    fun recordAttempt(pkg: String, now: Long) {
        appendLog("att_$pkg", now)
        bumpDay("d_att", now)
    }

    /** L'utilisateur a choisi d'ouvrir ou de prolonger. */
    fun recordOpen(pkg: String, now: Long) = appendLog("open_$pkg", now)

    /** L'utilisateur a renoncé. */
    fun recordGiveUp(now: Long) {
        appendLog("giveup", now)
        bumpDay("d_give", now)
    }

    // ---- Historique par jour (pour le graphique de la semaine) ----------------

    private fun dayKey(prefix: String, time: Long): String {
        val c = Calendar.getInstance().apply { timeInMillis = time }
        val y = c.get(Calendar.YEAR)
        val d = c.get(Calendar.DAY_OF_YEAR)
        return "${prefix}_${y}_$d"
    }

    private fun bumpDay(prefix: String, now: Long) {
        val key = dayKey(prefix, now)
        sp.edit().putInt(key, sp.getInt(key, 0) + 1).apply()
    }

    /** Dernière analyse Reels/Shorts, affichée dans l'écran de diagnostic. */
    fun saveShortDiagnostic(pkg: String, r: ShortFormBlocker.Result) {
        val time = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.ROOT).format(java.util.Date())
        val text = buildString {
            append(time).append("  ").append(pkg).append('\n')
            append("fenêtres ").append(r.windows).append(", méthode ").append(r.method)
            append(", ").append(r.durationMs).append(" ms")
            if (r.nodes > 0) append(", nœuds ").append(r.nodes)
            append('\n')
            append("résultat : ").append(r.reason).append('\n')
            append("identifiants : ").append(if (r.seen.isEmpty()) "aucun" else r.seen.joinToString(", "))
        }
        sp.edit().putString("diag_$pkg", text).apply()
    }

    fun learnedShortIds(pkg: String): Set<String> =
        sp.getStringSet("learned_$pkg", emptySet())?.toSet() ?: emptySet()

    fun addLearnedShortId(pkg: String, id: String) {
        sp.edit().putStringSet("learned_$pkg", HashSet(learnedShortIds(pkg) + id)).apply()
    }

    fun shortDiagnostic(): String = listOf(ShortFormBlocker.YOUTUBE, ShortFormBlocker.INSTAGRAM)
        .mapNotNull { sp.getString("diag_$it", null) }
        .joinToString("\n\n")

    fun recordShortBlocked(now: Long) = bumpDay("d_short", now)

    fun shortsBlockedToday(now: Long = System.currentTimeMillis()): Int =
        sp.getInt(dayKey("d_short", now), 0)

    /** Renoncements des 7 derniers jours, du plus ancien à aujourd'hui. */
    fun giveUpsLastWeek(now: Long = System.currentTimeMillis()): List<Int> =
        (6 downTo 0).map { back -> sp.getInt(dayKey("d_give", now - back * DAY_MS), 0) }

    fun attemptsSince(pkg: String, since: Long) = readLog("att_$pkg").count { it >= since }

    fun opensToday(pkg: String) = readLog("open_$pkg").count { it >= startOfDay() }

    fun giveUpsToday() = readLog("giveup").count { it >= startOfDay() }

    fun attemptsTodayAll(): Int {
        val start = startOfDay()
        return sp.all.keys
            .filter { it.startsWith("att_") }
            .sumOf { key -> readLog(key).count { it >= start } }
    }

    fun quotaReached(pkg: String): Boolean {
        val quota = dailyQuota
        return quota > 0 && opensToday(pkg) >= quota
    }

    /**
     * Délai croissant : base + pas x (tentatives précédentes dans l'heure).
     * À appeler après recordAttempt, la tentative en cours est donc déjà comptée.
     */
    fun delayFor(pkg: String, now: Long): Int {
        val previous = (attemptsSince(pkg, now - HOUR_MS) - 1).coerceAtLeast(0)
        return (baseDelay + stepDelay * previous).coerceAtMost(MAX_DELAY_S)
    }

    companion object {
        private const val K_TARGETS = "targets"
        private const val K_BASE = "base_delay"
        private const val K_STEP = "step_delay"
        private const val K_SESSION = "session_minutes"
        private const val K_QUOTA = "daily_quota"
        private const val K_REELS = "block_reels"
        private const val K_SHORTS = "block_shorts"

        const val HOUR_MS = 60L * 60 * 1000
        const val DAY_MS = 24 * HOUR_MS
        const val MAX_DELAY_S = 90

        fun startOfDay(now: Long = System.currentTimeMillis()): Long =
            Calendar.getInstance().apply {
                timeInMillis = now
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
    }
}
