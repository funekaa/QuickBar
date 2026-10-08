package com.picdraw.quickbar.tiles

import androidx.annotation.StringRes
import com.picdraw.quickbar.R
import com.picdraw.quickbar.data.ShortcutRepository
import com.picdraw.quickbar.ui.ViewerActivity

/**
 * One-tap access to a single bound shortcut.
 *
 * Each subclass is a separate manifest entry because Android binds a tile's icon, label and
 * component to the service declaration. [slot] is the index into
 * [com.picdraw.quickbar.data.QuickBarState.slotBindings].
 */
abstract class DirectTileService : BaseQuickBarTileService() {

    protected abstract val slot: Int

    @get:StringRes
    final override val defaultLabelRes: Int
        get() = when (slot) {
            0 -> R.string.tile_slot_1_label
            1 -> R.string.tile_slot_2_label
            2 -> R.string.tile_slot_3_label
            3 -> R.string.tile_slot_4_label
            4 -> R.string.tile_slot_5_label
            else -> R.string.tile_slot_6_label
        }

    override fun render() {
        val shortcut = ShortcutRepository.get(this).state.value.shortcutForSlot(slot)
        applyTile(
            shortcut?.title?.takeIf { it.isNotBlank() } ?: getString(defaultLabelRes),
            shortcut != null,
        )
    }

    override fun onClick() {
        val shortcut = ShortcutRepository.get(this).state.value.shortcutForSlot(slot) ?: run {
            render()
            return
        }
        // The viewer is a real activity launched by the system on our behalf, which is what
        // keeps this working while the app itself is closed on MIUI and HyperOS.
        startAndCollapse(ViewerActivity.intent(this, shortcut.id), REQUEST_CODE_BASE + slot)
    }

    private companion object {
        const val REQUEST_CODE_BASE = 200
    }
}

class DirectTileService1 : DirectTileService() {
    override val slot: Int = 0
}

class DirectTileService2 : DirectTileService() {
    override val slot: Int = 1
}

class DirectTileService3 : DirectTileService() {
    override val slot: Int = 2
}

class DirectTileService4 : DirectTileService() {
    override val slot: Int = 3
}

class DirectTileService5 : DirectTileService() {
    override val slot: Int = 4
}

class DirectTileService6 : DirectTileService() {
    override val slot: Int = 5
}
