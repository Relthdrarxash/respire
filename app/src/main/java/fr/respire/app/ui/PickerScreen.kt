package fr.respire.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PickerScreen(
    apps: List<AppItem>,
    selected: Set<String>,
    onToggle: (String, Boolean) -> Unit,
    onBack: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var pendingRemoval by remember { mutableStateOf<AppItem?>(null) }
    val shown = remember(apps, query) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) apps else apps.filter { it.label.lowercase().contains(q) }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Calme.Ground)
            .systemBarsPadding()
    ) {
        Row(
            Modifier.padding(start = 8.dp, end = 20.dp, top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour", tint = Calme.Ink)
            }
            Text("Applis à freiner", fontFamily = Fraunces, fontWeight = FontWeight.SemiBold, fontSize = 26.sp, color = Calme.Ink)
        }

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            placeholder = { Text("Rechercher une appli", fontFamily = Nunito, color = Calme.Muted) },
            singleLine = true,
            shape = RoundedCornerShape(50),
            textStyle = TextStyle(fontFamily = Nunito, fontSize = 16.sp, color = Calme.Ink),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Calme.Sage,
                unfocusedBorderColor = Calme.Ring2,
                focusedContainerColor = Calme.Card,
                unfocusedContainerColor = Calme.Card,
                cursorColor = Calme.Sage,
            ),
        )

        if (apps.isEmpty()) {
            Text(
                "Chargement des applis…",
                modifier = Modifier.padding(24.dp),
                fontFamily = Nunito, fontSize = 15.sp, color = Calme.Muted,
            )
        }

        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(shown, key = { it.pkg }) { app ->
                val checked = app.pkg in selected
                Row(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 64.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (checked) Calme.Card else Color.Transparent)
                        .clickable(role = Role.Switch) {
                            if (checked) pendingRemoval = app else onToggle(app.pkg, true)
                        }
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    AppIcon(app.icon, 36)
                    Text(
                        app.label,
                        modifier = Modifier.weight(1f),
                        fontFamily = Nunito, fontWeight = FontWeight.SemiBold, fontSize = 16.sp,
                        color = Calme.Ink, maxLines = 1,
                    )
                    Switch(
                        checked = checked,
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
            }
        }
    }

    pendingRemoval?.let { app ->
        LoosenAppDialog(
            label = app.label,
            onDismiss = { pendingRemoval = null },
            onConfirm = {
                onToggle(app.pkg, false)
                pendingRemoval = null
            },
        )
    }
}
