package app.silberkorn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import app.silberkorn.ui.CameraScreen
import app.silberkorn.ui.EditScreen
import app.silberkorn.ui.GalleryScreen
import app.silberkorn.ui.Ink
import app.silberkorn.ui.SettingsScreen
import app.silberkorn.ui.SilberkornTheme

private sealed interface Screen {
    data object Camera : Screen
    data object Gallery : Screen
    data object Settings : Screen
    data class Edit(val id: String, val from: Screen) : Screen
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Library.init(this)
        Settings.init(this)
        Darkroom.init(this)
        enableEdgeToEdge()
        setContent {
            SilberkornTheme {
                var screen by remember { mutableStateOf<Screen>(Screen.Camera) }
                var settingsFrom by remember { mutableStateOf<Screen>(Screen.Camera) }
                Box(Modifier.fillMaxSize().background(Ink).safeDrawingPadding()) {
                    when (val s = screen) {
                        Screen.Camera -> CameraScreen(
                            onGallery = { screen = Screen.Gallery },
                            onSettings = { settingsFrom = s; screen = Screen.Settings },
                            onOpen = { screen = Screen.Edit(it, Screen.Camera) },
                        )
                        Screen.Gallery -> {
                            BackHandler { screen = Screen.Camera }
                            GalleryScreen(
                                onCamera = { screen = Screen.Camera },
                                onSettings = { settingsFrom = s; screen = Screen.Settings },
                                onOpen = { screen = Screen.Edit(it, Screen.Gallery) },
                            )
                        }
                        Screen.Settings -> {
                            BackHandler { screen = settingsFrom }
                            SettingsScreen(onBack = { screen = settingsFrom })
                        }
                        is Screen.Edit -> EditScreen(
                            id = s.id,
                            onBack = { screen = Screen.Gallery },
                            onCamera = { screen = Screen.Camera },
                        )
                    }
                }
            }
        }
    }
}
