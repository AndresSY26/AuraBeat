package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.player.MusicPlayerEngine
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

enum class SmartShuffleMode(val displayName: String, val description: String) {
    STANDARD("Estándar", "Orden aleatorio de todas las pistas"),
    ARTIST("Por Artista", "Agrupación de artistas similares"),
    MOOD("Por Mood", "Basado en energía (Relajante, Moderado, Enérgico)")
}

enum class EqPreset(val displayName: String, val bass: Float, val mid: Float, val treble: Float) {
    FLAT("Plano", 1.0f, 1.0f, 1.0f),
    BASS_BOOST("Bass Boost", 1.6f, 1.0f, 0.9f),
    VOCAL_FOCUS("Vocal Focus", 0.8f, 1.6f, 1.2f),
    ACOUSTIC("Acoustic", 1.2f, 0.9f, 1.4f),
    CUSTOM("Personalizado", 1.0f, 1.0f, 1.0f)
}

object SongMetadataHelper {
    fun getEnergyLevel(songId: String): Float {
        return when (songId) {
            "deep_bass" -> 0.9f
            "pixel_hearts" -> 0.85f
            "digital_dr" -> 0.8f
            "neon_horizon" -> 0.7f
            "midnight_drive" -> 0.6f
            "golden_sky" -> 0.5f
            "acoustic_rn" -> 0.3f
            "solar_winds" -> 0.15f
            "sys_mock_1" -> 0.5f
            "sys_mock_2" -> 0.2f
            "sys_mock_3" -> 0.1f
            else -> 0.5f
        }
    }

    fun getMoodTag(songId: String): String {
        val energy = getEnergyLevel(songId)
        return when {
            energy >= 0.75f -> "Enérgico 🔥"
            energy >= 0.45f -> "Moderado ⚡"
            else -> "Relajante 🍃"
        }
    }
}

class MusicViewModel(application: Application) : AndroidViewModel(application) {
    private val database = MusicDatabase.getDatabase(application, viewModelScope)
    private val repository = MusicRepository(database.musicDao())

