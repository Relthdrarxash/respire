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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import fr.respire.app.R
import fr.respire.app.Suggestions
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
    // Suggestions : applis à flux sans fin installées et pas encore choisies (masquées pendant une recherche).
    val suggested = remember(apps, selected, query) {
        if (query.isNotBlank()) emptyList()
        else {
            val byPkg = apps.associateBy { it.pkg }
            Suggestions.pick(byPkg.keys, selected).mapNotNull { byPkg[it] }
        }
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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = Calme.Ink)
            }
            Text(stringResource(R.string.picker_title), fontFamily = Fraunces, fontWeight = FontWeight.SemiBold, fontSize = 26.sp, color = Calme.Ink)
        }

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            placeholder = { Text(stringResource(R.string.picker_search), fontFamily = Nunito, color = Calme.Muted) },
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
                stringResource(R.string.picker_loading),
                modifier = Modifier.padding(24.dp),
                fontFamily = Nunito, fontSize = 15.sp, color = Calme.Muted,
            )
        }

        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (suggested.isNotEmpty()) {
                item(key = "suggest_title") { PickerSection(stringResource(R.string.picker_suggest_title)) }
                items(suggested, key = { "s_" + it.pkg }) { app ->
                    AppRow(app, checked = false) { onToggle(app.pkg, true) }
                }
                if (suggested.size > 1) {
                    item(key = "suggest_all") {
                        PillButton(
                            pluralStringResource(R.plurals.picker_add_all, suggested.size, suggested.size),
                            onClick = { suggested.forEach { onToggle(it.pkg, true) } },
                            modifier = Modifier.padding(top = 4.dp),
                            height = 48.dp,
                        )
                    }
                }
                item(key = "all_title") { PickerSection(stringResource(R.string.picker_all_title)) }
            }
            items(shown, key = { it.pkg }) { app ->
                val checked = app.pkg in selected
                AppRow(app, checked) {
                    if (checked) pendingRemoval = app else onToggle(app.pkg, true)
                }
            }
        }
    }

    pendingRemoval?.let { app ->
        LoosenDialog(
            title = stringResource(R.string.loosen_app_title, app.label),
            what = stringResource(R.string.loosen_app_what, app.label),
            confirmLabel = stringResource(R.string.loosen_app_confirm),
            onDismiss = { pendingRemoval = null },
            onConfirm = {
                onToggle(app.pkg, false)
                pendingRemoval = null
            },
        )
    }
}

@Composable
private fun PickerSection(text: String) {
    Text(
        text,
        modifier = Modifier.padding(start = 4.dp, top = 12.dp, bottom = 2.dp),
        fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Calme.Muted,
    )
}

@Composable
private fun AppRow(app: AppItem, checked: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(if (checked) Calme.Card else Color.Transparent)
            .clickable(role = Role.Switch, onClick = onClick)
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
