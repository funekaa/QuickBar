package com.picdraw.quickbar.data

import android.content.Context
import android.content.pm.PackageManager
import android.service.quicksettings.TileService

/**
 * Shows or hides one of this app's tiles in the shade.
 *
 * Android exposes no API for an app to remove a tile the user already added, so the tile's
 * service component is enabled or disabled instead. SystemUI drops the tile from the panel
 * when the component disappears, and enabling it again makes the tile available to add back.
 *
 * The component state is the single source of truth here rather than a persisted flag, so it
 * cannot drift from what the system actually shows. A reinstall resets it to the default,
 * which counts as visible.
 */
object TileAvailability {

    fun isVisible(context: Context, service: Class<out TileService>): Boolean =
        stateOf(context, service) != PackageManager.COMPONENT_ENABLED_STATE_DISABLED

    fun setVisible(context: Context, service: Class<out TileService>, visible: Boolean) {
        val next = if (visible) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }
        context.packageManager.setComponentEnabledSetting(
            Tiles.componentName(context, service),
            next,
            PackageManager.DONT_KILL_APP,
        )
    }

    private fun stateOf(context: Context, service: Class<out TileService>): Int =
        context.packageManager.getComponentEnabledSetting(Tiles.componentName(context, service))
}
