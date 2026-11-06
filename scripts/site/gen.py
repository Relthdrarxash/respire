import os, sys

OUT = sys.argv[1]
REPO = "https://github.com/Relthdrarxash/respire"
APK = REPO + "/releases/latest/download/Respire.apk"
PLAY_STORE = "https://play.google.com/store/apps/details?id=fr.respire.app"
ISSUES = REPO + "/issues"
MAIL = "pilmil.studio@proton.me"

LOGO = ('<svg viewBox="0 0 108 108" aria-hidden="true"><rect width="108" height="108" rx="30" fill="#4E7363"/>'
        '<circle cx="54" cy="54" r="22" fill="none" stroke="#EDEEE6" stroke-width="3"/>'
        '<circle cx="54" cy="54" r="9" fill="#EDEEE6"/></svg>')

T = {
"en": dict(
    lang="en", home="index.html", privacy="privacy.html", root="",
    other_lang="fr/index.html", other_privacy="fr/confidentialite.html", other_label="Français",
    title="Respire: a pause before the apps you open on autopilot",
    desc="Free, open-source Android app against doomscrolling. A breathing pause before the apps you open without thinking, session and daily limits, Reels and Shorts filter. No trackers.",
    nav_how="How it works", nav_privacy="Privacy", nav_install="Install",
    h1="Why are you opening it now?",
    lede="Respire is a free Android app that puts a breathing pause in front of the apps you open without thinking. Close them in one tap, or wait a few seconds and open them on purpose.",
    dl="Download APK", dl_play="Google Play", src="View the source code",
    fine="Android 8 and later. Free and open source (GPL-3.0). No ads, no account, no Internet permission. Available on Google Play and GitHub.",
    caption="Breathe in as the circle grows, out as it shrinks.",
    how_h="Built from what research says works",
    how_intro='Each feature rests on a study of smartphone overuse or on a stated design choice. The details, rule by rule, are on the page <a href="why.html">Why Respire works the way it does</a>.',
    why="why.html", nav_why="Why", foot_why="Why these choices",
    features=[
        ("Pause before opening", "A breathing screen with a countdown and one question. Closing is available right away; opening only once the countdown ends.", "Friction before opening social apps cut opening attempts and made openings more intentional (Grüning, Riedel and Lorenz-Spreen, PNAS, 2023)."),
        ("A wait that grows", "Each new attempt within the hour adds a few seconds. The more the urge comes back, the slower it gets.", None),
        ("Session end", "Opening an app gives you 10 minutes. If you are still there, the pause comes back in the middle of the feed.", None),
        ("Daily limit", "After a set number of opens per app, the app stays closed until tomorrow. Extending a session counts as an open.", "Hard-to-override limits reduced social media use by about 16% over 12 weeks, and most people kept them (Allcott, Gentzkow and Song, NBER, 2021)."),
        ("Risky hours", "Stricter rules during the hours you choose, for example in the evening. Respire suggests a time range from your own hourly data.", None),
        ("Reels and Shorts filter", "Leaves Instagram Reels and YouTube Shorts as soon as they open. Messages, search and regular videos stay available.", None),
        ("A lock on loosening", "Removing an app or relaxing a rule takes a 30-second wait, so you don't undo your settings on a whim.", "People tend to pause these tools and relapse quickly (Haliburton et al., CHI, 2024)."),
        ("Your week at a glance", "Pauses and how often you gave up, by day, by hour and by app, to see when temptation is strongest.", None),
    ],
    priv_h="Everything stays on your phone",
    priv_p="Respire has no Internet permission, so it cannot send anything anywhere, even by mistake.",
    priv_list=[
        "Pauses only use the name of the app in the foreground.",
        "The Reels and Shorts filter, off by default, only checks technical element IDs inside Instagram, YouTube and Facebook. It never reads text, messages or images.",
        "Settings and counters stay in the app's private storage and disappear when you uninstall it.",
    ],
    priv_link="Read the privacy policy",
    why_h="Why Respire is free",
    why_p="I spend too much time scrolling endless feeds myself, and people close to me have needed the same help. Apps that really help do exist, but their most useful features are often behind a paywall: daily limits, blocking Reels or Shorts, statistics. Respire aims to make these tools available to everyone, with no subscription, no ads and no data collection. Someone who wants to take back control of their time should not have to pay for it.",
    inst_h="Install",
    steps=[
        f'<strong>Install from Google Play</strong> via the <a href="{PLAY_STORE}">Google Play Store</a>, or <strong>download</strong> <a href="{APK}">Respire.apk</a> on your phone (Android will ask you to allow installs from your browser).',
        "<strong>Choose the apps</strong> to slow down in Respire.",
        "<strong>Turn on Respire</strong> in Accessibility settings, from the button in the app.",
        "<strong>Android 13 and later:</strong> if the switch is greyed out, go to Settings, Apps, Respire, the ⋮ menu, then Allow restricted settings, and try again.",
    ],
    faq_h="Questions",
    faq=[
        ("Why does Respire need accessibility access?", "It is the only way on Android to know which app is opening, so the pause can appear before it. Respire uses it for nothing else."),
        ("Can I turn it off?", "Yes, at any time in Accessibility settings or by uninstalling the app. The 30-second lock only applies to Respire's own rules."),
        ("Does it work on iPhone?", "No. iOS doesn't allow this kind of app outside Apple's Screen Time framework."),
        ("How can I report a problem?", f'Open an issue on <a href="{ISSUES}">GitHub</a> or write to <a href="mailto:{MAIL}">{MAIL}</a>. Respire collects no data, so your feedback is the only way to know what to improve.'),
    ],
    foot_license="Respire, by Pilmil Studio. Free software under the GPL-3.0 license.",
    foot_src="Source code", foot_privacy="Privacy policy",
    p_title="Privacy policy",
    p_date="Last updated: 28 October 2026",
    p_sections=[
        ("In short", ["Respire collects no personal data. It has no Internet permission, no analytics, no ads and no account. Everything it records stays on your phone."]),
        ("What Respire accesses", [
            "<ul><li>The name of the app coming to the foreground, through Android's accessibility service, to show a pause before the apps you chose.</li>"
            "<li>If you turn on the Reels or Shorts filter: the technical IDs of on-screen elements, only inside Instagram, YouTube and Facebook, to detect the short-video player. Text, messages and images are never read.</li>"
            "<li>The list of installed apps that have an icon, so you can choose which ones to slow down.</li></ul>"]),
        ("What Respire stores, on your phone only", [
            "<ul><li>Your settings and the apps you chose.</li>"
            "<li>The times of recent pauses and opens (last 24 hours), used for waits and limits.</li>"
            "<li>Daily and hourly counts of pauses, opens, times you gave up and Reels or Shorts skipped, kept for up to 60 days.</li>"
            "<li>A short diagnostic text listing technical element IDs, if you use the filters.</li></ul>",
            "This data stays in the app's private storage. Other apps cannot read it, and it is not included in Android cloud backups."]),
        ("What Respire shares", ["Nothing. Without the Internet permission, the app cannot send data. The diagnostic text only leaves the app if you tap Copy and paste it yourself."]),
        ("Deleting your data", ["Uninstall Respire, or clear its storage in Settings, Apps, Respire, Storage."]),
        ("Changes", ["Any change to this policy will be published on this page with a new date."]),
        ("Contact", [f'Respire is published by Pilmil Studio, an independent developer. Questions about this policy: <a href="mailto:{MAIL}">{MAIL}</a>, or open an issue on <a href="{ISSUES}">GitHub</a>.']),
    ],
    back="Back to Respire",
),
"fr": dict(
    lang="fr", home="index.html", privacy="confidentialite.html", root="../",
    other_lang="../index.html", other_privacy="../privacy.html", other_label="English",
    title="Respire : une pause avant les applis que tu ouvres par réflexe",
    desc="Appli Android libre et gratuite contre le doom scroll. Une pause de respiration avant les applis que tu ouvres sans y penser, limites de session et quotidiennes, filtre Reels et Shorts. Sans traqueur.",
    nav_how="Fonctionnement", nav_privacy="Vie privée", nav_install="Installer",
    h1="Pourquoi tu l’ouvres maintenant ?",
    lede="Respire est une appli Android gratuite qui place une pause de respiration devant les applis que tu ouvres sans y penser. Ferme-les d’un geste, ou attends quelques secondes et ouvre-les en connaissance de cause.",
    dl="Télécharger l’APK", dl_play="Google Play", src="Voir le code source",
    fine="Android 8 et plus. Libre et gratuite (GPL-3.0). Sans pub, sans compte, sans permission Internet. Disponible sur Google Play et GitHub.",
    caption="Inspire quand le cercle grandit, expire quand il rétrécit.",
    how_h="Conçue à partir de ce que dit la recherche",
    how_intro='Chaque fonction s’appuie sur une étude sur l’usage excessif du smartphone ou sur un choix de conception assumé. Le détail, règle par règle, est sur la page <a href="pourquoi.html">Pourquoi Respire est faite comme ça</a>.',
    why="pourquoi.html", nav_why="Pourquoi", foot_why="Pourquoi ces choix",
    features=[
        ("Une pause avant d’ouvrir", "Un écran de respiration avec un compte à rebours et une seule question. Fermer est possible tout de suite, ouvrir seulement à la fin du décompte.", "Ajouter une friction avant d’ouvrir les réseaux sociaux a réduit les tentatives d’ouverture et rendu les ouvertures plus intentionnelles (Grüning, Riedel et Lorenz-Spreen, PNAS, 2023)."),
        ("Une attente qui s’allonge", "Chaque nouvelle tentative dans l’heure ajoute quelques secondes. Plus l’envie revient, plus c’est lent.", None),
        ("Fin de session", "Ouvrir une appli te donne 10 minutes. Si tu y es encore, la pause revient au milieu du fil.", None),
        ("Quota quotidien", "Au-delà d’un nombre d’ouvertures par appli, l’appli reste fermée jusqu’au lendemain. Prolonger une session compte comme une ouverture.", "Des limites difficiles à contourner ont réduit l’usage des réseaux sociaux d’environ 16 % sur 12 semaines, et la plupart des participants les ont gardées (Allcott, Gentzkow et Song, NBER, 2021)."),
        ("Heures à risque", "Des règles plus strictes aux heures que tu choisis, le soir par exemple. Respire propose une plage à partir de ton propre suivi par heure.", None),
        ("Filtre Reels et Shorts", "Quitte les Reels d’Instagram et les Shorts de YouTube dès qu’ils s’ouvrent. Les messages, la recherche et les vidéos classiques restent accessibles.", None),
        ("Un verrou contre l’assouplissement", "Retirer une appli ou relâcher une règle impose 30 secondes d’attente, pour ne pas défaire tes réglages sur un coup de tête.", "Les utilisateurs ont tendance à mettre ces outils en pause et à rechuter vite (Haliburton et al., CHI, 2024)."),
        ("Ta semaine en un coup d’œil", "Les pauses et les renoncements par jour, par heure et par appli, pour voir quand la tentation est la plus forte.", None),
    ],
    priv_h="Tout reste sur ton téléphone",
    priv_p="Respire n’a pas la permission Internet : elle ne peut rien envoyer nulle part, même par erreur.",
    priv_list=[
        "Les pauses n’utilisent que le nom de l’appli au premier plan.",
        "Le filtre Reels et Shorts, désactivé par défaut, ne consulte que des identifiants techniques d’éléments, uniquement dans Instagram, YouTube et Facebook. Il ne lit jamais les textes, les messages ou les images.",
        "Les réglages et compteurs restent dans le stockage privé de l’appli et disparaissent à la désinstallation.",
    ],
    priv_link="Lire la politique de confidentialité",
    why_h="Pourquoi Respire est gratuite",
    why_p="Je passe moi aussi trop de temps à faire défiler des fils sans fin, et des proches ont eu le même besoin. Les applications qui aident vraiment existent, mais leurs fonctions les plus utiles sont souvent payantes : limites quotidiennes, blocage des Reels ou des Shorts, statistiques. L’idée de Respire est de rendre ces outils accessibles à tout le monde, sans abonnement, sans publicité et sans collecte de données. Ce n’est pas à la personne qui veut reprendre la main sur son temps de payer pour y arriver.",
    inst_h="Installer",
    steps=[
        f'<strong>Installe depuis Google Play</strong> sur le <a href="{PLAY_STORE}">Google Play Store</a>, ou <strong>télécharge</strong> <a href="{APK}">Respire.apk</a> depuis ton téléphone (Android te demandera d’autoriser l’installation depuis ton navigateur).',
        "<strong>Choisis les applis</strong> à freiner dans Respire.",
        "<strong>Active Respire</strong> dans les paramètres d’accessibilité, depuis le bouton de l’appli.",
        "<strong>Android 13 et plus :</strong> si l’interrupteur est grisé, va dans Paramètres, Applications, Respire, menu ⋮, puis Autoriser les paramètres restreints, et recommence.",
    ],
    faq_h="Questions",
    faq=[
        ("Pourquoi Respire a besoin de l’accessibilité ?", "C’est le seul moyen sous Android de savoir quelle appli s’ouvre, pour afficher la pause avant elle. Respire ne s’en sert pour rien d’autre."),
        ("Je peux la désactiver ?", "Oui, à tout moment dans les paramètres d’accessibilité ou en désinstallant l’appli. Le verrou de 30 secondes ne concerne que les règles de Respire."),
        ("Elle marche sur iPhone ?", "Non. iOS n’autorise pas ce type d’appli en dehors du cadre Temps d’écran d’Apple."),
        ("Comment signaler un problème ?", f'Ouvre une issue sur <a href="{ISSUES}">GitHub</a> ou écris à <a href="mailto:{MAIL}">{MAIL}</a>. Respire ne collecte aucune donnée : tes retours sont le seul moyen de savoir quoi améliorer.'),
    ],
    foot_license="Respire, par Pilmil Studio. Logiciel libre sous licence GPL-3.0.",
    foot_src="Code source", foot_privacy="Politique de confidentialité",
    p_title="Politique de confidentialité",
    p_date="Dernière mise à jour : 28 octobre 2026",
    p_sections=[
        ("En bref", ["Respire ne collecte aucune donnée personnelle. Elle n’a ni permission Internet, ni outil de mesure d’audience, ni publicité, ni compte. Tout ce qu’elle enregistre reste sur ton téléphone."]),
        ("Ce à quoi Respire accède", [
            "<ul><li>Le nom de l’appli qui passe au premier plan, via le service d’accessibilité d’Android, pour afficher une pause avant les applis que tu as choisies.</li>"
            "<li>Si tu actives le filtre Reels ou Shorts : les identifiants techniques des éléments affichés, uniquement dans Instagram, YouTube et Facebook, pour repérer le lecteur de vidéos courtes. Les textes, messages et images ne sont jamais lus.</li>"
            "<li>La liste des applis installées qui ont une icône, pour que tu choisisses celles à freiner.</li></ul>"]),
        ("Ce que Respire enregistre, uniquement sur ton téléphone", [
            "<ul><li>Tes réglages et les applis choisies.</li>"
            "<li>Les heures des pauses et ouvertures récentes (dernières 24 heures), utilisées pour les attentes et les limites.</li>"
            "<li>Les comptes par jour et par heure des pauses, ouvertures, renoncements et Reels ou Shorts écartés, conservés 60 jours au plus.</li>"
            "<li>Un court texte de diagnostic listant des identifiants techniques, si tu utilises les filtres.</li></ul>",
            "Ces données restent dans le stockage privé de l’appli. Les autres applis ne peuvent pas les lire, et elles ne sont pas incluses dans les sauvegardes Android dans le cloud."]),
        ("Ce que Respire partage", ["Rien. Sans permission Internet, l’appli ne peut pas envoyer de données. Le texte de diagnostic ne sort de l’appli que si tu appuies sur Copier et que tu le colles toi-même."]),
        ("Supprimer tes données", ["Désinstalle Respire, ou vide son stockage dans Paramètres, Applications, Respire, Stockage."]),
        ("Modifications", ["Toute modification de cette politique sera publiée sur cette page avec une nouvelle date."]),
        ("Contact", [f'Respire est publiée par Pilmil Studio, développeur indépendant. Pour toute question sur cette politique : <a href="mailto:{MAIL}">{MAIL}</a>, ou ouvre une issue sur <a href="{ISSUES}">GitHub</a>.']),
    ],
    back="Retour à Respire",
),
}

