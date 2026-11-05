# Internationalisation

Ce guide est le même dans les trois dépôts (Lunaison, Sommelle, Respire). Seule la partie « Où sont les textes » change d'une appli à l'autre.

## Langues visées

| Code | Langue | Pourquoi |
|---|---|---|
| `fr` | Français | Langue de rédaction, langue de référence |
| `en` | Anglais | Langue de repli pour tout téléphone dans une langue non prise en charge |
| `pl` | Polonais | Forte demande d'outils de santé sans traceur ; contexte juridique de la santé reproductive |
| `uk` | Ukrainien | Personnes déplacées dans toute l'Europe, accès aux soins interrompu |
| `es` | Espagnol | Grand bassin de personnes concernées, en Europe et en Amérique latine |
| `de` | Allemand | Premier marché européen des applis de santé, forte sensibilité à la vie privée |

## Une langue n'est proposée que si elle est complète

Une seule source de vérité : `app/src/main/res/xml/locales_config.xml`, référencé par `android:localeConfig` dans `AndroidManifest.xml`. C'est ce fichier qui fait apparaître la langue dans **Paramètres > Applications > [appli] > Langue** (Android 13+).

On n'ajoute une langue à ce fichier **que** lorsque tous les textes de l'appli existent dans cette langue et ont été relus par une personne qui la parle. Une appli à moitié traduite, surtout sur des consignes de santé ou de sécurité, est pire qu'une appli en anglais.

## Ajouter une langue, pas à pas

1. **Textes d'interface** : copier chaque `values/strings.xml` dans `values-xx/strings.xml` et traduire. Garder les mêmes clés, les mêmes `%1$s`, les mêmes `plurals` (attention, le polonais et l'ukrainien ont des formes `few` et `many`).
2. **Textes composés dans le code** (voir ci-dessous) : ajouter la langue à l'énumération de langue et à chaque traduction.
3. **Sources médicales** : pour les fiches santé, ajouter les recommandations de la société savante ou du ministère du pays, à côté des références internationales (OMS, FIGO, ESHRE, ESRS…), dans `documentation/sources.md` et dans le code.
4. **Numéros d'urgence** : vérifier chaque numéro cité (urgences, prévention du suicide) pour le pays visé.
5. **Relecture** par une personne qui parle la langue, idéalement soignante pour les fiches santé.
6. Ajouter `<locale android:name="xx" />` à `locales_config.xml`.
7. Lancer `./gradlew test` : les tests vérifient qu'aucun texte n'est vide dans aucune langue.

## Où sont les textes

### Lunaison

- Interface Compose et contenus : composés dans le code avec `tr(fr, en, pl, uk, es, de)` ([`Language.kt`](https://github.com/Relthdrarxash/lunaison/blob/main/core/model/src/main/kotlin/fr/lunaison/core/model/Language.kt)). Les arguments `pl`, `uk`, `es`, `de` se replient sur l'anglais s'ils sont absents.
- Fiches « Comprendre » : `ArticlesFr`, `ArticlesEn` ; une langue sans fichier dédié affiche les fiches anglaises ([`Articles.kt`](https://github.com/Relthdrarxash/lunaison/blob/main/core/content/src/main/kotlin/fr/lunaison/core/content/Articles.kt)).
- Notifications et widget : `app/src/main/res/values-xx/strings.xml`.

### Sommelle

- Interface : `strings.xml` de chaque module (`app`, `core/designsystem`, `feature/*`).
- Textes calculés (bilans, résumé pour le médecin, graphiques) : une implémentation de `TextFormat` par langue ([`TextFormat.kt`](https://github.com/Relthdrarxash/sommelle/blob/main/core/presentation/src/main/kotlin/fr/sommelle/core/presentation/TextFormat.kt)).

### Respire

- Interface : `app/src/main/res/values-xx/strings.xml`, un seul module.

## État actuel

| | fr | en | pl | uk | es | de |
|---|---|---|---|---|---|---|
| Respire | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Lunaison, interface | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Lunaison, fiches et conseils | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Sommelle, interface et bilans | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |

Les traductions pl, uk, es et de ont été rédigées avec l'aide d'un assistant IA et **doivent être relues** par des personnes qui parlent ces langues avant publication.
