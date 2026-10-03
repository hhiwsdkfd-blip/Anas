package com.example

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.AlertCoral
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NightObsidian
import com.example.ui.theme.NightOutline
import com.example.ui.theme.NightSurface
import com.example.ui.theme.NightSurfaceElevated
import com.example.ui.theme.SleepAmber
import com.example.ui.theme.SleepAmberBright
import com.example.ui.theme.SleepAmberDim
import com.example.ui.theme.TextMutedNight
import com.example.ui.theme.TextPrimaryNight
import com.example.ui.theme.TextSecondaryNight
import com.example.ui.theme.WifiActiveGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class TimerPreset(
  val labelRes: Int,
  val durationMillis: Long,
  val testTag: String,
)

enum class MainTab {
  TIMER,
  DATA_USAGE
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        WifiSleepTimerScreen()
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WifiSleepTimerScreen() {
  val baseContext = LocalContext.current
  var isArabic by rememberSaveable {
    mutableStateOf(Locale.getDefault().language == "ar")
  }
  var selectedTab by rememberSaveable {
    mutableStateOf(MainTab.TIMER)
  }

  BackHandler(enabled = selectedTab != MainTab.TIMER) {
    selectedTab = MainTab.TIMER
  }

  val localizedResources =
    remember(baseContext, isArabic) {
      val locale = if (isArabic) Locale("ar") else Locale.US
      val config = Configuration(baseContext.resources.configuration)
      config.setLocale(locale)
      config.setLayoutDirection(locale)
      baseContext.createConfigurationContext(config).resources
    }

  val str = remember(localizedResources) { { resId: Int -> localizedResources.getString(resId) } }
  val layoutDirection = if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr
  val timerState by TimerService.timerState.collectAsStateWithLifecycle()
  var isWifiEnabled by remember { mutableStateOf(checkWifiEnabled(baseContext)) }

  // Preferences & Feature Settings
  val settingsPrefs = remember(baseContext) {
    baseContext.getSharedPreferences(TimerService.PREFS_SETTINGS, Context.MODE_PRIVATE)
  }
  val statsPrefs = remember(baseContext) {
    baseContext.getSharedPreferences(TimerService.PREFS_STATS, Context.MODE_PRIVATE)
  }

  var vibrationEnabled by remember {
    mutableStateOf(settingsPrefs.getBoolean(TimerService.KEY_VIBRATION_ENABLED, true))
  }
  var shakeEnabled by remember {
    mutableStateOf(settingsPrefs.getBoolean(TimerService.KEY_SHAKE_ENABLED, true))
  }

  // Stats state
  var totalSessions by remember {
    mutableIntStateOf(statsPrefs.getInt(TimerService.KEY_TOTAL_SESSIONS, 0))
  }
  var totalMinutesSaved by remember {
    mutableLongStateOf(statsPrefs.getLong(TimerService.KEY_TOTAL_MINUTES_SAVED, 0L))
  }
  var lastTimestamp by remember {
    mutableLongStateOf(statsPrefs.getLong(TimerService.KEY_LAST_TIMESTAMP, 0L))
  }

  // Refresh stats when timer finishes
  LaunchedEffect(timerState.isFinished) {
    if (timerState.isFinished) {
      totalSessions = statsPrefs.getInt(TimerService.KEY_TOTAL_SESSIONS, 0)
      totalMinutesSaved = statsPrefs.getLong(TimerService.KEY_TOTAL_MINUTES_SAVED, 0L)
      lastTimestamp = statsPrefs.getLong(TimerService.KEY_LAST_TIMESTAMP, 0L)
    }
  }

  // Custom Duration Dialog State
  var showCustomDurationDialog by remember { mutableStateOf(false) }

  // Observe real-time system Wi-Fi state changes
  DisposableEffect(baseContext) {
    val receiver =
      object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
          if (intent?.action == WifiManager.WIFI_STATE_CHANGED_ACTION) {
            val wifiState =
              intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)
            isWifiEnabled = wifiState == WifiManager.WIFI_STATE_ENABLED
          }
        }
      }
    val filter = IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION)
    ContextCompat.registerReceiver(
      baseContext,
      receiver,
      filter,
      ContextCompat.RECEIVER_NOT_EXPORTED,
    )
    onDispose {
      try {
        baseContext.unregisterReceiver(receiver)
      } catch (_: Exception) {}
    }
  }

  // Accelerometer Shake-to-Extend Listener (when timer is active & shakeEnabled)
  DisposableEffect(timerState.isRunning, shakeEnabled) {
    if (!timerState.isRunning || !shakeEnabled) {
      return@DisposableEffect onDispose {}
    }

    val sensorManager = baseContext.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    var lastShakeTimestamp = 0L

    val listener =
      object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
          event ?: return
          val x = event.values[0]
          val y = event.values[1]
          val z = event.values[2]
          val acceleration = sqrt((x * x + y * y + z * z).toDouble()) - SensorManager.GRAVITY_EARTH

          if (acceleration > 13.0) {
            val now = System.currentTimeMillis()
            if (now - lastShakeTimestamp > 2500L) {
              lastShakeTimestamp = now
              addFiveMinutesToService(baseContext)
              Toast.makeText(
                baseContext,
                str(R.string.shake_detected_toast),
                Toast.LENGTH_SHORT,
              ).show()
            }
          }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
      }

    if (accelerometer != null) {
      sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
    }

    onDispose {
      sensorManager?.unregisterListener(listener)
    }
  }

  // When the timer finishes (00:00), ensure Wi-Fi is turned off automatically and update UI state
  LaunchedEffect(timerState.isFinished) {
    if (timerState.isFinished) {
      TimerService.disableWifiAutomatically(baseContext)
      isWifiEnabled = false
    }
  }

  val notificationPermissionLauncher =
    rememberLauncherForActivityResult(
      contract = ActivityResultContracts.RequestPermission(),
    ) { _ ->
      startTimerForegroundService(baseContext, timerState.remainingMillis)
    }

  val onStartTimerClicked = {
    val durationToStart =
      if (timerState.remainingMillis <= 0L) {
        val resetDuration =
          timerState.totalDurationMillis.takeIf { it > 0L } ?: TimerService.DEFAULT_DURATION_MILLIS
        TimerService.selectDuration(resetDuration)
        resetDuration
      } else {
        timerState.remainingMillis
      }

    if (
      Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(baseContext, Manifest.permission.POST_NOTIFICATIONS) !=
          PackageManager.PERMISSION_GRANTED
    ) {
      notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    } else {
      startTimerForegroundService(baseContext, durationToStart)
    }
  }

  val presets =
    remember {
      listOf(
        TimerPreset(R.string.preset_5_min, 5 * 60 * 1000L, "preset_5m_button"),
        TimerPreset(R.string.preset_10_min, 10 * 60 * 1000L, "preset_10m_button"),
        TimerPreset(R.string.preset_15_min, 15 * 60 * 1000L, "preset_15m_button"),
        TimerPreset(R.string.preset_30_min, 30 * 60 * 1000L, "preset_30m_button"),
        TimerPreset(R.string.preset_45_min, 45 * 60 * 1000L, "preset_45m_button"),
        TimerPreset(R.string.preset_60_min, 60 * 60 * 1000L, "preset_60m_button"),
      )
    }

  CompositionLocalProvider(
    LocalLayoutDirection provides layoutDirection,
  ) {
    Scaffold(
      modifier = Modifier.fillMaxSize(),
      containerColor = NightObsidian,
      contentWindowInsets = WindowInsets.safeDrawing,
      bottomBar = {
        NavigationBar(
          containerColor = NightSurfaceElevated,
          contentColor = TextPrimaryNight,
          tonalElevation = 8.dp,
        ) {
          NavigationBarItem(
            selected = selectedTab == MainTab.TIMER,
            onClick = { selectedTab = MainTab.TIMER },
            icon = {
              Icon(
                imageVector = Icons.Filled.Bedtime,
                contentDescription = str(R.string.nav_timer),
              )
            },
            label = {
              Text(
                text = str(R.string.nav_timer),
                fontWeight = if (selectedTab == MainTab.TIMER) FontWeight.Bold else FontWeight.Normal,
              )
            },
            colors =
              NavigationBarItemDefaults.colors(
                selectedIconColor = SleepAmberBright,
                selectedTextColor = SleepAmberBright,
                indicatorColor = SleepAmberDim,
                unselectedIconColor = TextSecondaryNight,
                unselectedTextColor = TextSecondaryNight,
              ),
            modifier = Modifier.testTag("nav_tab_timer"),
          )

          NavigationBarItem(
            selected = selectedTab == MainTab.DATA_USAGE,
            onClick = { selectedTab = MainTab.DATA_USAGE },
            icon = {
              Icon(
                imageVector = Icons.Filled.DataUsage,
                contentDescription = str(R.string.nav_data_usage),
              )
            },
            label = {
              Text(
                text = str(R.string.nav_data_usage),
                fontWeight = if (selectedTab == MainTab.DATA_USAGE) FontWeight.Bold else FontWeight.Normal,
              )
            },
            colors =
              NavigationBarItemDefaults.colors(
                selectedIconColor = SleepAmberBright,
                selectedTextColor = SleepAmberBright,
                indicatorColor = SleepAmberDim,
                unselectedIconColor = TextSecondaryNight,
                unselectedTextColor = TextSecondaryNight,
              ),
            modifier = Modifier.testTag("nav_tab_data_usage"),
          )
        }
      },
    ) { innerPadding ->
      Box(
        modifier =
          Modifier.fillMaxSize()
            .background(
              Brush.verticalGradient(
                colors = listOf(NightObsidian, Color(0xFF0A0F1C), NightObsidian),
              ),
            )
            .padding(innerPadding),
        contentAlignment = Alignment.TopCenter,
      ) {
        when (selectedTab) {
          MainTab.TIMER -> {
            Column(
              modifier =
                Modifier.fillMaxSize()
                  .widthIn(max = 500.dp)
                  .verticalScroll(rememberScrollState())
                  .padding(horizontal = 24.dp, vertical = 16.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
            ) {
          // Top Header & Live Wi-Fi Status Pill + Language Switch
          TopHeaderSection(
            isWifiEnabled = isWifiEnabled,
            isArabic = isArabic,
            str = str,
            onToggleLanguage = { isArabic = !isArabic },
            onWifiPillClick = { openSystemWifiPanel(baseContext) },
          )

          Spacer(modifier = Modifier.height(16.dp))

          // Center Countdown Dial + Fine Adjustment Controls
          CountdownDialSection(
            timerState = timerState,
            str = str,
            onDecreaseMinute = {
              TimerService.adjustIdleDurationByDelta(-TimerService.ONE_MINUTE_MILLIS)
            },
            onIncreaseMinute = {
              TimerService.adjustIdleDurationByDelta(TimerService.ONE_MINUTE_MILLIS)
            },
            onResetTimer = {
              TimerService.resetToDefault()
            },
          )

          Spacer(modifier = Modifier.height(16.dp))

          // Expired Alert Banner when 00:00 is reached
          AnimatedVisibility(
            visible = timerState.isFinished,
            enter = fadeIn(),
            exit = fadeOut(),
          ) {
            TimerFinishedCard(
              str = str,
              requiresPanelPrompt = timerState.requiresPanelPrompt,
              onOpenWifiPanel = {
                TimerService.acknowledgeFinishedPrompt()
                openSystemWifiPanel(baseContext)
              },
            )
          }

          // Preset Time Selection Buttons + Primary Action Button
          Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Row(
              modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = str(R.string.select_duration_label),
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondaryNight,
              )

              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Custom Duration Picker Button
                Surface(
                  modifier =
                    Modifier.minimumInteractiveComponentSize()
                      .clip(RoundedCornerShape(50))
                      .clickable(enabled = !timerState.isRunning) {
                        showCustomDurationDialog = true
                      }
                      .testTag("preset_custom_button"),
                  shape = RoundedCornerShape(50),
                  color = NightSurfaceElevated,
                  border = BorderStroke(1.dp, NightOutline),
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Icon(
                      imageVector = Icons.Filled.HourglassTop,
                      contentDescription = null,
                      tint = SleepAmber,
                      modifier = Modifier.size(14.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = str(R.string.button_custom_time),
                      style = MaterialTheme.typography.labelSmall,
                      color = SleepAmberBright,
                    )
                  }
                }

                // Quick 10s Test chip
                Surface(
                  modifier =
                    Modifier.minimumInteractiveComponentSize()
                      .clip(RoundedCornerShape(50))
                      .clickable(enabled = !timerState.isRunning) {
                        TimerService.selectDuration(10_000L)
                      }
                      .testTag("preset_10s_button"),
                  shape = RoundedCornerShape(50),
                  color =
                    if (!timerState.isRunning && timerState.totalDurationMillis == 10_000L) {
                      SleepAmberDim
                    } else {
                      NightSurfaceElevated
                    },
                  border =
                    BorderStroke(
                      1.dp,
                      if (!timerState.isRunning && timerState.totalDurationMillis == 10_000L) {
                        SleepAmber
                      } else {
                        NightOutline
                      },
                    ),
                ) {
                  Text(
                    text = str(R.string.preset_10_sec_test),
                    style = MaterialTheme.typography.labelSmall,
                    color =
                      if (!timerState.isRunning && timerState.totalDurationMillis == 10_000L) {
                        SleepAmberBright
                      } else {
                        TextSecondaryNight
                      },
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                  )
                }
              }
            }

            FlowRow(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp),
              maxItemsInEachRow = 3,
            ) {
              presets.forEach { preset ->
                val isSelected =
                  !timerState.isRunning &&
                    timerState.remainingMillis == preset.durationMillis &&
                    !timerState.isFinished
                PresetDurationButton(
                  label = str(preset.labelRes),
                  isSelected = isSelected,
                  enabled = !timerState.isRunning,
                  onClick = { TimerService.selectDuration(preset.durationMillis) },
                  modifier = Modifier.weight(1f).testTag(preset.testTag),
                )
              }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Primary Confirm / Start or Active Timer Controls
            if (!timerState.isRunning) {
              Button(
                onClick = onStartTimerClicked,
                modifier =
                  Modifier.fillMaxWidth()
                    .height(58.dp)
                    .testTag("confirm_start_button"),
                shape = RoundedCornerShape(20.dp),
                colors =
                  ButtonDefaults.buttonColors(
                    containerColor = SleepAmber,
                    contentColor = NightObsidian,
                  ),
                elevation =
                  ButtonDefaults.buttonElevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 2.dp,
                  ),
              ) {
                Icon(
                  imageVector = Icons.Filled.PlayArrow,
                  contentDescription = null,
                  modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                  text = str(R.string.button_confirm_start),
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                )
              }
            } else {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                FilledTonalButton(
                  onClick = { addFiveMinutesToService(baseContext) },
                  modifier =
                    Modifier.weight(0.4f)
                      .height(58.dp)
                      .testTag("add_5m_button"),
                  shape = RoundedCornerShape(20.dp),
                  colors =
                    ButtonDefaults.filledTonalButtonColors(
                      containerColor = NightSurfaceElevated,
                      contentColor = SleepAmberBright,
                    ),
                ) {
                  Text(
                    text = str(R.string.button_add_5_min),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                  )
                }

                Button(
                  onClick = { stopTimerForegroundService(baseContext) },
                  modifier =
                    Modifier.weight(0.6f)
                      .height(58.dp)
                      .testTag("cancel_timer_button"),
                  shape = RoundedCornerShape(20.dp),
                  colors =
                    ButtonDefaults.buttonColors(
                      containerColor = AlertCoral,
                      contentColor = NightObsidian,
                    ),
                ) {
                  Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = str(R.string.button_cancel_timer),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(24.dp))

          // Sleep & Battery Savings Card with Toggles
          SleepStatsAndFeaturesCard(
            totalSessions = totalSessions,
            totalMinutesSaved = totalMinutesSaved,
            lastTimestamp = lastTimestamp,
            vibrationEnabled = vibrationEnabled,
            shakeEnabled = shakeEnabled,
            str = str,
            onVibrationToggle = { enabled ->
              vibrationEnabled = enabled
              settingsPrefs.edit().putBoolean(TimerService.KEY_VIBRATION_ENABLED, enabled).apply()
            },
            onShakeToggle = { enabled ->
              shakeEnabled = enabled
              settingsPrefs.edit().putBoolean(TimerService.KEY_SHAKE_ENABLED, enabled).apply()
            },
          )

          Spacer(modifier = Modifier.height(16.dp))

          Text(
            text =
              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                str(R.string.api_q_note)
              } else {
                str(R.string.api_legacy_note)
              },
            style = MaterialTheme.typography.bodyMedium,
            color = TextMutedNight,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
          )
        }
      }
      MainTab.DATA_USAGE -> {
        DataUsageScreen(
          str = str,
        )
      }
    }
  }

      // Custom Time Dialog
      if (showCustomDurationDialog) {
        CustomDurationDialog(
          str = str,
          onDismiss = { showCustomDurationDialog = false },
          onConfirm = { durationMillis ->
            TimerService.selectDuration(durationMillis)
            showCustomDurationDialog = false
          },
        )
      }
    }
  }
}

