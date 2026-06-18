package com.example.data

import kotlinx.coroutines.flow.Flow

class MusicRepository(private val musicDao: MusicDao) {
    val allSongs: Flow<List<Song>> = musicDao.getAllSongs()

    val allPlaylists: Flow<List<PlaylistWithSongs>> = musicDao.getPlaylistsWithSongs()

    fun getPlaylistById(playlistId: Long): Flow<PlaylistWithSongs?> =
        musicDao.getPlaylistWithSongsById(playlistId)

    suspend fun createPlaylist(name: String, description: String) {
        val playlist = Playlist(name = name, description = description)
        musicDao.insertPlaylist(playlist)
    }

    suspend fun deletePlaylist(playlist: Playlist) {
        musicDao.deletePlaylist(playlist)
    }

    suspend fun addSongToPlaylist(playlistId: Long, songId: String) {
        musicDao.insertPlaylistSongCrossRef(PlaylistSongCrossRef(playlistId, songId))
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: String) {
        musicDao.deletePlaylistSongCrossRef(playlistId, songId)
    }

    suspend fun insertSongs(songs: List<Song>) {
        musicDao.insertSongs(songs)
    }

    suspend fun toggleDownload(songId: String, download: Boolean) {
        musicDao.updateDownloadStatus(songId, download)
    }

    val recentSearches: Flow<List<RecentSearch>> = musicDao.getRecentSearches(10)

    suspend fun insertRecentSearch(query: String) {
        if (query.isNotBlank()) {
            musicDao.insertRecentSearch(RecentSearch(query.trim(), System.currentTimeMillis()))
        }
    }

    suspend fun deleteRecentSearch(query: String) {
        musicDao.deleteRecentSearch(query)
    }

    suspend fun clearRecentSearches() {
        musicDao.clearRecentSearches()
    }
}
