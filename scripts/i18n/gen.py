import sys, os
from xml.sax.saxutils import escape
RES = sys.argv[1]
L = ["en", "fr", "es", "de"]
S = {
"app_name": ["Respire"]*4,
"service_label": ["Respire: pause before apps", "Respire : pause avant les applis", "Respire: pausa antes de las apps", "Respire: Pause vor Apps"],
"service_description": [
"Respire detects which app comes to the foreground to show a pause before the apps you chose. If you turn on the Reels or Shorts filter, it also checks the technical IDs of on-screen elements, only in Instagram, YouTube and Facebook, to spot the short-video player. Nothing is stored or sent: the app has no Internet access.",
"Respire détecte quelle appli passe au premier plan pour afficher une pause avant celles que tu as choisies. Si tu actives le filtre Reels ou Shorts, il consulte aussi les identifiants techniques des éléments affichés, uniquement dans Instagram, YouTube et Facebook, pour repérer le lecteur de vidéos courtes. Rien n’est stocké ni envoyé : l’appli n’a pas accès à Internet.",
"Respire detecta qué app pasa a primer plano para mostrar una pausa antes de las que elegiste. Si activas el filtro de Reels o Shorts, también consulta los identificadores técnicos de los elementos en pantalla, solo en Instagram, YouTube y Facebook, para detectar el reproductor de vídeos cortos. No se guarda ni se envía nada: la app no tiene acceso a Internet.",
"Respire erkennt, welche App in den Vordergrund kommt, um vor den von dir gewählten Apps eine Pause anzuzeigen. Wenn du den Reels- oder Shorts-Filter aktivierst, prüft es außerdem die technischen IDs der Bildschirmelemente, nur in Instagram, YouTube und Facebook, um den Kurzvideo-Player zu erkennen. Nichts wird gespeichert oder gesendet: Die App hat keinen Internetzugriff."],
"pause_hint_blocked": ["Come back tomorrow.", "Reviens demain.", "Vuelve mañana.", "Komm morgen wieder."],
"pause_hint_breathe": ["Breathe in as the circle grows, out as it shrinks.", "Inspire quand le cercle grandit, expire quand il rétrécit.", "Inspira cuando el círculo crece, espira cuando se encoge.", "Atme ein, wenn der Kreis wächst, und aus, wenn er schrumpft."],
"pause_hint_ready": ["If it’s really useful, go ahead.", "Si c’est vraiment utile, tu peux y aller.", "Si de verdad es útil, adelante.", "Wenn es wirklich nützlich ist, nur zu."],
"pause_title_blocked": ["Daily limit reached.", "Quota du jour atteint.", "Límite diario alcanzado.", "Tageslimit erreicht."],
"pause_title_session": ["That’s %1$d min.\\nStill going?", "Ça fait %1$d min.\\nTu continues vraiment ?", "Llevas %1$d min.\\n¿De verdad sigues?", "Schon %1$d Min.\\nWirklich weiter?"],
"pause_title_open": ["Why are you opening it now?", "Pourquoi tu l’ouvres maintenant ?", "¿Por qué la abres ahora?", "Warum öffnest du sie gerade?"],
"pause_opens_quota": ["Opens today: %1$d / %2$d", "Ouvertures aujourd’hui : %1$d / %2$d", "Aperturas hoy: %1$d / %2$d", "Heute geöffnet: %1$d / %2$d"],
"pause_opens": ["Opens today: %1$d", "Ouvertures aujourd’hui : %1$d", "Aperturas hoy: %1$d", "Heute geöffnet: %1$d"],
"pause_close": ["No, close it", "Non, je ferme", "No, la cierro", "Nein, schließen"],
"pause_open": ["Open anyway", "Ouvrir quand même", "Abrir de todos modos", "Trotzdem öffnen"],
"pause_continue": ["Continue", "Continuer", "Continuar", "Weiter"],
"pause_wait": ["%1$s in %2$d s", "%1$s dans %2$d s", "%1$s en %2$d s", "%1$s in %2$d s"],
"home_active": ["Active", "Actif", "Activo", "Aktiv"],
"home_inactive": ["Inactive", "Inactif", "Inactivo", "Inaktiv"],
"activation_title": ["One step before you start", "Une étape avant de commencer", "Un paso antes de empezar", "Ein Schritt vor dem Start"],
"activation_body": [
"Turn on “Respire” in Accessibility settings so the pauses can appear. The app has no Internet access and keeps nothing outside your phone.",
"Active « Respire » dans les paramètres d’accessibilité pour que les pauses s’affichent. L’appli n’a pas accès à Internet et ne garde rien hors de ton téléphone.",
"Activa «Respire» en los ajustes de accesibilidad para que aparezcan las pausas. La app no tiene acceso a Internet y no guarda nada fuera de tu teléfono.",
"Aktiviere „Respire“ in den Bedienungshilfen, damit die Pausen erscheinen. Die App hat keinen Internetzugriff und speichert nichts außerhalb deines Handys."],
"activation_restricted": [
"Option greyed out? Settings > Apps > Respire > ⋮ menu > Allow restricted settings.",
"Option grisée ? Paramètres > Applications > Respire > menu ⋮ > Autoriser les paramètres restreints.",
"¿Opción en gris? Ajustes > Aplicaciones > Respire > menú ⋮ > Permitir ajustes restringidos.",
"Option ausgegraut? Einstellungen > Apps > Respire > Menü ⋮ > Eingeschränkte Einstellungen zulassen."],
"activation_button": ["Open settings", "Ouvrir les paramètres", "Abrir ajustes", "Einstellungen öffnen"],
"stats_today": ["TODAY", "AUJOURD’HUI", "HOY", "HEUTE"],
"stats_none": ["no pauses\\nyet", "aucune pause\\npour l’instant", "ninguna pausa\\npor ahora", "noch keine\\nPausen"],
"stats_week": ["Gave up, last 7 days", "Renoncements sur 7 jours", "Renuncias en 7 días", "Verzichtet, letzte 7 Tage"],
"stats_shorts": ["Reels and Shorts skipped today: %1$d", "Reels et Shorts écartés aujourd’hui : %1$d", "Reels y Shorts evitados hoy: %1$d", "Heute übersprungene Reels und Shorts: %1$d"],
"section_apps": ["Slowed-down apps", "Applis freinées", "Apps frenadas", "Gebremste Apps"],
"apps_empty": ["No apps yet. Start with the ones you open without thinking.", "Aucune appli pour l’instant. Commence par celles que tu ouvres sans y penser.", "Aún no hay apps. Empieza por las que abres sin pensar.", "Noch keine Apps. Fang mit denen an, die du ohne nachzudenken öffnest."],
"apps_choose": ["Choose apps", "Choisir les applis", "Elegir apps", "Apps auswählen"],
"target_blocked": ["Blocked", "Bloqué", "Bloqueada", "Gesperrt"],
"section_rules": ["Rules", "Règles", "Reglas", "Regeln"],
"section_short": ["Short videos", "Vidéos courtes", "Vídeos cortos", "Kurzvideos"],
"short_reels_title": ["Hide Instagram Reels", "Masquer les Reels d’Instagram", "Ocultar los Reels de Instagram", "Instagram Reels ausblenden"],
"short_shorts_title": ["Hide YouTube Shorts", "Masquer les Shorts de YouTube", "Ocultar los Shorts de YouTube", "YouTube Shorts ausblenden"],
"short_note": ["The rest of the app stays usable. A Reel someone sends you in a message still opens.", "Le reste de l’appli reste utilisable. Un Reel reçu en message s’ouvre quand même.", "El resto de la app sigue disponible. Un Reel que te envían por mensaje se abre igualmente.", "Der Rest der App bleibt nutzbar. Ein Reel, das dir jemand per Nachricht schickt, öffnet sich trotzdem."],
"toast_reels_blocked": ["Reels hidden by Respire", "Reels masqués par Respire", "Reels ocultados por Respire", "Reels von Respire ausgeblendet"],
"toast_shorts_blocked": ["Shorts hidden by Respire", "Shorts masqués par Respire", "Shorts ocultados por Respire", "Shorts von Respire ausgeblendet"],
"loosen_short_title": ["Turn off this filter?", "Désactiver ce filtre ?", "¿Desactivar este filtro?", "Diesen Filter ausschalten?"],
"loosen_short_what": ["%1$s will be shown again.", "Les %1$s seront de nouveau visibles.", "Los %1$s volverán a verse.", "%1$s werden wieder angezeigt."],
"loosen_short_confirm": ["Turn off", "Désactiver", "Desactivar", "Ausschalten"],
"rule_delay_title": ["Wait", "Attente", "Espera", "Wartezeit"],
"rule_delay_help": ["Time to wait before you can open a slowed-down app.", "Temps à patienter avant de pouvoir ouvrir une appli freinée.", "Tiempo de espera antes de poder abrir una app frenada.", "Wartezeit, bevor du eine gebremste App öffnen kannst."],
"rule_step_title": ["Increase", "Allongement", "Incremento", "Steigerung"],
"rule_step_help": ["Seconds added for each new attempt within the hour.", "Secondes ajoutées à chaque nouvelle tentative dans l’heure.", "Segundos añadidos con cada nuevo intento en la misma hora.", "Sekunden, die bei jedem weiteren Versuch innerhalb einer Stunde hinzukommen."],
"rule_session_title": ["Session", "Session", "Sesión", "Sitzung"],
"rule_session_help": ["After this time, the pause comes back even if you’re still in the app.", "Au bout de cette durée, la pause revient même si tu es encore dans l’appli.", "Pasado este tiempo, la pausa vuelve aunque sigas en la app.", "Nach dieser Zeit kommt die Pause zurück, auch wenn du noch in der App bist."],
"rule_quota_title": ["Limit", "Quota", "Límite", "Limit"],
"rule_quota_help": ["Opens (and extensions) allowed per app per day. Beyond that, the app is blocked until tomorrow.", "Ouvertures (et prolongations) autorisées par appli et par jour. Au-delà, l’appli est bloquée jusqu’au lendemain.", "Aperturas (y prórrogas) permitidas por app y día. Después, la app queda bloqueada hasta mañana.", "Erlaubte Öffnungen (und Verlängerungen) pro App und Tag. Danach ist die App bis morgen gesperrt."],
"fmt_seconds": ["%1$d s"]*4,
"fmt_plus_seconds": ["+%1$d s"]*4,
"fmt_minutes": ["%1$d min", "%1$d min", "%1$d min", "%1$d Min."],
"fmt_per_day": ["%1$d / day", "%1$d / jour", "%1$d / día", "%1$d / Tag"],
"fmt_unlimited": ["unlimited", "illimité", "ilimitado", "unbegrenzt"],
"dialog_save": ["Save", "Enregistrer", "Guardar", "Speichern"],
"dialog_cancel": ["Cancel", "Annuler", "Cancelar", "Abbrechen"],
"dialog_wait": ["Wait (%1$d s)", "Patiente (%1$d s)", "Espera (%1$d s)", "Warte (%1$d s)"],
"loosen_rule": ["You’re making “%1$s” more permissive (%2$s → %3$s).", "Tu rends la règle « %1$s » plus permissive (%2$s → %3$s).", "Estás haciendo «%1$s» más permisiva (%2$s → %3$s).", "Du lockerst „%1$s“ (%2$s → %3$s)."],
"loosen_why": ["Relapses mostly happen when you loosen your rules on a whim. Take these few seconds to check you really want this.", "Les rechutes arrivent surtout quand on assouplit ses règles sur un coup de tête. Prends ces quelques secondes pour vérifier que tu le veux vraiment.", "Las recaídas suelen llegar al relajar tus reglas por impulso. Tómate estos segundos para comprobar que de verdad lo quieres.", "Rückfälle passieren meist, wenn man seine Regeln spontan lockert. Nimm dir diese Sekunden, um zu prüfen, ob du das wirklich willst."],
"loosen_app_title": ["Remove %1$s?", "Retirer %1$s ?", "¿Quitar %1$s?", "%1$s entfernen?"],
"loosen_app_what": ["%1$s will no longer be slowed down.", "%1$s ne sera plus freinée.", "%1$s ya no estará frenada.", "%1$s wird nicht mehr gebremst."],
"loosen_app_confirm": ["Remove", "Retirer", "Quitar", "Entfernen"],
"loosen_keep": ["Keep", "Garder", "Mantener", "Behalten"],
"picker_title": ["Apps to slow down", "Applis à freiner", "Apps que frenar", "Apps bremsen"],
"picker_search": ["Search for an app", "Rechercher une appli", "Buscar una app", "App suchen"],
"picker_loading": ["Loading apps…", "Chargement des applis…", "Cargando apps…", "Apps werden geladen…"],
"short_fb_title": ["Hide Facebook Reels (testing)", "Masquer les Reels de Facebook (en test)", "Ocultar los Reels de Facebook (en pruebas)", "Facebook-Reels ausblenden (Test)"],
"short_fb_note": ["Testing: this filter doesn’t block anything yet. Open a few Reels in Facebook, then copy the diagnostic below and share it through “Give feedback” so it can be tuned.", "En test : ce filtre ne bloque encore rien. Ouvre quelques Reels dans Facebook, puis copie le diagnostic ci-dessous et partage-le via « Donner mon avis » pour qu’on puisse le régler.", "En pruebas: este filtro todavía no bloquea nada. Abre algunos Reels en Facebook, copia el diagnóstico de abajo y compártelo con «Dar mi opinión» para poder ajustarlo.", "Test: Dieser Filter blockiert noch nichts. Öffne ein paar Reels in Facebook, kopiere die Diagnose unten und teile sie über „Feedback geben“, damit er eingestellt werden kann."],
"picker_suggest_title": ["Often opened on autopilot", "Souvent ouvertes par réflexe", "Se suelen abrir por inercia", "Oft aus Gewohnheit geöffnet"],
"picker_all_title": ["All apps", "Toutes les applis", "Todas las apps", "Alle Apps"],
"diag_link": ["Not working? Diagnostic", "Ça ne marche pas ? Diagnostic", "¿No funciona? Diagnóstico", "Funktioniert nicht? Diagnose"],
"diag_title": ["Reels and Shorts diagnostic", "Diagnostic Reels et Shorts", "Diagnóstico de Reels y Shorts", "Diagnose Reels und Shorts"],
"diag_hint": ["Open a Short in YouTube (or a Reel in Instagram or Facebook), come back here and copy this text. If it says no readable window, turn Respire off and on again in Accessibility settings.", "Ouvre un Short dans YouTube (ou un Reel dans Instagram ou Facebook), reviens ici et copie ce texte. S’il indique « aucune fenêtre lisible », désactive puis réactive Respire dans les paramètres d’accessibilité.", "Abre un Short en YouTube (o un Reel en Instagram o Facebook), vuelve aquí y copia este texto. Si indica que no hay ventana legible, desactiva y vuelve a activar Respire en los ajustes de accesibilidad.", "Öffne ein Short in YouTube (oder ein Reel in Instagram oder Facebook), komm hierher zurück und kopiere diesen Text. Wenn kein lesbares Fenster gemeldet wird, schalte Respire in den Bedienungshilfen aus und wieder ein."],
"diag_empty": ["Nothing analysed yet. Turn on a filter, then open YouTube, Instagram or Facebook.", "Rien d’analysé pour l’instant. Active un filtre, puis ouvre YouTube, Instagram ou Facebook.", "Aún no se ha analizado nada. Activa un filtro y abre YouTube, Instagram o Facebook.", "Noch nichts analysiert. Aktiviere einen Filter und öffne dann YouTube, Instagram oder Facebook."],
"diag_copy": ["Copy", "Copier", "Copiar", "Kopieren"],
"diag_copied": ["Copied", "Copié", "Copiado", "Kopiert"],
"dialog_close": ["Close", "Fermer", "Cerrar", "Schließen"],
"stats_tap_hint": ["tap a day for the hour-by-hour view", "touche un jour pour le détail par heure", "toca un día para verlo por horas", "tippe auf einen Tag für die Stundenansicht"],
"week_peak": ["This week, temptation peaks around %1$s", "Cette semaine, la tentation culmine vers %1$s", "Esta semana, la tentación alcanza su pico hacia las %1$s", "Diese Woche ist die Versuchung gegen %1$s am größten"],
"hours_peak": ["Most tempting time: %1$s", "Moment le plus tentant : %1$s", "Momento más tentador: %1$s", "Verlockendste Zeit: %1$s"],
"hours_empty": ["No temptation recorded that day.", "Aucune tentation enregistrée ce jour-là.", "No se registró ninguna tentación ese día.", "An diesem Tag wurde keine Versuchung erfasst."],
"hours_attempts": ["Temptations", "Tentations", "Tentaciones", "Versuchungen"],
"hours_giveups": ["Gave up", "Renoncements", "Renuncias", "Verzichtet"],
"hours_totals": ["Pauses: %1$d · gave up: %2$d · Reels/Shorts skipped: %3$d", "Pauses : %1$d · renoncements : %2$d · Reels/Shorts écartés : %3$d", "Pausas: %1$d · renuncias: %2$d · Reels/Shorts evitados: %3$d", "Pausen: %1$d · verzichtet: %2$d · übersprungene Reels/Shorts: %3$d"],
"hours_note": ["Hourly tracking started with version 3.5. Earlier days only have daily totals.", "Le suivi par heure a commencé avec la version 3.5. Les jours précédents n’ont que le total quotidien.", "El seguimiento por horas empezó con la versión 3.5. Los días anteriores solo tienen el total diario.", "Die stündliche Erfassung begann mit Version 3.5. Frühere Tage haben nur die Tagessumme."],
"section_risk": ["Risky hours", "Heures à risque", "Horas de riesgo", "Risikozeiten"],
"risk_title": ["Stricter rules at set hours", "Règles renforcées sur une plage", "Reglas reforzadas en una franja", "Strengere Regeln zu festen Zeiten"],
"risk_range": ["%1$s → %2$s"]*4,
"risk_now": ["Now", "En cours", "Ahora", "Jetzt"],
"risk_edit": ["Change the time range", "Modifier la plage", "Cambiar la franja", "Zeitraum ändern"],
"risk_desc_blocked": ["Wait doubled, apps blocked.", "Attente doublée, applis bloquées.", "Espera doble, apps bloqueadas.", "Doppelte Wartezeit, Apps gesperrt."],
"risk_help": ["During this time, the wait is doubled and opens are capped. Pick the hours when you give in most.", "Pendant cette plage, l’attente est doublée et les ouvertures sont plafonnées. Choisis les heures où tu craques le plus.", "Durante esta franja, la espera se duplica y las aperturas se limitan. Elige las horas en que más cedes.", "In diesem Zeitraum verdoppelt sich die Wartezeit und die Öffnungen sind begrenzt. Wähle die Stunden, in denen du am häufigsten schwach wirst."],
"risk_suggest": ["From your week: %1$s → %2$s", "D’après ta semaine : %1$s → %2$s", "Según tu semana: %1$s → %2$s", "Laut deiner Woche: %1$s → %2$s"],
"risk_use": ["Use", "Utiliser", "Usar", "Übernehmen"],
"risk_start": ["Start: %1$s", "Début : %1$s", "Inicio: %1$s", "Beginn: %1$s"],
"risk_end": ["End: %1$s", "Fin : %1$s", "Fin: %1$s", "Ende: %1$s"],
"risk_quota": ["Opens per app during this time: %1$d", "Ouvertures par appli sur la plage : %1$d", "Aperturas por app en la franja: %1$d", "Öffnungen pro App in diesem Zeitraum: %1$d"],
"risk_quota_zero_hint": ["0 means the apps stay blocked.", "0 : les applis restent bloquées.", "0: las apps quedan bloqueadas.", "0: Die Apps bleiben gesperrt."],
"risk_off_title": ["Turn off risky hours?", "Désactiver les heures à risque ?", "¿Desactivar las horas de riesgo?", "Risikozeiten ausschalten?"],
"loosen_risk": ["You’re loosening your risky hours.", "Tu assouplis tes heures à risque.", "Estás relajando tus horas de riesgo.", "Du lockerst deine Risikozeiten."],
"pause_title_risk": ["Risky hours: blocked until %1$s.", "Heures à risque : bloqué jusqu’à %1$s.", "Horas de riesgo: bloqueado hasta las %1$s.", "Risikozeit: gesperrt bis %1$s."],
"pause_hint_risk": ["You chose to protect this time of day.", "Tu as choisi de protéger ce moment de la journée.", "Elegiste proteger este momento del día.", "Du hast entschieden, diese Tageszeit zu schützen."],
"pause_opens_risk": ["Risky hours: %1$d / %2$d opens", "Heures à risque : %1$d / %2$d ouvertures", "Horas de riesgo: %1$d / %2$d aperturas", "Risikozeit: %1$d / %2$d Öffnungen"],
"apps_tap_hint": ["Tap an app to see its week.", "Touche une appli pour voir sa semaine.", "Toca una app para ver su semana.", "Tippe auf eine App, um ihre Woche zu sehen."],
"app_empty": ["No data for this app yet.", "Pas encore de données pour cette appli.", "Aún no hay datos para esta app.", "Noch keine Daten für diese App."],
"app_week_totals": ["Last 7 days: %1$d temptations · %2$d opens · %3$d gave up", "7 derniers jours : %1$d tentations · %2$d ouvertures · %3$d renoncements", "Últimos 7 días: %1$d tentaciones · %2$d aperturas · %3$d renuncias", "Letzte 7 Tage: %1$d Versuchungen · %2$d Öffnungen · %3$d verzichtet"],
"app_giveup_rate": ["You gave up %1$d%% of the time", "Tu as renoncé %1$d %% des fois", "Renunciaste el %1$d %% de las veces", "Du hast in %1$d %% der Fälle verzichtet"],
"app_note": ["Per-app tracking started with version 3.7.", "Le suivi par appli a commencé avec la version 3.7.", "El seguimiento por app empezó con la versión 3.7.", "Die Erfassung pro App begann mit Version 3.7."],
"disclosure_title": ["Before you turn on Respire", "Avant d’activer Respire", "Antes de activar Respire", "Bevor du Respire aktivierst"],
"disclosure_body": [
"Respire uses Android’s Accessibility Service API. The app needs it to work.\n\nWhat it accesses:\n• The name of the app that comes to the foreground, to show a pause before the apps you chose.\n• If you turn on the Reels or Shorts filter: the technical IDs of on-screen elements, in Instagram, YouTube and Facebook only, to detect the short-video player. Text, messages and images are never read.\n\nWhat happens to it: it is used on your phone only, at the moment it is accessed. Nothing is collected, sent or shared. Respire has no Internet permission.",
"Respire utilise l’API du service d’accessibilité d’Android. L’appli en a besoin pour fonctionner.\n\nCe à quoi elle accède :\n• Le nom de l’appli qui passe au premier plan, pour afficher une pause avant les applis que tu as choisies.\n• Si tu actives le filtre Reels ou Shorts : les identifiants techniques des éléments affichés, uniquement dans Instagram, YouTube et Facebook, pour repérer le lecteur de vidéos courtes. Les textes, messages et images ne sont jamais lus.\n\nCe qu’elle en fait : ces informations sont utilisées sur ton téléphone uniquement, au moment où elles sont lues. Rien n’est collecté, envoyé ni partagé. Respire n’a pas la permission Internet.",
"Respire utiliza la API del servicio de accesibilidad de Android. La app la necesita para funcionar.\n\nA qué accede:\n• El nombre de la app que pasa a primer plano, para mostrar una pausa antes de las apps que elegiste.\n• Si activas el filtro de Reels o Shorts: los identificadores técnicos de los elementos en pantalla, solo en Instagram, YouTube y Facebook, para detectar el reproductor de vídeos cortos. Nunca se leen textos, mensajes ni imágenes.\n\nQué hace con ello: se usa solo en tu teléfono, en el momento en que se lee. No se recopila, envía ni comparte nada. Respire no tiene permiso de Internet.",
"Respire nutzt die Schnittstelle des Android-Bedienungshilfendienstes. Die App braucht sie, um zu funktionieren.\n\nWorauf sie zugreift:\n• Den Namen der App, die in den Vordergrund kommt, um vor den von dir gewählten Apps eine Pause anzuzeigen.\n• Wenn du den Reels- oder Shorts-Filter aktivierst: die technischen IDs der Bildschirmelemente, nur in Instagram, YouTube und Facebook, um den Kurzvideo-Player zu erkennen. Texte, Nachrichten und Bilder werden nie gelesen.\n\nWas damit passiert: Die Daten werden nur auf deinem Handy verwendet, in dem Moment, in dem sie gelesen werden. Nichts wird erfasst, gesendet oder geteilt. Respire hat keine Internetberechtigung."],
"disclosure_accept": ["I agree", "J’accepte", "Acepto", "Ich stimme zu"],
"disclosure_decline": ["Not now", "Pas maintenant", "Ahora no", "Nicht jetzt"],
"link_privacy": ["Privacy policy", "Confidentialité", "Privacidad", "Datenschutz"],
"link_feedback": ["Give feedback", "Donner mon avis", "Dar mi opinión", "Feedback geben"],
"link_source": ["Source code", "Code source", "Código fuente", "Quellcode"],
"url_privacy": ["https://relthdrarxash.github.io/respire/privacy.html", "https://relthdrarxash.github.io/respire/fr/confidentialite.html", "https://relthdrarxash.github.io/respire/privacy.html", "https://relthdrarxash.github.io/respire/privacy.html"],
"app_version": ["Respire %1$s", "Respire %1$s", "Respire %1$s", "Respire %1$s"],
"back": ["Back", "Retour", "Atrás", "Zurück"],
}
P = {
"risk_desc_quota": [
 {"one": "Wait doubled, up to %d open per app.", "other": "Wait doubled, up to %d opens per app."},
 {"one": "Attente doublée, jusqu’à %d ouverture par appli.", "other": "Attente doublée, jusqu’à %d ouvertures par appli."},
 {"one": "Espera doble, hasta %d apertura por app.", "other": "Espera doble, hasta %d aperturas por app."},
 {"one": "Doppelte Wartezeit, bis zu %d Öffnung pro App.", "other": "Doppelte Wartezeit, bis zu %d Öffnungen pro App."}],
"stats_gave_up": [
 {"one": "time you gave up\\nout of %d pause", "other": "times you gave up\\nout of %d pauses"},
 {"one": "fois où tu as renoncé\\nsur %d pause", "other": "fois où tu as renoncé\\nsur %d pauses"},
 {"one": "veces que renunciaste\\nde %d pausa", "other": "veces que renunciaste\\nde %d pausas"},
 {"one": "Mal verzichtet\\nbei %d Pause", "other": "Mal verzichtet\\nbei %d Pausen"}],
"picker_add_all": [
 {"one": "Add this app", "other": "Add these %d apps"},
 {"one": "Ajouter cette appli", "other": "Ajouter ces %d applis"},
 {"one": "Añadir esta app", "other": "Añadir estas %d apps"},
 {"one": "Diese App hinzufügen", "other": "Diese %d Apps hinzufügen"}],
}
NOTRANS = {"app_name", "fmt_seconds", "fmt_plus_seconds", "risk_range", "app_version"}
def esc(v):
    v = escape(v).replace("'", "\\'").replace('"', '\\"')
    return v
for i, lang in enumerate(L):
    d = os.path.join(RES, "values" if lang == "en" else "values-" + lang)
    os.makedirs(d, exist_ok=True)
    out = ['<?xml version="1.0" encoding="utf-8"?>', '<resources>']
    for k, vals in S.items():
        if lang != "en" and k in NOTRANS: continue
        attr = ' translatable="false"' if (lang == "en" and k in NOTRANS) else ''
        out.append(f'    <string name="{k}"{attr}>{esc(vals[i])}</string>')
    for k, vals in P.items():
        out.append(f'    <plurals name="{k}">')
        forms = vals[i]
        for q in (["one", "many", "other"] if lang in ("fr", "es") else ["one", "other"]):
            text = forms.get(q, forms["other"])
            out.append(f'        <item quantity="{q}">{esc(text)}</item>')
        out.append('    </plurals>')
    out.append('</resources>\n')
    open(os.path.join(d, "strings.xml"), "w").write("\n".join(out))
    print(lang, len(S) - (len(NOTRANS) if lang != "en" else 0))
