package app.silberkorn.ui

import android.content.Intent
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.silberkorn.Darkroom
import app.silberkorn.Library
import app.silberkorn.NegMeta
import app.silberkorn.Settings
import app.silberkorn.core.Aspect
import app.silberkorn.core.Look
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

private val TABS = listOf("Ton", "Farbe", "Textur", "Format")

@Composable
fun EditScreen(id: String, onBack: () -> Unit, onCamera: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val shots by Library.shots.collectAsState()
    val shot = shots.firstOrNull { it.id == id }
    if (shot == null) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    var look by remember(id) { mutableStateOf(shot.look) }
    var session by remember(id) { mutableStateOf<Darkroom.Session?>(null) }
    var rendered by remember(id) { mutableStateOf<Bitmap?>(null) }
    var tab by remember { mutableIntStateOf(0) }
    var menu by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var exporting by remember { mutableStateOf(false) }

    LaunchedEffect(id) { session = runCatching { Darkroom.openSession(id) }.getOrNull() }
    LaunchedEffect(session) {
        val s = session ?: return@LaunchedEffect
        // Always print the newest slider position; skip the ones in between.
        snapshotFlow { look }.conflate().collect { l ->
            rendered = withContext(Dispatchers.Default) { runCatching { s.render(l) }.getOrNull() } ?: rendered
        }
    }

    /** Stores the look and re-develops the gallery preview if anything changed. */
    fun commit() {
        if (look != shot.look) {
            Library.setLook(id, look)
            Darkroom.enqueue(id, export = false)
        }
    }

    fun leave(to: () -> Unit) {
        commit()
        to()
    }
    BackHandler { leave(onBack) }

    fun export(share: Boolean) {
        if (exporting) return
        exporting = true
        commit()
        scope.launch {
            val uri = runCatching { Darkroom.export(id) }.getOrNull()
            exporting = false
            if (uri == null) {
                Toast.makeText(context, "Export fehlgeschlagen", Toast.LENGTH_SHORT).show()
            } else if (share) {
                val send = Intent(Intent.ACTION_SEND).setType("image/jpeg").putExtra(Intent.EXTRA_STREAM, uri)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                context.startActivity(Intent.createChooser(send, "Bild teilen"))
            } else {
                Toast.makeText(context, "In Bilder/Silberkorn gesichert", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { leave(onBack) }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Zur Galerie", tint = Paper) }
            TextButton(onClick = { leave(onCamera) }) { Text("Kamera") }
            Spacer(Modifier.weight(1f))
            if (exporting) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            TextButton(onClick = { export(false) }, enabled = !exporting) { Text("Sichern") }
            IconButton(onClick = { export(true) }, enabled = !exporting) { Icon(Icons.Default.Share, "Teilen", tint = Paper) }
            Box {
                IconButton(onClick = { menu = true }) { Icon(Icons.Default.MoreVert, "Mehr", tint = Paper) }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(text = { Text("Zurücksetzen") }, onClick = {
                        menu = false
                        look = Look(aspect = look.aspect, quarterTurns = look.quarterTurns)
                    })
                    DropdownMenuItem(text = { Text("Als Standard-Look für neue Bilder") }, onClick = {
                        menu = false
                        Settings.update { it.copy(defaultLook = look.copy(quarterTurns = 0, aspect = it.defaultLook.aspect)) }
                        Toast.makeText(context, "Standard-Look gespeichert", Toast.LENGTH_SHORT).show()
                    })
                    DropdownMenuItem(
                        text = { Text("Löschen") },
                        leadingIcon = { Icon(Icons.Default.Delete, null) },
                        onClick = { menu = false; confirmDelete = true },
                    )
                }
            }
        }

        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            val live = rendered
            if (live != null) {
                Image(live.asImageBitmap(), "Entwickeltes Bild", contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize())
            } else {
                rememberPreview(shot, 2000)?.let {
                    Image(it, "Entwickeltes Bild", contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize())
                }
                CircularProgressIndicator(Modifier.size(28.dp), strokeWidth = 2.dp)
            }
        }

        Text(
            describe(shot.meta), color = Dim, fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        )

        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            TABS.forEachIndexed { i, name ->
                Text(
                    name, color = if (tab == i) Safelight else Dim, fontWeight = FontWeight.Medium,
                    modifier = Modifier.clip(CircleShape).clickable { tab = i }.padding(horizontal = 14.dp, vertical = 10.dp),
                )
            }
        }

        Column(Modifier.fillMaxWidth().height(232.dp).padding(horizontal = 16.dp), verticalArrangement = Arrangement.Center) {
            when (tab) {
                0 -> {
                    Knob("Belichtung", look.exposure, -3f..3f, 0f) { look = look.copy(exposure = it) }
                    Knob("Kontrast", look.contrast, -1f..1f, 0f) { look = look.copy(contrast = it) }
                    Knob("Lichter", look.highlights, -1f..1f, 0f) { look = look.copy(highlights = it) }
                    Knob("Schatten", look.shadows, -1f..1f, 0f) { look = look.copy(shadows = it) }
                }
                1 -> {
                    Knob("Temperatur", look.temperature, -1f..1f, 0f) { look = look.copy(temperature = it) }
                    Knob("Tönung", look.tint, -1f..1f, 0f) { look = look.copy(tint = it) }
                    Knob("Sättigung", look.saturation, -1f..1f, 0f) { look = look.copy(saturation = it) }
                    Row(Modifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Schwarzweiß", color = Paper, modifier = Modifier.weight(1f))
                        Switch(checked = look.monochrome, onCheckedChange = { look = look.copy(monochrome = it) })
                    }
                }
                2 -> {
                    val d = Look()
                    Knob("Korn", look.grain, 0f..1f, d.grain) { look = look.copy(grain = it) }
                    Knob("Korngröße", look.grainSize, 0f..1f, d.grainSize) { look = look.copy(grainSize = it) }
                    Knob("Halation", look.halation, 0f..1f, d.halation) { look = look.copy(halation = it) }
                }
                else -> {
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                        Aspect.ALL.forEachIndexed { i, a ->
                            val selected = look.aspect == i
                            Text(
                                a.label, color = if (selected) Ink else Paper, fontSize = 14.sp,
                                modifier = Modifier.padding(end = 8.dp).clip(CircleShape)
                                    .background(if (selected) Paper else Tray)
                                    .clickable { look = look.copy(aspect = i) }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    TextButton(onClick = { look = look.copy(quarterTurns = (look.quarterTurns + 1) % 4) }) {
                        Text("Um 90° drehen")
                    }
                    Text("Der Zuschnitt ist nie endgültig – das volle Sensorbild bleibt erhalten.", color = Dim, fontSize = 12.sp)
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Bild löschen?") },
            text = { Text("Das Negativ wird endgültig entfernt. Bereits gesicherte JPEGs bleiben in der Galerie.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    Library.delete(id)
                    onBack()
                }) { Text("Löschen") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Abbrechen") } },
        )
    }
}

/** A labelled slider. Tapping the value puts it back to its default. */
@Composable
private fun Knob(label: String, value: Float, range: ClosedFloatingPointRange<Float>, default: Float, onChange: (Float) -> Unit) {
    Row(Modifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Paper, fontSize = 14.sp, modifier = Modifier.width(92.dp))
        Slider(
            value = value, valueRange = range, onValueChange = onChange,
            modifier = Modifier.weight(1f).semantics { contentDescription = label },
        )
        val shown = (value * 100 / maxOf(range.endInclusive, 1f)).roundToInt()
        Text(
            if (range.endInclusive > 1f) String.format("%+.1f", value).replace('.', ',') else "$shown",
            color = if (value == default) Dim else Safelight, fontSize = 13.sp,
            modifier = Modifier.width(48.dp).clip(CircleShape).clickable(onClickLabel = "$label zurücksetzen") { onChange(default) }
                .padding(start = 8.dp, top = 6.dp, bottom = 6.dp),
        )
    }
}

private fun describe(m: NegMeta): String {
    val parts = mutableListOf<String>()
    parts += if (m.raw) "RAW" else "JPEG"
    if (m.exposureNs > 0) {
        val s = m.exposureNs / 1e9
        parts += if (s >= 0.4) String.format("%.1f s", s).replace('.', ',') else "1/${(1 / s).roundToInt()} s"
    }
    if (m.aperture > 0f) parts += "f/" + String.format("%.1f", m.aperture).replace('.', ',')
    if (m.iso > 0) parts += "ISO ${m.iso}"
    if (m.focalEq > 0f) parts += "${m.focalEq.roundToInt()} mm"
    parts += "${m.width} × ${m.height}"
    return parts.joinToString("  ·  ")
}
