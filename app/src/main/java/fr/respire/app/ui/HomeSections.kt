package fr.respire.app.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.res.Resources
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.respire.app.R

/** Section « Tes applis » : liste des applications suivies, jauge du quota quotidien et bouton d'ajout. */
@Composable
internal fun AppsSection(
    targets: List<TargetStat>,
    quota: Int,
    onOpenApp: (AppItem) -> Unit,
    onPickApps: () -> Unit,
) {
    Column {
        SectionTitle(stringResource(R.string.section_apps))
        CalmeCard(radius = 24.dp, padding = 0.dp) {
            if (targets.isEmpty()) {
                Text(
                    stringResource(R.string.apps_empty),
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 6.dp),
                    fontFamily = Nunito, fontSize = 15.sp, color = Calme.Muted,
                )
            }
            targets.forEachIndexed { index, stat ->
                if (index > 0) HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = Calme.Divider)
                TargetRow(stat, quota) { onOpenApp(stat.app) }
            }
            if (targets.isNotEmpty()) {
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

/** Section « Vidéos courtes » : interrupteurs Reels / Shorts / Facebook et lien vers le diagnostic. */
@Composable
internal fun ShortFormSection(
    ui: HomeUi,
    onEnable: (ShortKind) -> Unit,
    onRequestDisable: (ShortKind) -> Unit,
    onShowDiag: () -> Unit,
) {
    Column {
        SectionTitle(stringResource(R.string.section_short))
        CalmeCard(radius = 24.dp, padding = 0.dp) {
            ShortRow(stringResource(R.string.short_reels_title), ui.blockReels) { on ->
                if (on) onEnable(ShortKind.REELS) else onRequestDisable(ShortKind.REELS)
            }
            HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = Calme.Divider)
            ShortRow(stringResource(R.string.short_shorts_title), ui.blockShorts) { on ->
                if (on) onEnable(ShortKind.SHORTS) else onRequestDisable(ShortKind.SHORTS)
            }
            if (ui.facebookInstalled || ui.blockFacebookReels) {
                HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = Calme.Divider)
                ShortRow(stringResource(R.string.short_fb_title), ui.blockFacebookReels) { on ->
                    if (on) onEnable(ShortKind.FACEBOOK) else onRequestDisable(ShortKind.FACEBOOK)
                }
                if (ui.blockFacebookReels) {
                    Text(
                        stringResource(R.string.short_fb_note),
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                        fontFamily = Nunito, fontSize = 13.sp, color = Calme.Muted,
                    )
                }
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
                    .clickable(role = Role.Button, onClick = onShowDiag)
                    .padding(16.dp),
                fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Calme.Sage,
            )
        }
    }
}

/** Grille des 4 tuiles de règles (durée de pause, allongement, session, quota quotidien). */
@Composable
internal fun RulesSection(
    rules: Map<Rule, Int>,
    res: Resources,
    onEditRule: (Rule) -> Unit,
    onOpenWhy: () -> Unit,
) {
    Column {
        SectionTitle(stringResource(R.string.section_rules))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                RuleTile(Rule.DELAY, rules, res, Modifier.weight(1f), onEditRule)
                RuleTile(Rule.STEP, rules, res, Modifier.weight(1f), onEditRule)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                RuleTile(Rule.SESSION, rules, res, Modifier.weight(1f), onEditRule)
                RuleTile(Rule.QUOTA, rules, res, Modifier.weight(1f), onEditRule)
            }
        }
        FooterLink(stringResource(R.string.link_why), onOpenWhy)
    }
}

@Composable
internal fun ShortRow(title: String, checked: Boolean, onChange: (Boolean) -> Unit) {
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
internal fun TargetRow(stat: TargetStat, quota: Int, onClick: () -> Unit) {
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
internal fun DiagnosticDialog(text: String, onDismiss: () -> Unit) {
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
internal fun FooterLink(label: String, onClick: () -> Unit) {
    Text(
        label,
        modifier = Modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 14.dp),
        fontFamily = Nunito, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Calme.Muted,
    )
}

/** Section « Dans la même démarche » : mise en valeur des applications soeurs libres et hors-ligne. */
@Composable
internal fun SuiteSection(onOpenLink: (String) -> Unit) {
    Column {
        SectionTitle(stringResource(R.string.suite_title))
        CalmeCard(radius = 24.dp, padding = 16.dp) {
            Text(
                stringResource(R.string.suite_subtitle),
                fontFamily = Nunito,
                fontSize = 14.sp,
                color = Calme.Muted,
            )
            Spacer(Modifier.height(10.dp))
            CompanionAppRow(
                badge = "L",
                name = stringResource(R.string.app_lunaison_name),
                description = stringResource(R.string.app_lunaison_desc),
                onClick = { onOpenLink("https://relthdrarxash.github.io/lunaison/") },
            )
            HorizontalDivider(Modifier.padding(vertical = 8.dp), color = Calme.Divider)
            CompanionAppRow(
                badge = "S",
                name = stringResource(R.string.app_sommelle_name),
                description = stringResource(R.string.app_sommelle_desc),
                onClick = { onOpenLink("https://relthdrarxash.github.io/sommelle/") },
            )
        }
    }
}

@Composable
private fun CompanionAppRow(
    badge: String,
    name: String,
    description: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Calme.Chip),
            contentAlignment = Alignment.Center,
        ) {
            Text(badge, fontSize = 18.sp)
        }
        Column(Modifier.weight(1f)) {
            Text(name, fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Calme.Ink)
            Text(description, fontFamily = Nunito, fontSize = 13.sp, color = Calme.Muted)
        }
    }
}

