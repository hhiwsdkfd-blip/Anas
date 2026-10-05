package com.example

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AlertCoral
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

@Composable
fun SetupScreen(
  str: (Int) -> String,
  onOpenSettingsClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val prefs = remember(context) {
    context.getSharedPreferences("data_monitor_setup_prefs", Context.MODE_PRIVATE)
  }

  // State variables from Screenshot 1
  var widgetEnabled by remember {
    mutableStateOf(prefs.getBoolean("setup_widget_enabled", true))
  }
  var notificationEnabled by remember {
    mutableStateOf(prefs.getBoolean("setup_notification_enabled", true))
  }
  var liveSpeedEnabled by remember {
    mutableStateOf(prefs.getBoolean("setup_live_speed_enabled", true))
  }

  // Widget settings
  var widgetIntervalMinutes by remember {
    mutableIntStateOf(prefs.getInt("widget_interval", 1))
  }
  var showRemainingData by remember {
    mutableStateOf(prefs.getBoolean("widget_show_remaining", true))
  }
  var showWifiUsageInWidget by remember {
    mutableStateOf(prefs.getBoolean("widget_show_wifi", true))
  }

  // Notification settings
  var notifIntervalMinutes by remember {
    mutableIntStateOf(prefs.getInt("notif_interval", 1))
  }
  var showMobileUsageInNotif by remember {
    mutableStateOf(prefs.getBoolean("notif_show_mobile", true))
  }
  var showWifiUsageInNotif by remember {
    mutableStateOf(prefs.getBoolean("notif_show_wifi", true))
  }
  var alwaysShowTotal by remember {
    mutableStateOf(prefs.getBoolean("notif_always_total", true))
  }
  var autoHideSpeedWhenDisconnected by remember {
    mutableStateOf(prefs.getBoolean("notif_auto_hide_speed", true))
  }
  var combineNotifications by remember {
    mutableStateOf(prefs.getBoolean("notif_combine_all", true))
  }
  var showOnLockscreen by remember {
    mutableStateOf(prefs.getBoolean("notif_lockscreen", true))
  }

  // Data Consumption Alert
  var alertEnabled by remember {
    mutableStateOf(prefs.getBoolean("alert_enabled", true))
  }
  var alertThresholdPercent by remember {
    mutableIntStateOf(prefs.getInt("alert_threshold_percent", 50))
  }

  // General Settings
  var dataPlanGigabytes by remember {
    mutableStateOf(prefs.getString("data_plan_gb", "20") ?: "20")
  }
  var resetTimeStr by remember {
    mutableStateOf(prefs.getString("data_reset_time", "12:00 am") ?: "12:00 am")
  }
  var smartAllocationEnabled by remember {
    mutableStateOf(prefs.getBoolean("smart_allocation_enabled", true))
  }

  // Dialog states
  var showPlanDialog by remember { mutableStateOf(false) }
  var showThresholdDialog by remember { mutableStateOf(false) }
  var showIntervalDialog by remember { mutableStateOf(false) }
  var showResetTimeDialog by remember { mutableStateOf(false) }

  Column(
    modifier =
      modifier
        .fillMaxSize()
        .background(NightObsidian)
        .verticalScroll(rememberScrollState())
        .padding(bottom = 24.dp),
  ) {
    // Top Bar
    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = str(R.string.setup_screen_title),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = TextPrimaryNight,
      )
      IconButton(
        onClick = onOpenSettingsClick,
        modifier = Modifier.testTag("setup_gear_settings_button"),
      ) {
        Icon(
          imageVector = Icons.Filled.Settings,
          contentDescription = str(R.string.settings_title),
          tint = SleepAmber,
        )
      }
    }

    // Top Preview Banner with Phones Mockup from Screenshot 1
    SetupPreviewBanner(str = str)

    Spacer(modifier = Modifier.height(16.dp))

    // Section 1: إعداد مراقب البيانات (Data Monitor Setup)
    SetupSectionHeader(title = str(R.string.setup_section_monitor_header))

    SetupActionRow(
      title = str(R.string.setup_widget_config),
      icon = Icons.Filled.Widgets,
      iconTint = WifiActiveGreen,
      onClick = {
        Toast.makeText(context, str(R.string.setup_widget_toast), Toast.LENGTH_SHORT).show()
      },
    )

    SetupToggleRow(
      title = str(R.string.setup_notif_config),
      icon = Icons.Filled.Notifications,
      iconTint = SleepAmber,
      checked = notificationEnabled,
      onCheckedChange = { checked ->
        notificationEnabled = checked
        prefs.edit().putBoolean("setup_notification_enabled", checked).apply()
      },
    )

    SetupToggleRow(
      title = str(R.string.setup_live_speed),
      icon = Icons.Filled.SwapVert,
      iconTint = Color(0xFF64B5F6),
      checked = liveSpeedEnabled,
      onCheckedChange = { checked ->
        liveSpeedEnabled = checked
        prefs.edit().putBoolean("setup_live_speed_enabled", checked).apply()
      },
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Section 2: الأداة (The Widget)
    SetupSectionHeader(title = str(R.string.setup_section_widget))

    SetupValueRow(
      title = str(R.string.setup_widget_interval_title),
      value = "$widgetIntervalMinutes ${str(R.string.setup_minute_unit)}",
      onClick = { showIntervalDialog = true },
    )

    SetupToggleRow(
      title = str(R.string.setup_show_remaining_data),
      checked = showRemainingData,
      onCheckedChange = { checked ->
        showRemainingData = checked
        prefs.edit().putBoolean("widget_show_remaining", checked).apply()
      },
    )

    SetupToggleRow(
      title = str(R.string.setup_show_wifi_in_widget),
      checked = showWifiUsageInWidget,
      onCheckedChange = { checked ->
        showWifiUsageInWidget = checked
        prefs.edit().putBoolean("widget_show_wifi", checked).apply()
      },
    )

    SetupSimpleActionRow(
      title = str(R.string.setup_refresh_widget_title),
      subtitle = str(R.string.setup_refresh_widget_desc),
      onClick = {
        Toast.makeText(context, str(R.string.setup_refresh_widget_done), Toast.LENGTH_SHORT).show()
      },
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Section 3: إشعار (Notification)
    SetupSectionHeader(title = str(R.string.setup_section_notification))

    SetupValueRow(
      title = str(R.string.setup_notif_interval_title),
      value = "$notifIntervalMinutes ${str(R.string.setup_minute_unit)}",
      onClick = { showIntervalDialog = true },
    )

    SetupToggleRow(
      title = str(R.string.setup_show_mobile_data),
      checked = showMobileUsageInNotif,
      onCheckedChange = { checked ->
        showMobileUsageInNotif = checked
        prefs.edit().putBoolean("notif_show_mobile", checked).apply()
      },
    )

    SetupToggleRow(
      title = str(R.string.setup_show_wifi_in_notif),
      checked = showWifiUsageInNotif,
      onCheckedChange = { checked ->
        showWifiUsageInNotif = checked
        prefs.edit().putBoolean("notif_show_wifi", checked).apply()
      },
    )

    SetupToggleRow(
      title = str(R.string.setup_always_show_total_title),
      subtitle = str(R.string.setup_always_show_total_desc),
      checked = alwaysShowTotal,
      onCheckedChange = { checked ->
        alwaysShowTotal = checked
        prefs.edit().putBoolean("notif_always_total", checked).apply()
      },
    )

    SetupToggleRow(
      title = str(R.string.setup_auto_hide_speed_title),
      subtitle = str(R.string.setup_auto_hide_speed_desc),
      checked = autoHideSpeedWhenDisconnected,
      onCheckedChange = { checked ->
        autoHideSpeedWhenDisconnected = checked
        prefs.edit().putBoolean("notif_auto_hide_speed", checked).apply()
      },
    )

    SetupToggleRow(
      title = str(R.string.setup_combine_notifs_title),
      subtitle = str(R.string.setup_combine_notifs_desc),
      checked = combineNotifications,
      onCheckedChange = { checked ->
        combineNotifications = checked
        prefs.edit().putBoolean("notif_combine_all", checked).apply()
      },
    )

    SetupActionRow(
      title = str(R.string.setup_combined_icon_title),
      subtitle = str(R.string.setup_combined_icon_desc),
      onClick = {
        Toast.makeText(context, str(R.string.setup_icon_selected_toast), Toast.LENGTH_SHORT).show()
      },
    )

    SetupToggleRow(
      title = str(R.string.setup_lockscreen_notif_title),
      checked = showOnLockscreen,
      onCheckedChange = { checked ->
        showOnLockscreen = checked
        prefs.edit().putBoolean("notif_lockscreen", checked).apply()
      },
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Section 4: راقب استهلاك البيانات (Monitor Data Consumption)
    SetupSectionHeader(title = str(R.string.setup_section_consumption))

    SetupToggleRow(
      title = str(R.string.setup_consumption_alert_title),
      subtitle = str(R.string.setup_consumption_alert_desc),
      checked = alertEnabled,
      onCheckedChange = { checked ->
        alertEnabled = checked
        prefs.edit().putBoolean("alert_enabled", checked).apply()
      },
    )

    SetupValueRow(
      title = str(R.string.setup_warning_level_title),
      subtitle = "${str(R.string.setup_warning_at)} $alertThresholdPercent% ${str(R.string.setup_of_data)}",
      onClick = { showThresholdDialog = true },
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Section 5: عام (General)
    SetupSectionHeader(title = str(R.string.setup_section_general))

    SetupActionRow(
      title = str(R.string.setup_add_data_plan_title),
      subtitle = "${str(R.string.setup_current_plan)}: $dataPlanGigabytes GB",
      onClick = { showPlanDialog = true },
    )

    SetupValueRow(
      title = str(R.string.setup_reset_time_title),
      value = resetTimeStr,
      onClick = { showResetTimeDialog = true },
    )

    SetupToggleRow(
      title = str(R.string.setup_smart_allocation_title),
      subtitle = str(R.string.setup_smart_allocation_desc),
      checked = smartAllocationEnabled,
      onCheckedChange = { checked ->
        smartAllocationEnabled = checked
        prefs.edit().putBoolean("smart_allocation_enabled", checked).apply()
      },
    )

    SetupActionRow(
      title = str(R.string.setup_exclude_apps_title),
      subtitle = str(R.string.setup_exclude_apps_desc),
      onClick = {
        Toast.makeText(context, str(R.string.setup_exclude_apps_toast), Toast.LENGTH_SHORT).show()
      },
    )
  }

  // Dialogs
  if (showPlanDialog) {
    var tempPlan by remember { mutableStateOf(dataPlanGigabytes) }
    AlertDialog(
      onDismissRequest = { showPlanDialog = false },
      containerColor = NightSurfaceElevated,
      title = {
        Text(
          text = str(R.string.setup_add_data_plan_title),
          color = TextPrimaryNight,
          fontWeight = FontWeight.Bold,
        )
      },
      text = {
        Column {
          Text(
            text = str(R.string.setup_plan_dialog_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondaryNight,
          )
          Spacer(modifier = Modifier.height(12.dp))
          OutlinedTextField(
            value = tempPlan,
            onValueChange = { tempPlan = it.filter { ch -> ch.isDigit() } },
            label = { Text("GB") },
            colors =
              OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimaryNight,
                unfocusedTextColor = TextPrimaryNight,
                focusedBorderColor = SleepAmber,
                unfocusedBorderColor = NightOutline,
              ),
            modifier = Modifier.fillMaxWidth(),
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            dataPlanGigabytes = tempPlan.ifEmpty { "20" }
            prefs.edit().putString("data_plan_gb", dataPlanGigabytes).apply()
            showPlanDialog = false
            Toast.makeText(context, str(R.string.setup_plan_saved_toast), Toast.LENGTH_SHORT).show()
          },
          colors = ButtonDefaults.buttonColors(containerColor = SleepAmber, contentColor = NightObsidian),
        ) {
          Text(text = str(R.string.custom_set_button), fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showPlanDialog = false }) {
          Text(text = str(R.string.custom_cancel_button), color = TextSecondaryNight)
        }
      },
    )
  }

  if (showThresholdDialog) {
    val thresholds = listOf(25, 50, 75, 90)
    AlertDialog(
      onDismissRequest = { showThresholdDialog = false },
      containerColor = NightSurfaceElevated,
      title = {
        Text(
          text = str(R.string.setup_warning_level_title),
          color = TextPrimaryNight,
          fontWeight = FontWeight.Bold,
        )
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          thresholds.forEach { pct ->
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (alertThresholdPercent == pct) SleepAmberDim else NightSurface,
              border = BorderStroke(1.dp, if (alertThresholdPercent == pct) SleepAmber else NightOutline),
              modifier =
                Modifier
                  .fillMaxWidth()
                  .clickable {
                    alertThresholdPercent = pct
                    prefs.edit().putInt("alert_threshold_percent", pct).apply()
                    showThresholdDialog = false
                  },
            ) {
              Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Text(
                  text = "$pct%",
                  style = MaterialTheme.typography.titleMedium,
                  color = if (alertThresholdPercent == pct) SleepAmberBright else TextPrimaryNight,
                  fontWeight = FontWeight.Bold,
                )
                if (alertThresholdPercent == pct) {
                  Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = SleepAmber,
                  )
                }
              }
            }
          }
        }
      },
      confirmButton = {},
      dismissButton = {
        TextButton(onClick = { showThresholdDialog = false }) {
          Text(text = str(R.string.custom_cancel_button), color = TextSecondaryNight)
        }
      },
    )
  }

  if (showIntervalDialog) {
    val intervals = listOf(1, 2, 5, 10, 15)
    AlertDialog(
      onDismissRequest = { showIntervalDialog = false },
      containerColor = NightSurfaceElevated,
      title = {
        Text(
          text = str(R.string.setup_select_interval),
          color = TextPrimaryNight,
          fontWeight = FontWeight.Bold,
        )
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          intervals.forEach { mins ->
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (widgetIntervalMinutes == mins) SleepAmberDim else NightSurface,
              border = BorderStroke(1.dp, if (widgetIntervalMinutes == mins) SleepAmber else NightOutline),
              modifier =
                Modifier
                  .fillMaxWidth()
                  .clickable {
                    widgetIntervalMinutes = mins
                    notifIntervalMinutes = mins
                    prefs.edit().putInt("widget_interval", mins).putInt("notif_interval", mins).apply()
                    showIntervalDialog = false
                  },
            ) {
              Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Text(
                  text = "$mins ${str(R.string.setup_minute_unit)}",
                  style = MaterialTheme.typography.titleMedium,
                  color = if (widgetIntervalMinutes == mins) SleepAmberBright else TextPrimaryNight,
                  fontWeight = FontWeight.Bold,
                )
                if (widgetIntervalMinutes == mins) {
                  Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = SleepAmber,
                  )
                }
              }
            }
          }
        }
      },
      confirmButton = {},
      dismissButton = {
        TextButton(onClick = { showIntervalDialog = false }) {
          Text(text = str(R.string.custom_cancel_button), color = TextSecondaryNight)
        }
      },
    )
  }

  if (showResetTimeDialog) {
    val times = listOf("12:00 am", "06:00 am", "12:00 pm", "06:00 pm")
    AlertDialog(
      onDismissRequest = { showResetTimeDialog = false },
      containerColor = NightSurfaceElevated,
      title = {
        Text(
          text = str(R.string.setup_reset_time_title),
          color = TextPrimaryNight,
          fontWeight = FontWeight.Bold,
        )
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          times.forEach { t ->
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (resetTimeStr == t) SleepAmberDim else NightSurface,
              border = BorderStroke(1.dp, if (resetTimeStr == t) SleepAmber else NightOutline),
              modifier =
                Modifier
                  .fillMaxWidth()
                  .clickable {
                    resetTimeStr = t
                    prefs.edit().putString("data_reset_time", t).apply()
                    showResetTimeDialog = false
                  },
            ) {
              Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Text(
                  text = t,
                  style = MaterialTheme.typography.titleMedium,
                  color = if (resetTimeStr == t) SleepAmberBright else TextPrimaryNight,
                  fontWeight = FontWeight.Bold,
                )
                if (resetTimeStr == t) {
                  Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = SleepAmber,
                  )
                }
              }
            }
          }
        }
      },
      confirmButton = {},
      dismissButton = {
        TextButton(onClick = { showResetTimeDialog = false }) {
          Text(text = str(R.string.custom_cancel_button), color = TextSecondaryNight)
        }
      },
    )
  }
}