def head(t, title, desc, self_path, alt_path):
    other = "fr" if t["lang"] == "en" else "en"
    return f'''<!doctype html>
<html lang="{t["lang"]}">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>{title}</title>
<meta name="description" content="{desc}">
<link rel="alternate" hreflang="{other}" href="{alt_path}">
<link rel="icon" href="{t["root"]}assets/icon.svg" type="image/svg+xml">
<link rel="stylesheet" href="{t["root"]}assets/site.css">
<meta property="og:title" content="{title}">
<meta property="og:description" content="{desc}">
</head>
<body>
'''

def topbar(t, home_href, show_sections=True):
    links = ""
    if show_sections:
        links = (f'<a class="hide-sm" href="#how">{t["nav_how"]}</a>'
                 f'<a href="{t["why"]}">{t["nav_why"]}</a>'
                 f'<a href="#privacy">{t["nav_privacy"]}</a>'
                 f'<a href="#install">{t["nav_install"]}</a>')
    return f'''<header class="wrap top">
<a class="brand" href="{home_href}">{LOGO}Respire</a>
<nav aria-label="Menu">{links}<a href="{{other}}" lang="{"fr" if t["lang"]=="en" else "en"}">{t["other_label"]}</a></nav>
</header>
'''

def footer(t):
    return f'''<footer>
<div class="wrap">
<p style="margin:0">{t["foot_license"]}</p>
<nav aria-label="{t["foot_src"]}"><a href="mailto:{MAIL}">{MAIL}</a><a href="{REPO}">{t["foot_src"]}</a><a href="{t["why"]}">{t["foot_why"]}</a><a href="{t["privacy"]}">{t["foot_privacy"]}</a></nav>
</div>
</footer>
</body>
</html>
'''

