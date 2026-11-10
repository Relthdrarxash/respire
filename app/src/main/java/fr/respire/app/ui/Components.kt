package fr.respire.app.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Trois disques concentriques qui grandissent pendant 4 s (inspiration)
 * puis rétrécissent pendant 4 s (expiration), légèrement décalés.
 */
@Composable
fun BreathingCircles(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val transition = rememberInfiniteTransition(label = "respiration")
    val s1 by transition.breath(0)
    val s2 by transition.breath(400)
    val s3 by transition.breath(800)

    Box(modifier, contentAlignment = Alignment.Center) {
        Disc(1f, s1, Calme.Ring1)
        Disc(0.714f, s2, Calme.Ring2)
        Disc(0.4f, s3, Calme.Ring3)
        content()
    }
}

@Composable
private fun InfiniteTransition.breath(offsetMs: Int) = animateFloat(
    initialValue = 0.62f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
        animation = tween(durationMillis = 4000, easing = FastOutSlowInEasing),
        repeatMode = RepeatMode.Reverse,
        initialStartOffset = StartOffset(offsetMs),
    ),
    label = "cercle$offsetMs",
)

@Composable
private fun BoxScope.Disc(fraction: Float, scale: Float, color: Color) {
    Box(
        Modifier
            .align(Alignment.Center)
            .fillMaxSize(fraction)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .background(color, CircleShape)
    )
}

@Composable
fun PillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = 56.dp,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().heightIn(min = height),
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = Calme.Sage,
            contentColor = Color.White,
            disabledContainerColor = Calme.Chip,
            disabledContentColor = Calme.Muted,
        ),
    ) {
        Text(text, fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 17.sp)
    }
}

@Composable
fun CalmeCard(
    modifier: Modifier = Modifier,
    radius: Dp = 28.dp,
    padding: Dp = 22.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(radius))
            .background(Calme.Card)
            .padding(padding),
        content = content,
    )
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier = modifier
            .padding(start = 4.dp, top = 8.dp, bottom = 8.dp)
            .semantics { heading() },
        fontFamily = Nunito,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        color = Calme.Ink,
    )
}

object RespireIcons {
    val Apps: ImageVector by lazy {
        stroke(
            "Apps",
            "M4 4h6v6H4z", "M14 4h6v6h-6z", "M4 14h6v6H4z", "M14 14h6v6h-6z",
        )
    }
    val Chart: ImageVector by lazy {
        stroke("Chart", "M5 20V10", "M12 20V4", "M19 20v-7")
    }
    val Settings: ImageVector by lazy {
        stroke(
            "Settings",
            "M4 21v-7", "M4 10V3", "M12 21v-9", "M12 8V3", "M20 21v-5", "M20 12V3",
            "M1 14h6", "M9 8h6", "M17 16h6",
        )
    }

    private fun stroke(name: String, vararg paths: String): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            paths.forEach { d ->
                addPath(
                    pathData = PathParser().parsePathString(d).toNodes(),
                    fill = null,
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                )
            }
        }.build()
}