/** Section « À propos & Démarche » avec version, engagement éthique et liens utiles. */
@Composable
internal fun AboutSection(
    version: String,
    onOpenPrivacy: () -> Unit,
    onOpenFeedback: () -> Unit,
    onOpenSource: () -> Unit,
    onShowDiag: () -> Unit,
    onReplayOnboarding: () -> Unit,
    onShowDisclosure: () -> Unit,
) {
    Column {
        SectionTitle(stringResource(R.string.section_about))
        CalmeCard(radius = 24.dp, padding = 16.dp) {
            if (version.isNotEmpty()) {
                Text(
                    stringResource(R.string.app_version, version),
                    fontFamily = Fraunces,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = Calme.Ink,
                )
            }
            Text(
                stringResource(R.string.suite_commitment),
                modifier = Modifier.padding(top = 4.dp),
                fontFamily = Nunito,
                fontSize = 13.sp,
                color = Calme.Muted,
            )
            HorizontalDivider(Modifier.padding(vertical = 12.dp), color = Calme.Divider)
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                FooterLink(stringResource(R.string.link_privacy), onClick = onOpenPrivacy)
                FooterLink(stringResource(R.string.link_source), onClick = onOpenSource)
                FooterLink(stringResource(R.string.link_feedback), onClick = onOpenFeedback)
            }
            TextAction(stringResource(R.string.settings_replay_onboarding), onClick = onReplayOnboarding)
            TextAction(stringResource(R.string.settings_review_disclosure), onClick = onShowDisclosure)
            val uriHandler = LocalUriHandler.current
            Text(
                "pilmil.studio@proton.me",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(role = Role.Button) {
                        runCatching { uriHandler.openUri("mailto:pilmil.studio@proton.me?subject=Respire") }
                    }
                    .padding(top = 6.dp),
                fontFamily = Nunito,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = Calme.Sage,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            Text(
                stringResource(R.string.diag_title),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clickable(role = Role.Button, onClick = onShowDiag)
                    .padding(top = 8.dp),
                fontFamily = Nunito,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = Calme.Sage,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

/**
 * Checklist de démarrage, tant que tout n'est pas prêt : d'abord les applis (avec des suggestions
 * ajoutées en un geste), puis le service d'accessibilité, puis, en option, les notifications.
 * Sans applis choisies, activer le service ne servirait à rien : l'ordre est voulu.
 */
@Composable
internal fun StartChecklist(
    hasApps: Boolean,
    serviceOn: Boolean,
    canNotify: Boolean,
    suggestions: List<AppItem>,
    onAddApp: (String) -> Unit,
    onPickApps: () -> Unit,
    onOpenAccessibility: () -> Unit,
    onAllowNotifications: () -> Unit,
    onOpenAppInfo: () -> Unit,
) {
    CalmeCard {
        Text(stringResource(R.string.start_title), fontFamily = Fraunces, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, color = Calme.Ink)
        Spacer(Modifier.height(12.dp))

        StepLine(stringResource(R.string.start_step_apps), done = hasApps)
        if (!hasApps) {
            if (suggestions.isNotEmpty()) {
                Text(
                    stringResource(R.string.start_suggest),
                    modifier = Modifier.padding(top = 6.dp, bottom = 4.dp),
                    fontFamily = Nunito, fontSize = 13.sp, color = Calme.Muted,
                )
                suggestions.take(4).forEach { app ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(role = Role.Button) { onAddApp(app.pkg) }
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AppIcon(app.icon, 28)
                        Spacer(Modifier.size(10.dp))
                        Text(app.label, modifier = Modifier.weight(1f), fontFamily = Nunito, fontSize = 15.sp, color = Calme.Ink)
                        Text("+", fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Calme.Sage)
                    }
                }
            }
            Text(
                stringResource(R.string.apps_choose),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clickable(role = Role.Button, onClick = onPickApps)
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Calme.Sage,
            )
        }

        Spacer(Modifier.height(10.dp))
        StepLine(stringResource(R.string.start_step_service), done = serviceOn)
        if (!serviceOn) {
            Text(
                stringResource(R.string.activation_restricted),
                modifier = Modifier.padding(top = 4.dp),
                fontFamily = Nunito, fontSize = 13.sp, color = Calme.Muted,
            )
            TextAction(stringResource(R.string.onb_act_appinfo), onClick = onOpenAppInfo, fill = true, center = false)
            Spacer(Modifier.height(4.dp))
            PillButton(stringResource(R.string.activation_button), onClick = onOpenAccessibility, enabled = hasApps, height = 52.dp)
        }

        if (!canNotify) {
            Spacer(Modifier.height(14.dp))
            StepLine(stringResource(R.string.start_step_notif), done = false)
            Text(
                stringResource(R.string.start_notif_button),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clickable(role = Role.Button, onClick = onAllowNotifications)
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Calme.Sage,
            )
        }
    }
}

@Composable
private fun StepLine(text: String, done: Boolean) {
    val doneLabel = stringResource(R.string.start_done)
    Row(
        Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { if (done) stateDescription = doneLabel },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(50))
                .background(if (done) Calme.Sage else Calme.Chip),
            contentAlignment = Alignment.Center,
        ) {
            if (done) Text("✓", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Spacer(Modifier.size(10.dp))
        Text(
            text,
            fontFamily = Nunito,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            color = if (done) Calme.Muted else Calme.Ink,
        )
    }
}

