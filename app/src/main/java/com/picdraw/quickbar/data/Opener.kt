package com.picdraw.quickbar.data

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Builds and fires the ACTION_VIEW intent that shows the target file.
 *
 * Two things matter for a cold tile process: the MIME type comes off disk rather than out of
 * memory, and the caller can tell whether this app still holds a persistable grant for the
 * URI. Without the grant the target app would be handed a URI it cannot read.
 */
object Opener {

    fun viewIntent(context: Context, shortcut: Shortcut): Intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(shortcut.parsedUri, mimeOf(context, shortcut))
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    /**
     * @return false when nothing on the device can show the file, so callers can surface a
     *         message instead of the system dying with [ActivityNotFoundException].
     */
    fun open(context: Context, shortcut: Shortcut): Boolean = try {
        context.startActivity(viewIntent(context, shortcut).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        true
    } catch (_: ActivityNotFoundException) {
        false
    } catch (_: SecurityException) {
        false
    }

    /**
     * A persistable grant from the document picker is what survives this process being
     * killed. Transient grants do not, which is exactly why a tile works while the app is
     * open and stops working after the app is closed.
     */
    fun hasPersistedAccess(context: Context, shortcut: Shortcut): Boolean =
        runCatching {
            context.contentResolver.persistedUriPermissions.any {
                it.uri == shortcut.parsedUri && it.isReadPermission
            }
        }.getOrDefault(false)

    /**
     * Prefers the persisted MIME, then asks the provider, then falls back to the target type.
     *
     * The middle step covers shortcuts stored before the MIME was persisted, and providers
     * that only report a concrete type on query. A wildcard is never sent while a concrete
     * type is obtainable, because many viewers do not accept a top-level wildcard.
     */
    private fun mimeOf(context: Context, shortcut: Shortcut): String {
        if (shortcut.mimeType.isConcrete()) return shortcut.mimeType
        val queried = runCatching { context.contentResolver.getType(shortcut.parsedUri) }.getOrNull()
        if (queried.isConcrete()) return queried!!
        return defaultMimeType(shortcut.targetType)
    }

    private fun String?.isConcrete(): Boolean = !isNullOrBlank() && !endsWith("/*") && this != "*/*"
}
