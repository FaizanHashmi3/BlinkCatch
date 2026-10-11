package com.snapgrab.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SnapGrabApp : Application() {

    companion object {
        const val CHANNEL_ID = "snapgrab_downloads"
        private const val TAG = "SnapGrabApp"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        // Asynchronously initialize YoutubeDL & FFmpeg so app starts in milliseconds
        CoroutineScope(Dispatchers.IO).launch {
            try {
                YoutubeDL.getInstance().init(applicationContext)
                FFmpeg.getInstance().init(applicationContext)
                Log.d(TAG, "YoutubeDL & FFmpeg initialized successfully")
            } catch (e: YoutubeDLException) {
                Log.e(TAG, "Failed to initialize YoutubeDL: ${e.message}", e)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_download_name)
            val descriptionText = getString(R.string.channel_download_desc)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
            }
            val notificationManager: NotificationManager =
                getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}
