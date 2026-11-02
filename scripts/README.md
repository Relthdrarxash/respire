# Scripts

- `i18n/gen.py` : source unique des textes de l'appli dans les 4 langues. Modifier les textes ici, puis
  `python3 scripts/i18n/gen.py app/src/main/res` régénère les quatre `strings.xml`.
- `site/gen.py` : source du site GitHub Pages (anglais et français). `python3 scripts/site/gen.py docs`.
- `version-from-tag.sh`, `play-whatsnew.sh` : utilisés par le workflow de publication.
- `screenshots/` : captures d'écran et vidéo de démonstration sur émulateur (workflow `screenshots.yml`).
