package fr.respire.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import fr.respire.app.R

/** Palette « Calme » : sauge et sable. */
object Calme {
    val Ground = Color(0xFFEDEEE6)
    val Card = Color(0xFFF8F8F3)
    val Chip = Color(0xFFE1E4D9)
    val Divider = Color(0xFFE5E7DF)
    val Ink = Color(0xFF26332C)
    val InkSoft = Color(0xFF3E4C44)
    val Muted = Color(0xFF5D6B63)
    val Sage = Color(0xFF4E7363)
    val SageDark = Color(0xFF3A584B)
    val SageTint = Color(0xFFDCE6DC)
    val Ring1 = Color(0xFFD8DFD3)
    val Ring2 = Color(0xFFC3D1C4)
    val Ring3 = Color(0xFFA9C0AF)
    val Clay = Color(0xFFA0613E)
    val ClayText = Color(0xFF8A4A2B)
}

val Fraunces = FontFamily(
    Font(R.font.fraunces_regular, FontWeight.Normal),
    Font(R.font.fraunces_semibold, FontWeight.SemiBold),
)

val Nunito = FontFamily(
    Font(R.font.nunito_regular, FontWeight.Normal),
    Font(R.font.nunito_semibold, FontWeight.SemiBold),
    Font(R.font.nunito_bold, FontWeight.Bold),
)

private val scheme = lightColorScheme(
    primary = Calme.Sage,
    onPrimary = Color.White,
    primaryContainer = Calme.SageTint,
    onPrimaryContainer = Calme.Ink,
    secondary = Calme.Muted,
    background = Calme.Ground,
    onBackground = Calme.Ink,
    surface = Calme.Card,
    onSurface = Calme.Ink,
    surfaceVariant = Calme.Chip,
    onSurfaceVariant = Calme.Muted,
    surfaceContainerHigh = Calme.Card,
    outline = Calme.Muted,
)

@Composable
fun RespireTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = scheme, content = content)
}
