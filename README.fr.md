# Respire

[English](README.md) · **Français**

Une petite appli Android contre le doom scroll. Respire place une pause de respiration devant les applis que tu ouvres par réflexe, limite la durée d'une session, plafonne le nombre d'ouvertures par jour et peut masquer les Reels d'Instagram et les Shorts de YouTube tout en laissant le reste de ces applis utilisable.

Pas de permission Internet, pas de traqueur, pas de compte. Tout reste sur ton téléphone.

Disponible en français, anglais, espagnol et allemand.

## Pourquoi ces fonctions

Respire réunit les techniques qui ont le plus de soutien dans la recherche sur la réduction de l'usage excessif du smartphone :

| Fonction | Ce qu'elle fait | Ce que dit la recherche |
|---|---|---|
| **Pause avant ouverture** | Un écran de respiration avec un compte à rebours et la question « Pourquoi tu l'ouvres maintenant ? ». Fermer est possible tout de suite, ouvrir seulement à la fin du décompte. | Ajouter une friction avant d'ouvrir les réseaux sociaux réduit les tentatives d'ouverture et rend les ouvertures plus intentionnelles (Grüning, Riedel et Lorenz-Spreen, *PNAS* 2023 ; Haliburton et al., *CHI* 2024). |
| **Délai croissant** | Chaque nouvelle tentative dans l'heure ajoute quelques secondes. | Même principe de friction, renforcé quand l'envie revient. |
| **Fin de session** | Une ouverture ne vaut que pour une durée fixée (10 min par défaut). Si tu es toujours dans l'appli, la pause revient en plein défilement. | Vise le défilement lui-même, pas seulement l'ouverture. |
| **Quota quotidien** | Au-delà d'un nombre d'ouvertures par appli et par jour, l'appli est bloquée jusqu'au lendemain. Prolonger une session compte comme une ouverture. | Des limites difficiles à contourner ont réduit l'usage des réseaux sociaux d'environ 16 % sur 12 semaines, et la plupart des participants les ont gardées (Allcott, Gentzkow et Song, NBER 2021). |
| **Heures à risque** | Sur une plage que tu choisis (22 h à 7 h par défaut, avec une suggestion tirée de ton propre suivi par heure), l'attente est doublée et les ouvertures plafonnées, à zéro par défaut. | Changer le contexte aux moments où tu craques le plus marche mieux que compter sur la volonté. |
| **Verrou d'assouplissement** | Retirer une appli ou relâcher une règle impose 30 secondes d'attente. | Les utilisateurs ont tendance à mettre ces outils en pause et à rechuter vite (Haliburton et al., 2024). |
| **Filtre Reels et Shorts** | Quitte le lecteur plein écran de vidéos courtes dès qu'il apparaît dans Instagram ou YouTube. Un Reel reçu en message s'ouvre quand même. | Supprime le flux sans fin tout en gardant les messages, la recherche et les vidéos classiques. |

Réglages par défaut : 10 s d'attente, +5 s par tentative dans l'heure, sessions de 10 min, 6 ouvertures par appli et par jour. Les heures à risque restent désactivées tant que tu ne les actives pas.

Touche un jour du graphique de la semaine pour le voir heure par heure et repérer ton moment le plus tentant. Touche une appli de la liste pour voir sa semaine : tentations, ouvertures, part de renoncements et heure la plus tentante.

## Installer

1. Depuis ton téléphone, télécharge `Respire.apk` dans la [dernière version](../../releases/latest) et installe-le. Android te demandera d'autoriser l'installation depuis ton navigateur ou ton gestionnaire de fichiers.
2. Ouvre Respire, appuie sur **Choisir les applis** et active celles à freiner.
3. Appuie sur **Ouvrir les paramètres** (ou sur la pastille **Inactif**) et active **Respire** dans les paramètres d'accessibilité.
4. **Android 13 et plus** : si l'interrupteur est grisé (« paramètre restreint »), va dans Paramètres > Applications > Respire > menu ⋮ > **Autoriser les paramètres restreints**, puis recommence l'étape 3.
5. Sur certaines surcouches (Xiaomi, Huawei, Oppo...), désactive l'optimisation de batterie pour Respire, sinon le système peut couper le service.

Sur Android 13 et plus, tu peux choisir la langue de l'appli dans Paramètres > Applications > Respire > Langue.

## Vie privée

Respire utilise un service d'accessibilité, seul moyen sous Android de savoir quelle appli s'ouvre.

