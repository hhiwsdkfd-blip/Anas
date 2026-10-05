package com.example

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@Composable
fun AppSettingsScreen(
  str: (Int) -> String,
  onBackClick: () -> Unit,
  onToggleLanguage: () -> Unit,
  onOpenAboutClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  BackHandler { onBackClick() }

  val context = LocalContext.current
  val prefs = remember(context) {
    context.getSharedPreferences("app_settings_prefs", Context.MODE_PRIVATE)
  }

  var appThemeOption by remember {
    mutableStateOf(prefs.getString("app_theme_mode", "follow_system") ?: "follow_system")
  }
  var saveResultsEnabled by remember {
    mutableStateOf(prefs.getBoolean("diag_save_results", true))
  }
  var disableTouchFeedback by remember {
    mutableStateOf(prefs.getBoolean("disable_haptics", false))
  }
  var selectedServer by remember {
    mutableStateOf(prefs.getString("selected_diag_server", "Google Edge CDN") ?: "Google Edge CDN")
  }

  var showThemeDialog by remember { mutableStateOf(false) }
  var showServerDialog by remember { mutableStateOf(false) }

  Column(
    modifier =
      modifier
        .fillMaxSize()
        .background(NightObsidian)
        .verticalScroll(rememberScrollState())
        .padding(bottom = 24.dp),
  ) {
    // Top Bar with Back Arrow matching Screenshot 3
    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(horizontal = 8.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      IconButton(onClick = onBackClick) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Back",
          tint = TextPrimaryNight,
        )
      }
      Text(
        text = str(R.string.settings_title),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = TextPrimaryNight,
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Section 1: المظهر (Appearance)
    SettingsSectionTitle(title = str(R.string.settings_section_appearance))

    SettingsItemRow(
      title = str(R.string.settings_app_theme_title),
      subtitle = when (appThemeOption) {
        "dark" -> str(R.string.settings_theme_dark)
        "light" -> str(R.string.settings_theme_light)
        else -> str(R.string.settings_theme_follow_system)
      },
      icon = Icons.Filled.DarkMode,
      iconTint = Color.White,
      onClick = { showThemeDialog = true },
    )

    Spacer(modifier = Modifier.height(20.dp))

    // Section 2: التفضيلات (Preferences)
    SettingsSectionTitle(title = str(R.string.settings_section_preferences))

    SettingsItemRow(
      title = str(R.string.settings_language_title),
      subtitle = str(R.string.settings_language_desc),
      icon = Icons.Filled.Language,
      iconTint = Color(0xFFEF5350), // Red globe icon from Screenshot 3
      onClick = onToggleLanguage,
    )

    SettingsItemRow(
      title = str(R.string.settings_network_diag_title),
      subtitle = "$selectedServer (${str(R.string.settings_server_switch_desc)})",
      icon = Icons.Filled.Speed,
      iconTint = Color(0xFF00B0FF), // Blue speed icon from Screenshot 3
      onClick = { showServerDialog = true },
    )

    SettingsToggleRow(
      title = str(R.string.settings_save_results_title),
      subtitle = str(R.string.settings_save_results_desc),
      icon = Icons.Filled.History,
      iconTint = Color(0xFFFFB300), // Amber clock icon from Screenshot 3
      checked = saveResultsEnabled,
      onCheckedChange = { checked ->
        saveResultsEnabled = checked
        prefs.edit().putBoolean("diag_save_results", checked).apply()
      },
    )

    SettingsToggleRow(
      title = str(R.string.settings_disable_touch_title),
      subtitle = str(R.string.settings_disable_touch_desc),
      icon = Icons.Filled.Vibration,
      iconTint = Color(0xFF66BB6A), // Green vibration icon from Screenshot 3
      checked = disableTouchFeedback,
      onCheckedChange = { checked ->
        disableTouchFeedback = checked
        prefs.edit().putBoolean("disable_haptics", checked).apply()
      },
    )

    Spacer(modifier = Modifier.height(20.dp))

    // Section 3: عام (General)
    SettingsSectionTitle(title = str(R.string.setup_section_general))

    SettingsItemRow(
      title = str(R.string.settings_about_title),
      subtitle = str(R.string.settings_about_desc),
      icon = Icons.Filled.Info,
      iconTint = Color(0xFF42A5F5), // Blue info icon from Screenshot 3
      onClick = onOpenAboutClick,
    )
  }

  // Theme dialog
  if (showThemeDialog) {
    val themes = listOf(
      "follow_system" to str(R.string.settings_theme_follow_system),
      "dark" to str(R.string.settings_theme_dark),
      "light" to str(R.string.settings_theme_light),
    )
    AlertDialog(
      onDismissRequest = { showThemeDialog = false },
      containerColor = NightSurfaceElevated,
      title = {
        Text(text = str(R.string.settings_app_theme_title), color = TextPrimaryNight, fontWeight = FontWeight.Bold)
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          themes.forEach { (key, label) ->
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (appThemeOption == key) SleepAmberDim else NightSurface,
              border = BorderStroke(1.dp, if (appThemeOption == key) SleepAmber else NightOutline),
              modifier =
                Modifier
                  .fillMaxWidth()
                  .clickable {
                    appThemeOption = key
                    prefs.edit().putString("app_theme_mode", key).apply()
                    showThemeDialog = false
                    Toast.makeText(context, label, Toast.LENGTH_SHORT).show()
                  },
            ) {
              Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Text(
                  text = label,
                  color = if (appThemeOption == key) SleepAmberBright else TextPrimaryNight,
                  fontWeight = FontWeight.Bold,
                )
                if (appThemeOption == key) {
                  Icon(Icons.Filled.Check, contentDescription = null, tint = SleepAmber)
                }
              }
            }
          }
        }
      },
      confirmButton = {},
      dismissButton = {
        TextButton(onClick = { showThemeDialog = false }) {
          Text(text = str(R.string.custom_cancel_button), color = TextSecondaryNight)
        }
      },
    )
  }

  // Server switch dialog
  if (showServerDialog) {
    val servers = listOf("Google Edge CDN", "Cloudflare Anycast", "Fastly Global", "Local ISP Gateway")
    AlertDialog(
      onDismissRequest = { showServerDialog = false },
      containerColor = NightSurfaceElevated,
      title = {
        Text(text = str(R.string.settings_network_diag_title), color = TextPrimaryNight, fontWeight = FontWeight.Bold)
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          servers.forEach { srv ->
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (selectedServer == srv) SleepAmberDim else NightSurface,
              border = BorderStroke(1.dp, if (selectedServer == srv) SleepAmber else NightOutline),
              modifier =
                Modifier
                  .fillMaxWidth()
                  .clickable {
                    selectedServer = srv
                    prefs.edit().putString("selected_diag_server", srv).apply()
                    showServerDialog = false
                    Toast.makeText(context, srv, Toast.LENGTH_SHORT).show()
                  },
            ) {
              Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Text(
                  text = srv,
                  color = if (selectedServer == srv) SleepAmberBright else TextPrimaryNight,
                  fontWeight = FontWeight.Bold,
                )
                if (selectedServer == srv) {
                  Icon(Icons.Filled.Check, contentDescription = null, tint = SleepAmber)
                }
              }
            }
          }
        }
      },
      confirmButton = {},
      dismissButton = {
        TextButton(onClick = { showServerDialog = false }) {
          Text(text = str(R.string.custom_cancel_button), color = TextSecondaryNight)
        }
      },
    )
  }
}

