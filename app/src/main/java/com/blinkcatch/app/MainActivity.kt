package com.blinkcatch.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.blinkcatch.app.model.FormatPresets
import com.blinkcatch.app.service.DownloadService
import com.blinkcatch.app.ui.theme.AccentGradientEnd
import com.blinkcatch.app.ui.theme.AccentGradientStart
import com.blinkcatch.app.ui.theme.BlinkCatchTheme
import com.blinkcatch.app.utils.DownloadFileManager
import com.blinkcatch.app.utils.DownloadedMediaItem
import com.blinkcatch.app.utils.UrlExtractor
import com.yausername.youtubedl_android.YoutubeDL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BlinkCatchTheme {
                MainAppScreen()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainAppScreen() {
        val context = LocalContext.current
        var selectedTab by remember { mutableStateOf(0) } // 0: Downloader, 1: Downloads
        var downloadedFiles by remember { mutableStateOf(listOf<DownloadedMediaItem>()) }
        var isUpdatingEngine by remember { mutableStateOf(false) }

        fun refreshFiles() {
            downloadedFiles = DownloadFileManager.getDownloadedFiles()
        }

        LaunchedEffect(selectedTab) {
            refreshFiles()
        }

        // Notification permission request for Android 13+
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (!granted) {
                Toast.makeText(context, "Notification permission is needed for download progress", Toast.LENGTH_SHORT).show()
            }
        }

        LaunchedEffect(Unit) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_faizit_logo),
                                contentDescription = "Logo",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "FaizIt",
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            )
                        }
                    },
                    actions = {
                        if (selectedTab == 1) {
                            IconButton(onClick = { refreshFiles() }) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh Files")
                            }
                        } else {
                            IconButton(
                                onClick = {
                                    if (!isUpdatingEngine) {
                                        isUpdatingEngine = true
                                        lifecycleScope.launch {
                                            try {
                                                val status = withContext(Dispatchers.IO) {
                                                    YoutubeDL.getInstance().updateYoutubeDL(applicationContext)
                                                }
                                                Toast.makeText(context, "Engine updated: $status", Toast.LENGTH_LONG).show()
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Update failed: ${e.message}", Toast.LENGTH_LONG).show()
                                            } finally {
                                                isUpdatingEngine = false
                                            }
                                        }
                                    }
                                }
                            ) {
                                if (isUpdatingEngine) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Update Extractor Rules"
                                    )
                                }
                            }
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.Download, contentDescription = "Downloader") },
                        label = { Text("Downloader") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (downloadedFiles.isNotEmpty()) {
                                        Badge { Text(downloadedFiles.size.toString()) }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Folder, contentDescription = "My Downloads")
                            }
                        },
                        label = { Text("Downloads") }
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (selectedTab == 0) {
                    DownloaderTabContent()
                } else {
                    DownloadsListTabContent(
                        files = downloadedFiles,
                        onDelete = { fileItem ->
                            DownloadFileManager.deleteFile(context, fileItem.file)
                            refreshFiles()
                        },
                        onOpen = { fileItem ->
                            DownloadFileManager.openFile(context, fileItem.file)
                        },
                        onShare = { fileItem ->
                            DownloadFileManager.shareFile(context, fileItem.file)
                        }
                    )
                }
            }
        }
    }

    @Composable
    fun DownloaderTabContent() {
        val context = LocalContext.current
        var inputUrl by remember { mutableStateOf("") }
        var isAudioOnly by remember { mutableStateOf(false) }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Gradient hero banner
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(
                                    listOf(AccentGradientStart, AccentGradientEnd)
                                )
                            )
                            .padding(22.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = Color.Yellow,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "1-Tap Media Downloader",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "YouTube, Instagram Reels, Twitter (X), TikTok & more.",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            item {
                // Manual URL Input Box
                Text(
                    text = "Download by Link",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = inputUrl,
                    onValueChange = { inputUrl = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Paste video or reel URL...") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Format switch pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !isAudioOnly,
                        onClick = { isAudioOnly = false },
                        label = { Text("🎬 Video (MP4)") },
                        leadingIcon = {
                            if (!isAudioOnly) Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    )
                    FilterChip(
                        selected = isAudioOnly,
                        onClick = { isAudioOnly = true },
                        label = { Text("🎵 Audio Only (MP3)") },
                        leadingIcon = {
                            if (isAudioOnly) Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val cleanUrl = UrlExtractor.extractUrl(inputUrl)
                        if (cleanUrl.isNullOrBlank()) {
                            Toast.makeText(context, "Please paste a valid video URL", Toast.LENGTH_SHORT).show()
                        } else {
                            val platform = UrlExtractor.getPlatformName(cleanUrl)
                            val formatSpec = if (isAudioOnly) {
                                FormatPresets.defaultFormats.first { it.isAudioOnly }.formatSpec
                            } else {
                                FormatPresets.defaultFormats.first { !it.isAudioOnly }.formatSpec
                            }
                            DownloadService.startDownload(
                                context = context,
                                url = cleanUrl,
                                formatSpec = formatSpec,
                                title = "$platform Media",
                                isAudio = isAudioOnly
                            )
                            Toast.makeText(context, "Download started in background!", Toast.LENGTH_SHORT).show()
                            inputUrl = ""
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isAudioOnly) "Download MP3 Audio" else "Download Video")
                }
            }

            item {
                // Quick Guide Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "How Instant Share Works:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "1. Open YouTube, Instagram, or Twitter.\n" +
                                    "2. Tap 'Share' on any video/reel.\n" +
                                    "3. Choose 'FaizIt' from the share sheet.\n" +
                                    "4. Pick Video or MP3, and it downloads directly in the background!",
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    @Composable
    fun DownloadsListTabContent(
        files: List<DownloadedMediaItem>,
        onDelete: (DownloadedMediaItem) -> Unit,
        onOpen: (DownloadedMediaItem) -> Unit,
        onShare: (DownloadedMediaItem) -> Unit
    ) {
        if (files.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Downloads Yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Videos and MP3s you download via Instant Share or URL will appear right here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Saved Media (${files.size})",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                items(files, key = { it.file.absolutePath }) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpen(item) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // File type icon badge
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (item.isAudio)
                                    MaterialTheme.colorScheme.tertiaryContainer
                                else
                                    MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (item.isAudio) Icons.Default.MusicNote else Icons.Default.VideoLibrary,
                                        contentDescription = null,
                                        tint = if (item.isAudio)
                                            MaterialTheme.colorScheme.onTertiaryContainer
                                        else
                                            MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // File metadata
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = item.extension,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${item.sizeFormatted} • ${item.dateFormatted}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Actions
                            Row {
                                IconButton(onClick = { onShare(item) }, modifier = Modifier.size(36.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(onClick = { onDelete(item) }, modifier = Modifier.size(36.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
