package com.blinkcatch.app.model

data class VideoFormatItem(
    val id: String,
    val label: String,
    val description: String,
    val formatSpec: String,
    val isAudioOnly: Boolean = false
)

data class VideoMetadata(
    val title: String,
    val uploader: String,
    val durationText: String,
    val thumbnailUrl: String?,
    val webUrl: String
)

object FormatPresets {
    val defaultFormats = listOf(
        VideoFormatItem(
            id = "1080p",
            label = "1080p Full HD",
            description = "High quality video",
            formatSpec = "bestvideo[height<=1080]+bestaudio/best[height<=1080]/best"
        ),
        VideoFormatItem(
            id = "720p",
            label = "720p HD",
            description = "Fast download, great quality",
            formatSpec = "bestvideo[height<=720]+bestaudio/best[height<=720]/best"
        ),
        VideoFormatItem(
            id = "480p",
            label = "480p SD",
            description = "Saves mobile data",
            formatSpec = "bestvideo[height<=480]+bestaudio/best[height<=480]/best"
        ),
        VideoFormatItem(
            id = "audio",
            label = "Audio Only (MP3)",
            description = "Extract audio only",
            formatSpec = "bestaudio/best",
            isAudioOnly = true
        )
    )
}
