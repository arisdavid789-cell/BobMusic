package com.example.bobmusic.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaController
import coil.compose.AsyncImage
import com.example.bobmusic.NavidromeClient
import com.example.bobmusic.Playlist
import com.example.bobmusic.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun PlaylistsScreen(
    client: NavidromeClient,
    controller: MediaController?,
    gaplessEnabled: Boolean
) {
    var playlists by remember { mutableStateOf<List<Playlist>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    var refreshTrigger by remember { mutableStateOf(0) }

    // State for clicking into a playlist
    var selectedPlaylist by remember { mutableStateOf<Playlist?>(null) }
    var songs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var isLoadingSongs by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(refreshTrigger) {
        isLoading = true
        try {
            playlists = withContext(Dispatchers.IO) { client.getPlaylists() }
        } catch (e: Exception) { }
        isLoading = false
    }

    LaunchedEffect(selectedPlaylist) {
        val playlist = selectedPlaylist ?: return@LaunchedEffect
        isLoadingSongs = true
        songs = emptyList()
        try {
            songs = withContext(Dispatchers.IO) { client.getPlaylistSongs(playlist.id) }
        } catch (e: Exception) { }
        isLoadingSongs = false
    }

    BackHandler(enabled = selectedPlaylist != null) { selectedPlaylist = null }

    val playlist = selectedPlaylist

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        if (playlist == null) {
            // --- MAIN PLAYLISTS GRID ---
            Text(
                text = "Your Playlists",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (playlists.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text(
                            text = "The Playlists CANNOT be that good",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Create one to get started.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
                        )
                        Surface(
                            onClick = { showCreateDialog = true },
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Filled.Add,
                                    contentDescription = "Create Playlist",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(bottom = 160.dp, start = 16.dp, end = 16.dp, top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(playlists) { p ->
                        PlaylistCard(
                            playlist = p,
                            client = client,
                            onClick = { selectedPlaylist = p } // THIS MAKES IT CLICKABLE
                        )
                    }
                }
            }
        } else {
            // --- PLAYLIST DETAIL (SONG LIST) ---
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(top = 8.dp, end = 16.dp)) {
                IconButton(onClick = { selectedPlaylist = null }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }

            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                val coverModel = if (playlist.coverArtId.isNullOrBlank()) null else client.getCoverUrl(playlist.coverArtId)
                if (coverModel != null) {
                    AsyncImage(
                        model = coverModel,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(100.dp).shadow(10.dp, RoundedCornerShape(12.dp), clip = false).clip(RoundedCornerShape(12.dp))
                    )
                } else {
                    Surface(
                        modifier = Modifier.size(100.dp).shadow(10.dp, RoundedCornerShape(12.dp), clip = false).clip(RoundedCornerShape(12.dp)),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.QueueMusic, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                        }
                    }
                }

                Column(modifier = Modifier.padding(start = 16.dp).weight(1f)) {
                    Text(playlist.name, style = MaterialTheme.typography.titleLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text("${songs.size} songs", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, modifier = Modifier.padding(top = 4.dp))
                }
            }

            if (isLoadingSongs) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = 160.dp)) {
                    items(songs) { s ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val c = controller ?: return@clickable
                                    val coverUri = Uri.parse(client.getCoverUrl(s.coverArtId ?: playlist.coverArtId))

                                    fun buildItem(song: Song): MediaItem =
                                        MediaItem.Builder()
                                            .setUri(client.getStreamUrl(song.id))
                                            .setMediaId(song.id)
                                            .setMediaMetadata(
                                                MediaMetadata.Builder()
                                                    .setTitle(song.title)
                                                    .setArtist(song.artist.ifBlank { "Unknown Artist" })
                                                    .setAlbumTitle(playlist.name)
                                                    .setArtworkUri(coverUri)
                                                    .build()
                                            )
                                            .build()

                                    if (gaplessEnabled) {
                                        val list = songs.map { buildItem(it) }
                                        c.setMediaItems(list, songs.indexOf(s), 0L)
                                    } else {
                                        c.setMediaItem(buildItem(s))
                                    }
                                    c.prepare()
                                    c.play()
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = client.getCoverUrl(s.coverArtId ?: playlist.coverArtId),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(52.dp).clip(RoundedCornerShape(8.dp))
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = s.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = s.artist,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("New Playlist") },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    label = { Text("Playlist Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newPlaylistName.isNotBlank()) {
                        scope.launch(Dispatchers.IO) {
                            val success = client.createPlaylist(newPlaylistName)
                            if (success) {
                                withContext(Dispatchers.Main) {
                                    newPlaylistName = ""
                                    showCreateDialog = false
                                    refreshTrigger++
                                }
                            }
                        }
                    }
                }) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun PlaylistCard(
    playlist: Playlist,
    client: NavidromeClient,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        if (playlist.coverArtId.isNullOrBlank()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .shadow(8.dp, RoundedCornerShape(12.dp), clip = false)
                    .clip(RoundedCornerShape(12.dp)),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.QueueMusic,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        } else {
            AsyncImage(
                model = client.getCoverUrl(playlist.coverArtId),
                contentDescription = "Cover for ${playlist.name}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .shadow(8.dp, RoundedCornerShape(12.dp), clip = false)
                    .clip(RoundedCornerShape(12.dp))
            )
        }
        Text(
            playlist.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 6.dp)
        )
        Text(
            "${playlist.songCount} songs",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}