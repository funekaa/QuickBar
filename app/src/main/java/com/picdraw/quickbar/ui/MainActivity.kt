package com.picdraw.quickbar.ui

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.service.quicksettings.TileService
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.IntentCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.picdraw.quickbar.R
import com.picdraw.quickbar.data.DirectSlotCount
import com.picdraw.quickbar.data.IconCatalog
import com.picdraw.quickbar.data.Opener
import com.picdraw.quickbar.data.QuickBarState
import com.picdraw.quickbar.data.RomCompat
import com.picdraw.quickbar.data.Shortcut
import com.picdraw.quickbar.data.ShortcutRepository
import com.picdraw.quickbar.data.TargetType
import com.picdraw.quickbar.data.TileAdder
import com.picdraw.quickbar.data.TileAvailability
import com.picdraw.quickbar.data.Tiles
import com.picdraw.quickbar.data.defaultMimeType
import com.picdraw.quickbar.data.newShortcutId
import com.picdraw.quickbar.data.queryDisplayName
import com.picdraw.quickbar.data.resolveTargetType
import com.picdraw.quickbar.data.slotForTileComponent
import com.picdraw.quickbar.ui.theme.QuickBarTheme

class MainActivity : ComponentActivity() {

    private val repository by lazy { ShortcutRepository.get(this) }

    /**
     * Set by [onNewIntent] too, because the activity is singleTop and a long press on a tile
     * reuses this instance instead of recreating it.
     */
    private var requestedSlot = mutableStateOf<Int?>(null)