@Composable
private fun CustomDurationDialog(
  str: (Int) -> String,
  onDismiss: () -> Unit,
  onConfirm: (Long) -> Unit,
) {
  var hours by remember { mutableIntStateOf(1) }
  var minutes by remember { mutableIntStateOf(30) }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = NightSurfaceElevated,
    title = {
      Text(
        text = str(R.string.custom_time_title),
        style = MaterialTheme.typography.titleLarge,
        color = TextPrimaryNight,
      )
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          // Hours Picker Column
          TimeCounterColumn(
            label = str(R.string.custom_hours_label),
            value = hours,
            onDecrement = { if (hours > 0) hours-- },
            onIncrement = { if (hours < 12) hours++ },
          )

          Text(
            text = ":",
            style = MaterialTheme.typography.displayMedium,
            color = SleepAmberBright,
            fontWeight = FontWeight.Bold,
          )

          // Minutes Picker Column
          TimeCounterColumn(
            label = str(R.string.custom_minutes_label),
            value = minutes,
            onDecrement = { if (minutes > 0) minutes = (minutes - 5).coerceAtLeast(0) },
            onIncrement = { if (minutes < 55) minutes = (minutes + 5).coerceAtMost(55) },
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Total calculated preview
        val totalMillis = (hours * 3600L + minutes * 60L) * 1000L
        Text(
          text = String.format(Locale.US, "%02d:%02d:00", hours, minutes),
          style = MaterialTheme.typography.titleMedium,
          color = SleepAmber,
          fontWeight = FontWeight.Bold,
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val totalMillis = ((hours * 3600L + minutes * 60L) * 1000L).coerceAtLeast(60_000L)
          onConfirm(totalMillis)
        },
        colors =
          ButtonDefaults.buttonColors(
            containerColor = SleepAmber,
            contentColor = NightObsidian,
          ),
      ) {
        Text(
          text = str(R.string.custom_set_button),
          fontWeight = FontWeight.Bold,
        )
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(
          text = str(R.string.custom_cancel_button),
          color = TextSecondaryNight,
        )
      }
    },
  )
}

