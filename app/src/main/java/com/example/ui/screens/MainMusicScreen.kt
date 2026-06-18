package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.MusicVideo
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.palette.graphics.Palette
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.LinearGradient
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Playlist
import com.example.data.Song
import com.example.ui.MusicViewModel
import com.example.ui.SmartShuffleMode
import com.example.ui.EqPreset
import com.example.ui.SongMetadataHelper
import com.example.ui.components.WaveformVisualizer
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import androidx.compose.ui.text.TextStyle
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.text.font.FontFamily


@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun MainMusicScreen(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val songs by viewModel.allSongs.collectAsStateWithLifecycle()
    val playlists by viewModel.allPlaylists.collectAsStateWithLifecycle()
    
    val currentSong by viewModel.currentSong.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val isOfflineMode by viewModel.isOfflineMode.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val recentSearches by viewModel.recentSearches.collectAsStateWithLifecycle()
    val downloadingSongs by viewModel.downloadingSongs.collectAsStateWithLifecycle()

    // Collect settings personalizations state
    val synthVolume by viewModel.synthVolume.collectAsStateWithLifecycle()
    val synthWaveformType by viewModel.synthWaveformType.collectAsStateWithLifecycle()
    val waveformStyleStr by viewModel.waveformStyleStr.collectAsStateWithLifecycle()
    val showDailyRecommended by viewModel.showDailyRecommended.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()

    val sleepTimerMinutesLeft by viewModel.sleepTimerMinutesLeft.collectAsStateWithLifecycle()
    val eqBass by viewModel.eqBass.collectAsStateWithLifecycle()
    val eqMid by viewModel.eqMid.collectAsStateWithLifecycle()
    val eqTreble by viewModel.eqTreble.collectAsStateWithLifecycle()
    val selectedEqPreset by viewModel.selectedEqPreset.collectAsStateWithLifecycle()
    val crossfadeSecs by viewModel.crossfadeSecs.collectAsStateWithLifecycle()

    val syncProjectId by viewModel.syncProjectId.collectAsStateWithLifecycle()
    val syncCode by viewModel.syncCode.collectAsStateWithLifecycle()
    val syncApiKey by viewModel.syncApiKey.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val wifiAutoSyncEnabled by viewModel.wifiAutoSyncEnabled.collectAsStateWithLifecycle()
    val smartShuffleMode by viewModel.smartShuffleMode.collectAsStateWithLifecycle()
    val likedSongIds by viewModel.likedSongIds.collectAsStateWithLifecycle()
    val playbackQueue by viewModel.playbackQueueState.collectAsStateWithLifecycle()

    var realtimeWaveform by remember { mutableStateOf(FloatArray(128)) }
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isActive) {
                realtimeWaveform = viewModel.getRealtimeWaveform()
                delay(16) // ~60 FPS rate
            }
        } else {
            realtimeWaveform = FloatArray(128)
        }
    }

    var activeTab by remember { mutableIntStateOf(0) } // 0: Biblioteca, 1: Playlist, 2: Descargados, 3: Ajustes
    var isPlayerExpanded by remember { mutableStateOf(false) }

    // Settings & Dialog visibility states
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var songToAssignPlaylist by remember { mutableStateOf<Song?>(null) }

    // Download modal / dialog options
    var songToDownload by remember { mutableStateOf<Song?>(null) }
    var showDownloadConfirmationDialog by remember { mutableStateOf(false) }
    var selectedDownloadQuality by remember { mutableStateOf("Alta Fidelidad (320 kbps)") }

    // Content Provider and Permissions Scanning
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val permissionToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val scanSystemAudio: ((Int) -> Unit) -> Unit = { onCompleted ->
        val songList = mutableListOf<Song>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        try {
            val cursor = context.contentResolver.query(uri, projection, selection, null, null)
            cursor?.use {
                val idCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

                while (it.moveToNext()) {
                    val id = "sys_" + it.getString(idCol)
                    val title = it.getString(titleCol) ?: "Audio Local"
                    val artist = it.getString(artistCol) ?: "Dispositivo"
                    val album = it.getString(albumCol) ?: "Local"
                    val durationMs = it.getInt(durationCol)
                    val durationSec = if (durationMs > 0) durationMs / 1000 else 180
                    val seed = 220f + (title.length * 15f) % 400f

                    songList.add(
                        Song(
                            id = id,
                            title = title,
                            artist = artist,
                            album = album,
                            durationSecs = durationSec,
                            isDownloaded = true,
                            genre = "Local",
                            frequencySeed = seed
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // If no user system songs exist, gracefully preload system audios
        if (songList.isEmpty()) {
            songList.add(Song("sys_mock_1", "Grabación de Ensayo", "Estudio Local", "Grabaciones", 112, true, "Local", 329.63f))
            songList.add(Song("sys_mock_2", "Nota de Voz WhatsApp", "Grupo Banda", "Media Local", 42, true, "Voice", 493.88f))
            songList.add(Song("sys_mock_3", "Tono de Prueba", "Dispositivo", "Alarmas OS", 75, true, "System", 554.37f))
        }

        viewModel.addNewSongs(songList)
        onCompleted(songList.size)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "Permiso concedido. Escaneando audios...", Toast.LENGTH_SHORT).show()
            scanSystemAudio { count ->
                Toast.makeText(context, "Se agregaron $count audios locales.", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(context, "Permiso denegado. No se pueden cargar audios del sistema.", Toast.LENGTH_LONG).show()
        }
    }

    val launchScanning = {
        val checkPermission = ContextCompat.checkSelfPermission(context, permissionToRequest)
        if (checkPermission == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Iniciando escaneo de audios...", Toast.LENGTH_SHORT).show()
            scanSystemAudio { count ->
                Toast.makeText(context, "Escaneo completado. $count audios locales cargados.", Toast.LENGTH_LONG).show()
            }
        } else {
            permissionLauncher.launch(permissionToRequest)
        }
    }

    // Filter songs based on Tab & Offline state & Search Query
    val filteredSongs = remember(songs, activeTab, isOfflineMode, searchQuery) {
        songs.filter { song ->
            // Offline Filter
            val matchesOffline = !isOfflineMode || song.isDownloaded
            
            // Tab Filter (Tab 2 shows only downloaded files)
            val matchesTab = if (activeTab == 2) song.isDownloaded else true
            
            // Search filter
            val matchesSearch = song.title.contains(searchQuery, ignoreCase = true) || 
                                song.artist.contains(searchQuery, ignoreCase = true) ||
                                song.album.contains(searchQuery, ignoreCase = true)

            matchesOffline && matchesTab && matchesSearch
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            if (!isPlayerExpanded) {
                Column(
                    modifier = Modifier
                        .background(DeepSableSpace)
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                // Header with custom title & offline selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "AuraBeat",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = NeonViolet,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = "Music Deck",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = NeonCyan,
                                letterSpacing = 2.sp
                            )
                        )
                    }

                    // Gorgeous Offline Toggle
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isOfflineMode) NeonCyan.copy(alpha = 0.15f) else DarkSurfaceCard)
                            .border(
                                width = 1.dp,
                                color = if (isOfflineMode) NeonCyan else BorderAmbient,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { viewModel.toggleOfflineMode() }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isOfflineMode) Icons.Default.CloudOff else Icons.Default.CloudQueue,
                            contentDescription = "Offline trigger",
                            tint = if (isOfflineMode) NeonCyan else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isOfflineMode) "Modo Offline" else "Modo Online",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isOfflineMode) NeonCyan else TextSecondary
                        )
                        Switch(
                            checked = isOfflineMode,
                            onCheckedChange = { viewModel.toggleOfflineMode() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NeonCyan,
                                checkedTrackColor = NeonCyan.copy(alpha = 0.3f),
                                uncheckedThumbColor = TextSecondary,
                                uncheckedTrackColor = DeepSableSpace
                            ),
                            modifier = Modifier.scale(0.6f).height(12.dp)
                        )
                    }
                }

                if (activeTab != 3) {
                    Spacer(modifier = Modifier.height(12.dp))

                    // Modern premium Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("search_bar"),
                        placeholder = {
                            Text(
                                "Buscar por canción, artista...",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Buscar",
                                tint = NeonViolet
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Limpiar",
                                        tint = TextSecondary
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = DarkSurfaceCard,
                            unfocusedContainerColor = DarkSurfaceCard,
                            focusedBorderColor = NeonViolet,
                            unfocusedBorderColor = BorderAmbient
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )

                    // Recent Searches section
                    if (recentSearches.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "BÚSQUEDAS RECIENTES",
                                color = NeonCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Limpiar Todo",
                                color = TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Normal,
                                modifier = Modifier
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.clearRecentSearches()
                                    }
                                    .testTag("clear_recent_searches")
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(recentSearches) { recent ->
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(DarkSurfaceCard)
                                        .border(1.dp, BorderAmbient, RoundedCornerShape(16.dp))
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            viewModel.setSearchQuery(recent.query)
                                        }
                                        .padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
                                        .testTag("recent_search_chip_${recent.query}"),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = recent.query,
                                        color = TextPrimary,
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(
                                        onClick = {
                                            viewModel.deleteRecentSearch(recent.query)
                                        },
                                        modifier = Modifier.size(24.dp).testTag("delete_recent_search_${recent.query}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Eliminar",
                                            tint = TextSecondary,
                                            modifier = Modifier.size(10.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
            }
        },
        bottomBar = {
            if (!isPlayerExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurfaceCard)
                ) {
                    // 1. Mini player when contracted, floating above bottom navigation
                    if (currentSong != null && !isPlayerExpanded) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            MiniPlayerBar(
                                song = currentSong!!,
                                isPlaying = isPlaying,
                                progressSec = viewModel.currentPlayProgress.collectAsStateWithLifecycle().value,
                                muteSynth = viewModel.synthMuted.collectAsStateWithLifecycle().value,
                                onPlayPause = { viewModel.togglePlayPause() },
                                onNext = { viewModel.nextSong() },
                                onPrev = { viewModel.prevSong() },
                                isShuffle = viewModel.isShuffleEnabled.collectAsStateWithLifecycle().value,
                                isRepeat = viewModel.isRepeatEnabled.collectAsStateWithLifecycle().value,
                                onToggleShuffle = { viewModel.toggleShuffle() },
                                onToggleRepeat = { viewModel.toggleRepeat() },
                                onToggleMute = { viewModel.toggleSynthMute() },
                                onClick = { isPlayerExpanded = true }
                            )
                        }
                    }

                    // 2. Beautiful material bottom Navigation Bar according to design theme
                    NavigationBar(
                        containerColor = DarkSurfaceCard,
                        contentColor = NeonViolet,
                        tonalElevation = 8.dp,
                        windowInsets = WindowInsets.navigationBars,
                        modifier = Modifier.border(
                            width = (0.5).dp,
                            color = BorderAmbient,
                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                        )
                    ) {
                        NavigationBarItem(
                            selected = activeTab == 0,
                            onClick = { activeTab = 0 },
                            label = { Text("Biblioteca", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                            icon = {
                                Icon(
                                    imageVector = if (activeTab == 0) Icons.Default.LibraryMusic else Icons.Outlined.LibraryMusic,
                                    contentDescription = "Biblioteca"
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PlayerOnSoftLilacColor,
                                selectedTextColor = PlayerOnSoftLilacColor,
                                indicatorColor = SoftLilac,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            )
                        )

                        NavigationBarItem(
                            selected = activeTab == 1,
                            onClick = { activeTab = 1 },
                            label = { Text("Listas", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                            icon = {
                                Icon(
                                    imageVector = if (activeTab == 1) Icons.Default.QueueMusic else Icons.Outlined.QueueMusic,
                                    contentDescription = "Listas"
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PlayerOnSoftLilacColor,
                                selectedTextColor = PlayerOnSoftLilacColor,
                                indicatorColor = SoftLilac,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            )
                        )

                        NavigationBarItem(
                            selected = activeTab == 2,
                            onClick = { activeTab = 2 },
                            label = { Text("Descargados", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                            icon = {
                                Icon(
                                    imageVector = if (activeTab == 2) Icons.Default.CloudQueue else Icons.Outlined.CloudQueue,
                                    contentDescription = "Descargados"
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PlayerOnSoftLilacColor,
                                selectedTextColor = PlayerOnSoftLilacColor,
                                indicatorColor = SoftLilac,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            )
                        )

                        NavigationBarItem(
                            selected = activeTab == 3,
                            onClick = { activeTab = 3 },
                            label = { Text("Ajustes", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                            icon = {
                                Icon(
                                    imageVector = if (activeTab == 3) Icons.Default.Settings else Icons.Outlined.Settings,
                                    contentDescription = "Ajustes"
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PlayerOnSoftLilacColor,
                                selectedTextColor = PlayerOnSoftLilacColor,
                                indicatorColor = SoftLilac,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DeepSableSpace)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
            // Active offline notification banner
            Column(modifier = Modifier.fillMaxSize()) {
                if (isOfflineMode) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(NeonCyan.copy(alpha = 0.15f))
                            .border(width = (0.5).dp, color = NeonCyan.copy(alpha = 0.3f))
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(GreenStatus)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "MODO OFFLINE ACTIVO — Solo música local disponible",
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Screen main lists
                when (activeTab) {
                    0, 2 -> { // Biblioteca or Descargados
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(top = 12.dp, bottom = 120.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Render beautiful Hero Mix Card inside Biblioteca Tab at the very top
                            if (activeTab == 0) {
                                item {
                                    HeroCard(
                                        onPlaySmart = {
                                            if (songs.isNotEmpty()) {
                                                val randomSong = songs.random()
                                                viewModel.playSongNow(randomSong, songs)
                                            }
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "TU BIBLIOTECA",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextSecondary,
                                        letterSpacing = 1.sp,
                                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                                    )
                                }
                            }

                            if (filteredSongs.isEmpty()) {
                                item {
                                    EmptyStatePlaceholder(
                                        message = if (activeTab == 2) "Aún no tienes música descargada offline." 
                                                  else "No se encontraron canciones en tu biblioteca.",
                                        suggestion = if (activeTab == 2) "Busca canciones en la Biblioteca y presiona el icono de descarga para guardarlas."
                                                     else "Intenta cambiar los términos de búsqueda o desactiva el modo offline."
                                    )
                                }
                            } else {
                                items(filteredSongs) { song ->
                                    SongItemCard(
                                        song = song,
                                        isCurrent = currentSong?.id == song.id,
                                        isPlaying = isPlaying,
                                        isFavorite = likedSongIds.contains(song.id),
                                        downloadProgress = downloadingSongs[song.id],
                                        onPlay = { viewModel.playSongNow(song, filteredSongs) },
                                        onAddToPlaylist = { songToAssignPlaylist = song },
                                        onDownload = {
                                            if (song.isDownloaded) {
                                                viewModel.triggerSongDownload(song)
                                            } else {
                                                songToDownload = song
                                                showDownloadConfirmationDialog = true
                                            }
                                        },
                                        onToggleFavorite = { viewModel.toggleFavorite(song.id) }
                                    )
                                }
                            }
                        }
                    }

                    1 -> { // Playlists View
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Create playlist trigger button
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Tus Listas de Reproducción",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Button(
                                    onClick = { showCreatePlaylistDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Crear Lista", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (playlists.isEmpty()) {
                                EmptyStatePlaceholder(
                                    message = "No tienes listas de reproducción personalizadas.",
                                    suggestion = "Crea una lista arriba para agrupar y organizar tu música favorita."
                                )
                            } else {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 16.dp),
                                    contentPadding = PaddingValues(bottom = 100.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    items(playlists) { playlistWithSongs ->
                                        // Filter songs in playlist if offline mode is on
                                        val validPlaylistSongs = if (isOfflineMode) {
                                            playlistWithSongs.songs.filter { it.isDownloaded }
                                        } else {
                                            playlistWithSongs.songs
                                        }

                                        PlaylistCard(
                                            playlist = playlistWithSongs.playlist, currentSong = currentSong, isPlaying = isPlaying, likedSongIds = likedSongIds, onPlaySong = { song -> viewModel.playSongNow(song, validPlaylistSongs) }, onToggleFavorite = { songId -> viewModel.toggleFavorite(songId) },
                                            songs = validPlaylistSongs,
                                            isOffline = isOfflineMode,
                                            onPlayAll = {
                                                if (validPlaylistSongs.isNotEmpty()) {
                                                    viewModel.playSongNow(validPlaylistSongs.first(), validPlaylistSongs)
                                                }
                                            },
                                            onRemoveSong = { songId ->
                                                viewModel.removeSongFromPlaylist(playlistWithSongs.playlist.id, songId)
                                            },
                                            onDeletePlaylist = {
                                                viewModel.deletePlaylist(playlistWithSongs.playlist)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    3 -> { // Ajustes View
                        var showQuickstartGuide by remember { mutableStateOf(true) }
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Title with show/hide Quick Guide
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Ajustes del Reproductor",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                TextButton(
                                    onClick = { showQuickstartGuide = !showQuickstartGuide },
                                    colors = ButtonDefaults.textButtonColors(contentColor = NeonCyan)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (showQuickstartGuide) "Ocultar Guía" else "Guía Rápida",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Interactive quick learning guide
                            androidx.compose.animation.AnimatedVisibility(
                                visible = showQuickstartGuide,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                                    border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .background(NeonCyan.copy(alpha = 0.15f), CircleShape)
                                                    .padding(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = null,
                                                    tint = NeonCyan,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            Text(
                                                text = "APRENDIZAJE RÁPIDO Y PREMIUM",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = NeonCyan,
                                                letterSpacing = 1.sp
                                            )
                                        }

                                        Text(
                                            text = "¡Aprende a dominar AuraBeat en 30 segundos! Súper fácil y rápido:",
                                            fontSize = 12.sp,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Medium
                                        )

                                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                            // Bullet 1
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.MusicNote,
                                                    contentDescription = null,
                                                    tint = NeonViolet,
                                                    modifier = Modifier.size(16.dp).padding(top = 2.dp)
                                                )
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        "1. Reproducción Offline Instantánea",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = TextPrimary
                                                    )
                                                    Text(
                                                        "La app genera música retro synthesized de forma nativa en tu dispositivo. No consume datos y funciona 100% offline.",
                                                        fontSize = 10.sp,
                                                        color = TextSecondary
                                                    )
                                                }
                                            }

                                            // Bullet 2
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Sync,
                                                    contentDescription = null,
                                                    tint = NeonCyan,
                                                    modifier = Modifier.size(16.dp).padding(top = 2.dp)
                                                )
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        "2. ¿Cómo Sincronizar tus Listas?",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = TextPrimary
                                                    )
                                                    Text(
                                                        "Para pasar tus listas a otro celular, pon el mismo 'Código de Sincronización' (ej: sync-abc) en ambos y pulsa 'Sincronizar'. Se fusionarán al instante sin complicaciones.",
                                                        fontSize = 10.sp,
                                                        color = TextSecondary
                                                    )
                                                }
                                            }

                                            // Bullet 3
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CloudQueue,
                                                    contentDescription = null,
                                                    tint = HotPink,
                                                    modifier = Modifier.size(16.dp).padding(top = 2.dp)
                                                )
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        "3. ¿Tienes un Error al Sincronizar?",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = TextPrimary
                                                    )
                                                    Text(
                                                        "Si sale 'Fallo al conectar', es porque Firestore requiere tu propia base de datos. Ve a console.firebase.google.com, crea una base Firestore en 'Modo de Prueba' (Público), y pon tu 'Project ID' en los ajustes avanzados de abajo.",
                                                        fontSize = 10.sp,
                                                        color = TextSecondary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            
                            HorizontalDivider(color = BorderAmbient.copy(alpha = 0.5f))

                            // SECTION 1: SÍNTESIS DE AUDIO (SINTETIZADOR)
                            Text(
                                "MOTOR DE SÍNTESIS DE AUDIO",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan,
                                letterSpacing = 1.sp
                            )

                            // Waveform Selector Card/Section
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                                border = BorderStroke(0.5.dp, BorderAmbient),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("Forma de Onda (Chiptune)", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                                        Text("Modifica el timbre físico del sintetizador analógico en tiempo real.", fontSize = 11.sp, color = TextSecondary)
                                    }
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        listOf("Sine", "Square", "Triangle", "Sawtooth").forEach { wave ->
                                            val isSelected = synthWaveformType == wave
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(if (isSelected) NeonViolet.copy(alpha = 0.25f) else DeepSableSpace)
                                                    .border(
                                                        width = 1.dp,
                                                        color = if (isSelected) NeonViolet else BorderAmbient,
                                                        shape = RoundedCornerShape(10.dp)
                                                    )
                                                    .clickable { viewModel.setSynthWaveform(wave) }
                                                    .padding(vertical = 10.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = when(wave) {
                                                        "Sine" -> "Seno"
                                                        "Square" -> "Cuadrada"
                                                        "Triangle" -> "Triángulo"
                                                        "Sawtooth" -> "Sierra"
                                                        else -> wave
                                                    },
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) NeonCyan else TextSecondary
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Synth volume slider Card/Section
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                                border = BorderStroke(0.5.dp, BorderAmbient),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Atenuación del Sintetizador", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                                        Text("${(synthVolume * 100).toInt()}%", fontSize = 12.sp, color = NeonCyan, fontWeight = FontWeight.Bold)
                                    }
                                    Text("Protege tu audición regulando el volumen de salida de los osciladores.", fontSize = 11.sp, color = TextSecondary)
                                    
                                    Slider(
                                        value = synthVolume,
                                        onValueChange = { viewModel.setSynthVolume(it) },
                                        valueRange = 0.02f..0.35f,
                                        colors = SliderDefaults.colors(
                                            thumbColor = NeonCyan,
                                            activeTrackColor = NeonCyan,
                                            inactiveTrackColor = DeepSableSpace
                                        ),
                                        modifier = Modifier.height(28.dp)
                                    )
                                }
                            }

                            GraphicEqualizerCard(
                                bass = eqBass,
                                mid = eqMid,
                                treble = eqTreble,
                                onBassChange = { viewModel.setEqBass(it) },
                                onMidChange = { viewModel.setEqMid(it) },
                                onTrebleChange = { viewModel.setEqTreble(it) },
                                selectedPreset = selectedEqPreset,
                                onPresetSelected = { viewModel.selectEqPreset(it) },
                                isPlaying = isPlaying,
                                realtimeWaveform = realtimeWaveform
                            )

                            SleepTimerDialCard(
                                minutesLeft = sleepTimerMinutesLeft,
                                onSetTimer = { viewModel.setSleepTimer(it) }
                            )

                            // Audio Crossfade Configuration Card
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                                border = BorderStroke(0.5.dp, BorderAmbient),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("audio_crossfade_card")
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Fundido Cruzado (Crossfade)",
                                            fontSize = 13.sp,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = if (crossfadeSecs == 0) "Desactivado" else "$crossfadeSecs seg",
                                            fontSize = 12.sp,
                                            color = NeonCyan,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.testTag("crossfade_secs_label")
                                        )
                                    }
                                    Text(
                                        text = "Mezcla la transición entre canciones de forma fluida y sin silencios abruptos.",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                    
                                    Slider(
                                        value = crossfadeSecs.toFloat(),
                                        onValueChange = { viewModel.setCrossfadeSecs(it.toInt()) },
                                        valueRange = 0f..10f,
                                        steps = 9,
                                        colors = SliderDefaults.colors(
                                            thumbColor = NeonCyan,
                                            activeTrackColor = NeonCyan,
                                            inactiveTrackColor = DeepSableSpace
                                        ),
                                        modifier = Modifier
                                            .height(28.dp)
                                            .testTag("crossfade_slider")
                                     )
                                }
                            }

                            HorizontalDivider(color = BorderAmbient.copy(alpha = 0.5f))

                            // SECTION 2: INTERFAZ Y VISUALIZADOR
                            Text(
                                "PERSONALIZACIÓN Y APARIENCIA",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan,
                                letterSpacing = 1.sp
                            )

                            // Visualizer selection card
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                                border = BorderStroke(0.5.dp, BorderAmbient),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text("Estilo de Visualización del Reproductor", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        listOf("Classic Neon", "Pulsing Line", "Solid Wave", "Fluid Synced").forEach { style ->
                                            val isSelected = waveformStyleStr == style
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(if (isSelected) NeonCyan.copy(alpha = 0.15f) else DeepSableSpace)
                                                    .border(
                                                        width = 1.dp,
                                                        color = if (isSelected) NeonCyan else BorderAmbient,
                                                        shape = RoundedCornerShape(10.dp)
                                                    )
                                                    .clickable { viewModel.setWaveformStyle(style) }
                                                    .padding(vertical = 12.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = when(style) {
                                                        "Classic Neon" -> "Barras Neon"
                                                        "Pulsing Line" -> "Línea Pulsante"
                                                        "Solid Wave" -> "Onda Sólida"
                                                        "Fluid Synced" -> "Ondas Fluidas"
                                                        else -> style
                                                    },
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) NeonCyan else TextSecondary,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Show / Hide Recommendations Card
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                                border = BorderStroke(0.5.dp, BorderAmbient),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Mostrar Recomendación Diaria", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                                        Text("Ver el banner 'Hero Mix' al principio de la biblioteca.", fontSize = 11.sp, color = TextSecondary)
                                    }
                                    Switch(
                                        checked = showDailyRecommended,
                                        onCheckedChange = { viewModel.setShowDailyRecommended(it) },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = NeonCyan,
                                            checkedTrackColor = NeonCyan.copy(alpha = 0.3f),
                                            uncheckedThumbColor = TextSecondary,
                                            uncheckedTrackColor = DeepSableSpace
                                        )
                                    )
                                }
                            }

                            HorizontalDivider(color = BorderAmbient.copy(alpha = 0.5f))

                            // Theme Selection Card
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                                border = BorderStroke(0.5.dp, BorderAmbient),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Tema Especial del Reproductor",
                                            fontSize = 13.sp,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (isDarkTheme) "Interfaz Neon Dark (Espacio Profundo)" else "Interfaz M3 Claro (Lilac Radiante)",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                    
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LightMode, 
                                            contentDescription = "Claro",
                                            tint = if (!isDarkTheme) Color(0xFF6750A4) else TextSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Switch(
                                            checked = isDarkTheme,
                                            onCheckedChange = { viewModel.toggleTheme() },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = NeonCyan,
                                                checkedTrackColor = NeonCyan.copy(alpha = 0.3f),
                                                uncheckedThumbColor = TextSecondary,
                                                uncheckedTrackColor = DeepSableSpace
                                            ),
                                            modifier = Modifier.scale(0.85f)
                                        )
                                        Icon(
                                            imageVector = Icons.Default.DarkMode, 
                                            contentDescription = "Oscuro",
                                            tint = if (isDarkTheme) NeonCyan else TextSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(color = BorderAmbient.copy(alpha = 0.5f))

                            // SECTION 3: DATOS Y SISTEMA
                            Text(
                                "DATOS Y DISPOSITIVO",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan,
                                letterSpacing = 1.sp
                            )

                            // SCAN BUTTON!
                            Button(
                                onClick = { launchScanning() },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                contentPadding = PaddingValues(vertical = 12.dp)
                            ) {
                                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(horizontalAlignment = Alignment.Start) {
                                    Text("Escanear Música del Dispositivo", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text("Busca audios locales del sistema de almacenamiento", fontSize = 10.sp, color = TextPrimary.copy(alpha = 0.7f))
                                }
                            }

                            HorizontalDivider(color = BorderAmbient.copy(alpha = 0.5f))

                            // SECTION 4: CLIENT-SIDE PLAYLIST SYNC
                            Text(
                                "SINCRONIZACIÓN CLOUD FIRESTORE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan,
                                letterSpacing = 1.sp
                            )

                            Card(
                                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                                border = BorderStroke(0.5.dp, BorderAmbient),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            "Sincronizar Listas de Reproducción",
                                            fontSize = 13.sp,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            "Conecta múltiples dispositivos ingresando el mismo código de sincronización.",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }

                                    // Primary Sync Code Input Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = syncCode,
                                            onValueChange = { viewModel.setSyncCode(it) },
                                            label = { Text("Código de Sincronización", fontSize = 11.sp, color = TextSecondary) },
                                            singleLine = true,
                                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Bold),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                unfocusedContainerColor = DeepSableSpace,
                                                focusedContainerColor = DeepSableSpace,
                                                unfocusedBorderColor = BorderAmbient,
                                                focusedBorderColor = NeonCyan
                                            ),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.weight(1f)
                                        )

                                        // Refresh Button to generate a new sync code
                                        IconButton(
                                            onClick = {
                                                val charPool = "abcdefghijklmnopqrstuvwxyz0123456789"
                                                val rand = (1..6).map { charPool.random() }.joinToString("")
                                                viewModel.setSyncCode("sync-$rand")
                                            },
                                            modifier = Modifier
                                                .size(48.dp)
                                                .background(DeepSableSpace, shape = RoundedCornerShape(12.dp))
                                                .border(1.5.dp, BorderAmbient, RoundedCornerShape(12.dp))
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = "Generar código nuevo",
                                                tint = NeonCyan,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    // Collapsible Advanced Settings (Project ID & API Key)
                                    var showAdvanced by remember { mutableStateOf(false) }
                                    
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { showAdvanced = !showAdvanced }
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Ajustes de Firebase Firestore (Opcional)",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (showAdvanced) NeonCyan else TextSecondary
                                            )
                                            Icon(
                                                imageVector = if (showAdvanced) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                contentDescription = null,
                                                tint = TextSecondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        androidx.compose.animation.AnimatedVisibility(visible = showAdvanced) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 8.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                OutlinedTextField(
                                                    value = syncProjectId,
                                                    onValueChange = { viewModel.setSyncProjectId(it) },
                                                    label = { Text("Firestore Project ID", fontSize = 11.sp, color = TextSecondary) },
                                                    singleLine = true,
                                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = TextPrimary),
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        unfocusedContainerColor = DeepSableSpace,
                                                        focusedContainerColor = DeepSableSpace,
                                                        unfocusedBorderColor = BorderAmbient,
                                                        focusedBorderColor = NeonCyan
                                                    ),
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                )

                                                OutlinedTextField(
                                                    value = syncApiKey,
                                                    onValueChange = { viewModel.setSyncApiKey(it) },
                                                    label = { Text("Firestore API Key (Opcional)", fontSize = 11.sp, color = TextSecondary) },
                                                    singleLine = true,
                                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = TextPrimary),
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        unfocusedContainerColor = DeepSableSpace,
                                                        focusedContainerColor = DeepSableSpace,
                                                        unfocusedBorderColor = BorderAmbient,
                                                        focusedBorderColor = NeonCyan
                                                    ),
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                
                                                Text(
                                                    text = "Por defecto usa un proyecto público sandbox para tests rápidos. Si usas tu propio proyecto, asegúrate de activar Firestore en modo Público.",
                                                    fontSize = 9.sp,
                                                    color = TextSecondary,
                                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Display Sync-Status Message
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                if (syncStatus.contains("éxito")) Color(0xFF1B5E20).copy(alpha = 0.15f)
                                                else if (syncStatus.contains("Error") || syncStatus.contains("Fallo") || syncStatus.contains("Falla")) Color(0xFFB71C1C).copy(alpha = 0.15f)
                                                else DeepSableSpace
                                            )
                                            .border(
                                                1.dp,
                                                if (syncStatus.contains("éxito")) Color(0xFF4CAF50).copy(alpha = 0.4f)
                                                else if (syncStatus.contains("Error") || syncStatus.contains("Fallo") || syncStatus.contains("Falla")) Color(0xFFF44336).copy(alpha = 0.4f)
                                                else BorderAmbient,
                                                RoundedCornerShape(10.dp)
                                            )
                                            .padding(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (syncStatus.contains("éxito")) Icons.Default.CloudDone
                                                             else if (syncStatus.contains("Error") || syncStatus.contains("Fallo") || syncStatus.contains("Falla")) Icons.Default.CloudOff
                                                             else if (isSyncing) Icons.Default.CloudSync
                                                             else Icons.Default.CloudQueue,
                                                contentDescription = null,
                                                tint = if (syncStatus.contains("éxito")) Color(0xFF4CAF50)
                                                       else if (syncStatus.contains("Error") || syncStatus.contains("Fallo") || syncStatus.contains("Falla")) Color(0xFFF44336)
                                                       else NeonCyan,
                                                modifier = Modifier.size(20.dp)
                                            )

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = if (isSyncing) "Operación en progreso" else "Estado de Sincronización",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextSecondary
                                                )
                                                Text(
                                                    text = syncStatus,
                                                    fontSize = 11.sp,
                                                    color = TextPrimary,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }

                                    // Wi-Fi Auto Sync toggle option
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                color = Color.Black.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Wifi,
                                                contentDescription = null,
                                                tint = if (wifiAutoSyncEnabled) NeonCyan else TextSecondary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Column {
                                                Text(
                                                    text = "Sincronización en Wi-Fi",
                                                    fontSize = 12.sp,
                                                    color = TextPrimary,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Text(
                                                    text = "Sincroniza automáticamente en Wi-Fi al cambiar preferencias o listas.",
                                                    fontSize = 10.sp,
                                                    color = TextSecondary
                                                )
                                            }
                                        }
                                        Switch(
                                            checked = wifiAutoSyncEnabled,
                                            onCheckedChange = { viewModel.setWifiAutoSyncEnabled(it) },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = NeonCyan,
                                                checkedTrackColor = NeonCyan.copy(alpha = 0.4f),
                                                uncheckedThumbColor = TextSecondary,
                                                uncheckedTrackColor = Color.Transparent
                                            ),
                                            modifier = Modifier.scale(0.85f)
                                        )
                                    }

                                    // Trigger sync button
                                    Button(
                                        onClick = { viewModel.triggerPlaylistSync() },
                                        enabled = !isSyncing && syncCode.isNotBlank(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = NeonCyan,
                                            contentColor = Color.Black,
                                            disabledContainerColor = BorderAmbient.copy(alpha = 0.3f),
                                            disabledContentColor = TextSecondary
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth().height(46.dp)
                                    ) {
                                        if (isSyncing) {
                                            CircularProgressIndicator(
                                                color = Color.Black,
                                                modifier = Modifier.size(20.dp),
                                                strokeWidth = 2.dp
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Sincronizando...", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        } else {
                                            Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Sincronizar Ahora (PULL & PUSH)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(100.dp)) // Extra padding for mini-player and bottom bar space
                        }
                    }
                }
            }

            }

            // Expanding Full screen Player Card Overlay
            AnimatedVisibility(
                visible = isPlayerExpanded,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium)
                ),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(durationMillis = 250)
                )
            ) {
                currentSong?.let { song ->
                    FullPlayerOverlay(
                        viewModel = viewModel,
                        song = song,
                        isPlaying = isPlaying,
                        progressSec = viewModel.currentPlayProgress.collectAsStateWithLifecycle().value,
                        isShuffle = viewModel.isShuffleEnabled.collectAsStateWithLifecycle().value,
                        smartShuffleMode = smartShuffleMode,
                        onSmartShuffleModeChange = { viewModel.setSmartShuffleMode(it) },
                        isRepeat = viewModel.isRepeatEnabled.collectAsStateWithLifecycle().value,
                        muteSynth = viewModel.synthMuted.collectAsStateWithLifecycle().value,
                        synthVolume = synthVolume,
                        waveformStyle = waveformStyleStr,
                        onPlayPause = { viewModel.togglePlayPause() },
                        onNext = { viewModel.nextSong() },
                        onPrev = { viewModel.prevSong() },
                        onSeek = { viewModel.seekTo(it) },
                        onVolumeChange = { viewModel.setSynthVolume(it) },
                        onToggleShuffle = { viewModel.toggleShuffle() },
                        onToggleRepeat = { viewModel.toggleRepeat() },
                        onToggleMute = { viewModel.toggleSynthMute() },
                        onDownloadCurrent = {
                            if (song.isDownloaded) {
                                viewModel.triggerSongDownload(song)
                            } else {
                                songToDownload = song
                                showDownloadConfirmationDialog = true
                            }
                        },
                        onCollapse = { isPlayerExpanded = false },
                        eqBass = eqBass,
                        eqMid = eqMid,
                        eqTreble = eqTreble,
                        realtimeWaveform = realtimeWaveform,
                        playbackQueue = playbackQueue,
                        onPlaySongFromQueue = { viewModel.playSongFromQueue(it) }
                    )
                }
            }
        }
    }

    // Dialog: Create Playlist
    if (showCreatePlaylistDialog) {
        var playlistName by remember { mutableStateOf("") }
        var playlistDesc by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showCreatePlaylistDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, BorderAmbient, RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Crear Lista Personalizada",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = NeonViolet
                    )

                    OutlinedTextField(
                        value = playlistName,
                        onValueChange = { playlistName = it },
                        label = { Text("Nombre de la lista", color = TextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            focusedBorderColor = NeonViolet,
                            unfocusedBorderColor = BorderAmbient
                        )
                    )

                    OutlinedTextField(
                        value = playlistDesc,
                        onValueChange = { playlistDesc = it },
                        label = { Text("Descripción corta", color = TextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            focusedBorderColor = NeonViolet,
                            unfocusedBorderColor = BorderAmbient
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showCreatePlaylistDialog = false }) {
                            Text("Cancelar", color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                if (playlistName.isNotBlank()) {
                                    viewModel.createPlaylist(playlistName, playlistDesc)
                                    showCreatePlaylistDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonViolet)
                        ) {
                            Text("Guardar", color = TextPrimary)
                        }
                    }
                }
            }
        }
    }

    // Dialog: Assign to Playlist
    if (songToAssignPlaylist != null) {
        val activeSong = songToAssignPlaylist!!
        Dialog(onDismissRequest = { songToAssignPlaylist = null }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, BorderAmbient, RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Añadir a lista",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = NeonViolet
                    )
                    Text(
                        "Selecciona una lista para añadir \"${activeSong.title}\":",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )

                    if (playlists.isEmpty()) {
                        Text(
                            "No tienes listas creadas.",
                            color = HotPink,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.heightIn(max = 240.dp)
                        ) {
                            items(playlists) { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(DeepSableSpace)
                                        .clickable {
                                            viewModel.addSongToPlaylist(item.playlist.id, activeSong.id)
                                            songToAssignPlaylist = null
                                        }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(item.playlist.name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Text("${item.songs.size} canciones", color = TextSecondary, fontSize = 11.sp)
                                    }
                                    Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { songToAssignPlaylist = null }) {
                            Text("Cerrar", color = TextSecondary)
                        }
                    }
                }
            }
        }
    }

    // Dialog: Interactive Download Options / Enable Download Modal
    if (showDownloadConfirmationDialog && songToDownload != null) {
        val downloadSong = songToDownload!!
        
        Dialog(onDismissRequest = { showDownloadConfirmationDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, BorderAmbient, RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Lock icon inside a circle highlight
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(NeonCyan.copy(alpha = 0.15f))
                            .align(Alignment.CenterHorizontally),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Text(
                        "Habilitar Descarga Local",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        "Configura el formato para habilitar y descargar offline en tu dispositivo la canción:",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Song Info Card row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DeepSableSpace)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.MusicNote, contentDescription = null, tint = NeonViolet, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(downloadSong.title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(downloadSong.artist, color = TextSecondary, fontSize = 11.sp)
                        }
                    }

                    Text(
                        "CALIDAD DE AUDIO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        letterSpacing = 1.sp
                    )

                    // Interactive Quality list selectors! (Alta, Estándar, Comprimida)
                    listOf(
                        "Alta Calidad (320 kbps)",
                        "Estándar Balanceada (128 kbps)",
                        "Lofi Chiptune (Retro 8-bit)"
                    ).forEach { qualityOption ->
                        val isSelected = selectedDownloadQuality == qualityOption
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) NeonViolet.copy(alpha = 0.12f) else DeepSableSpace)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) NeonViolet else BorderAmbient,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedDownloadQuality = qualityOption }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = qualityOption,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = if (isSelected) TextPrimary else TextSecondary
                                )
                                Text(
                                    text = when(qualityOption) {
                                        "Alta Calidad (320 kbps)" -> "Máxima fidelidad acústica para auriculares"
                                        "Estándar Balanceada (128 kbps)" -> "Compresión óptima, almacenamiento mínimo"
                                        else -> "Tono y síntesis retro Chiptune super ligero"
                                    },
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedDownloadQuality = qualityOption },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = NeonViolet,
                                    unselectedColor = TextSecondary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showDownloadConfirmationDialog = false },
                            border = BorderStroke(1.dp, BorderAmbient),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Text("Cancelar", fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                showDownloadConfirmationDialog = false
                                viewModel.triggerSongDownload(downloadSong)
                                Toast.makeText(context, "Habilitando descarga en formato $selectedDownloadQuality...", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1.2f),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Text("Habilitar y Descargar", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF121212))
                        }
                    }
                }
            }
        }
    }
}

