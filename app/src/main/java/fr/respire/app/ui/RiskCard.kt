package fr.respire.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.respire.app.Rules
import fr.respire.app.R
import kotlin.math.roundToInt

data class RiskUi(
    val enabled: Boolean,
    val start: Int,
    val end: Int,
    val quota: Int,
    /** La plage est en cours en ce moment. */
    val active: Boolean,
)

/**
 * Plage de 3 heures où la tentation a été la plus forte sur la semaine,
 * ou null s'il n'y a pas encore de données.
 */
fun suggestRiskWindow(days: List<DayDetail>): Pair<Int, Int>? {
    val hours = sumHours(days)
    if (hours.sum() == 0) return null
    val best = (0 until 24).maxByOrNull { h -> (0 until 3).sumOf { hours[(h + it) % 24] } } ?: return null
    return best to (best + 3) % 24
}

@Composable
fun RiskCard(
    risk: RiskUi,
    days: List<DayDetail>,
    onSave: (enabled: Boolean, start: Int, end: Int, quota: Int) -> Unit,
) {
    var editing by remember { mutableStateOf(false) }
    var disabling by remember { mutableStateOf(false) }

    CalmeCard(radius = 24.dp, padding = 0.dp) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(role = Role.Switch) {
                    if (risk.enabled) disabling = true else onSave(true, risk.start, risk.end, risk.quota)
                }
                .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    stringResource(R.string.risk_title),
                    fontFamily = Nunito, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Calme.Ink,
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        stringResource(R.string.risk_range, hourLabel(risk.start), hourLabel(risk.end)),
                        fontFamily = Fraunces, fontSize = 20.sp,
                        color = if (risk.enabled) Calme.Sage else Calme.Muted,
                    )
                    if (risk.enabled && risk.active) {
                        Text(
                            stringResource(R.string.risk_now),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Calme.SageTint)
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Calme.SageDark,
                        )
                    }
                }
            }
            Switch(
                checked = risk.enabled,
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
        Text(
            riskDescription(risk.quota),
            modifier = Modifier.padding(horizontal = 16.dp),
            fontFamily = Nunito, fontSize = 13.sp, color = Calme.Muted,
        )
        Text(
            stringResource(R.string.risk_edit),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(role = Role.Button) { editing = true }
                .padding(16.dp),
            fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Calme.Sage,
        )
    }

    if (editing) {
        RiskDialog(
            risk = risk,
            suggestion = suggestRiskWindow(days),
            onDismiss = { editing = false },
            onSave = { start, end, quota ->
                onSave(true, start, end, quota)
                editing = false
            },
        )
    }

    if (disabling) {
        LoosenDialog(
            title = stringResource(R.string.risk_off_title),
            what = stringResource(R.string.loosen_risk),
            confirmLabel = stringResource(R.string.loosen_short_confirm),
            onDismiss = { disabling = false },
            onConfirm = {
                onSave(false, risk.start, risk.end, risk.quota)
                disabling = false
            },
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun riskDescription(quota: Int): String =
    if (quota == 0) stringResource(R.string.risk_desc_blocked)
    else pluralStringResource(R.plurals.risk_desc_quota, quota, quota)

@Composable
private fun RiskDialog(
    risk: RiskUi,
    suggestion: Pair<Int, Int>?,
    onDismiss: () -> Unit,
    onSave: (start: Int, end: Int, quota: Int) -> Unit,
) {
    var start by remember { mutableIntStateOf(risk.start) }
    var end by remember { mutableIntStateOf(risk.end) }
    var quota by remember { mutableIntStateOf(risk.quota) }
    var waiting by remember { mutableStateOf(false) }
    val left = rememberCountdown(waiting)

    // Assouplir = plage active qui perd des heures, ou quota de plage relevé.
    fun looser(): Boolean = Rules.isRiskLooser(
        risk.enabled, risk.start, risk.end, risk.quota, start, end, quota,
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Calme.Card,
        title = { Text(stringResource(R.string.section_risk), fontFamily = Fraunces, fontWeight = FontWeight.SemiBold, color = Calme.Ink) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                if (waiting) {
                    LoosenText(stringResource(R.string.loosen_risk), left)
                    return@Column
                }
                Text(stringResource(R.string.risk_help), fontFamily = Nunito, fontSize = 14.sp, color = Calme.Muted)

                suggestion?.let { (s, e) ->
                    Row(
                        Modifier
                            .padding(top = 12.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Calme.Ground)
                            .padding(start = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            stringResource(R.string.risk_suggest, hourLabel(s), hourLabel(e)),
                            modifier = Modifier.weight(1f),
                            fontFamily = Nunito, fontSize = 14.sp, color = Calme.Ink,
                        )
                        TextButton(onClick = { start = s; end = e }) {
                            Text(stringResource(R.string.risk_use), fontFamily = Nunito, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Text(
                    stringResource(R.string.risk_range, hourLabel(start), hourLabel(end)),
                    modifier = Modifier.padding(top = 16.dp),
                    fontFamily = Fraunces, fontSize = 32.sp, color = Calme.Sage,
                )
                HourSlider(stringResource(R.string.risk_start, hourLabel(start)), start, 0, 23) { start = it }
                HourSlider(stringResource(R.string.risk_end, hourLabel(end)), end, 0, 23) { end = it }
                HourSlider(stringResource(R.string.risk_quota, quota), quota, 0, 5) { quota = it }
                Text(stringResource(R.string.risk_quota_zero_hint), fontFamily = Nunito, fontSize = 12.sp, color = Calme.Muted)
            }
        },
        confirmButton = {
            TextButton(
                enabled = !waiting || left == 0,
                onClick = { if (!waiting && looser()) waiting = true else onSave(start, end, quota) },
            ) {
                Text(
                    if (waiting && left > 0) stringResource(R.string.dialog_wait, left) else stringResource(R.string.dialog_save),
                    fontFamily = Nunito, fontWeight = FontWeight.Bold,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_cancel), fontFamily = Nunito, fontWeight = FontWeight.Bold, color = Calme.Muted)
            }
        },
    )
}

@Composable
private fun HourSlider(label: String, value: Int, min: Int, max: Int, onChange: (Int) -> Unit) {
    Text(label, modifier = Modifier.padding(top = 12.dp), fontFamily = Nunito, fontSize = 14.sp, color = Calme.Ink)
    Slider(
        value = value.toFloat(),
        onValueChange = { onChange(it.roundToInt()) },
        valueRange = min.toFloat()..max.toFloat(),
        steps = (max - min - 1).coerceAtLeast(0),
        colors = SliderDefaults.colors(
            thumbColor = Calme.Sage,
            activeTrackColor = Calme.Sage,
            inactiveTrackColor = Calme.Chip,
            activeTickColor = Calme.Sage,
            inactiveTickColor = Calme.Chip,
        ),
    )
}