@Composable
private fun TimeCounterColumn(
  label: String,
  value: Int,
  onDecrement: () -> Unit,
  onIncrement: () -> Unit,
) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelMedium,
      color = TextSecondaryNight,
    )
    Spacer(modifier = Modifier.height(6.dp))
    FilledTonalIconButton(
      onClick = onIncrement,
      modifier = Modifier.size(40.dp),
      colors =
        IconButtonDefaults.filledTonalIconButtonColors(
          containerColor = NightSurface,
          contentColor = SleepAmberBright,
        ),
    ) {
      Icon(Icons.Filled.Add, contentDescription = null)
    }
    Text(
      text = String.format(Locale.US, "%02d", value),
      style = MaterialTheme.typography.headlineMedium,
      color = TextPrimaryNight,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(vertical = 4.dp),
    )
    FilledTonalIconButton(
      onClick = onDecrement,
      modifier = Modifier.size(40.dp),
      colors =
        IconButtonDefaults.filledTonalIconButtonColors(
          containerColor = NightSurface,
          contentColor = SleepAmberBright,
        ),
    ) {
      Icon(Icons.Filled.Remove, contentDescription = null)
    }
  }
}

@Composable
private fun SleepStatsAndFeaturesCard(
  totalSessions: Int,
  totalMinutesSaved: Long,
  lastTimestamp: Long,
  vibrationEnabled: Boolean,
  shakeEnabled: Boolean,
  str: (Int) -> String,
  onVibrationToggle: (Boolean) -> Unit,
  onShakeToggle: (Boolean) -> Unit,
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = NightSurfaceElevated),
    border = BorderStroke(1.dp, NightOutline),
  ) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Icon(
          imageVector = Icons.Filled.BatteryChargingFull,
          contentDescription = null,
          tint = SleepAmber,
          modifier = Modifier.size(20.dp),
        )
        Text(
          text = str(R.string.stats_title),
          style = MaterialTheme.typography.titleMedium,
          color = TextPrimaryNight,
          fontWeight = FontWeight.Bold,
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Stats Badges Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        // Sessions Badge
        Surface(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(14.dp),
          color = NightSurface,
          border = BorderStroke(1.dp, NightOutline),
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Text(
              text = str(R.string.stats_sessions_label),
              style = MaterialTheme.typography.labelSmall,
              color = TextSecondaryNight,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "$totalSessions",
              style = MaterialTheme.typography.titleLarge,
              color = SleepAmberBright,
              fontWeight = FontWeight.Bold,
            )
          }
        }

        // Est Battery Saved Badge
        val hoursSaved = (totalMinutesSaved / 60.0)
        val formattedHours = String.format(Locale.US, "%.1fh", hoursSaved)
        Surface(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(14.dp),
          color = NightSurface,
          border = BorderStroke(1.dp, NightOutline),
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Text(
              text = str(R.string.stats_battery_saved),
              style = MaterialTheme.typography.labelSmall,
              color = TextSecondaryNight,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = formattedHours,
              style = MaterialTheme.typography.titleLarge,
              color = WifiActiveGreen,
              fontWeight = FontWeight.Bold,
            )
          }
        }
      }

      // Last Shutdown Time
      if (lastTimestamp > 0L) {
        Spacer(modifier = Modifier.height(8.dp))
        val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(lastTimestamp))
        Text(
          text = "${str(R.string.stats_last_time)}: $dateStr",
          style = MaterialTheme.typography.bodySmall,
          color = TextMutedNight,
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Toggles
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.weight(1f),
        ) {
          Icon(
            imageVector = Icons.Filled.Vibration,
            contentDescription = null,
            tint = TextSecondaryNight,
            modifier = Modifier.size(18.dp),
          )
          Text(
            text = str(R.string.feature_vibrate_label),
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimaryNight,
          )
        }
        Switch(
          checked = vibrationEnabled,
          onCheckedChange = onVibrationToggle,
          colors =
            SwitchDefaults.colors(
              checkedThumbColor = SleepAmber,
              checkedTrackColor = SleepAmberDim,
            ),
        )
      }

      Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.weight(1f),
        ) {
          Icon(
            imageVector = Icons.Filled.ScreenRotation,
            contentDescription = null,
            tint = TextSecondaryNight,
            modifier = Modifier.size(18.dp),
          )
          Text(
            text = str(R.string.feature_shake_label),
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimaryNight,
          )
        }
        Switch(
          checked = shakeEnabled,
          onCheckedChange = onShakeToggle,
          colors =
            SwitchDefaults.colors(
              checkedThumbColor = SleepAmber,
              checkedTrackColor = SleepAmberDim,
            ),
        )
      }
    }
  }
}

