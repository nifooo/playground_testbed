package app.silberkorn.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.silberkorn.Settings
import app.silberkorn.core.Look
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val prefs by Settings.prefs.collectAsState()
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Zurück", tint = Paper) }
            Text("Einstellungen", color = Paper, fontSize = 22.sp, fontWeight = FontWeight.Medium)
        }
        Column(Modifier.padding(horizontal = 16.dp)) {
            Toggle(
                "Automatisch sichern",
                "Jedes neue Bild wird nach dem Entwickeln als JPEG in Bilder/Silberkorn abgelegt.",
                prefs.autoExport,
            ) { v -> Settings.update { it.copy(autoExport = v) } }
            Toggle(
                "DNG zusätzlich speichern",
                "Legt die unveränderten Sensordaten als DNG in Bilder/Silberkorn ab, für andere RAW-Programme.",
                prefs.saveDng,
            ) { v -> Settings.update { it.copy(saveDng = v) } }

            Spacer(Modifier.height(12.dp))
            Text("JPEG-Qualität: ${prefs.jpegQuality}", color = Paper)
            Slider(
                value = prefs.jpegQuality.toFloat(), valueRange = 70f..100f,
                onValueChange = { v -> Settings.update { it.copy(jpegQuality = v.roundToInt()) } },
                modifier = Modifier.semantics { contentDescription = "JPEG-Qualität" },
            )

            Spacer(Modifier.height(12.dp))
            Text("Standard-Look", color = Paper)
            Text(
                "Neue Bilder starten mit dem Look, den du im Editor über „Als Standard-Look“ festgelegt hast.",
                color = Dim, fontSize = 13.sp,
            )
            TextButton(
                onClick = { Settings.update { it.copy(defaultLook = Look(aspect = it.defaultLook.aspect)) } },
                enabled = prefs.defaultLook.copy(aspect = 0) != Look(),
            ) { Text("Auf Werkseinstellung zurücksetzen") }

            Spacer(Modifier.height(20.dp))
            Text(
                "Silberkorn entwickelt jedes Bild aus den rohen Sensordaten: eigene Tonkurve, eigene Farben, " +
                    "Halation und Korn. Die Negative bleiben unverändert in der App, jede Bearbeitung lässt sich " +
                    "später ändern. Der Sucher zeigt das Motiv – den fertigen Look siehst du nach dem Entwickeln.",
                color = Dim, fontSize = 13.sp,
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun Toggle(title: String, detail: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, color = Paper)
            Text(detail, color = Dim, fontSize = 13.sp)
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
