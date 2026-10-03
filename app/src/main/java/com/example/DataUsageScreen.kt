package com.example

import android.graphics.drawable.Drawable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
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
import kotlinx.coroutines.launch

enum class AppSortOption {
  ALL,
  WIFI_ONLY,
  MOBILE_ONLY
}

@Composable
fun DataUsageScreen(
  str: (Int) -> String,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val focusManager = LocalFocusManager.current

  var selectedPeriod by remember { mutableStateOf(DataPeriod.TODAY) }
  var sortOption by remember { mutableStateOf(AppSortOption.ALL) }
  var searchQuery by remember { mutableStateOf("") }
  var selectedAppForDetails by remember { mutableStateOf<AppUsageInfo?>(null) }

  var summary by remember {
    mutableStateOf(
      NetworkDataSummary(
        totalWifiRx = 0L,
        totalWifiTx = 0L,
        totalMobileRx = 0L,
        totalMobileTx = 0L,
        appList = emptyList(),
        hasPermission = DataUsageManager.hasUsageStatsPermission(context),
      ),
    )
  }
  var isLoading by remember { mutableStateOf(true) }

  val loadStats: () -> Unit = {
    coroutineScope.launch {
      isLoading = true
      val result = DataUsageManager.fetchNetworkStats(context, selectedPeriod)
      summary = result
      isLoading = false
    }
  }

  // Load stats initially and whenever selected period changes
  LaunchedEffect(selectedPeriod) {
    loadStats()
  }

  // Filtered and sorted apps
  val filteredApps =
    remember(summary.appList, searchQuery, sortOption) {
      var list = summary.appList
      if (searchQuery.isNotBlank()) {
        val q = searchQuery.trim().lowercase()
        list = list.filter { it.appName.lowercase().contains(q) || it.packageName.lowercase().contains(q) }
      }
      when (sortOption) {
        AppSortOption.ALL -> list.sortedByDescending { it.totalBytes }
        AppSortOption.WIFI_ONLY -> list.sortedByDescending { it.wifiTotalBytes }
        AppSortOption.MOBILE_ONLY -> list.sortedByDescending { it.mobileTotalBytes }
      }
    }

  Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.TopCenter,
  ) {
    LazyColumn(
      modifier =
        Modifier.fillMaxSize()
          .widthIn(max = 500.dp)
          .padding(horizontal = 20.dp),
      contentPadding = PaddingValues(vertical = 16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // Header Section
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = str(R.string.data_usage_title),
              style = MaterialTheme.typography.headlineSmall,
              color = TextPrimaryNight,
              fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = str(R.string.data_usage_subtitle),
              style = MaterialTheme.typography.bodyMedium,
              color = TextSecondaryNight,
            )
          }

          IconButton(
            onClick = loadStats,
            enabled = !isLoading,
            modifier = Modifier.testTag("refresh_data_usage_button"),
          ) {
            if (isLoading) {
              CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = SleepAmber,
                strokeWidth = 2.dp,
              )
            } else {
              Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = str(R.string.data_refresh_button),
                tint = SleepAmber,
              )
            }
          }
        }
      }

      // Time Period Filter Chips
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          DataPeriod.values().forEach { period ->
            val labelRes =
              when (period) {
                DataPeriod.TODAY -> R.string.data_period_today
                DataPeriod.LAST_7_DAYS -> R.string.data_period_week
                DataPeriod.THIS_MONTH -> R.string.data_period_month
              }
            val isSelected = selectedPeriod == period
            FilterChip(
              selected = isSelected,
              onClick = { selectedPeriod = period },
              label = {
                Text(
                  text = str(labelRes),
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                )
              },
              leadingIcon =
                if (isSelected) {
                  {
                    Icon(
                      imageVector = Icons.Filled.Check,
                      contentDescription = null,
                      modifier = Modifier.size(16.dp),
                      tint = SleepAmberBright,
                    )
                  }
                } else null,
              colors =
                FilterChipDefaults.filterChipColors(
                  containerColor = NightSurface,
                  selectedContainerColor = SleepAmberDim,
                  labelColor = TextSecondaryNight,
                  selectedLabelColor = SleepAmberBright,
                ),
              border =
                FilterChipDefaults.filterChipBorder(
                  borderColor = NightOutline,
                  selectedBorderColor = SleepAmber,
                  borderWidth = 1.dp,
                  selectedBorderWidth = 1.dp,
                  enabled = true,
                  selected = isSelected,
                ),
              shape = RoundedCornerShape(50),
              modifier = Modifier.testTag("period_chip_${period.name.lowercase()}"),
            )
          }
        }
      }

      // Permission Request Banner (if PACKAGE_USAGE_STATS not granted)
      if (!summary.hasPermission) {
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = NightSurfaceElevated),
            border = BorderStroke(1.dp, SleepAmber.copy(alpha = 0.5f)),
          ) {
            Column(
              modifier = Modifier.fillMaxWidth().padding(16.dp),
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                Surface(
                  shape = CircleShape,
                  color = SleepAmberDim,
                  modifier = Modifier.size(36.dp),
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Icon(
                      imageVector = Icons.Filled.Security,
                      contentDescription = null,
                      tint = SleepAmber,
                      modifier = Modifier.size(20.dp),
                    )
                  }
                }
                Text(
                  text = str(R.string.data_permission_title),
                  style = MaterialTheme.typography.titleMedium,
                  color = SleepAmberBright,
                  fontWeight = FontWeight.Bold,
                )
              }
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = str(R.string.data_permission_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondaryNight,
              )
              Spacer(modifier = Modifier.height(12.dp))
              Button(
                onClick = { DataUsageManager.openUsageAccessSettings(context) },
                modifier =
                  Modifier.fillMaxWidth()
                    .height(44.dp)
                    .testTag("grant_usage_permission_button"),
                shape = RoundedCornerShape(12.dp),
                colors =
                  ButtonDefaults.buttonColors(
                    containerColor = SleepAmber,
                    contentColor = NightObsidian,
                  ),
              ) {
                Text(
                  text = str(R.string.data_permission_button),
                  fontWeight = FontWeight.Bold,
                )
              }
            }
          }
        }
      }

      // Summary Cards: Wi-Fi & Mobile Data
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          // Wi-Fi Summary Card
          DataCategoryCard(
            title = str(R.string.data_wifi_card_title),
            totalFormatted = DataUsageManager.formatBytes(summary.totalWifiBytes),
            rxFormatted = DataUsageManager.formatBytes(summary.totalWifiRx),
            txFormatted = DataUsageManager.formatBytes(summary.totalWifiTx),
            icon = Icons.Filled.Wifi,
            accentColor = SleepAmber,
            str = str,
            modifier = Modifier.weight(1f).testTag("wifi_data_summary_card"),
          )

          // Mobile Data Summary Card
          DataCategoryCard(
            title = str(R.string.data_mobile_card_title),
            totalFormatted = DataUsageManager.formatBytes(summary.totalMobileBytes),
            rxFormatted = DataUsageManager.formatBytes(summary.totalMobileRx),
            txFormatted = DataUsageManager.formatBytes(summary.totalMobileTx),
            icon = Icons.Filled.SignalCellularAlt,
            accentColor = WifiActiveGreen,
            str = str,
            modifier = Modifier.weight(1f).testTag("mobile_data_summary_card"),
          )
        }
      }

      // Grand Total Card with proportional split bar
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = NightSurfaceElevated),
          border = BorderStroke(1.dp, NightOutline),
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = str(R.string.data_total_label),
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondaryNight,
              )
              Text(
                text = DataUsageManager.formatBytes(summary.grandTotalBytes),
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimaryNight,
                fontWeight = FontWeight.Bold,
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Proportional split bar
            val wifiFraction =
              if (summary.grandTotalBytes > 0L) {
                (summary.totalWifiBytes.toFloat() / summary.grandTotalBytes.toFloat()).coerceIn(0f, 1f)
              } else 0.5f

            LinearProgressIndicator(
              progress = { wifiFraction },
              modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
              color = SleepAmber,
              trackColor = WifiActiveGreen,
              strokeCap = StrokeCap.Round,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier.size(8.dp).clip(CircleShape).background(SleepAmber),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "${str(R.string.data_wifi_only)}: ${DataUsageManager.formatBytes(summary.totalWifiBytes)}",
                  style = MaterialTheme.typography.bodySmall,
                  color = SleepAmberBright,
                )
              }
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier.size(8.dp).clip(CircleShape).background(WifiActiveGreen),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "${str(R.string.data_mobile_only)}: ${DataUsageManager.formatBytes(summary.totalMobileBytes)}",
                  style = MaterialTheme.typography.bodySmall,
                  color = WifiActiveGreen,
                )
              }
            }
          }
        }
      }

      // App Breakdown Heading + Search & Sort Controls
      item {
        Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Icon(
                imageVector = Icons.Filled.Apps,
                contentDescription = null,
                tint = SleepAmber,
                modifier = Modifier.size(20.dp),
              )
              Text(
                text = "${str(R.string.data_apps_heading)} (${filteredApps.size})",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimaryNight,
                fontWeight = FontWeight.Bold,
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Search Field
          OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth().testTag("app_search_field"),
            placeholder = {
              Text(
                text = str(R.string.search_apps_placeholder),
                color = TextMutedNight,
                style = MaterialTheme.typography.bodyMedium,
              )
            },
            leadingIcon = {
              Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = TextSecondaryNight,
                modifier = Modifier.size(20.dp),
              )
            },
            trailingIcon = {
              if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { searchQuery = "" }) {
                  Icon(
                    imageVector = Icons.Filled.Clear,
                    contentDescription = "Clear",
                    tint = TextSecondaryNight,
                    modifier = Modifier.size(18.dp),
                  )
                }
              }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors =
              OutlinedTextFieldDefaults.colors(
                focusedContainerColor = NightSurface,
                unfocusedContainerColor = NightSurface,
                focusedBorderColor = SleepAmber,
                unfocusedBorderColor = NightOutline,
                focusedTextColor = TextPrimaryNight,
                unfocusedTextColor = TextPrimaryNight,
              ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Sort Chips Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(
              imageVector = Icons.Filled.Sort,
              contentDescription = null,
              tint = TextSecondaryNight,
              modifier = Modifier.size(16.dp),
            )

            AppSortOption.values().forEach { option ->
              val labelRes =
                when (option) {
                  AppSortOption.ALL -> R.string.sort_by_highest
                  AppSortOption.WIFI_ONLY -> R.string.sort_by_wifi
                  AppSortOption.MOBILE_ONLY -> R.string.sort_by_mobile
                }
              val isSelected = sortOption == option
              FilterChip(
                selected = isSelected,
                onClick = { sortOption = option },
                label = {
                  Text(
                    text = str(labelRes),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  )
                },
                colors =
                  FilterChipDefaults.filterChipColors(
                    containerColor = NightSurface,
                    selectedContainerColor = SleepAmberDim,
                    labelColor = TextSecondaryNight,
                    selectedLabelColor = SleepAmberBright,
                  ),
                border =
                  FilterChipDefaults.filterChipBorder(
                    borderColor = NightOutline,
                    selectedBorderColor = SleepAmber,
                    borderWidth = 1.dp,
                    selectedBorderWidth = 1.dp,
                    enabled = true,
                    selected = isSelected,
                  ),
                shape = RoundedCornerShape(50),
              )
            }
          }
        }
      }

      // App List or Empty Placeholder
      if (filteredApps.isEmpty()) {
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = NightSurface),
            border = BorderStroke(1.dp, NightOutline),
          ) {
            Box(
              modifier = Modifier.fillMaxWidth().padding(24.dp),
              contentAlignment = Alignment.Center,
            ) {
              Text(
                text = str(R.string.data_no_apps_found),
                style = MaterialTheme.typography.bodyMedium,
                color = TextMutedNight,
              )
            }
          }
        }
      } else {
        val maxUsage =
          when (sortOption) {
            AppSortOption.ALL -> filteredApps.firstOrNull()?.totalBytes?.coerceAtLeast(1L) ?: 1L
            AppSortOption.WIFI_ONLY -> filteredApps.firstOrNull()?.wifiTotalBytes?.coerceAtLeast(1L) ?: 1L
            AppSortOption.MOBILE_ONLY -> filteredApps.firstOrNull()?.mobileTotalBytes?.coerceAtLeast(1L) ?: 1L
          }
        val grandTotal = summary.grandTotalBytes.coerceAtLeast(1L)

        items(filteredApps, key = { it.packageName + it.uid }) { app ->
          AppUsageRow(
            app = app,
            maxUsage = maxUsage,
            grandTotal = grandTotal,
            sortOption = sortOption,
            str = str,
            onClick = { selectedAppForDetails = app },
          )
        }
      }
    }

    // App Detail Modal Dialog
    if (selectedAppForDetails != null) {
      val app = selectedAppForDetails!!
      AppDetailsDialog(
        app = app,
        grandTotal = summary.grandTotalBytes,
        str = str,
        onDismiss = { selectedAppForDetails = null },
        onOpenAppInfo = {
          DataUsageManager.openAppDetailsSettings(context, app.packageName)
        },
      )
    }
  }
}