@Composable
private fun TopHeaderSection(
  isWifiEnabled: Boolean,
  isArabic: Boolean,
  str: (Int) -> String,
  onToggleLanguage: () -> Unit,
  onWifiPillClick: () -> Unit,
) {
  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Surface(
          shape = CircleShape,
          color = SleepAmberDim,
          modifier = Modifier.size(42.dp),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Filled.Bedtime,
              contentDescription = null,
              tint = SleepAmber,
              modifier = Modifier.size(22.dp),
            )
          }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(
            text = str(R.string.title_wifi_sleep_timer),
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimaryNight,
          )
          Text(
            text = str(R.string.subtitle_night_mode_timer),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondaryNight,
          )
        }
      }

      // Language Switch Pill (عربي / EN)
      Surface(
        modifier =
          Modifier.minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onToggleLanguage)
            .testTag("language_toggle_button"),
        shape = RoundedCornerShape(50),
        color = NightSurfaceElevated,
        border = BorderStroke(1.dp, NightOutline),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(
            imageVector = Icons.Filled.Language,
            contentDescription = "Switch Language",
            tint = SleepAmber,
            modifier = Modifier.size(15.dp),
          )
          Spacer(modifier = Modifier.width(5.dp))
          Text(
            text = if (isArabic) "EN" else "عربي",
            style = MaterialTheme.typography.labelLarge,
            color = TextPrimaryNight,
          )
        }
      }
    }

    // Current Wi-Fi State Badge
    val wifiStatusDesc = str(R.string.cd_wifi_status)
    Surface(
      modifier =
        Modifier.minimumInteractiveComponentSize()
          .clip(RoundedCornerShape(50))
          .clickable(onClick = onWifiPillClick)
          .semantics { contentDescription = wifiStatusDesc }
          .testTag("wifi_status_badge"),
      shape = RoundedCornerShape(50),
      color = NightSurfaceElevated,
      border =
        BorderStroke(
          1.dp,
          if (isWifiEnabled) WifiActiveGreen.copy(alpha = 0.45f) else NightOutline,
        ),
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Icon(
          imageVector = if (isWifiEnabled) Icons.Filled.Wifi else Icons.Filled.WifiOff,
          contentDescription = null,
          tint = if (isWifiEnabled) WifiActiveGreen else TextMutedNight,
          modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text =
            if (isWifiEnabled) {
              str(R.string.wifi_status_on)
            } else {
              str(R.string.wifi_status_off)
            },
          style = MaterialTheme.typography.labelLarge,
          color = if (isWifiEnabled) TextPrimaryNight else TextSecondaryNight,
        )
      }
    }
  }
}

