package com.picdraw.quickbar.panel

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.picdraw.quickbar.R
import com.picdraw.quickbar.data.Opener
import com.picdraw.quickbar.data.ShortcutRepository

/**
 * Trampoline that turns a tile tap into a foreground activity, which then hands the file to
 * a viewer.
 *
 * MIUI and HyperOS block background activity launches from a bound TileService, so a direct
 * `startActivity` on the click only works while the app happens to be open. Routing the tap
 * through here, launched by the system via a PendingIntent, puts the launch in the foreground
 * where the restriction does not apply.
 */
@SuppressLint("CustomSplashScreen")
class ShortcutLaunchActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shortcut = ShortcutRepository.get(this).state.value.byId(intent.getStringExtra(EXTRA_SHORTCUT_ID))

        when {
            shortcut == null -> toast(getString(R.string.error_shortcut_missing))

            !Opener.hasPersistedAccess(this, shortcut) -> {
                // The document picker's grant did not survive; the file has to be picked again.
                toast(getString(R.string.error_persist_permission))
            }

            !Opener.open(this, shortcut) -> toast(getString(R.string.error_no_viewer))
        }

        finish()
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    companion object {
        const val EXTRA_SHORTCUT_ID = "com.picdraw.quickbar.extra.SHORTCUT_ID"

        fun intent(context: Context, shortcutId: String): Intent =
            Intent(context, ShortcutLaunchActivity::class.java).putExtra(EXTRA_SHORTCUT_ID, shortcutId)
    }
}