    private val picker = registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        uris.firstOrNull()?.let(::adoptPickedFile)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestedSlot.value = slotFromIntent(intent)
        setContent {
            QuickBarTheme {
                val state by repository.state.collectAsStateWithLifecycle()
                MainScreen(
                    state = state,
                    requestedSlot = requestedSlot,
                    onAdd = { picker.launch(arrayOf("image/*", "video/*")) },
                    onAddTile = ::requestAddTile,
                    isTileVisible = TileAvailability::isVisible,
                    onSetTileVisible = ::setTileVisible,
                    onSaveShortcut = repository::upsert,
                    onDeleteShortcut = repository::delete,
                    onBindSlot = repository::bind,
                    onOpenRomSettings = ::openRomSettings,
                )
            }
        }
    }

    private fun setTileVisible(service: Class<out TileService>, visible: Boolean) {
        TileAvailability.setVisible(this, service, visible)
        toast(getString(if (visible) R.string.tile_restored else R.string.tile_removed))
    }

    private fun openRomSettings() {
        val intent = RomCompat.backgroundLaunchSettings(this) ?: return
        runCatching { startActivity(intent) }
            .onFailure { toast(getString(R.string.error_open_settings)) }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        slotFromIntent(intent)?.let { requestedSlot.value = it }
    }

    // EXTRA_COMPONENT_NAME is a compile-time String constant, so the API 26 annotation that
    // lint reports is irrelevant here.
    @SuppressLint("InlinedApi")
    private fun slotFromIntent(intent: Intent?): Int? {
        if (intent?.action != TileService.ACTION_QS_TILE_PREFERENCES) return null
        val component = IntentCompat.getParcelableExtra(
            intent,
            Intent.EXTRA_COMPONENT_NAME,
            ComponentName::class.java,
        )
        return slotForTileComponent(component)
    }

    private fun requestAddTile(service: Class<out TileService>, labelRes: Int, iconRes: Int) {
        TileAdder.request(this, service, labelRes, iconRes) { result ->
            if (result == TileAdder.UNSUPPORTED) {
                toast(getString(R.string.error_tile_add_unsupported))
            }
        }
    }

    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    private fun adoptPickedFile(uri: Uri) {
        val mime = contentResolver.getType(uri).orEmpty()
        if (mime.isBlank()) {
            toast(getString(R.string.error_unknown_mime))
        }
        val persisted = runCatching {
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }.isSuccess
        if (!persisted) {
            // Without a persistable grant the tile stops working once this process dies.
            toast(getString(R.string.error_persist_permission))
        }
        repository.upsert(
            Shortcut(
                id = newShortcutId(),
                title = queryDisplayName(this, uri).orEmpty(),
                uri = uri.toString(),
                targetType = resolveTargetType(this, uri),
                iconKey = IconCatalog.DEFAULT_KEY,
                mimeType = mime.ifBlank { defaultMimeType(resolveTargetType(this, uri)) },
                showInPanel = true,
            ),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreen(
    state: QuickBarState,
    requestedSlot: State<Int?>,
    onAdd: () -> Unit,
    onAddTile: (Class<out TileService>, Int, Int) -> Unit,
    isTileVisible: (Context, Class<out TileService>) -> Boolean,
    onSetTileVisible: (Class<out TileService>, Boolean) -> Unit,
    onSaveShortcut: (Shortcut) -> Unit,
    onDeleteShortcut: (String) -> Unit,
    onBindSlot: (Int, String?) -> Unit,
    onOpenRomSettings: () -> Unit,
) {
    var editing by remember { mutableStateOf<Shortcut?>(null) }
    var bindingSlot by remember { mutableStateOf<Int?>(null) }
    val context = LocalContext.current

    // A long press on a tile arrives through onNewIntent after this screen is already up.
    LaunchedEffect(requestedSlot.value) {
        requestedSlot.value?.let { bindingSlot = it }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAdd,
                icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.action_add)) },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            item(key = "help") {
                HelpCard(onOpenRomSettings, isTileVisible)
            }

            item(key = "header_shortcuts") {
                SectionHeader(stringResource(R.string.title_shortcuts))
            }

            if (state.shortcuts.isEmpty()) {
                item(key = "empty") {
                    Text(
                        text = stringResource(R.string.empty_shortcuts),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp),
                    )
                }
            } else {
                items(state.shortcuts, key = { it.id }) { shortcut ->
                    ShortcutRow(
                        shortcut = shortcut,
                        boundSlotLabel = state.boundSlotLabelOf(shortcut.id),
                        onClick = { editing = shortcut },
                    )
                }
            }

            item(key = "header_slots") {
                SectionHeader(
                    text = stringResource(R.string.title_slots),
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
            item(key = "panel_slot") { PanelTileRow(state, isTileVisible, onSetTileVisible) }

            items(DirectSlotCount, key = { "slot_$it" }) { slot ->
                SlotRow(
                    slot = slot,
                    bound = state.shortcutForSlot(slot),
                    visible = isTileVisible(context, Tiles.directServices[slot]),
                    onVisibilityChange = { onSetTileVisible(Tiles.directServices[slot], it) },
                    onClick = { bindingSlot = slot },
                    onAddTile = onAddTile,
                )
            }
        }
    }

    editing?.let { target ->
        ShortcutEditSheet(
            shortcut = target,
            typeLabel = stringResource(R.string.label_type, stringResource(typeLabelRes(target.targetType))),
            onSave = onSaveShortcut,
            onDelete = {
                onDeleteShortcut(target.id)
                editing = null
            },
            onDismiss = { editing = null },
        )
    }

    bindingSlot?.let { slot ->
        SlotBindingSheet(
            slotLabel = stringResource(Tiles.slotLabelRes[slot]),
            shortcuts = state.shortcuts,
            boundId = state.slotBindings.getOrNull(slot),
            onBind = { onBindSlot(slot, it) },
            onUnbind = { onBindSlot(slot, null) },
            onAddTile = { onAddTile(Tiles.directServices[slot], Tiles.slotLabelRes[slot], Tiles.slotIconRes[slot]) },
            onDismiss = { bindingSlot = null },
        )
    }
}

@Composable
private fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(top = 12.dp, bottom = 4.dp),
    )
}

