package com.picdraw.quickbar.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.picdraw.quickbar.R
import com.picdraw.quickbar.data.FileAccess
import com.picdraw.quickbar.data.Shortcut
import com.picdraw.quickbar.data.ShortcutRepository
import com.picdraw.quickbar.data.TargetType
import com.picdraw.quickbar.ui.theme.QuickBarTheme

/**
 * The built-in viewer.
 *
 * Rendering the file in-process removes the whole class of problems that came with handing
 * the URI to another app: no MIME type has to match, no default app has to be remembered,
 * and no URI grant has to survive a trip through the system. It also means the tile cannot be
 * defeated by an uninstalled or non-exporting gallery.
 */
class ViewerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shortcut = ShortcutRepository.get(this)
            .state
            .value
            .byId(intent.getStringExtra(EXTRA_SHORTCUT_ID))

        setContent {
            QuickBarTheme {
                ViewerScreen(
                    shortcut = shortcut,
                    accessGranted = shortcut?.let { FileAccess.hasPersistedAccess(this, it) } == true,
                    onClose = { finish() },
                )
            }
        }
    }

    companion object {
        const val EXTRA_SHORTCUT_ID = "com.picdraw.quickbar.extra.VIEW_SHORTCUT_ID"

        fun intent(context: Context, shortcutId: String): Intent =
            Intent(context, ViewerActivity::class.java)
                .putExtra(EXTRA_SHORTCUT_ID, shortcutId)
    }
}

@Composable
private fun ViewerScreen(
    shortcut: Shortcut?,
    accessGranted: Boolean,
    onClose: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        when {
            shortcut == null -> {
                LaunchedEffect(Unit) { onClose() }
                ViewerMessage(stringResource(R.string.error_shortcut_missing))
            }

            !accessGranted -> {
                LaunchedEffect(Unit) { onClose() }
                ViewerMessage(stringResource(R.string.error_persist_permission))
            }

            shortcut.targetType == TargetType.VIDEO -> VideoPane(shortcut.parsedUri)
            else -> ImagePane(shortcut)
        }

        ViewerTopBar(title = shortcut?.title.orEmpty(), onClose = onClose)
    }
}

@Composable
private fun ViewerTopBar(title: String, onClose: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (title.isNotBlank()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 8.dp).weight(1f),
            )
        } else {
            Spacer(Modifier.weight(1f))
        }
        IconButton(onClick = onClose) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = stringResource(R.string.action_close),
                tint = Color.White,
            )
        }
    }
}

/** Pinch and double tap zoom, plus drag while zoomed in. */
@Composable
private fun ImagePane(shortcut: Shortcut) {
    val context = LocalContext.current
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    fun clampOffset(value: Offset, currentScale: Float, viewportWidth: Float, viewportHeight: Float): Offset {
        val maxX = viewportWidth * (currentScale - 1f) / 2f
        val maxY = viewportHeight * (currentScale - 1f) / 2f
        return Offset(value.x.coerceIn(-maxX, maxX), value.y.coerceIn(-maxY, maxY))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val next = (scale * zoom).coerceIn(1f, MAX_ZOOM)
                    scale = next
                    if (next <= 1f) {
                        offset = Offset.Zero
                    } else {
                        // Pixels of slack available once the image is scaled up.
                        offset = clampOffset(offset + pan, next, size.width.toFloat(), size.height.toFloat())
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        if (scale > 1f) {
                            scale = 1f
                            offset = Offset.Zero
                        } else {
                            scale = 3f
                        }
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
                .data(shortcut.parsedUri)
                .crossfade(true)
                .build(),
            contentDescription = shortcut.title,
            contentScale = ContentScale.Fit,
            loading = {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            },
            error = { ViewerMessage(stringResource(R.string.error_decode_failed)) },
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                },
        )
    }
}

@Composable
private fun VideoPane(uri: Uri) {
    AndroidView(
        factory = { context ->
            VideoView(context).apply {
                val controller = MediaController(context)
                controller.setAnchorView(this)
                setMediaController(controller)
                setVideoURI(uri)
            }
        },
        update = { videoView ->
            // duration is only valid once the media has been prepared.
            if (videoView.duration > 0 && !videoView.isPlaying) {
                videoView.start()
            }
        },
        onRelease = { videoView -> videoView.stopPlayback() },
        modifier = Modifier.fillMaxSize(),
    )
}

@Composable
private fun ViewerMessage(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            modifier = Modifier.padding(32.dp),
        )
    }
}

private const val MAX_ZOOM = 5f