// 1. ALBUM DECORATION DRAW COMPOSABLE
@Composable
fun AnimatedAlbumCover(
    title: String,
    genre: String,
    isSpinning: Boolean,
    modifier: Modifier = Modifier
) {
    val rotationTransition = rememberInfiniteTransition(label = "disc_rota")
    val rotationAngle by rotationTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "disc_rotation"
    )

    val finalAngle = if (isSpinning) rotationAngle else 0f

    // Animated dynamic Tonearm pivoting angle (swings into place when spinning)
    val tonearmAngle by animateFloatAsState(
        targetValue = if (isSpinning) -16f else -45f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "tonearm_angle"
    )

    // Breathing neon shadows around the turntable base
    val infiniteTransition = rememberInfiniteTransition(label = "deck_glow")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse_val"
    )

    val songInitials = title.take(2).uppercase()

    // 1. Turntable Outer Deck Casing
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E1E2E), // Carbon slate gray
                        Color(0xFF11111A)  // Deep metallic black
                    )
                )
            )
            .border(
                BorderStroke(2.dp, Brush.horizontalGradient(listOf(NeonViolet.copy(alpha = glowPulse), NeonCyan.copy(alpha = 1f - glowPulse)))),
                RoundedCornerShape(24.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        // Decorative physical dots/grids in the background representing a DJ Mixer deck
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val elementColor = BorderAmbient.copy(alpha = 0.08f)
            // Left grid dot patterns
            for (i in 0..4) {
                for (j in 0..4) {
                    drawCircle(
                        color = elementColor,
                        radius = 1.5f * density,
                        center = Offset(24f * density + i * 16f * density, 24f * density + j * 16f * density)
                    )
                }
            }
            // Brushed metallic deck circular base ring around platter
            drawCircle(
                color = Color.White.copy(alpha = 0.03f),
                radius = (size.width * 0.46f),
                center = center
            )
        }

        // 2. High-fidelity Dark Platter
        Box(
            modifier = Modifier
                .fillMaxSize(0.92f)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF18181F), Color(0xFF0D0D11)),
                        radius = 500f
                    )
                )
                .border(2.dp, Color.Black, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            
            // Platter rim indicators (stroboscopic dots on turntable edge)
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val pointColor = NeonCyan.copy(alpha = 0.3f)
                val dotRadius = 1.25f * density
                val radius = size.width * 0.49f
                for (i in 0 until 360 step 8) {
                    val angleRad = Math.toRadians(i.toDouble())
                    val dx = (size.width / 2) + radius * kotlin.math.cos(angleRad).toFloat()
                    val dy = (size.height / 2) + radius * kotlin.math.sin(angleRad).toFloat()
                    drawCircle(
                        color = if (i % 24 == 0) NeonViolet.copy(alpha = 0.5f) else pointColor,
                        radius = dotRadius,
                        center = Offset(dx, dy)
                    )
                }
            }

            // 3. Rotating Vinyl Record (Concentric tracks + Center sticker Label)
            Box(
                modifier = Modifier
                    .rotate(finalAngle)
                    .fillMaxSize(0.9f)
                    .clip(CircleShape)
                    .background(Color(0xFF141416)) // Carbon Charcoal core
                    .border(1.5.dp, Color.White.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Drawing continuous dense vinyl soundtracks groves
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    val grooveColor = Color.White.copy(alpha = 0.05f)
                    val strokeW = 0.5f * density
                    // Sound track gap lines (separating multiple songs tracks on the record)
                    drawCircle(color = Color.Black, radius = size.width * 0.42f, style = Stroke(width = 2f * density))
                    drawCircle(color = Color.Black, radius = size.width * 0.34f, style = Stroke(width = 2f * density))
                    drawCircle(color = Color.Black, radius = size.width * 0.26f, style = Stroke(width = 2f * density))

                    // Fine micro groove lines
                    for (r in listOf(0.46f, 0.44f, 0.40f, 0.38f, 0.36f, 0.32f, 0.30f, 0.28f, 0.24f)) {
                        drawCircle(
                            color = grooveColor,
                            radius = size.width * r,
                            style = Stroke(width = strokeW)
                        )
                    }
                }

                // 4. Central Record Label sticker
                Box(
                    modifier = Modifier
                        .fillMaxSize(0.35f)
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(
                                colors = listOf(NeonViolet, HotPink, NeonCyan, NeonViolet)
                            )
                        )
                        .border(1.5.dp, Color.White.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // Center spindle hole
                    Box(
                        modifier = Modifier
                            .fillMaxSize(0.3f)
                            .clip(CircleShape)
                            .background(Color(0xFF222222))
                            .border(2.dp, Color(0xFFD1D5DB), CircleShape)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(Color.Black)
                                .align(Alignment.Center)
                        )
                    }

                    // Rotating label text
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(2.dp)
                    ) {
                        Text(
                            text = songInitials,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = genre.uppercase(),
                            fontSize = 6.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.8f),
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            // 5. STATIC VINYL GLOSS HIGHLIGHTS OVERLAY (mimics spotlight reflection)
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize(0.9f)) {
                val center = Offset(size.width / 2f, size.height / 2f)
                
                // Reflection Sector 1 (45 Degrees)
                val reflectionWedge1 = Path().apply {
                    moveTo(center.x, center.y)
                    lineTo(center.x - size.width * 0.55f, center.y - size.height * 0.25f)
                    lineTo(center.x - size.width * 0.25f, center.y - size.height * 0.55f)
                    close()
                }
                drawPath(
                    path = reflectionWedge1,
                    color = Color.White.copy(alpha = 0.08f)
                )

                // Reflection Sector 2 (Opposite 225 Degrees)
                val reflectionWedge2 = Path().apply {
                    moveTo(center.x, center.y)
                    lineTo(center.x + size.width * 0.55f, center.y + size.height * 0.25f)
                    lineTo(center.x + size.width * 0.25f, center.y + size.height * 0.55f)
                    close()
                }
                drawPath(
                    path = reflectionWedge2,
                    color = Color.White.copy(alpha = 0.08f)
                )
            }
        }

        // 6. LAYERED CHROME TONEARM ASSEMBLY (Pivoting dynamically)
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val pivotX = size.width * 0.86f
            val pivotY = size.height * 0.14f
            
            // joint base rings
            drawCircle(
                color = Color(0xFF1F1F2E),
                radius = 16f * density,
                center = Offset(pivotX, pivotY)
            )
            drawCircle(
                color = Color(0xFFE5E7EB),
                radius = 12f * density,
                center = Offset(pivotX, pivotY)
            )
            drawCircle(
                color = Color(0xFF4B5563),
                radius = 7f * density,
                center = Offset(pivotX, pivotY)
            )
            drawCircle(
                color = NeonCyan,
                radius = 3f * density,
                center = Offset(pivotX, pivotY)
            )

            // Rotate coordinates space around joint pivot to render pivoting tonearm
            rotate(degrees = tonearmAngle, pivot = Offset(pivotX, pivotY)) {
                val armPath = Path().apply {
                    val startX = pivotX
                    val startY = pivotY
                    val midX = size.width * 0.65f
                    val midY = size.height * 0.45f
                    val endX = size.width * 0.43f
                    val endY = size.height * 0.57f
                    
                    moveTo(startX, startY)
                    cubicTo(
                        startX - 15f * density, startY + 30f * density,
                        midX + 15f * density, midY - 20f * density,
                        midX, midY
                    )
                    lineTo(endX, endY)
                }

                drawPath(
                    path = armPath,
                    color = Color(0xFFD1D5DB),
                    style = Stroke(width = 3.5f * density)
                )

                // needle headshell stylus
                val targetNeedleX = size.width * 0.43f
                val targetNeedleY = size.height * 0.57f

                val headshellPath = Path().apply {
                    moveTo(targetNeedleX - 5f * density, targetNeedleY - 2f * density)
                    lineTo(targetNeedleX + 12f * density, targetNeedleY + 14f * density)
                    lineTo(targetNeedleX + 4f * density, targetNeedleY + 22f * density)
                    lineTo(targetNeedleX - 11f * density, targetNeedleY + 4f * density)
                    close()
                }
                drawPath(
                    path = headshellPath,
                    color = Color(0xFF1E293B)
                )

                drawCircle(
                    color = HotPink,
                    radius = 3f * density,
                    center = Offset(targetNeedleX + 4f * density, targetNeedleY + 12f * density)
                )
            }
        }
    }
}