@Composable
private fun AppDetailsDialog(
  app: AppUsageInfo,
  grandTotal: Long,
  str: (Int) -> String,
  onDismiss: () -> Unit,
  onOpenAppInfo: () -> Unit,
) {
  val percent =
    if (grandTotal > 0L) {
      (app.totalBytes.toDouble() / grandTotal.toDouble() * 100.0)
    } else 0.0

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = NightSurfaceElevated,
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        AppIconView(icon = app.appIcon, label = app.appName)
        Column {
          Text(
            text = app.appName,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimaryNight,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            text = app.packageName,
            style = MaterialTheme.typography.bodySmall,
            color = TextMutedNight,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        // Total highlight
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = NightSurface,
          border = BorderStroke(1.dp, NightOutline),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Text(
              text = str(R.string.data_total_label),
              style = MaterialTheme.typography.labelMedium,
              color = TextSecondaryNight,
            )
            Text(
              text = DataUsageManager.formatBytes(app.totalBytes),
              style = MaterialTheme.typography.headlineMedium,
              color = SleepAmberBright,
              fontWeight = FontWeight.Bold,
            )
            Text(
              text = String.format(java.util.Locale.US, "%.1f%% %s", percent, str(R.string.percent_of_total)),
              style = MaterialTheme.typography.bodySmall,
              color = TextSecondaryNight,
            )
          }
        }

        // Wi-Fi Breakdown Box
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = NightSurface,
          border = BorderStroke(1.dp, SleepAmber.copy(alpha = 0.4f)),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Icon(Icons.Filled.Wifi, contentDescription = null, tint = SleepAmber, modifier = Modifier.size(16.dp))
              Text(
                text = str(R.string.data_wifi_card_title),
                style = MaterialTheme.typography.titleSmall,
                color = SleepAmberBright,
                fontWeight = FontWeight.Bold,
              )
              Spacer(modifier = Modifier.weight(1f))
              Text(
                text = DataUsageManager.formatBytes(app.wifiTotalBytes),
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimaryNight,
                fontWeight = FontWeight.Bold,
              )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text(
                text = "↓ ${str(R.string.download_label)}: ${DataUsageManager.formatBytes(app.wifiRxBytes)}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryNight,
              )
              Text(
                text = "↑ ${str(R.string.upload_label)}: ${DataUsageManager.formatBytes(app.wifiTxBytes)}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryNight,
              )
            }
          }
        }

        // Mobile Data Breakdown Box
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = NightSurface,
          border = BorderStroke(1.dp, WifiActiveGreen.copy(alpha = 0.4f)),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Icon(Icons.Filled.SignalCellularAlt, contentDescription = null, tint = WifiActiveGreen, modifier = Modifier.size(16.dp))
              Text(
                text = str(R.string.data_mobile_card_title),
                style = MaterialTheme.typography.titleSmall,
                color = WifiActiveGreen,
                fontWeight = FontWeight.Bold,
              )
              Spacer(modifier = Modifier.weight(1f))
              Text(
                text = DataUsageManager.formatBytes(app.mobileTotalBytes),
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimaryNight,
                fontWeight = FontWeight.Bold,
              )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text(
                text = "↓ ${str(R.string.download_label)}: ${DataUsageManager.formatBytes(app.mobileRxBytes)}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryNight,
              )
              Text(
                text = "↑ ${str(R.string.upload_label)}: ${DataUsageManager.formatBytes(app.mobileTxBytes)}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryNight,
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onOpenAppInfo,
        colors =
          ButtonDefaults.buttonColors(
            containerColor = SleepAmber,
            contentColor = NightObsidian,
          ),
      ) {
        Icon(Icons.Filled.ArrowOutward, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = str(R.string.button_open_app_info), fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(text = str(R.string.close_button), color = TextSecondaryNight)
      }
    },
  )
}