@Composable
private fun CountdownDialSection(
  timerState: TimerUiState,
  str: (Int) -> String,
  onDecreaseMinute: () -> Unit,
  onIncreaseMinute: () -> Unit,
  onResetTimer: () -> Unit,
) {
  val animatedProgress by
    animateFloatAsState(
      targetValue = if (timerState.isFinished) 0f else timerState.progressFraction,
      animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
      label = "countdown_ring_progress",
    )

  val dialColor by
    animateColorAsState(
      targetValue =
        when {
          timerState.isFinished -> AlertCoral
          timerState.isRunning -> SleepAmber
          else -> SleepAmber.copy(alpha = 0.85f)
        },
      label = "dial_accent_color",
    )

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Box(
      modifier = Modifier.size(270.dp),
      contentAlignment = Alignment.Center,
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeWidthPx = 12.dp.toPx()
        val diameter = size.minDimension - strokeWidthPx * 2f
        val topLeftOffset =
          Offset(
            x = (size.width - diameter) / 2f,
            y = (size.height - diameter) / 2f,
          )
        val arcSize = Size(diameter, diameter)

        // Subtle outer tick marks
        val center = Offset(size.width / 2f, size.height / 2f)
        val outerRadius = diameter / 2f + strokeWidthPx * 0.9f
        val innerTickRadius = diameter / 2f + strokeWidthPx * 0.45f
        for (i in 0 until 60) {
          val angleRad = Math.toRadians((i * 6 - 90).toDouble())
          val isMajor = i % 5 == 0
          val startR = if (isMajor) innerTickRadius - 2.dp.toPx() else innerTickRadius
          val start =
            Offset(
              x = center.x + (startR * cos(angleRad)).toFloat(),
              y = center.y + (startR * sin(angleRad)).toFloat(),
            )
          val end =
            Offset(
              x = center.x + (outerRadius * cos(angleRad)).toFloat(),
              y = center.y + (outerRadius * sin(angleRad)).toFloat(),
            )
          drawLine(
            color =
              if (isMajor) NightOutline.copy(alpha = 0.9f)
              else NightOutline.copy(alpha = 0.35f),
            start = start,
            end = end,
            strokeWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx(),
            cap = StrokeCap.Round,
          )
        }

        // Background track ring
        drawArc(
          color = NightSurfaceElevated,
          startAngle = -90f,
          sweepAngle = 360f,
          useCenter = false,
          topLeft = topLeftOffset,
          size = arcSize,
          style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
        )

        // Active countdown arc
        val sweep = (animatedProgress * 360f).coerceIn(0f, 360f)
        if (sweep > 0.5f) {
          drawArc(
            color = dialColor,
            startAngle = -90f,
            sweepAngle = sweep,
            useCenter = false,
            topLeft = topLeftOffset,
            size = arcSize,
            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
          )
        }
      }

      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
      ) {
        Text(
          text =
            when {
              timerState.isFinished -> str(R.string.status_completed)
              timerState.isRunning -> str(R.string.status_counting_down)
              else -> str(R.string.status_ready)
            },
          style = MaterialTheme.typography.labelSmall,
          color =
            when {
              timerState.isFinished -> AlertCoral
              timerState.isRunning -> SleepAmberBright
              else -> TextSecondaryNight
            },
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Always display digits LTR (MM:SS)
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
          Text(
            text = timerState.formattedTime,
            style = MaterialTheme.typography.displayLarge,
            color = TextPrimaryNight,
            modifier = Modifier.testTag("countdown_display"),
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Fine-tune -1m / +1m or Reset controls when timer is not actively running
        if (!timerState.isRunning) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            FilledTonalIconButton(
              onClick = onDecreaseMinute,
              modifier =
                Modifier.size(48.dp)
                  .testTag("decrease_time_button"),
              colors =
                IconButtonDefaults.filledTonalIconButtonColors(
                  containerColor = NightSurfaceElevated,
                  contentColor = TextPrimaryNight,
                ),
            ) {
              Icon(
                imageVector = Icons.Filled.Remove,
                contentDescription = str(R.string.cd_decrease_minute),
              )
            }

            if (timerState.isFinished || timerState.remainingMillis != TimerService.DEFAULT_DURATION_MILLIS) {
              OutlinedButton(
                onClick = onResetTimer,
                modifier =
                  Modifier.height(48.dp)
                    .testTag("reset_timer_button"),
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, NightOutline),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
              ) {
                Icon(
                  imageVector = Icons.Filled.Refresh,
                  contentDescription = null,
                  tint = TextSecondaryNight,
                  modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = str(R.string.button_reset_timer),
                  style = MaterialTheme.typography.labelLarge,
                  color = TextSecondaryNight,
                )
              }
            }

            FilledTonalIconButton(
              onClick = onIncreaseMinute,
              modifier =
                Modifier.size(48.dp)
                  .testTag("increase_time_button"),
              colors =
                IconButtonDefaults.filledTonalIconButtonColors(
                  containerColor = NightSurfaceElevated,
                  contentColor = TextPrimaryNight,
                ),
            ) {
              Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = str(R.string.cd_increase_minute),
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun TimerFinishedCard(
  str: (Int) -> String,
  requiresPanelPrompt: Boolean,
  onOpenWifiPanel: () -> Unit,
) {
  Card(
    modifier =
      Modifier.fillMaxWidth()
        .padding(bottom = 16.dp),
    shape = RoundedCornerShape(18.dp),
    colors =
      CardDefaults.cardColors(
        containerColor = NightSurfaceElevated,
      ),
    border = BorderStroke(1.dp, SleepAmber.copy(alpha = 0.6f)),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
      ) {
        Icon(
          imageVector = Icons.Filled.WifiOff,
          contentDescription = null,
          tint = SleepAmberBright,
          modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = str(R.string.notification_title_finished),
          style = MaterialTheme.typography.titleMedium,
          color = SleepAmberBright,
        )
      }
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = str(R.string.notification_body_finished_disabled),
        style = MaterialTheme.typography.bodyMedium,
        color = TextSecondaryNight,
        textAlign = TextAlign.Center,
      )
      if (requiresPanelPrompt) {
        Spacer(modifier = Modifier.height(12.dp))
        Button(
          onClick = onOpenWifiPanel,
          modifier =
            Modifier.fillMaxWidth()
              .height(48.dp)
              .testTag("open_wifi_panel_button"),
          shape = RoundedCornerShape(12.dp),
          colors =
            ButtonDefaults.buttonColors(
              containerColor = SleepAmber,
              contentColor = NightObsidian,
            ),
        ) {
          Icon(
            imageVector = Icons.Filled.WifiOff,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = str(R.string.button_open_wifi_panel),
            style = MaterialTheme.typography.labelLarge,
          )
        }
      }
    }
  }
}

