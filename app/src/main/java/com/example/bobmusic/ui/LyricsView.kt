package com.example.bobmusic.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bobmusic.SyncedLyrics
import kotlinx.coroutines.delay

@Composable
fun LyricsView(
    lyrics: SyncedLyrics?,
    currentPositionMs: Long,
    modifier: Modifier = Modifier
) {
    if (lyrics == null) {
        Box(
            modifier = modifier.fillMaxWidth().height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Lyrics not available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    if (!lyrics.isSynced) {
        // Plain, unsynced lyrics – just a scrollable column
        LazyColumn(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(lyrics.lines) { _, line ->
                Text(
                    text = line.text,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                )
            }
        }
        return
    }

    // Synced lyrics – auto-scroll
    val listState = rememberLazyListState()

    // Determine the active line index
    val activeIndex = remember(currentPositionMs, lyrics) {
        val adjustedPos = currentPositionMs - lyrics.offsetMs
        lyrics.lines.indexOfLast { it.startMs <= adjustedPos }.coerceAtLeast(0)
    }

    // Auto-scroll to the active line, keeping it roughly centered
    LaunchedEffect(activeIndex) {
        // Small delay to let the line highlight update first
        delay(50)
        listState.animateScrollToItem(
            index = activeIndex,
            scrollOffset = -200 // keeps the active line a bit above center
        )
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 120.dp)
    ) {
        itemsIndexed(lyrics.lines) { index, line ->
            val isActive = index == activeIndex
            Text(
                text = line.text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                    fontSize = if (isActive) 20.sp else 16.sp
                ),
                color = if (isActive) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )
        }
    }
}