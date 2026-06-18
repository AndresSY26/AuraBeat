package com.example.player

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.*
import kotlin.math.sin

/**
 * A client-side synthesized music player engine.
 * Generates beautiful, soft chiptune-like musical tones in real time based on active song seed.
 */
class MusicPlayerEngine(private val context: android.content.Context) {
    private var audioTrack: AudioTrack? = null
    private var playbackJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private var currentFrequency = 440.0f
    private var isMuted = false

    @Volatile var volumeAttenuation = 0.12f
    @Volatile var waveformType = "Sine" // "Sine", "Square", "Triangle", "Sawtooth"
    @Volatile var eqBass = 1.0f
    @Volatile var eqMid = 1.0f
    @Volatile var eqTreble = 1.0f
    @Volatile var latestWaveform = FloatArray(128)

    // Advanced ADSR parameters
    @Volatile var envelopeAttack = 0.15f
    @Volatile var envelopeDecay = 0.15f
    @Volatile var envelopeSustain = 0.7f
    @Volatile var envelopeRelease = 0.25f

    // Advanced LFO parameters
    @Volatile var lfoTremoloActive = false
    @Volatile var lfoTremoloRate = 3.0f
    @Volatile var lfoTremoloDepth = 0.3f
    @Volatile var lfoVibratoActive = false
    @Volatile var lfoVibratoRate = 4.0f
    @Volatile var lfoVibratoDepth = 0.1f

    // Interactive Pitch bend / scratch
    @Volatile var pitchBendMultiplier = 1.0f

    @Volatile var crossfadeDurationMs: Long = 0
    @Volatile private var transitionProgress = 1.0f
    @Volatile private var oldFrequency = 0.0f

    fun computeAdsrFactor(t: Int, N: Double): Double {
        val a = envelopeAttack.coerceIn(0.01f, 0.9f)
        val d = envelopeDecay.coerceIn(0.01f, 0.9f)
        val r = envelopeRelease.coerceIn(0.01f, 0.9f)
        val s = envelopeSustain.coerceIn(0f, 1f)
        
        val sum = a + d + r
        val scale = if (sum > 1.0) 1.0 / sum else 1.0
        val normA = a * scale
        val normD = d * scale
        val normR = r * scale
        
        val p = t.toDouble() / N
        
        return when {
            p < normA -> {
                p / normA
            }
            p < normA + normD -> {
                val dProgress = (p - normA) / normD
                1.0 - dProgress * (1.0 - s)
            }
            p < 1.0 - normR -> {
                s.toDouble()
            }
            p < 1.0 -> {
                val rProgress = (p - (1.0 - normR)) / normR
                s * (1.0 - rProgress)
            }
            else -> 0.0
        }
    }

