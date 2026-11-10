# Scripts

- `i18n/gen.py` : ancien générateur des `strings.xml` (anglais, français, espagnol, allemand). Il n'est plus à jour : il ignore une quarantaine de textes ajoutés directement dans les fichiers `values*/strings.xml` (dont le polonais et l'ukrainien) et les écraserait. Ne pas le lancer sur `app/src/main/res` tant qu'il n'a pas été resynchronisé. Les textes se modifient pour l'instant directement dans les six `strings.xml`, en gardant les mêmes clés partout.
- `site/gen.py` : source du site GitHub Pages (anglais et français). `python3 scripts/site/gen.py docs`.
- `version-from-tag.sh`, `play-whatsnew.sh` : utilisés par le workflow de publication.
- `screenshots/` : captures d'écran et vidéo de démonstration sur émulateur (workflow `screenshots.yml`).
