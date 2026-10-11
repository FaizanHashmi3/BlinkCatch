package com.snapgrab.app.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.snapgrab.app.MainActivity
import com.snapgrab.app.R
import com.snapgrab.app.SnapGrabApp
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
        private const val NOTIFICATION_ID = 1001
        private const val TAG = "DownloadService"

        fun startDownload(context: Context, url: String, formatSpec: String, title: String? = null) {
            val intent = Intent(context, DownloadService::class.java).apply {
                putExtra(EXTRA_URL, url)
                putExtra(EXTRA_FORMAT, formatSpec)
                putExtra(EXTRA_TITLE, title)
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
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
        val title = intent?.getStringExtra(EXTRA_TITLE) ?: "Video"

        if (url.isNullOrBlank()) {
            stopSelf()
            return START_NOT_STICKY
        }

        // Start Foreground immediately
        val initialNotification = buildProgressNotification(title, 0, "Starting download...")
        startForeground(NOTIFICATION_ID, initialNotification.build())

        serviceScope.launch {
            downloadVideo(url, format, title)
        }

        return START_NOT_STICKY
    }

    private fun downloadVideo(url: String, format: String, title: String) {
        val downloadFolder = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "SnapGrab"
        )
        if (!downloadFolder.exists()) {
            downloadFolder.mkdirs()
        }

        val request = YoutubeDLRequest(url).apply {
            addOption("-o", "${downloadFolder.absolutePath}/%(title)s.%(ext)s")
            addOption("-f", format)
            addOption("--no-mtime")
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
                    notificationManager.notify(NOTIFICATION_ID, updatedNotification.build())
                }
            }

            // Download Completed
            handleDownloadSuccess(title, downloadFolder)
        } catch (e: Exception) {
            Log.e(TAG, "Download failed: ${e.message}", e)
            handleDownloadError(title, e.message ?: "Unknown error")
        } finally {
            stopForeground(false)
            stopSelf()
        }
    }

    private fun handleDownloadSuccess(title: String, folder: File) {
        // Scan new files into Android MediaStore so they show up in Gallery
        MediaScannerConnection.scanFile(
            applicationContext,
            arrayOf(folder.absolutePath),
            null
        ) { path, uri ->
            Log.d(TAG, "Scanned $path: $uri")
        }

        val openAppIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val completedNotification = NotificationCompat.Builder(this, SnapGrabApp.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_download)
            .setContentTitle("Download Complete")
            .setContentText(title)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setOngoing(false)
            .build()

        notificationManager.notify(NOTIFICATION_ID, completedNotification)
    }

    private fun handleDownloadError(title: String, errorMsg: String) {
        val errorNotification = NotificationCompat.Builder(this, SnapGrabApp.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_download)
            .setContentTitle("Download Failed: $title")
            .setContentText(errorMsg)
            .setAutoCancel(true)
            .setOngoing(false)
            .build()

        notificationManager.notify(NOTIFICATION_ID, errorNotification)
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

        return NotificationCompat.Builder(this, SnapGrabApp.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_download)
            .setContentTitle(title)
            .setContentText(statusText)
            .setProgress(100, progress, progress <= 0)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
