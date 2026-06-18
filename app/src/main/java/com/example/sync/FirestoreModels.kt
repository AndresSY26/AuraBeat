package com.example.sync

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FirestoreDocument(
    val fields: Map<String, FirestoreValue>? = null
)

@JsonClass(generateAdapter = true)
data class FirestoreValue(
    val stringValue: String? = null,
    val arrayValue: FirestoreArrayValue? = null,
    val mapValue: FirestoreMapValue? = null
)

@JsonClass(generateAdapter = true)
data class FirestoreArrayValue(
    val values: List<FirestoreValue>? = null
)

@JsonClass(generateAdapter = true)
data class FirestoreMapValue(
    val fields: Map<String, FirestoreValue>? = null
)

data class RemotePlaylist(
    val name: String,
    val description: String,
    val songIds: List<String>
)

data class PlaybackPreferencesValue(
    val isShuffleEnabled: Boolean,
    val isRepeatEnabled: Boolean,
    val synthVolume: Float,
    val synthWaveformType: String,
    val waveformStyleStr: String,
    val eqBass: Float,
    val eqMid: Float,
    val eqTreble: Float,
    val isDarkTheme: Boolean,
    val crossfadeSecs: Int = 0
)

object FirestoreTranslation {
    fun convertPlaylistsToFirestoreDoc(
        localPlaylists: List<com.example.data.PlaylistWithSongs>,
        prefs: PlaybackPreferencesValue? = null
    ): FirestoreDocument {
        val plistValues = localPlaylists.map { playlistWithSongs ->
            val nameVal = FirestoreValue(stringValue = playlistWithSongs.playlist.name)
            val descVal = FirestoreValue(stringValue = playlistWithSongs.playlist.description)
            
            val songVals = playlistWithSongs.songs.map { song ->
                FirestoreValue(stringValue = song.id)
            }
            val songsVal = FirestoreValue(arrayValue = FirestoreArrayValue(songVals))
            
            val mapFields = mapOf(
                "name" to nameVal,
                "description" to descVal,
                "songs" to songsVal
            )
            FirestoreValue(mapValue = FirestoreMapValue(mapFields))
        }
        
        val playlistsField = FirestoreValue(arrayValue = FirestoreArrayValue(plistValues))
        val fields = mutableMapOf<String, FirestoreValue>(
            "playlists" to playlistsField
        )

        if (prefs != null) {
            val prefFields = mapOf(
                "isShuffleEnabled" to FirestoreValue(stringValue = prefs.isShuffleEnabled.toString()),
                "isRepeatEnabled" to FirestoreValue(stringValue = prefs.isRepeatEnabled.toString()),
                "synthVolume" to FirestoreValue(stringValue = prefs.synthVolume.toString()),
                "synthWaveformType" to FirestoreValue(stringValue = prefs.synthWaveformType),
                "waveformStyleStr" to FirestoreValue(stringValue = prefs.waveformStyleStr),
                "eqBass" to FirestoreValue(stringValue = prefs.eqBass.toString()),
                "eqMid" to FirestoreValue(stringValue = prefs.eqMid.toString()),
                "eqTreble" to FirestoreValue(stringValue = prefs.eqTreble.toString()),
                "isDarkTheme" to FirestoreValue(stringValue = prefs.isDarkTheme.toString()),
                "crossfadeSecs" to FirestoreValue(stringValue = prefs.crossfadeSecs.toString())
            )
            fields["playbackPreferences"] = FirestoreValue(mapValue = FirestoreMapValue(prefFields))
        }

        return FirestoreDocument(fields = fields)
    }

    fun convertFirestoreDocToPlaylists(doc: FirestoreDocument): List<RemotePlaylist> {
        val playlistsField = doc.fields?.get("playlists") ?: return emptyList()
        val values = playlistsField.arrayValue?.values ?: return emptyList()
        
        return values.mapNotNull { value ->
            val mFields = value.mapValue?.fields ?: return@mapNotNull null
            val name = mFields["name"]?.stringValue ?: "Playlist Sin Nombre"
            val desc = mFields["description"]?.stringValue ?: ""
            val songIds = mFields["songs"]?.arrayValue?.values?.mapNotNull { it.stringValue } ?: emptyList()
            RemotePlaylist(name = name, description = desc, songIds = songIds)
        }
    }

    fun convertFirestoreDocToPreferences(doc: FirestoreDocument): PlaybackPreferencesValue? {
        val prefField = doc.fields?.get("playbackPreferences") ?: return null
        val mFields = prefField.mapValue?.fields ?: return null
        return PlaybackPreferencesValue(
            isShuffleEnabled = mFields["isShuffleEnabled"]?.stringValue?.toBoolean() ?: false,
            isRepeatEnabled = mFields["isRepeatEnabled"]?.stringValue?.toBoolean() ?: false,
            synthVolume = mFields["synthVolume"]?.stringValue?.toFloatOrNull() ?: 0.12f,
            synthWaveformType = mFields["synthWaveformType"]?.stringValue ?: "Sine",
            waveformStyleStr = mFields["waveformStyleStr"]?.stringValue ?: "Classic Neon",
            eqBass = mFields["eqBass"]?.stringValue?.toFloatOrNull() ?: 1.0f,
            eqMid = mFields["eqMid"]?.stringValue?.toFloatOrNull() ?: 1.0f,
            eqTreble = mFields["eqTreble"]?.stringValue?.toFloatOrNull() ?: 1.0f,
            isDarkTheme = mFields["isDarkTheme"]?.stringValue?.toBoolean() ?: false,
            crossfadeSecs = mFields["crossfadeSecs"]?.stringValue?.toIntOrNull() ?: 0
        )
    }
}
