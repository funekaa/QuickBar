package com.picdraw.quickbar.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

/**
 * The built-in viewer reads the file itself, so the only thing that matters about access is
 * whether the grant from the document picker survived.
 *
 * A persistable grant outlives the process; the transient one that comes with
 * ACTION_OPEN_DOCUMENT does not. That distinction is why a tile works while the app is open
 * and stops working after the app is closed.
 */
object FileAccess {

    fun hasPersistedAccess(context: Context, shortcut: Shortcut): Boolean =
        hasPersistedAccess(context, shortcut.parsedUri)

    fun hasPersistedAccess(context: Context, uri: Uri): Boolean =
        runCatching {
            context.contentResolver.persistedUriPermissions.any {
                it.uri == uri && it.isReadPermission
            }
        }.getOrDefault(false)
}

fun defaultMimeType(type: TargetType): String = when (type) {
    TargetType.IMAGE -> "image/"
    TargetType.VIDEO -> "video/"
    TargetType.UNKNOWN -> "*"
}

/**
 * Resolves the target type from the picked content URI, falling back to the display name.
 *
 * A wildcard MIME still identifies the kind, so it is accepted here even though it would be
 * useless for launching an external viewer.
 */
fun resolveTargetType(context: Context, uri: Uri): TargetType {
    val mime = context.contentResolver.getType(uri).orEmpty()
    if (mime.startsWith("image/", ignoreCase = true)) return TargetType.IMAGE
    if (mime.startsWith("video/", ignoreCase = true)) return TargetType.VIDEO

    val name = queryDisplayName(context, uri).orEmpty().lowercase()
    return when {
        IMAGE_EXTENSIONS.any { name.endsWith(it) } -> TargetType.IMAGE
        VIDEO_EXTENSIONS.any { name.endsWith(it) } -> TargetType.VIDEO
        else -> TargetType.UNKNOWN
    }
}

fun queryDisplayName(context: Context, uri: Uri): String? = runCatching {
    context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) cursor.getString(0) else null
    }
}.getOrNull()

private val IMAGE_EXTENSIONS = listOf(".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp", ".heic", ".heif")
private val VIDEO_EXTENSIONS = listOf(".mp4", ".mkv", ".mov", ".avi", ".webm", ".3gp", ".m4v", ".ts")