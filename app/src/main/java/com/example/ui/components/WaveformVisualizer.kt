package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonViolet
import kotlin.random.Random

@Composable
fun WaveformVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    styleStr: String = "Classic Neon",
    barCount: Int = 24,
    color1: Color = NeonViolet,
    color2: Color = NeonCyan,
    bass: Float = 1.0f,
    mid: Float = 1.0f,
    treble: Float = 1.0f,
    realtimeWaveform: FloatArray = FloatArray(0)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    
    // Create multiple animated values for visualizer randomness
    val animProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave_progress"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // 4. FLUID SYNCED BIOLOGICAL WAVE
        if (styleStr == "Fluid Synced") {
            val step = 4f
            val pointsCount = (width / step).toInt() + 1

            // Bass Wave (Graves) - Neon Violet
            val pathBass = androidx.compose.ui.graphics.Path()
            pathBass.moveTo(0f, height)
            for (i in 0..pointsCount) {
                val x = i * step
                val phase = (animProgress * 2 * Math.PI * 1.0) + (x * 2.0 * Math.PI / width)
                val amp = if (isPlaying) {
                    height * 0.35f * bass
                } else {
                    height * 0.08f * bass
                }
                val realMod = if (realtimeWaveform.isNotEmpty() && isPlaying) {
                    val sampleIdx = (i * 42) / pointsCount
                    if (sampleIdx < realtimeWaveform.size) realtimeWaveform[sampleIdx] else 0f
                } else 0f
                val y = (height / 2f) + amp * kotlin.math.sin(phase).toFloat() + realMod * height * 0.35f * bass
                pathBass.lineTo(x, y)
            }
            pathBass.lineTo(width, height)
            pathBass.lineTo(0f, height)
            pathBass.close()
            
            drawPath(
                path = pathBass,
                brush = Brush.verticalGradient(
                    colors = listOf(color1.copy(alpha = 0.55f), color1.copy(alpha = 0.0f)),
                    startY = height * 0.2f,
                    endY = height
                )
            )
            
            val strokePathBass = androidx.compose.ui.graphics.Path()
            for (i in 0..pointsCount) {
                val x = i * step
                val phase = (animProgress * 2 * Math.PI * 1.0) + (x * 2.0 * Math.PI / width)
                val amp = if (isPlaying) {
                    height * 0.35f * bass
                } else {
                    height * 0.08f * bass
                }
                val realMod = if (realtimeWaveform.isNotEmpty() && isPlaying) {
                    val sampleIdx = (i * 42) / pointsCount
                    if (sampleIdx < realtimeWaveform.size) realtimeWaveform[sampleIdx] else 0f
                } else 0f
                val y = (height / 2f) + amp * kotlin.math.sin(phase).toFloat() + realMod * height * 0.35f * bass
                if (i == 0) strokePathBass.moveTo(x, y) else strokePathBass.lineTo(x, y)
            }
            drawPath(
                path = strokePathBass,
                color = color1,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
            )

            // Mid Wave (Medios) - Neon Cyan
            val pathMid = androidx.compose.ui.graphics.Path()
            pathMid.moveTo(0f, height)
            for (i in 0..pointsCount) {
                val x = i * step
                val phase = (-animProgress * 2 * Math.PI * 1.4) + (x * 4.0 * Math.PI / width)
                val amp = if (isPlaying) {
                    height * 0.25f * mid
                } else {
                    height * 0.06f * mid
                }
                val realMod = if (realtimeWaveform.isNotEmpty() && isPlaying) {
                    val sampleIdx = 43 + (i * 42) / pointsCount
                    if (sampleIdx < realtimeWaveform.size) realtimeWaveform[sampleIdx] else 0f
                } else 0f
                val y = (height / 2f) + amp * kotlin.math.sin(phase).toFloat() + realMod * height * 0.25f * mid
                pathMid.lineTo(x, y)
            }
            pathMid.lineTo(width, height)
            pathMid.lineTo(0f, height)
            pathMid.close()

            drawPath(
                path = pathMid,
                brush = Brush.verticalGradient(
                    colors = listOf(color2.copy(alpha = 0.45f), color2.copy(alpha = 0.0f)),
                    startY = height * 0.2f,
                    endY = height
                )
            )
            
            val strokePathMid = androidx.compose.ui.graphics.Path()
            for (i in 0..pointsCount) {
                val x = i * step
                val phase = (-animProgress * 2 * Math.PI * 1.4) + (x * 4.0 * Math.PI / width)
                val amp = if (isPlaying) {
                    height * 0.25f * mid
                } else {
                    height * 0.06f * mid
                }
                val realMod = if (realtimeWaveform.isNotEmpty() && isPlaying) {
                    val sampleIdx = 43 + (i * 42) / pointsCount
                    if (sampleIdx < realtimeWaveform.size) realtimeWaveform[sampleIdx] else 0f
                } else 0f
                val y = (height / 2f) + amp * kotlin.math.sin(phase).toFloat() + realMod * height * 0.25f * mid
                if (i == 0) strokePathMid.moveTo(x, y) else strokePathMid.lineTo(x, y)
            }
            drawPath(
                path = strokePathMid,
                color = color2,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5f)
            )

            // Treble Wave (Agudos) - Hot Pink
            val pathTreble = androidx.compose.ui.graphics.Path()
            pathTreble.moveTo(0f, height)
            val hotPink = Color(0xFFFF007F)
            for (i in 0..pointsCount) {
                val x = i * step
                val phase = (animProgress * 3 * Math.PI * 0.9) + (x * 10.0 * Math.PI / width)
                val amp = if (isPlaying) {
                    height * 0.15f * treble
                } else {
                    height * 0.04f * treble
                }
                val realMod = if (realtimeWaveform.isNotEmpty() && isPlaying) {
                    val sampleIdx = 86 + (i * 41) / pointsCount
                    if (sampleIdx < realtimeWaveform.size) realtimeWaveform[sampleIdx] else 0f
                } else 0f
                val y = (height / 2f) + amp * kotlin.math.sin(phase).toFloat() + realMod * height * 0.15f * treble
                pathTreble.lineTo(x, y)
            }
            pathTreble.lineTo(width, height)
            pathTreble.lineTo(0f, height)
            pathTreble.close()

            drawPath(
                path = pathTreble,
                brush = Brush.verticalGradient(
                    colors = listOf(hotPink.copy(alpha = 0.35f), hotPink.copy(alpha = 0.0f)),
                    startY = height * 0.2f,
                    endY = height
                )
            )
            
            val strokePathTreble = androidx.compose.ui.graphics.Path()
            for (i in 0..pointsCount) {
                val x = i * step
                val phase = (animProgress * 3 * Math.PI * 0.9) + (x * 10.0 * Math.PI / width)
                val amp = if (isPlaying) {
                    height * 0.15f * treble
                } else {
                    height * 0.04f * treble
                }
                val realMod = if (realtimeWaveform.isNotEmpty() && isPlaying) {
                    val sampleIdx = 86 + (i * 41) / pointsCount
                    if (sampleIdx < realtimeWaveform.size) realtimeWaveform[sampleIdx] else 0f
                } else 0f
                val y = (height / 2f) + amp * kotlin.math.sin(phase).toFloat() + realMod * height * 0.15f * treble
                if (i == 0) strokePathTreble.moveTo(x, y) else strokePathTreble.lineTo(x, y)
            }
            drawPath(
                path = strokePathTreble,
                color = hotPink,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
            )

            return@Canvas
        }

        // 1. PULSING NEON LINE oscilloscopo
        if (styleStr == "Pulsing Line") {
            val path = androidx.compose.ui.graphics.Path()
            val step = width / 80f
            path.moveTo(0f, height / 2f)
            
            for (i in 0..80) {
                val x = i * step
                val phase = (animProgress * 2 * Math.PI) + (i * 0.18f)
                val realMod = if (realtimeWaveform.isNotEmpty() && isPlaying) {
                    val sampleIdx = (i * 127) / 80
                    if (sampleIdx < realtimeWaveform.size) realtimeWaveform[sampleIdx] else 0f
                } else 0f
                
                val amp = if (isPlaying) {
                    if (realtimeWaveform.isNotEmpty()) {
                        height * 0.45f
                    } else {
                        (height / 2.4f) * kotlin.math.sin(phase * 0.35f).toFloat()
                    }
                } else {
                    3f
                }
                
                val yVal = if (realtimeWaveform.isNotEmpty() && isPlaying) {
                    (height / 2f) + realMod * amp
                } else {
                    val scaleFactor = if (isPlaying) kotlin.math.sin(phase).toFloat() else kotlin.math.sin(phase * 2f).toFloat()
                    (height / 2f) + amp * scaleFactor
                }
                path.lineTo(x, yVal)
            }
            
            drawPath(
                path = path,
                color = color2,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 6f,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            )
            return@Canvas
        }

        // 2. SOLID RETRO SYNTH WAVE
        if (styleStr == "Solid Wave") {
            val path = androidx.compose.ui.graphics.Path()
            path.moveTo(0f, height)
            val step = width / 50f
            
            for (i in 0..50) {
                val x = i * step
                val phase = (animProgress * 2.5 * Math.PI) + (i * 0.28f)
                val realMod = if (realtimeWaveform.isNotEmpty() && isPlaying) {
                    val sampleIdx = (i * 127) / 50
                    if (sampleIdx < realtimeWaveform.size) realtimeWaveform[sampleIdx] else 0f
                } else 0f
                
                val amp = if (isPlaying) {
                    if (realtimeWaveform.isNotEmpty()) {
                        height * 0.4f
                    } else {
                        (height / 2.6f) * (0.3f + 0.7f * kotlin.math.cos(phase * 0.25f).toFloat())
                    }
                } else {
                    2f
                }
                
                val y = if (realtimeWaveform.isNotEmpty() && isPlaying) {
                    (height * 0.55f) + realMod * amp
                } else {
                    (height * 0.55f) + amp * kotlin.math.sin(phase).toFloat()
                }
                path.lineTo(x, y)
            }
            path.lineTo(width, height)
            path.close()
            
            drawPath(
                path = path,
                brush = Brush.verticalGradient(
                    colors = listOf(color1.copy(alpha = 0.85f), color2.copy(alpha = 0.05f)),
                    startY = height * 0.15f,
                    endY = height
                )
            )
            return@Canvas
        }

        // 3. CLASSIC NEON BARS
        val spacing = 8f
        val totalSpacing = spacing * (barCount - 1)
        val barWidth = (width - totalSpacing) / barCount

        val randomValues = floatArrayOf(
            0.3f, 0.7f, 0.5f, 0.9f, 0.4f, 0.2f, 0.8f, 0.6f,
            0.9f, 0.4f, 0.6f, 0.8f, 0.5f, 0.3f, 0.7f, 0.8f,
            0.4f, 0.6f, 0.9f, 0.5f, 0.7f, 0.3f, 0.5f, 0.8f
        )

        for (i in 0 until barCount) {
            val baseVal = if (i < randomValues.size) randomValues[i] else 0.5f
            
            // Calculate height multiplier based on whether it is playing
            val mult = if (isPlaying) {
                if (realtimeWaveform.isNotEmpty()) {
                    val idxStart = (i * 128) / barCount
                    val idxEnd = ((i + 1) * 128) / barCount
                    var energySum = 0f
                    var energyCount = 0
                    for (index in idxStart until idxEnd.coerceAtMost(realtimeWaveform.size)) {
                        energySum += kotlin.math.abs(realtimeWaveform[index])
                        energyCount++
                    }
                    val realValue = if (energyCount > 0) (energySum / energyCount) * 8.5f else 0.1f
                    realValue.coerceIn(0.08f, 1.1f)
                } else {
                    // Apply a wavy sine formula combined with the infinite transition value
                    val phase = (animProgress * 2 * Math.PI) + (i * 0.4f)
                    val wave = (kotlin.math.sin(phase).toFloat() + 1f) / 2f
                    (0.2f + 0.8f * wave) * baseVal
                }
            } else {
                0.08f // Tiny idle state bar
            }

            val barHeight = (height * mult).coerceIn(4.dp.toPx(), height)
            val x = i * (barWidth + spacing)
            val y = height - barHeight

            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(color1, color2),
                    startY = y,
                    endY = height
                ),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
            )
        }
    }
}
