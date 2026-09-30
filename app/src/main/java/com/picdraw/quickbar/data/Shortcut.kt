package com.picdraw.quickbar.data

import android.net.Uri

enum class TargetType { IMAGE, VIDEO, UNKNOWN }

data class Shortcut(
    val id: String,
    val title: String,
    val uri: String,
    val targetType: TargetType,
    val iconKey: String,
    /**
     * Persisted on purpose. The tile runs in a cold process, so the MIME type cannot live in
     * memory: falling back to a wildcard would make many viewers reject the file.
     */
    val mimeType: String = defaultMimeType(TargetType.UNKNOWN),
    val showInPanel: Boolean = true,
) {
    val parsedUri: Uri get() = Uri.parse(uri)
}

/**
 * The whole persisted state. [slotBindings] is indexed the same way as
 * [DirectTileService] slot numbers: index 0 is slot 1, and so on. A null entry
 * means the slot is not bound to any shortcut yet.
 */
data class QuickBarState(
    val shortcuts: List<Shortcut> = emptyList(),
    val slotBindings: List<String?> = List(DirectSlotCount) { null },
) {
    fun byId(id: String?): Shortcut? = id?.let { key -> shortcuts.firstOrNull { it.id == key } }

    fun shortcutForSlot(slot: Int): Shortcut? = byId(slotBindings.getOrNull(slot))

    fun panelShortcuts(): List<Shortcut> = shortcuts.filter { it.showInPanel }
}

const val DirectSlotCount = 6
