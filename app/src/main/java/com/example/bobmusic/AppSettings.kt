package com.example.bobmusic

import android.content.Context
import android.content.SharedPreferences

class AppSettings(context: Context) {

    // Inside AppSettings class...
    var preloadSongs: Int
        get() = prefs.getInt("preload_songs", 2)
        set(value) = prefs.edit().putInt("preload_songs", value).apply()
    private val prefs: SharedPreferences =
        context.getSharedPreferences("bobmusic_prefs", Context.MODE_PRIVATE)

    var serverUrl: String
        get() = prefs.getString("server_url", "") ?: ""
        set(value) = prefs.edit().putString("server_url", value).apply()

    var username: String
        get() = prefs.getString("username", "") ?: ""
        set(value) = prefs.edit().putString("username", value).apply()

    var password: String
        get() = prefs.getString("password", "") ?: ""
        set(value) = prefs.edit().putString("password", value).apply()

    var userName: String
        get() = prefs.getString("user_name", "") ?: ""
        set(value) = prefs.edit().putString("user_name", value).apply()

    // Audio cache limit in GB (2, 4, 6, 8 or 12)
    var cacheSizeGb: Int
        get() = prefs.getInt("cache_size_gb", 2)
        set(value) = prefs.edit().putInt("cache_size_gb", value).apply()

    // true = queue the whole album so tracks play gaplessly
    var gaplessEnabled: Boolean
        get() = prefs.getBoolean("gapless_enabled", true)
        set(value) = prefs.edit().putBoolean("gapless_enabled", value).apply()

    fun isLoggedIn(): Boolean {
        return serverUrl.isNotBlank() && username.isNotBlank() && password.isNotBlank()
    }

    fun saveCredentials(url: String, user: String, pass: String, name: String) {
        serverUrl = url
        username = user
        password = pass
        userName = name
    }

    // Removes only the login info, so cache size and gapless survive a logout
    fun clearCredentials() {
        prefs.edit()
            .remove("server_url")
            .remove("username")
            .remove("password")
            .remove("user_name")
            .apply()
    }
}