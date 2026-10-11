package com.snapgrab.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.snapgrab.app.model.VideoMetadata
import com.snapgrab.app.service.DownloadService
import com.snapgrab.app.ui.components.QuickDownloadSheet
import com.snapgrab.app.ui.theme.SnapGrabTheme
import com.snapgrab.app.utils.UrlExtractor
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
            SnapGrabTheme {
                var isLoading by remember { mutableStateOf(true) }
                var metadata by remember { mutableStateOf<VideoMetadata?>(null) }
                var errorMessage by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(targetUrl) {
                    try {
                        val fetchedMeta = withContext(Dispatchers.IO) {
                            val request = YoutubeDLRequest(targetUrl)
                            val info = YoutubeDL.getInstance().getInfo(request)
                            VideoMetadata(
                                title = info.title ?: "Online Video",
                                uploader = info.uploader ?: platform,
                                durationText = "${info.duration}s",
                                thumbnailUrl = info.thumbnail,
                                webUrl = targetUrl
                            )
                        }
                        metadata = fetchedMeta
                        isLoading = false
                    } catch (e: Exception) {
                        isLoading = false
                        errorMessage = "Could not fetch stream preview. Tap below to download directly."
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
                        val videoTitle = metadata?.title ?: "Video ($platform)"
                        DownloadService.startDownload(
                            context = this@ShareActivity,
                            url = targetUrl,
                            formatSpec = selectedFormat.formatSpec,
                            title = videoTitle
                        )
                        Toast.makeText(
                            this@ShareActivity,
                            "Download started: $videoTitle",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                )
            }
        }
    }
}
