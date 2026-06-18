package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Song::class, Playlist::class, PlaylistSongCrossRef::class, RecentSearch::class], version = 2, exportSchema = false)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun musicDao(): MusicDao

    companion object {
        @Volatile
        private var INSTANCE: MusicDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): MusicDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MusicDatabase::class.java,
                    "music_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(MusicDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class MusicDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.musicDao())
                }
            }
        }

        suspend fun populateDatabase(musicDao: MusicDao) {
            val defaultSongs = listOf(
                Song("neon_horizon", "Neon Horizon", "Lumina Key", "Synthwave Chronicles", 184, isDownloaded = true, "Synthwave", 440.0f),
                Song("midnight_drive", "Midnight Drive", "Sunset Rider", "Outrun Days", 220, isDownloaded = false, "Chiptune", 523.25f),
                Song("digital_dr", "Digital Dreams", "ByteSized", "Glitch Theory", 195, isDownloaded = true, "Electronic", 587.33f),
                Song("acoustic_rn", "Acoustic Rain", "Clara Woods", "Wooden Stories", 162, isDownloaded = false, "Acoustic", 349.23f),
                Song("solar_winds", "Solar Winds", "Cosmic Dawn", "Orion Nebula", 285, isDownloaded = false, "Ambient", 293.66f),
                Song("pixel_hearts", "Pixel Hearts", "Chiptune Kid", "8-Bit Arcade", 138, isDownloaded = true, "Retro", 659.25f),
                Song("deep_bass", "Deep Bassline", "The Sub", "Underground Sub", 240, isDownloaded = false, "Techno", 220.0f),
                Song("golden_sky", "Golden Sky", "Aura Volver", "Daylight", 205, isDownloaded = false, "Pop", 392.00f)
            )
            musicDao.insertSongs(defaultSongs)

            // Let's also create two default playlists
            val p1Id = musicDao.insertPlaylist(Playlist(1, "Mis Favoritas", "Canciones que escucho todo el día"))
            val p2Id = musicDao.insertPlaylist(Playlist(2, "Modo Chill", "Música relajante para concentración"))

            // Add some songs to default playlists
            musicDao.insertPlaylistSongCrossRef(PlaylistSongCrossRef(p1Id, "neon_horizon"))
            musicDao.insertPlaylistSongCrossRef(PlaylistSongCrossRef(p1Id, "digital_dr"))
            musicDao.insertPlaylistSongCrossRef(PlaylistSongCrossRef(p2Id, "solar_winds"))
            musicDao.insertPlaylistSongCrossRef(PlaylistSongCrossRef(p2Id, "acoustic_rn"))
        }
    }
}
