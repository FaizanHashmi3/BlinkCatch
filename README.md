# ⚡ BlinkCatch - 1-Tap Video Downloader for Android

**BlinkCatch** is an ultra-fast Android application that enables seamless, 1-tap video downloading from **YouTube, Instagram Reels, X (Twitter), TikTok, Facebook**, and more, directly via the native Android Share Sheet without switching away from your current app.

---

## 🌟 Key Features

1. **System Share Sheet Integration (`ACTION_SEND`)**:
   - Tap "Share" on any video in YouTube, Instagram, or Twitter.
   - Select **BlinkCatch**.
   - A lightweight, transparent **BottomSheet overlay** appears directly over the video player.

2. **Format / Resolution Selector**:
   - Choose between **1080p Full HD**, **720p HD**, **480p SD**, or **Audio Only (MP3)**.
   - Shows thumbnail, title, and platform detection.

3. **Background Foreground Download Service**:
   - Download runs seamlessly in the background with persistent notification progress bar (Speed & ETA).
   - Once finished, the video is automatically indexed into the user's phone **Gallery / Google Photos / Downloads**.

4. **Zero Server Cost (On-Device Extraction Engine)**:
   - Uses `youtubedl-android` (modern `yt-dlp` wrapper) with bundled FFmpeg.
   - Runs directly on the device using residential mobile data/WiFi — zero server hosting costs and no IP bans.

5. **Over-The-Air Engine Updates**:
   - In-app refresh button to update `yt-dlp` extraction rules if platforms update their video layouts.

---

## 🚀 How to Build the APK using GitHub Actions (Zero Local Setup)

1. Push this repository to your GitHub account:
   ```bash
   cd BlinkCatch
   git add .
   git commit -m "feat: rename to BlinkCatch"
   git branch -M main
   git remote add origin https://github.com/<YOUR_USERNAME>/BlinkCatch.git
   git push -u origin main
   ```

2. Go to your GitHub repository -> Click on the **Actions** tab.
3. The **"Build BlinkCatch APK"** workflow will run automatically.
4. Once completed (~2-3 minutes), scroll down to **Artifacts** and download **`BlinkCatch-Debug-APK`**.
5. Transfer and install the `.apk` on your Android phone!
