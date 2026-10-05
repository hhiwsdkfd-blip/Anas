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

    TimerService.selectDuration(15 * 60 * 1000L)
    assertEquals("15:00", TimerService.timerState.value.formattedTime)

    TimerService.selectDuration(30 * 60 * 1000L)
    assertEquals("30:00", TimerService.timerState.value.formattedTime)

    TimerService.selectDuration(60 * 60 * 1000L)
    assertEquals("60:00", TimerService.timerState.value.formattedTime)
  }

  @Test
  fun `verify preset string resources exist`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    assertEquals("15m", context.getString(R.string.preset_15m_short))
    assertEquals("30m", context.getString(R.string.preset_30m_short))
    assertEquals("60m", context.getString(R.string.preset_60m_short))
  }

  @Test
  fun `verify auto-shutoff history records and limits to 5 items`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    TimerService.clearShutoffHistory(context)
    assertTrue(TimerService.getShutoffHistory(context).isEmpty())

    val baseTime = 1700000000000L
    for (i in 1..7) {
      TimerService.addShutoffHistoryRecord(context, baseTime + (i * 1000L))
    }

    val history = TimerService.getShutoffHistory(context)
    assertEquals(5, history.size)
    // Most recent timestamp should be first
    assertEquals(baseTime + 7000L, history[0])
    assertEquals(baseTime + 6000L, history[1])

    TimerService.clearShutoffHistory(context)
    assertTrue(TimerService.getShutoffHistory(context).isEmpty())
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
