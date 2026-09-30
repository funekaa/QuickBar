package com.picdraw.quickbar.data

import androidx.annotation.StringRes
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.automirrored.outlined.LabelImportant
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.automirrored.outlined.PlaylistAdd
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.automirrored.outlined.StarHalf
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CameraRoll
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.ControlCamera
import androidx.compose.material.icons.outlined.Cottage
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.DownloadForOffline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.FolderCopy
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.FolderSpecial
import androidx.compose.material.icons.outlined.Grade
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Image as ImageIcon
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.LocationCity
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.MovieCreation
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.OndemandVideo
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material.icons.outlined.PermMedia
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material.icons.outlined.PhotoAlbum
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Radio
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Slideshow
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.Terrain
import androidx.compose.material.icons.outlined.Theaters
import androidx.compose.material.icons.outlined.Train
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Unarchive
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.outlined.VideoFile
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.ui.graphics.vector.ImageVector
import com.picdraw.quickbar.R

/**
 * A curated set of 100 outline icons from Material Icons (Apache-2.0), grouped so the
 * picker stays readable. Keys are persisted, so they must never change.
 */
data class IconEntry(val key: String, val vector: ImageVector)

data class IconGroup(@StringRes val titleRes: Int, val entries: List<IconEntry>)

object IconCatalog {

    val DEFAULT_KEY = "media_photo"

    private fun entry(key: String, vector: ImageVector) = IconEntry(key, vector)

