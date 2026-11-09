package fr.respire.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.respire.app.ActivationStatus
import fr.respire.app.OnboardingStep
import fr.respire.app.R

/**
 * Accueil du premier lancement : quatre écrans, une idée chacun.
 * La promesse, les applis, l'information sur l'accessibilité (avec consentement), l'activation.
 * Les boutons de bas d'écran restent fixes et le contenu défile, pour tenir avec une grande taille de police.
 */
@Composable
fun OnboardingScreen(
    step: OnboardingStep,
    ui: HomeUi,
    activation: ActivationStatus,
    showRestrictedHelp: Boolean,
    onAddApp: (String) -> Unit,
    onPickApps: () -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onSkip: () -> Unit,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onOpenAccessibility: () -> Unit,
    onOpenAppInfo: () -> Unit,
    onFinish: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Calme.Ground)
            .systemBarsPadding(),
    ) {
        OnboardingTopBar(step, onBack = onBack, onSkip = onSkip)

        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp),
        ) {
            when (step) {
                OnboardingStep.WELCOME -> WelcomeContent()
                OnboardingStep.APPS -> AppsContent(ui, onAddApp, onPickApps)
                OnboardingStep.DISCLOSURE -> DisclosureContent()
                OnboardingStep.ACTIVATION -> ActivationContent(activation, showRestrictedHelp, onOpenAppInfo)
            }
        }

        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 8.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            when (step) {
                OnboardingStep.WELCOME -> PillButton(stringResource(R.string.onb_welcome_cta), onClick = onNext, height = 56.dp)
                OnboardingStep.APPS -> PillButton(stringResource(R.string.onb_next), onClick = onNext, height = 56.dp)
                OnboardingStep.DISCLOSURE -> {
                    Text(
                        stringResource(R.string.onb_disclosure_refuse_note),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                        fontFamily = Nunito, fontSize = 13.sp, lineHeight = 18.sp, color = Calme.Muted,
                    )
                    // Les deux choix ont le même poids visuel : le refus doit rester un vrai choix.
                    PillButton(stringResource(R.string.disclosure_accept), onClick = onAccept, height = 52.dp)
                    OutlinePillButton(stringResource(R.string.disclosure_decline), onClick = onDecline, height = 52.dp)
                }
                OnboardingStep.ACTIVATION -> when (activation) {
                    ActivationStatus.ENABLED ->
                        PillButton(stringResource(R.string.onb_done), onClick = onFinish, height = 56.dp)
                    ActivationStatus.NOT_TRIED -> {
                        PillButton(stringResource(R.string.activation_button), onClick = onOpenAccessibility, height = 56.dp)
                        TextAction(stringResource(R.string.onb_later), onClick = onFinish)
                    }
                    ActivationStatus.STILL_OFF -> {
                        PillButton(stringResource(R.string.onb_act_retry), onClick = onOpenAccessibility, height = 56.dp)
                        TextAction(stringResource(R.string.onb_later), onClick = onFinish)
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingTopBar(step: OnboardingStep, onBack: () -> Unit, onSkip: () -> Unit) {
    val total = OnboardingStep.entries.size
    val progress = stringResource(R.string.onb_progress, step.ordinal + 1, total)
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (step.previous() != null) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = Calme.Ink)
            }
        } else {
            Spacer(Modifier.size(48.dp))
        }
        Text(
            progress,
            modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
            fontFamily = Nunito, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Calme.Muted,
            textAlign = TextAlign.Center,
        )
        // Pas de « Passer » sur l'information d'accessibilité (le choix est « Refuser ») ni sur la dernière étape.
        if (step == OnboardingStep.WELCOME || step == OnboardingStep.APPS) {
            TextAction(stringResource(R.string.onb_skip), onClick = onSkip, fill = false)
        } else {
            Spacer(Modifier.size(48.dp))
        }
    }
}

