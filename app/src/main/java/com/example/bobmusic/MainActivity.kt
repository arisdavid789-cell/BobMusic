package com.example.bobmusic

import android.content.ComponentName
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.bobmusic.ui.RootScreen
import com.example.bobmusic.ui.screens.LoginScreen
import com.example.bobmusic.ui.theme.AppTheme
import com.example.bobmusic.ui.theme.BobMusicTheme
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val context = LocalContext.current
            val settings = remember { AppSettings(context) }
            val scope = rememberCoroutineScope()

            var currentTheme by remember {
                mutableStateOf(
                    try {
                        AppTheme.valueOf(
                            context.getSharedPreferences("bobmusic", MODE_PRIVATE)
                                .getString("theme", "FlamingRed") ?: "FlamingRed"
                        )
                    } catch (e: Exception) { AppTheme.FlamingRed }
                )
            }

            var isLoggedIn by remember { mutableStateOf(settings.isLoggedIn()) }
            var isLoggingIn by remember { mutableStateOf(false) }
            var loginError by remember { mutableStateOf<String?>(null) }

            var cacheSizeGb by remember { mutableStateOf(settings.cacheSizeGb.toFloat()) }
            var preloadSongs by remember { mutableStateOf(settings.preloadSongs) } // ONLY ONE DECLARATION HERE
            var gaplessEnabled by remember { mutableStateOf(settings.gaplessEnabled) }

            var client by remember {
                mutableStateOf(
                    if (isLoggedIn) NavidromeClient(settings.serverUrl, settings.username, settings.password)
                    else null
                )
            }

            BobMusicTheme(appTheme = currentTheme) {
                if (!isLoggedIn || client == null) {
                    LoginScreen(
                        existingName = settings.userName,
                        isLoading = isLoggingIn,
                        errorMessage = loginError,
                        onLogin = { url, user, pass, name ->
                            isLoggingIn = true
                            loginError = null

                            scope.launch(Dispatchers.IO) {
                                try {
                                    val testClient = NavidromeClient(url, user, pass)
                                    val pingResult = testClient.ping()

                                    withContext(Dispatchers.Main) {
                                        if (pingResult == "ok") {
                                            settings.saveCredentials(url, user, pass, name)
                                            client = testClient
                                            isLoggedIn = true
                                        } else {
                                            loginError = "Couldn't connect. Please check credentials."
                                        }
                                        isLoggingIn = false
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        loginError = "Network error. Is the server running?"
                                        isLoggingIn = false
                                    }
                                }
                            }
                        }
                    )
                } else {
                    var controller by remember { mutableStateOf<MediaController?>(null) }
                    DisposableEffect(Unit) {
                        val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
                        val future: ListenableFuture<MediaController> = MediaController.Builder(context, token).buildAsync()
                        future.addListener({ controller = future.get() }, MoreExecutors.directExecutor())
                        onDispose {
                            MediaController.releaseFuture(future)
                            controller = null
                        }
                    }

                    RootScreen(
                        client = client!!,
                        controller = controller,
                        currentTheme = currentTheme,
                        onThemeChange = { newTheme ->
                            currentTheme = newTheme
                            context.getSharedPreferences("bobmusic", MODE_PRIVATE)
                                .edit().putString("theme", newTheme.name).apply()
                        },
                        serverUrl = settings.serverUrl,
                        onLogout = {
                            settings.clearCredentials()
                            isLoggedIn = false
                            client = null
                        },
                        cacheSizeGb = cacheSizeGb,
                        onCacheSizeChange = {
                            cacheSizeGb = it
                            settings.cacheSizeGb = it.toInt()
                        },
                        preloadSongs = preloadSongs,
                        onPreloadChange = {
                            preloadSongs = it
                            settings.preloadSongs = it
                        },
                        gaplessEnabled = gaplessEnabled,
                        onGaplessChange = {
                            gaplessEnabled = it
                            settings.gaplessEnabled = it
                        }
                    )
                }
            }
        }
    }
}