package com.example

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.provider.Settings
import androidx.core.app.NotificationCompat
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class TimerUiState(
  val totalDurationMillis: Long = TimerService.DEFAULT_DURATION_MILLIS,
  val remainingMillis: Long = TimerService.DEFAULT_DURATION_MILLIS,
  val isRunning: Boolean = false,
  val isFinished: Boolean = false,
  val requiresPanelPrompt: Boolean = false,
) {
  val formattedTime: String
    get() = TimerService.formatMillisToMmSs(remainingMillis)

  val progressFraction: Float
    get() =
      if (totalDurationMillis <= 0L) 0f
      else (remainingMillis.toFloat() / totalDurationMillis.toFloat()).coerceIn(0f, 1f)
}

class TimerService : Service() {

  private var countDownTimer: CountDownTimer? = null
  private var lastNotifiedSecond: Long = -1L

  override fun onCreate() {
    super.onCreate()
    createNotificationChannel()
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    when (intent?.action) {
      ACTION_START -> {
        val durationMillis =
          intent.getLongExtra(EXTRA_DURATION_MILLIS, _timerState.value.remainingMillis)
            .coerceAtLeast(1_000L)
        startCountdown(durationMillis)
      }
      ACTION_ADD_TIME -> {
        val additionalMillis = intent.getLongExtra(EXTRA_ADD_MILLIS, FIVE_MINUTES_MILLIS)
        if (_timerState.value.isRunning) {
          val updatedRemaining =
            (_timerState.value.remainingMillis + additionalMillis).coerceAtMost(MAX_DURATION_MILLIS)
          val updatedTotal =
            maxOf(_timerState.value.totalDurationMillis, updatedRemaining)
          startCountdown(updatedRemaining, totalMillisOverride = updatedTotal)
        }
      }
      ACTION_STOP -> {
        stopCountdown(resetToTotal = true)
        stopForegroundCompat()
        stopSelf()
      }
    }
    return START_NOT_STICKY
  }

  private fun startCountdown(durationMillis: Long, totalMillisOverride: Long? = null) {
    countDownTimer?.cancel()
    lastNotifiedSecond = -1L

    val totalMillis = totalMillisOverride ?: durationMillis
    _timerState.value =
      TimerUiState(
        totalDurationMillis = totalMillis,
        remainingMillis = durationMillis,
        isRunning = true,
        isFinished = false,
        requiresPanelPrompt = false,
      )

    val initialNotification = buildRunningNotification(durationMillis)
    startForegroundSafely(NOTIFICATION_ID_ACTIVE, initialNotification)

    countDownTimer =
      object : CountDownTimer(durationMillis, 250L) {
        override fun onTick(millisUntilFinished: Long) {
          _timerState.update { state ->
            state.copy(
              remainingMillis = millisUntilFinished,
              isRunning = true,
              isFinished = false,
            )
          }
          val currentSecond = (millisUntilFinished + 999L) / 1000L
          if (currentSecond != lastNotifiedSecond) {
            lastNotifiedSecond = currentSecond
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(NOTIFICATION_ID_ACTIVE, buildRunningNotification(millisUntilFinished))
          }
        }

        override fun onFinish() {
          onTimerReachedZero()
        }
      }.start()
  }

  internal fun onTimerReachedZero() {
    countDownTimer?.cancel()
    countDownTimer = null

    WifiAutomationService.executeAutoTurnOff(applicationContext)
    recordSleepSession(applicationContext, _timerState.value.totalDurationMillis)
    triggerGentleVibration(applicationContext)

    _timerState.update { state ->
      state.copy(
        remainingMillis = 0L,
        isRunning = false,
        isFinished = true,
        requiresPanelPrompt = false,
      )
    }

    showCompletionNotification(disabledAutomatically = true)
    stopForegroundCompat()
    stopSelf()
  }

  private fun stopCountdown(resetToTotal: Boolean) {
    countDownTimer?.cancel()
    countDownTimer = null
    _timerState.update { state ->
      val resetMillis =
        if (resetToTotal && state.totalDurationMillis > 0L) state.totalDurationMillis
        else DEFAULT_DURATION_MILLIS
      state.copy(
        totalDurationMillis = resetMillis,
        remainingMillis = resetMillis,
        isRunning = false,
        isFinished = false,
        requiresPanelPrompt = false,
      )
    }
  }

