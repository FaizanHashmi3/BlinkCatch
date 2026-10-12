package com.blinkcatch.app.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DownloadedMediaItem(
    val file: File,
    val name: String,
    val sizeFormatted: String,
    val dateFormatted: String,
    val isAudio: Boolean,
    val extension: String
)

object DownloadFileManager {

    val downloadFolder: File
        get() = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "BlinkCatch"
        )

    fun getDownloadedFiles(): List<DownloadedMediaItem> {
        val folder = downloadFolder
        if (!folder.exists() || !folder.isDirectory) {
            return emptyList()
        }

        val files = folder.listFiles { file ->
            !file.isDirectory && !file.name.endsWith(".part") && !file.name.endsWith(".ytdl")
        } ?: return emptyList()

        val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

        return files
            .sortedByDescending { it.lastModified() }
            .map { file ->
                val ext = file.extension.lowercase()
                val isAudio = ext in listOf("mp3", "m4a", "wav", "aac", "opus", "ogg")
                DownloadedMediaItem(
                    file = file,
                    name = file.nameWithoutExtension,
                    sizeFormatted = formatFileSize(file.length()),
                    dateFormatted = dateFormat.format(Date(file.lastModified())),
                    isAudio = isAudio,
                    extension = ext.uppercase()
                )
            }
    }

    fun openFile(context: Context, file: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            val ext = file.extension.lowercase()
            val mimeType = if (ext in listOf("mp3", "m4a", "wav", "aac", "opus", "ogg")) {
                "audio/*"
            } else {
                "video/*"
            }

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "No app found to open this file", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareFile(context: Context, file: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            val ext = file.extension.lowercase()
            val mimeType = if (ext in listOf("mp3", "m4a", "wav", "aac", "opus", "ogg")) {
                "audio/*"
            } else {
                "video/*"
            }

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share Media"))
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to share file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteFile(context: Context, file: File): Boolean {
        return try {
            val deleted = file.delete()
            if (deleted) {
                Toast.makeText(context, "File deleted", Toast.LENGTH_SHORT).show()
            }
            deleted
        } catch (e: Exception) {
            Toast.makeText(context, "Delete failed: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun formatFileSize(bytes: Long): String {
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        return when {
            gb >= 1.0 -> String.format(Locale.US, "%.1f GB", gb)
            mb >= 1.0 -> String.format(Locale.US, "%.1f MB", mb)
            else -> String.format(Locale.US, "%.1f KB", kb)
        }
    }
}