    fun startPlaying(baseFreq: Float) {
        if (playbackJob != null && playbackJob?.isActive == true && currentFrequency != baseFreq) {
            oldFrequency = currentFrequency
            currentFrequency = baseFreq
            transitionProgress = 0.0f
            return
        }

        currentFrequency = baseFreq
        transitionProgress = 1.0f
        oldFrequency = 0.0f
        stopPlaying()

        playbackJob = scope.launch {
            val sampleRate = 44100
            var minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            if (minBufferSize <= 0) {
                minBufferSize = 4096
            }

            try {
                val builder = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .build()
                    )
                    .setBufferSizeInBytes(minBufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    builder.setContext(context)
                }

                audioTrack = builder.build()

                val track = audioTrack
                if (track != null && track.state == AudioTrack.STATE_INITIALIZED) {
                    track.play()
                } else {
                    Log.e("MusicPlayerEngine", "AudioTrack was not initialized: state = ${track?.state}")
                }

                // Generate a rotating arpeggio pattern based on the base frequency
                val notesMult = listOf(1.0f, 1.2f, 1.5f, 1.8f, 1.5f, 1.2f)
                var noteIndex1 = 0
                var noteIndex2 = 0
                var samplesWritten1 = 0
                var samplesWritten2 = 0
                var totalSamples = 0L
                val shortBuffer = ShortArray(1024)

                var phase1 = 0.0
                var phase2 = 0.0

                while (isActive) {
                    if (isMuted) {
                        shortBuffer.fill(0)
                        delay(25)
                    } else {
                        // Blend progress increment
                        if (crossfadeDurationMs > 0 && transitionProgress < 1.0f) {
                            val increment = 1024.0f / (sampleRate * (crossfadeDurationMs / 1000.0f))
                            transitionProgress = (transitionProgress + increment).coerceAtMost(1.0f)
                        } else {
                            transitionProgress = 1.0f
                        }

                        if (transitionProgress >= 1.0f || oldFrequency <= 0.0f) {
                            val samplesPerNote = sampleRate * 0.3
                            if (samplesWritten2 > samplesPerNote) {
                                noteIndex2 = (noteIndex2 + 1) % notesMult.size
                                samplesWritten2 = 0
                            }

                            for (i in shortBuffer.indices) {
                                val sampleTime = (totalSamples + i).toDouble() / sampleRate
                                
                                val vibratoFactor = if (lfoVibratoActive) {
                                    1.0 + kotlin.math.sin(2.0 * Math.PI * lfoVibratoRate * sampleTime) * 0.08 * lfoVibratoDepth
                                } else {
                                    1.0
                                }
                                val tremoloFactor = if (lfoTremoloActive) {
                                    1.0 - (kotlin.math.sin(2.0 * Math.PI * lfoTremoloRate * sampleTime) * 0.5 + 0.5) * lfoTremoloDepth
                                } else {
                                    1.0
                                }

                                val currentNoteFreq2 = currentFrequency * notesMult[noteIndex2] * pitchBendMultiplier * vibratoFactor
                                val phaseIncrement2 = (2.0 * Math.PI * currentNoteFreq2) / sampleRate

                                val bassWave = sin(phase2 * 0.5)
                                val midWave = when (waveformType) {
                                    "Square" -> if (phase2 < Math.PI) 1.0 else -1.0
                                    "Triangle" -> {
                                        val t = phase2 / (2.0 * Math.PI)
                                        2.0 * kotlin.math.abs(2.0 * t - 1.0) - 1.0
                                    }
                                    "Sawtooth" -> 2.0 * (phase2 / (2.0 * Math.PI)) - 1.0
                                    "White Noise" -> Math.random() * 2.0 - 1.0
                                    else -> sin(phase2)
                                }
                                val trebleWave = sin(phase2 * 3.0)

                                val mixedWave = (bassWave * eqBass * 0.45) + (midWave * eqMid * 0.50) + (trebleWave * eqTreble * 0.25)
                                val adsrFactor = computeAdsrFactor(samplesWritten2 + i, samplesPerNote)
                                
                                val value = (mixedWave * 32767.0 * volumeAttenuation * tremoloFactor * adsrFactor).toInt().coerceIn(-32768, 32767).toShort()
                                
                                shortBuffer[i] = value
                                phase2 += phaseIncrement2
                                if (phase2 > 2.0 * Math.PI) {
                                    phase2 -= 2.0 * Math.PI
                                }
                            }
                            samplesWritten2 += shortBuffer.size
                            totalSamples += shortBuffer.size
                            
                            phase1 = phase2
                            noteIndex1 = noteIndex2
                            samplesWritten1 = samplesWritten2
                        } else {
                            val samplesPerNote = sampleRate * 0.3
                            
                            // Voice 1 (old)
                            if (samplesWritten1 > samplesPerNote) {
                                noteIndex1 = (noteIndex1 + 1) % notesMult.size
                                samplesWritten1 = 0
                            }

                            // Voice 2 (new)
                            if (samplesWritten2 > samplesPerNote) {
                                noteIndex2 = (noteIndex2 + 1) % notesMult.size
                                samplesWritten2 = 0
                            }

                            for (i in shortBuffer.indices) {
                                val sampleTime = (totalSamples + i).toDouble() / sampleRate
                                
                                val vibratoFactor = if (lfoVibratoActive) {
                                    1.0 + kotlin.math.sin(2.0 * Math.PI * lfoVibratoRate * sampleTime) * 0.08 * lfoVibratoDepth
                                } else {
                                    1.0
                                }
                                val tremoloFactor = if (lfoTremoloActive) {
                                    1.0 - (kotlin.math.sin(2.0 * Math.PI * lfoTremoloRate * sampleTime) * 0.5 + 0.5) * lfoTremoloDepth
                                } else {
                                    1.0
                                }

                                // Voice 1 (old)
                                val currentNoteFreq1 = oldFrequency * notesMult[noteIndex1] * pitchBendMultiplier * vibratoFactor
                                val phaseIncrement1 = (2.0 * Math.PI * currentNoteFreq1) / sampleRate

                                val bassWave1 = sin(phase1 * 0.5)
                                val midWave1 = when (waveformType) {
                                    "Square" -> if (phase1 < Math.PI) 1.0 else -1.0
                                    "Triangle" -> {
                                        val t = phase1 / (2.0 * Math.PI)
                                        2.0 * kotlin.math.abs(2.0 * t - 1.0) - 1.0
                                    }
                                    "Sawtooth" -> 2.0 * (phase1 / (2.0 * Math.PI)) - 1.0
                                    "White Noise" -> Math.random() * 2.0 - 1.0
                                    else -> sin(phase1)
                                }
                                val trebleWave1 = sin(phase1 * 3.0)
                                val mixedWave1 = (bassWave1 * eqBass * 0.45) + (midWave1 * eqMid * 0.50) + (trebleWave1 * eqTreble * 0.25)
                                val adsrFactor1 = computeAdsrFactor(samplesWritten1 + i, samplesPerNote)
                                val envelopeWave1 = mixedWave1 * adsrFactor1

                                // Voice 2 (new)
                                val currentNoteFreq2 = currentFrequency * notesMult[noteIndex2] * pitchBendMultiplier * vibratoFactor
                                val phaseIncrement2 = (2.0 * Math.PI * currentNoteFreq2) / sampleRate

                                val bassWave2 = sin(phase2 * 0.5)
                                val midWave2 = when (waveformType) {
                                    "Square" -> if (phase2 < Math.PI) 1.0 else -1.0
                                    "Triangle" -> {
                                        val t = phase2 / (2.0 * Math.PI)
                                        2.0 * kotlin.math.abs(2.0 * t - 1.0) - 1.0
                                    }
                                    "Sawtooth" -> 2.0 * (phase2 / (2.0 * Math.PI)) - 1.0
                                    "White Noise" -> Math.random() * 2.0 - 1.0
                                    else -> sin(phase2)
                                }
                                val trebleWave2 = sin(phase2 * 3.0)
                                val mixedWave2 = (bassWave2 * eqBass * 0.45) + (midWave2 * eqMid * 0.50) + (trebleWave2 * eqTreble * 0.25)
                                val adsrFactor2 = computeAdsrFactor(samplesWritten2 + i, samplesPerNote)
                                val envelopeWave2 = mixedWave2 * adsrFactor2

                                val weightOld = 1.0f - transitionProgress
                                val weightNew = transitionProgress
                                val blendedWave = (envelopeWave1 * weightOld) + (envelopeWave2 * weightNew)

                                val value = (blendedWave * 32767.0 * volumeAttenuation * tremoloFactor).toInt().coerceIn(-32768, 32767).toShort()
                                shortBuffer[i] = value

                                phase1 += phaseIncrement1
                                if (phase1 > 2.0 * Math.PI) phase1 -= 2.0 * Math.PI

                                phase2 += phaseIncrement2
                                if (phase2 > 2.0 * Math.PI) phase2 -= 2.0 * Math.PI
                            }
                            samplesWritten1 += shortBuffer.size
                            samplesWritten2 += shortBuffer.size
                            totalSamples += shortBuffer.size
                        }
                    }

                    if (!isMuted) {
                        val tempWave = FloatArray(128)
                        for (j in 0 until 128) {
                            val idx = (j * shortBuffer.size) / 128
                            tempWave[j] = shortBuffer[idx] / 32768.0f
                        }
                        latestWaveform = tempWave
                    } else {
                        latestWaveform = FloatArray(128)
                    }

                    val written = audioTrack?.write(shortBuffer, 0, shortBuffer.size) ?: 0
                    if (written <= 0) {
                        delay(20)
                    }
                }
            } catch (e: Exception) {
                Log.e("MusicPlayerEngine", "Error during synthesizer playback", e)
            } finally {
                cleanupTrack()
            }
        }
    }

    fun stopPlaying() {
        playbackJob?.cancel()
        playbackJob = null
        latestWaveform = FloatArray(128)
        cleanupTrack()
    }

    fun setMute(muted: Boolean) {
        isMuted = muted
    }

    fun isMuted(): Boolean = isMuted

    private fun cleanupTrack() {
        try {
            audioTrack?.let {
                if (it.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: Exception) {
            Log.e("MusicPlayerEngine", "Error cleaning up AudioTrack", e)
        }
        audioTrack = null
    }

    fun release() {
        stopPlaying()
        scope.cancel()
    }
}
