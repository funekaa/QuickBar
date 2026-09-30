package com.picdraw.quickbar.data

import android.content.ComponentName
import android.content.Context
import android.service.quicksettings.TileService
import com.picdraw.quickbar.R
import com.picdraw.quickbar.tiles.DirectTileService1
import com.picdraw.quickbar.tiles.DirectTileService2
import com.picdraw.quickbar.tiles.DirectTileService3
import com.picdraw.quickbar.tiles.DirectTileService4
import com.picdraw.quickbar.tiles.DirectTileService5
import com.picdraw.quickbar.tiles.DirectTileService6
import com.picdraw.quickbar.tiles.QuickBarTileService

/** Central registry of every tile this app publishes. */
object Tiles {
    val panelService: Class<out TileService> = QuickBarTileService::class.java

    /** Indexed by slot number minus one. */
    val directServices: List<Class<out TileService>> = listOf(
        DirectTileService1::class.java,
        DirectTileService2::class.java,
        DirectTileService3::class.java,
        DirectTileService4::class.java,
        DirectTileService5::class.java,
        DirectTileService6::class.java,
    )

    val all: List<Class<out TileService>> = listOf(panelService) + directServices

    /** Indexed by slot number minus one. Fixed per slot: a tile icon cannot change at runtime. */
    val slotIconRes: List<Int> = listOf(
        R.drawable.ic_tile_photo,
        R.drawable.ic_tile_video,
        R.drawable.ic_tile_folder,
        R.drawable.ic_tile_star,
        R.drawable.ic_tile_link,
        R.drawable.ic_tile_camera,
    )
    /** Indexed by slot number minus one. */
    val slotLabelRes: List<Int> = listOf(
        R.string.tile_slot_1_label,
        R.string.tile_slot_2_label,
        R.string.tile_slot_3_label,
        R.string.tile_slot_4_label,
        R.string.tile_slot_5_label,
        R.string.tile_slot_6_label,
    )

    fun componentName(context: Context, service: Class<out TileService>): ComponentName =
        ComponentName(context, service)
}