@Composable
private fun SettingsSectionTitle(title: String) {
  Text(
    text = title,
    style = MaterialTheme.typography.labelMedium,
    color = Color(0xFF64B5F6), // Blue header color from Screenshot 3
    fontWeight = FontWeight.Bold,
    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
  )
}

@Composable
private fun SettingsItemRow(
  title: String,
  subtitle: String,
  icon: ImageVector,
  iconTint: Color,
  onClick: () -> Unit,
) {
  Row(
    modifier =
      Modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
        .padding(horizontal = 16.dp, vertical = 14.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(16.dp),
      modifier = Modifier.weight(1f),
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = iconTint,
        modifier = Modifier.size(24.dp),
      )
      Column {
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

    Icon(
      imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
      contentDescription = null,
      tint = TextMutedNight,
      modifier = Modifier.size(16.dp),
    )
  }
}

@Composable
private fun SettingsToggleRow(
  title: String,
  subtitle: String,
  icon: ImageVector,
  iconTint: Color,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
) {
  Row(
    modifier =
      Modifier
        .fillMaxWidth()
        .clickable { onCheckedChange(!checked) }
        .padding(horizontal = 16.dp, vertical = 14.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(16.dp),
      modifier = Modifier.weight(1f),
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = iconTint,
        modifier = Modifier.size(24.dp),
      )
      Column {
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

    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      colors =
        SwitchDefaults.colors(
          checkedThumbColor = Color.White,
          checkedTrackColor = Color(0xFF1E88E5), // Bright blue switch from Screenshot 3
          uncheckedThumbColor = TextMutedNight,
          uncheckedTrackColor = NightSurface,
        ),
    )
  }
}