// 2. MINI SONG ROW COMPOSABLE
@Composable
fun SongItemCard(
    song: Song,
    isCurrent: Boolean,
    isPlaying: Boolean,
    isFavorite: Boolean = false,
    downloadProgress: Float?,
    onPlay: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onDownload: () -> Unit,
    onToggleFavorite: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("song_item_${song.id}")
            .clickable { onPlay() }
            .border(
                width = 1.dp,
                color = if (isCurrent) NeonViolet.copy(alpha = 0.6f) else BorderAmbient,
                shape = RoundedCornerShape(14.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) NeonViolet.copy(alpha = 0.08f) else DarkSurfaceCard
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flat custom mini album cover
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                NeonViolet.copy(alpha = 0.6f),
                                NeonCyan.copy(alpha = 0.6f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCurrent && isPlaying) {
                    // Small equalizer bars jumping
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Reproduciendo",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = TextPrimary.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Meta info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    color = if (isCurrent) NeonCyan else TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${song.artist} • ${song.album}",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Genre badge / duration
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Text(
                    text = formatDuration(song.durationSecs),
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val mood = SongMetadataHelper.getMoodTag(song.id)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when {
                                    "🔥" in mood -> Color(0xFFFF5722).copy(alpha = 0.15f)
                                    "⚡" in mood -> Color(0xFFFFC107).copy(alpha = 0.15f)
                                    else -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                                }
                            )
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = mood,
                            fontSize = 8.sp,
                            color = when {
                                "🔥" in mood -> Color(0xFFFF3D00)
                                "⚡" in mood -> Color(0xFFFFC107)
                                else -> Color(0xFF4CAF50)
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(BorderAmbient)
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(song.genre, fontSize = 8.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Action triggers: Playlist + Download
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Favorite Toggle
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleFavorite()
                    },
                    modifier = Modifier.size(36.dp).testTag("song_favorite_${song.id}")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorito",
                        tint = if (isFavorite) HotPink else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Add to playlist trigger
                IconButton(
                    onClick = onAddToPlaylist,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlaylistAdd,
                        contentDescription = "Añadir a lista",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Download Toggle
                Box(contentAlignment = Alignment.Center) {
                    if (downloadProgress != null) {
                        CircularProgressIndicator(
                            progress = { downloadProgress },
                            modifier = Modifier.size(22.dp),
                            color = NeonCyan,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        IconButton(
                            onClick = onDownload,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (song.isDownloaded) Icons.Default.CheckCircle else Icons.Default.FileDownload,
                                contentDescription = if (song.isDownloaded) "Descargado" else "Descargar",
                                tint = if (song.isDownloaded) GreenStatus else TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// 3. MINI PLAYER BOTTOM BAR COMPOSABLE
@Composable
fun MiniPlayerBar(
    song: Song,
    isPlaying: Boolean,
    progressSec: Int,
    muteSynth: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    isShuffle: Boolean,
    isRepeat: Boolean,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleMute: () -> Unit,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("mini_player")
            .clickable { onClick() }
            .border(
                width = 1.5.dp,
                brush = Brush.horizontalGradient(
                    colors = if (isSystemDarkThemeState) {
                        listOf(NeonCyan.copy(alpha = 0.6f), NeonViolet.copy(alpha = 0.6f))
                    } else {
                        listOf(BorderAmbient, BorderAmbient)
                    }
                ),
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = SoftLilac),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Album image placeholder with a vibrant linear gradient background
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(NeonViolet, HotPink)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Song text with primary purple-black styling
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        color = PlayerOnSoftLilacColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${song.artist} • ${song.album}",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Synth sound toggle
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleMute()
                    }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (muteSynth) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = "Mudo",
                            tint = if (muteSynth) HotPink else PlayerOnSoftLilacColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Shuffle trigger
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleShuffle()
                    }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Aleatorio",
                            tint = if (isShuffle) (if (isSystemDarkThemeState) NeonCyan else NeonViolet) else TextSecondary.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Prev trigger
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onPrev()
                    }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Anterior",
                            tint = PlayerOnSoftLilacColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Play trigger
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onPlayPause()
                    }, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pausa" else "Reproducir",
                            tint = PlayerOnSoftLilacColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Next trigger
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNext()
                    }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Siguiente",
                            tint = PlayerOnSoftLilacColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Repeat trigger
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleRepeat()
                    }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = "Repetir",
                            tint = if (isRepeat) (if (isSystemDarkThemeState) NeonCyan else NeonViolet) else TextSecondary.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Beautiful glowing CSS-style progress bar
            val progressPercent = if (song.durationSecs > 0) {
                progressSec.toFloat() / song.durationSecs.toFloat()
            } else 0f

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(BorderAmbient.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressPercent)
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(
                                colors = if (isSystemDarkThemeState) {
                                    listOf(NeonCyan, NeonViolet)
                                } else {
                                    listOf(NeonViolet, HotPink)
                                }
                            )
                        )
                )
            }
        }
    }
}

