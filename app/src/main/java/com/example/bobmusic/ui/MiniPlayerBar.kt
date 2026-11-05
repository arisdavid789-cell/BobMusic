package com.example.bobmusic.ui

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import coil.compose.AsyncImage
import com.example.bobmusic.ui.theme.liquidGlass
import kotlinx.coroutines.delay

@Composable
fun MiniPlayerBar(
    controller: MediaController?,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }
    var artist by remember { mutableStateOf("") }
    var artwork by remember { mutableStateOf<Uri?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var hasMedia by remember { mutableStateOf(false) }
    var position by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }

    DisposableEffect(controller) {
        val c = controller ?: return@DisposableEffect onDispose {}
        val listener = object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val m = mediaItem?.mediaMetadata
                title = m?.title?.toString() ?: "Unknown Track"
                artist = m?.artist?.toString() ?: ""
                artwork = m?.artworkUri
                hasMedia = true
            }
            override fun onIsPlayingChanged(playing: Boolean) { isPlaying = playing }
            override fun onMediaMetadataChanged(mediaMetadata: androidx.media3.common.MediaMetadata) {
                title = mediaMetadata.title?.toString() ?: title
                artist = mediaMetadata.artist?.toString() ?: artist
                artwork = mediaMetadata.artworkUri ?: artwork
            }
        }
        c.addListener(listener)
        isPlaying = c.isPlaying
        hasMedia = c.currentMediaItem != null
        if (hasMedia) {
            val m = c.mediaMetadata
            title = m.title?.toString() ?: "Playing"
            artist = m.artist?.toString() ?: ""
            artwork = m.artworkUri
        }
        onDispose { c.removeListener(listener) }
    }

    LaunchedEffect(controller) {
        val c = controller ?: return@LaunchedEffect
        while (true) {
            position = c.currentPosition
            duration = c.duration.coerceAtLeast(0L)
            delay(500)
        }
    }

    if (!hasMedia) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlass(shape = RoundedCornerShape(20.dp), shadowElevation = 20.dp)
    ) {
        // Thin progress bar at the top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .padding(horizontal = 14.dp)
        ) {
            val frac = if (duration > 0) (position.toFloat() / duration).coerceIn(0f, 1f) else 0f
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(frac)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                if (artwork != null) {
                    AsyncImage(
                        model = artwork,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().fillMaxHeight()
                    )
                } else {
                    Icon(
                        Icons.Filled.MusicNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title.ifBlank { "Unknown Track" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (artist.isNotBlank()) {
                    Text(
                        artist,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            IconButton(onClick = {
                if (isPlaying) controller?.pause() else controller?.play()
            }) {
                Icon(
                    if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = { controller?.seekToNextMediaItem() }) {
                Icon(Icons.Filled.SkipNext, contentDescription = "Next")
            }
        }
    }
}