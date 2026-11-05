package com.example.bobmusic.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

// Change this to your own name
private const val DEVELOPER = "Aris"

@Composable
fun AboutScreen() {
    val context = LocalContext.current
    val version = remember {
        try {
            context.packageManager
                .getPackageInfo(context.packageName, 0)
                .versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "BobMusic",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Version $version",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "A music player for your own Navidrome server, with your files played exactly as they are.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        AboutCard(
            title = "Features",
            lines = listOf(
                "Streams your library from your own server",
                "Original quality: no transcoding, FLAC stays FLAC",
                "Gapless album playback",
                "Audio cache of 2 to 12 GB for replaying songs",
                "Full-screen player with format, bit depth and bitrate",
                "Album covers, saved on the phone after the first load",
                "Several color themes"
            )
        )

        AboutCard(
            title = "Built with",
            lines = listOf(
                "Navidrome and the Subsonic API",
                "Jetpack Compose",
                "Media3 (ExoPlayer)",
                "OkHttp",
                "Coil"
            )
        )

        AboutCard(
            title = "Credits",
            lines = listOf(
                "Made by $DEVELOPER",
                "Music and server data belong to you. The app only reads what your server sends."
            )
        )

        // Keeps the last card clear of the floating bar
        Spacer(modifier = Modifier.height(140.dp))
    }
}

@Composable
private fun AboutCard(title: String, lines: List<String>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            lines.forEach { line ->
                Text(
                    text = "•  $line",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}