@Composable
private fun DataCategoryCard(
  title: String,
  totalFormatted: String,
  rxFormatted: String,
  txFormatted: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  accentColor: Color,
  str: (Int) -> String,
  modifier: Modifier = Modifier,
) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = NightSurfaceElevated),
    border = BorderStroke(1.dp, NightOutline),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(14.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Surface(
          shape = CircleShape,
          color = accentColor.copy(alpha = 0.15f),
          modifier = Modifier.size(32.dp),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = icon,
              contentDescription = null,
              tint = accentColor,
              modifier = Modifier.size(18.dp),
            )
          }
        }
        Text(
          text = title,
          style = MaterialTheme.typography.labelMedium,
          color = TextSecondaryNight,
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = totalFormatted,
        style = MaterialTheme.typography.headlineSmall,
        color = TextPrimaryNight,
        fontWeight = FontWeight.Bold,
      )

      Spacer(modifier = Modifier.height(6.dp))

      // Down / Up Details
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Filled.ArrowDownward,
            contentDescription = null,
            tint = TextMutedNight,
            modifier = Modifier.size(12.dp),
          )
          Spacer(modifier = Modifier.width(2.dp))
          Text(
            text = rxFormatted,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondaryNight,
          )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Filled.ArrowUpward,
            contentDescription = null,
            tint = TextMutedNight,
            modifier = Modifier.size(12.dp),
          )
          Spacer(modifier = Modifier.width(2.dp))
          Text(
            text = txFormatted,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondaryNight,
          )
        }
      }
    }
  }
}

