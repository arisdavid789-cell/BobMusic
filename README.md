# BobMusic 🎧

**Your music, from your server or your phone.**

BobMusic is a native Android music player for [Navidrome](https://www.navidrome.org/) and compatible Subsonic servers, with a separate library for music stored on your device. It combines playback, playlists, downloads, an equalizer, and a colorful interface in one app.

## Features

- **Two music sources:** switch between your server library and music stored on your device.
- **Album playback:** start from any track and continue through the album queue.
- **Playlists and favorites:** manage server playlists and keep a liked-songs collection.
- **Song and album downloads:** save individual tracks or complete albums for offline listening.
- **Full-screen player:** view album art, playback controls, lyrics when available, and track audio details such as format, bit depth, sample rate, and bitrate.
- **Equalizer:** adjust playback with built-in presets and custom controls.
- **Personal appearance:** choose from seven color themes, customize RGB colors, and set album art to square, rounded, or circular.
- **Configurable audio cache:** set the cache size from 1 to 20 GB in Settings.

## Get started

### Connect to a server

1. Install and open BobMusic.
2. Enter your Navidrome or compatible Subsonic server URL, username, and password.
3. Connect to browse and play your server library.

### Play music on your device

1. Open **Home** and tap **Library** to open the music-source menu.
2. Choose **Internal storage**.
3. Allow audio access when Android asks, then select a song to play.

BobMusic uses Android's indexed audio library to find music stored in shared device storage.

## Build from source

**Requirements:** Android Studio and an Android device or emulator running Android 7.0 (API 24) or later.

1. Clone this repository and open its folder in Android Studio.
2. Let Gradle sync the project.
3. Run the `app` configuration on a connected device or emulator.

To build a debug APK from the project root:

```powershell
.\gradlew.bat :app:assembleDebug
```

On macOS or Linux:

```bash
./gradlew :app:assembleDebug
```

## Permissions

- **Internet:** connects to your configured music server and streams server tracks.
- **Audio access:** lets you browse and play music stored on your device. Android 13 and later use `READ_MEDIA_AUDIO`; older Android versions use `READ_EXTERNAL_STORAGE`.
- **Download storage:** Android 9 and earlier may ask for storage permission to save music to the device's Music folder.

## Built with

- [Kotlin](https://kotlinlang.org/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose) and Material 3
- [AndroidX Media3](https://developer.android.com/media/media3)
- [OkHttp](https://square.github.io/okhttp/)
- [Coil](https://coil-kt.github.io/coil/)

---

*Made by Aris, Bob, and Panos.*
