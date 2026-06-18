package com.example.sync

import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

// --- JSON request representations ---
data class GeminiPart(val text: String)
data class GeminiContent(val parts: List<GeminiPart>)
data class GeminiGenerationConfig(val responseMimeType: String)
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent? = null,
    val generationConfig: GeminiGenerationConfig? = null
)

// --- Response structures parsed via Moshi or manual fallback ---
data class GeminiTriviaResponse(val trivia: String)

data class GeminiSynthParamsResponse(
    val waveformType: String, // "Sine", "Square", "Triangle", "Sawtooth", "White Noise"
    val eqBass: Float, // 0.1 to 2.0
    val eqMid: Float, // 0.1 to 2.0
    val eqTreble: Float, // 0.1 to 2.0
    val attack: Float, // 0.01 to 0.9
    val decay: Float,  // 0.01 to 0.9
    val sustain: Float, // 0.0 to 1.0
    val release: Float, // 0.01 to 0.9
    val lfoTremoloActive: Boolean,
    val lfoTremoloRate: Float, // 0.1 to 10.0
    val lfoTremoloDepth: Float, // 0.0 to 1.0
    val lfoVibratoActive: Boolean,
    val lfoVibratoRate: Float, // 0.1 to 10.0
    val lfoVibratoDepth: Float // 0.0 to 1.0
)

data class GeminiVibePlaylistResponse(
    val songIds: List<String>,
    val explanation: String,
    val waveformType: String,
    val eqPresetName: String // "Plano", "Bass Boost", "Vocal Focus", "Acoustic"
)

object GeminiSoundCopilot {
    private val TAG = "GeminiSoundCopilot"
    private const val MODEL_NAME = "gemini-3.5-flash"
    
    // OkHttpClient with 60s timeout as requested by Gemini API Skill
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private fun getApiKey(): String {
        return com.example.BuildConfig.GEMINI_API_KEY
    }