- **Les pauses** n'utilisent que le nom du paquet de l'appli au premier plan.
- **Le filtre Reels et Shorts** (désactivé par défaut) : uniquement dans Instagram et YouTube, le service consulte les identifiants techniques des éléments affichés pour repérer le lecteur de vidéos courtes. Il ne lit jamais les textes, les messages ou les images.
- Les réglages et compteurs restent dans le stockage local de l'appli. Le manifeste ne déclare pas la permission `INTERNET` : rien ne peut être envoyé nulle part.

## Compiler depuis les sources

Le dépôt se compile tout seul avec GitHub Actions à chaque push et publie l'APK dans la release `latest`.

Pour compiler en local, ouvre le dossier dans Android Studio, ou lance :

```bash
./gradlew assembleDebug
```

Prérequis : JDK 17. L'appli est écrite en Kotlin avec Jetpack Compose et fonctionne à partir d'Android 8.0 (API 26).

Pour lancer les tests unitaires : `./gradlew testReleaseUnitTest`.

## Limites connues

- On peut toujours désactiver le service dans les paramètres d'accessibilité. Le verrou ne protège que les réglages de Respire.
- Le filtre Reels et Shorts repose sur des identifiants de vues qu'Instagram ou YouTube peuvent modifier lors d'une mise à jour.
- Les applis ouvertes via un lien depuis une autre appli déclenchent aussi la pause. C'est voulu.

## Comment Respire est fait

Respire est un projet personnel de Relthdrarxash, publié sous le nom Escale. Contact : escale.apps@proton.me. L'essentiel du code a été écrit avec un assistant IA (Claude, d'Anthropic), et je préfère le dire clairement.

Concrètement :

- **Les décisions sont humaines.** Le problème à régler, les fonctions retenues à partir des études citées plus bas, ce qui a été écarté (séries, badges, notifications), le design, et chaque correction après des tests sur un vrai téléphone.
- **Pas besoin de me croire sur parole.** Tout le code est ici, sous licence GPL-3.0. Le manifeste ne déclare pas la permission `INTERNET` : même un bug ne peut rien envoyer nulle part. Le code lié à l'accessibilité se trouve dans [`PauseService.kt`](app/src/main/java/fr/respire/app/PauseService.kt) et [`ShortFormBlocker.kt`](app/src/main/java/fr/respire/app/ShortFormBlocker.kt).
- **Les règles sont testées.** Tout ce qui décide d'une attente ou d'un blocage se trouve dans [`Rules.kt`](app/src/main/java/fr/respire/app/Rules.kt), sans dépendance à Android, et est couvert par des tests lancés à chaque compilation (y compris les changements d'heure et les plages qui passent minuit).
- **Quelqu'un en répond.** Si tu trouves un bug, un souci de vie privée ou quelque chose de flou, [ouvre une issue](https://github.com/Relthdrarxash/respire/issues) ou écris à escale.apps@proton.me. Les relectures du code d'accessibilité sont particulièrement bienvenues.

## Licence

Copyright (C) 2026 Relthdrarxash

Respire est un logiciel libre : tu peux le redistribuer et le modifier selon les termes de la [licence publique générale GNU v3.0](LICENSE). Les versions modifiées que tu distribues doivent rester sous la même licence, avec leur code source disponible.

Les polices de `app/src/main/res/font/` restent sous leur propre licence SIL Open Font License (voir `licences/`).

## Crédits

- Identifiants de vues pour repérer les Reels et les Shorts : [short-video-rules](https://github.com/bhargavprajapati949/short-video-rules) (MIT) et [appblock](https://github.com/rubberpencils/appblock).
- Polices : [Fraunces](https://github.com/undercasetype/Fraunces) et [Nunito](https://github.com/googlefonts/nunito), licence SIL Open Font License, voir `licences/`.

## Références

- Grüning, D. J., Riedel, F., et Lorenz-Spreen, P. (2023). Directing smartphone use through the self-nudge app one sec. *PNAS*. [doi:10.1073/pnas.2213114120](https://doi.org/10.1073/pnas.2213114120)
- Haliburton, L., Grüning, D. J., Riedel, F., Schmidt, A., et Terzimehić, N. (2024). A longitudinal in-the-wild investigation of design frictions to prevent smartphone overuse. *CHI 2024*.
- Allcott, H., Gentzkow, M., et Song, L. (2021). Digital Addiction. *NBER Working Paper* 28936. [nber.org/papers/w28936](https://www.nber.org/papers/w28936)
