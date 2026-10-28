package com.blinkcatch.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import com.blinkcatch.app.model.VideoMetadata
import com.blinkcatch.app.service.DownloadService
import com.blinkcatch.app.ui.components.QuickDownloadSheet
import com.blinkcatch.app.ui.theme.BlinkCatchTheme
import com.blinkcatch.app.utils.UrlExtractor
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ShareActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedText = intent?.getStringExtra(Intent.EXTRA_TEXT)
        val targetUrl = UrlExtractor.extractUrl(sharedText)

        if (targetUrl.isNullOrBlank()) {
            Toast.makeText(this, "No valid video link found in shared content", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val platform = UrlExtractor.getPlatformName(targetUrl)

        setContent {
            BlinkCatchTheme {
                var isLoading by remember { mutableStateOf(true) }
                var metadata by remember { mutableStateOf<VideoMetadata?>(null) }
                var errorMessage by remember { mutableStateOf<String?>(null) }

                // Check notification permission for Android 13+
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { /* proceed */ }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(this@ShareActivity, Manifest.permission.POST_NOTIFICATIONS)
                            != PackageManager.PERMISSION_GRANTED) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                LaunchedEffect(targetUrl) {
                    try {
                        val fetchedMeta = withContext(Dispatchers.IO) {
                            val request = YoutubeDLRequest(targetUrl).apply {
                                addOption("--no-playlist")
                                addOption("--no-check-certificates")
                                addOption("--no-warnings")
                                addOption("--no-update")
                                addOption("--add-header", "Accept-Language: en-US,en;q=0.9")
                            }
                            val info = YoutubeDL.getInstance().getInfo(request)
                            VideoMetadata(
                                title = info.title ?: "Online Video",
                                uploader = info.uploader ?: platform,
                                durationText = if (info.duration > 0) "${info.duration}s" else "",
                                thumbnailUrl = info.thumbnail,
                                webUrl = targetUrl
                            )
                        }
                        metadata = fetchedMeta
                        isLoading = false
                    } catch (e: Exception) {
                        isLoading = false
                        errorMessage = "Could not fetch preview. Tap below to download directly."
                    }
                }

                QuickDownloadSheet(
                    url = targetUrl,
                    platform = platform,
                    isLoading = isLoading,
                    metadata = metadata,
                    errorMessage = errorMessage,
                    onDismiss = { finish() },
                    onDownloadSelected = { selectedFormat ->
                        val mediaTitle = metadata?.title ?: "Media ($platform)"
                        DownloadService.startDownload(
                            context = this@ShareActivity,
                            url = targetUrl,
                            formatSpec = selectedFormat.formatSpec,
                            title = mediaTitle,
                            isAudio = selectedFormat.isAudioOnly
                        )
                        val typeText = if (selectedFormat.isAudioOnly) "audio" else "video"
                        Toast.makeText(
                            this@ShareActivity,
                            "Downloading $typeText in background...",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                )
            }
        }
    }
}
