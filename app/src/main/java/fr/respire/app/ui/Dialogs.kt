package fr.respire.app.ui

import android.content.res.Resources
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.respire.app.R
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/** Attente imposée avant d'assouplir une règle : c'est là qu'on rechute le plus. */
const val LOOSEN_DELAY_S = 30

enum class Rule(
    @StringRes val title: Int,
    @StringRes val help: Int,
    val min: Int,
    val max: Int,
    private val looserWhenHigher: Boolean,
) {
    DELAY(R.string.rule_delay_title, R.string.rule_delay_help, 3, 60, false),
    STEP(R.string.rule_step_title, R.string.rule_step_help, 0, 15, false),
    SESSION(R.string.rule_session_title, R.string.rule_session_help, 1, 30, true),
    QUOTA(R.string.rule_quota_title, R.string.rule_quota_help, 0, 20, true);

    fun format(res: Resources, value: Int): String = when (this) {
        DELAY -> res.getString(R.string.fmt_seconds, value)
        STEP -> res.getString(R.string.fmt_plus_seconds, value)
        SESSION -> res.getString(R.string.fmt_minutes, value)
        QUOTA -> if (value == 0) res.getString(R.string.fmt_unlimited) else res.getString(R.string.fmt_per_day, value)
    }

    fun isLooser(old: Int, new: Int): Boolean = when (this) {
        QUOTA -> old > 0 && (new == 0 || new > old)
        else -> if (looserWhenHigher) new > old else new < old
    }
}

/** Compte à rebours partagé par toutes les confirmations d'assouplissement. */
@Composable
internal fun rememberCountdown(start: Boolean): Int {
    var left by remember { mutableIntStateOf(LOOSEN_DELAY_S) }
    LaunchedEffect(start) {
        if (start) {
            left = LOOSEN_DELAY_S
            while (left > 0) {
                delay(1000)
                left--
            }
        }
    }
    return left
}

@Composable
fun RuleDialog(rule: Rule, current: Int, onDismiss: () -> Unit, onSave: (Int) -> Unit) {
    val res = LocalContext.current.resources
    var value by remember { mutableIntStateOf(current) }
    var waiting by remember { mutableStateOf(false) }
    val left = rememberCountdown(waiting)
    val title = stringResource(rule.title)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Calme.Card,
        title = { Text(title, fontFamily = Fraunces, fontWeight = FontWeight.SemiBold, color = Calme.Ink) },
        text = {
            Column {
                if (!waiting) {
                    Text(stringResource(rule.help), fontFamily = Nunito, fontSize = 15.sp, color = Calme.Muted)
                    Text(
                        rule.format(res, value),
                        modifier = Modifier.padding(top = 20.dp),
                        fontFamily = Fraunces,
                        fontSize = 40.sp,
                        color = Calme.Sage,
                    )
                    Slider(
                        value = value.toFloat(),
                        onValueChange = { value = it.roundToInt() },
                        valueRange = rule.min.toFloat()..rule.max.toFloat(),
                        steps = (rule.max - rule.min - 1).coerceAtLeast(0),
                        colors = SliderDefaults.colors(
                            thumbColor = Calme.Sage,
                            activeTrackColor = Calme.Sage,
                            inactiveTrackColor = Calme.Chip,
                            activeTickColor = Calme.Sage,
                            inactiveTickColor = Calme.Chip,
                        ),
                    )
                } else {
                    LoosenText(
                        stringResource(R.string.loosen_rule, title, rule.format(res, current), rule.format(res, value)),
                        left,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !waiting || left == 0,
                onClick = {
                    if (!waiting && rule.isLooser(current, value)) waiting = true else onSave(value)
                },
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

/** Confirmation avec attente imposée (retirer une appli, couper un filtre...). */
@Composable
fun LoosenDialog(
    title: String,
    what: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val left = rememberCountdown(true)
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Calme.Card,
        title = { Text(title, fontFamily = Fraunces, fontWeight = FontWeight.SemiBold, color = Calme.Ink) },
        text = { LoosenText(what, left) },
        confirmButton = {
            TextButton(enabled = left == 0, onClick = onConfirm) {
                Text(
                    if (left > 0) stringResource(R.string.dialog_wait, left) else confirmLabel,
                    fontFamily = Nunito, fontWeight = FontWeight.Bold,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.loosen_keep), fontFamily = Nunito, fontWeight = FontWeight.Bold, color = Calme.Muted)
            }
        },
    )
}

@Composable
internal fun LoosenText(what: String, left: Int) {
    Column {
        Text(what, fontFamily = Nunito, fontSize = 15.sp, color = Calme.Ink)
        Text(
            stringResource(R.string.loosen_why),
            modifier = Modifier.padding(top = 12.dp),
            fontFamily = Nunito,
            fontSize = 14.sp,
            color = Calme.Muted,
        )
        if (left > 0) {
            Text(
                "$left",
                modifier = Modifier.padding(top = 16.dp),
                fontFamily = Fraunces,
                fontSize = 40.sp,
                color = Calme.Sage,
            )
        }
    }
}
