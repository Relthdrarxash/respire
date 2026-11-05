# Parcours utilisateurs formalisés : Respire

Novembre 2026. Ce document formalise le parcours utilisateur dans **Respire** (pause de respiration avant les applications ouvertes par réflexe, inspirée des travaux de Grüning et al., *PNAS* 2023, et Haliburton et al., *CHI 2024*).

---

## 1. Parcours complet : de l'installation à l'autorégulation

| Étape | Besoin de l'utilisateur | Réponse dans Respire | Fichier d'implémentation |
|---|---|---|---|
| **1. Choix des applications (30 s)** | Protéger immédiatement les applications qui captent son attention, sans limite commerciale | Suggestions automatiques en haut de liste (`TikTok`, `Instagram`, `YouTube`, `X`, `Reddit`, etc. via `Suggestions.kt`) + sélection libre d'un nombre illimité d'applications | `PickerScreen.kt`, `Suggestions.kt` |
| **2. Activation du service d'accessibilité** | Comprendre pourquoi l'autorisation est demandée et être certain que rien n'est espionné | Explication claire en français courant + garantie architecturale (`0 permission INTERNET` dans `AndroidManifest.xml`, code d'accessibilité isolé dans `PauseService.kt` et `ShortFormBlocker.kt`) + aide pas-à-pas pour débloquer les « Paramètres restreints » d'Android 13+ | `HomeScreen.kt`, `Dialogs.kt`, `PauseService.kt` |
| **3. Ouverture réflexe d'une application** | Casser l'automatisme en quelques secondes tout en gardant la liberté de fermer immédiatement | Écran de respiration (`10 s` par défaut) où le bouton **« Fermer »** est actif dès la première seconde (le levier le plus efficace selon Grüning et al., 2023) et le bouton **« Ouvrir »** apparaît à la fin du souffle | `PauseActivity.kt`, `PauseScreen.kt` |
| **4. Tentatives rapprochées dans la même heure** | Freiner la boucle compulsive quand on rouvre la même application plusieurs fois par heure | Allongement progressif de la pause (`+5 s` par tentative dans l'heure, plafonné à `90 s`, doublé pendant les heures à risque) | `Rules.kt` |
| **5. Fin de session & quota quotidien** | Recréer un point d'arrêt dans les fils infinis | Rappel de la pause après `10 min` d'utilisation continue (`session end`) et fermeture jusqu'au lendemain au-delà du quota quotidien choisi (`6 ouvertures/jour` par défaut, Allcott et al., 2022) | `PauseService.kt`, `Rules.kt` |
| **6. Filtre Reels & Shorts (optionnel)** | Continuer à utiliser la messagerie d'Instagram ou les vidéos longues de YouTube sans tomber dans les vidéos courtes | Détection locale des identifiants de vues des lecteurs Reels et Shorts et retour en arrière automatique sans bloquer le reste de l'application | `ShortFormBlocker.kt` |
| **7. Tentation d'assouplir les règles sur un coup de tête** | Éviter de désactiver une règle par impulsion tout en restant libre de modifier ses réglages | **Verrou contre l'assouplissement (`30 s`)** déclenché uniquement lorsqu'un changement rend les règles plus permissives (`Rules.isRiskLooser`, Haliburton et al., 2024) | `Rules.kt`, `Dialogs.kt` |
| **8. Bilan hebdomadaire (« Ta semaine »)** | Comprendre à quelles heures la tentation est la plus forte, sans série culpabilisante ni score | Graphique des 7 derniers jours (renoncements vs ouvertures par jour et par heure) + suggestion automatique d'une plage d'heures à risque personnalisée + widget d'accueil sobre | `HoursChart.kt`, `RiskCard.kt`, `RespireWidget.kt` |