@Composable
private fun SetupPreviewBanner(str: (Int) -> String) {
  Card(
    modifier =
      Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = NightSurfaceElevated),
    border = BorderStroke(1.dp, NightOutline),
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      // Mockup Preview Visual representation matching Screenshot 1
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        // Widget preview card
        Surface(
          modifier = Modifier.width(135.dp).height(100.dp),
          shape = RoundedCornerShape(16.dp),
          color = Color(0xFF1E2128),
          border = BorderStroke(1.dp, Color(0xFF333842)),
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              Surface(shape = CircleShape, color = AlertCoral, modifier = Modifier.size(8.dp)) {}
              Text(
                text = "Data usage",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondaryNight,
                fontSize = 9.sp,
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Mobile: 1.05 GB",
              style = MaterialTheme.typography.labelSmall,
              color = TextPrimaryNight,
              fontWeight = FontWeight.Bold,
              fontSize = 10.sp,
            )
            Text(
              text = "Remaining: 18.9 GB",
              style = MaterialTheme.typography.labelSmall,
              color = TextMutedNight,
              fontSize = 8.sp,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Wifi: 6.9 GB",
              style = MaterialTheme.typography.labelSmall,
              color = WifiActiveGreen,
              fontWeight = FontWeight.Bold,
              fontSize = 10.sp,
            )
          }
        }

        // Notification preview card
        Surface(
          modifier = Modifier.width(150.dp).height(100.dp),
          shape = RoundedCornerShape(16.dp),
          color = Color(0xFF15181E),
          border = BorderStroke(1.dp, Color(0xFF333842)),
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth(),
            ) {
              Text(text = "9:30", color = TextSecondaryNight, fontSize = 9.sp)
              Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Filled.Wifi, contentDescription = null, tint = SleepAmber, modifier = Modifier.size(10.dp))
                Icon(Icons.Filled.CellTower, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
              }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color(0xFF222630),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Column(modifier = Modifier.padding(6.dp)) {
                Text(
                  text = "Data usage today",
                  color = TextPrimaryNight,
                  fontSize = 9.sp,
                  fontWeight = FontWeight.SemiBold,
                )
                Text(
                  text = "Wifi: 6.46 GB • 420 KB/s",
                  color = WifiActiveGreen,
                  fontSize = 8.sp,
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = str(R.string.setup_banner_quote),
        style = MaterialTheme.typography.bodySmall,
        color = TextSecondaryNight,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 8.dp),
      )
    }
  }
}

