package com.picdraw.quickbar.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.picdraw.quickbar.R

/**
 * MIUI and HyperOS ship a per-app "background pop-up" switch that silently blocks activity
 * launches coming from a bound TileService. The trampoline avoids it, but the switch is worth
 * having on anyway for the panel tile on older releases, so the app offers a way in.
 */
object RomCompat {

    private const val MIUI_APP_PERM_EDITOR = "miui.intent.action.APP_PERM_EDITOR"

    fun isXiaomi(): Boolean = Build.MANUFACTURER.contains("xiaomi", ignoreCase = true) ||
        Build.BRAND.contains("xiaomi", ignoreCase = true) ||
        Build.BRAND.contains("redmi", ignoreCase = true) ||
        Build.BRAND.contains("poco", ignoreCase = true)

    /** Best effort: MIUI's behaviour page when it exists, app details otherwise. */
    fun backgroundLaunchSettings(context: Context): Intent? {
        val miui = runCatching {
            Intent(MIUI_APP_PERM_EDITOR).apply {
                putExtra("extra_pkgname", context.packageName)
                putExtra("extra_title", context.getString(R.string.action_open_settings))
            }
        }.getOrNull()
        if (miui != null && context.canResolve(miui)) return miui
        return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
    }

    private fun Context.canResolve(intent: Intent): Boolean = runCatching {
        packageManager.resolveActivity(intent, 0) != null
    }.getOrDefault(false)
}
