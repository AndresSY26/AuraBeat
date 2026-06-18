package com.example

import com.example.ui.EqPreset
import org.junit.Assert.*
import org.junit.Test

class EqualizerPresetTest {
    @Test
    fun testEqPresetValues() {
        assertEquals(1.0f, EqPreset.FLAT.bass, 0.001f)
        assertEquals(1.0f, EqPreset.FLAT.mid, 0.001f)
        assertEquals(1.0f, EqPreset.FLAT.treble, 0.001f)

        assertEquals(1.6f, EqPreset.BASS_BOOST.bass, 0.001f)
        assertEquals(1.0f, EqPreset.BASS_BOOST.mid, 0.001f)
        assertEquals(0.9f, EqPreset.BASS_BOOST.treble, 0.001f)

        assertEquals(0.8f, EqPreset.VOCAL_FOCUS.bass, 0.001f)
        assertEquals(1.6f, EqPreset.VOCAL_FOCUS.mid, 0.001f)
        assertEquals(1.2f, EqPreset.VOCAL_FOCUS.treble, 0.001f)

        assertEquals(1.2f, EqPreset.ACOUSTIC.bass, 0.001f)
        assertEquals(0.9f, EqPreset.ACOUSTIC.mid, 0.001f)
        assertEquals(1.4f, EqPreset.ACOUSTIC.treble, 0.001f)
    }
}