    // Synth Engine
    private val synthEngine = MusicPlayerEngine(
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            application.createAttributionContext("default")
        } else {
            application
        }
    )

    // Screen State
    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Main Flows
    val allSongs: StateFlow<List<Song>> = repository.allSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPlaylists: StateFlow<List<PlaylistWithSongs>> = repository.allPlaylists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val likedSongIds: StateFlow<Set<String>> = repository.allPlaylists
        .map { list ->
            list.find { it.playlist.name.equals("Liked", ignoreCase = true) }
                ?.songs?.map { it.id }?.toSet() ?: emptySet()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val recentSearches: StateFlow<List<RecentSearch>> = repository.recentSearches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Player State
    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPlayProgress = MutableStateFlow(0)
    val currentPlayProgress: StateFlow<Int> = _currentPlayProgress.asStateFlow()

    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()

    private val _crossfadeSecs = MutableStateFlow(0)
    val crossfadeSecs: StateFlow<Int> = _crossfadeSecs.asStateFlow()

    private val _smartShuffleMode = MutableStateFlow(SmartShuffleMode.STANDARD)
    val smartShuffleMode: StateFlow<SmartShuffleMode> = _smartShuffleMode.asStateFlow()

    private val _isRepeatEnabled = MutableStateFlow(false)
    val isRepeatEnabled: StateFlow<Boolean> = _isRepeatEnabled.asStateFlow()

    private val _synthMuted = MutableStateFlow(false)
    val synthMuted: StateFlow<Boolean> = _synthMuted.asStateFlow()

    // Settings & Personalizations Live Configurations
    private val _synthVolume = MutableStateFlow(0.12f)
    val synthVolume: StateFlow<Float> = _synthVolume.asStateFlow()

    private val _synthWaveformType = MutableStateFlow("Sine")
    val synthWaveformType: StateFlow<String> = _synthWaveformType.asStateFlow()

    private val _waveformStyleStr = MutableStateFlow("Classic Neon") // "Classic Neon", "Pulsing Line", "Solid Wave"
    val waveformStyleStr: StateFlow<String> = _waveformStyleStr.asStateFlow()

    private val _showDailyRecommended = MutableStateFlow(true)
    val showDailyRecommended: StateFlow<Boolean> = _showDailyRecommended.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    // Active Sleep Timer States (0 means disabled)
    private val _sleepTimerMinutesLeft = MutableStateFlow(0)
    val sleepTimerMinutesLeft: StateFlow<Int> = _sleepTimerMinutesLeft.asStateFlow()

    private var sleepTimerJob: Job? = null
    private var preFadeVolume: Float = 0.12f

    // 3 active EQ Bands starting at 1.0f (Baseline)
    private val _eqBass = MutableStateFlow(1.0f)
    val eqBass: StateFlow<Float> = _eqBass.asStateFlow()

    private val _eqMid = MutableStateFlow(1.0f)
    val eqMid: StateFlow<Float> = _eqMid.asStateFlow()

    private val _eqTreble = MutableStateFlow(1.0f)
    val eqTreble: StateFlow<Float> = _eqTreble.asStateFlow()

    private val _selectedEqPreset = MutableStateFlow(EqPreset.FLAT)
    val selectedEqPreset: StateFlow<EqPreset> = _selectedEqPreset.asStateFlow()

    // Firestore Synchronization State
    private val _syncProjectId = MutableStateFlow("musicplayer-synth-sync")
    val syncProjectId: StateFlow<String> = _syncProjectId.asStateFlow()

    private val _syncCode = MutableStateFlow("")
    val syncCode: StateFlow<String> = _syncCode.asStateFlow()

    private val _syncApiKey = MutableStateFlow("")
    val syncApiKey: StateFlow<String> = _syncApiKey.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncStatus = MutableStateFlow("Listo para sincronizar")
    val syncStatus: StateFlow<String> = _syncStatus.asStateFlow()

    // Wi-Fi Auto sync quality of life setting
    private val _wifiAutoSyncEnabled = MutableStateFlow(false)
    val wifiAutoSyncEnabled: StateFlow<Boolean> = _wifiAutoSyncEnabled.asStateFlow()

    // Advanced ADSR StateFlows
    private val _envelopeAttack = MutableStateFlow(0.15f)
    val envelopeAttack: StateFlow<Float> = _envelopeAttack.asStateFlow()

    private val _envelopeDecay = MutableStateFlow(0.15f)
    val envelopeDecay: StateFlow<Float> = _envelopeDecay.asStateFlow()

    private val _envelopeSustain = MutableStateFlow(0.7f)
    val envelopeSustain: StateFlow<Float> = _envelopeSustain.asStateFlow()

    private val _envelopeRelease = MutableStateFlow(0.25f)
    val envelopeRelease: StateFlow<Float> = _envelopeRelease.asStateFlow()

    // Advanced LFO StateFlows
    private val _lfoTremoloActive = MutableStateFlow(false)
    val lfoTremoloActive: StateFlow<Boolean> = _lfoTremoloActive.asStateFlow()

    private val _lfoTremoloRate = MutableStateFlow(3.0f)
    val lfoTremoloRate: StateFlow<Float> = _lfoTremoloRate.asStateFlow()

    private val _lfoTremoloDepth = MutableStateFlow(0.3f)
    val lfoTremoloDepth: StateFlow<Float> = _lfoTremoloDepth.asStateFlow()

    private val _lfoVibratoActive = MutableStateFlow(false)
    val lfoVibratoActive: StateFlow<Boolean> = _lfoVibratoActive.asStateFlow()

    private val _lfoVibratoRate = MutableStateFlow(4.0f)
    val lfoVibratoRate: StateFlow<Float> = _lfoVibratoRate.asStateFlow()

    private val _lfoVibratoDepth = MutableStateFlow(0.1f)
    val lfoVibratoDepth: StateFlow<Float> = _lfoVibratoDepth.asStateFlow()

    // Gemini states
    private val _songTrivia = MutableStateFlow("")
    val songTrivia: StateFlow<String> = _songTrivia.asStateFlow()

    private val _isTriviaLoading = MutableStateFlow(false)
    val isTriviaLoading: StateFlow<Boolean> = _isTriviaLoading.asStateFlow()

    private val _isAmbientLoading = MutableStateFlow(false)
    val isAmbientLoading: StateFlow<Boolean> = _isAmbientLoading.asStateFlow()

    private val _isPlaylistLoading = MutableStateFlow(false)
    val isPlaylistLoading: StateFlow<Boolean> = _isPlaylistLoading.asStateFlow()

    private val _vibeExplanation = MutableStateFlow<String?>(null)
    val vibeExplanation: StateFlow<String?> = _vibeExplanation.asStateFlow()

    // ADVANCED SYNTH ENGINE SETTERS
    fun setEnvelopeAttack(v: Float) {
        _envelopeAttack.value = v
        synthEngine.envelopeAttack = v
    }
    fun setEnvelopeDecay(v: Float) {
        _envelopeDecay.value = v
        synthEngine.envelopeDecay = v
    }
    fun setEnvelopeSustain(v: Float) {
        _envelopeSustain.value = v
        synthEngine.envelopeSustain = v
    }
    fun setEnvelopeRelease(v: Float) {
        _envelopeRelease.value = v
        synthEngine.envelopeRelease = v
    }

    fun setLfoTremoloActive(active: Boolean) {
        _lfoTremoloActive.value = active
        synthEngine.lfoTremoloActive = active
    }
    fun setLfoTremoloRate(rate: Float) {
        _lfoTremoloRate.value = rate
        synthEngine.lfoTremoloRate = rate
    }
    fun setLfoTremoloDepth(depth: Float) {
        _lfoTremoloDepth.value = depth
        synthEngine.lfoTremoloDepth = depth
    }

    fun setLfoVibratoActive(active: Boolean) {
        _lfoVibratoActive.value = active
        synthEngine.lfoVibratoActive = active
    }
    fun setLfoVibratoRate(rate: Float) {
        _lfoVibratoRate.value = rate
        synthEngine.lfoVibratoRate = rate
    }
    fun setLfoVibratoDepth(depth: Float) {
        _lfoVibratoDepth.value = depth
        synthEngine.lfoVibratoDepth = depth
    }

    fun setPitchBend(multiplier: Float) {
        synthEngine.pitchBendMultiplier = multiplier
    }

    // GEMINI FEATURES LOGIC
    fun fetchSongTriviaForSong(song: Song) {
        viewModelScope.launch {
            _isTriviaLoading.value = true
            _songTrivia.value = "Sintonizando frecuencias en el cosmos para traerte secretos..."
            try {
                val triviaText = com.example.sync.GeminiSoundCopilot.fetchSongTrivia(song.title, song.artist)
                _songTrivia.value = triviaText
            } catch (e: Exception) {
                _songTrivia.value = "Disfrutando del espectro retro de ${song.title}."
            } finally {
                _isTriviaLoading.value = false
            }
        }
    }

    fun designAmbientLayer(intentText: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isAmbientLoading.value = true
            try {
                val design = com.example.sync.GeminiSoundCopilot.designAmbientLayer(intentText)
                if (design != null) {
                    setSynthWaveform(design.waveformType)
                    
                    _eqBass.value = design.eqBass
                    synthEngine.eqBass = design.eqBass
                    _eqMid.value = design.eqMid
                    synthEngine.eqMid = design.eqMid
                    _eqTreble.value = design.eqTreble
                    synthEngine.eqTreble = design.eqTreble

                    setEnvelopeAttack(design.attack)
                    setEnvelopeDecay(design.decay)
                    setEnvelopeSustain(design.sustain)
                    setEnvelopeRelease(design.release)

                    setLfoTremoloActive(design.lfoTremoloActive)
                    setLfoTremoloRate(design.lfoTremoloRate)
                    setLfoTremoloDepth(design.lfoTremoloDepth)

                    setLfoVibratoActive(design.lfoVibratoActive)
                    setLfoVibratoRate(design.lfoVibratoRate)
                    setLfoVibratoDepth(design.lfoVibratoDepth)
                    
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e("MusicViewModel", "Failed to design ambient", e)
            } finally {
                _isAmbientLoading.value = false
            }
        }
    }

    fun generateVibePlaylist(promptText: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isPlaylistLoading.value = true
            _vibeExplanation.value = "Consultando constelaciones musicales..."
            try {
                val response = com.example.sync.GeminiSoundCopilot.generateVibePlaylist(promptText)
                if (response != null && response.songIds.isNotEmpty()) {
                    val candidateSongs = allSongs.value
                    val orderedSongs = response.songIds.mapNotNull { id ->
                        candidateSongs.find { it.id == id }
                    }
                    if (orderedSongs.isNotEmpty()) {
                        _playbackQueueState.value = orderedSongs
                        queueIndex = 0
                        _currentSong.value = orderedSongs.first()
                        _isPlaying.value = true
                        _currentPlayProgress.value = 0
                        
                        setSynthWaveform(response.waveformType)
                        
                        val matchedPreset = EqPreset.values().find {
                            it.displayName.equals(response.eqPresetName, ignoreCase = true)
                        } ?: EqPreset.FLAT
                        selectEqPreset(matchedPreset)
                        
                        _vibeExplanation.value = response.explanation
                        onSuccess()
                    } else {
                        _vibeExplanation.value = "No se pudieron acomodar las pistas locales. Intenta con otro vibe."
                    }
                } else {
                    _vibeExplanation.value = "Vibe indescifrable... Intenta con palabras clave como 'chill', 'tecnológica' o 'retro'."
                }
            } catch (e: Exception) {
                Log.e("MusicViewModel", "Vibe playlist error", e)
                _vibeExplanation.value = "Señal de Gemini perdida en la órbita."
            } finally {
                _isPlaylistLoading.value = false
            }
        }
    }

    fun setWifiAutoSyncEnabled(enabled: Boolean) {
        _wifiAutoSyncEnabled.value = enabled
        if (enabled) {
            triggerAutoSyncIfOnWifi()
        }
    }

    private val syncManager = com.example.sync.PlaylistSyncManager(database.musicDao())

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun setTheme(isDark: Boolean) {
        _isDarkTheme.value = isDark
    }

    fun setShuffleEnabled(enabled: Boolean) {
        _isShuffleEnabled.value = enabled
    }

    fun setSmartShuffleMode(mode: SmartShuffleMode) {
        _smartShuffleMode.value = mode
    }

    fun setRepeatEnabled(enabled: Boolean) {
        _isRepeatEnabled.value = enabled
    }

    // Download Animations (Simulated downloads)
    private val _downloadingSongs = MutableStateFlow<Map<String, Float>>(emptyMap())
    val downloadingSongs: StateFlow<Map<String, Float>> = _downloadingSongs.asStateFlow()

    // Queue State
    private val _playbackQueueState = MutableStateFlow<List<Song>>(emptyList())
    val playbackQueueState: StateFlow<List<Song>> = _playbackQueueState.asStateFlow()

    private var playbackQueue: List<Song>
        get() = _playbackQueueState.value
        set(value) {
            _playbackQueueState.value = value
        }

    private var queueIndex = -1

    fun playSongFromQueue(song: Song) {
        val index = playbackQueue.indexOfFirst { it.id == song.id }
        if (index != -1) {
            queueIndex = index
            _currentSong.value = song
            _currentPlayProgress.value = 0
            _isPlaying.value = true
        }
    }

    // Simulated Player Timer Job
    private var playerTimerJob: Job? = null

    init {
        val randomParts = "abcdefghijklmnopqrstuvwxyz0123456789"
        val generatedCode = "sync-" + (1..6).map { randomParts.random() }.joinToString("")
        _syncCode.value = generatedCode

        // Observe playing status and start/stop synth engine and progress counter
        viewModelScope.launch {
            combine(_isPlaying, _currentSong) { playing, song ->
                Pair(playing, song)
            }.collect { (playing, song) ->
                if (playing && song != null) {
                    synthEngine.crossfadeDurationMs = _crossfadeSecs.value.toLong() * 1000L
                    synthEngine.startPlaying(song.frequencySeed)
                    startTimer()
                } else {
                    synthEngine.stopPlaying()
                    stopTimer()
                }
            }
        }

        // Register Network Callbacks to listen for Wi-Fi changes
        registerNetworkCallback()

        // Fetch trivia whenever current active song changes via Gemini
        viewModelScope.launch {
            _currentSong.collect { song ->
                if (song != null) {
                    fetchSongTriviaForSong(song)
                } else {
                    _songTrivia.value = ""
                }
            }
        }

        // Observe preferences to trigger auto sync when on Wi-Fi
        viewModelScope.launch {
            val flows: List<Flow<Any>> = listOf(
                _isShuffleEnabled,
                _isRepeatEnabled,
                _synthMuted,
                _synthVolume,
                _synthWaveformType,
                _waveformStyleStr,
                _eqBass,
                _eqMid,
                _eqTreble,
                _isDarkTheme,
                _wifiAutoSyncEnabled,
                _crossfadeSecs
            )
            flows.merge().collect {
                if (_wifiAutoSyncEnabled.value) {
                    triggerAutoSyncDebounced()
                }
            }
        }

        viewModelScope.launch {
            allPlaylists.collect {
                if (_wifiAutoSyncEnabled.value) {
                    triggerAutoSyncDebounced()
                }
            }
        }
    }

    private fun startTimer() {
        playerTimerJob?.cancel()
        playerTimerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                val song = _currentSong.value ?: continue
                val currentProgress = _currentPlayProgress.value
                val crossfadeLimit = song.durationSecs - _crossfadeSecs.value
                if (currentProgress < song.durationSecs && (_crossfadeSecs.value <= 0 || currentProgress < crossfadeLimit)) {
                    _currentPlayProgress.value = currentProgress + 1
                } else {
                    if (_isRepeatEnabled.value) {
                        _currentPlayProgress.value = 0
                    } else {
                        nextSong()
                    }
                }
            }
        }
    }

    private fun stopTimer() {
        playerTimerJob?.cancel()
        playerTimerJob = null
    }

    fun toggleOfflineMode() {
        _isOfflineMode.value = !_isOfflineMode.value
        // If current song is not downloaded and we enter offline mode, stop playing
        if (_isOfflineMode.value) {
            val song = _currentSong.value
            if (song != null && !song.isDownloaded) {
                _isPlaying.value = false
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun insertRecentSearch(query: String) {
        viewModelScope.launch {
            repository.insertRecentSearch(query)
        }
    }

    fun deleteRecentSearch(query: String) {
        viewModelScope.launch {
            repository.deleteRecentSearch(query)
        }
    }

    fun clearRecentSearches() {
        viewModelScope.launch {
            repository.clearRecentSearches()
        }
    }

    fun playSongNow(song: Song, contextQueue: List<Song>) {
        // Ensure we can play this song in offline mode
        if (_isOfflineMode.value && !song.isDownloaded) {
            // Cannot play online songs in offline mode
            return
        }

        // If search query is non-empty, save it to recent searches on song selection
        val curQuery = _searchQuery.value
        if (curQuery.isNotBlank()) {
            insertRecentSearch(curQuery)
        }

        playbackQueue = contextQueue
        queueIndex = playbackQueue.indexOfFirst { it.id == song.id }
        if (queueIndex == -1) {
            playbackQueue = listOf(song)
            queueIndex = 0
        }

        _currentSong.value = song
        _currentPlayProgress.value = 0
        _isPlaying.value = true
    }

    fun togglePlayPause() {
        val song = _currentSong.value
        if (song != null) {
            // Check offline mode safety
            if (_isOfflineMode.value && !song.isDownloaded && !_isPlaying.value) {
                return
            }
            _isPlaying.value = !_isPlaying.value
        } else {
            // Play first available song if none is selected
            viewModelScope.launch {
                val songs = allSongs.value
                val availableSongs = if (_isOfflineMode.value) songs.filter { it.isDownloaded } else songs
                if (availableSongs.isNotEmpty()) {
                    playSongNow(availableSongs.first(), availableSongs)
                }
            }
        }
    }

    private fun getShuffledIndex(current: Song): Int {
        if (playbackQueue.size <= 1) return queueIndex
        return when (_smartShuffleMode.value) {
            SmartShuffleMode.STANDARD -> {
                val idxs = (playbackQueue.indices).filter { it != queueIndex }
                if (idxs.isNotEmpty()) idxs.random() else (playbackQueue.indices).random()
            }
            SmartShuffleMode.ARTIST -> {
                val artistSub = current.artist.lowercase()
                val relatedGroup = when {
                    "lumina" in artistSub || "rider" in artistSub || "bytesized" in artistSub || "chiptune" in artistSub || "sub" in artistSub -> {
                        listOf("Lumina Key", "Sunset Rider", "ByteSized", "Chiptune Kid", "The Sub")
                    }
                    "clara" in artistSub || "cosmic" in artistSub || "dawn" in artistSub || "woods" in artistSub -> {
                        listOf("Clara Woods", "Cosmic Dawn")
                    }
                    else -> listOf(current.artist)
                }
                val candidates = playbackQueue.filter { it.id != current.id && it.artist in relatedGroup }
                if (candidates.isNotEmpty()) {
                    val selected = candidates.random()
                    playbackQueue.indexOfFirst { it.id == selected.id }.coerceAtLeast(0)
                } else {
                    // Fallback to same genre
                    val genreCandidates = playbackQueue.filter { it.id != current.id && it.genre.equals(current.genre, ignoreCase = true) }
                    if (genreCandidates.isNotEmpty()) {
                        val selected = genreCandidates.random()
                        playbackQueue.indexOfFirst { it.id == selected.id }.coerceAtLeast(0)
                    } else {
                        val idxs = (playbackQueue.indices).filter { it != queueIndex }
                        if (idxs.isNotEmpty()) idxs.random() else (playbackQueue.indices).random()
                    }
                }
            }
            SmartShuffleMode.MOOD -> {
                val currentEnergy = SongMetadataHelper.getEnergyLevel(current.id)
                val candidates = playbackQueue.filter {
                    it.id != current.id &&
                    java.lang.Math.abs(SongMetadataHelper.getEnergyLevel(it.id) - currentEnergy) <= 0.35f
                }
                if (candidates.isNotEmpty()) {
                    val selected = candidates.random()
                    playbackQueue.indexOfFirst { it.id == selected.id }.coerceAtLeast(0)
                } else {
                    val idxs = (playbackQueue.indices).filter { it != queueIndex }
                    if (idxs.isNotEmpty()) idxs.random() else (playbackQueue.indices).random()
                }
            }
        }
    }

    fun nextSong() {
        if (playbackQueue.isEmpty()) return

        var nextIndex = queueIndex
        if (_isShuffleEnabled.value) {
            val current = _currentSong.value
            nextIndex = if (current != null) getShuffledIndex(current) else (playbackQueue.indices).random()
        } else {
            nextIndex = (queueIndex + 1) % playbackQueue.size
        }

        val nextSong = playbackQueue[nextIndex]
        // Check if playable in offline mode
        if (_isOfflineMode.value && !nextSong.isDownloaded) {
            // Try to find the next downloadable song in queue
            var found = false
            for (i in 1..playbackQueue.size) {
                val idx = (nextIndex + i) % playbackQueue.size
                if (playbackQueue[idx].isDownloaded) {
                    nextIndex = idx
                    found = true
                    break
                }
            }
            if (!found) {
                _isPlaying.value = false
                return
            }
        }

        queueIndex = nextIndex
        _currentSong.value = playbackQueue[queueIndex]
        _currentPlayProgress.value = 0
        _isPlaying.value = true
    }

    fun prevSong() {
        if (playbackQueue.isEmpty()) return

        var prevIndex = queueIndex
        if (_isShuffleEnabled.value) {
            val current = _currentSong.value
            prevIndex = if (current != null) getShuffledIndex(current) else (playbackQueue.indices).random()
        } else {
            prevIndex = queueIndex - 1
            if (prevIndex < 0) {
                prevIndex = playbackQueue.size - 1
            }
        }

        val prevSong = playbackQueue[prevIndex]
        // Check offline safety
        if (_isOfflineMode.value && !prevSong.isDownloaded) {
            var found = false
            for (i in 1..playbackQueue.size) {
                var idx = prevIndex - i
                if (idx < 0) idx += playbackQueue.size
                if (playbackQueue[idx].isDownloaded) {
                    prevIndex = idx
                    found = true
                    break
                }
            }
            if (!found) {
                _isPlaying.value = false
                return
            }
        }

        queueIndex = prevIndex
        _currentSong.value = playbackQueue[queueIndex]
        _currentPlayProgress.value = 0
        _isPlaying.value = true
    }

    fun seekTo(seconds: Int) {
        val song = _currentSong.value ?: return
        _currentPlayProgress.value = seconds.coerceIn(0, song.durationSecs)
    }

    fun toggleShuffle() {
        _isShuffleEnabled.value = !_isShuffleEnabled.value
    }

    fun toggleRepeat() {
        _isRepeatEnabled.value = !_isRepeatEnabled.value
    }

    fun toggleSynthMute() {
        _synthMuted.value = !_synthMuted.value
        synthEngine.setMute(_synthMuted.value)
    }

    fun setSynthVolume(vol: Float) {
        _synthVolume.value = vol
        synthEngine.volumeAttenuation = vol
    }

    fun setSynthWaveform(type: String) {
        _synthWaveformType.value = type
        synthEngine.waveformType = type
    }

    fun setWaveformStyle(style: String) {
        _waveformStyleStr.value = style
    }

    fun setCrossfadeSecs(secs: Int) {
        val clamped = secs.coerceIn(0, 10)
        _crossfadeSecs.value = clamped
        synthEngine.crossfadeDurationMs = clamped.toLong() * 1000L
    }

    // Configures the Sleep Timer with fade-out
    fun setSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        _sleepTimerMinutesLeft.value = minutes
        if (minutes <= 0) {
            // Restore regular volume if we cancelled or finished
            if (synthVolume.value < preFadeVolume) {
                setSynthVolume(preFadeVolume)
            }
            return
        }

        // Lock pre-fade volume to current value
        preFadeVolume = _synthVolume.value

        sleepTimerJob = viewModelScope.launch {
            var secondsRemaining = minutes * 60
            val totalSeconds = secondsRemaining
            while (secondsRemaining > 0) {
                delay(1000)
                secondsRemaining--
                // Convert remainder to minutes for UI text
                _sleepTimerMinutesLeft.value = (secondsRemaining + 59) / 60

                // Progressive Volume Fade over the last 30 seconds of the timer
                val fadeDuration = 30.coerceAtMost(totalSeconds / 2)
                if (secondsRemaining <= fadeDuration) {
                    val scaleFactor = secondsRemaining.toFloat() / fadeDuration.toFloat()
                    val fadedVol = preFadeVolume * scaleFactor
                    synthEngine.volumeAttenuation = fadedVol.coerceIn(0f, 1f)
                    // Sync inside ViewModel flow silently
                    _synthVolume.value = fadedVol.coerceIn(0f, 1f)
                }
            }

            // Sleep Timer reached zero! Turn off playing peacefully
            _isPlaying.value = false
            _sleepTimerMinutesLeft.value = 0
            
            // Slowly silence engine completely
            synthEngine.volumeAttenuation = 0f
            _synthVolume.value = 0f
            delay(1000)
            
            // Recover regular volume profile for next usage
            synthEngine.volumeAttenuation = preFadeVolume
            _synthVolume.value = preFadeVolume
        }
    }

    private fun updatePresetMatch() {
        val b = _eqBass.value
        val m = _eqMid.value
        val t = _eqTreble.value
        val match = EqPreset.values().find {
            it != EqPreset.CUSTOM &&
            kotlin.math.abs(it.bass - b) < 0.01f &&
            kotlin.math.abs(it.mid - m) < 0.01f &&
            kotlin.math.abs(it.treble - t) < 0.01f
        }
        _selectedEqPreset.value = match ?: EqPreset.CUSTOM
    }

    fun selectEqPreset(preset: EqPreset) {
        if (preset != EqPreset.CUSTOM) {
            val b = preset.bass.coerceIn(0.2f, 2.0f)
            val m = preset.mid.coerceIn(0.2f, 2.0f)
            val t = preset.treble.coerceIn(0.2f, 2.0f)

            _eqBass.value = b
            synthEngine.eqBass = b

            _eqMid.value = m
            synthEngine.eqMid = m

            _eqTreble.value = t
            synthEngine.eqTreble = t

            _selectedEqPreset.value = preset
        }
    }

    // EQ Bands Setters
    fun setEqBass(value: Float) {
        val clamped = value.coerceIn(0.2f, 2.0f)
        _eqBass.value = clamped
        synthEngine.eqBass = clamped
        updatePresetMatch()
    }

    fun setEqMid(value: Float) {
        val clamped = value.coerceIn(0.2f, 2.0f)
        _eqMid.value = clamped
        synthEngine.eqMid = clamped
        updatePresetMatch()
    }

    fun setEqTreble(value: Float) {
        val clamped = value.coerceIn(0.2f, 2.0f)
        _eqTreble.value = clamped
        synthEngine.eqTreble = clamped
        updatePresetMatch()
    }

    fun setShowDailyRecommended(show: Boolean) {
        _showDailyRecommended.value = show
    }

    fun addNewSongs(songs: List<Song>) {
        viewModelScope.launch {
            repository.insertSongs(songs)
        }
    }

    // Playlists Operations
    fun createPlaylist(name: String, description: String) {
        viewModelScope.launch {
            repository.createPlaylist(name, description)
        }
    }

    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            repository.deletePlaylist(playlist)
        }
    }

    fun addSongToPlaylist(playlistId: Long, songId: String) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlistId, songId)
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: String) {
        viewModelScope.launch {
            repository.removeSongFromPlaylist(playlistId, songId)
        }
    }

    fun toggleFavorite(songId: String) {
        viewModelScope.launch {
            val playlistsSnapshot = database.musicDao().getPlaylistsWithSongsSnapshot()
            var likedPlaylist = playlistsSnapshot.find { it.playlist.name.equals("Liked", ignoreCase = true) }
            val playlistId = if (likedPlaylist == null) {
                database.musicDao().insertPlaylist(Playlist(name = "Liked", description = "Canciones favoritas sincronizadas"))
            } else {
                likedPlaylist.playlist.id
            }

            likedPlaylist = database.musicDao().getPlaylistsWithSongsSnapshot().find { it.playlist.id == playlistId }
            val alreadyLiked = likedPlaylist?.songs?.any { it.id == songId } ?: false

            if (alreadyLiked) {
                database.musicDao().deletePlaylistSongCrossRef(playlistId, songId)
            } else {
                database.musicDao().insertPlaylistSongCrossRef(PlaylistSongCrossRef(playlistId, songId))
            }

            if (_wifiAutoSyncEnabled.value) {
                triggerAutoSyncDebounced()
            }
        }
    }

    // Offline download simulation
    fun triggerSongDownload(song: Song) {
        if (song.isDownloaded) {
            // Already downloaded, remove download (offline delete)
            viewModelScope.launch {
                repository.toggleDownload(song.id, false)
                // If song was playing in offline mode and gets deleted, stop playing or update
                val current = _currentSong.value
                if (current?.id == song.id) {
                    // Update track model field locally
                    _currentSong.value = current.copy(isDownloaded = false)
                    if (_isOfflineMode.value) {
                        _isPlaying.value = false
                    }
                }
            }
        } else {
            // Simulate dynamic download progression
            if (_downloadingSongs.value.containsKey(song.id)) return // Already downloading
            
            viewModelScope.launch {
                var progress = 0.0f
                while (progress < 1.0f) {
                    delay(300)
                    progress += 0.2f
                    _downloadingSongs.value = _downloadingSongs.value.toMutableMap().apply {
                        put(song.id, progress.coerceAtMost(1.0f))
                    }
                }
                _downloadingSongs.value = _downloadingSongs.value.toMutableMap().apply {
                    remove(song.id)
                }
                repository.toggleDownload(song.id, true)
                
                // Update active song status if it's the one we downloaded
                val current = _currentSong.value
                if (current?.id == song.id) {
                    _currentSong.value = current.copy(isDownloaded = true)
                }
            }
        }
    }

    fun setSyncProjectId(value: String) { _syncProjectId.value = value }
    fun setSyncCode(value: String) { _syncCode.value = value }
    fun setSyncApiKey(value: String) { _syncApiKey.value = value }

    fun triggerPlaylistSync() {
        if (_isSyncing.value) return
        _isSyncing.value = true
        _syncStatus.value = "Conectando con Firestore..."

        val localPrefs = com.example.sync.PlaybackPreferencesValue(
            isShuffleEnabled = _isShuffleEnabled.value,
            isRepeatEnabled = _isRepeatEnabled.value,
            synthVolume = _synthVolume.value,
            synthWaveformType = _synthWaveformType.value,
            waveformStyleStr = _waveformStyleStr.value,
            eqBass = _eqBass.value,
            eqMid = _eqMid.value,
            eqTreble = _eqTreble.value,
            isDarkTheme = _isDarkTheme.value,
            crossfadeSecs = _crossfadeSecs.value
        )

        viewModelScope.launch {
            try {
                val result = syncManager.synchronize(
                    projectId = _syncProjectId.value,
                    syncCode =_syncCode.value,
                    apiKey = if (_syncApiKey.value.isEmpty()) null else _syncApiKey.value,
                    localPrefs = localPrefs
                )
                when (result) {
                    is com.example.sync.SyncResult.Success -> {
                        _syncStatus.value = "Sincronizado con éxito. Nuevas: ${result.playlistsAdded} listas, ${result.songsAdded} canciones. Total: ${result.totalPlaylists} listas."
                        
                        // Apply remote preferences if they exist
                        result.remotePrefs?.let { remote ->
                            applyRemotePreferences(remote)
                        }
                    }
                    is com.example.sync.SyncResult.Error -> {
                        _syncStatus.value = "Error al sincronizar: ${result.message}"
                    }
                }
            } catch (e: Exception) {
                _syncStatus.value = "Error inesperado al conectar: ${e.localizedMessage}"
            } finally {
                _isSyncing.value = false
            }
        }
    }

    private fun applyRemotePreferences(remote: com.example.sync.PlaybackPreferencesValue) {
        _isShuffleEnabled.value = remote.isShuffleEnabled
        _isRepeatEnabled.value = remote.isRepeatEnabled
        setSynthVolume(remote.synthVolume)
        setSynthWaveform(remote.synthWaveformType)
        setWaveformStyle(remote.waveformStyleStr)
        setEqBass(remote.eqBass)
        setEqMid(remote.eqMid)
        setEqTreble(remote.eqTreble)
        setTheme(remote.isDarkTheme)
        setCrossfadeSecs(remote.crossfadeSecs)
    }

    private var networkCallback: android.net.ConnectivityManager.NetworkCallback? = null

    private fun registerNetworkCallback() {
        val connectivityManager = getApplication<Application>()
            .getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
        if (connectivityManager != null) {
            val request = android.net.NetworkRequest.Builder()
                .addTransportType(android.net.NetworkCapabilities.TRANSPORT_WIFI)
                .build()
            
            val callback = object : android.net.ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: android.net.Network) {
                    if (_wifiAutoSyncEnabled.value) {
                        triggerAutoSyncIfOnWifi()
                    }
                }
            }
            networkCallback = callback
            try {
                connectivityManager.registerNetworkCallback(request, callback)
            } catch (e: Exception) {
                android.util.Log.e("MusicViewModel", "Failed to register network callback", e)
            }
        }
    }

    private fun unregisterNetworkCallback() {
        val connectivityManager = getApplication<Application>()
            .getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
        networkCallback?.let {
            try {
                connectivityManager?.unregisterNetworkCallback(it)
            } catch (e: Exception) {
                android.util.Log.e("MusicViewModel", "Failed to unregister network callback", e)
            }
        }
    }

    private var debouncedSyncJob: Job? = null

    fun triggerAutoSyncDebounced() {
        if (!_wifiAutoSyncEnabled.value) return
        if (_isSyncing.value) return
        val context = getApplication<Application>()
        if (!isWifiConnected(context)) return

        debouncedSyncJob?.cancel()
        debouncedSyncJob = viewModelScope.launch {
            delay(3000) // 3 seconds debounce
            if (!_isSyncing.value && _wifiAutoSyncEnabled.value) {
                triggerPlaylistSync()
            }
        }
    }

    fun triggerAutoSyncIfOnWifi() {
        if (!_wifiAutoSyncEnabled.value) return
        if (_isSyncing.value) return
        val context = getApplication<Application>()
        if (isWifiConnected(context)) {
            triggerPlaylistSync()
        }
    }

    private fun isWifiConnected(context: android.content.Context): Boolean {
        val cm = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager ?: return false
        val activeNetwork = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI)
    }

    fun getRealtimeWaveform(): FloatArray {
        return synthEngine.latestWaveform
    }

    override fun onCleared() {
        super.onCleared()
        unregisterNetworkCallback()
        synthEngine.release()
    }
}