// Helper to determine cover colors for song metadata matching its genre or title
fun getCoverColorsForSong(song: Song): Pair<Color, Color> {
    return when (song.genre) {
        "Synthwave" -> Pair(Color(0xFFFF007F), Color(0xFF00F0FF))
        "Chiptune" -> Pair(Color(0xFFBB86FC), Color(0xFF03DAC6))
        "Electronic" -> Pair(Color(0xFF03DAC6), Color(0xFFBB86FC))
        "Acoustic" -> Pair(Color(0xFFE17A47), Color(0xFFEFD469))
        "Ambient" -> Pair(Color(0xFF4A00E0), Color(0xFF8E2DE2))
        "Retro" -> Pair(Color(0xFFFF007F), Color(0xFFFFCC00))
        "Techno" -> Pair(Color(0xFF1B03A3), Color(0xFF00FFCC))
        "Pop" -> Pair(Color(0xFFFFA6C9), Color(0xFF74EBD5))
        else -> {
            val hash = song.title.hashCode()
            val hue1 = (hash and 0xFF) * 360f / 256f
            val hue2 = ((hash shr 8) and 0xFF) * 360f / 256f
            val c1 = android.graphics.Color.HSVToColor(floatArrayOf(hue1, 0.8f, 0.9f))
            val c2 = android.graphics.Color.HSVToColor(floatArrayOf(hue2, 0.7f, 0.8f))
            Pair(Color(c1), Color(c2))
        }
    }
}