    val groups: List<IconGroup> = listOf(
        IconGroup(
            R.string.icon_group_media,
            listOf(
                entry("media_image", Icons.Outlined.ImageIcon),
                entry("media_photo", Icons.Outlined.Photo),
                entry("media_photo_album", Icons.Outlined.PhotoAlbum),
                entry("media_photo_camera", Icons.Outlined.PhotoCamera),
                entry("media_photo_library", Icons.Outlined.PhotoLibrary),
                entry("media_collections", Icons.Outlined.Collections),
                entry("media_collections_bookmark", Icons.Outlined.CollectionsBookmark),
                entry("media_perm_media", Icons.Outlined.PermMedia),
                entry("media_videocam", Icons.Outlined.Videocam),
                entry("media_video_file", Icons.Outlined.VideoFile),
                entry("media_camera_alt", Icons.Outlined.CameraAlt),
                entry("media_control_camera", Icons.Outlined.ControlCamera),
                entry("media_camera_roll", Icons.Outlined.CameraRoll),
            ),
        ),
        IconGroup(
            R.string.icon_group_files,
            listOf(
                entry("file_folder", Icons.Outlined.Folder),
                entry("file_folder_open", Icons.Outlined.FolderOpen),
                entry("file_folder_special", Icons.Outlined.FolderSpecial),
                entry("file_folder_copy", Icons.Outlined.FolderCopy),
                entry("file_folder_new", Icons.Outlined.CreateNewFolder),
                entry("file_document", Icons.AutoMirrored.Outlined.InsertDriveFile),
                entry("file_attach", Icons.Outlined.AttachFile),
                entry("file_description", Icons.Outlined.Description),
                entry("file_save", Icons.Outlined.Save),
                entry("file_archive", Icons.Outlined.Archive),
                entry("file_unarchive", Icons.Outlined.Unarchive),
                entry("file_storage", Icons.Outlined.Storage),
                entry("file_pdf", Icons.Outlined.PictureAsPdf),
            ),
        ),
        IconGroup(
            R.string.icon_group_transfer,
            listOf(
                entry("net_cloud", Icons.Outlined.Cloud),
                entry("net_cloud_queue", Icons.Outlined.CloudQueue),
                entry("net_cloud_download", Icons.Outlined.CloudDownload),
                entry("net_cloud_upload", Icons.Outlined.CloudUpload),
                entry("net_backup", Icons.Outlined.Backup),
                entry("net_restore", Icons.Outlined.Restore),
                entry("net_download", Icons.Outlined.Download),
                entry("net_download_offline", Icons.Outlined.DownloadForOffline),
                entry("net_upload", Icons.Outlined.Upload),
                entry("net_sync", Icons.Outlined.Sync),
                entry("net_share", Icons.Outlined.Share),
                entry("net_open_in_new", Icons.AutoMirrored.Outlined.OpenInNew),
            ),
        ),
        IconGroup(
            R.string.icon_group_favorites,
            listOf(
                entry("fav_star", Icons.Outlined.Star),
                entry("fav_star_border", Icons.Outlined.StarBorder),
                entry("fav_star_half", Icons.AutoMirrored.Outlined.StarHalf),
                entry("fav_favorite", Icons.Outlined.Favorite),
                entry("fav_favorite_border", Icons.Outlined.FavoriteBorder),
                entry("fav_bookmark", Icons.Outlined.Bookmark),
                entry("fav_bookmark_border", Icons.Outlined.BookmarkBorder),
                entry("fav_label", Icons.AutoMirrored.Outlined.Label),
                entry("fav_label_important", Icons.AutoMirrored.Outlined.LabelImportant),
                entry("fav_flag", Icons.Outlined.Flag),
                entry("fav_local_offer", Icons.Outlined.LocalOffer),
                entry("fav_grade", Icons.Outlined.Grade),
            ),
        ),
        IconGroup(
            R.string.icon_group_playback,
            listOf(
                entry("play_arrow", Icons.Outlined.PlayArrow),
                entry("play_pause", Icons.Outlined.PauseCircle),
                entry("play_music", Icons.Outlined.MusicNote),
                entry("play_movie", Icons.Outlined.Movie),
                entry("play_movie_creation", Icons.Outlined.MovieCreation),
                entry("play_ondemand", Icons.Outlined.OndemandVideo),
                entry("play_video_library", Icons.Outlined.VideoLibrary),
                entry("play_theaters", Icons.Outlined.Theaters),
                entry("play_slideshow", Icons.Outlined.Slideshow),
                entry("play_mic", Icons.Outlined.Mic),
                entry("play_headphones", Icons.Outlined.Headphones),
                entry("play_radio", Icons.Outlined.Radio),
                entry("play_playlist_add", Icons.AutoMirrored.Outlined.PlaylistAdd),
            ),
        ),
        IconGroup(
            R.string.icon_group_places,
            listOf(
                entry("place_pin", Icons.Outlined.Place),
                entry("place_location", Icons.Outlined.LocationOn),
                entry("place_map", Icons.Outlined.Map),
                entry("place_city", Icons.Outlined.LocationCity),
                entry("place_explore", Icons.Outlined.Explore),
                entry("place_travel", Icons.Outlined.TravelExplore),
                entry("place_public", Icons.Outlined.Public),
                entry("place_car", Icons.Outlined.DirectionsCar),
                entry("place_train", Icons.Outlined.Train),
                entry("place_cafe", Icons.Outlined.LocalCafe),
                entry("place_cottage", Icons.Outlined.Cottage),
                entry("place_terrain", Icons.Outlined.Terrain),
            ),
        ),
        IconGroup(
            R.string.icon_group_tools,
            listOf(
                entry("tool_create", Icons.Outlined.Create),
                entry("tool_edit", Icons.Outlined.Edit),
                entry("tool_edit_note", Icons.Outlined.EditNote),
                entry("tool_delete", Icons.Outlined.Delete),
                entry("tool_delete_outline", Icons.Outlined.DeleteOutline),
                entry("tool_check", Icons.Outlined.Check),
                entry("tool_check_circle", Icons.Outlined.CheckCircle),
                entry("tool_close", Icons.Outlined.Close),
                entry("tool_done", Icons.Outlined.Done),
                entry("tool_done_all", Icons.Outlined.DoneAll),
                entry("tool_sort", Icons.AutoMirrored.Outlined.Sort),
                entry("tool_filter", Icons.Outlined.FilterList),
                entry("tool_tune", Icons.Outlined.Tune),
            ),
        ),
        IconGroup(
            R.string.icon_group_system,
            listOf(
                entry("sys_home", Icons.Outlined.Home),
                entry("sys_info", Icons.Outlined.Info),
                entry("sys_settings", Icons.Outlined.Settings),
                entry("sys_notifications", Icons.Outlined.Notifications),
                entry("sys_notifications_none", Icons.Outlined.NotificationsNone),
                entry("sys_notifications_active", Icons.Outlined.NotificationsActive),
                entry("sys_person", Icons.Outlined.Person),
                entry("sys_group", Icons.Outlined.Group),
                entry("sys_search", Icons.Outlined.Search),
                entry("sys_menu", Icons.Outlined.Menu),
                entry("sys_more_vert", Icons.Outlined.MoreVert),
                entry("sys_format_list", Icons.AutoMirrored.Outlined.FormatListBulleted),
            ),
        ),
    )

    val all: List<IconEntry> = groups.flatMap { it.entries }

    private val byKey: Map<String, IconEntry> = all.associateBy { it.key }

    fun entry(key: String): IconEntry = byKey[key] ?: byKey.getValue(DEFAULT_KEY)
}
