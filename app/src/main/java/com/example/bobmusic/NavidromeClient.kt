package com.example.bobmusic

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.security.MessageDigest
import java.util.UUID

data class Album(
    val id: String,
    val name: String,
    val artist: String,
    val coverArtId: String? = null
)

data class Song(
    val id: String,
    val title: String,
    val track: Int,
    val suffix: String = "",
    val bitRate: Int = 0,
    val bitDepth: Int = 0,
    val samplingRate: Int = 0,
    val artist: String = "",
    val coverArtId: String? = null
)

data class Playlist(
    val id: String,
    val name: String,
    val songCount: Int,
    val coverArtId: String? = null
)

// --- ADDED FOR LYRICS ---
data class LyricLine(
    val startMs: Long,
    val text: String
)

data class SyncedLyrics(
    val lines: List<LyricLine>,
    val isSynced: Boolean,
    val offsetMs: Long = 0L
)
// ------------------------

class NavidromeClient(
    private val baseUrl: String,
    private val username: String,
    private val password: String
) {
    private val http = OkHttpClient()
    private val salt: String = UUID.randomUUID().toString().replace("-", "").take(12)
    private val token: String = md5(password + salt)

    private fun md5(s: String): String =
        MessageDigest.getInstance("MD5").digest(s.toByteArray())
            .joinToString("") { "%02x".format(it) }

    private fun buildUrl(endpoint: String): HttpUrl.Builder {
        return "$baseUrl/rest/$endpoint".toHttpUrl().newBuilder()
            .addQueryParameter("u", username)
            .addQueryParameter("t", token)
            .addQueryParameter("s", salt)
            .addQueryParameter("v", "1.16.1")
            .addQueryParameter("c", "BobMusic")
            .addQueryParameter("f", "json")
    }

    fun ping(): String {
        val url = buildUrl("ping").build()
        http.newCall(Request.Builder().url(url).build()).execute().use { resp ->
            val body = resp.body?.string() ?: return "empty response"
            val json = JSONObject(body).getJSONObject("subsonic-response")
            return json.getString("status")
        }
    }

    fun getAlbums(): List<Album> {
        val url = buildUrl("getAlbumList2")
            .addQueryParameter("type", "alphabeticalByName")
            .addQueryParameter("size", "490")
            .build()
        http.newCall(Request.Builder().url(url).build()).execute().use { resp ->
            val body = resp.body?.string() ?: return emptyList()
            val root = JSONObject(body).getJSONObject("subsonic-response")
            val array = root.optJSONObject("albumList2")?.optJSONArray("album")
                ?: return emptyList()
            val albums = mutableListOf<Album>()
            for (i in 0 until array.length()) {
                val a = array.getJSONObject(i)
                albums.add(
                    Album(
                        id = a.getString("id"),
                        name = a.optString("name"),
                        artist = a.optString("artist"),
                        coverArtId = a.optString("coverArt")
                    )
                )
            }
            return albums
        }
    }

    fun getSongs(albumId: String): List<Song> {
        val url = buildUrl("getAlbum")
            .addQueryParameter("id", albumId)
            .build()
        http.newCall(Request.Builder().url(url).build()).execute().use { resp ->
            val body = resp.body?.string() ?: return emptyList()
            val root = JSONObject(body).getJSONObject("subsonic-response")
            val array = root.optJSONObject("album")?.optJSONArray("song")
                ?: return emptyList()
            val songs = mutableListOf<Song>()
            for (i in 0 until array.length()) {
                val s = array.getJSONObject(i)
                songs.add(
                    Song(
                        id = s.getString("id"),
                        title = s.optString("title"),
                        track = s.optInt("track"),
                        suffix = s.optString("suffix"),
                        bitRate = s.optInt("bitRate"),
                        bitDepth = s.optInt("bitDepth"),
                        samplingRate = s.optInt("samplingRate"),
                        artist = s.optString("artist"),
                        coverArtId = s.optString("coverArt")
                    )
                )
            }
            return songs
        }
    }

    fun getStreamUrl(songId: String): String {
        return buildUrl("stream")
            .addQueryParameter("id", songId)
            .addQueryParameter("format", "raw")
            .addQueryParameter("maxBitRate", "0")
            .build()
            .toString()
    }

    fun getCoverUrl(coverArtId: String?, size: Int? = null): String {
        val idToUse = if (coverArtId.isNullOrBlank()) "" else coverArtId
        val builder = buildUrl("getCoverArt")
            .addQueryParameter("id", idToUse)
        if (size != null) builder.addQueryParameter("size", size.toString())
        return builder.build().toString()
    }

    fun getPlaylists(): List<Playlist> {
        val url = buildUrl("getPlaylists").build()
        http.newCall(Request.Builder().url(url).build()).execute().use { resp ->
            val body = resp.body?.string() ?: return emptyList()
            val root = JSONObject(body).getJSONObject("subsonic-response")
            val array = root.optJSONObject("playlists")?.optJSONArray("playlist")
                ?: return emptyList()
            val playlists = mutableListOf<Playlist>()
            for (i in 0 until array.length()) {
                val p = array.getJSONObject(i)
                playlists.add(
                    Playlist(
                        id = p.getString("id"),
                        name = p.optString("name"),
                        songCount = p.optInt("songCount"),
                        coverArtId = p.optString("coverArt")
                    )
                )
            }
            return playlists
        }
    }

    fun getPlaylistSongs(playlistId: String): List<Song> {
        val url = buildUrl("getPlaylist")
            .addQueryParameter("id", playlistId)
            .build()
        http.newCall(Request.Builder().url(url).build()).execute().use { resp ->
            val body = resp.body?.string() ?: return emptyList()
            val root = JSONObject(body).getJSONObject("subsonic-response")
            val array = root.optJSONObject("playlist")?.optJSONArray("entry")
                ?: return emptyList()
            val songs = mutableListOf<Song>()
            for (i in 0 until array.length()) {
                val s = array.getJSONObject(i)
                songs.add(
                    Song(
                        id = s.getString("id"),
                        title = s.optString("title"),
                        track = s.optInt("track"),
                        suffix = s.optString("suffix"),
                        bitRate = s.optInt("bitRate"),
                        bitDepth = s.optInt("bitDepth"),
                        samplingRate = s.optInt("samplingRate"),
                        artist = s.optString("artist"),
                        coverArtId = s.optString("coverArt")
                    )
                )
            }
            return songs
        }
    }

    fun createPlaylist(name: String, songId: String? = null): Boolean {
        val builder = buildUrl("createPlaylist").addQueryParameter("name", name)
        if (songId != null) builder.addQueryParameter("songId", songId)

        http.newCall(Request.Builder().url(builder.build()).build()).execute().use { resp ->
            val body = resp.body?.string() ?: return false
            val root = JSONObject(body).getJSONObject("subsonic-response")
            return root.getString("status") == "ok"
        }
    }

    fun addToPlaylist(playlistId: String, songId: String): Boolean {
        val url = buildUrl("updatePlaylist")
            .addQueryParameter("playlistId", playlistId)
            .addQueryParameter("songIdToAdd", songId)
            .build()
        http.newCall(Request.Builder().url(url).build()).execute().use { resp ->
            val body = resp.body?.string() ?: return false
            val root = JSONObject(body).getJSONObject("subsonic-response")
            return root.getString("status") == "ok"
        }
    }

    fun getLyrics(songId: String): SyncedLyrics? {
        val url = buildUrl("getLyricsBySongId")
            .addQueryParameter("id", songId)
            .build()

        return try {
            http.newCall(Request.Builder().url(url).build()).execute().use { resp ->
                val body = resp.body?.string() ?: return null
                val root = JSONObject(body).getJSONObject("subsonic-response")
                val lyricsList = root.optJSONObject("lyricsList") ?: return null
                val structuredArray = lyricsList.optJSONArray("structuredLyrics") ?: return null

                // Look for the first synced lyric entry
                var syncedLyrics: JSONObject? = null
                for (i in 0 until structuredArray.length()) {
                    val obj = structuredArray.getJSONObject(i)
                    if (obj.optBoolean("synced", false)) {
                        syncedLyrics = obj
                        break
                    }
                }

                // Fallback to the first unsynced entry if no synced one exists
                val target = syncedLyrics ?: structuredArray.optJSONObject(0) ?: return null
                val isSynced = target.optBoolean("synced", false)
                val offset = target.optLong("offset", 0L)

                val linesArray = target.optJSONArray("line") ?: return null
                val lines = mutableListOf<LyricLine>()
                for (i in 0 until linesArray.length()) {
                    val lineObj = linesArray.getJSONObject(i)
                    val text = lineObj.optString("value", "")
                    if (text.isNotBlank()) {
                        val start = lineObj.optLong("start", 0L)
                        lines.add(LyricLine(start, text))
                    }
                }

                if (lines.isEmpty()) null else SyncedLyrics(lines, isSynced, offset)
            }
        } catch (e: Exception) {
            null
        }
    }
}