// 4. FULL SCREEN DETAIL PLAYER DECK OVERLAY
@Composable
fun FullPlayerOverlay(
    viewModel: MusicViewModel,
    song: Song,
    isPlaying: Boolean,
    progressSec: Int,
    isShuffle: Boolean,
    smartShuffleMode: SmartShuffleMode = SmartShuffleMode.STANDARD,
    onSmartShuffleModeChange: (SmartShuffleMode) -> Unit = {},
    isRepeat: Boolean,
    muteSynth: Boolean,
    synthVolume: Float,
    waveformStyle: String = "Classic Neon",
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onSeek: (Int) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleMute: () -> Unit,
    onDownloadCurrent: () -> Unit,
    onCollapse: () -> Unit,
    eqBass: Float = 1.0f,
    eqMid: Float = 1.0f,
    eqTreble: Float = 1.0f,
    realtimeWaveform: FloatArray = FloatArray(0),
    playbackQueue: List<Song> = emptyList(),
    onPlaySongFromQueue: (Song) -> Unit = {}
) {
    var showLyrics by remember { mutableStateOf(false) }
    var isLyricsExpanded by remember { mutableStateOf(false) }
    var showQueueSheet by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    val ambientColorState = remember(song) {
        try {
            // Get cover colors for this song metadata
            val (c1, c2) = getCoverColorsForSong(song)
            
            // Create a small bitmap to process with Palette
            val bitmap = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint().apply {
                shader = LinearGradient(
                    0f, 0f, 32f, 32f,
                    c1.toArgb(), c2.toArgb(),
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, 32f, 32f, paint)
            
            // Extract the color using Palette
            val palette = Palette.from(bitmap).generate()
            bitmap.recycle()
            
            // Extract dominant, fallback to Vibrant or c1
            val extractedColor = palette.getDominantColor(
                palette.getVibrantColor(
                    palette.getLightVibrantColor(c1.toArgb())
                )
            )
            Color(extractedColor)
        } catch (e: Exception) {
            NeonViolet
        }
    }

    val animatedAmbientColor by animateColorAsState(
        targetValue = ambientColorState,
        animationSpec = tween(durationMillis = 1000, easing = LinearOutSlowInEasing),
        label = "ambient_glow_color"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "glow_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse_scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.12f,
        targetValue = 0.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DeepSableSpace, DarkSurfaceCard)
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        // Fondo de Brillo Ambiental Dinámico (Ambient Glow)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
                .align(Alignment.BottomCenter)
                .scale(pulseScale)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            animatedAmbientColor.copy(alpha = pulseAlpha),
                            animatedAmbientColor.copy(alpha = pulseAlpha * 0.5f),
                            Color.Transparent
                        ),
                        radius = 650f
                    )
                )
        )

        // Main outer container: holds FIXED header elements and the scrollable content below.
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Header top actions (FIXED)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCollapse) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Cerrar",
                        tint = TextPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = "REPRODUCTOR DIGITAL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NeonViolet,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )

                // Quick view Queue trigger icon button in header
                IconButton(onClick = { showQueueSheet = true }) {
                    Icon(
                        imageVector = Icons.Default.QueueMusic,
                        contentDescription = "Ver Cola de Reproducción",
                        tint = if (showQueueSheet) NeonCyan else TextSecondary,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // Download inside player option
                IconButton(onClick = onDownloadCurrent) {
                    Icon(
                        imageVector = if (song.isDownloaded) Icons.Default.CheckCircle else Icons.Default.FileDownload,
                        contentDescription = "Descargar",
                        tint = if (song.isDownloaded) GreenStatus else TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Premium segmented tab control for vinyl/lyrics switcher (FIXED)
            var activePlayerSubTab by remember { mutableIntStateOf(0) } // 0: DISCO, 1: CO-PILOTO, 2: SINTETIZADOR, 3: LETRA

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(BorderAmbient.copy(alpha = 0.3f))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("DISCO", "CO-PILOTO", "SINTETIZADOR", "LETRA").forEachIndexed { index, label ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (activePlayerSubTab == index) NeonViolet else Color.Transparent)
                            .clickable { activePlayerSubTab = index }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.sp,
                            color = if (activePlayerSubTab == index) Color.White else TextSecondary
                        )
                    }
                }
            }

            // Central Deck (Flexible / Non-Scrollable core player area)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                when (activePlayerSubTab) {
                    0 -> {
                        // Responsive album cover scales down to fit smaller viewports without overflowing
                        Box(
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .fillMaxWidth(0.58f)
                                .aspectRatio(1f)
                                .padding(vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                modifier = Modifier.fillMaxSize(),
                                shape = RoundedCornerShape(24.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                AnimatedAlbumCover(
                                    title = song.title,
                                    genre = song.genre,
                                    isSpinning = isPlaying,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        // Dynamic Waveform Visualizer
                        WaveformVisualizer(
                            isPlaying = isPlaying,
                            styleStr = waveformStyle,
                            bass = eqBass,
                            mid = eqMid,
                            treble = eqTreble,
                            realtimeWaveform = realtimeWaveform,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                                .padding(vertical = 2.dp)
                                .testTag("waveform_visualizer")
                        )

                        // Track detailed descriptions
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = song.title,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = TextPrimary,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${song.artist} • ${song.album}",
                                fontSize = 10.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Interactive Scratch Card Pad
                        var scratchAngle by remember { mutableStateOf(0f) }
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, BorderAmbient),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .padding(horizontal = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .draggable(
                                        state = androidx.compose.foundation.gestures.rememberDraggableState { delta ->
                                            val newMult = (1.0f + (delta / 80f)).coerceIn(0.4f, 2.2f)
                                            viewModel.setPitchBend(newMult)
                                            scratchAngle += delta * 1.5f
                                        },
                                        orientation = androidx.compose.foundation.gestures.Orientation.Horizontal,
                                        onDragStarted = { _ ->
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        },
                                        onDragStopped = { _ ->
                                            viewModel.setPitchBend(1.0f)
                                        }
                                    )
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Visual rotating scratch disc
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .rotate(scratchAngle)
                                            .background(Color.Black, shape = CircleShape)
                                            .border(3.dp, NeonViolet, CircleShape)
                                            .border(12.dp, Color(0xFF1C1B1F), CircleShape)
                                            .border(13.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(NeonCyan, shape = CircleShape)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "INTERACTÚA CON EL VINILO",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 9.sp,
                                            color = NeonCyan,
                                            letterSpacing = 1.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            "Desliza horizontalmente para modular la velocidad (scratch) y alterar la frecuencia del sintetizador.",
                                            fontSize = 8.sp,
                                            color = TextSecondary,
                                            lineHeight = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        // Co-pilot AI Card Root
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "🧠 CO-PILOTO CON GEMINI AI",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonViolet,
                                letterSpacing = 1.sp
                            )

                            // Section 1: Vibe Playlist Generator
                            Card(
                                colors = CardDefaults.cardColors(containerColor = BorderAmbient.copy(alpha = 0.15f)),
                                border = BorderStroke(1.dp, BorderAmbient),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        "Planificador con Vibe",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = NeonCyan
                                    )
                                    Text(
                                        "Describe tu ambiente para que Gemini arme y afine la mezcla ideal.",
                                        fontSize = 9.sp,
                                        color = TextSecondary
                                    )

                                    var vibePrompt by remember { mutableStateOf("") }
                                    val isPlaylistLoading by viewModel.isPlaylistLoading.collectAsStateWithLifecycle()
                                    val vibeExplanation by viewModel.vibeExplanation.collectAsStateWithLifecycle()

                                    OutlinedTextField(
                                        value = vibePrompt,
                                        onValueChange = { vibePrompt = it },
                                        placeholder = { Text("Ej: Cabaña nocturna lluviosa, retro arcade...", fontSize = 9.sp, color = TextSecondary) },
                                        textStyle = TextStyle(fontSize = 10.sp, color = TextPrimary),
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = NeonCyan,
                                            unfocusedBorderColor = BorderAmbient,
                                            cursorColor = NeonCyan
                                        ),
                                        maxLines = 1,
                                        singleLine = true
                                    )

                                    Button(
                                        onClick = {
                                            if (vibePrompt.isNotBlank()) {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                viewModel.generateVibePlaylist(vibePrompt)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth().height(32.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        if (isPlaylistLoading) {
                                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                                        } else {
                                            Text("SINTONIZAR AMBIENTE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                        }
                                    }

                                    vibeExplanation?.let {
                                        Text(
                                            text = "✨ $it",
                                            fontSize = 9.sp,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Medium,
                                            lineHeight = 11.sp,
                                            modifier = Modifier
                                                .background(BorderAmbient.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                                .padding(6.dp)
                                        )
                                    }
                                }
                            }

                            // Section 2: Ambient design presets
                            Card(
                                colors = CardDefaults.cardColors(containerColor = BorderAmbient.copy(alpha = 0.15f)),
                                border = BorderStroke(1.dp, BorderAmbient),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        "Ajustador de Capas Teatrales",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = NeonViolet
                                    )
                                    Text(
                                        "Deja que Gemini ajuste ADSR y modulación LFO ideal para ti:",
                                        fontSize = 9.sp,
                                        color = TextSecondary
                                    )

                                    val isAmbientLoading by viewModel.isAmbientLoading.collectAsStateWithLifecycle()
                                    val context = LocalContext.current

                                    val statesList = listOf(
                                        Pair("💤 Descanso Profundo", "sueño cósmico y descanso profundo con ondas ultrasuaves y un sutil tremolo"),
                                        Pair("🧠 Enfoque de Estudio", "concentración máxima, estudio universitario, ondas triangulares limpias y medios claros"),
                                        Pair("✨ Zen Inspiracional", "creatividad artística activa, ondas senoidales y cuadradas mezcladas con vibrato inspirador")
                                    )

                                    if (isAmbientLoading) {
                                        Box(modifier = Modifier.fillMaxWidth().height(40.dp), contentAlignment = Alignment.Center) {
                                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = NeonViolet)
                                        }
                                    } else {
                                        statesList.forEach { (title, query) ->
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(BorderAmbient.copy(alpha = 0.25f))
                                                    .clickable {
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        viewModel.designAmbientLayer(query) {
                                                            Toast.makeText(context, "¡Parámetros optimizados para $title!", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                    .padding(8.dp)
                                            ) {
                                                Text(title, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    2 -> {
                        // Advanced Synth Control center
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "🎨 SINTETIZADOR ADSR y LFO",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonViolet,
                                letterSpacing = 1.sp
                            )

                            // Retrieve states
                            val attack by viewModel.envelopeAttack.collectAsStateWithLifecycle()
                            val decay by viewModel.envelopeDecay.collectAsStateWithLifecycle()
                            val sustain by viewModel.envelopeSustain.collectAsStateWithLifecycle()
                            val release by viewModel.envelopeRelease.collectAsStateWithLifecycle()

                            val tremoloActive by viewModel.lfoTremoloActive.collectAsStateWithLifecycle()
                            val tremoloRate by viewModel.lfoTremoloRate.collectAsStateWithLifecycle()
                            val tremoloDepth by viewModel.lfoTremoloDepth.collectAsStateWithLifecycle()

                            val vibratoActive by viewModel.lfoVibratoActive.collectAsStateWithLifecycle()
                            val vibratoRate by viewModel.lfoVibratoRate.collectAsStateWithLifecycle()
                            val vibratoDepth by viewModel.lfoVibratoDepth.collectAsStateWithLifecycle()

                            // Custom Canvas showing the envelope curve!
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.Black),
                                border = BorderStroke(1.dp, BorderAmbient),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                                        val w = size.width
                                        val h = size.height
                                        val padding = 15f
                                        val plotH = h - 2 * padding
                                        
                                        // Calculate segments safely based on normalized values
                                        val a = attack.coerceIn(0.01f, 0.9f)
                                        val d = decay.coerceIn(0.01f, 0.9f)
                                        val r = release.coerceIn(0.01f, 0.9f)
                                        val s = sustain.coerceIn(0f, 1f)

                                        val sum = a + d + r
                                        val scaleFactor = if (sum > 1.0f) 1.0f / sum else 1.0f
                                        val normA = a * scaleFactor
                                        val normD = d * scaleFactor
                                        val normR = r * scaleFactor

                                        val startX = padding
                                        val ax = startX + normA * (w - 2 * padding) * 0.4f
                                        val dx = ax + normD * (w - 2 * padding) * 0.3f
                                        val sx = dx + (w - 2 * padding) * 0.2f
                                        val rx = sx + normR * (w - 2 * padding) * 0.1f

                                        val peakY = padding
                                        val sustainY = h - padding - s * plotH
                                        val baselineY = h - padding

                                        val path = Path().apply {
                                            moveTo(startX, baselineY)
                                            lineTo(ax, peakY)
                                            lineTo(dx, sustainY)
                                            lineTo(sx, sustainY)
                                            lineTo(rx.coerceAtMost(w - padding), baselineY)
                                        }

                                        drawPath(
                                            path = path,
                                            color = NeonViolet,
                                            style = Stroke(width = 3.dp.toPx())
                                        )
                                        
                                        // Draw a neon glow dot on current phase
                                        drawCircle(
                                            color = NeonCyan,
                                            radius = 4.dp.toPx(),
                                            center = Offset(ax, peakY)
                                        )
                                    }
                                    Text(
                                        text = "ENVOLVENTE ADSR",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NeonViolet,
                                        modifier = Modifier.padding(6.dp).align(Alignment.BottomEnd)
                                    )
                                }
                            }

                            // Waveform selector built as grid Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Waveform:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf("Sine", "Square", "Triangle", "Sawtooth", "White Noise").forEach { wave ->
                                        val active = (viewModel.synthWaveformType.collectAsStateWithLifecycle().value == wave)
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (active) NeonCyan else BorderAmbient.copy(alpha = 0.25f))
                                                .clickable { viewModel.setSynthWaveform(wave) }
                                                .padding(horizontal = 6.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                wave, 
                                                fontSize = 8.sp, 
                                                fontWeight = FontWeight.Black, 
                                                color = if (active) Color.Black else TextSecondary
                                            )
                                        }
                                    }
                                }
                            }

                            // ADSR sliders panel
                            Card(
                                colors = CardDefaults.cardColors(containerColor = BorderAmbient.copy(alpha = 0.15f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Control de Envolventes (ADSR)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    
                                    // Attack Slider
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Ataque: ${String.format("%.2f", attack)}s", fontSize = 9.sp, color = TextSecondary, modifier = Modifier.width(75.dp))
                                        Slider(
                                            value = attack,
                                            onValueChange = { viewModel.setEnvelopeAttack(it) },
                                            valueRange = 0.01f..0.9f,
                                            colors = SliderDefaults.colors(thumbColor = NeonViolet, activeTrackColor = NeonViolet),
                                            modifier = Modifier.weight(1f).height(16.dp)
                                        )
                                    }

                                    // Decay Slider
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Decay: ${String.format("%.2f", decay)}s", fontSize = 9.sp, color = TextSecondary, modifier = Modifier.width(75.dp))
                                        Slider(
                                            value = decay,
                                            onValueChange = { viewModel.setEnvelopeDecay(it) },
                                            valueRange = 0.01f..0.9f,
                                            colors = SliderDefaults.colors(thumbColor = NeonViolet, activeTrackColor = NeonViolet),
                                            modifier = Modifier.weight(1f).height(16.dp)
                                        )
                                    }

                                    // Sustain Slider
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Sustain: ${String.format("%.2f", sustain)}", fontSize = 9.sp, color = TextSecondary, modifier = Modifier.width(75.dp))
                                        Slider(
                                            value = sustain,
                                            onValueChange = { viewModel.setEnvelopeSustain(it) },
                                            valueRange = 0.0f..1.0f,
                                            colors = SliderDefaults.colors(thumbColor = NeonViolet, activeTrackColor = NeonViolet),
                                            modifier = Modifier.weight(1f).height(16.dp)
                                        )
                                    }

                                    // Release Slider
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Release: ${String.format("%.2f", release)}s", fontSize = 9.sp, color = TextSecondary, modifier = Modifier.width(75.dp))
                                        Slider(
                                            value = release,
                                            onValueChange = { viewModel.setEnvelopeRelease(it) },
                                            valueRange = 0.01f..0.9f,
                                            colors = SliderDefaults.colors(thumbColor = NeonViolet, activeTrackColor = NeonViolet),
                                            modifier = Modifier.weight(1f).height(16.dp)
                                        )
                                    }
                                }
                            }

                            // Moduladores LFO panel
                            Card(
                                colors = CardDefaults.cardColors(containerColor = BorderAmbient.copy(alpha = 0.15f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Oscilador de Baja Frecuencia (LFO)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    
                                    // Tremolo modulation
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text("Trémolo (Seno-Volumen)", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = NeonCyan)
                                        Switch(
                                            checked = tremoloActive,
                                            onCheckedChange = { viewModel.setLfoTremoloActive(it) },
                                            modifier = Modifier.scale(0.7f)
                                        )
                                    }
                                    if (tremoloActive) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Rate: ${String.format("%.1f", tremoloRate)}Hz", fontSize = 8.sp, color = TextSecondary, modifier = Modifier.width(75.dp))
                                            Slider(
                                                value = tremoloRate,
                                                onValueChange = { viewModel.setLfoTremoloRate(it) },
                                                valueRange = 0.5f..10.0f,
                                                colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan),
                                                modifier = Modifier.weight(1f).height(16.dp)
                                            )
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Depth: ${String.format("%.0f", tremoloDepth * 100f)}%", fontSize = 8.sp, color = TextSecondary, modifier = Modifier.width(75.dp))
                                            Slider(
                                                value = tremoloDepth,
                                                onValueChange = { viewModel.setLfoTremoloDepth(it) },
                                                valueRange = 0.0f..1.0f,
                                                colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan),
                                                modifier = Modifier.weight(1f).height(16.dp)
                                            )
                                        }
                                    }

                                    // Vibrato modulation
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text("Vibrato (Seno-Fono / Pitch)", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = NeonViolet)
                                        Switch(
                                            checked = vibratoActive,
                                            onCheckedChange = { viewModel.setLfoVibratoActive(it) },
                                            modifier = Modifier.scale(0.7f)
                                        )
                                    }
                                    if (vibratoActive) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Rate: ${String.format("%.1f", vibratoRate)}Hz", fontSize = 8.sp, color = TextSecondary, modifier = Modifier.width(75.dp))
                                            Slider(
                                                value = vibratoRate,
                                                onValueChange = { viewModel.setLfoVibratoRate(it) },
                                                valueRange = 0.5f..10.0f,
                                                colors = SliderDefaults.colors(thumbColor = NeonViolet, activeTrackColor = NeonViolet),
                                                modifier = Modifier.weight(1f).height(16.dp)
                                            )
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Depth: ${String.format("%.0f", vibratoDepth * 100f)}%", fontSize = 8.sp, color = TextSecondary, modifier = Modifier.width(75.dp))
                                            Slider(
                                                value = vibratoDepth,
                                                onValueChange = { viewModel.setLfoVibratoDepth(it) },
                                                valueRange = 0.0f..1.0f,
                                                colors = SliderDefaults.colors(thumbColor = NeonViolet, activeTrackColor = NeonViolet),
                                                modifier = Modifier.weight(1f).height(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    3 -> {
                        // SincronizedLyricsView as usual
                        SincronizedLyricsView(
                            lyrics = getLyricsForSong(song.id),
                            progressSec = progressSec,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            onExpandClick = { isLyricsExpanded = true }
                        )

                        // Automated Gemini Song Trivia
                        val songTrivia by viewModel.songTrivia.collectAsStateWithLifecycle()
                        val isTriviaLoading by viewModel.isTriviaLoading.collectAsStateWithLifecycle()

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)),
                            border = BorderStroke(1.dp, BorderAmbient),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = NeonCyan,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        "MÓDULO DE TRIVIA GEMINI",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 9.sp,
                                        color = NeonCyan,
                                        letterSpacing = 1.sp
                                    )
                                    if (isTriviaLoading) {
                                        CircularProgressIndicator(modifier = Modifier.size(10.dp), strokeWidth = 1.dp, color = NeonCyan)
                                    }
                                }
                                Text(
                                    text = songTrivia.ifEmpty { "Descargando conocimiento poético de ${song.title} desde la órbita..." },
                                    fontSize = 9.sp,
                                    color = TextPrimary,
                                    lineHeight = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

                // Elegant Time Scrubber Slider
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                ) {
                    Slider(
                        value = progressSec.toFloat(),
                        onValueChange = { onSeek(it.toInt()) },
                        valueRange = 0f..song.durationSecs.toFloat(),
                        colors = SliderDefaults.colors(
                            thumbColor = if (isSystemDarkThemeState) NeonCyan else NeonViolet,
                            activeTrackColor = if (isSystemDarkThemeState) NeonCyan else NeonViolet,
                            inactiveTrackColor = BorderAmbient.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth().height(16.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatDuration(progressSec),
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = formatDuration(song.durationSecs),
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Big Player Actions Deck with generous spacing and beautiful circular layout keys
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Shuffle trigger
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleShuffle()
                    }, modifier = Modifier.size(44.dp).bounceClick()) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Aleatorio",
                            tint = if (isShuffle) NeonCyan else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Prev trigger
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onPrev()
                    }, modifier = Modifier.size(48.dp).bounceClick()) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Anterior",
                            tint = TextPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Beautiful large play action target circle shape with deluxe glowing gradient accent
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .bounceClick()
                            .shadow(elevation = 8.dp, shape = CircleShape)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(NeonViolet, HotPink)
                                )
                            )
                            .border(1.5.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onPlayPause()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Next trigger
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNext()
                    }, modifier = Modifier.size(48.dp).bounceClick()) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Siguiente",
                            tint = TextPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Repeat trigger
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleRepeat()
                    }, modifier = Modifier.size(44.dp).bounceClick()) {
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = "Repetir",
                            tint = if (isRepeat) NeonCyan else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Smart Shuffle Selector Row
                if (isShuffle) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "REPRODUCCIÓN ALEATORIA INTELIGENTE",
                            color = NeonViolet,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SmartShuffleMode.values().forEach { mode ->
                                val isSelected = smartShuffleMode == mode
                                val bg = if (isSelected) NeonViolet.copy(alpha = 0.25f) else Color.Transparent
                                val border = if (isSelected) BorderStroke(1.dp, NeonViolet) else BorderStroke(1.dp, Color.Transparent)
                                val textColor = if (isSelected) Color.White else TextSecondary
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(bg)
                                        .border(border, RoundedCornerShape(8.dp))
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            SmartShuffleMode.values().getOrNull(mode.ordinal)?.let {
                                                onSmartShuffleModeChange(it)
                                            }
                                        }
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = mode.displayName,
                                        fontSize = 11.sp,
                                        color = textColor,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

            // BEAUTIFUL PREMIUM "UP NEXT" PLAYLIST QUEUE LIST (FIXED AT THE BOTTOM, INDEPENDENTLY SCROLLABLE)
            Card(
                colors = CardDefaults.cardColors(containerColor = TextPrimary.copy(alpha = 0.02f)),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                border = BorderStroke(1.dp, BorderAmbient.copy(alpha = 0.15f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 140.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // List Header
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(NeonCyan.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QueueMusic,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = NeonCyan
                                )
                            }

                            Text(
                                text = "A CONTINUACIÓN",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = NeonCyan,
                                letterSpacing = 1.5.sp
                            )

                            // Beautiful badge with remaining count
                            val currentSongIdx = playbackQueue.indexOfFirst { it.id == song.id }
                            val upcomingSongs = if (playbackQueue.isNotEmpty() && currentSongIdx != -1 && currentSongIdx + 1 < playbackQueue.size) {
                                playbackQueue.subList(currentSongIdx + 1, playbackQueue.size)
                            } else {
                                emptyList()
                            }

                            if (upcomingSongs.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(NeonViolet.copy(alpha = 0.25f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${upcomingSongs.size}",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Ver Todo",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showQueueSheet = true
                                }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Render all upcoming songs of the playback queue
                    val currentSongIdx = playbackQueue.indexOfFirst { it.id == song.id }
                    val upcomingSongs = if (playbackQueue.isNotEmpty() && currentSongIdx != -1 && currentSongIdx + 1 < playbackQueue.size) {
                        playbackQueue.subList(currentSongIdx + 1, playbackQueue.size)
                    } else {
                        emptyList()
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        if (upcomingSongs.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = TextSecondary
                                    )
                                    Text(
                                        "Fin de la cola de reproducción",
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                upcomingSongs.forEachIndexed { i, nextItem ->
                                    val gradientColors = getCoverColorsForSong(nextItem)

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(TextPrimary.copy(alpha = 0.03f))
                                            .border(1.dp, TextPrimary.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                onPlaySongFromQueue(nextItem)
                                            }
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(
                                                        Brush.linearGradient(
                                                            colors = listOf(gradientColors.first, gradientColors.second)
                                                        )
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = String.format("%02d", i + 1),
                                                    color = Color.White.copy(alpha = 0.85f),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    style = TextStyle(shadow = androidx.compose.ui.graphics.Shadow(color = Color.Black, blurRadius = 4f))
                                                )
                                            }

                                            Column {
                                                Text(
                                                    text = nextItem.title,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextPrimary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "${nextItem.artist} • ${nextItem.album}",
                                                    fontSize = 10.sp,
                                                    color = TextSecondary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            if (nextItem.isDownloaded) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(14.dp)
                                                        .clip(CircleShape)
                                                        .background(GreenStatus.copy(alpha = 0.15f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = "Descargado",
                                                        tint = GreenStatus,
                                                        modifier = Modifier.size(8.dp)
                                                    )
                                                }
                                            }

                                            Text(
                                                text = formatDuration(nextItem.durationSecs),
                                                fontSize = 10.sp,
                                                color = TextSecondary,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold
                                            )

                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowRight,
                                                contentDescription = null,
                                                tint = TextSecondary.copy(alpha = 0.4f),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }

        // Fullscreen dynamic synchronized lyrics view
        AnimatedVisibility(
            visible = isLyricsExpanded,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium)
            ),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(durationMillis = 250)
            )
        ) {
            ExpandedLyricsView(
                song = song,
                lyrics = getLyricsForSong(song.id),
                progressSec = progressSec,
                isPlaying = isPlaying,
                onPlayPause = onPlayPause,
                onNext = onNext,
                onPrev = onPrev,
                onSeek = onSeek,
                onClose = { isLyricsExpanded = false },
                modifier = Modifier.fillMaxSize()
            )
        }

        // DELUXE SLIDE-UP QUEUE SHEET OVERLAY PANEL (Draws directly over the player)
        AnimatedVisibility(
            visible = showQueueSheet,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium)
            ),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(durationMillis = 250)
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(enabled = true, onClick = { showQueueSheet = false })
            ) {
                Card(
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.82f)
                        .align(Alignment.BottomCenter)
                        .border(
                            width = 1.2.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(NeonViolet, Color.Transparent)
                            ),
                            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                        )
                        .clickable(enabled = false) {},
                    elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp, vertical = 22.dp)
                    ) {
                        // Drag Indicator
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(width = 44.dp, height = 4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(TextSecondary.copy(alpha = 0.35f))
                                    .clickable { showQueueSheet = false }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QueueMusic,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = "Cola de Reproducción",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextPrimary
                                )
                            }

                            IconButton(onClick = { showQueueSheet = false }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cerrar",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Text(
                            text = "${playbackQueue.size} canciones cargadas",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 14.dp)
                        )

                        // Playback complete elements list
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            itemsIndexed(playbackQueue) { idx, item ->
                                val isCurrent = item.id == song.id
                                val gradientColors = getCoverColorsForSong(item)
                                val itemBg = if (isCurrent) NeonViolet.copy(alpha = 0.16f) else TextPrimary.copy(alpha = 0.02f)
                                val itemBorderColor = if (isCurrent) NeonCyan.copy(alpha = 0.4f) else Color.Transparent

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(itemBg)
                                        .border(1.dp, itemBorderColor, RoundedCornerShape(12.dp))
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            onPlaySongFromQueue(item)
                                        }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    Brush.linearGradient(colors = listOf(gradientColors.first, gradientColors.second))
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isCurrent && isPlaying) {
                                                MiniatureLiveEqualizer(color = Color.White, isPlaying = true)
                                            } else {
                                                Text(
                                                    text = (idx + 1).toString(),
                                                    color = Color.White.copy(alpha = 0.7f),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        Column {
                                            Text(
                                                text = item.title,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isCurrent) NeonCyan else TextPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = item.artist,
                                                fontSize = 11.sp,
                                                color = if (isCurrent) NeonCyan.copy(alpha = 0.8f) else TextSecondary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (item.isDownloaded) {
                                            Icon(
                                                imageVector = Icons.Default.OfflinePin,
                                                contentDescription = null,
                                                tint = GreenStatus,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                        Text(
                                            text = formatDuration(item.durationSecs),
                                            fontSize = 11.sp,
                                            color = TextSecondary,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 5. CUSTOM PLAYLIST DISPLAY CARD
@Composable
fun PlaylistCard(
    playlist: Playlist,
    songs: List<Song>,
    isOffline: Boolean,
    currentSong: Song?,
    isPlaying: Boolean,
    likedSongIds: Set<String> = emptySet(),
    onPlayAll: () -> Unit,
    onPlaySong: (Song) -> Unit,
    onRemoveSong: (String) -> Unit,
    onDeletePlaylist: () -> Unit,
    onToggleFavorite: (String) -> Unit = {}
) {
    var isExpanded by remember { mutableStateOf(false) }
    var songSearchQuery by remember { mutableStateOf("") }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderAmbient, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(NeonViolet.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.QueueMusic, contentDescription = null, tint = NeonViolet)
                    }

                    Column {
                        Text(playlist.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            text = playlist.description.ifEmpty { "Lista personalizada" },
                            color = TextSecondary,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Play all in playlist button
                    if (songs.isNotEmpty()) {
                        IconButton(onClick = onPlayAll) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play All", tint = NeonCyan, modifier = Modifier.size(24.dp))
                        }
                    }

                    // Delete playlist button
                    IconButton(onClick = onDeletePlaylist) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = HotPink, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Expandable Songs rows list
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DeepSableSpace)
                    .clickable { isExpanded = !isExpanded }
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${songs.size} canciones " + if (isOffline) "(offline)" else "",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (songs.isNotEmpty()) {
                        // Compact and beautiful Material 3 search bar for finding songs inside the playlist
                        OutlinedTextField(
                            value = songSearchQuery,
                            onValueChange = { songSearchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("playlist_song_search_${playlist.id}"),
                            placeholder = {
                                Text(
                                    "Buscar en esta lista...",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Buscar canción",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            trailingIcon = {
                                if (songSearchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { songSearchQuery = "" },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Limpiar búsqueda",
                                            tint = TextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = DeepSableSpace,
                                unfocusedContainerColor = DeepSableSpace,
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = BorderAmbient.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    val filteredSongs = remember(songs, songSearchQuery) {
                        if (songSearchQuery.isBlank()) {
                            songs
                        } else {
                            songs.filter {
                                it.title.contains(songSearchQuery, ignoreCase = true) ||
                                it.artist.contains(songSearchQuery, ignoreCase = true)
                            }
                        }
                    }

                    if (filteredSongs.isEmpty()) {
                        Text(
                            text = if (songs.isEmpty()) "No hay canciones en esta lista." else "No se encontraron canciones que coincidan.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        )
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            filteredSongs.forEach { song ->
                                val isCurrent = currentSong?.id == song.id
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isCurrent) NeonCyan.copy(alpha = 0.12f)
                                            else DeepSableSpace.copy(alpha = 0.5f)
                                        )
                                        .clickable { onPlaySong(song) }
                                        .border(
                                            width = 1.dp,
                                            color = if (isCurrent) NeonCyan.copy(alpha = 0.4f) else Color.Transparent,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = if (isCurrent && isPlaying) Icons.Default.VolumeUp else Icons.Default.MusicNote,
                                            contentDescription = if (isCurrent) "Reproduciendo" else "Canción",
                                            tint = if (isCurrent) NeonCyan else TextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )

                                        Column {
                                            Text(
                                                text = song.title,
                                                color = if (isCurrent) NeonCyan else TextPrimary,
                                                fontSize = 12.sp,
                                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = song.artist,
                                                color = TextSecondary,
                                                fontSize = 10.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        val isLiked = likedSongIds.contains(song.id)
                                        IconButton(
                                            onClick = { onToggleFavorite(song.id) },
                                            modifier = Modifier.size(28.dp).testTag("playlist_song_favorite_${song.id}")
                                        ) {
                                            Icon(
                                                imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                contentDescription = "Favorito",
                                                tint = if (isLiked) HotPink else TextSecondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = { onPlaySong(song) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isCurrent && isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                contentDescription = "Reproducir o pausar",
                                                tint = if (isCurrent) NeonCyan else TextSecondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = { onRemoveSong(song.id) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.RemoveCircleOutline,
                                                contentDescription = "Quitar de la lista",
                                                tint = HotPink.copy(alpha = 0.8f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 6. EMPTY STATE HELPER
@Composable
fun EmptyStatePlaceholder(
    message: String,
    suggestion: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(DarkSurfaceCard),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Inbox,
                contentDescription = null,
                tint = NeonViolet,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = suggestion,
            color = TextSecondary,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
    }
}

// HELPER DURATION FORMATTER
private fun formatDuration(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%d:%02d", m, s)
}

// 7. HERO SMART MIX CARD
@Composable
fun HeroCard(onPlaySmart: () -> Unit = {}) {
    Column {
        Text(
            text = "FOR YOU",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(NeonViolet, NeonCyan)
                    )
                )
                .clickable { onPlaySmart() }
                .padding(20.dp)
        ) {
            // "Daily Mix" Badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Daily Mix",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            Column(
                modifier = Modifier.align(Alignment.BottomStart)
            ) {
                Text(
                    text = "Basado en tu historial",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Melodías de Medianoche",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 26.sp
                )
            }
        }
    }
}

// Bouncy scale touch feedback modifier for premium micro-interactions
fun Modifier.bounceClick() = composed {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "click_bounce"
    )

    this
        .scale(scale)
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    isPressed = true
                    try {
                        tryAwaitRelease()
                    } finally {
                        isPressed = false
                    }
                }
            )
        }
}

// Lyrics Models and database
data class LyricLine(val timeSec: Int, val text: String)

fun getLyricsForSong(songId: String): List<LyricLine> {
    return when (songId) {
        "neon_horizon" -> listOf(
            LyricLine(0, "[Intro Instrumental Synthwave]"),
            LyricLine(4, "Bajo las luces que cruzan la ciudad..."),
            LyricLine(10, "El viento frío empieza a susurrar..."),
            LyricLine(16, "Buscando un norte en la eternidad..."),
            LyricLine(22, "Siento los beats de este neón brillar."),
            LyricLine(28, "[Estribillo: Sintetizadores Analógicos]"),
            LyricLine(34, "¡Corre, corre hacia el horizonte de neón!"),
            LyricLine(40, "Donde el destino pierde su dirección..."),
            LyricLine(46, "En este viaje no hay separación."),
            LyricLine(54, "[Solo de Sintetizador de Ondas]"),
            LyricLine(68, "Las siluetas van perdiendo el control..."),
            LyricLine(74, "Persiguiendo destellos de un lejano sol..."),
            LyricLine(81, "Y bajo el manto del amanecer..."),
            LyricLine(88, "Volveremos a renacer."),
            LyricLine(95, "¡Sólo sígueme, no mires atrás!")
        )
        "midnight_drive" -> listOf(
            LyricLine(0, "[Acelerando el motor de 8 bits]"),
            LyricLine(6, "Carretera vacía, la noche es de los dos..."),
            LyricLine(12, "Las luces traseras se desvanecen hoy."),
            LyricLine(18, "Pisando el acelerador sin temor..."),
            LyricLine(24, "Con el cassette tocando la canción de amor."),
            LyricLine(31, "[Melodía Retro Wave Intensa]"),
            LyricLine(40, "Carretera sin fin, destino espacial..."),
            LyricLine(47, "Ondas retro en un viaje sideral."),
            LyricLine(54, "La medianoche nos viene a abrazar."),
            LyricLine(62, "¡Esquivando el tiempo, un segundo más!")
        )
        "digital_dr" -> listOf(
            LyricLine(0, "[Cargando flujo de datos digitales...]"),
            LyricLine(8, "Sueños en código binario y bytes..."),
            LyricLine(15, "Algoritmos que cantan en la oscuridad."),
            LyricLine(22, "Nuestra mente conectada a la red..."),
            LyricLine(29, "¿Es esto un sueño o la realidad virtual?"),
            LyricLine(36, "[Solo de Chiptune Eléctrico]"),
            LyricLine(48, "Viajando por la web, sin rumbo fijo..."),
            LyricLine(55, "Señales digitales que tocan el corazón."),
            LyricLine(62, "La frequency perfecta para soñar.")
        )
        "acoustic_rn" -> listOf(
            LyricLine(0, "[Acordes de guitarra acústica suave]"),
            LyricLine(8, "Gotas de lluvia golpean el cristal..."),
            LyricLine(15, "Un café templado para recordar."),
            LyricLine(23, "Palabras gastadas que el viento se llevó..."),
            LyricLine(31, "Pero esta melodía aún vive entre los dos."),
            LyricLine(39, "[Fusión de Flauta y Acústica]"),
            LyricLine(48, "Si la tormenta ruge con fuerza hoy..."),
            LyricLine(56, "La madera templada nos da calor."),
            LyricLine(64, "El tiempo pasa lento, vuelve el amor.")
        )
        else -> listOf(
            LyricLine(0, "[Melodía Ambiental Relajante]"),
            LyricLine(10, "La música fluye por tus venas..."),
            LyricLine(22, "Siente la frecuencia sintética vibrar..."),
            LyricLine(35, "Bajo un dosel de estrellas virtuales..."),
            LyricLine(48, "Donde las notas nunca van a parar..."),
            LyricLine(60, "El pulso analógico te guía..."),
            LyricLine(75, "[Solo Instrumental de Cierre]")
        )
    }
}

// Centered auto-scrolling lyrics reader panel
@Composable
fun MiniatureLiveEqualizer(
    color: Color,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    if (isPlaying) {
        val infiniteTransition = rememberInfiniteTransition(label = "eq_bar_jump")
        val b1 by infiniteTransition.animateFloat(
            initialValue = 4f, targetValue = 20f,
            animationSpec = infiniteRepeatable(tween(450, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "b1"
        )
        val b2 by infiniteTransition.animateFloat(
            initialValue = 6f, targetValue = 16f,
            animationSpec = infiniteRepeatable(tween(350, easing = LinearEasing), RepeatMode.Reverse),
            label = "b2"
        )
        val b3 by infiniteTransition.animateFloat(
            initialValue = 5f, targetValue = 18f,
            animationSpec = infiniteRepeatable(tween(400, easing = FastOutLinearInEasing), RepeatMode.Reverse),
            label = "b3"
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.5.dp),
            verticalAlignment = Alignment.Bottom,
            modifier = modifier.height(20.dp)
        ) {
            Box(Modifier.width(3.dp).height(b1.dp).clip(RoundedCornerShape(1.5.dp)).background(color))
            Box(Modifier.width(3.dp).height(b2.dp).clip(RoundedCornerShape(1.5.dp)).background(color))
            Box(Modifier.width(3.dp).height(b3.dp).clip(RoundedCornerShape(1.5.dp)).background(color))
        }
    } else {
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.5.dp),
            verticalAlignment = Alignment.Bottom,
            modifier = modifier.height(20.dp)
        ) {
            Box(Modifier.width(3.dp).height(5.dp).clip(RoundedCornerShape(1.5.dp)).background(color.copy(alpha = 0.5f)))
            Box(Modifier.width(3.dp).height(8.dp).clip(RoundedCornerShape(1.5.dp)).background(color.copy(alpha = 0.5f)))
            Box(Modifier.width(3.dp).height(4.dp).clip(RoundedCornerShape(1.5.dp)).background(color.copy(alpha = 0.5f)))
        }
    }
}

@Composable
fun SincronizedLyricsView(
    lyrics: List<LyricLine>,
    progressSec: Int,
    modifier: Modifier = Modifier,
    onExpandClick: (() -> Unit)? = null
) {
    val activeLineIndex = lyrics.indexOfLast { progressSec >= it.timeSec }.coerceAtLeast(0)
    val listState = rememberLazyListState()
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(activeLineIndex) {
        if (lyrics.isNotEmpty() && activeLineIndex >= 0) {
            listState.animateScrollToItem(activeLineIndex)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(310.dp)
            .background(Color.Transparent)
            .then(
                if (onExpandClick != null) {
                    Modifier.clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onExpandClick()
                    }
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        // Active line accent highlight box
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .height(60.dp)
                .align(Alignment.Center)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            NeonCyan.copy(alpha = 0.08f),
                            NeonCyan.copy(alpha = 0.01f)
                        )
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
        )

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 120.dp), // Height spacing to keep active elements centered
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(lyrics) { idx, lyric ->
                val isActive = idx == activeLineIndex
                val fontSize = if (isActive) 18.sp else 14.sp
                val fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Medium
                val fontColor = if (isActive) NeonCyan else TextSecondary.copy(alpha = 0.4f)
                val scale by animateFloatAsState(
                    targetValue = if (isActive) 1.05f else 0.95f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                    label = "v_lyric_txt_scale"
                )

                Text(
                    text = lyric.text,
                    fontSize = fontSize,
                    fontWeight = fontWeight,
                    color = fontColor,
                    textAlign = TextAlign.Center,
                    style = if (isActive) {
                        TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = NeonCyan.copy(alpha = 0.6f),
                                offset = Offset(0f, 0f),
                                blurRadius = 14f
                            )
                        )
                    } else {
                        TextStyle.Default
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .scale(scale)
                        .padding(horizontal = 4.dp),
                    maxLines = 2
                )
            }
        }

        // Top and bottom smooth theatrical fade overlays
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(DeepSableSpace, Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, DeepSableSpace)
                    )
                )
        )

        // Mini expandable overlay helper icon at Top-Right
        if (onExpandClick != null) {
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onExpandClick()
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.25f))
                    .testTag("lyrics_expand_icon")
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInFull,
                    contentDescription = "Pantalla Completa",
                    tint = NeonCyan,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// Fullscreen, responsive, highly animated expanded lyrics panel
@Composable
fun ExpandedLyricsView(
    song: Song,
    lyrics: List<LyricLine>,
    progressSec: Int,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onSeek: (Int) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val activeLineIndex = lyrics.indexOfLast { progressSec >= it.timeSec }.coerceAtLeast(0)
    val listState = rememberLazyListState()

    // Smooth auto-scroll the expanded lyrics to the active line
    LaunchedEffect(activeLineIndex) {
        if (lyrics.isNotEmpty() && activeLineIndex >= 0) {
            listState.animateScrollToItem(activeLineIndex)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "expanded_lyrics_gradients")
    
    // Smooth flowing shimmer gradient offset
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1500f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "txt_gradient_offset"
    )

    // Flowing liquid element base progress
    val rawProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "raw_liquid_progress"
    )

    val waveOffset = rawProgress * 2f * Math.PI.toFloat()
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepSableSpace)
            .pointerInput(Unit) {} // Prevent clicking behind the overlay
            .drawBehind {
                val width = size.width
                val height = size.height
                
                // Draw dynamic ambient space radial gradient
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(DarkSurfaceCard, DeepSableSpace),
                        center = Offset(width / 2f, height / 2f),
                        radius = width
                    )
                )

                // 4 fluid ambient molten liquid bubbles slowly drifting (AURA CHROME M3)
                // Bubble 1 (Neon Violet)
                val bx1 = width * (0.3f + 0.15f * kotlin.math.sin(waveOffset).toFloat())
                val by1 = height * (0.25f + 0.12f * kotlin.math.cos(waveOffset * 1.3f).toFloat())
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(NeonViolet.copy(alpha = 0.22f), Color.Transparent),
                        center = Offset(bx1, by1),
                        radius = width * 0.6f
                    ),
                    center = Offset(bx1, by1),
                    radius = width * 0.6f
                )

                // Bubble 2 (Neon Cyan)
                val bx2 = width * (0.7f + 0.12f * kotlin.math.cos(waveOffset * 0.7f).toFloat())
                val by2 = height * (0.6f + 0.15f * kotlin.math.sin(waveOffset * 1.4f).toFloat())
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(NeonCyan.copy(alpha = 0.18f), Color.Transparent),
                        center = Offset(bx2, by2),
                        radius = width * 0.5f
                    ),
                    center = Offset(bx2, by2),
                    radius = width * 0.5f
                )

                // Bubble 3 (Soft Lilac Edge Drift)
                val bx3 = width * (0.1f + 0.1f * kotlin.math.sin(waveOffset * 0.8f).toFloat())
                val by3 = height * (0.8f + 0.1f * kotlin.math.cos(waveOffset * 1.1f).toFloat())
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(SoftLilac.copy(alpha = 0.25f), Color.Transparent),
                        center = Offset(bx3, by3),
                        radius = width * 0.55f
                    ),
                    center = Offset(bx3, by3),
                    radius = width * 0.55f
                )

                // Bubble 4 (Hot Pink pulse)
                val bx4 = width * (0.85f + 0.08f * kotlin.math.cos(waveOffset * 1.2f).toFloat())
                val by4 = height * (0.15f + 0.08f * kotlin.math.sin(waveOffset * 0.9f).toFloat())
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(HotPink.copy(alpha = 0.15f), Color.Transparent),
                        center = Offset(bx4, by4),
                        radius = width * 0.45f
                    ),
                    center = Offset(bx4, by4),
                    radius = width * 0.45f
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onClose()
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(TextPrimary.copy(alpha = 0.08f))
                        .testTag("lyrics_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = "Collapse lyrics",
                        tint = TextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "LETRA EN VIVO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NeonCyan,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = song.title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = song.artist,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(TextPrimary.copy(alpha = 0.05f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        tint = NeonViolet,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Central auto-scrolled scrolling canvas
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background focus box to frame/highlight active line height nicely
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(96.dp)
                        .align(Alignment.Center)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    NeonCyan.copy(alpha = 0.16f),
                                    NeonCyan.copy(alpha = 0.02f)
                                )
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(
                            width = 1.dp,
                            brush = Brush.horizontalGradient(
                                colors = listOf(NeonCyan.copy(alpha = 0.4f), Color.Transparent, NeonCyan.copy(alpha = 0.4f))
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                )

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 220.dp), // Spacious vertical padding to keep active centered
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(28.dp)
                ) {
                    itemsIndexed(lyrics) { idx, lyric ->
                        val isActive = idx == activeLineIndex
                        
                        val scale by animateFloatAsState(
                            targetValue = if (isActive) 1.08f else 0.94f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "expanded_lyric_scale"
                        )
                        
                        val fontSize = if (isActive) 23.sp else 16.sp
                        val fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Bold
                        
                        // Continuous animated horizontal-diagonal gradient offset for dormant lines
                        val dormantBrush = Brush.linearGradient(
                            colors = listOf(
                                TextSecondary.copy(alpha = 0.45f),
                                TextPrimary.copy(alpha = 0.70f),
                                TextSecondary.copy(alpha = 0.45f)
                            ),
                            start = Offset(gradientOffset + (idx * 60f), 0f),
                            end = Offset(gradientOffset + (idx * 60f) + 400f, 400f),
                            tileMode = TileMode.Repeated
                        )

                        // Highly structured elegant row containing timestamps and interactive controls
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isActive) TextPrimary.copy(alpha = 0.04f) else Color.Transparent)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onSeek(lyric.timeSec)
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                                .testTag("expanded_lyric_line_$idx"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Left column: miniature live sound visualizer dancer or Monospace seeking timestamp
                            Box(
                                modifier = Modifier.width(48.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (isActive) {
                                    MiniatureLiveEqualizer(color = NeonCyan, isPlaying = isPlaying)
                                } else {
                                    val formattedTime = String.format("%02d:%02d", lyric.timeSec / 60, lyric.timeSec % 60)
                                    Text(
                                        text = formattedTime,
                                        color = TextPrimary.copy(alpha = 0.35f),
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Center column: centered elegant lyric statement text
                            Text(
                                text = lyric.text,
                                fontSize = fontSize,
                                fontWeight = fontWeight,
                                textAlign = TextAlign.Start,
                                style = if (isActive) {
                                    TextStyle(
                                        color = NeonCyan,
                                        shadow = androidx.compose.ui.graphics.Shadow(
                                            color = NeonCyan.copy(alpha = 0.75f),
                                            offset = Offset(0f, 0f),
                                            blurRadius = 18f
                                        )
                                    )
                                } else {
                                    TextStyle(brush = dormantBrush)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .scale(scale)
                                    .padding(horizontal = 10.dp)
                            )

                            // Right column: subtle navigation assist indicator
                            Icon(
                                imageVector = if (isActive) Icons.Default.MusicNote else Icons.Default.PlayArrow,
                                contentDescription = "Seeking icon indicator",
                                tint = if (isActive) NeonViolet else TextPrimary.copy(alpha = 0.25f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Top and bottom cinematic edge fades
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(DeepSableSpace, Color.Transparent)
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, DeepSableSpace)
                            )
                        )
                )
            }

            // Dynamic bottom console with progress Slider & minimal tactile controllers
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftLilac.copy(alpha = 0.12f)),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, BorderAmbient.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .testTag("lyrics_bottom_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val totalDuration = song.durationSecs
                    val progressFloat = progressSec.coerceIn(0, totalDuration).toFloat()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = String.format("%02d:%02d", progressSec / 60, progressSec % 60),
                            fontSize = 11.sp,
                            color = TextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = String.format("%02d:%02d", totalDuration / 60, totalDuration % 60),
                            fontSize = 11.sp,
                            color = TextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Slider(
                        value = progressFloat,
                        onValueChange = { onSeek(it.toInt()) },
                        valueRange = 0f..totalDuration.toFloat(),
                        colors = SliderDefaults.colors(
                            thumbColor = NeonCyan,
                            activeTrackColor = NeonCyan,
                            inactiveTrackColor = TextPrimary.copy(alpha = 0.12f)
                        ),
                        modifier = Modifier
                            .height(18.dp)
                            .testTag("lyrics_playback_slider")
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(32.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onPrev()
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipPrevious,
                                contentDescription = "Retroceder cancion",
                                tint = TextPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(NeonViolet, NeonCyan)
                                    )
                                )
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onPlayPause()
                                }
                                .testTag("lyrics_play_pause"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play o pausar",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onNext()
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Siguiente cancion",
                                tint = TextPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Gorgeous reactive Clock Dial widget for the Sleep Timer
@Composable
fun SleepTimerDialCard(
    minutesLeft: Int,
    onSetTimer: (Int) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        border = BorderStroke(0.5.dp, BorderAmbient),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Temporizador de Apagado (Sleep Timer)", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    Text("Desvanece la música progresivamente para dormir.", fontSize = 11.sp, color = TextSecondary)
                }
                if (minutesLeft > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(NeonCyan.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Activo: ${minutesLeft} min",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                    }
                } else {
                    Text("Inactivo", fontSize = 11.sp, color = TextSecondary)
                }
            }

            // Dial selection row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(0, 15, 30, 45, 60).forEach { mins ->
                    val isMatched = if (mins == 0) minutesLeft == 0 else (minutesLeft > mins - 15 && minutesLeft <= mins)

                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(if (isMatched) NeonViolet.copy(alpha = 0.2f) else DeepSableSpace)
                            .border(
                                width = 1.dp,
                                color = if (isMatched) NeonViolet else BorderAmbient,
                                shape = CircleShape
                            )
                            .clickable { onSetTimer(mins) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (mins == 0) "OFF" else "${mins}m",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isMatched) NeonCyan else TextSecondary
                        )
                    }
                }
            }

            // Interactive Concentric Ring Arc
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Canvas(modifier = Modifier.size(110.dp)) {
                    // Background track ring
                    drawArc(
                        color = BorderAmbient.copy(alpha = 0.4f),
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 6.dp.toPx(),
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    )

                    // Active progression arc ring
                    if (minutesLeft > 0) {
                        val maxMins = 60f
                        val sweep = (minutesLeft.toFloat() / maxMins).coerceAtMost(1f) * 270f
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(NeonViolet, NeonCyan, NeonViolet)
                            ),
                            startAngle = 135f,
                            sweepAngle = sweep,
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = 6.dp.toPx(),
                                cap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = if (minutesLeft > 0) NeonCyan else TextSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    if (minutesLeft > 0) {
                        Text(
                            text = "${minutesLeft}m",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Text("restantes", fontSize = 9.sp, color = TextSecondary)
                    } else {
                        Text(
                            text = "Ajustar",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

// Graphic Equalizer panel with sliders for Bass, Mid and Treble harmonics
@Composable
fun GraphicEqualizerCard(
    bass: Float,
    mid: Float,
    treble: Float,
    onBassChange: (Float) -> Unit,
    onMidChange: (Float) -> Unit,
    onTrebleChange: (Float) -> Unit,
    selectedPreset: EqPreset,
    onPresetSelected: (EqPreset) -> Unit,
    isPlaying: Boolean = false,
    realtimeWaveform: FloatArray = FloatArray(0)
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        border = BorderStroke(0.5.dp, BorderAmbient),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().testTag("graphic_equalizer_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column {
                Text("Ecualizador Gráfico de Harmónicos", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text("Sintoniza el timbre de la síntesis analógica directa o elige un preset.", fontSize = 11.sp, color = TextSecondary)
            }

            // Scrollable row of premium presets
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().testTag("eq_presets_row")
            ) {
                items(EqPreset.values()) { preset ->
                    val isSelected = selectedPreset == preset
                    val haptic = LocalHapticFeedback.current
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) NeonViolet.copy(alpha = 0.22f) else DeepSableSpace.copy(alpha = 0.45f))
                            .border(
                                width = 1.dp,
                                color = if (isSelected) NeonViolet else BorderAmbient.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onPresetSelected(preset)
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("eq_preset_${preset.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = NeonViolet,
                                    modifier = Modifier.size(13.dp).padding(end = 4.dp)
                                )
                            }
                            Text(
                                text = when (preset) {
                                    EqPreset.FLAT -> "Plano 🎚️"
                                    EqPreset.BASS_BOOST -> "Bass Boost 🔊"
                                    EqPreset.VOCAL_FOCUS -> "Vocal Focus 🎙️"
                                    EqPreset.ACOUSTIC -> "Acoustic 🎸"
                                    EqPreset.CUSTOM -> "Personalizado ⚙️"
                                },
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (isSelected) NeonViolet else TextPrimary
                            )
                        }
                    }
                }
            }

            // Embedded live biological spectrum visualizer
            WaveformVisualizer(
                isPlaying = isPlaying,
                styleStr = "Fluid Synced",
                bass = bass,
                mid = mid,
                treble = treble,
                realtimeWaveform = realtimeWaveform,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DeepSableSpace)
                    .padding(vertical = 4.dp)
                    .testTag("eq_wave_canvas")
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bass
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("GRAVES", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = NeonViolet, letterSpacing = 0.5.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Slider(
                            value = bass,
                            onValueChange = onBassChange,
                            valueRange = 0.2f..2.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = NeonViolet,
                                activeTrackColor = NeonViolet,
                                inactiveTrackColor = DeepSableSpace
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${(bass * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }

                // Mid
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("MEDIOS", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = NeonCyan, letterSpacing = 0.5.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Slider(
                            value = mid,
                            onValueChange = onMidChange,
                            valueRange = 0.2f..2.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = NeonCyan,
                                activeTrackColor = NeonCyan,
                                inactiveTrackColor = DeepSableSpace
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${(mid * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }

                // Treble
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("AGUDOS", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = HotPink, letterSpacing = 0.5.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Slider(
                            value = treble,
                            onValueChange = onTrebleChange,
                            valueRange = 0.2f..2.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = HotPink,
                                activeTrackColor = HotPink,
                                inactiveTrackColor = DeepSableSpace
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${(treble * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            }
        }
    }
}
