package com.example.bobmusic.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.media3.session.MediaController
import com.example.bobmusic.NavidromeClient
import com.example.bobmusic.ui.screens.AboutScreen
import com.example.bobmusic.ui.screens.LibraryScreen
import com.example.bobmusic.ui.screens.PlaylistsScreen
import com.example.bobmusic.ui.screens.SettingsScreen
import com.example.bobmusic.ui.theme.AppTheme
import com.example.bobmusic.ui.theme.liquidGlass

enum class AppTab(val title: String, val icon: ImageVector) {
    Library("Library", Icons.Filled.LibraryMusic),
    Playlists("Playlists", Icons.Filled.QueueMusic),
    Settings("Settings", Icons.Filled.Settings),
    About("About", Icons.Filled.Info)
}

@Composable
fun RootScreen(
    client: NavidromeClient,
    controller: MediaController?,
    currentTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit,
    serverUrl: String,
    onLogout: () -> Unit,
    cacheSizeGb: Float,
    onCacheSizeChange: (Float) -> Unit,
    preloadSongs: Int,
    onPreloadChange: (Int) -> Unit,
    gaplessEnabled: Boolean,
    onGaplessChange: (Boolean) -> Unit
) {
    var currentTab by remember { mutableStateOf(AppTab.Library) }
    var showPlayer by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = if (currentTab == AppTab.Library) Modifier.fillMaxSize()
                        else Modifier.size(0.dp)
                    ) {
                        LibraryScreen(
                            client = client,
                            controller = controller,
                            active = currentTab == AppTab.Library && !showPlayer,
                            gaplessEnabled = gaplessEnabled,
                        )
                    }

                    when (currentTab) {
                        AppTab.Library -> {}
                        AppTab.Playlists -> PlaylistsScreen(
                            client = client,
                            controller = controller,
                            gaplessEnabled = gaplessEnabled
                        )
                        AppTab.Settings -> SettingsScreen(
                            currentTheme = currentTheme,
                            onThemeChange = onThemeChange,
                            serverUrl = serverUrl,
                            onLogout = onLogout,
                            cacheSizeGb = cacheSizeGb,
                            onCacheSizeChange = onCacheSizeChange,
                            preloadSongs = preloadSongs,
                            onPreloadChange = onPreloadChange,
                            gaplessEnabled = gaplessEnabled,
                            onGaplessChange = onGaplessChange
                        )
                        AppTab.About -> AboutScreen()
                    }
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                        .padding(horizontal = 12.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MiniPlayerBar(controller = controller, onClick = { showPlayer = true })
                    FloatingNavBar(currentTab = currentTab, onTabSelected = { currentTab = it })
                }
            }

            AnimatedVisibility(
                visible = showPlayer,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.systemBarsPadding()) {
                        PlayerScreen(
                            client = client,
                            controller = controller,
                            onClose = { showPlayer = false }
                        )
                    }
                }
            }
        }
    }
}

// FloatingNavBar is correctly positioned outside the RootScreen function
@Composable
private fun FloatingNavBar(
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(68.dp)
            .liquidGlass(
                shape = RoundedCornerShape(50),
                alpha = 0.85f,
                shadowElevation = 22.dp
            )
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppTab.entries.forEach { tab ->
            val selected = tab == currentTab
            val bg by animateColorAsState(
                targetValue = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
                else Color.Transparent,
                label = "navBg"
            )
            val tint by animateColorAsState(
                targetValue = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                label = "navTint"
            )
            Surface(
                onClick = { onTabSelected(tab) },
                shape = RoundedCornerShape(50),
                color = bg,
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(tab.icon, contentDescription = tab.title, tint = tint)
                }
            }
        }
    }
}