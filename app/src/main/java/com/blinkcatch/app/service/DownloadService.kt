package com.blinkcatch.app.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.blinkcatch.app.MainActivity
import com.blinkcatch.app.R
import com.blinkcatch.app.BlinkCatchApp
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File

class DownloadService : Service() {

    companion object {
        const val EXTRA_URL = "extra_video_url"
        const val EXTRA_FORMAT = "extra_video_format"
        const val EXTRA_TITLE = "extra_video_title"
        const val EXTRA_IS_AUDIO = "extra_is_audio"

        private const val PROGRESS_NOTIFICATION_ID = 1001
        private const val TAG = "DownloadService"

        fun startDownload(
            context: Context,
            url: String,
            formatSpec: String,
            title: String? = null,
            isAudio: Boolean = false
        ) {
            val intent = Intent(context, DownloadService::class.java).apply {
                putExtra(EXTRA_URL, url)
                putExtra(EXTRA_FORMAT, formatSpec)
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_IS_AUDIO, isAudio)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private lateinit var notificationManager: NotificationManager
    private var lastProgress = -1

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val url = intent?.getStringExtra(EXTRA_URL)
        val format = intent?.getStringExtra(EXTRA_FORMAT) ?: "best"
        val title = intent?.getStringExtra(EXTRA_TITLE) ?: "Media"
        val isAudio = intent?.getBooleanExtra(EXTRA_IS_AUDIO, false) ?: false

        if (url.isNullOrBlank()) {
            stopSelf()
            return START_NOT_STICKY
        }

        // Start Foreground immediately with high visibility notification
        val initialNotification = buildProgressNotification(title, 0, "Starting download...")
        startForeground(PROGRESS_NOTIFICATION_ID, initialNotification.build())

        serviceScope.launch {
            downloadMedia(url, format, title, isAudio)
        }

        return START_NOT_STICKY
    }

    private fun downloadMedia(url: String, format: String, title: String, isAudio: Boolean) {
        val downloadFolder = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "FaizIt"
        )
        if (!downloadFolder.exists()) {
            downloadFolder.mkdirs()
        }

        // Capture existing files snapshot to detect newly saved file
        val existingFiles = downloadFolder.listFiles()?.map { it.absolutePath }?.toSet() ?: emptySet()

        val request = YoutubeDLRequest(url).apply {
            // Safe filename options: restricts special characters (/ , : * ? " < > |) that crash filesystem
            addOption("-o", "${downloadFolder.absolutePath}/%(title).80B.%(ext)s")
            addOption("--restrict-filenames")
            addOption("--no-playlist")
            addOption("--no-mtime")
            addOption("--no-check-certificates")
            addOption("--user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")

            if (isAudio) {
                // Audio extraction parameters using FFmpeg
                addOption("-x")
                addOption("--audio-format", "mp3")
                addOption("--audio-quality", "0")
            } else {
                // Video parameters
                addOption("-f", "$format/best")
                addOption("--merge-output-format", "mp4")
            }
        }

        try {
            YoutubeDL.getInstance().execute(request) { progress, etaInSeconds, line ->
                val currentProgress = progress.toInt()
                if (currentProgress != lastProgress && currentProgress in 0..100) {
                    lastProgress = currentProgress
                    val etaText = if (etaInSeconds > 0) " (ETA: ${etaInSeconds}s)" else ""
                    val updatedNotification = buildProgressNotification(
                        title,
                        currentProgress,
                        "$currentProgress%$etaText"
                    )
                    notificationManager.notify(PROGRESS_NOTIFICATION_ID, updatedNotification.build())
                }
            }

            // Download finished successfully - detect and scan the new file
            val newFiles = downloadFolder.listFiles()?.filter { it.absolutePath !in existingFiles } ?: emptyList()
            val targetFile = newFiles.firstOrNull() ?: downloadFolder.listFiles()?.maxByOrNull { it.lastModified() }

            handleDownloadSuccess(title, targetFile, isAudio)
        } catch (e: Exception) {
            Log.e(TAG, "Download failed: ${e.message}", e)
            handleDownloadError(title, e.message ?: "Failed to extract/download media")
        } finally {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                stopForeground(true)
            }
            stopSelf()
        }
    }

    private fun handleDownloadSuccess(title: String, file: File?, isAudio: Boolean) {
        // Register file with Android MediaStore so it immediately appears in Gallery & Music players
        file?.let { target ->
            val mimeType = if (isAudio || target.extension.equals("mp3", ignoreCase = true)) "audio/mpeg" else "video/mp4"
            MediaScannerConnection.scanFile(
                applicationContext,
                arrayOf(target.absolutePath),
                arrayOf(mimeType)
            ) { path, uri ->
                Log.d(TAG, "MediaScanner registered: $path -> $uri")
            }
        }

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Completion notification with distinct ID so it NEVER disappears when service stops
        val completionId = (System.currentTimeMillis() % 100000).toInt() + 2000
        val completedNotification = NotificationCompat.Builder(this, BlinkCatchApp.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_download)
            .setContentTitle("Download Complete! ✅")
            .setContentText(if (isAudio) "🎵 $title (Saved as MP3)" else "🎬 $title")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setOngoing(false)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(completionId, completedNotification)
    }

    private fun handleDownloadError(title: String, errorMsg: String) {
        val errorId = (System.currentTimeMillis() % 100000).toInt() + 3000
        val errorNotification = NotificationCompat.Builder(this, BlinkCatchApp.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_download)
            .setContentTitle("Download Failed ❌")
            .setContentText("$title: $errorMsg")
            .setAutoCancel(true)
            .setOngoing(false)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(errorId, errorNotification)
    }

    private fun buildProgressNotification(
        title: String,
        progress: Int,
        statusText: String
    ): NotificationCompat.Builder {
        val openAppIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, BlinkCatchApp.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_download)
            .setContentTitle("Downloading: $title")
            .setContentText(statusText)
            .setProgress(100, progress, progress <= 0)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
