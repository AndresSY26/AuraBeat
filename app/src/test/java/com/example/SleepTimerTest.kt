package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.ui.MusicViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SleepTimerTest {

    @Test
    fun testSleepTimerInitialization() = runTest {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = MusicViewModel(app)

        assertNotNull(viewModel)
        assertEquals(0, viewModel.sleepTimerMinutesLeft.value)
    }

    @Test
    fun testSetSleepTimer() = runTest {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = MusicViewModel(app)

        viewModel.setSleepTimer(30)
        assertEquals(30, viewModel.sleepTimerMinutesLeft.value)

        // Reset sleep timer
        viewModel.setSleepTimer(0)
        assertEquals(0, viewModel.sleepTimerMinutesLeft.value)
    }
}
