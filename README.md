# Respire

Appli Android contre le doom scroll, interface « Calme » (sauge et sable, polices Fraunces et Nunito sous licence SIL OFL, voir `licences/`). Aucune permission Internet, aucun traqueur, aucune donnée qui sort du téléphone.

## Ce que fait l'appli

Les mécanismes retenus sont ceux qui ont le plus de soutien dans la recherche :

1. **Pause avant ouverture** (principe de one sec, PNAS 2023) : quand tu ouvres une appli de la liste, un écran de respiration s'affiche avec un compte à rebours et la question « Pourquoi tu ouvres cette appli maintenant ? ». Le bouton « Non, je ferme » est disponible tout de suite, « Ouvrir quand même » seulement à la fin de l'attente.
2. **Délai croissant** : chaque nouvelle tentative dans l'heure ajoute quelques secondes d'attente.
3. **Fin de session** : une ouverture ne vaut que pour une durée limitée (10 min par défaut). Si tu es toujours dans l'appli à la fin, la pause revient en plein défilement. C'est ce qui vise directement le doom scroll.
4. **Quota d'ouvertures par jour** (limite difficile à contourner, cf. Allcott, Gentzkow et Song) : au-delà du quota, l'appli est bloquée jusqu'au lendemain. Chaque prolongation de session compte comme une ouverture.
5. **Verrou d'assouplissement** : retirer une appli de la liste ou rendre les réglages plus permissifs impose 30 s d'attente. Les études de terrain montrent que les rechutes arrivent surtout quand on « met en pause » l'outil.

Réglages par défaut : 10 s d'attente, +5 s par tentative dans l'heure, sessions de 10 min, 6 ouvertures par appli et par jour (soit 1 h maximum).

## Compiler l'APK

### Option A : GitHub Actions (rien à installer)

1. Crée un dépôt GitHub (privé si tu veux) et pousse ce dossier.
2. L'action « Build APK » se lance automatiquement (onglet Actions).
3. À la fin, télécharge l'artefact `respire-apk` : il contient `app-debug.apk`.

### Option B : Android Studio

Ouvre le dossier dans Android Studio, laisse Gradle se synchroniser, puis Run sur ton téléphone branché en USB (débogage USB activé). En ligne de commande : `./gradlew assembleDebug`.

## Installer et activer

1. Copie l'APK sur le téléphone et installe-le (autorise l'installation depuis ton gestionnaire de fichiers si Android le demande).
2. Ouvre Respire, appuie sur « Choisir les applis » et active celles à freiner.
3. Appuie sur « Ouvrir les paramètres » (ou sur la pastille Inactif) et active « Respire ».
4. **Android 13 et plus** : si l'activation est grisée (« paramètre restreint »), va dans Paramètres > Applications > Respire > menu ⋮ en haut à droite > « Autoriser les paramètres restreints », puis recommence l'étape 3.
5. Sur certaines surcouches (Xiaomi, Huawei, Oppo...), désactive l'optimisation de batterie pour Respire, sinon le système peut couper le service.

## Vie privée

Le service d'accessibilité ne reçoit que l'événement « une fenêtre a changé » et le nom du paquet concerné. Il ne lit pas le contenu de l'écran (`canRetrieveWindowContent="false"`). Les réglages et compteurs restent dans les préférences locales de l'appli. Le manifeste ne déclare pas la permission INTERNET.

## Limites connues

- On peut toujours désactiver le service dans les paramètres d'accessibilité. Le verrou ne protège que les réglages internes.
- Les applis ouvertes via un lien depuis une autre appli déclenchent aussi la pause, c'est voulu.
- Le délai et le quota sont communs à toutes les applis de la liste (le quota est compté par appli).
