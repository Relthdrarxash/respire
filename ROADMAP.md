# Feuille de route — Respire

> Dernière mise à jour : novembre 2026.

## 1. Fonctionnalités à venir
- **v4.x** :
  - Affinage de la détection des lecteurs plein écran (Reels / Shorts) suite aux mises à jour des applications tierces, sans jamais demander la permission `INTERNET`.
  - Intégration optionnelle de `pilmil-core` pour l'export/import chiffré des statistiques et réglages.

## 2. Aspect visuel & UX
- Suivi des captures d'écran Paparazzi (`ScreensTest`) sur l'ensemble des états (accueil, pause 4s/6s, bilan, réglages).
- Amélioration des contrastes et du guidage d'accessibilité Android (y compris l'écran « paramètres restreints » d'Android 13+).

## 3. Qualité de code & sécurité
- Sauvegarde Android configurable par application (`-Ppilmil.allowAutoBackup=false` par défaut dans `app/build.gradle.kts`, aucune permission `INTERNET`).
- Exécution systématique de `testReleaseUnitTest` et `lintDebug` en CI (zéro erreur de formatage i18n/lint).
- À faire : ajouter `ktlint` / `detekt` et des tests unitaires supplémentaires sur les règles de blocage (`ShortFormBlocker`, `Prefs`).

## 4. Feuille de route iOS
- **État actuel** : 100 % Android (l'appli repose sur `AccessibilityService`, inexistant sur iOS).
- **Étapes** :
  1. Étudier une version iOS dédiée basée sur la **Screen Time API** (`FamilyControls`, `ManagedSettings`, `DeviceActivity` en Swift).
  2. Demander à Apple l'entitlement **Family Controls** (prérequis bloquant avant toute distribution).
  3. Porter la cohérence cardiaque et les statistiques via Compose Multiplatform si l'entitlement est accordé.