@Composable
private fun HelpCard(
    onOpenRomSettings: () -> Unit,
    isTileVisible: (Context, Class<out TileService>) -> Boolean,
) {
    val context = LocalContext.current
    val hiddenCount = Tiles.all.count { !isTileVisible(context, it) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(8.dp))
                Text(text = stringResource(R.string.title_help), style = MaterialTheme.typography.titleSmall)
            }
            Text(stringResource(R.string.help_intro), style = MaterialTheme.typography.bodyMedium)
            HelpStep(stringResource(R.string.help_step_1_1), stringResource(R.string.help_step_1_2))
            HelpStep(stringResource(R.string.help_step_2_1), stringResource(R.string.help_step_2_2))
            HelpStep(stringResource(R.string.help_step_3_1), stringResource(R.string.help_step_3_2))
            HorizontalDivider()
            Text(text = stringResource(R.string.help_remove_title), style = MaterialTheme.typography.labelLarge)
            Text(
                text = stringResource(R.string.help_remove_body),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (hiddenCount > 0) {
                OutlinedButton(onClick = {
                    Tiles.all.forEach { TileAvailability.setVisible(context, it, true) }
                }) {
                    Text(stringResource(R.string.action_restore_all_tiles))
                }
            }
            HorizontalDivider()
            Text(text = stringResource(R.string.help_note_title), style = MaterialTheme.typography.labelLarge)
            Text(
                text = stringResource(R.string.help_note_body),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (RomCompat.isXiaomi()) {
                HorizontalDivider()
                Text(
                    text = stringResource(R.string.help_xiaomi_title),
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(
                    text = stringResource(R.string.help_xiaomi_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedButton(onClick = onOpenRomSettings) {
                    Text(stringResource(R.string.action_open_settings))
                }
            }
        }
    }
}

@Composable
private fun HelpStep(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = title, style = MaterialTheme.typography.labelLarge)
        Text(
            text = body,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ShortcutRow(shortcut: Shortcut, boundSlotLabel: String?, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ShortcutIcon(iconKey = shortcut.iconKey, size = 44.dp)
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = shortcut.title.ifBlank { stringResource(R.string.default_shortcut_title) },
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = buildString {
                    append(stringResource(R.string.label_type, stringResource(typeLabelRes(shortcut.targetType))))
                    boundSlotLabel?.let {
                        append("  ·  ")
                        append(it)
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun PanelTileRow(
    state: QuickBarState,
    isTileVisible: (Context, Class<out TileService>) -> Boolean,
    onSetTileVisible: (Class<out TileService>, Boolean) -> Unit,
) {
    val context = LocalContext.current
    val service = Tiles.panelService
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .padding(vertical = 10.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TileIconBox(drawableRes = R.drawable.ic_tile_quickbar)
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = stringResource(R.string.slot_panel_title), style = MaterialTheme.typography.bodyLarge)
            Text(
                text = pluralStringResource(
                    R.plurals.panel_shortcut_count,
                    state.panelShortcuts().size,
                    state.panelShortcuts().size,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        TileVisibilitySwitch(
            checked = isTileVisible(context, service),
            label = stringResource(R.string.slot_panel_title),
            onCheckedChange = { onSetTileVisible(service, it) },
        )
    }
}

/**
 * A bare [Switch] carries no label, which leaves TalkBack announcing only "switch, on". The
 * row text is not picked up, so the description is set explicitly here.
 */
@Composable
private fun TileVisibilitySwitch(
    checked: Boolean,
    label: String,
    onCheckedChange: (Boolean) -> Unit,
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = Modifier.semantics { contentDescription = label },
    )
}

@Composable
private fun SlotRow(
    slot: Int,
    bound: Shortcut?,
    visible: Boolean,
    onVisibilityChange: (Boolean) -> Unit,
    onClick: () -> Unit,
    onAddTile: (Class<out TileService>, Int, Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TileIconBox(drawableRes = Tiles.slotIconRes[slot])
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = stringResource(Tiles.slotLabelRes[slot]), style = MaterialTheme.typography.bodyLarge)
            Text(
                text = if (bound == null) {
                    stringResource(R.string.tile_slot_unbound)
                } else {
                    stringResource(R.string.label_bound_to, bound.title)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        TileVisibilitySwitch(
            checked = visible,
            label = stringResource(R.string.tile_show_in_shade),
            onCheckedChange = onVisibilityChange,
        )
    }
}

@Composable
private fun ShortcutIcon(iconKey: String, size: Dp) {
    Surface(
        modifier = Modifier.size(size),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = IconCatalog.entry(iconKey).vector,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(size * 0.52f),
            )
        }
    }
}

@Composable
private fun TileIconBox(drawableRes: Int) {
    Surface(
        modifier = Modifier.size(44.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(drawableRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun QuickBarState.boundSlotLabelOf(shortcutId: String): String? =
    slotBindings.indexOfFirst { it == shortcutId }
        .takeIf { it >= 0 }
        ?.let { stringResource(Tiles.slotLabelRes[it]) }

private fun typeLabelRes(type: TargetType): Int = when (type) {
    TargetType.IMAGE -> R.string.type_image
    TargetType.VIDEO -> R.string.type_video
    TargetType.UNKNOWN -> R.string.type_unknown
}
