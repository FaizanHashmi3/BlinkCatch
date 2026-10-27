# ⚡ FaizIt - Ultra-Fast 1-Tap Media Downloader for Android

**FaizIt** is a high-performance, privacy-first Android application that enables instant, 1-tap video and audio extraction from **YouTube, Instagram Reels, X (Twitter), TikTok, Facebook**, and web video sources directly via Android's native Share Sheet without switching apps.

Built and architected by **Faizan Hashmi** ([@FaizanHashmi3](https://github.com/FaizanHashmi3) • `mdfaizanhashmi3@gmail.com`).

---

## 🌟 Key Features

1. **Native Share Sheet Integration (`ACTION_SEND`)**:
   - Tap "Share" on any video in Instagram, YouTube, X, or TikTok.
   - Select **FaizIt**.
   - A lightweight, transparent bottom sheet appears directly over the video player.

2. **Multi-Resolution & Format Extractor**:
   - Pick between **1080p Full HD**, **720p HD**, **480p SD**, or **Studio MP3 Audio**.
   - Live thumbnail, creator attribution, and platform recognition.

3. **Background Foreground Download Service**:
   - Runs in the background with persistent notification progress (Speed & ETA).
   - Once finished, files are indexed into **Gallery / Google Photos / Downloads / FaizIt** via Android `MediaScannerConnection`.

4. **Zero Server Footprint (On-Device Extraction Engine)**:
   - Uses `youtubedl-android` (modern `yt-dlp` wrapper) with bundled FFmpeg.
   - Executes directly on the user's phone via residential mobile IP — zero server hosting costs, zero rate limits, and 100% private.

5. **Over-The-Air Engine Updates**:
   - In-app refresh button to update `yt-dlp` extraction rules if platforms update their layouts.

---

## 🚀 How to Build the APK using GitHub Actions

1. Push this repository to GitHub under `FaizanHashmi3/FaizIt`:
   ```bash
   git add .
   git commit -m "feat: FaizIt release"
   git push -u origin main
   ```

2. Go to your GitHub repository -> Click on the **Actions** tab.
3. The **"Build FaizIt APK"** workflow runs automatically.
4. Once completed, download **`FaizIt-arm64-APK`** from Artifacts and install it on your device!

---

## 🌐 Official Marketing Website
The official marketing website and live simulator is located in `/docs` (or `/website`) and ready for GitHub Pages hosting.
- **Creator**: Faizan Hashmi
- **GitHub**: [FaizanHashmi3](https://github.com/FaizanHashmi3)
- **Contact**: `mdfaizanhashmi3@gmail.com`
