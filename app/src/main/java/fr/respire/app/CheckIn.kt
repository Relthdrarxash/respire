package fr.respire.app

/**
 * Quand prévenir la personne, sans dépendance à Android (testé à part).
 * Un seul message possible, utilitaire : le service s'est arrêté. Pas de rappel ni de bilan,
 * Respire n'envoie rien pour faire revenir.
 */
object CheckIn {

    const val DAY_MS = 24 * 60 * 60 * 1000L

    /**
     * Le service a été vu actif, des applis sont choisies, mais il ne tourne plus :
     * Android ou l'économie de batterie l'a coupé. Au plus un message par jour.
     */
    fun shouldWarnStopped(hasTargets: Boolean, seenOn: Boolean, running: Boolean, now: Long, lastWarning: Long): Boolean =
        hasTargets && seenOn && !running && now - lastWarning >= DAY_MS

}
