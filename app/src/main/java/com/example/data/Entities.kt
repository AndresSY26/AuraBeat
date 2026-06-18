package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Junction
import androidx.room.Relation
import androidx.room.Embedded

@Entity(tableName = "songs")
data class Song(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationSecs: Int,
    val isDownloaded: Boolean = false,
    val genre: String,
    val frequencySeed: Float = 440f // Used by synth engine to play a distinctive tone
)

@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlist_song_cross_ref", primaryKeys = ["playlistId", "songId"])
data class PlaylistSongCrossRef(
    val playlistId: Long,
    val songId: String
)

data class PlaylistWithSongs(
    @Embedded val playlist: Playlist,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            PlaylistSongCrossRef::class,
            parentColumn = "playlistId",
            entityColumn = "songId"
        )
    )
    val songs: List<Song>
)

@Entity(tableName = "recent_searches")
data class RecentSearch(
    @PrimaryKey val query: String,
    val timestamp: Long = System.currentTimeMillis()
)

