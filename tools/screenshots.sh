#!/usr/bin/env bash
# Régénère les captures d'écran de l'appli (docs/marketing/captures/*.png) sans émulateur ni téléphone.
# Paparazzi rend les écrans Compose sur la JVM ; le script lance les tests de capture puis copie les PNG bruts (1080x2340), en français puis en anglais (captures/en/).
# Usage : ./tools/screenshots.sh
set -euo pipefail

racine="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$racine"

sortie="docs/marketing/captures"
# Dossier où le mode « record » de Paparazzi écrit ses images.
images="app/src/test/snapshots/images"

# Repart d'un dossier d'images propre et force le rendu : une capture périmée ne doit jamais passer pour récente.
rm -rf "$images"
./gradlew :app:cleanTestDebugUnitTest :app:recordPaparazziDebug --no-daemon

# Français : docs/marketing/captures/ ; anglais : docs/marketing/captures/en/ (entrée du générateur de visuels, via fr/ et en/).
for langue in fr en; do
  if [ "$langue" = fr ]; then classe=ScreensTest; dest="$sortie"; else classe=ScreensEnTest; dest="$sortie/en"; fi
  mkdir -p "$dest"
  for nom in accueil demarrage statistiques reglages choix_applis respiration quota_atteint; do
    source=$(find "$images" -name "*_${classe}_*_${nom}.png" | head -n 1)
    if [ -z "$source" ]; then
      echo "Capture introuvable : ${langue} ${nom}" >&2
      exit 1
    fi
    cp "$source" "$dest/${nom}.png"
    echo "OK ${dest}/${nom}.png"
  done
done
