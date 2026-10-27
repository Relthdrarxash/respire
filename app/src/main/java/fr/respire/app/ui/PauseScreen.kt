package fr.respire.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class PauseUi(
    val appLabel: String,
    val appIcon: ImageBitmap?,
    val sessionEnd: Boolean,
    val blocked: Boolean,
    val opensToday: Int,
    val quota: Int,
    val sessionMinutes: Int,
)

@Composable
fun PauseScreen(
    ui: PauseUi,
    remaining: Int,
    onClose: () -> Unit,
    onOpen: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Calme.Ground)
            .systemBarsPadding()
            .padding(start = 28.dp, end = 28.dp, top = 28.dp, bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AppChip(ui.appLabel, ui.appIcon)

        Spacer(Modifier.weight(0.6f))

        BreathingCircles(
            Modifier
                .fillMaxWidth(0.72f)
                .widthIn(max = 300.dp)
                .aspectRatio(1f)
        ) {
            if (remaining > 0) {
                Text(
                    remaining.toString(),
                    fontFamily = Fraunces,
                    fontWeight = FontWeight.Normal,
                    fontSize = 64.sp,
                    color = Calme.Ink,
                )
            }
        }

        Text(
            text = when {
                ui.blocked -> "Reviens demain."
                remaining > 0 -> "Inspire quand le cercle grandit, expire quand il rétrécit."
                else -> "Si c’est vraiment utile, tu peux y aller."
            },
            modifier = Modifier.padding(top = 20.dp),
            fontFamily = Nunito,
            fontSize = 15.sp,
            color = Calme.Muted,
            textAlign = TextAlign.Center,
        )

        Text(
            text = when {
                ui.blocked -> "Quota du jour atteint."
                ui.sessionEnd -> "Ça fait ${ui.sessionMinutes} min.\nTu continues vraiment ?"
                else -> "Pourquoi tu l’ouvres maintenant ?"
            },
            modifier = Modifier.padding(top = 32.dp),
            fontFamily = Fraunces,
            fontWeight = FontWeight.SemiBold,
            fontSize = 28.sp,
            lineHeight = 34.sp,
            color = Calme.Ink,
            textAlign = TextAlign.Center,
        )

        Text(
            text = if (ui.quota > 0) {
                "${ui.opensToday} sur ${ui.quota} ouvertures aujourd’hui"
            } else {
                "${ui.opensToday} ouvertures aujourd’hui"
            },
            modifier = Modifier.padding(top = 12.dp),
            fontFamily = Nunito,
            fontSize = 15.sp,
            color = Calme.Muted,
        )

        Spacer(Modifier.weight(1f))

        PillButton("Non, je ferme", onClick = onClose)

        if (!ui.blocked) {
            val verb = if (ui.sessionEnd) "Continuer" else "Ouvrir quand même"
            TextButton(
                onClick = onOpen,
                enabled = remaining == 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .height(48.dp),
            ) {
                Text(
                    if (remaining > 0) "$verb dans $remaining s" else verb,
                    fontFamily = Nunito,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Calme.Muted,
                )
            }
        }
    }
}

@Composable
private fun AppChip(label: String, icon: ImageBitmap?) {
    Row(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(Calme.Chip)
            .padding(start = 6.dp, end = 14.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (icon != null) {
            Image(icon, contentDescription = null, modifier = Modifier.size(28.dp).clip(RoundedCornerShape(9.dp)))
        } else {
            Box(Modifier.size(28.dp).clip(RoundedCornerShape(9.dp)).background(Calme.Ring2))
        }
        Text(label, fontFamily = Nunito, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Calme.InkSoft)
    }
}
