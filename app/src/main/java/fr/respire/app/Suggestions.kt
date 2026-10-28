package fr.respire.app

/**
 * Applis souvent ouvertes par réflexe (flux sans fin, vidéos courtes), proposées
 * en haut de l'écran de choix. Logique pure, sans dépendance à Android, testée à part.
 */
object Suggestions {

    /** Noms de paquets, dans l'ordre d'affichage. */
    val PACKAGES = listOf(
        "com.zhiliaoapp.musically", // TikTok
        "com.ss.android.ugc.trill", // TikTok (certains pays)
        "com.zhiliaoapp.musically.go", // TikTok Lite
        "com.instagram.android",
        "com.facebook.katana",
        "com.facebook.lite",
        "com.google.android.youtube",
        "com.snapchat.android",
        "com.twitter.android", // X
        "com.instagram.barcelona", // Threads
        "xyz.blueskyweb.app", // Bluesky
        "com.reddit.frontpage",
        "com.pinterest",
        "tv.twitch.android.app",
        "com.tumblr",
        "com.linkedin.android",
    )

    /** Applis installées à proposer : connues, pas encore choisies, dans l'ordre de la liste. */
    fun pick(installed: Collection<String>, selected: Set<String>): List<String> {
        val have = installed.toSet()
        return PACKAGES.filter { it in have && it !in selected }
    }
}
