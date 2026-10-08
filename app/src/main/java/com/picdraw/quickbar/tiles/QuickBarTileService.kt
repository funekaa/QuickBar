package com.picdraw.quickbar.tiles

import android.content.Intent
import android.widget.Toast
import androidx.annotation.StringRes
import com.picdraw.quickbar.R
import com.picdraw.quickbar.data.ShortcutRepository
import com.picdraw.quickbar.panel.QuickBarPanelActivity

/** Expands the full shortcut grid over the shade. */
class QuickBarTileService : BaseQuickBarTileService() {

    override val defaultLabelRes: Int = R.string.tile_panel_label

    override fun render() {
        val state = ShortcutRepository.get(this).state.value
        applyTile(getString(defaultLabelRes), state.panelShortcuts().isNotEmpty())
    }

    override fun onClick() {
        val intent = Intent(this, QuickBarPanelActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        startAndCollapse(intent, REQUEST_CODE)
    }

    private companion object {
        const val REQUEST_CODE = 100
    }
}
