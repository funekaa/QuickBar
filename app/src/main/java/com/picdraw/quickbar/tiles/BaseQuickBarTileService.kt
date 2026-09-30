package com.picdraw.quickbar.tiles

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.StringRes

/**
 * Shared plumbing for the quick settings tiles.
 *
 * Android reads a tile's icon from its manifest entry, so it cannot be swapped at runtime.
 * That is why the six direct tiles carry a fixed line icon each, and the panel tile is the
 * one that exposes the full 100-icon library.
 *
 * All tiles are declared in active mode. Android 13+ otherwise allows only three concurrent
 * bindings for third-party tiles, and tapping one of the remaining published tiles would
 * cold-start its service on every press.
 */
abstract class BaseQuickBarTileService : TileService() {

    /** Label shown when nothing is bound yet. */
    @get:StringRes
    protected abstract val defaultLabelRes: Int

    /** Pushes the current label and enabled state into the tile. */
    protected abstract fun render()

    // In active mode the service stays bound, so onTileAdded is the first chance to paint.
    override fun onTileAdded() = render()

    override fun onStartListening() = render()

    override fun onTileRemoved() = Unit

    /**
     * Pushes the current label and enabled state into the tile.
     *
     * The label is written on every API level, including 34+, where `setLabel` is marked
     * deprecated. It still takes effect there: a tile declared through `TileService` reads
     * its label back from the `Tile` object, so without this the shade would only ever show
     * the static manifest string.
     */
    @Suppress("DEPRECATION")
    protected fun applyTile(label: String, active: Boolean) {
        val tile: Tile = qsTile ?: return
        tile.label = label
        tile.state = if (active) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.updateTile()
    }

    /**
     * Fires [intent] and collapses the shade.
     *
     * From API 34 the collapse goes through a [PendingIntent], which the system fires on this
     * app's behalf. That is deliberate: MIUI and HyperOS refuse background activity launches,
     * and a fired PendingIntent is not attributed to this app, so a tile still opens a file
     * while the app itself is closed.
     *
     * The URI grant is carried by [intent]'s own FLAG_GRANT_READ_URI_PERMISSION. The
     * PendingIntent creation flags do not accept grant bits, and do not need to.
     */
    protected fun startAndCollapse(intent: Intent, requestCode: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val flags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            startActivityAndCollapse(PendingIntent.getActivity(this, requestCode, intent, flags))
        } else {
            collapseWithLegacyApi(intent)
        }
    }

    /**
     * The PendingIntent overload does not exist below API 34. This branch is unreachable on
     * versions where the call throws, hence the targeted suppression.
     */
    @Suppress("DEPRECATION", "StartActivityAndCollapseDeprecated")
    private fun collapseWithLegacyApi(intent: Intent) {
        startActivityAndCollapse(intent)
    }
}
