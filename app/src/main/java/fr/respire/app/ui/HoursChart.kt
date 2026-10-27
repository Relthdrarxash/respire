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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import fr.respire.app.R
import java.text.DateFormat
import java.util.Calendar

/** Une journée, heure par heure (24 valeurs par liste). */
data class DayDetail(
    val title: String,
    val attempts: List<Int>,
    val giveUps: List<Int>,
    val shorts: List<Int>,
) {
    /** Tentation = pause affichée ou vidéo courte écartée. */
    val temptation: List<Int> get() = attempts.zip(shorts) { a, s -> a + s }
}

/** Heure lisible dans la langue du téléphone : « 21:00 » ou « 9:00 PM ». */
fun hourLabel(hour: Int): String {
    val c = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, 0)
    }
    return DateFormat.getTimeInstance(DateFormat.SHORT).format(c.time)
}

/** Heure du pic, ou null si rien n'a été enregistré. */
fun peakHour(values: List<Int>): Int? {
    val max = values.maxOrNull() ?: 0
    return if (max == 0) null else values.indexOf(max)
}

/** Somme heure par heure sur plusieurs jours. */
fun sumHours(days: List<DayDetail>): List<Int> =
    (0 until 24).map { h -> days.sumOf { it.temptation[h] } }

@Composable
fun DayDialog(day: DayDetail, onDismiss: () -> Unit) {
    val temptation = day.temptation
    val peak = peakHour(temptation)
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Calme.Card,
        title = { Text(day.title, fontFamily = Fraunces, fontWeight = FontWeight.SemiBold, color = Calme.Ink) },
        text = {
            Column {
                if (peak == null) {
                    Text(stringResource(R.string.hours_empty), fontFamily = Nunito, fontSize = 15.sp, color = Calme.Muted)
                } else {
                    Text(
                        stringResource(R.string.hours_peak, hourLabel(peak)),
                        fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Calme.Ink,
                    )
                    HoursBars(temptation, day.giveUps, Modifier.padding(top = 16.dp))
                    Legend(Modifier.padding(top = 12.dp))
                    Text(
                        stringResource(
                            R.string.hours_totals,
                            day.attempts.sum(),
                            day.giveUps.sum(),
                            day.shorts.sum(),
                        ),
                        modifier = Modifier.padding(top = 12.dp),
                        fontFamily = Nunito, fontSize = 14.sp, color = Calme.Muted,
                    )
                }
                Text(
                    stringResource(R.string.hours_note),
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

/**
 * 24 barres : la hauteur totale = tentations, la partie sauge = renoncements.
 * Repères sous l'axe à 0 h, 6 h, 12 h et 18 h.
 */
@Composable
private fun HoursBars(temptation: List<Int>, giveUps: List<Int>, modifier: Modifier = Modifier) {
    val max = (temptation.maxOrNull() ?: 0).coerceAtLeast(1)
    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(96.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            for (h in 0 until 24) {
                val total = temptation[h]
                val gave = giveUps[h].coerceAtMost(total)
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight((total.toFloat() / max).coerceAtLeast(0.03f))
                        .background(if (total == 0) Calme.Chip else Calme.Ring2, RoundedCornerShape(3.dp)),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    if (gave > 0) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(gave.toFloat() / total)
                                .background(Calme.Sage, RoundedCornerShape(3.dp))
                        )
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 4.dp)) {
            for (h in listOf(0, 6, 12, 18)) {
                Text(
                    hourLabel(h),
                    modifier = Modifier.weight(1f),
                    fontFamily = Nunito, fontSize = 11.sp, color = Calme.Muted,
                    textAlign = TextAlign.Start,
                )
            }
        }
    }
}

@Composable
private fun Legend(modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        LegendItem(Calme.Ring2, stringResource(R.string.hours_attempts))
        LegendItem(Calme.Sage, stringResource(R.string.hours_giveups))
    }
}

@Composable
private fun LegendItem(color: androidx.compose.ui.graphics.Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(10.dp).background(color, RoundedCornerShape(3.dp)))
        Text(label, fontFamily = Nunito, fontSize = 13.sp, color = Calme.Ink)
    }
}
