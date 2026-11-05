package com.example.bobmusic.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.bobmusic.EqualizerManager
import com.example.bobmusic.ui.theme.AppTheme
import com.example.bobmusic.ui.theme.softSurface

@Composable
fun SettingsScreen(
    currentTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit,
    serverUrl: String,
    onLogout: () -> Unit,
    cacheSizeGb: Float,
    onCacheSizeChange: (Float) -> Unit,
    preloadSongs: Int,             // <-- NEW
    onPreloadChange: (Int) -> Unit, // <-- NEW
    gaplessEnabled: Boolean,
    onGaplessChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SettingsSection(title = "Appearance") {
            AppTheme.entries.forEach { theme ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onThemeChange(theme) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = currentTheme == theme,
                        onClick = { onThemeChange(theme) }
                    )
                    Text(
                        text = theme.displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }

        SettingsSection(title = "Playback") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Gapless Playback", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = gaplessEnabled,
                    onCheckedChange = onGaplessChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF1DB954),
                        checkedBorderColor = Color.Transparent
                    )
                )
            }
            Spacer(Modifier.height(10.dp))

            // Cache Size Slider
            Text("Cache Size: ${cacheSizeGb.toInt()} GB", style = MaterialTheme.typography.bodyMedium)
            Slider(
                value = cacheSizeGb,
                onValueChange = onCacheSizeChange,
                valueRange = 1f..20f,
                steps = 18
            )

            Spacer(Modifier.height(10.dp))

            // Preload Slider (NEW)
            Text("Songs to preload: $preloadSongs", style = MaterialTheme.typography.bodyMedium)
            Slider(
                value = preloadSongs.toFloat(),
                onValueChange = { onPreloadChange(it.toInt()) },
                valueRange = 0f..5f,
                steps = 4
            )
        }

        Text(
            text = "Equalizer",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )

        val eqController = EqualizerManager.controller
        if (eqController != null) {
            BeautifulEqualizer(controller = eqController)
        } else {
            SettingsSection(title = "") {
                Text(
                    text = "Play a song first to activate the Equalizer.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        SettingsSection(title = "Server") {
            Text(
                text = "Connected to: $serverUrl",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text("Log Out")
            }
        }

        Spacer(modifier = Modifier.height(160.dp))
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .softSurface(shape = RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        if (title.isNotBlank()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(10.dp))
        }
        content()
    }
}