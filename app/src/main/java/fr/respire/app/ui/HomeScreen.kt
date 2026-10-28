package fr.respire.app.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.widget.Toast
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.font.FontFamily
import android.content.res.Resources
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import fr.respire.app.Prefs
import fr.respire.app.R
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    val shortsBlockedToday: Int,
    val diagnostic: String,
    val days: List<DayDetail>,
    val risk: RiskUi,
    /** Version installée, affichée en bas de l'accueil pour vérifier une mise à jour. */
    val version: String = "",
)

enum class ShortKind { REELS, SHORTS }

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
) {
    var openApp by remember { mutableStateOf<AppItem?>(null) }
    var editing by remember { mutableStateOf<Rule?>(null) }
    var disabling by remember { mutableStateOf<ShortKind?>(null) }
    var showDiag by remember { mutableStateOf(false) }
    val res = LocalContext.current.resources
    val quota = ui.rules[Rule.QUOTA] ?: 0

    LazyColumn(
        Modifier
            .fillMaxSize()
            .background(Calme.Ground)
            .systemBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { Header(ui.serviceOn, onOpenAccessibility) }

        if (!ui.serviceOn) {
            item { ActivationCard(onOpenAccessibility) }
        }

        item { StatsCard(ui) }

        item {
            Column {
                SectionTitle(stringResource(R.string.section_apps))
                CalmeCard(radius = 24.dp, padding = 0.dp) {
                    if (ui.targets.isEmpty()) {
                        Text(
                            stringResource(R.string.apps_empty),
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 6.dp),
                            fontFamily = Nunito, fontSize = 15.sp, color = Calme.Muted,
                        )
                    }
                    ui.targets.forEachIndexed { index, stat ->
                        if (index > 0) HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = Calme.Divider)
                        TargetRow(stat, quota) { openApp = stat.app }
                    }
                    if (ui.targets.isNotEmpty()) {
                        Text(
                            stringResource(R.string.apps_tap_hint),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            fontFamily = Nunito, fontSize = 12.sp, color = Calme.Muted,
                        )
                    }
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = Calme.Divider)
                    Text(
                        stringResource(R.string.apps_choose),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(role = Role.Button, onClick = onPickApps)
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Calme.Sage,
                    )
                }
            }
        }

        item {
            Column {
                SectionTitle(stringResource(R.string.section_risk))
                RiskCard(ui.risk, ui.days, onSaveRisk)
            }
        }

        item {
            Column {
                SectionTitle(stringResource(R.string.section_short))
                CalmeCard(radius = 24.dp, padding = 0.dp) {
                    ShortRow(stringResource(R.string.short_reels_title), ui.blockReels) { on ->
                        if (on) onSetShortForm(ShortKind.REELS, true) else disabling = ShortKind.REELS
                    }
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = Calme.Divider)
                    ShortRow(stringResource(R.string.short_shorts_title), ui.blockShorts) { on ->
                        if (on) onSetShortForm(ShortKind.SHORTS, true) else disabling = ShortKind.SHORTS
                    }
                    Text(
                        stringResource(R.string.short_note),
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp),
                        fontFamily = Nunito, fontSize = 13.sp, color = Calme.Muted,
                    )
                    Text(
                        stringResource(R.string.diag_link),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(role = Role.Button) { showDiag = true }
                            .padding(16.dp),
                        fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Calme.Sage,
                    )
                }
            }
        }

        item {
            Column {
                SectionTitle(stringResource(R.string.section_rules))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        RuleTile(Rule.DELAY, ui.rules, res, Modifier.weight(1f)) { editing = it }
                        RuleTile(Rule.STEP, ui.rules, res, Modifier.weight(1f)) { editing = it }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        RuleTile(Rule.SESSION, ui.rules, res, Modifier.weight(1f)) { editing = it }
                        RuleTile(Rule.QUOTA, ui.rules, res, Modifier.weight(1f)) { editing = it }
                    }
                }
            }
        }
        item {
            val privacyUrl = stringResource(R.string.url_privacy)
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                FooterLink(stringResource(R.string.link_privacy)) { onOpenLink(privacyUrl) }
                FooterLink(stringResource(R.string.link_feedback)) { onOpenLink("https://github.com/Relthdrarxash/respire/issues") }
                FooterLink(stringResource(R.string.link_source)) { onOpenLink("https://github.com/Relthdrarxash/respire") }
            }
            if (ui.version.isNotEmpty()) {
                Text(
                    stringResource(R.string.app_version, ui.version),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontFamily = Nunito, fontSize = 12.sp, color = Calme.Muted,
                )
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
        AppDialog(app, appWeek(app.pkg), ui.weekLabels) { openApp = null }
    }

    if (showDiag) {
        DiagnosticDialog(ui.diagnostic) { showDiag = false }
    }

    disabling?.let { kind ->
        val name = if (kind == ShortKind.REELS) "Reels" else "Shorts"
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
private fun ShortRow(title: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .clickable(role = Role.Switch) { onChange(!checked) }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            modifier = Modifier.weight(1f),
            fontFamily = Nunito, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Calme.Ink,
        )
        Switch(
            checked = checked,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Calme.Sage,
                uncheckedThumbColor = Calme.Muted,
                uncheckedTrackColor = Calme.Chip,
                uncheckedBorderColor = Calme.Ring2,
            ),
        )
    }
}

