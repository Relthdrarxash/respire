#!/usr/bin/env bash
# Captures d'écran et vidéo de démonstration de Respire sur un émulateur (lancé par screenshots.yml).
# Les données affichées sont des exemples générés par seed.py.
set -uo pipefail
cd "$(dirname "$0")"
OUT="${OUT:-$PWD/out}"
mkdir -p "$OUT"
APK="$1"
PKG=fr.respire.app
UI="python3 ui.py"

adb root >/dev/null; sleep 3; adb wait-for-device
adb shell setprop persist.sys.timezone Europe/Paris
adb shell settings put global auto_time_zone 0
adb shell settings put system screen_off_timeout 1800000
adb shell svc power stayon true
# Barre d'état propre (mode démo).
adb shell settings put global sysui_demo_allowed 1
demo() { adb shell am broadcast -a com.android.systemui.demo -e command "$@" >/dev/null; }
demo enter
demo clock -e hhmm 2140
demo battery -e level 82 -e plugged false
demo network -e wifi show -e level 4
demo network -e mobile show -e datatype none -e level 4
demo notifications -e visible false

adb install -r "$APK"
adb shell cmd locale set-app-locales $PKG --locales fr-FR

# Applis cibles disponibles sur l'émulateur.
INSTALLED=$(adb shell pm list packages | tr -d '\r' | sed 's/package://')
TARGETS=""
for p in com.google.android.youtube com.android.chrome com.google.android.gm com.google.android.apps.photos com.google.android.apps.maps; do
  if echo "$INSTALLED" | grep -qx "$p"; then TARGETS="$TARGETS,$p"; fi
done
TARGETS="${TARGETS#,}"
echo "Cibles : $TARGETS"
FIRST="${TARGETS%%,*}"
NOW=$(adb shell date +%s | tr -d '\r')

push_prefs() {
  adb shell am force-stop $PKG
  adb shell mkdir -p /data/data/$PKG/shared_prefs
  adb push "$1" /data/data/$PKG/shared_prefs/respire.xml >/dev/null
  local owner
  owner=$(adb shell stat -c %u /data/data/$PKG | tr -d '\r')
  adb shell chown "$owner:$owner" /data/data/$PKG/shared_prefs /data/data/$PKG/shared_prefs/respire.xml
  adb shell chmod 660 /data/data/$PKG/shared_prefs/respire.xml
}
shot() { sleep "${2:-2}"; adb exec-out screencap -p > "$OUT/$1.png"; echo "capture $1"; }
service_on() {
  adb shell settings put secure enabled_accessibility_services $PKG/.PauseService
  adb shell settings put secure accessibility_enabled 1
}

# L'écran de pause s'anime en continu (uiautomator ne peut pas le lire) : boutons par coordonnées,
# mesurées sur l'écran 1080 x 1920 du profil pixel_2.
CLOSE_X=540; CLOSE_Y=1583; OPEN_X=540; OPEN_Y=1740

# ---- Vidéo de démonstration (déclaration d'accessibilité) -------------------------
python3 seed.py "$NOW" "$TARGETS" /tmp/demo.xml demo
adb shell pm clear $PKG >/dev/null
adb shell cmd locale set-app-locales $PKG --locales fr-FR
push_prefs /tmp/demo.xml
adb shell settings put secure enabled_accessibility_services null
adb shell input keyevent KEYCODE_HOME
adb shell screenrecord --bit-rate 6000000 --time-limit 170 /sdcard/demo.mp4 &
REC=$!
sleep 2
adb shell am start -n $PKG/.MainActivity >/dev/null; sleep 4
$UI tap "Ouvrir les paramètres" || $UI tap "Inactif"
sleep 3                                   # écran d'information sur l'accessibilité
$UI dump | head -40
sleep 4
$UI tap "accepte"; sleep 3                # paramètres d'accessibilité d'Android
$UI tap "Respire" ; sleep 2
$UI tap "Use Respire" || $UI tap "Utiliser" ; sleep 2
$UI tap "=Allow" || $UI tap "=Autoriser" ; sleep 2
adb shell am force-stop com.android.settings
service_on                                 # au cas où l'interface n'aurait pas suivi
adb shell input keyevent KEYCODE_HOME; sleep 2
adb shell monkey -p com.android.chrome -c android.intent.category.LAUNCHER 1 >/dev/null; sleep 6
shot demo_pause 0
sleep 10                                   # fin du compte à rebours
adb shell input tap $CLOSE_X $CLOSE_Y; sleep 3
adb shell monkey -p com.google.android.youtube -c android.intent.category.LAUNCHER 1 >/dev/null; sleep 5
sleep 12
adb shell input tap $OPEN_X $OPEN_Y ; sleep 6            # l'appli s'ouvre après l'attente
adb shell input keyevent KEYCODE_HOME; sleep 2
kill -INT $REC 2>/dev/null; sleep 4
adb pull /sdcard/demo.mp4 "$OUT/demo-accessibilite.mp4" >/dev/null && echo "vidéo ok"

# ---- Captures d'écran pour la fiche ------------------------------------------------
python3 seed.py "$NOW" "$TARGETS" /tmp/full.xml
adb shell am force-stop com.android.settings
adb shell am force-stop com.google.android.youtube
push_prefs /tmp/full.xml
service_on
adb shell am start -n $PKG/.MainActivity >/dev/null
shot 1-accueil 5
adb shell input swipe 540 1500 540 500 600; shot 2-accueil-suite 2
adb shell input swipe 540 1500 540 400 600; shot 3-accueil-regles 2
$UI tap "Choisir les applis"; shot 5-choix-applis 3
adb shell input keyevent KEYCODE_BACK; sleep 2
adb shell am start -n $PKG/.MainActivity >/dev/null; sleep 2
# Écran de pause, ouvert directement pour la première appli cible.
adb shell am start -n $PKG/.PauseActivity --es pkg "$FIRST" --es mode open >/dev/null
shot 4-pause 3
ls -l "$OUT"