@Composable
private fun SetupSectionHeader(title: String) {
  Text(
    text = title,
    style = MaterialTheme.typography.labelLarge,
    color = Color(0xFF64B5F6), // Sky blue header color from screenshot
    fontWeight = FontWeight.Bold,
    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
  )
}

@Composable
private fun SetupToggleRow(
  title: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  subtitle: String? = null,
  icon: ImageVector? = null,
  iconTint: Color = SleepAmber,
) {
  Row(
    modifier =
      Modifier
        .fillMaxWidth()
        .clickable { onCheckedChange(!checked) }
        .padding(horizontal = 16.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(14.dp),
      modifier = Modifier.weight(1f),
    ) {
      if (icon != null) {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = iconTint.copy(alpha = 0.15f),
          modifier = Modifier.size(34.dp),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = icon,
              contentDescription = null,
              tint = iconTint,
              modifier = Modifier.size(20.dp),
            )
          }
        }
      }
      Column {
        Text(
          text = title,
          style = MaterialTheme.typography.bodyLarge,
          color = TextPrimaryNight,
          fontWeight = FontWeight.Normal,
        )
        if (!subtitle.isNullOrEmpty()) {
          Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = TextMutedNight,
            fontSize = 11.sp,
          )
        }
      }
    }

    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      colors =
        SwitchDefaults.colors(
          checkedThumbColor = Color.White,
          checkedTrackColor = Color(0xFF1E88E5), // Bright blue switch from screenshot
          uncheckedThumbColor = TextMutedNight,
          uncheckedTrackColor = NightSurface,
        ),
    )
  }
}

