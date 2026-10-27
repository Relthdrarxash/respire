package fr.respire.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.respire.app.Prefs
import fr.respire.app.R

/** Détail d'une appli sur les 7 derniers jours. */
@Composable
fun AppDialog(app: AppItem, week: Prefs.AppWeek, dayLabels: List<String>, onDismiss: () -> Unit) {
    val temptation = week.attempts.zip(week.shorts) { a, s -> a + s }
    val totalAttempts = week.attempts.sum()
    val totalGiveUps = week.giveUps.sum()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Calme.Card,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppIcon(app.icon, 32)
                Text(
                    app.label,
                    modifier = Modifier.padding(start = 12.dp),
                    fontFamily = Fraunces, fontWeight = FontWeight.SemiBold, color = Calme.Ink,
                )
            }
        },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                if (week.isEmpty) {
                    Text(stringResource(R.string.app_empty), fontFamily = Nunito, fontSize = 15.sp, color = Calme.Muted)
                } else {
                    Text(
                        stringResource(R.string.app_week_totals, temptation.sum(), week.opens.sum(), totalGiveUps),
                        fontFamily = Nunito, fontSize = 15.sp, color = Calme.Ink,
                    )
                    if (totalAttempts > 0) {
                        Text(
                            stringResource(R.string.app_giveup_rate, totalGiveUps * 100 / totalAttempts),
                            modifier = Modifier.padding(top = 4.dp),
                            fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Calme.Sage,
                        )
                    }
                    peakHour(week.hours)?.let { h ->
                        Text(
                            stringResource(R.string.hours_peak, hourLabel(h)),
                            modifier = Modifier.padding(top = 4.dp),
                            fontFamily = Nunito, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Calme.Ink,
                        )
                    }
                    DayBars(temptation, week.giveUps, dayLabels, Modifier.padding(top = 16.dp))
                    Row(
                        Modifier.padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        LegendDot(Calme.Ring2, stringResource(R.string.hours_attempts))
                        LegendDot(Calme.Sage, stringResource(R.string.hours_giveups))
                    }
                }
                Text(
                    stringResource(R.string.app_note),
                    modifier = Modifier.padding(top = 12.dp),
                    fontFamily = Nunito, fontSize = 12.sp, color = Calme.Muted,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_close), fontFamily = Nunito, fontWeight = FontWeight.Bold)
            }
        },
    )
}

/** 7 barres : hauteur = tentations, partie sauge = renoncements. */
@Composable
private fun DayBars(totals: List<Int>, overlay: List<Int>, labels: List<String>, modifier: Modifier = Modifier) {
    val max = (totals.maxOrNull() ?: 0).coerceAtLeast(1)
    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(80.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            totals.forEachIndexed { i, total ->
                val part = overlay.getOrElse(i) { 0 }.coerceAtMost(total)
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight((total.toFloat() / max).coerceAtLeast(0.05f))
                        .background(if (total == 0) Calme.Chip else Calme.Ring2, RoundedCornerShape(5.dp)),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    if (part > 0) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(part.toFloat() / total)
                                .background(Calme.Sage, RoundedCornerShape(5.dp))
                        )
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            labels.forEach { d ->
                Text(
                    d, modifier = Modifier.weight(1f),
                    fontFamily = Nunito, fontSize = 12.sp, color = Calme.Muted, textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun LegendDot(color: androidx.compose.ui.graphics.Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(10.dp).background(color, RoundedCornerShape(3.dp)))
        Text(label, fontFamily = Nunito, fontSize = 13.sp, color = Calme.Ink)
    }
}