@Composable
private fun PresetDurationButton(
  label: String,
  isSelected: Boolean,
  enabled: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val containerColor =
    when {
      isSelected -> SleepAmberDim
      else -> NightSurface
    }
  val borderColor =
    when {
      isSelected -> SleepAmber
      else -> NightOutline
    }
  val textColor =
    when {
      !enabled -> TextMutedNight
      isSelected -> SleepAmberBright
      else -> TextPrimaryNight
    }

  OutlinedButton(
    onClick = onClick,
    enabled = enabled,
    modifier = modifier.height(48.dp),
    shape = RoundedCornerShape(14.dp),
    border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor),
    colors =
      ButtonDefaults.outlinedButtonColors(
        containerColor = containerColor,
        contentColor = textColor,
        disabledContainerColor = NightSurface.copy(alpha = 0.5f),
        disabledContentColor = TextMutedNight,
      ),
    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelMedium,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
    )
  }
}

private fun startTimerForegroundService(context: Context, durationMillis: Long) {
  val serviceIntent =
    Intent(context, TimerService::class.java).apply {
      action = TimerService.ACTION_START
      putExtra(TimerService.EXTRA_DURATION_MILLIS, durationMillis)
    }
  ContextCompat.startForegroundService(context, serviceIntent)
}

private fun stopTimerForegroundService(context: Context) {
  val serviceIntent =
    Intent(context, TimerService::class.java).apply {
      action = TimerService.ACTION_STOP
    }
  context.startService(serviceIntent)
}

private fun addFiveMinutesToService(context: Context) {
  val serviceIntent =
    Intent(context, TimerService::class.java).apply {
      action = TimerService.ACTION_ADD_TIME
      putExtra(TimerService.EXTRA_ADD_MILLIS, TimerService.FIVE_MINUTES_MILLIS)
    }
  context.startService(serviceIntent)
}

private fun openSystemWifiPanel(context: Context) {
  TimerService.disableWifiOrPrompt(context)
}

private fun checkWifiEnabled(context: Context): Boolean {
  return try {
    val wifiManager =
      context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
    wifiManager?.isWifiEnabled == true
  } catch (_: Exception) {
    false
  }
}

@Preview(showBackground = true)
@Composable
fun WifiSleepTimerPreview() {
  MyApplicationTheme {
    WifiSleepTimerScreen()
  }
}
