package app.silberkorn.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.silberkorn.Darkroom
import app.silberkorn.Library
import app.silberkorn.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun GalleryScreen(onCamera: () -> Unit, onSettings: () -> Unit, onOpen: (String) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val shots by Library.shots.collectAsState()
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(20)) { uris ->
        scope.launch(Dispatchers.IO) {
            // Imported pictures keep their own framing.
            val look = Settings.prefs.value.defaultLook.copy(aspect = 0, quarterTurns = 0)
            uris.forEach { uri ->
                runCatching { Library.importUri(context, uri, look) }.getOrNull()
                    ?.let { Darkroom.enqueue(it.id, export = false) }
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Negative", color = Paper, fontSize = 22.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.weight(1f))
            TextButton(onClick = {
                picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }) { Text("Importieren") }
            IconButton(onClick = onSettings) { Icon(Icons.Default.Settings, "Einstellungen", tint = Paper) }
        }

        if (shots.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                Text(
                    "Noch keine Bilder.\nNimm eins auf oder importiere ein Foto, um es zu entwickeln.",
                    color = Dim, textAlign = TextAlign.Center,
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                items(shots, key = { it.id }) { shot ->
                    Box(
                        Modifier.aspectRatio(1f).background(Tray).clickable { onOpen(shot.id) },
                        contentAlignment = Alignment.Center,
                    ) {
                        rememberPreview(shot, 360)?.let {
                            Image(it, "Bild öffnen", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        }
                        if (shot.developing) CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                    }
                }
            }
        }

        Button(
            onClick = onCamera,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp).height(56.dp),
        ) { Text("Kamera", fontSize = 17.sp, fontWeight = FontWeight.Medium) }
    }
}
