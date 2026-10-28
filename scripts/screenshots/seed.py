#!/usr/bin/env python3
"""Données d'exemple pour les captures d'écran (jamais utilisé dans l'appli).

Usage : seed.py <epoch_secondes> <paquets séparés par des virgules> <fichier de sortie> [demo]
Écrit un fichier SharedPreferences « respire.xml » avec une semaine d'activité plausible,
calculée dans le fuseau Europe/Paris comme le ferait l'appli.
"""
import sys
from datetime import datetime, timedelta
from zoneinfo import ZoneInfo
from xml.sax.saxutils import escape

TZ = ZoneInfo("Europe/Paris")
now_s = int(sys.argv[1])
pkgs = [p for p in sys.argv[2].split(",") if p]
out = sys.argv[3]
minimal = len(sys.argv) > 4 and sys.argv[4] == "demo"
now = datetime.fromtimestamp(now_s, TZ)

ints, strings, bools = {}, {}, {}

def key(prefix, d):
    return f"{prefix}_{d.year}_{d.timetuple().tm_yday}"

def add_hours(prefix, d, hours):
    k = key(prefix, d)
    cur = [int(x) for x in strings.get(k, ",".join(["0"] * 24)).split(",")]
    for h, v in hours.items():
        cur[h] += v
    strings[k] = ",".join(map(str, cur))

# Profil de tentation : midi, fin d'après-midi et surtout le soir.
BASE = {8: 1, 12: 2, 13: 2, 17: 1, 18: 2, 21: 3, 22: 4, 23: 3}
WEEK_SCALE = [0.9, 1.2, 0.8, 1.0, 1.3, 0.7, 0.6]  # du plus ancien à aujourd'hui

if not minimal:
    for i in range(7):
        day = (now - timedelta(days=6 - i)).replace(hour=12, minute=0, second=0, microsecond=0)
        last_hour = now.hour - 1 if i == 6 else 23
        att, give = {}, {}
        for h, v in BASE.items():
            if h > last_hour:
                continue
            a = max(1, round(v * WEEK_SCALE[i]))
            att[h] = a
            give[h] = max(0, a - (1 if h in (22, 23) else 0) - (i % 2 if h == 13 else 0))
        add_hours("h_att", day, att)
        add_hours("h_give", day, give)
        ints[key("d_att", day)] = sum(att.values())
        ints[key("d_give", day)] = sum(give.values())
        # Répartition par appli : la première appli concentre l'essentiel.
        shares = [0.5, 0.3, 0.15, 0.05][: len(pkgs)]
        for p, s in zip(pkgs, shares):
            a = {h: round(v * s) for h, v in att.items() if round(v * s) > 0}
            add_hours(f"ph_att_{p}", day, a)
            ints[key(f"pd_att_{p}", day)] = sum(a.values())
            g = round(sum(give.values()) * s)
            ints[key(f"pd_give_{p}", day)] = g
            ints[key(f"pd_open_{p}", day)] = max(0, sum(a.values()) - g)
        if i == 6:
            # Journaux du jour (horodatages en millisecondes).
            stamps = []
            for h, a in att.items():
                for n in range(a):
                    stamps.append(int(day.replace(hour=h, minute=5 + 7 * n).timestamp() * 1000))
            gives = stamps[: sum(give.values())]
            for j, p in enumerate(pkgs[:2]):
                strings[f"att_{p}"] = ",".join(map(str, stamps[j::2]))
            strings["giveup"] = ",".join(map(str, gives))
            if pkgs:
                strings[f"open_{pkgs[0]}"] = ",".join(map(str, stamps[:2]))
    ints[key("d_short", now)] = 7
    add_hours("h_short", now, {h: 1 for h in (12, 13, 18, 21) if h < now.hour})
    bools["block_reels"] = True
    bools["block_shorts"] = True

bools["a11y_consent"] = not minimal

lines = ["<?xml version='1.0' encoding='utf-8' standalone='yes' ?>", "<map>"]
lines.append('    <set name="targets">')
lines += [f"        <string>{escape(p)}</string>" for p in pkgs]
lines.append("    </set>")
for k, v in sorted(ints.items()):
    lines.append(f'    <int name="{escape(k)}" value="{v}" />')
for k, v in sorted(strings.items()):
    lines.append(f'    <string name="{escape(k)}">{escape(v)}</string>')
for k, v in sorted(bools.items()):
    lines.append(f'    <boolean name="{escape(k)}" value="{str(v).lower()}" />')
lines.append("</map>")
open(out, "w").write("\n".join(lines) + "\n")
print(f"{len(ints)} compteurs, {len(strings)} journaux, applis : {pkgs}")