@Composable
private fun Header(serviceOn: Boolean, onOpenAccessibility: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text("Respire", fontFamily = Fraunces, fontWeight = FontWeight.SemiBold, fontSize = 32.sp, color = Calme.Ink)
        Row(
            Modifier
                .clip(RoundedCornerShape(50))
                .background(if (serviceOn) Calme.SageTint else Calme.Chip)
                .clickable(role = Role.Button, onClick = onOpenAccessibility)
                .padding(horizontal = 12.dp, vertical = 6.dp),
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
private fun ActivationCard(onOpenAccessibility: () -> Unit) {
    CalmeCard {
        Text(stringResource(R.string.activation_title), fontFamily = Fraunces, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, color = Calme.Ink)
        Text(
            stringResource(R.string.activation_body),
            modifier = Modifier.padding(top = 8.dp),
            fontFamily = Nunito, fontSize = 15.sp, color = Calme.Muted,
        )
        Text(
            stringResource(R.string.activation_restricted),
            modifier = Modifier.padding(top = 8.dp),
            fontFamily = Nunito, fontSize = 13.sp, color = Calme.Muted,
        )
        Spacer(Modifier.height(16.dp))
        PillButton(stringResource(R.string.activation_button), onClick = onOpenAccessibility, height = 52.dp)
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun StatsCard(ui: HomeUi) {
    var openDay by remember { mutableStateOf<Int?>(null) }
    CalmeCard {
        Text(
            stringResource(R.string.stats_today),
            fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 13.sp,
            letterSpacing = 0.8.sp, color = Calme.Muted,
        )
        Row(
            Modifier.padding(top = 12.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(ui.giveUps.toString(), fontFamily = Fraunces, fontSize = 56.sp, lineHeight = 56.sp, color = Calme.Sage)
            Text(
                if (ui.pauses == 0) {
                    stringResource(R.string.stats_none)
                } else {
                    pluralStringResource(R.plurals.stats_gave_up, ui.pauses, ui.pauses)
                },
                modifier = Modifier.padding(bottom = 6.dp),
                fontFamily = Nunito, fontSize = 16.sp, lineHeight = 21.sp, color = Calme.Ink,
            )
        }

        val max = (ui.week.maxOrNull() ?: 0).coerceAtLeast(1)
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .height(44.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            ui.week.forEachIndexed { i, v ->
                val today = i == ui.week.lastIndex
                // Toute la colonne est cliquable, pas seulement la barre (plus facile au doigt).
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(role = Role.Button) { openDay = i },
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .fillMaxHeight((v.toFloat() / max).coerceAtLeast(0.08f))
                            .background(if (today) Calme.Sage else Calme.Ring2, RoundedCornerShape(6.dp))
                    )
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ui.weekLabels.forEach { d ->
                Text(
                    d, modifier = Modifier.weight(1f),
                    fontFamily = Nunito, fontSize = 12.sp, color = Calme.Muted,
                    textAlign = TextAlign.Center,
                )
            }
        }
        Text(
            stringResource(R.string.stats_week) + " · " + stringResource(R.string.stats_tap_hint),
            modifier = Modifier.padding(top = 4.dp),
            fontFamily = Nunito, fontSize = 12.sp, color = Calme.Muted,
        )
        peakHour(sumHours(ui.days))?.let { h ->
            Text(
                stringResource(R.string.week_peak, hourLabel(h)),
                modifier = Modifier.padding(top = 10.dp),
                fontFamily = Nunito, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Calme.Ink,
            )
        }
        if (ui.blockReels || ui.blockShorts) {
            Text(
                stringResource(R.string.stats_shorts, ui.shortsBlockedToday),
                modifier = Modifier.padding(top = 10.dp),
                fontFamily = Nunito, fontSize = 14.sp, color = Calme.Ink,
            )
        }
    }

    openDay?.let { i ->
        ui.days.getOrNull(i)?.let { day -> DayDialog(day) { openDay = null } }
    }
}

@Composable
private fun TargetRow(stat: TargetStat, quota: Int, onClick: () -> Unit) {
    val blocked = stat.blocked
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .heightIn(min = 64.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        AppIcon(stat.app.icon, 36)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(stat.app.label, fontFamily = Nunito, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Calme.Ink, maxLines = 1)
            if (quota > 0) {
                val frac = (stat.opens.toFloat() / quota).coerceIn(0f, 1f)
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Calme.Chip)
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(frac)
                            .fillMaxHeight()
                            .background(if (blocked) Calme.Clay else Calme.Sage)
                    )
                }
            }
        }
        Text(
            when {
                blocked -> stringResource(R.string.target_blocked)
                quota > 0 -> "${stat.opens} / $quota"
                else -> "${stat.opens}"
            },
            fontFamily = Nunito,
            fontWeight = if (blocked) FontWeight.Bold else FontWeight.Normal,
            fontSize = 14.sp,
            color = if (blocked) Calme.ClayText else Calme.Muted,
        )
    }
}

@Composable
private fun RuleTile(rule: Rule, rules: Map<Rule, Int>, res: Resources, modifier: Modifier, onClick: (Rule) -> Unit) {
    Column(
        modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Calme.Card)
            .clickable(role = Role.Button) { onClick(rule) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(stringResource(rule.title), fontFamily = Nunito, fontSize = 13.sp, color = Calme.Muted)
        Text(rule.format(res, rules[rule] ?: rule.min), fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Calme.Ink)
    }
}

@Composable
fun AppIcon(icon: ImageBitmap?, sizeDp: Int) {
    val shape = RoundedCornerShape((sizeDp * 0.3f).dp)
    if (icon != null) {
        Image(icon, contentDescription = null, modifier = Modifier.size(sizeDp.dp).clip(shape))
    } else {
        Box(Modifier.size(sizeDp.dp).clip(shape).background(Calme.Ring2))
    }
}

@Composable
private fun DiagnosticDialog(text: String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val copied = stringResource(R.string.diag_copied)
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Calme.Card,
        title = { Text(stringResource(R.string.diag_title), fontFamily = Fraunces, fontWeight = FontWeight.SemiBold, color = Calme.Ink) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(stringResource(R.string.diag_hint), fontFamily = Nunito, fontSize = 14.sp, color = Calme.Muted)
                Text(
                    text.ifBlank { stringResource(R.string.diag_empty) },
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Calme.Ground)
                        .padding(12.dp),
                    fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = Calme.Ink,
                )
            }
        },
        confirmButton = {
            TextButton(enabled = text.isNotBlank(), onClick = {
                val cm = context.getSystemService(ClipboardManager::class.java)
                cm?.setPrimaryClip(ClipData.newPlainText("Respire", text))
                Toast.makeText(context, copied, Toast.LENGTH_SHORT).show()
            }) {
                Text(stringResource(R.string.diag_copy), fontFamily = Nunito, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_close), fontFamily = Nunito, fontWeight = FontWeight.Bold, color = Calme.Muted)
            }
        },
    )
}

@Composable
private fun FooterLink(label: String, onClick: () -> Unit) {
    Text(
        label,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 12.dp),
        fontFamily = Nunito, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Calme.Muted,
    )
}
