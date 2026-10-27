package fr.respire.app.ui

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/** Attente imposée avant d'assouplir une règle : c'est là qu'on rechute le plus. */
const val LOOSEN_DELAY_S = 30

enum class Rule(
    val title: String,
    val help: String,
    val min: Int,
    val max: Int,
    private val looserWhenHigher: Boolean,
) {
    DELAY("Attente", "Temps à patienter avant de pouvoir ouvrir une appli freinée.", 3, 60, false),
    STEP("Allongement", "Secondes ajoutées à chaque nouvelle tentative dans l’heure.", 0, 15, false),
    SESSION("Session", "Au bout de cette durée, la pause revient même si tu es encore dans l’appli.", 1, 30, true),
    QUOTA("Quota", "Ouvertures (et prolongations) autorisées par appli et par jour. Au-delà, l’appli est bloquée jusqu’au lendemain.", 0, 20, true);

    fun format(value: Int): String = when (this) {
        DELAY -> "$value s"
        STEP -> "+$value s"
        SESSION -> "$value min"
        QUOTA -> if (value == 0) "illimité" else "$value / jour"
    }

    fun isLooser(old: Int, new: Int): Boolean = when (this) {
        QUOTA -> old > 0 && (new == 0 || new > old)
        else -> if (looserWhenHigher) new > old else new < old
    }
}

@Composable
fun RuleDialog(rule: Rule, current: Int, onDismiss: () -> Unit, onSave: (Int) -> Unit) {
    var value by remember { mutableIntStateOf(current) }
    var waiting by remember { mutableStateOf(false) }
    var left by remember { mutableIntStateOf(LOOSEN_DELAY_S) }

    LaunchedEffect(waiting) {
        if (waiting) {
            left = LOOSEN_DELAY_S
            while (left > 0) {
                delay(1000)
                left--
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Calme.Card,
        title = { Text(rule.title, fontFamily = Fraunces, fontWeight = FontWeight.SemiBold, color = Calme.Ink) },
        text = {
            Column {
                if (!waiting) {
                    Text(rule.help, fontFamily = Nunito, fontSize = 15.sp, color = Calme.Muted)
                    Text(
                        rule.format(value),
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
                    LoosenText("Tu rends la règle « ${rule.title} » plus permissive (${rule.format(current)} → ${rule.format(value)}).", left)
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
                    if (waiting && left > 0) "Patiente ($left s)" else "Enregistrer",
                    fontFamily = Nunito, fontWeight = FontWeight.Bold,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", fontFamily = Nunito, fontWeight = FontWeight.Bold, color = Calme.Muted)
            }
        },
    )
}

@Composable
fun LoosenAppDialog(label: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    var left by remember { mutableIntStateOf(LOOSEN_DELAY_S) }
    LaunchedEffect(Unit) {
        while (left > 0) {
            delay(1000)
            left--
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Calme.Card,
        title = { Text("Retirer $label ?", fontFamily = Fraunces, fontWeight = FontWeight.SemiBold, color = Calme.Ink) },
        text = { LoosenText("$label ne sera plus freinée.", left) },
        confirmButton = {
            TextButton(enabled = left == 0, onClick = onConfirm) {
                Text(if (left > 0) "Patiente ($left s)" else "Retirer", fontFamily = Nunito, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Garder", fontFamily = Nunito, fontWeight = FontWeight.Bold, color = Calme.Muted)
            }
        },
    )
}

@Composable
private fun LoosenText(what: String, left: Int) {
    Column {
        Text(what, fontFamily = Nunito, fontSize = 15.sp, color = Calme.Ink)
        Text(
            "Les rechutes arrivent surtout quand on assouplit ses règles sur un coup de tête. Prends ces quelques secondes pour vérifier que tu le veux vraiment.",
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
