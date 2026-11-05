# Kit de distribution éthique (F-Droid, IzzyOnDroid et annuaires libres)

Ce document regroupe la fiche technique prête à copier-coller pour référencer **Respire** (`fr.respire.app`) sur F-Droid et IzzyOnDroid.

---

## 1. Métadonnées F-Droid (`metadata/fr.respire.app.yml`)

```yaml
Categories:
  - Time
  - System
License: GPL-3.0-or-later
AuthorName: Pilmil Studio
AuthorEmail: pilmil.studio@proton.me
WebSite: https://relthdrarxash.github.io/respire/
SourceCode: https://github.com/Relthdrarxash/respire
IssueTracker: https://github.com/Relthdrarxash/respire/issues
Changelog: https://github.com/Relthdrarxash/respire/releases

AutoName: Respire

RepoType: git
Repo: https://github.com/Relthdrarxash/respire.git

Builds:
  - versionName: 4.3.0
    versionCode: 40300
    commit: v4.3.0
    subdir: app
    gradle:
      - yes
    prebuild: sed -i -e 's/versionCode = .*/versionCode = 40300/' -e 's/versionName = .*/versionName = "4.3.0"/' build.gradle.kts

AutoUpdateMode: Version
UpdateCheckMode: Tags
CurrentVersion: 4.3.0
CurrentVersionCode: 40300
```

---

## 2. Référencement IzzyOnDroid / AlternativeTo / Framalibre

- **IzzyOnDroid** : soumettre `https://github.com/Relthdrarxash/respire` sur `https://gitlab.com/IzzyOnDroid/repo/-/issues` (structure Fastlane en 4 langues `fr-FR`, `en-US`, `es-ES`, `de-DE` déjà présente, 0 pisteur, 0 permission `INTERNET`).
- **AlternativeTo** : référencer dans les catégories *Screen Time*, *Digital Wellbeing* et *App Blocker* avec les critères libres (`Open Source`, `Free`, `Privacy-focused`).
