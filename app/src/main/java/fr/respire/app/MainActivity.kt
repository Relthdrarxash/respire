package fr.respire.app

import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import fr.respire.app.ui.AppItem
import fr.respire.app.ui.DayDetail
import fr.respire.app.ui.DisclosureDialog
import fr.respire.app.ui.Calme
import fr.respire.app.ui.HomeScreen
import fr.respire.app.ui.HomeUi
import fr.respire.app.ui.OnboardingScreen
import fr.respire.app.ui.PickerScreen
import fr.respire.app.ui.RespireTheme
import fr.respire.app.ui.RiskUi
import fr.respire.app.ui.Rule
import fr.respire.app.ui.ShortKind
import fr.respire.app.ui.TargetStat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    private lateinit var prefs: Prefs

    private var home by mutableStateOf<HomeUi?>(null)
    private var allApps by mutableStateOf<List<AppItem>>(emptyList())
    private var selected by mutableStateOf<Set<String>>(emptySet())

    /** Vrai entre le départ vers les réglages d'accessibilité et le retour dans l'appli. */
    private var settingsLaunched = false

    /** Vrai une fois revenu des réglages : permet de dire « pas encore actif » plutôt que de rester muet. */
    private var returnedFromSettings by mutableStateOf(false)

    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { refresh() }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        prefs = Prefs(this)
        prefs.pruneOldDays()
        selected = prefs.targets
        loadApps()
        CheckInWorker.schedule(this)

        setContent {
            RespireTheme {
                var picking by rememberSaveable { mutableStateOf(false) }
                var disclosing by rememberSaveable { mutableStateOf(false) }
                var onboarding by rememberSaveable {
                    mutableStateOf(
                        Onboarding.shouldShow(
                            done = prefs.onboardingDone,
                            hasTargets = prefs.targets.isNotEmpty(),
                            consent = prefs.accessibilityConsent,
                            serviceSeenOn = prefs.serviceSeenOn,
                        )
                    )
                }
                var onboardingStep by rememberSaveable { mutableIntStateOf(0) }
                val step = OnboardingStep.entries[onboardingStep.coerceIn(0, OnboardingStep.entries.lastIndex)]
                val finishOnboarding = {
                    prefs.onboardingDone = true
                    onboarding = false
                    onboardingStep = 0
                }
                if (disclosing) {
                    DisclosureDialog(
                        onAccept = {
                            prefs.accessibilityConsent = true
                            disclosing = false
                            openAccessibilitySettings()
                        },
                        onDecline = { disclosing = false },
                    )
                }
                BackHandler(enabled = picking) { picking = false }
                // Retour dans l'accueil : étape précédente. Sur la première étape, le retour système quitte l'appli.
                BackHandler(enabled = onboarding && !picking && step.previous() != null) {
                    step.previous()?.let { onboardingStep = it.ordinal }
                }

                val ui = home
                when {
                    picking -> PickerScreen(
                        apps = allApps,
                        selected = selected,
                        onToggle = ::setTarget,
                        onBack = { picking = false },
                    )
                    ui != null && onboarding -> OnboardingScreen(
                        step = step,
                        ui = ui,
                        activation = Onboarding.activationStatus(ui.serviceOn, returnedFromSettings),
                        showRestrictedHelp = Onboarding.needsRestrictedHelp(Build.VERSION.SDK_INT),
                        onAddApp = { pkg -> setTarget(pkg, true) },
                        onPickApps = { picking = true },
                        onNext = { step.next()?.let { onboardingStep = it.ordinal } },
                        onBack = { step.previous()?.let { onboardingStep = it.ordinal } },
                        onSkip = finishOnboarding,
                        onAccept = {
                            prefs.accessibilityConsent = true
                            val next = Onboarding.afterDisclosure(accepted = true)
                            if (next != null) onboardingStep = next.ordinal else finishOnboarding()
                        },
                        onDecline = {
                            val next = Onboarding.afterDisclosure(accepted = false)
                            if (next != null) onboardingStep = next.ordinal else finishOnboarding()
                        },
                        onOpenAccessibility = ::openAccessibilitySettings,
                        onOpenAppInfo = ::openAppInfo,
                        onFinish = finishOnboarding,
                    )
                    ui != null -> HomeScreen(
                        ui = ui,
                        onOpenAccessibility = {
                            if (prefs.accessibilityConsent) openAccessibilitySettings() else disclosing = true
                        },
                        onOpenLink = ::openLink,
                        onPickApps = { picking = true },
                        onSaveRule = ::saveRule,
                        onSetShortForm = ::setShortForm,
                        onSaveRisk = ::saveRisk,
                        appWeek = { pkg -> prefs.appWeek(pkg) },
                        onAddApp = { pkg -> setTarget(pkg, true) },
                        onAllowNotifications = ::askNotifications,
                        onReview = {
                            prefs.reviewPromptDone = true
                            openStore()
                            refresh()
                        },
                        onDismissReview = {
                            prefs.reviewPromptDone = true
                            refresh()
                        },
                        onReplayOnboarding = {
                            onboardingStep = 0
                            onboarding = true
                        },
                        onOpenAppInfo = ::openAppInfo,
                    )
                    else -> Box(Modifier.fillMaxSize().background(Calme.Ground))
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (settingsLaunched) {
            settingsLaunched = false
            returnedFromSettings = true
        }
        refresh()
        RespireWidget.refreshAll(this)
    }

    private fun installedVersion(): String = try {
        packageManager.getPackageInfo(packageName, 0).versionName.orEmpty()
    } catch (e: android.content.pm.PackageManager.NameNotFoundException) {
        ""
    }

    private fun openAccessibilitySettings() {
        settingsLaunched = true
        returnedFromSettings = false
        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }

    /** Infos de l'appli, où se trouve « Autoriser les paramètres restreints » (menu ⋮, Android 13+). */
    private fun openAppInfo() {
        try {
            startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, android.net.Uri.fromParts("package", packageName, null))
            )
        } catch (e: android.content.ActivityNotFoundException) {
            // Pas d'écran d'infos : rien à faire.
        }
    }

    private fun openLink(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url)))
        } catch (e: android.content.ActivityNotFoundException) {
            // Pas de navigateur : rien à faire.
        }
    }

    private fun serviceEnabled(): Boolean {
        val enabled = Settings.Secure.getString(
            contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        val me = ComponentName(this, PauseService::class.java)
        return enabled.split(':').any { ComponentName.unflattenFromString(it) == me }
    }

    /** Fiche Play Store, ou sa page web sans Store (APK GitHub). */
    private fun openStore() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse("market://details?id=$packageName")))
        } catch (e: android.content.ActivityNotFoundException) {
            openLink("https://play.google.com/store/apps/details?id=$packageName")
        }
    }

    private fun askNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun refresh() {
        val byPkg = allApps.associateBy { it.pkg }
        val serviceOn = serviceEnabled()
        // Vu actif au moins une fois : un arrêt plus tard sera signalé par CheckInWorker.
        if (serviceOn) prefs.serviceSeenOn = true
        val targets = selected
            .map { pkg -> byPkg[pkg] ?: AppItem(pkg, pkg, null) }
            .sortedBy { it.label.lowercase() }
            .map { TargetStat(it, prefs.opensToday(it.pkg), prefs.quotaReached(it.pkg)) }

        home = HomeUi(
            serviceOn = serviceOn,
            giveUps = prefs.giveUpsToday(),
            pauses = prefs.attemptsTodayAll(),
            week = prefs.giveUpsLastWeek(),
            weekLabels = weekLabels(),
            targets = targets,
            rules = mapOf(
                Rule.DELAY to prefs.baseDelay,
                Rule.STEP to prefs.stepDelay,
                Rule.SESSION to prefs.sessionMinutes,
                Rule.QUOTA to prefs.dailyQuota,
            ),
            blockReels = prefs.blockReels,
            blockShorts = prefs.blockShorts,
            blockFacebookReels = prefs.blockFacebookReels,
            facebookInstalled = packageManager.getLaunchIntentForPackage(ShortFormBlocker.FACEBOOK) != null,
            shortsBlockedToday = prefs.shortsBlockedToday(),
            diagnostic = prefs.shortDiagnostic(),
            days = weekDays(),
            risk = RiskUi(
                enabled = prefs.riskEnabled,
                start = prefs.riskStart,
                end = prefs.riskEnd,
                quota = prefs.riskQuota,
                active = prefs.inRiskWindow(),
            ),
            version = installedVersion(),
            suggestions = Suggestions.pick(byPkg.keys, selected).mapNotNull { byPkg[it] },
            canNotify = CheckInWorker.canNotify(this),
            showReviewPrompt = !prefs.reviewPromptDone && prefs.giveUpsLastWeek(count = 7).sum() >= REVIEW_AFTER_GIVE_UPS,
        )
    }

    /** Initiale du jour dans la langue du téléphone (L, M, M... / M, T, W...). */
    private fun weekLabels(count: Int = 56): List<String> {
        val fmt = SimpleDateFormat("EEEEE", Locale.getDefault())
        return Rules.pastDays(System.currentTimeMillis(), count).map { fmt.format(Date(it)) }
    }

    /** Les derniers jours, heure par heure, du plus ancien à aujourd'hui (8 semaines = 56 jours). */
    private fun weekDays(count: Int = 56): List<DayDetail> {
        val fmt = SimpleDateFormat("EEEE d MMMM", Locale.getDefault())
        return Rules.pastDays(System.currentTimeMillis(), count).map { t ->
            val h = prefs.hoursOf(t)
            DayDetail(
                title = fmt.format(Date(t)).replaceFirstChar { it.titlecase(Locale.getDefault()) },
                attempts = h.attempts,
                giveUps = h.giveUps,
                shorts = h.shorts,
            )
        }
    }

    private fun saveRisk(enabled: Boolean, start: Int, end: Int, quota: Int) {
        prefs.riskEnabled = enabled
        prefs.riskStart = start
        prefs.riskEnd = end
        prefs.riskQuota = quota
        refresh()
    }

    private fun setShortForm(kind: ShortKind, on: Boolean) {
        when (kind) {
            ShortKind.REELS -> prefs.blockReels = on
            ShortKind.SHORTS -> prefs.blockShorts = on
            ShortKind.FACEBOOK -> prefs.blockFacebookReels = on
        }
        refresh()
    }

    private fun setTarget(pkg: String, on: Boolean) {
        prefs.targets = if (on) prefs.targets + pkg else prefs.targets - pkg
        selected = prefs.targets
        refresh()
    }

    private fun saveRule(rule: Rule, value: Int) {
        when (rule) {
            Rule.DELAY -> prefs.baseDelay = value
            Rule.STEP -> prefs.stepDelay = value
            Rule.SESSION -> prefs.sessionMinutes = value
            Rule.QUOTA -> prefs.dailyQuota = value
        }
        refresh()
    }

    private fun loadApps() {
        Thread {
            val pm = packageManager
            val launcher = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            @Suppress("DEPRECATION")
            val infos = pm.queryIntentActivities(launcher, 0)
            val chosen = prefs.targets
            val size = (40 * resources.displayMetrics.density).toInt().coerceAtLeast(48)
            val result = infos
                .distinctBy { it.activityInfo.packageName }
                .filter { it.activityInfo.packageName != packageName }
                .map {
                    AppItem(
                        pkg = it.activityInfo.packageName,
                        label = it.loadLabel(pm).toString(),
                        icon = it.loadIcon(pm).toBitmap(size, size).asImageBitmap(),
                    )
                }
                .sortedWith(compareBy<AppItem>({ it.pkg !in chosen }, { it.label.lowercase() }))
            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread
                allApps = result
                refresh()
            }
        }.start()
    }
}

/** Renoncements sur 7 jours à partir desquels Respire a clairement servi. */
private const val REVIEW_AFTER_GIVE_UPS = 10
