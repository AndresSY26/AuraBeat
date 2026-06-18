package com.example.sync

import android.util.Log
import com.example.data.MusicDao
import com.example.data.Playlist
import com.example.data.PlaylistSongCrossRef
import com.example.data.PlaylistWithSongs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PlaylistSyncManager(private val musicDao: MusicDao) {

    suspend fun synchronize(
        projectId: String,
        syncCode: String,
        apiKey: String?,
        localPrefs: PlaybackPreferencesValue? = null
    ): SyncResult = withContext(Dispatchers.IO) {
        val sanitizedProject = projectId.trim().ifEmpty { "musicplayer-synth-sync" }
        val sanitizedCode = syncCode.trim().ifEmpty { "default_sync" }
        val key = apiKey?.trim()?.ifEmpty { null }

        try {
            // 1. Get local playlists
            val localPlaylists = musicDao.getPlaylistsWithSongsSnapshot()

            // 2. Fetch remote playlists
            val response = try {
                FirestoreClient.api.getDocument(sanitizedProject, sanitizedCode, key)
            } catch (e: Exception) {
                Log.e("PlaylistSync", "Network call failed", e)
                return@withContext SyncResult.Error("Error de conexión a la red: ${e.localizedMessage}")
            }

            var remotePrefs: PlaybackPreferencesValue? = null
            val remotePlaylists: List<RemotePlaylist> = if (response.isSuccessful) {
                val doc = response.body()
                if (doc != null) {
                    remotePrefs = FirestoreTranslation.convertFirestoreDocToPreferences(doc)
                    FirestoreTranslation.convertFirestoreDocToPlaylists(doc)
                } else {
                    emptyList()
                }
            } else if (response.code() == 404) {
                emptyList()
            } else {
                val errorMsg = response.errorBody()?.string() ?: ""
                Log.e("PlaylistSync", "Firestore responded with code ${response.code()}: $errorMsg")
                val friendlyError = extractCleanErrorMessage(errorMsg, response.code())
                return@withContext SyncResult.Error("Fallo al conectar ($friendlyError). Por favor verifica los ajustes de conexión.")
            }

            // 3. Two-way Merge Logic
            val mergedPlaylistsMap = mutableMapOf<String, MergedPlaylistData>()

            // Add all remote playlists to merged map
            remotePlaylists.forEach { remote ->
                mergedPlaylistsMap[remote.name] = MergedPlaylistData(
                    name = remote.name,
                    description = remote.description,
                    songIds = remote.songIds.distinct().toMutableSet()
                )
            }

            // Merge local playlists in
            localPlaylists.forEach { local ->
                val name = local.playlist.name
                val localSongIds = local.songs.map { it.id }
                
                val existing = mergedPlaylistsMap[name]
                if (existing != null) {
                    existing.songIds.addAll(localSongIds)
                } else {
                    mergedPlaylistsMap[name] = MergedPlaylistData(
                        name = name,
                        description = local.playlist.description,
                        songIds = localSongIds.toMutableSet()
                    )
                }
            }

            // 4. Update local Room database to reflect merged state
            var newPlaylistsAdded = 0
            var songsMerged = 0

            mergedPlaylistsMap.values.forEach { merged ->
                val localMatch = localPlaylists.find { it.playlist.name == merged.name }
                val targetPlaylistId: Long

                if (localMatch == null) {
                    val newPlaylist = Playlist(name = merged.name, description = merged.description)
                    targetPlaylistId = musicDao.insertPlaylist(newPlaylist)
                    newPlaylistsAdded++
                } else {
                    targetPlaylistId = localMatch.playlist.id
                }

                val localSongsSet = localMatch?.songs?.map { it.id }?.toSet() ?: emptySet()
                merged.songIds.forEach { songId ->
                    if (!localSongsSet.contains(songId)) {
                        musicDao.insertPlaylistSongCrossRef(
                            PlaylistSongCrossRef(playlistId = targetPlaylistId, songId = songId)
                        )
                        songsMerged++
                    }
                }
            }

            val finalLocalState = musicDao.getPlaylistsWithSongsSnapshot()

            // 5. Push merged state back to Firestore
            val uploadDoc = FirestoreTranslation.convertPlaylistsToFirestoreDoc(finalLocalState, localPrefs)
            val uploadResponse = try {
                FirestoreClient.api.updateDocument(sanitizedProject, sanitizedCode, uploadDoc, key)
            } catch (e: Exception) {
                Log.e("PlaylistSync", "Upload call failed", e)
                return@withContext SyncResult.Error("Error enviando datos merged a Firestore: ${e.localizedMessage}")
            }

            if (!uploadResponse.isSuccessful) {
                val errorMsg = uploadResponse.errorBody()?.string() ?: ""
                Log.e("PlaylistSync", "Firestore upload responded with ${uploadResponse.code()}: $errorMsg")
                val friendlyError = extractCleanErrorMessage(errorMsg, uploadResponse.code())
                return@withContext SyncResult.Error("Fallo al subir datos merged ($friendlyError)")
            }

            SyncResult.Success(
                playlistsAdded = newPlaylistsAdded,
                songsAdded = songsMerged,
                totalPlaylists = finalLocalState.size,
                remotePrefs = remotePrefs
            )
        } catch (e: Exception) {
            Log.e("PlaylistSync", "Sync failed with exception", e)
            SyncResult.Error("Error inesperado de sincronización: ${e.localizedMessage}")
        }
    }

    private fun extractCleanErrorMessage(rawError: String, defaultCode: Int): String {
        if (rawError.isBlank()) {
            return "Respuesta vacía (Código $defaultCode)"
        }
        try {
            // Check for CONSUMER_INVALID which is common on empty/wrong project ID
            if (rawError.contains("CONSUMER_INVALID", ignoreCase = true)) {
                return "Proyecto inválido. El ID de Proyecto de Firestore o la API Key no es válida."
            }
            // Parse standard JSON error message from google resting surface
            val messageKey = "\"message\":"
            if (rawError.contains(messageKey)) {
                val startIndex = rawError.indexOf(messageKey) + messageKey.length
                val openQuote = rawError.indexOf("\"", startIndex)
                if (openQuote != -1) {
                    val closeQuote = rawError.indexOf("\"", openQuote + 1)
                    if (closeQuote != -1) {
                        val msg = rawError.substring(openQuote + 1, closeQuote)
                        if (msg.isNotBlank()) {
                            if (msg.contains("Permission denied", ignoreCase = true)) {
                                return "Permiso denegado. Verifica las Reglas de Firestore o la API Key."
                            }
                            return msg
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // fallback
        }
        val trimmed = rawError.trim().take(120)
        return if (trimmed.length >= 120) "$trimmed..." else trimmed
    }
}

data class MergedPlaylistData(
    val name: String,
    val description: String,
    val songIds: MutableSet<String>
)

sealed class SyncResult {
    data class Success(
        val playlistsAdded: Int,
        val songsAdded: Int,
        val totalPlaylists: Int,
        val remotePrefs: PlaybackPreferencesValue? = null
    ) : SyncResult()
    data class Error(val message: String) : SyncResult()
}
