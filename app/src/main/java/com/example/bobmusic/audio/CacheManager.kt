package com.example.bobmusic.audio

import android.content.Context
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.database.StandaloneDatabaseProvider
import java.io.File

object CacheManager {
    private var simpleCache: SimpleCache? = null
    private var cacheDataSourceFactory: CacheDataSource.Factory? = null

    fun init(context: Context, maxBytes: Long) {
        if (simpleCache != null) return
        val cacheDir = File(context.cacheDir, "media_cache")
        val evictor = LeastRecentlyUsedCacheEvictor(maxBytes)
        val databaseProvider = StandaloneDatabaseProvider(context)
        simpleCache = SimpleCache(cacheDir, evictor, databaseProvider)

        val upstreamFactory = DefaultHttpDataSource.Factory()
        cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(simpleCache!!)
            .setUpstreamDataSourceFactory(upstreamFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }

    fun getCacheDataSourceFactory(): CacheDataSource.Factory? = cacheDataSourceFactory
}