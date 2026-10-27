package fr.respire.app

import java.util.Calendar

/**
 * Logique des règles de Respire, sans aucune dépendance à Android :
 * tout ce qui décide d'une attente ou d'un blocage passe par ici, et est couvert
 * par les tests de `app/src/test`.
 *
 * Les heures et les jours suivent le fuseau horaire du téléphone (Calendar),
 * y compris les jours de 23 h ou 25 h des changements d'heure.
 */
object Rules {

    const val HOUR_MS = 60L * 60 * 1000
    const val DAY_MS = 24 * HOUR_MS

    /** Attente maximale, en secondes, quelles que soient les règles. */
    const val MAX_DELAY_S = 90

    /**
     * Heures couvertes par une plage [start, end[ (0-23), en passant minuit si besoin.
     * start == end : toute la journée.
     */
    fun riskHours(start: Int, end: Int): Set<Int> {
        if (start == end) return (0 until 24).toSet()
        val out = mutableSetOf<Int>()
        var h = start
        while (h != end) {
            out.add(h)
            h = (h + 1) % 24
        }
        return out
    }

    /**
     * Début (horodatage) de la plage horaire en cours, ou null si `now` est hors plage.
     * Une plage 22 h → 7 h consultée à 2 h renvoie 22 h la veille.
     */
    fun riskWindowStart(now: Long, start: Int, end: Int): Long? {
        val hours = riskHours(start, end)
        var c = Calendar.getInstance().apply { timeInMillis = now }
        if (c.get(Calendar.HOUR_OF_DAY) !in hours) return null
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        // On remonte heure par heure tant qu'on reste dans la plage. Si l'heure de début
        // n'existe pas cette nuit-là (passage à l'heure d'été), on s'arrête à la première
        // heure réelle de la plage. 24 pas au plus.
        var steps = 0
        while (c.get(Calendar.HOUR_OF_DAY) != start && steps < 24) {
            val prev = (c.clone() as Calendar).apply { add(Calendar.HOUR_OF_DAY, -1) }
            if (prev.get(Calendar.HOUR_OF_DAY) !in hours) break
            c = prev
            steps++
        }
        return c.timeInMillis
    }

    /**
     * Pourquoi une appli est bloquée maintenant.
     * @param dailyQuota ouvertures par jour, 0 = illimité
     * @param riskStart début de la plage à risque en cours, ou null hors plage
     * @param opensSinceRisk ouvertures depuis le début de cette plage
     * @param riskQuota ouvertures permises pendant la plage, 0 = appli bloquée
     */
    fun blockReason(
        dailyQuota: Int,
        opensToday: Int,
        riskStart: Long?,
        opensSinceRisk: Int,
        riskQuota: Int,
    ): Prefs.Block {
        if (dailyQuota > 0 && opensToday >= dailyQuota) return Prefs.Block.DAILY
        if (riskStart != null && opensSinceRisk >= riskQuota) return Prefs.Block.RISK
        return Prefs.Block.NONE
    }

    /**
     * Attente avant de pouvoir ouvrir, en secondes : base + pas × tentatives précédentes
     * dans l'heure, doublée pendant les heures à risque, plafonnée à [MAX_DELAY_S].
     * @param attemptsInHour tentatives de la dernière heure, celle en cours comprise
     */
    fun delay(base: Int, step: Int, attemptsInHour: Int, inRisk: Boolean): Int {
        val previous = (attemptsInHour - 1).coerceAtLeast(0)
        val raw = base + step * previous
        return (raw * (if (inRisk) 2 else 1)).coerceAtMost(MAX_DELAY_S)
    }

    /**
     * Le nouveau réglage des heures à risque est-il plus permissif que l'ancien ?
     * Oui si la plage était active et perd au moins une heure, ou si son plafond monte.
     * Dans ce cas, l'appli impose l'attente de 30 s avant d'enregistrer.
     */
    fun isRiskLooser(
        enabled: Boolean,
        oldStart: Int, oldEnd: Int, oldQuota: Int,
        newStart: Int, newEnd: Int, newQuota: Int,
    ): Boolean {
        if (!enabled) return false
        val lostHours = !riskHours(newStart, newEnd).containsAll(riskHours(oldStart, oldEnd))
        return lostHours || newQuota > oldQuota
    }

    /** Minuit du jour de `now`, dans le fuseau du téléphone. */
    fun startOfDay(now: Long): Long =
        Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    /**
     * Midi de chacun des `count` derniers jours, du plus ancien à aujourd'hui.
     * Compté en jours de calendrier et non en tranches de 24 h : un jour de 23 h
     * ou de 25 h (changement d'heure) n'est ni sauté ni compté deux fois.
     */
    fun pastDays(now: Long, count: Int = 7): List<Long> {
        val noon = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return (count - 1 downTo 0).map { back ->
            (noon.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -back) }.timeInMillis
        }
    }
}
