package com.example.bobmusic.ui

import android.graphics.drawable.BitmapDrawable
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import com.example.bobmusic.NavidromeClient
import com.example.bobmusic.Playlist
import com.example.bobmusic.SyncedLyrics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

private fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    return "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
}

private fun buildQuality(m: MediaMetadata): String {
    val extras = m.extras ?: return ""
    val suffix = extras.getString("suffix", "")
    val bitRate = extras.getInt("bitRate", 0)
    val bitDepth = extras.getInt("bitDepth", 0)
    val samplingRate = extras.getInt("samplingRate", 0)

    val parts = mutableListOf<String>()
    if (suffix.isNotBlank()) parts.add(suffix.uppercase())
    if (bitDepth > 0) parts.add("$bitDepth-bit")
    if (samplingRate > 0) {
        val khz = if (samplingRate % 1000 == 0) "${samplingRate / 1000}"
        else String.format(Locale.US, "%.1f", samplingRate / 1000.0)
        parts.add("$khz kHz")
    }
    if (bitRate > 0) parts.add("$bitRate kbps")
    return parts.joinToString(" · ")
}

@Composable
fun PlayerScreen(
    client: NavidromeClient,
    controller: MediaController?,
    onClose: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var artist by remember { mutableStateOf("") }
    var albumName by remember { mutableStateOf("") }
    var quality by remember { mutableStateOf("") }
    var artwork by remember { mutableStateOf<android.net.Uri?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var position by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var dragging by remember { mutableStateOf(false) }

    // Dynamic Color State
    var albumColor by remember { mutableStateOf<Color?>(null) }
    val context = LocalContext.current

    // Playlist state
    var showMenu by remember { mutableStateOf(false) }
    var showPlaylistDialog by remember { mutableStateOf(false) }
    var playlists by remember { mutableStateOf<List<Playlist>>(emptyList()) }
    var isLoadingPlaylists by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Lyrics State
    var currentLyrics by remember { mutableStateOf<SyncedLyrics?>(null) }
    var isLoadingLyrics by remember { mutableStateOf(false) }

    BackHandler(onBack = onClose)

    // 1. Extract Palette when artwork changes
    LaunchedEffect(artwork) {
        val uri = artwork
        if (uri == null) {
            albumColor = null
            return@LaunchedEffect
        }

        val request = ImageRequest.Builder(context)
            .data(uri)
            .allowHardware(false)
            .build()

        val result = context.imageLoader.execute(request)
        val bitmap = (result.drawable as? BitmapDrawable)?.bitmap

        if (bitmap != null) {
            val palette = withContext(Dispatchers.Default) {
                Palette.from(bitmap).generate()
            }
            val color = palette.getDarkVibrantColor(
                palette.getMutedColor(
                    palette.getDominantColor(android.graphics.Color.DKGRAY)
                )
            )
            albumColor = Color(color)
        }
    }

    // 2. Read Metadata
    DisposableEffect(controller) {
        val c = controller ?: return@DisposableEffect onDispose {}

        fun readMetadata(m: MediaMetadata) {
            title = m.title?.toString() ?: ""
            artist = m.artist?.toString() ?: ""
            albumName = m.albumTitle?.toString() ?: ""
            quality = buildQuality(m)
            artwork = m.artworkUri
        }

        val listener = object : Player.Listener {
            override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                readMetadata(mediaMetadata)
            }
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }
        }
        c.addListener(listener)
        readMetadata(c.mediaMetadata)
        isPlaying = c.isPlaying
        onDispose { c.removeListener(listener) }
    }

    // 3. Poll Position
    LaunchedEffect(controller) {
        val c = controller ?: return@LaunchedEffect
        while (true) {
            if (!dragging) {
                position = c.currentPosition
                duration = c.duration.coerceAtLeast(0L)
            }
            delay(500)
        }
    }

    // 4. Fetch Lyrics when song changes
    LaunchedEffect(controller?.currentMediaItem?.mediaId) {
        val songId = controller?.currentMediaItem?.mediaId
        if (songId != null) {
            isLoadingLyrics = true
            currentLyrics = withContext(Dispatchers.IO) { client.getLyrics(songId) }
            isLoadingLyrics = false
        } else {
            currentLyrics = null
        }
    }

    val remainingMs = (duration - position).coerceAtLeast(0L)
    val remainingTimeStr = "-${formatTime(remainingMs)}"

    val baseColor = albumColor ?: MaterialTheme.colorScheme.primary
    val gradient = Brush.verticalGradient(
        colors = listOf(
            baseColor.copy(alpha = 0.85f),
            baseColor.copy(alpha = 0.4f),
            Color.Black
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            // --- Top Bar ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Close player",
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = albumName.ifBlank { "Playing" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )

                // 3-Dots Menu
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            Icons.Filled.MoreHoriz,
                            contentDescription = "Options",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Add to playlist") },
                            onClick = {
                                showMenu = false
                                isLoadingPlaylists = true
                                showPlaylistDialog = true
                                scope.launch(Dispatchers.IO) {
                                    playlists = client.getPlaylists()
                                    withContext(Dispatchers.Main) {
                                        isLoadingPlaylists = false
                                    }
                                }
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // --- Album Art ---
            AsyncImage(
                model = artwork,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .shadow(16.dp, RoundedCornerShape(12.dp), clip = false)
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(Modifier.height(32.dp))

            // --- Track Info ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = artist,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Liked",
                    tint = baseColor,
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .size(28.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            // --- Progress Bar ---
            Slider(
                value = position.toFloat(),
                onValueChange = {
                    dragging = true
                    position = it.toLong()
                },
                onValueChangeFinished = {
                    controller?.seekTo(position)
                    dragging = false
                },
                valueRange = 0f..maxOf(duration, 1L).toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White,
                    inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                ),
                modifier = Modifier.height(24.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatTime(position),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Text(
                    text = remainingTimeStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            Spacer(Modifier.height(24.dp))

            // --- Playback Controls ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { controller?.seekToPreviousMediaItem() },
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        Icons.Filled.SkipPrevious,
                        contentDescription = "Previous",
                        modifier = Modifier.size(48.dp)
                    )
                }

                Surface(
                    onClick = { if (isPlaying) controller?.pause() else controller?.play() },
                    shape = CircleShape,
                    color = Color.White,
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.Black,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                IconButton(
                    onClick = { controller?.seekToNextMediaItem() },
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        Icons.Filled.SkipNext,
                        contentDescription = "Next",
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // --- Quality Badge ---
            if (quality.isNotBlank()) {
                Surface(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = quality,
                        color = Color(0xFF4ADE80),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                Spacer(Modifier.height(32.dp))
            }

            // --- Lyrics View ---
            if (isLoadingLyrics) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else if (currentLyrics != null) {
                LyricsView(
                    lyrics = currentLyrics,
                    currentPositionMs = position,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .background(baseColor.copy(alpha = 0.15f))
                        .padding(vertical = 16.dp)
                )
            } else {
                // No lyrics available
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .background(baseColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Lyrics not available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    // --- Add to Playlist Dialog ---
    if (showPlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showPlaylistDialog = false },
            title = { Text("Add to Playlist") },
            text = {
                if (isLoadingPlaylists) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (playlists.isEmpty()) {
                    Text("No playlists found. Create one in the Playlists tab first.")
                } else {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        playlists.forEach { playlist ->
                            Text(
                                text = playlist.name,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        val currentSongId = controller?.currentMediaItem?.mediaId
                                        if (currentSongId != null) {
                                            scope.launch(Dispatchers.IO) {
                                                val success = client.addToPlaylist(playlist.id, currentSongId)
                                                withContext(Dispatchers.Main) {
                                                    if (success) {
                                                        Toast.makeText(context, "Song added to ${playlist.name} successfully", Toast.LENGTH_SHORT).show()
                                                        showPlaylistDialog = false
                                                    } else {
                                                        Toast.makeText(context, "Failed to add song", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    .padding(16.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPlaylistDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}