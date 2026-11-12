package app.silberkorn.ui

import android.Manifest
import android.content.pm.PackageManager
import android.view.OrientationEventListener
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import app.silberkorn.CameraController
import app.silberkorn.Darkroom
import app.silberkorn.Lens
import app.silberkorn.Library
import app.silberkorn.Settings
import app.silberkorn.core.Aspect
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun CameraScreen(onGallery: () -> Unit, onSettings: () -> Unit, onOpen: (String) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs by Settings.prefs.collectAsState()
    val shots by Library.shots.collectAsState()

    var granted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val askPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted = it }
    LaunchedEffect(Unit) { if (!granted) askPermission.launch(Manifest.permission.CAMERA) }

    val flash = remember { Animatable(0f) }
    val controller = remember {
        CameraController(
            context,
            onShot = { shot -> Darkroom.enqueue(shot.id, export = Settings.prefs.value.autoExport) },
            onError = { msg ->
                ContextCompat.getMainExecutor(context).execute { Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() }
            },
        )
    }
    DisposableEffect(Unit) { onDispose { controller.release() } }

    // How the phone is being held, so pictures come out upright even though the UI stays portrait.
    var rotation by remember { mutableIntStateOf(0) }
    DisposableEffect(Unit) {
        val listener = object : OrientationEventListener(context) {
            override fun onOrientationChanged(degrees: Int) {
                if (degrees != ORIENTATION_UNKNOWN) rotation = ((degrees + 45) / 90 * 90) % 360
            }
        }
        listener.enable()
        onDispose { listener.disable() }
    }

    val lenses = remember(granted) { if (granted) runCatching { controller.lenses }.getOrDefault(emptyList()) else emptyList() }
    val lens: Lens? = lenses.firstOrNull { it.id == prefs.lastLens } ?: lenses.firstOrNull { !it.front } ?: lenses.firstOrNull()
    var ev by remember(lens?.id) { mutableFloatStateOf(0f) }
    var countdown by remember { mutableIntStateOf(0) }

    fun shoot() {
        if (lens == null || countdown > 0 || controller.busy) return
        scope.launch {
            countdown = prefs.timerSeconds
            while (countdown > 0) {
                delay(1000)
                countdown--
            }
            controller.capture(rotation)
            flash.snapTo(1f)
            flash.animateTo(0f, tween(260))
        }
    }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = {
                Settings.update { it.copy(timerSeconds = when (it.timerSeconds) { 0 -> 3; 3 -> 10; else -> 0 }) }
            }) {
                Text(if (prefs.timerSeconds == 0) "Timer aus" else "Timer ${prefs.timerSeconds} s",
                    color = if (prefs.timerSeconds == 0) Dim else Safelight)
            }
            TextButton(onClick = { Settings.update { it.copy(grid = !it.grid) } }) {
                Text("Raster", color = if (prefs.grid) Safelight else Dim)
            }
            TextButton(onClick = {
                Settings.update { p ->
                    p.copy(defaultLook = p.defaultLook.copy(aspect = (p.defaultLook.aspect + 1) % Aspect.ALL.size))
                }
            }) {
                Text(Aspect.ALL[prefs.defaultLook.aspect].label, color = if (prefs.defaultLook.aspect == 0) Dim else Safelight)
            }
            Spacer(Modifier.weight(1f))
            if (lens != null && !lens.raw) Text("kein RAW", color = Dim, fontSize = 12.sp)
            IconButton(onClick = onSettings) { Icon(Icons.Default.Settings, "Einstellungen", tint = Paper) }
        }

        Box(Modifier.fillMaxWidth().aspectRatio(3f / 4f).background(Color.Black), contentAlignment = Alignment.Center) {
            if (!granted) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                    Text("Silberkorn braucht Zugriff auf die Kamera.", color = Paper, textAlign = TextAlign.Center)
                    Spacer(Modifier.size(16.dp))
                    Button(onClick = { askPermission.launch(Manifest.permission.CAMERA) }) { Text("Kamera erlauben") }
                }
            } else if (lens == null) {
                Text("Keine Kamera gefunden.", color = Paper)
            } else {
                key(lens.id) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            SurfaceView(ctx).apply {
                                holder.setFixedSize(lens.previewSize.width, lens.previewSize.height)
                                holder.addCallback(object : SurfaceHolder.Callback {
                                    override fun surfaceCreated(h: SurfaceHolder) {}
                                    override fun surfaceChanged(h: SurfaceHolder, format: Int, w: Int, ht: Int) {
                                        if (w == lens.previewSize.width && ht == lens.previewSize.height) {
                                            controller.open(lens, h.surface)
                                        }
                                    }
                                    override fun surfaceDestroyed(h: SurfaceHolder) {
                                        controller.close(h.surface)
                                    }
                                })
                            }
                        },
                    )
                }
                FrameOverlay(Aspect.ALL[prefs.defaultLook.aspect].ratio, prefs.grid)
            }
            if (flash.value > 0f) Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = flash.value)))
            if (countdown > 0) Text("$countdown", color = Paper, fontSize = 96.sp, fontWeight = FontWeight.Light)
        }

        Spacer(Modifier.weight(1f))

        if (lens != null && lens.evStep > 0f && lens.evRange.last > lens.evRange.first) {
            val limit = minOf(lens.evRange.last * lens.evStep, 3f)
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    (if (ev > 0.04f) "+" else "") + String.format("%.1f", ev).replace('.', ',') + " EV",
                    color = if (ev == 0f) Dim else Safelight, fontSize = 13.sp,
                    modifier = Modifier.width(64.dp).clickable {
                        ev = 0f
                        controller.setExposureCompensation(0)
                    },
                )
                Slider(
                    value = ev, valueRange = -limit..limit,
                    onValueChange = {
                        ev = it
                        controller.setExposureCompensation((it / lens.evStep).roundToInt())
                    },
                    modifier = Modifier.weight(1f).semantics { contentDescription = "Belichtungskorrektur" },
                )
            }
        }

        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.Center) {
            lenses.filter { !it.front }.takeIf { it.size > 1 }?.forEach { l ->
                val selected = l.id == lens?.id
                Text(
                    l.label, color = if (selected) Ink else Paper, fontSize = 14.sp, fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 6.dp).clip(CircleShape)
                        .background(if (selected) Paper else Tray)
                        .clickable { Settings.update { it.copy(lastLens = l.id) } }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                )
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(start = 28.dp, end = 28.dp, top = 8.dp, bottom = 20.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            val latest = shots.firstOrNull()
            Box(
                Modifier.size(56.dp).clip(RoundedCornerShape(10.dp)).background(Tray)
                    .clickable(onClickLabel = "Galerie öffnen", onClick = onGallery),
                contentAlignment = Alignment.Center,
            ) {
                if (latest != null) {
                    rememberPreview(latest, 160)?.let {
                        Image(it, "Letztes Bild", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    }
                    if (latest.developing) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    Text("Galerie", color = Dim, fontSize = 11.sp)
                }
            }
            Box(
                Modifier.size(78.dp).clip(CircleShape).border(3.dp, Paper, CircleShape).padding(7.dp)
                    .clip(CircleShape).background(if (countdown > 0) Safelight else Paper)
                    .clickable(onClickLabel = "Auslösen") { shoot() },
            )
            IconButton(
                onClick = {
                    val target = lenses.firstOrNull { it.front != (lens?.front == true) }
                    if (target != null) Settings.update { it.copy(lastLens = target.id) }
                },
                modifier = Modifier.size(56.dp).clip(CircleShape).background(Tray),
            ) { Icon(Icons.Default.Refresh, "Kamera wechseln", tint = Paper) }
        }
    }
}

/** Darkens what the chosen format will crop away and optionally draws thirds. */
@Composable
private fun FrameOverlay(ratio: Float, grid: Boolean) {
    Canvas(Modifier.fillMaxSize()) {
        var w = size.width
        var h = size.height
        if (ratio > 0f) {
            if (ratio < h / w) h = w * ratio else w = h / ratio
        }
        val left = (size.width - w) / 2
        val top = (size.height - h) / 2
        val shade = Color.Black.copy(alpha = 0.72f)
        if (top > 0f) {
            drawRect(shade, Offset.Zero, Size(size.width, top))
            drawRect(shade, Offset(0f, top + h), Size(size.width, top))
        }
        if (left > 0f) {
            drawRect(shade, Offset.Zero, Size(left, size.height))
            drawRect(shade, Offset(left + w, 0f), Size(left, size.height))
        }
        if (grid) {
            val line = Color.White.copy(alpha = 0.35f)
            for (i in 1..2) {
                drawLine(line, Offset(left + w * i / 3, top), Offset(left + w * i / 3, top + h), 1.dp.toPx())
                drawLine(line, Offset(left, top + h * i / 3), Offset(left + w, top + h * i / 3), 1.dp.toPx())
            }
        }
    }
}
