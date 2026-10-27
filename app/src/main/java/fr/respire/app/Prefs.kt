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

    /** L'utilisateur a accepté l'écran d'information sur l'accessibilité (exigé par Google Play). */
    var accessibilityConsent: Boolean
        get() = sp.getBoolean("a11y_consent", false)
        set(v) = sp.edit().putBoolean("a11y_consent", v).apply()

    // ---- Heures à risque --------------------------------------------------------

    /** Plage où les règles se durcissent (attente doublée, ouvertures plafonnées). */
    var riskEnabled: Boolean
        get() = sp.getBoolean(K_RISK_ON, false)
        set(v) = sp.edit().putBoolean(K_RISK_ON, v).apply()

    /** Heure de début (0-23). */
    var riskStart: Int
        get() = sp.getInt(K_RISK_START, 22)
        set(v) = sp.edit().putInt(K_RISK_START, v).apply()

    /** Heure de fin (0-23), exclue. Peut être avant le début : la plage passe minuit. */
    var riskEnd: Int
        get() = sp.getInt(K_RISK_END, 7)
        set(v) = sp.edit().putInt(K_RISK_END, v).apply()

    /** Ouvertures autorisées par appli pendant une plage. 0 = applis bloquées. */
    var riskQuota: Int
        get() = sp.getInt(K_RISK_QUOTA, 0)
        set(v) = sp.edit().putInt(K_RISK_QUOTA, v).apply()

    /** Début de la plage en cours, ou null si on n'est pas dans une plage. */
    fun riskWindowStart(now: Long = System.currentTimeMillis()): Long? =
        if (riskEnabled) Rules.riskWindowStart(now, riskStart, riskEnd) else null

    fun inRiskWindow(now: Long = System.currentTimeMillis()) = riskWindowStart(now) != null

    fun opensSince(pkg: String, since: Long) = readLog("open_$pkg").count { it >= since }

    enum class Block { NONE, DAILY, RISK }

    /** Pourquoi l'appli est bloquée maintenant, s'il y a lieu. */
    fun blockReason(pkg: String, now: Long = System.currentTimeMillis()): Block {
        val start = riskWindowStart(now)
        return Rules.blockReason(
            dailyQuota = dailyQuota,
            opensToday = opensToday(pkg),
            riskStart = start,
            opensSinceRisk = if (start != null) opensSince(pkg, start) else 0,
            riskQuota = riskQuota,
        )
    }

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
        bumpHour("h_att", now)
        bumpDay("pd_att_$pkg", now)
        bumpHour("ph_att_$pkg", now)
    }

    /** L'utilisateur a choisi d'ouvrir ou de prolonger. */
    fun recordOpen(pkg: String, now: Long) {
        appendLog("open_$pkg", now)
        bumpDay("pd_open_$pkg", now)
    }

    /** L'utilisateur a renoncé. */
    fun recordGiveUp(now: Long, pkg: String? = null) {
        appendLog("giveup", now)
        bumpDay("d_give", now)
        bumpHour("h_give", now)
        if (pkg != null) bumpDay("pd_give_$pkg", now)
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

    fun clearLearnedShortIds() {
        val keys = sp.all.keys.filter { it.startsWith("learned_") }
        if (keys.isNotEmpty()) sp.edit().apply { keys.forEach { remove(it) } }.apply()
    }

    fun shortDiagnostic(): String = listOf(ShortFormBlocker.YOUTUBE, ShortFormBlocker.INSTAGRAM)
        .mapNotNull { sp.getString("diag_$it", null) }
        .joinToString("\n\n")

    fun recordShortBlocked(now: Long, pkg: String? = null) {
        bumpDay("d_short", now)
        bumpHour("h_short", now)
        if (pkg != null) {
            bumpDay("pd_short_$pkg", now)
            bumpHour("ph_short_$pkg", now)
        }
    }

    /** Semaine d'une appli : 7 valeurs par jour (du plus ancien à aujourd'hui) et 24 par heure. */
    data class AppWeek(
        val attempts: List<Int>,
        val opens: List<Int>,
        val giveUps: List<Int>,
        val shorts: List<Int>,
        /** Tentations cumulées sur la semaine, heure par heure. */
        val hours: List<Int>,
    ) {
        val isEmpty get() = attempts.sum() + opens.sum() + giveUps.sum() + shorts.sum() == 0
    }

    fun appWeek(pkg: String, now: Long = System.currentTimeMillis()): AppWeek {
        val days = Rules.pastDays(now)
        fun daily(prefix: String) = days.map { sp.getInt(dayKey(prefix, it), 0) }
        val hours = IntArray(24)
        for (d in days) {
            val a = readHours(dayKey("ph_att_$pkg", d))
            val sh = readHours(dayKey("ph_short_$pkg", d))
            for (h in 0 until 24) hours[h] += a[h] + sh[h]
        }
        return AppWeek(
            attempts = daily("pd_att_$pkg"),
            opens = daily("pd_open_$pkg"),
            giveUps = daily("pd_give_$pkg"),
            shorts = daily("pd_short_$pkg"),
            hours = hours.toList(),
        )
    }

    /** Supprime les compteurs journaliers de plus de 60 jours. */
    fun pruneOldDays(now: Long = System.currentTimeMillis()) {
        val c = Calendar.getInstance().apply { timeInMillis = now }
        val today = c.get(Calendar.YEAR) * 366 + c.get(Calendar.DAY_OF_YEAR)
        val old = sp.all.keys.filter { key ->
            val parts = key.split('_')
            val y = parts.getOrNull(parts.size - 2)?.toIntOrNull()
            val d = parts.lastOrNull()?.toIntOrNull()
            y != null && d != null && y in 2000..3000 && today - (y * 366 + d) > 60
        }
        if (old.isNotEmpty()) sp.edit().apply { old.forEach { remove(it) } }.apply()
    }

    // ---- Historique par heure (24 compteurs par jour) -------------------------------

    private fun readHours(key: String): IntArray {
        val out = IntArray(24)
        sp.getString(key, null)?.split(',')?.forEachIndexed { i, v ->
            if (i < 24) out[i] = v.toIntOrNull() ?: 0
        }
        return out
    }

    private fun bumpHour(prefix: String, now: Long) {
        val key = dayKey(prefix, now)
        val hours = readHours(key)
        val h = Calendar.getInstance().apply { timeInMillis = now }.get(Calendar.HOUR_OF_DAY)
        hours[h]++
        sp.edit().putString(key, hours.joinToString(",")).apply()
    }

    /** Tentatives (pauses affichées), renoncements et vidéos courtes écartées, heure par heure. */
    data class DayHours(val attempts: List<Int>, val giveUps: List<Int>, val shorts: List<Int>)

    fun hoursOf(dayTime: Long): DayHours = DayHours(
        attempts = readHours(dayKey("h_att", dayTime)).toList(),
        giveUps = readHours(dayKey("h_give", dayTime)).toList(),
        shorts = readHours(dayKey("h_short", dayTime)).toList(),
    )

    fun shortsBlockedToday(now: Long = System.currentTimeMillis()): Int =
        sp.getInt(dayKey("d_short", now), 0)

    /** Renoncements des 7 derniers jours, du plus ancien à aujourd'hui. */
    fun giveUpsLastWeek(now: Long = System.currentTimeMillis()): List<Int> =
        Rules.pastDays(now).map { day -> sp.getInt(dayKey("d_give", day), 0) }

    fun attemptsSince(pkg: String, since: Long) = readLog("att_$pkg").count { it >= since }

    fun opensToday(pkg: String) = readLog("open_$pkg").count { it >= startOfDay() }

    fun giveUpsToday() = readLog("giveup").count { it >= startOfDay() }

    fun attemptsTodayAll(): Int {
        val start = startOfDay()
        return sp.all.keys
            .filter { it.startsWith("att_") }
            .sumOf { key -> readLog(key).count { it >= start } }
    }

    fun quotaReached(pkg: String): Boolean = blockReason(pkg) != Block.NONE

    /**
     * Délai croissant : base + pas x (tentatives précédentes dans l'heure).
     * À appeler après recordAttempt, la tentative en cours est donc déjà comptée.
     */
    fun delayFor(pkg: String, now: Long): Int =
        Rules.delay(baseDelay, stepDelay, attemptsSince(pkg, now - HOUR_MS), inRiskWindow(now))

    companion object {
        private const val K_TARGETS = "targets"
        private const val K_BASE = "base_delay"
        private const val K_STEP = "step_delay"
        private const val K_SESSION = "session_minutes"
        private const val K_QUOTA = "daily_quota"
        private const val K_REELS = "block_reels"
        private const val K_SHORTS = "block_shorts"
        private const val K_RISK_ON = "risk_on"
        private const val K_RISK_START = "risk_start"
        private const val K_RISK_END = "risk_end"
        private const val K_RISK_QUOTA = "risk_quota"

        fun riskHours(start: Int, end: Int): Set<Int> = Rules.riskHours(start, end)

        const val HOUR_MS = Rules.HOUR_MS
        const val DAY_MS = Rules.DAY_MS
        const val MAX_DELAY_S = Rules.MAX_DELAY_S

        fun startOfDay(now: Long = System.currentTimeMillis()): Long = Rules.startOfDay(now)
    }
}
