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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaController
import coil.compose.AsyncImage
import com.example.bobmusic.Album
import com.example.bobmusic.NavidromeClient
import com.example.bobmusic.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun LibraryScreen(
    client: NavidromeClient,
    active: Boolean,
    gaplessEnabled: Boolean,
    controller: MediaController?
) {
    var albums by remember { mutableStateOf<List<Album>>(emptyList()) }
    var status by remember { mutableStateOf("Loading albums...") }
    var isError by remember { mutableStateOf(false) }
    var retryTrigger by remember { mutableStateOf(0) }
    var selectedAlbum by remember { mutableStateOf<Album?>(null) }
    var songs by remember { mutableStateOf<List<Song>>(emptyList()) }

    LaunchedEffect(retryTrigger) {
        isError = false
        status = "Loading albums..."
        try {
            val result = withContext(Dispatchers.IO) { client.getAlbums() }
            albums = result
            status = "${result.size} albums"
        } catch (e: Exception) {
            status = "Error: ${e.message}"
            isError = true
        }
    }

    LaunchedEffect(selectedAlbum) {
        val album = selectedAlbum ?: return@LaunchedEffect
        songs = emptyList()
        status = "Loading songs..."
        try {
            val result = withContext(Dispatchers.IO) { client.getSongs(album.id) }
            songs = result
            status = album.name
        } catch (e: Exception) {
            status = "Error: ${e.message}"
        }
    }

    BackHandler(enabled = selectedAlbum != null) { selectedAlbum = null }

    val album = selectedAlbum

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        if (album == null) {
            Text(
                text = "Library",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            )

            if (isError) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                        Icon(Icons.Filled.Warning, contentDescription = "Error", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(64.dp))
                        Text("Connection Failed", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
                        Text("Check your credentials or server URL.\n\n($status)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.padding(bottom = 24.dp))
                        Button(onClick = { retryTrigger++ }) { Text("Retry Connection") }
                    }
                }
            } else if (albums.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(bottom = 160.dp, start = 16.dp, end = 16.dp, top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(albums) { a ->
                        AlbumCard(album = a, client = client, onClick = { selectedAlbum = a })
                    }
                }
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(top = 8.dp, end = 16.dp)) {
                IconButton(onClick = { selectedAlbum = null }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }

            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = client.getCoverUrl(album.coverArtId),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(100.dp)
                        .shadow(10.dp, RoundedCornerShape(12.dp), clip = false)
                        .clip(RoundedCornerShape(12.dp))
                )
                Column(modifier = Modifier.padding(start = 16.dp).weight(1f)) {
                    Text(album.name, style = MaterialTheme.typography.titleLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(album.artist, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 4.dp))
                }
            }

            if (songs.isEmpty() && status == "Loading songs...") {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                // Hoisted so the URL is computed once for the whole list
                val coverUrl = client.getCoverUrl(album.coverArtId)
                LazyColumn(contentPadding = PaddingValues(bottom = 160.dp)) {
                    items(songs) { s ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val c = controller ?: return@clickable
                                    val coverUri = Uri.parse(client.getCoverUrl(album.coverArtId))

                                    fun buildItem(song: Song): MediaItem =
                                        MediaItem.Builder()
                                            .setUri(client.getStreamUrl(song.id))
                                            .setMediaId(song.id) // <--- THIS IS THE NEW LINE
                                            .setMediaMetadata(
                                                MediaMetadata.Builder()
                                                    .setTitle(song.title)
                                                    .setArtist(album.artist)
                                                    .setAlbumTitle(album.name)
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
                                model = coverUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${s.track}. ${s.title}",
                                    style = MaterialTheme.typography.bodyLarge,
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
}

@Composable
fun AlbumCard(
    album: Album,
    client: NavidromeClient,
    onClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        AsyncImage(
            model = client.getCoverUrl(album.coverArtId),
            contentDescription = "Cover for ${album.name}",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .shadow(8.dp, RoundedCornerShape(12.dp), clip = false)
                .clip(RoundedCornerShape(12.dp))
        )
        Text(album.name, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 6.dp))
        Text(album.artist, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }


}