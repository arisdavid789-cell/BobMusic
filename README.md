[README.md](https://github.com/user-attachments/files/34432814/README.md)
# 🎵 BobMusic

**BobMusic** is a modern, high-fidelity native Android music player designed specifically for self-hosted **Navidrome** and **Subsonic** servers. Built with **Jetpack Compose** and an **iOS-inspired Liquid Glass UI**, BobMusic streams your personal music library bit-for-bit without lossy transcoding.

---

## ✨ Features

- 🎧 **Pure Bitstream Quality**: Streams original audio formats (FLAC, ALAC, WAV, MP3) directly from your server without transcoding.
- ⚡ **Gapless Playback**: Seamless track transitions powered by **AndroidX Media3 (ExoPlayer)**.
- 📊 **Parametric Equalizer**: Built-in multi-band parametric EQ with pre-amp gain control, customizable band frequencies, and Q-factor sliders.
- 🖼️ **Dynamic Palette Player**: Full-screen player featuring dynamic color gradients automatically extracted from album artwork.
- 📜 **Synchronized Lyrics**: Real-time timed lyrics view synced to playback.
- 🏷️ **Hi-Res Quality Badges**: Displays format details, bit depth, and sampling rate (e.g., `FLAC 24-bit / 96 kHz`).
- 💾 **Smart Audio Cache**: Configurable local caching (1 GB to 20 GB) for instant replay and reduced server bandwidth usage.
- 🎨 **Theme Engine**: Support for multiple vibrant themes including *Midnight Purple*, *Forest*, *Sunset*, and *Mono*.

---

## 🛠️ Tech Stack

| Component | Technology |
| :--- | :--- |
| **Language** | [Kotlin](https://kotlinlang.org/) |
| **UI Framework** | [Jetpack Compose](https://developer.android.com/jetpack/compose) & Material 3 |
| **Media Engine** | [AndroidX Media3 / ExoPlayer](https://developer.android.com/media/media3) |
| **Networking** | [OkHttp 4](https://square.github.io/okhttp/) & Subsonic REST API |
| **Image Loading** | [Coil Compose](https://coil-kt.github.io/coil/) |
| **Palette Analysis** | AndroidX Palette KTX |



### Prerequisites

1. A running [Navidrome](https://www.navidrome.org/) or Subsonic-compatible music server.
2. Android device running **Android 7.0 (API level 24)** or higher.

### Connecting to Your Server

1. Launch **BobMusic**.
2. Enter your Navidrome server address (e.g., `https://music.yourdomain.com`).
3. Provide your username and password.
4. Tap **Connect** to sync your library.

---

## 🛠️ Building from Source

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/BobMusic.git
   cd BobMusic
   ```

2. Open the project in **Android Studio (2024.1+)**.

3. Build and install on your connected device or emulator:
   ```bash
   ./gradlew assembleDebug
   ```

---

## 🔒 Privacy & Permissions

- **Network Access**: Required to stream audio and fetch metadata/lyrics from your Navidrome server.
- **Data Ownership**: BobMusic never collects personal data, analytics, or telemetry. All media and server credentials remain strictly between your device and your server.

---

## 📄 License

BobMusic is open-source software licensed under the [MIT License](LICENSE).

