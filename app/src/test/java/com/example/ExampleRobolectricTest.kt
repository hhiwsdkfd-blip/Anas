package com.example

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val stopIntent = Intent(context, TimerService::class.java).apply {
      action = TimerService.ACTION_STOP
    }
    Robolectric.buildService(TimerService::class.java, stopIntent).create().startCommand(0, 1)
    TimerService.resetToDefault()
  }

  @Test
  fun `verify app name resource`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Wi-Fi Sleep Timer", appName)
  }

  @Test
  fun `verify duration selection and formatting`() {
    TimerService.selectDuration(5 * 60 * 1000L)
    assertEquals("05:00", TimerService.timerState.value.formattedTime)

    TimerService.selectDuration(30 * 60 * 1000L)
    assertEquals("30:00", TimerService.timerState.value.formattedTime)
  }

  @Test
  fun `verify foreground service start and stop lifecycle`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val startIntent = Intent(context, TimerService::class.java).apply {
      action = TimerService.ACTION_START
      putExtra(TimerService.EXTRA_DURATION_MILLIS, 10 * 60 * 1000L)
    }
    val controller = Robolectric.buildService(TimerService::class.java, startIntent).create()
    controller.startCommand(0, 1)

    assertTrue(TimerService.timerState.value.isRunning)
    assertEquals(10 * 60 * 1000L, TimerService.timerState.value.totalDurationMillis)

    val stopIntent = Intent(context, TimerService::class.java).apply {
      action = TimerService.ACTION_STOP
    }
    controller.get().onStartCommand(stopIntent, 0, 2)
    assertFalse(TimerService.timerState.value.isRunning)
  }

  @Test
  fun `verify MainActivity launches without crash`() {
    val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
    val activity = controller.get()
    assertTrue(activity != null)
  }
}