@Composable
private fun AppUsageRow(
  app: AppUsageInfo,
  maxUsage: Long,
  grandTotal: Long,
  sortOption: AppSortOption,
  str: (Int) -> String,
  onClick: () -> Unit,
) {
  val targetUsage =
    when (sortOption) {
      AppSortOption.ALL -> app.totalBytes
      AppSortOption.WIFI_ONLY -> app.wifiTotalBytes
      AppSortOption.MOBILE_ONLY -> app.mobileTotalBytes
    }
  val fraction = (targetUsage.toFloat() / maxUsage.toFloat()).coerceIn(0.02f, 1f)
  val percentOfTotal =
    if (grandTotal > 0L) {
      (app.totalBytes.toDouble() / grandTotal.toDouble() * 100.0)
    } else 0.0

  Card(
    modifier =
      Modifier.fillMaxWidth()
        .clickable(onClick = onClick)
        .testTag("app_usage_row_${app.packageName}"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = NightSurface),
    border = BorderStroke(1.dp, NightOutline),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(14.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          AppIconView(icon = app.appIcon, label = app.appName)
          Spacer(modifier = Modifier.width(12.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = app.appName,
              style = MaterialTheme.typography.bodyLarge,
              color = TextPrimaryNight,
              fontWeight = FontWeight.SemiBold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            Text(
              text = String.format(java.util.Locale.US, "%.1f%% %s", percentOfTotal, str(R.string.percent_of_total)),
              style = MaterialTheme.typography.bodySmall,
              color = TextMutedNight,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
          text = DataUsageManager.formatBytes(targetUsage),
          style = MaterialTheme.typography.titleMedium,
          color =
            when (sortOption) {
              AppSortOption.ALL -> SleepAmberBright
              AppSortOption.WIFI_ONLY -> SleepAmber
              AppSortOption.MOBILE_ONLY -> WifiActiveGreen
            },
          fontWeight = FontWeight.Bold,
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Consumption Progress Bar
      LinearProgressIndicator(
        progress = { fraction },
        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
        color =
          when (sortOption) {
            AppSortOption.ALL -> SleepAmber
            AppSortOption.WIFI_ONLY -> SleepAmber
            AppSortOption.MOBILE_ONLY -> WifiActiveGreen
          },
        trackColor = NightSurfaceElevated,
        strokeCap = StrokeCap.Round,
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Breakdown chips (Wi-Fi vs Mobile)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(SleepAmber))
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "${str(R.string.data_wifi_only)}: ${DataUsageManager.formatBytes(app.wifiTotalBytes)}",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondaryNight,
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(WifiActiveGreen))
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "${str(R.string.data_mobile_only)}: ${DataUsageManager.formatBytes(app.mobileTotalBytes)}",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondaryNight,
          )
        }
      }
    }
  }
}

@Composable
private fun AppIconView(icon: Drawable?, label: String) {
  if (icon != null) {
    val bitmap =
      remember(icon) {
        try {
          icon.toBitmap(width = 80, height = 80).asImageBitmap()
        } catch (_: Exception) {
          null
        }
      }
    if (bitmap != null) {
      Image(
        bitmap = bitmap,
        contentDescription = label,
        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)),
      )
      return
    }
  }

  // Fallback initial badge
  Surface(
    shape = RoundedCornerShape(10.dp),
    color = NightSurfaceElevated,
    border = BorderStroke(1.dp, NightOutline),
    modifier = Modifier.size(40.dp),
  ) {
    Box(contentAlignment = Alignment.Center) {
      Text(
        text = label.firstOrNull()?.uppercase() ?: "A",
        style = MaterialTheme.typography.titleMedium,
        color = SleepAmberBright,
        fontWeight = FontWeight.Bold,
      )
    }
  }
}
