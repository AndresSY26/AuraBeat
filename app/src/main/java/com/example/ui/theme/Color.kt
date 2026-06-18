package com.example.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color

// Global mutable state to control the app's dark/light theme dynamically.
// This allows all references to static colors to adapt in real time during composition.
var isSystemDarkThemeState by mutableStateOf(false)

// Vibrant Palette - Material 3 Inspired Color Theme (Dynamic based on theme)
val DeepSableSpace: Color
    get() = if (isSystemDarkThemeState) Color(0xFF0C0914) else Color(0xFFFEF7FF) // Deep dark space vs light violet-white

val DarkSurfaceCard: Color
    get() = if (isSystemDarkThemeState) Color(0xFF150F26) else Color(0xFFF3EDF7) // Rich dark slate-purple vs soft light grey-purple

val NeonViolet: Color
    get() = if (isSystemDarkThemeState) Color(0xFFBB86FC) else Color(0xFF6750A4) // Light glowing violet vs deep purple

val NeonCyan: Color
    get() = if (isSystemDarkThemeState) Color(0xFF03DAC6) else Color(0xFFB69DF8) // Vivid neon cyan vs pastel violet-cyan

val SoftLilac: Color
    get() = if (isSystemDarkThemeState) Color(0xFF28114D) else Color(0xFFEADDFF) // Warm deep violet vs warm light lilac

val HotPink: Color
    get() = if (isSystemDarkThemeState) Color(0xFFFF4081) else Color(0xFFF43F5E) // Hot pink accents

val TextPrimary: Color
    get() = if (isSystemDarkThemeState) Color(0xFFFEF7FF) else Color(0xFF1D1B20) // Vibrant white vs deep dark purple-black

val TextSecondary: Color
    get() = if (isSystemDarkThemeState) Color(0xFFB1ABC0) else Color(0xFF49454F) // Soft lavender-grey vs muted medium grey

val GreenStatus = Color(0xFF10B981) // Secure offline green indicator

val BorderAmbient: Color
    get() = if (isSystemDarkThemeState) Color(0xFF2D2447) else Color(0xFFE7E0EC) // Dark borders vs light grey-purple borders

val PlayerOnSoftLilacColor: Color
    get() = if (isSystemDarkThemeState) Color(0xFFECE5F9) else Color(0xFF21005D) // Glowing soft lilac in dark theme vs deep purple in light theme

