package fr.respire.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.respire.app.Prefs
import fr.respire.app.R

data class AppItem(val pkg: String, val label: String, val icon: ImageBitmap?)

data class TargetStat(val app: AppItem, val opens: Int, val blocked: Boolean = false)

data class HomeUi(
    val serviceOn: Boolean,
    val giveUps: Int,
    val pauses: Int,
    val week: List<Int>,
    val weekLabels: List<String>,
    val targets: List<TargetStat>,
    val rules: Map<Rule, Int>,
    val blockReels: Boolean,
    val blockShorts: Boolean,
    /** Filtre Reels de Facebook (en test) : proposé seulement si Facebook est installé. */
    val blockFacebookReels: Boolean = false,
    val facebookInstalled: Boolean = false,
    val shortsBlockedToday: Int,
    val diagnostic: String,
    val days: List<DayDetail>,
    val risk: RiskUi,
    /** Version installée, affichée en bas de l'accueil pour vérifier une mise à jour. */
    val version: String = "",
    /** Applis à flux sans fin installées, proposées dans la checklist de démarrage. */
    val suggestions: List<AppItem> = emptyList(),
    val canNotify: Boolean = true,
    /** Une fois, après une semaine où les pauses ont vraiment servi. */
    val showReviewPrompt: Boolean = false,
)

enum class ShortKind { REELS, SHORTS, FACEBOOK }

enum class RespireTab { APPS, STATS, SETTINGS }

/**
 * Écran d'accueil de Respire avec navigation fluide par onglets (Applis, Statistiques, Configuration).
 * Les sous-sections sont découpées dans [StatsCard] et [HomeSections] pour garder chaque fichier court et lisible.
 */
@Composable
fun HomeScreen(
    ui: HomeUi,
    onOpenAccessibility: () -> Unit,
    onOpenLink: (String) -> Unit,
    onPickApps: () -> Unit,
    onSaveRule: (Rule, Int) -> Unit,
    onSetShortForm: (ShortKind, Boolean) -> Unit,
    onSaveRisk: (Boolean, Int, Int, Int) -> Unit,
    appWeek: (String) -> Prefs.AppWeek,
    onAddApp: (String) -> Unit = {},
    onAllowNotifications: () -> Unit = {},
    onReview: () -> Unit = {},
    onDismissReview: () -> Unit = {},
) {
    var currentTab by rememberSaveable { mutableStateOf(RespireTab.APPS) }
    var openApp by remember { mutableStateOf<AppItem?>(null) }
    var editing by remember { mutableStateOf<Rule?>(null) }
    var disabling by remember { mutableStateOf<ShortKind?>(null) }
    var showDiag by remember { mutableStateOf(false) }
    val res = LocalContext.current.resources
    val quota = ui.rules[Rule.QUOTA] ?: 0
    val whyUrl = stringResource(R.string.url_why)

    Scaffold(
        containerColor = Calme.Ground,
        bottomBar = {
            RespireBottomBar(
                selected = currentTab,
                onSelect = { currentTab = it },
            )
        },
    ) { padding ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .background(Calme.Ground)
                .padding(padding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            when (currentTab) {
                RespireTab.APPS -> {
                    item { Header("Respire", ui.serviceOn, onOpenAccessibility) }

                    if (!ui.serviceOn || ui.targets.isEmpty()) {
                        item {
                            StartChecklist(
                                hasApps = ui.targets.isNotEmpty(),
                                serviceOn = ui.serviceOn,
                                canNotify = ui.canNotify,
                                suggestions = ui.suggestions,
                                onAddApp = onAddApp,
                                onPickApps = onPickApps,
                                onOpenAccessibility = onOpenAccessibility,
                                onAllowNotifications = onAllowNotifications,
                            )
                        }
                    }

                    item {
                        AppsSection(
                            targets = ui.targets,
                            quota = quota,
                            onOpenApp = { openApp = it },
                            onPickApps = onPickApps,
                        )
                    }

                    item {
                        ShortFormSection(
                            ui = ui,
                            onEnable = { onSetShortForm(it, true) },
                            onRequestDisable = { disabling = it },
                            onShowDiag = { showDiag = true },
                        )
                    }
                }
                RespireTab.STATS -> {
                    item { Header(stringResource(R.string.nav_stats), ui.serviceOn, onOpenAccessibility) }

                    item { StatsCard(ui) }

                    if (ui.showReviewPrompt) {
                        item { ReviewCard(onReview = onReview, onDismiss = onDismissReview) }
                    }

                    if (ui.targets.isNotEmpty()) {
                        item {
                            SectionTitle(stringResource(R.string.section_apps))
                            CalmeCard(radius = 24.dp, padding = 0.dp) {
                                ui.targets.forEachIndexed { index, stat ->
                                    if (index > 0) HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = Calme.Divider)
                                    TargetRow(stat, quota) { openApp = stat.app }
                                }
                                Text(
                                    stringResource(R.string.apps_tap_hint),
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                    fontFamily = Nunito, fontSize = 12.sp, color = Calme.Muted,
                                )
                            }
                        }
                    }
                }
                RespireTab.SETTINGS -> {
                    item { Header(stringResource(R.string.nav_settings), ui.serviceOn, onOpenAccessibility) }

                    item {
                        Column {
                            SectionTitle(stringResource(R.string.section_risk))
                            RiskCard(ui.risk, ui.days, onSaveRisk)
                        }
                    }

                    item {
                        RulesSection(
                            rules = ui.rules,
                            res = res,
                            onEditRule = { editing = it },
                            onOpenWhy = { onOpenLink(whyUrl) },
                        )
                    }

                    item {
                        SuiteSection(onOpenLink = onOpenLink)
                    }

                    item {
                        val privacyUrl = stringResource(R.string.url_privacy)
                        AboutSection(
                            version = ui.version,
                            onOpenPrivacy = { onOpenLink(privacyUrl) },
                            onOpenFeedback = { onOpenLink("https://github.com/Relthdrarxash/respire/issues") },
                            onOpenSource = { onOpenLink("https://github.com/Relthdrarxash/respire") },
                            onShowDiag = { showDiag = true },
                        )
                    }
                }
            }
        }
    }

    editing?.let { rule ->
        RuleDialog(
            rule = rule,
            current = ui.rules[rule] ?: rule.min,
            onDismiss = { editing = null },
            onSave = { value ->
                onSaveRule(rule, value)
                editing = null
            },
        )
    }

    openApp?.let { app ->
        AppDialog(app, appWeek(app.pkg), ui.weekLabels.takeLast(7)) { openApp = null }
    }

    if (showDiag) {
        DiagnosticDialog(ui.diagnostic) { showDiag = false }
    }

    disabling?.let { kind ->
        val name = when (kind) {
            ShortKind.REELS -> "Reels"
            ShortKind.SHORTS -> "Shorts"
            ShortKind.FACEBOOK -> "Reels (Facebook)"
        }
        LoosenDialog(
            title = stringResource(R.string.loosen_short_title),
            what = stringResource(R.string.loosen_short_what, name),
            confirmLabel = stringResource(R.string.loosen_short_confirm),
            onDismiss = { disabling = null },
            onConfirm = {
                onSetShortForm(kind, false)
                disabling = null
            },
        )
    }
}

