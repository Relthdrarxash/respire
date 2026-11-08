package fr.respire.app.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.respire.app.R

/** Carte « Aujourd'hui / Ta semaine » : renoncements du jour, barres des derniers jours et heure de pic avec swipe semaine par semaine. */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun StatsCard(ui: HomeUi) {
    var openDay by remember { mutableStateOf<Int?>(null) }
    var weekOffset by remember { mutableIntStateOf(0) }
    var totalDrag by remember { mutableFloatStateOf(0f) }
    val totalWeeks = (ui.week.size / 7).coerceAtLeast(1)

    val currentWeek = remember(ui.week, weekOffset) {
        val end = ui.week.size - weekOffset * 7
        val start = (end - 7).coerceAtLeast(0)
        if (start < end) ui.week.subList(start, end) else ui.week.takeLast(7)
    }
    val currentDays = remember(ui.days, weekOffset) {
        val end = ui.days.size - weekOffset * 7
        val start = (end - 7).coerceAtLeast(0)
        if (start < end) ui.days.subList(start, end) else ui.days.takeLast(7)
    }
    val currentLabels = remember(ui.weekLabels, weekOffset) {
        val end = ui.weekLabels.size - weekOffset * 7
        val start = (end - 7).coerceAtLeast(0)
        if (start < end) ui.weekLabels.subList(start, end) else ui.weekLabels.takeLast(7)
    }

    val context = LocalContext.current
    val sessionMin = (ui.rules[Rule.SESSION] ?: 10).coerceAtLeast(1)
    val savedTodayMin = ui.giveUps * sessionMin
    val savedWeekMin = currentWeek.sum() * sessionMin

    CalmeCard(
        modifier = Modifier.pointerInput(weekOffset, totalWeeks) {
            detectHorizontalDragGestures(
                onDragStart = { totalDrag = 0f },
                onDragEnd = {
                    if (totalDrag < -50f && weekOffset > 0) {
                        weekOffset--
                    } else if (totalDrag > 50f && weekOffset < totalWeeks - 1) {
                        weekOffset++
                    }
                },
                onHorizontalDrag = { change, dragAmount ->
                    change.consume()
                    totalDrag += dragAmount
                }
            )
        }
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                if (weekOffset == 0) stringResource(R.string.stats_today)
                else "IL Y A $weekOffset SEMAINE${if (weekOffset > 1) "S" else ""}",
                fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 13.sp,
                letterSpacing = 0.8.sp, color = Calme.Muted,
            )
            if (weekOffset > 0) {
                Text(
                    "Aujourd'hui",
                    fontFamily = Nunito, fontWeight = FontWeight.SemiBold, fontSize = 12.sp,
                    color = Calme.Sage,
                    modifier = Modifier.clickable { weekOffset = 0 }
                )
            }
        }
        Row(
            Modifier.padding(top = 12.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                if (weekOffset == 0) ui.giveUps.toString() else currentWeek.sum().toString(),
                fontFamily = Fraunces, fontSize = 56.sp, lineHeight = 56.sp, color = Calme.Sage
            )
            Text(
                if (weekOffset == 0) {
                    if (ui.pauses == 0) {
                        stringResource(R.string.stats_none)
                    } else {
                        pluralStringResource(R.plurals.stats_gave_up, ui.pauses, ui.pauses)
                    }
                } else {
                    pluralStringResource(R.plurals.stats_week_giveups, currentWeek.sum(), currentWeek.sum())
                },
                modifier = Modifier.padding(bottom = 6.dp),
                fontFamily = Nunito, fontSize = 16.sp, lineHeight = 21.sp, color = Calme.Ink,
            )
        }
        if (savedWeekMin > 0) {
            Text(
                if (weekOffset == 0) stringResource(R.string.stats_time_saved, savedTodayMin, savedWeekMin)
                else stringResource(R.string.stats_week_saved, savedWeekMin),
                modifier = Modifier.padding(top = 6.dp),
                fontFamily = Nunito, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Calme.SageDark,
            )
        }

        val max = (currentWeek.maxOrNull() ?: 0).coerceAtLeast(1)
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .height(44.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            currentWeek.forEachIndexed { i, v ->
                val today = weekOffset == 0 && i == currentWeek.lastIndex
                val dayLabel = currentDays.getOrNull(i)?.title ?: currentLabels.getOrElse(i) { "" }
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .semantics { contentDescription = "$dayLabel : $v" }
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
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
                .clearAndSetSemantics { },
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            currentLabels.forEach { d ->
                Text(
                    d, modifier = Modifier.weight(1f),
                    fontFamily = Nunito, fontSize = 12.sp, color = Calme.Muted,
                    textAlign = TextAlign.Center,
                )
            }
        }
        Text(
            (if (weekOffset == 0) stringResource(R.string.stats_week) else stringResource(R.string.hours_giveups)) + " · " + stringResource(R.string.stats_tap_hint),
            modifier = Modifier.padding(top = 4.dp),
            fontFamily = Nunito, fontSize = 12.sp, color = Calme.Muted,
        )
        peakHour(sumHours(currentDays))?.let { h ->
            Text(
                stringResource(R.string.week_peak, hourLabel(h)),
                modifier = Modifier.padding(top = 10.dp),
                fontFamily = Nunito, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Calme.Ink,
            )
        }
        if (ui.blockReels || ui.blockShorts || ui.blockFacebookReels) {
            Text(
                stringResource(R.string.stats_shorts, ui.shortsBlockedToday),
                modifier = Modifier.padding(top = 10.dp),
                fontFamily = Nunito, fontSize = 14.sp, color = Calme.Ink,
            )
        }
        val shareLabel = stringResource(R.string.stats_share)
        val shareText = stringResource(R.string.stats_time_saved, savedTodayMin, savedWeekMin) +
            "\n" + currentDays.joinToString("\n") { d ->
                "${d.title} : ${d.giveUps.sum()} / ${d.attempts.sum()}"
            }
        Text(
            shareLabel,
            modifier = Modifier
                .padding(top = 8.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable(role = Role.Button) {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    context.startActivity(Intent.createChooser(intent, shareLabel))
                }
                .padding(vertical = 6.dp),
            fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Calme.Sage,
        )
    }

    openDay?.let { i ->
        currentDays.getOrNull(i)?.let { day -> DayDialog(day) { openDay = null } }
    }
}
