package com.picdraw.quickbar.data

import android.app.StatusBarManager
import android.content.ComponentName
import android.content.Context
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.TileService
import androidx.core.content.ContextCompat

/**
 * Wraps [StatusBarManager.requestAddTileService], which shows the system sheet that adds a
 * tile to the shade. On HyperOS the alternative is hunting for the edit button, so this is
 * the only practical way to get the tiles in front of the user.
 */
object TileAdder {

    /** Reported through the callback when the platform is too old to offer the sheet. */
    const val UNSUPPORTED = -1

    val isSupported: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    /**
     * @param labelRes manifest label of the tile
     * @param iconRes  drawable shown in the system sheet
     * @param onResult receives a [StatusBarManager] tile-add result code, or [UNSUPPORTED]
     */
    fun request(
        context: Context,
        service: Class<out TileService>,
        labelRes: Int,
        iconRes: Int,
        onResult: (Int) -> Unit,
    ) {
        if (!isSupported) {
            onResult(UNSUPPORTED)
            return
        }
        val statusBar = context.getSystemService(StatusBarManager::class.java)
        if (statusBar == null) {
            onResult(UNSUPPORTED)
            return
        }
        statusBar.requestAddTileService(
            Tiles.componentName(context, service),
            context.getString(labelRes),
            Icon.createWithResource(context, iconRes),
            ContextCompat.getMainExecutor(context),
            onResult,
        )
    }
}

/** Maps a tile component name back to the slot it drives, or null for the panel tile. */
fun slotForTileComponent(component: ComponentName?): Int? {
    val className = component?.className ?: return null
    return Tiles.directServices.indexOfFirst { it.name == className }.takeIf { it >= 0 }
}