    /**
     * General helper to query Gemini REST API with JSON response constraints
     */
    private suspend fun queryGemini(prompt: String, systemPrompt: String): String? = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "Gemini API Key is not configured.")
            return@withContext null
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent?key=$apiKey"
        
        // Build JSON manually to be extremely robust and lightweight
        val requestJson = """
            {
              "contents": [
                {
                  "parts": [
                    { "text": ${escapeString(prompt)} }
                  ]
                }
              ],
              "systemInstruction": {
                "parts": [
                  { "text": ${escapeString(systemPrompt)} }
                ]
              },
              "generationConfig": {
                "responseMimeType": "application/json"
              }
            }
        """.trimIndent()

        try {
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = requestJson.toRequestBody(mediaType)
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    Log.e(TAG, "Query failed: ${response.code} -> $errBody")
                    return@withContext null
                }

                val responseBody = response.body?.string() ?: return@withContext null
                
                // Parse the response candidates
                // Form: { "candidates": [ { "content": { "parts": [ { "text": "..." } ] } } ] }
                parseTextFromCandidates(responseBody)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network exception querying Gemini", e)
            null
        }
    }

    private fun escapeString(s: String): String {
        return Moshi.Builder().build().adapter(String::class.java).toJson(s)
    }

    private fun parseTextFromCandidates(json: String): String? {
        try {
            val root = moshi.adapter(Map::class.java).fromJson(json) as? Map<*, *> ?: return null
            val candidates = root["candidates"] as? List<*> ?: return null
            val firstCandidate = candidates.firstOrNull() as? Map<*, *> ?: return null
            val content = firstCandidate["content"] as? Map<*, *> ?: return null
            val parts = content["parts"] as? List<*> ?: return null
            val firstPart = parts.firstOrNull() as? Map<*, *> ?: return null
            return firstPart["text"] as? String
        } catch (e: Exception) {
            Log.e(TAG, "Failed parsing candidates JSON: $e")
            return null
        }
    }

    // --- High Level Features ---

    /**
     * Gemini: Automated Song Trivia
     */
    suspend fun fetchSongTrivia(songTitle: String, artistName: String): String = withContext(Dispatchers.IO) {
        val systemPrompt = "Eres un co-piloto e historiador de música premium. Genera un fun fact o trivia histórica extremadamente interesante, breve, inspiradora y poética sobre la canción indicada o sobre su género/artista en un formato JSON simple: {\"trivia\": \"texto aquí\"}."
        val prompt = "Canción: '$songTitle' del artista '$artistName'."

        val rawJson = queryGemini(prompt, systemPrompt)
        if (rawJson != null) {
            try {
                val adapter = moshi.adapter(GeminiTriviaResponse::class.java)
                val response = adapter.fromJson(rawJson)
                response?.trivia ?: "¡Qué pista tan increíble! Explora las texturas de sonido de $songTitle."
            } catch (e: Exception) {
                Log.e(TAG, "JSON parsing error for trivia: $e")
                extractTriviaFallback(rawJson, songTitle)
            }
        } else {
            "Sintonizando ondas de datos espaciales... Explora los sintetizadores en tiempo real de $songTitle."
        }
    }

    private fun extractTriviaFallback(raw: String, title: String): String {
        // Fallback string extraction if structure was polluted
        val lookup = "\"trivia\""
        if (raw.contains(lookup)) {
            val startIdx = raw.indexOf(lookup) + lookup.length
            val sub = raw.substring(startIdx)
            val firstQuote = sub.indexOf("\"")
            if (firstQuote != -1) {
                val endQuote = sub.indexOf("\"", firstQuote + 1)
                if (endQuote != -1) {
                    return sub.substring(firstQuote + 1, endQuote)
                }
            }
        }
        return "Disfrutando de la atmósfera retro-futurista de $title."
    }

    /**
     * Gemini: Designing custom Ambient synthesis layers (Synth Param Setter)
     */
    suspend fun designAmbientLayer(intent: String): GeminiSynthParamsResponse? = withContext(Dispatchers.IO) {
        val systemPrompt = """
            Eres un ingeniero analógico de sintetizadores. Vas a diseñar los parámetros ideales de un sintetizador basado en la intención de meditación, estudio o descanso del usuario.
            Debes responder estrictamente con un objeto JSON que coincida exactamente con este formato:
            {
              "waveformType": "Sine", // Puede ser "Sine", "Square", "Triangle", "Sawtooth", "White Noise"
              "eqBass": 1.2,          // Flotante entre 0.1 y 2.0 (potenciación de frecuencias bajas)
              "eqMid": 1.0,           // Flotante entre 0.1 y 2.0 (medios)
              "eqTreble": 0.8,        // Flotante entre 0.1 y 2.0 (agudos)
              "attack": 0.3,          // Flotante de ADSR entre 0.01 y 0.9 (tiempo de ataque)
              "decay": 0.2,           // Flotante de ADSR entre 0.01 y 0.9 (decaimiento)
              "sustain": 0.6,         // Flotante de ADSR entre 0.0 y 1.0 (sostenido)
              "release": 0.4,         // Flotante de ADSR entre 0.01 y 0.9 (liberación)
              "lfoTremoloActive": true,   // Si debe estar activado
              "lfoTremoloRate": 4.5,      // Tasa en Hz entre 0.1 y 10.0
              "lfoTremoloDepth": 0.3,    // Profundidad entre 0.0 y 1.0
              "lfoVibratoActive": false,  // Si debe estar activado
              "lfoVibratoRate": 5.0,      // Tasa en Hz entre 0.1 y 10.0
              "lfoVibratoDepth": 0.1      // Profundidad entre 0.0 y 1.0
            }
            El diseño debe sonar orgánico. Por ejemplo, para dormir se recomiendan Sine o White Noise con bajos potentes, ataque lento y LFO suave (tremolo lento). Para enfoque, algo de Triangle con agudos claros. Sé creativo y exacto en los tipos de datos.
        """.trimIndent()

        val rawJson = queryGemini(intent, systemPrompt) ?: return@withContext null
        try {
            val adapter = moshi.adapter(GeminiSynthParamsResponse::class.java)
            adapter.fromJson(rawJson)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse designed synth params: $e", e)
            null
        }
    }

    /**
     * Gemini: Generate atmospheric Playlists based on Vibe / Prompt
     */
    suspend fun generateVibePlaylist(prompt: String): GeminiVibePlaylistResponse? = withContext(Dispatchers.IO) {
        val systemPrompt = """
            Eres un programador y selector del reproductor de sonido AuraBeat. Dados los siguientes IDs de canciones que existen en nuestro catálogo local:
            - 'neon_horizon' (Lumina Key - Synthwave)
            - 'midnight_drive' (Sunset Rider - Chiptune)
            - 'digital_dr' (ByteSized - Electronic)
            - 'acoustic_rn' (Clara Woods - Acoustic)
            - 'solar_winds' (Cosmic Dawn - Ambient)
            - 'pixel_hearts' (Chiptune Kid - Retro)
            - 'deep_bass' (The Sub - Techno)
            - 'golden_sky' (Aura Volver - Pop)
            
            Debes seleccionar y ordenar de 3 a 6 canciones que mejor coincidan con el ambiente ('vibe') solicitado por el usuario.
            Debes responder estrictamente con un JSON que coincida exactamente con este formato:
            {
              "songIds": ["solar_winds", "acoustic_rn", "golden_sky"], // Lista de IDs ordenados
              "explanation": "Breve explicación lírica de 1 y 2 oraciones de por qué esta selección combina con el ambiente.",
              "waveformType": "Sine", // Onda recomendada para este ambiente: "Sine", "Square", "Triangle", "Sawtooth" o "White Noise"
              "eqPresetName": "Acoustic" // Ecualización recomendada: "Plano", "Bass Boost", "Vocal Focus" o "Acoustic"
            }
        """.trimIndent()

        val rawJson = queryGemini(prompt, systemPrompt) ?: return@withContext null
        try {
            val adapter = moshi.adapter(GeminiVibePlaylistResponse::class.java)
            adapter.fromJson(rawJson)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse vibe playlist response: $e", e)
            null
        }
    }
}
