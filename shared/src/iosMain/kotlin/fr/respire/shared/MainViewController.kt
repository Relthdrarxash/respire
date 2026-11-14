package fr.respire.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.coroutines.delay
import platform.UIKit.UIViewController

/** Point d'entrée iOS de Respire (cohérence cardiaque 4 s / 6 s ; bouclier Screen Time géré côté Swift). */
fun MainViewController(): UIViewController = ComposeUIViewController {
    var elapsedMs by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(50L)
            elapsedMs = (elapsedMs + 50L) % CoherenceTiming.CYCLE_MS
        }
    }
    val snap = CoherenceTiming.snapshotAt(elapsedMs)
    val scale = if (snap.phase == CoherenceTiming.Phase.INHALE) {
        0.72f + 0.28f * snap.progress
    } else {
        1.0f - 0.28f * snap.progress
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF0E1612)) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "Respire",
                    color = Color(0xFFE6EFEA),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(32.dp))
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .scale(scale)
                        .background(Color(0xFF3E7B5E), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "${snap.secondsRemaining} s",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.height(24.dp))
                Text(
                    text = if (snap.phase == CoherenceTiming.Phase.INHALE) "Inspirez doucement (4 s)" else "Expirez lentement (6 s)",
                    color = Color(0xFFA9C3B5),
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Le blocage d'applications sur iPhone s'appuiera sur l'API Screen Time d'Apple dès réception de l'entitlement Family Controls.",
                    color = Color(0xFF7B9386),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
