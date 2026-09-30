package com.picdraw.quickbar.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.picdraw.quickbar.R
import com.picdraw.quickbar.data.IconCatalog
import com.picdraw.quickbar.data.Shortcut
import com.picdraw.quickbar.data.TileAdder

/** Binds a shortcut to one of the six direct quick settings tiles. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotBindingSheet(
    slotLabel: String,
    shortcuts: List<Shortcut>,
    boundId: String?,
    onBind: (String) -> Unit,
    onUnbind: () -> Unit,
    onAddTile: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            Text(text = slotLabel, style = MaterialTheme.typography.titleMedium)
            Text(
                text = stringResource(R.string.slot_empty_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
            )

            if (TileAdder.isSupported) {
                OutlinedButton(
                    onClick = {
                        onAddTile()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.action_add_tile))
                }
            }

            if (shortcuts.isEmpty()) {
                Text(
                    text = stringResource(R.string.empty_shortcuts),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 24.dp),
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp),
                ) {
                    items(shortcuts, key = { it.id }) { shortcut ->
                        BindRow(
                            shortcut = shortcut,
                            selected = shortcut.id == boundId,
                            onClick = {
                                onBind(shortcut.id)
                                onDismiss()
                            },
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 20.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                if (boundId != null) {
                    TextButton(onClick = {
                        onUnbind()
                        onDismiss()
                    }) { Text(stringResource(R.string.action_unbind)) }
                }
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_close)) }
            }
        }
    }
}

@Composable
private fun BindRow(shortcut: Shortcut, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(13.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = IconCatalog.entry(shortcut.iconKey).vector,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(21.dp),
                )
            }
        }
        Spacer(Modifier.width(14.dp))
        Text(
            text = shortcut.title,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            modifier = Modifier.weight(1f),
        )
        if (selected) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = stringResource(R.string.action_bound),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
