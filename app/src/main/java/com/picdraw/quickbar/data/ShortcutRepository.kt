package com.picdraw.quickbar.data

import android.content.Context
import android.service.quicksettings.TileService
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

/**
 * Single source of truth for shortcuts and slot bindings, persisted as a small JSON
 * document in internal storage. Writes are synchronous so a tile click always sees the
 * state that was just saved from the main screen.
 */
class ShortcutRepository private constructor(private val appContext: Context) {

    private val file = File(appContext.filesDir, FILE_NAME)
    private val _state = MutableStateFlow(readFromDisk())
    val state: StateFlow<QuickBarState> = _state.asStateFlow()

    @Synchronized
    fun upsert(shortcut: Shortcut) {
        val current = _state.value
        val updated = if (current.shortcuts.any { it.id == shortcut.id }) {
            current.copy(shortcuts = current.shortcuts.map { if (it.id == shortcut.id) shortcut else it })
        } else {
            current.copy(shortcuts = current.shortcuts + shortcut)
        }
        commit(updated)
    }

    @Synchronized
    fun delete(id: String) {
        val current = _state.value
        val bindings = current.slotBindings.map { if (it == id) null else it }
        commit(current.copy(shortcuts = current.shortcuts.filterNot { it.id == id }, slotBindings = bindings))
    }

    @Synchronized
    fun bind(slot: Int, shortcutId: String?) {
        val current = _state.value
        if (slot !in 0 until DirectSlotCount) return
        val bindings = current.slotBindings.toMutableList()
        bindings[slot] = shortcutId
        commit(current.copy(slotBindings = bindings))
    }

    private fun commit(next: QuickBarState) {
        _state.value = sanitize(next)
        writeToDisk(_state.value)
        requestTileRefresh()
    }

    private fun requestTileRefresh() {
        // Tiles read the in-memory state that was just committed, so the refresh only needs to
        // reach SystemUI. Done off the main thread to keep binder I/O off the UI.
        Thread {
            Tiles.all.forEach { service ->
                runCatching {
                    TileService.requestListeningState(appContext, Tiles.componentName(appContext, service))
                }
            }
        }.apply { isDaemon = true }.start()
    }

    private fun sanitize(state: QuickBarState): QuickBarState {
        val ids = state.shortcuts.map { it.id }.toSet()
        val bindings = List(DirectSlotCount) { state.slotBindings.getOrNull(it)?.takeIf { id -> id in ids } }
        return state.copy(shortcuts = state.shortcuts, slotBindings = bindings)
    }

    private fun readFromDisk(): QuickBarState {
        if (!file.exists()) return QuickBarState()
        return runCatching {
            val root = JSONObject(file.readText())
            val shortcuts = root.optJSONArray(KEY_SHORTCUTS).mapObjects(::readShortcut)
            val bindings = root.optJSONArray(KEY_SLOTS)?.let { array ->
                List(DirectSlotCount) { index -> array.optString(index).takeIf { it.isNotEmpty() && it != "null" } }
            } ?: List(DirectSlotCount) { null }
            QuickBarState(shortcuts, bindings)
        }.getOrElse {
            Log.w(TAG, "Could not read $FILE_NAME, starting empty", it)
            QuickBarState()
        }
    }

    private fun writeToDisk(state: QuickBarState) {
        runCatching {
            val root = JSONObject().apply {
                put(KEY_SHORTCUTS, JSONArray().apply { state.shortcuts.forEach { put(writeShortcut(it)) } })
                put(KEY_SLOTS, JSONArray().apply { state.slotBindings.forEach { put(it ?: JSONObject.NULL) } })
            }
            val temp = File(file.parentFile, "$FILE_NAME.tmp")
            temp.writeText(root.toString())
            if (file.exists()) file.delete()
            temp.renameTo(file)
        }.onFailure { Log.w(TAG, "Could not write $FILE_NAME", it) }
    }

    private fun readShortcut(obj: JSONObject): Shortcut? {
        val id = obj.optString("id").takeIf { it.isNotEmpty() } ?: return null
        val uri = obj.optString("uri").takeIf { it.isNotEmpty() } ?: return null
        val type = runCatching { TargetType.valueOf(obj.optString("type", TargetType.UNKNOWN.name)) }
            .getOrDefault(TargetType.UNKNOWN)
        return Shortcut(
            id = id,
            title = obj.optString("title"),
            uri = uri,
            targetType = type,
            iconKey = obj.optString("icon").takeIf { it.isNotEmpty() } ?: IconCatalog.DEFAULT_KEY,
            showInPanel = obj.optBoolean("panel", true),
        )
    }

    private fun writeShortcut(shortcut: Shortcut): JSONObject = JSONObject().apply {
        put("id", shortcut.id)
        put("title", shortcut.title)
        put("uri", shortcut.uri)
        put("type", shortcut.targetType.name)
        put("icon", shortcut.iconKey)
        put("panel", shortcut.showInPanel)
    }

    companion object {
        private const val TAG = "ShortcutRepository"
        private const val FILE_NAME = "quickbar.json"
        private const val KEY_SHORTCUTS = "shortcuts"
        private const val KEY_SLOTS = "slots"

        @Volatile
        private var instance: ShortcutRepository? = null

        fun get(context: Context): ShortcutRepository =
            instance ?: synchronized(this) {
                instance ?: ShortcutRepository(context.applicationContext).also { instance = it }
            }
    }
}

fun newShortcutId(): String = UUID.randomUUID().toString()


private fun <T> JSONArray?.mapObjects(transform: (JSONObject) -> T?): List<T> {
    if (this == null) return emptyList()
    return buildList {
        for (index in 0 until length()) {
            optJSONObject(index)?.let(transform)?.let(::add)
        }
    }
}