@Composable
private fun SetupActionRow(
  title: String,
  onClick: () -> Unit,
  subtitle: String? = null,
  icon: ImageVector? = null,
  iconTint: Color = SleepAmber,
) {
  Row(
    modifier =
      Modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
        .padding(horizontal = 16.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(14.dp),
      modifier = Modifier.weight(1f),
    ) {
      if (icon != null) {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = iconTint.copy(alpha = 0.15f),
          modifier = Modifier.size(34.dp),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = icon,
              contentDescription = null,
              tint = iconTint,
              modifier = Modifier.size(20.dp),
            )
          }
        }
      }
      Column {
        Text(
          text = title,
          style = MaterialTheme.typography.bodyLarge,
          color = TextPrimaryNight,
          fontWeight = FontWeight.Normal,
        )
        if (!subtitle.isNullOrEmpty()) {
          Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = TextMutedNight,
            fontSize = 11.sp,
          )
        }
      }
    }

    Icon(
      imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
      contentDescription = null,
      tint = TextMutedNight,
      modifier = Modifier.size(16.dp),
    )
  }
}

@Composable
private fun SetupValueRow(
  title: String,
  onClick: () -> Unit,
  value: String? = null,
  subtitle: String? = null,
) {
  Row(
    modifier =
      Modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
        .padding(horizontal = 16.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = MaterialTheme.typography.bodyLarge,
        color = TextPrimaryNight,
        fontWeight = FontWeight.Normal,
      )
      if (!subtitle.isNullOrEmpty()) {
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = TextMutedNight,
          fontSize = 11.sp,
        )
      }
      if (!value.isNullOrEmpty()) {
        Text(
          text = value,
          style = MaterialTheme.typography.bodySmall,
          color = TextSecondaryNight,
          fontSize = 12.sp,
        )
      }
    }

    Icon(
      imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
      contentDescription = null,
      tint = TextMutedNight,
      modifier = Modifier.size(16.dp),
    )
  }
}

@Composable
private fun SetupSimpleActionRow(
  title: String,
  subtitle: String,
  onClick: () -> Unit,
) {
  Column(
    modifier =
      Modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
        .padding(horizontal = 16.dp, vertical = 12.dp),
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.bodyLarge,
      color = TextPrimaryNight,
      fontWeight = FontWeight.Normal,
    )
    Text(
      text = subtitle,
      style = MaterialTheme.typography.bodySmall,
      color = TextMutedNight,
      fontSize = 11.sp,
    )
  }
}