  private fun startForegroundSafely(id: Int, notification: Notification) {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        startForeground(id, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
      } else {
        startForeground(id, notification)
      }
    } catch (_: Exception) {
      try {
        startForeground(id, notification)
      } catch (_: Exception) {
        // Ignore in restricted test environments
      }
    }
  }

  private fun stopForegroundCompat() {
    try {
      stopForeground(STOP_FOREGROUND_REMOVE)
    } catch (_: Exception) {
      // Ignore if not in foreground
    }
  }

  private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      val activeChannel =
        NotificationChannel(
          CHANNEL_ID_TIMER,
          getString(R.string.notification_channel_name),
          NotificationManager.IMPORTANCE_LOW,
        ).apply {
          description = getString(R.string.notification_channel_desc)
          setShowBadge(false)
        }
      val alertChannel =
        NotificationChannel(
          CHANNEL_ID_FINISHED,
          getString(R.string.notification_title_finished),
          NotificationManager.IMPORTANCE_HIGH,
        ).apply {
          description = getString(R.string.notification_body_finished_panel)
        }
      manager.createNotificationChannel(activeChannel)
      manager.createNotificationChannel(alertChannel)
    }
  }

  private fun buildRunningNotification(remainingMillis: Long): Notification {
    val openAppIntent =
      Intent(this, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
      }
    val contentPendingIntent =
      PendingIntent.getActivity(
        this,
        0,
        openAppIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
      )

    val stopIntent =
      Intent(this, TimerService::class.java).apply {
        action = ACTION_STOP
      }
    val stopPendingIntent =
      PendingIntent.getService(
        this,
        1,
        stopIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
      )

    val formattedRemaining = formatMillisToMmSs(remainingMillis)

    return NotificationCompat.Builder(this, CHANNEL_ID_TIMER)
      .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
      .setContentTitle(getString(R.string.notification_title_active))
      .setContentText("$formattedRemaining remaining • Wi-Fi turns off at 00:00")
      .setOnlyAlertOnce(true)
      .setOngoing(true)
      .setCategory(NotificationCompat.CATEGORY_SERVICE)
      .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
      .setContentIntent(contentPendingIntent)
      .addAction(
        android.R.drawable.ic_menu_close_clear_cancel,
        getString(R.string.notification_action_stop),
        stopPendingIntent,
      )
      .build()
  }

  private fun showCompletionNotification(disabledAutomatically: Boolean = true) {
    val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val targetIntent =
      Intent(this, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
      }

    val pendingIntent =
      PendingIntent.getActivity(
        this,
        2,
        targetIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
      )

    val bodyText = getString(R.string.notification_body_finished_disabled)

    val builder =
      NotificationCompat.Builder(this, CHANNEL_ID_FINISHED)
        .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
        .setContentTitle(getString(R.string.notification_title_finished))
        .setContentText(bodyText)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setCategory(NotificationCompat.CATEGORY_ALARM)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)

    manager.notify(NOTIFICATION_ID_FINISHED, builder.build())
  }

  private fun recordSleepSession(context: Context, durationMillis: Long) {
    try {
      val prefs = context.getSharedPreferences(PREFS_STATS, Context.MODE_PRIVATE)
      val currentSessions = prefs.getInt(KEY_TOTAL_SESSIONS, 0)
      val currentMinutes = prefs.getLong(KEY_TOTAL_MINUTES_SAVED, 0L)
      val minutes = (durationMillis / 60_000L).coerceAtLeast(1L)
      prefs.edit()
        .putInt(KEY_TOTAL_SESSIONS, currentSessions + 1)
        .putLong(KEY_TOTAL_MINUTES_SAVED, currentMinutes + minutes)
        .putLong(KEY_LAST_TIMESTAMP, System.currentTimeMillis())
        .apply()
    } catch (_: Exception) {}
  }

  private fun triggerGentleVibration(context: Context) {
    try {
      val prefs = context.getSharedPreferences(PREFS_SETTINGS, Context.MODE_PRIVATE)
      val vibrationEnabled = prefs.getBoolean(KEY_VIBRATION_ENABLED, true)
      if (!vibrationEnabled) return

      val vibrator =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
          val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager
          manager?.defaultVibrator
        } else {
          @Suppress("DEPRECATION")
          context.getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator
        }
      if (vibrator?.hasVibrator() == true) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          val effect = android.os.VibrationEffect.createWaveform(longArrayOf(0, 150, 100, 250), -1)
          vibrator.vibrate(effect)
        } else {
          @Suppress("DEPRECATION")
          vibrator.vibrate(250)
        }
      }
    } catch (_: Exception) {}
  }

  override fun onDestroy() {
    countDownTimer?.cancel()
    countDownTimer = null
    super.onDestroy()
  }

  override fun onBind(intent: Intent?): IBinder? = null

  companion object {
    const val ACTION_START = "com.example.action.START_TIMER"
    const val ACTION_STOP = "com.example.action.STOP_TIMER"
    const val ACTION_ADD_TIME = "com.example.action.ADD_TIME"
    const val EXTRA_DURATION_MILLIS = "com.example.extra.DURATION_MILLIS"
    const val EXTRA_ADD_MILLIS = "com.example.extra.ADD_MILLIS"

    const val DEFAULT_DURATION_MILLIS = 15 * 60 * 1000L
    const val FIVE_MINUTES_MILLIS = 5 * 60 * 1000L
    const val ONE_MINUTE_MILLIS = 60 * 1000L
    const val MAX_DURATION_MILLIS = 720 * 60 * 1000L // Up to 12 hours for custom sleep

    const val PREFS_STATS = "wifi_sleep_stats"
    const val PREFS_SETTINGS = "wifi_sleep_settings"
    const val KEY_TOTAL_SESSIONS = "total_sessions"
    const val KEY_TOTAL_MINUTES_SAVED = "total_minutes_saved"
    const val KEY_LAST_TIMESTAMP = "last_timestamp"
    const val KEY_VIBRATION_ENABLED = "vibration_enabled"
    const val KEY_SHAKE_ENABLED = "shake_enabled"

    private const val CHANNEL_ID_TIMER = "wifi_sleep_timer_channel"
    private const val CHANNEL_ID_FINISHED = "wifi_sleep_timer_finished_channel"
    private const val NOTIFICATION_ID_ACTIVE = 1001
    private const val NOTIFICATION_ID_FINISHED = 1002

    private val _timerState = MutableStateFlow(TimerUiState())
    val timerState: StateFlow<TimerUiState> = _timerState.asStateFlow()

    fun selectDuration(durationMillis: Long) {
      val clamped = durationMillis.coerceIn(1_000L, MAX_DURATION_MILLIS)
      if (!_timerState.value.isRunning) {
        _timerState.value =
          TimerUiState(
            totalDurationMillis = clamped,
            remainingMillis = clamped,
            isRunning = false,
            isFinished = false,
            requiresPanelPrompt = false,
          )
      }
    }

    fun adjustIdleDurationByDelta(deltaMillis: Long) {
      if (!_timerState.value.isRunning) {
        val current = _timerState.value.remainingMillis
        val base = if (current <= 0L) DEFAULT_DURATION_MILLIS else current
        val updated = (base + deltaMillis).coerceIn(ONE_MINUTE_MILLIS, MAX_DURATION_MILLIS)
        selectDuration(updated)
      }
    }

    fun acknowledgeFinishedPrompt() {
      _timerState.update { state ->
        state.copy(requiresPanelPrompt = false)
      }
    }

    fun resetToDefault() {
      if (!_timerState.value.isRunning) {
        _timerState.value = TimerUiState()
      }
    }

    fun formatMillisToMmSs(millis: Long): String {
      val totalSeconds = ((millis.coerceAtLeast(0L) + 999L) / 1000L).takeIf { millis > 0L } ?: 0L
      val minutes = totalSeconds / 60L
      val seconds = totalSeconds % 60L
      return String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    /**
     * Disables Wi-Fi automatically via `WifiManager.setWifiEnabled(false)`.
     * Returns true if Wi-Fi was disabled automatically (or was already off).
     */
    @Suppress("DEPRECATION")
    fun disableWifiAutomatically(context: Context): Boolean {
      WifiAutomationService.executeAutoTurnOff(context)
      val wifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
      return wifiManager?.isWifiEnabled == false
    }

    @Suppress("DEPRECATION")
    fun disableWifiOrPrompt(context: Context) {
      WifiAutomationService.executeAutoTurnOff(context)
    }
  }
}
