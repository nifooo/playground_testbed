package app.silberkorn.ui

import android.graphics.BitmapFactory
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import app.silberkorn.Library
import app.silberkorn.Shot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

val Ink = Color(0xFF0E0C0A)
val Tray = Color(0xFF1A1612)
val Paper = Color(0xFFE9DFCF)
val Dim = Color(0xFF8C8275)
val Safelight = Color(0xFFFF5A36)

@Composable
fun SilberkornTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Safelight,
            onPrimary = Ink,
            secondary = Paper,
            background = Ink,
            onBackground = Paper,
            surface = Ink,
            onSurface = Paper,
            surfaceVariant = Tray,
            onSurfaceVariant = Dim,
            surfaceContainer = Tray,
            surfaceContainerHigh = Tray,
            outline = Dim,
        ),
        content = content,
    )
}

/**
 * The developed preview of a shot, decoded off the main thread. While a new
 * version loads the previous one stays on screen.
 */
@Composable
fun rememberPreview(shot: Shot, maxSide: Int): ImageBitmap? {
    var image by remember(shot.id) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(shot.id, shot.version) {
        if (shot.version == 0) return@LaunchedEffect
        image = withContext(Dispatchers.IO) {
            val path = Library.previewFile(shot.id).path
            val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(path, opts)
            var sample = 1
            while (maxOf(opts.outWidth, opts.outHeight) / (sample * 2) >= maxSide) sample *= 2
            BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })?.asImageBitmap()
        } ?: image
    }
    return image
}