@Composable
private fun RespireBottomBar(selected: RespireTab, onSelect: (RespireTab) -> Unit) {
    val navLabel = stringResource(R.string.nav_label)
    NavigationBar(
        containerColor = Calme.Card,
        contentColor = Calme.Ink,
        modifier = Modifier.semantics { contentDescription = navLabel },
    ) {
        val colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Calme.Ink,
            selectedTextColor = Calme.Ink,
            indicatorColor = Calme.SageTint,
            unselectedIconColor = Calme.Muted,
            unselectedTextColor = Calme.Muted,
        )
        NavigationBarItem(
            selected = selected == RespireTab.APPS,
            onClick = { onSelect(RespireTab.APPS) },
            icon = { Icon(RespireIcons.Apps, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_apps), fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
            alwaysShowLabel = true,
            colors = colors,
        )
        NavigationBarItem(
            selected = selected == RespireTab.STATS,
            onClick = { onSelect(RespireTab.STATS) },
            icon = { Icon(RespireIcons.Chart, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_stats), fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
            alwaysShowLabel = true,
            colors = colors,
        )
        NavigationBarItem(
            selected = selected == RespireTab.SETTINGS,
            onClick = { onSelect(RespireTab.SETTINGS) },
            icon = { Icon(RespireIcons.Settings, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_settings), fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
            alwaysShowLabel = true,
            colors = colors,
        )
    }
}

@Composable
private fun Header(title: String, serviceOn: Boolean, onOpenAccessibility: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            title,
            modifier = Modifier.semantics { heading() },
            fontFamily = Fraunces,
            fontWeight = FontWeight.SemiBold,
            fontSize = 32.sp,
            color = Calme.Ink,
        )
        Row(
            Modifier
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(50))
                .background(if (serviceOn) Calme.SageTint else Calme.Chip)
                .clickable(role = Role.Button, onClick = onOpenAccessibility)
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(Modifier.size(8.dp).background(if (serviceOn) Calme.Sage else Calme.Clay, CircleShape))
            Text(
                stringResource(if (serviceOn) R.string.home_active else R.string.home_inactive),
                fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 13.sp,
                color = if (serviceOn) Calme.SageDark else Calme.ClayText,
            )
        }
    }
}

@Composable
private fun ReviewCard(onReview: () -> Unit, onDismiss: () -> Unit) {
    CalmeCard {
        Text(stringResource(R.string.review_title), fontFamily = Fraunces, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = Calme.Ink)
        Text(
            stringResource(R.string.review_text),
            modifier = Modifier.padding(top = 6.dp, bottom = 12.dp),
            fontFamily = Nunito, fontSize = 14.sp, color = Calme.Muted,
        )
        PillButton(stringResource(R.string.review_yes), onClick = onReview, height = 48.dp)
        Text(
            stringResource(R.string.review_no),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clickable(role = Role.Button, onClick = onDismiss)
                .padding(vertical = 14.dp),
            fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Calme.Sage,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}
