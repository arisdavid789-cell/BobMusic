package com.example.bobmusic

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.bobmusic.audio.CacheManager
import com.example.bobmusic.audio.ParametricEqProcessor
import kotlinx.coroutines.runBlocking

@UnstableApi
class PlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private var equalizerController: EqualizerController? = null

    override fun onCreate() {
        super.onCreate()

        val settings = AppSettings(this)
        val cacheSizeBytes = settings.cacheSizeGb.toLong() * 1024 * 1024 * 1024
        val targetBufferBytes = (settings.preloadSongs * 15 * 1024 * 1024)

        // Cache is set up first, but a failure must never stop the player from existing
        var cacheFactory: CacheDataSource.Factory? = null
        try {
            runBlocking { CacheManager.init(this@PlaybackService, cacheSizeBytes) }
            cacheFactory = CacheManager.getCacheDataSourceFactory()
        } catch (e: Exception) {
            android.util.Log.e("PlaybackService", "Cache init failed", e)
        }

        // The session is created here, before onCreate returns,
        // so the app can always connect to it
        buildPlayer(cacheFactory, targetBufferBytes)
    }

    private fun buildPlayer(cacheFactory: CacheDataSource.Factory?, targetBufferBytes: Int) {
        val eqProcessor = ParametricEqProcessor()

        val renderersFactory = object : DefaultRenderersFactory(this) {
            override fun buildAudioSink(
                context: Context,
                enableFloatOutput: Boolean,
                enableAudioTrackPlaybackParams: Boolean
            ): AudioSink {
                return DefaultAudioSink.Builder(context)
                    .setAudioProcessorChain(
                        DefaultAudioSink.DefaultAudioProcessorChain(eqProcessor)
                    )
                    .setEnableFloatOutput(enableFloatOutput)
                    .setEnableAudioTrackPlaybackParams(enableAudioTrackPlaybackParams)
                    .build()
            }
        }

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(60_000, 300_000, 5_000, 15_000)
            .setPrioritizeTimeOverSizeThresholds(true)
            .setTargetBufferBytes(targetBufferBytes)
            .build()

        val builder = ExoPlayer.Builder(this)
            .setRenderersFactory(renderersFactory)
            .setLoadControl(loadControl)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                /* handleAudioFocus = */ true
            )
            .setHandleAudioBecomingNoisy(true)

        if (cacheFactory != null) {
            builder.setMediaSourceFactory(DefaultMediaSourceFactory(cacheFactory))
        }

        val player = builder.build()

        equalizerController = EqualizerController(eqProcessor)
        EqualizerManager.controller = equalizerController

        mediaSession = MediaSession.Builder(this, player).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        EqualizerManager.controller = null
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}