@Composable
private fun WelcomeContent() {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        // Illustration décorative : le texte dit déjà tout.
        BreathingCircles(
            Modifier
                .padding(top = 8.dp, bottom = 24.dp)
                .size(190.dp)
                .clearAndSetSemantics { },
        ) {}
        OnboardingTitle(stringResource(R.string.onb_welcome_title), centered = true)
        Text(
            stringResource(R.string.onb_welcome_body),
            modifier = Modifier.padding(top = 12.dp),
            fontFamily = Nunito, fontSize = 16.sp, lineHeight = 23.sp, color = Calme.InkSoft,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun AppsContent(ui: HomeUi, onAddApp: (String) -> Unit, onPickApps: () -> Unit) {
    // Liste figée à l'arrivée : un appli ajoutée reste affichée, cochée, au lieu de disparaître.
    var shown by remember { mutableStateOf<List<AppItem>>(emptyList()) }
    LaunchedEffect(ui.suggestions) {
        if (shown.isEmpty() && ui.suggestions.isNotEmpty()) shown = ui.suggestions.take(5)
    }
    val chosen = ui.targets.map { it.app.pkg }.toSet()
    val doneLabel = stringResource(R.string.start_done)

    OnboardingTitle(stringResource(R.string.onb_apps_title))
    Text(
        stringResource(R.string.onb_apps_body),
        modifier = Modifier.padding(top = 8.dp, bottom = 12.dp),
        fontFamily = Nunito, fontSize = 16.sp, lineHeight = 23.sp, color = Calme.InkSoft,
    )
    if (shown.isNotEmpty()) {
        CalmeCard(radius = 24.dp, padding = 8.dp) {
            shown.forEach { app ->
                val added = app.pkg in chosen
                Row(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(enabled = !added, role = Role.Button) { onAddApp(app.pkg) }
                        .semantics(mergeDescendants = true) { if (added) stateDescription = doneLabel }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AppIcon(app.icon, 32)
                    Spacer(Modifier.width(12.dp))
                    Text(app.label, modifier = Modifier.weight(1f), fontFamily = Nunito, fontSize = 16.sp, color = Calme.Ink)
                    Text(
                        if (added) "✓" else "+",
                        fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 20.sp,
                        color = if (added) Calme.SageDark else Calme.Sage,
                    )
                }
            }
        }
    }
    TextAction(stringResource(R.string.apps_choose), onClick = onPickApps)
    if (ui.targets.isNotEmpty()) {
        Text(
            stringResource(R.string.onb_apps_chosen, ui.targets.size),
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .semantics { liveRegion = LiveRegionMode.Polite },
            fontFamily = Nunito, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Calme.SageDark,
        )
    }
}

@Composable
private fun DisclosureContent() {
    OnboardingTitle(stringResource(R.string.disclosure_title))
    CalmeCard(modifier = Modifier.padding(top = 12.dp), radius = 24.dp, padding = 18.dp) {
        Text(
            stringResource(R.string.disclosure_body),
            fontFamily = Nunito, fontSize = 15.sp, lineHeight = 22.sp, color = Calme.Ink,
        )
    }
}

@Composable
private fun ActivationContent(activation: ActivationStatus, showRestrictedHelp: Boolean, onOpenAppInfo: () -> Unit) {
    val (title, body) = when (activation) {
        ActivationStatus.ENABLED -> R.string.onb_act_ok_title to R.string.onb_act_ok_body
        ActivationStatus.STILL_OFF -> R.string.onb_act_off_title to R.string.onb_act_off_body
        ActivationStatus.NOT_TRIED -> R.string.onb_act_title to R.string.onb_act_body
    }
    // Annoncé par le lecteur d'écran au retour des réglages.
    Column(Modifier.semantics { liveRegion = LiveRegionMode.Polite }) {
        OnboardingTitle(stringResource(title))
        Text(
            stringResource(body),
            modifier = Modifier.padding(top = 8.dp),
            fontFamily = Nunito, fontSize = 16.sp, lineHeight = 23.sp, color = Calme.InkSoft,
        )
    }
    if (activation != ActivationStatus.ENABLED && showRestrictedHelp) {
        CalmeCard(modifier = Modifier.padding(top = 20.dp), radius = 24.dp, padding = 18.dp) {
            Text(
                stringResource(R.string.onb_act_restricted_title),
                fontFamily = Fraunces, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = Calme.Ink,
            )
            Text(
                stringResource(R.string.onb_act_restricted_body),
                modifier = Modifier.padding(top = 6.dp),
                fontFamily = Nunito, fontSize = 14.sp, lineHeight = 20.sp, color = Calme.Muted,
            )
            TextAction(stringResource(R.string.onb_act_appinfo), onClick = onOpenAppInfo, center = false)
        }
    }
}

@Composable
private fun OnboardingTitle(text: String, centered: Boolean = false) {
    Text(
        text,
        modifier = Modifier.fillMaxWidth().semantics { heading() },
        fontFamily = Fraunces, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 34.sp,
        color = Calme.Ink,
        textAlign = if (centered) TextAlign.Center else TextAlign.Start,
    )
}

/** Action secondaire sous forme de texte, zone tactile d'au moins 48 dp. */
@Composable
internal fun TextAction(
    text: String,
    onClick: () -> Unit,
    fill: Boolean = true,
    center: Boolean = fill,
) {
    Text(
        text,
        modifier = (if (fill) Modifier.fillMaxWidth() else Modifier)
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = if (center) 8.dp else 0.dp, vertical = 12.dp),
        fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 15.sp,
        color = Calme.SageDark,
        textAlign = if (center) TextAlign.Center else TextAlign.Start,
    )
}

@Composable
private fun OutlinePillButton(text: String, onClick: () -> Unit, height: Dp) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().heightIn(min = height),
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.5.dp, Calme.Sage),
        colors = ButtonDefaults.buttonColors(containerColor = Calme.Ground, contentColor = Calme.SageDark),
    ) {
        Text(text, fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 17.sp)
    }
}