def index(t):
    feats = ""
    for title, text, why in t["features"]:
        w = f'<p class="why">{why}</p>' if why else ""
        feats += f'<li><h3>{title}</h3><p>{text}</p>{w}</li>\n'
    priv = "".join(f"<li>{x}</li>" for x in t["priv_list"])
    steps = "".join(f"<li>{x}</li>" for x in t["steps"])
    faq = "".join(f"<details><summary>{q}</summary><p>{a}</p></details>" for q, a in t["faq"])
    html = head(t, t["title"], t["desc"], t["home"], t["other_lang"])
    html += topbar(t, t["home"]).replace("{other}", t["other_lang"])
    html += f'''<main>
<div class="wrap hero">
<div>
<h1>{t["h1"]}</h1>
<p class="lede">{t["lede"]}</p>
<div class="actions">
<a class="btn btn-primary" href="{PLAY_STORE}">{t["dl_play"]}</a>
<a class="btn btn-quiet" href="{APK}">{t["dl"]}</a>
<a class="btn btn-quiet" href="{REPO}">{t["src"]}</a>
</div>
<p class="fine">{t["fine"]}</p>
</div>
<figure class="breath" style="margin:0">
<span class="r1"></span><span class="r2"></span><span class="r3"></span>
<b aria-hidden="true">10</b>
<figcaption>{t["caption"]}</figcaption>
</figure>
</div>

<section id="how"><div class="wrap">
<h2>{t["how_h"]}</h2>
<p class="intro">{t["how_intro"]}</p>
<ul class="features">
{feats}</ul>
</div></section>

<section id="privacy"><div class="wrap">
<div class="privacy-box">
<div><h2>{t["priv_h"]}</h2></div>
<div><p>{t["priv_p"]}</p><ul>{priv}</ul><p style="margin:16px 0 0"><a href="{t["privacy"]}">{t["priv_link"]}</a></p></div>
</div>
</div></section>

<section id="free"><div class="wrap">
<h2>{t["why_h"]}</h2>
<p class="intro">{t["why_p"]}</p>
</div></section>

<section id="install"><div class="wrap">
<h2>{t["inst_h"]}</h2>
<ol class="steps">{steps}</ol>
</div></section>

<section><div class="wrap">
<h2>{t["faq_h"]}</h2>
{faq}
</div></section>
</main>
'''
    html += footer(t)
    return html

def privacy(t):
    body = ""
    for h, paras in t["p_sections"]:
        body += f"<h2>{h}</h2>"
        for p in paras:
            body += p if p.startswith("<ul>") else f"<p>{p}</p>"
    html = head(t, t["p_title"] + " | Respire", t["p_title"], t["privacy"], t["other_privacy"])
    html += topbar(t, t["home"], show_sections=False).replace("{other}", t["other_privacy"])
    html += f'''<main class="wrap"><article class="doc">
<h1>{t["p_title"]}</h1>
<p class="date">{t["p_date"]}</p>
{body}
<p style="margin-top:40px"><a href="{t["home"]}">{t["back"]}</a></p>
</article></main>
'''
    html += footer(t)
    return html

for code, t in T.items():
    d = OUT if code == "en" else os.path.join(OUT, "fr")
    os.makedirs(d, exist_ok=True)
    open(os.path.join(d, "index.html"), "w").write(index(t))
    open(os.path.join(d, t["privacy"]), "w").write(privacy(t))
    print(code, "ok")

open(os.path.join(OUT, "assets", "icon.svg"), "w").write(LOGO.replace(' aria-hidden="true"', ' xmlns="http://www.w3.org/2000/svg"'))
