package fr.respire.app.ui

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class AppItem(val pkg: String, val label: String, val icon: ImageBitmap?)

data class TargetStat(val app: AppItem, val opens: Int)

data class HomeUi(
    val serviceOn: Boolean,
    val giveUps: Int,
    val pauses: Int,
    val week: List<Int>,
    val weekLabels: List<String>,
    val targets: List<TargetStat>,
    val rules: Map<Rule, Int>,
)

@Composable
fun HomeScreen(
    ui: HomeUi,
    onOpenAccessibility: () -> Unit,
    onPickApps: () -> Unit,
    onSaveRule: (Rule, Int) -> Unit,
) {
    var editing by remember { mutableStateOf<Rule?>(null) }
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
                SectionTitle("Applis freinées")
                CalmeCard(radius = 24.dp, padding = 0.dp) {
                    if (ui.targets.isEmpty()) {
                        Text(
                            "Aucune appli pour l’instant. Commence par celles que tu ouvres sans y penser.",
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 6.dp),
                            fontFamily = Nunito, fontSize = 15.sp, color = Calme.Muted,
                        )
                    }
                    ui.targets.forEachIndexed { index, stat ->
                        if (index > 0) HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = Calme.Divider)
                        TargetRow(stat, quota)
                    }
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = Calme.Divider)
                    Text(
                        "Choisir les applis",
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
                SectionTitle("Règles")
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        RuleTile(Rule.DELAY, ui.rules, Modifier.weight(1f)) { editing = it }
                        RuleTile(Rule.STEP, ui.rules, Modifier.weight(1f)) { editing = it }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        RuleTile(Rule.SESSION, ui.rules, Modifier.weight(1f)) { editing = it }
                        RuleTile(Rule.QUOTA, ui.rules, Modifier.weight(1f)) { editing = it }
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
                if (serviceOn) "Actif" else "Inactif",
                fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 13.sp,
                color = if (serviceOn) Calme.SageDark else Calme.ClayText,
            )
        }
    }
}

@Composable
private fun ActivationCard(onOpenAccessibility: () -> Unit) {
    CalmeCard {
        Text("Une étape avant de commencer", fontFamily = Fraunces, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, color = Calme.Ink)
        Text(
            "Active « Respire » dans les paramètres d’accessibilité pour que les pauses s’affichent. L’appli ne lit pas le contenu de ton écran et n’a pas accès à Internet.",
            modifier = Modifier.padding(top = 8.dp),
            fontFamily = Nunito, fontSize = 15.sp, color = Calme.Muted,
        )
        Text(
            "Option grisée ? Paramètres > Applications > Respire > menu ⋮ > Autoriser les paramètres restreints.",
            modifier = Modifier.padding(top = 8.dp),
            fontFamily = Nunito, fontSize = 13.sp, color = Calme.Muted,
        )
        Spacer(Modifier.height(16.dp))
        PillButton("Ouvrir les paramètres", onClick = onOpenAccessibility, height = 52.dp)
    }
}

@Composable
private fun StatsCard(ui: HomeUi) {
    CalmeCard {
        Text(
            "AUJOURD’HUI",
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
                when {
                    ui.pauses == 0 -> "aucune pause\npour l’instant"
                    else -> "fois où tu as renoncé\nsur ${ui.pauses} pauses"
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
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight((v.toFloat() / max).coerceAtLeast(0.08f))
                        .background(if (today) Calme.Sage else Calme.Ring2, RoundedCornerShape(6.dp))
                )
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
            "Renoncements sur 7 jours",
            modifier = Modifier.padding(top = 4.dp),
            fontFamily = Nunito, fontSize = 12.sp, color = Calme.Muted,
        )
    }
}

@Composable
private fun TargetRow(stat: TargetStat, quota: Int) {
    val blocked = quota > 0 && stat.opens >= quota
    Row(
        Modifier
            .fillMaxWidth()
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
                blocked -> "Bloqué"
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
private fun RuleTile(rule: Rule, rules: Map<Rule, Int>, modifier: Modifier, onClick: (Rule) -> Unit) {
    Column(
        modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Calme.Card)
            .clickable(role = Role.Button) { onClick(rule) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(rule.title, fontFamily = Nunito, fontSize = 13.sp, color = Calme.Muted)
        Text(rule.format(rules[rule] ?: rule.min), fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Calme.Ink)
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
