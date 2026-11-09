package fr.respire.app

/** Étapes de l'accueil du premier lancement, dans l'ordre. Chacune porte une seule idée. */
enum class OnboardingStep {
    /** La promesse de l'appli. */
    WELCOME,

    /** Le choix des applis à freiner. */
    APPS,

    /** L'information sur l'accessibilité, avec consentement explicite (exigence Google Play). */
    DISCLOSURE,

    /** L'activation du service dans les réglages d'Android, avec retour d'état. */
    ACTIVATION;

    fun next(): OnboardingStep? = entries.getOrNull(ordinal + 1)

    fun previous(): OnboardingStep? = entries.getOrNull(ordinal - 1)
}

/** Où en est l'activation du service d'accessibilité, vue depuis l'accueil. */
enum class ActivationStatus {
    /** La personne n'est pas encore allée dans les réglages. */
    NOT_TRIED,

    /** Le service est actif. */
    ENABLED,

    /** La personne est revenue des réglages et le service n'est toujours pas actif. */
    STILL_OFF,
}

/** Logique pure de l'accueil, sans dépendance à Android, testée à part. */
object Onboarding {

    /**
     * L'accueil ne s'affiche qu'au vrai premier lancement. Une personne qui a déjà choisi des applis,
     * accepté l'information ou vu le service actif (mise à jour depuis une version sans accueil)
     * ne le revoit pas : elle peut le rouvrir depuis les réglages.
     */
    fun shouldShow(done: Boolean, hasTargets: Boolean, consent: Boolean, serviceSeenOn: Boolean): Boolean =
        !done && !hasTargets && !consent && !serviceSeenOn

    fun activationStatus(serviceOn: Boolean, returnedFromSettings: Boolean): ActivationStatus = when {
        serviceOn -> ActivationStatus.ENABLED
        returnedFromSettings -> ActivationStatus.STILL_OFF
        else -> ActivationStatus.NOT_TRIED
    }

    /**
     * Étape qui suit l'information sur l'accessibilité. Un refus ne bloque rien : l'accueil se termine
     * (null) et l'accueil de l'appli propose d'activer le service plus tard.
     */
    fun afterDisclosure(accepted: Boolean): OnboardingStep? =
        if (accepted) OnboardingStep.ACTIVATION else null

    /** L'aide « paramètres restreints » ne concerne qu'Android 13 et plus. */
    fun needsRestrictedHelp(sdkInt: Int): Boolean = sdkInt >= 33
}
