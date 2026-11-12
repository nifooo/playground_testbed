package app.silberkorn

import android.content.Context
import android.content.SharedPreferences
import app.silberkorn.core.Look
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject

data class Prefs(
    val autoExport: Boolean = false,
    val saveDng: Boolean = false,
    val jpegQuality: Int = 95,
    val grid: Boolean = false,
    val timerSeconds: Int = 0,
    val lastLens: String? = null,
    /** Starting point for every new picture, including the viewfinder crop. */
    val defaultLook: Look = Look(),
)

object Settings {
    private lateinit var store: SharedPreferences
    private val state = MutableStateFlow(Prefs())
    val prefs: StateFlow<Prefs> = state

    fun init(context: Context) {
        if (::store.isInitialized) return
        store = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        state.value = Prefs(
            autoExport = store.getBoolean("autoExport", false),
            saveDng = store.getBoolean("saveDng", false),
            jpegQuality = store.getInt("jpegQuality", 95),
            grid = store.getBoolean("grid", false),
            timerSeconds = store.getInt("timerSeconds", 0),
            lastLens = store.getString("lastLens", null),
            defaultLook = store.getString("defaultLook", null)
                ?.let { runCatching { lookFromJson(JSONObject(it)) }.getOrNull() } ?: Look(),
        )
    }

    fun update(change: (Prefs) -> Prefs) {
        val p = change(state.value)
        state.value = p
        store.edit()
            .putBoolean("autoExport", p.autoExport)
            .putBoolean("saveDng", p.saveDng)
            .putInt("jpegQuality", p.jpegQuality)
            .putBoolean("grid", p.grid)
            .putInt("timerSeconds", p.timerSeconds)
            .putString("lastLens", p.lastLens)
            .putString("defaultLook", p.defaultLook.toJson().toString())
            .apply()
    }
}
