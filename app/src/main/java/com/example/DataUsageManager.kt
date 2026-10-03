package com.example

import android.app.AppOpsManager
import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.TrafficStats
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class DataPeriod {
  TODAY,
  LAST_7_DAYS,
  THIS_MONTH
}

data class AppUsageInfo(
  val uid: Int,
  val packageName: String,
  val appName: String,
  val wifiRxBytes: Long,
  val wifiTxBytes: Long,
  val mobileRxBytes: Long,
  val mobileTxBytes: Long,
  val appIcon: Drawable? = null,
) {
  val wifiTotalBytes: Long get() = (wifiRxBytes + wifiTxBytes).coerceAtLeast(0L)
  val mobileTotalBytes: Long get() = (mobileRxBytes + mobileTxBytes).coerceAtLeast(0L)
  val totalBytes: Long get() = (wifiTotalBytes + mobileTotalBytes).coerceAtLeast(0L)
}

data class NetworkDataSummary(
  val totalWifiRx: Long,
  val totalWifiTx: Long,
  val totalMobileRx: Long,
  val totalMobileTx: Long,
  val appList: List<AppUsageInfo>,
  val hasPermission: Boolean,
) {
  val totalWifiBytes: Long get() = (totalWifiRx + totalWifiTx).coerceAtLeast(0L)
  val totalMobileBytes: Long get() = (totalMobileRx + totalMobileTx).coerceAtLeast(0L)
  val grandTotalBytes: Long get() = (totalWifiBytes + totalMobileBytes).coerceAtLeast(0L)
}

object DataUsageManager {

  fun hasUsageStatsPermission(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
    val mode =
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        appOps.unsafeCheckOpNoThrow(
          AppOpsManager.OPSTR_GET_USAGE_STATS,
          Process.myUid(),
          context.packageName,
        )
      } else {
        @Suppress("DEPRECATION")
        appOps.checkOpNoThrow(
          AppOpsManager.OPSTR_GET_USAGE_STATS,
          Process.myUid(),
          context.packageName,
        )
      }
    return mode == AppOpsManager.MODE_ALLOWED
  }

  fun openUsageAccessSettings(context: Context) {
    try {
      val intent =
        Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
          data = Uri.parse("package:${context.packageName}")
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
      context.startActivity(intent)
    } catch (_: Exception) {
      try {
        val fallbackIntent =
          Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          }
        context.startActivity(fallbackIntent)
      } catch (_: Exception) {}
    }
  }

  fun openAppDetailsSettings(context: Context, packageName: String) {
    try {
      val intent =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
          data = Uri.parse("package:$packageName")
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
      context.startActivity(intent)
    } catch (_: Exception) {}
  }

  suspend fun fetchNetworkStats(
    context: Context,
    period: DataPeriod,
  ): NetworkDataSummary = withContext(Dispatchers.IO) {
    val hasPermission = hasUsageStatsPermission(context)
    val (startTime, endTime) = calculateTimeRange(period)

    if (hasPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      fetchStatsViaNetworkStatsManager(context, startTime, endTime)
    } else {
      fetchStatsViaTrafficStatsFallback(context)
    }
  }

  private fun calculateTimeRange(period: DataPeriod): Pair<Long, Long> {
    val now = System.currentTimeMillis()
    val calendar = Calendar.getInstance()

    val startTime =
      when (period) {
        DataPeriod.TODAY -> {
          calendar.set(Calendar.HOUR_OF_DAY, 0)
          calendar.set(Calendar.MINUTE, 0)
          calendar.set(Calendar.SECOND, 0)
          calendar.set(Calendar.MILLISECOND, 0)
          calendar.timeInMillis
        }
        DataPeriod.LAST_7_DAYS -> {
          now - (7L * 24L * 60L * 60L * 1000L)
        }
        DataPeriod.THIS_MONTH -> {
          calendar.set(Calendar.DAY_OF_MONTH, 1)
          calendar.set(Calendar.HOUR_OF_DAY, 0)
          calendar.set(Calendar.MINUTE, 0)
          calendar.set(Calendar.SECOND, 0)
          calendar.set(Calendar.MILLISECOND, 0)
          calendar.timeInMillis
        }
      }

    return Pair(startTime, now)
  }

  private fun fetchStatsViaNetworkStatsManager(
    context: Context,
    startTime: Long,
    endTime: Long,
  ): NetworkDataSummary {
    val networkStatsManager =
      context.getSystemService(Context.NETWORK_STATS_SERVICE) as? NetworkStatsManager
        ?: return fetchStatsViaTrafficStatsFallback(context)

    var totalWifiRx = 0L
    var totalWifiTx = 0L
    var totalMobileRx = 0L
    var totalMobileTx = 0L

    class UidStats {
      var wifiRx = 0L
      var wifiTx = 0L
      var mobileRx = 0L
      var mobileTx = 0L
    }
    val perUidMap = mutableMapOf<Int, UidStats>()

    // Query Device Wi-Fi Summary
    try {
      val wifiBucket =
        networkStatsManager.querySummaryForDevice(
          ConnectivityManager.TYPE_WIFI,
          null,
          startTime,
          endTime,
        )
      totalWifiRx = wifiBucket.rxBytes
      totalWifiTx = wifiBucket.txBytes
    } catch (_: Exception) {}

    // Query Device Mobile Data Summary
    try {
      val mobileBucket =
        networkStatsManager.querySummaryForDevice(
          ConnectivityManager.TYPE_MOBILE,
          null,
          startTime,
          endTime,
        )
      totalMobileRx = mobileBucket.rxBytes
      totalMobileTx = mobileBucket.txBytes
    } catch (_: Exception) {}

    // Query Per-App Wi-Fi usage
    var queriedWifi = false
    try {
      val wifiStats =
        networkStatsManager.querySummary(
          ConnectivityManager.TYPE_WIFI,
          null,
          startTime,
          endTime,
        )
      val bucket = NetworkStats.Bucket()
      while (wifiStats.hasNextBucket()) {
        wifiStats.getNextBucket(bucket)
        val uid = bucket.uid
        if (uid > 0) {
          val stats = perUidMap.getOrPut(uid) { UidStats() }
          stats.wifiRx += bucket.rxBytes
          stats.wifiTx += bucket.txBytes
          queriedWifi = true
        }
      }
      wifiStats.close()
    } catch (_: Exception) {}

    if (!queriedWifi) {
      try {
        val wifiStats =
          networkStatsManager.queryDetails(
            ConnectivityManager.TYPE_WIFI,
            null,
            startTime,
            endTime,
          )
        val bucket = NetworkStats.Bucket()
        while (wifiStats.hasNextBucket()) {
          wifiStats.getNextBucket(bucket)
          val uid = bucket.uid
          if (uid > 0) {
            val stats = perUidMap.getOrPut(uid) { UidStats() }
            stats.wifiRx += bucket.rxBytes
            stats.wifiTx += bucket.txBytes
          }
        }
        wifiStats.close()
      } catch (_: Exception) {}
    }

    // Query Per-App Mobile usage
    var queriedMobile = false
    try {
      val mobileStats =
        networkStatsManager.querySummary(
          ConnectivityManager.TYPE_MOBILE,
          null,
          startTime,
          endTime,
        )
      val bucket = NetworkStats.Bucket()
      while (mobileStats.hasNextBucket()) {
        mobileStats.getNextBucket(bucket)
        val uid = bucket.uid
        if (uid > 0) {
          val stats = perUidMap.getOrPut(uid) { UidStats() }
          stats.mobileRx += bucket.rxBytes
          stats.mobileTx += bucket.txBytes
          queriedMobile = true
        }
      }
      mobileStats.close()
    } catch (_: Exception) {}

    if (!queriedMobile) {
      try {
        val mobileStats =
          networkStatsManager.queryDetails(
            ConnectivityManager.TYPE_MOBILE,
            null,
            startTime,
            endTime,
          )
        val bucket = NetworkStats.Bucket()
        while (mobileStats.hasNextBucket()) {
          mobileStats.getNextBucket(bucket)
          val uid = bucket.uid
          if (uid > 0) {
            val stats = perUidMap.getOrPut(uid) { UidStats() }
            stats.mobileRx += bucket.rxBytes
            stats.mobileTx += bucket.txBytes
          }
        }
        mobileStats.close()
      } catch (_: Exception) {}
    }

    // Resolve app package names, titles, and icons
    val pm = context.packageManager
    val appList = mutableListOf<AppUsageInfo>()

    for ((uid, stats) in perUidMap) {
      val total = stats.wifiRx + stats.wifiTx + stats.mobileRx + stats.mobileTx
      if (total <= 512L) continue // Filter negligible background heartbeat

      val packages = pm.getPackagesForUid(uid)
      val primaryPkg = packages?.firstOrNull() ?: "UID: $uid"
      var label = primaryPkg
      var icon: Drawable? = null

      if (!packages.isNullOrEmpty()) {
        try {
          val appInfo = pm.getApplicationInfo(primaryPkg, 0)
          label = pm.getApplicationLabel(appInfo).toString()
          icon = pm.getApplicationIcon(appInfo)
        } catch (_: Exception) {
          label = primaryPkg
        }
      }

      appList.add(
        AppUsageInfo(
          uid = uid,
          packageName = primaryPkg,
          appName = label,
          wifiRxBytes = stats.wifiRx,
          wifiTxBytes = stats.wifiTx,
          mobileRxBytes = stats.mobileRx,
          mobileTxBytes = stats.mobileTx,
          appIcon = icon,
        ),
      )
    }

    // Sort apps by highest total consumption descending
    appList.sortByDescending { it.totalBytes }

    // If querySummaryForDevice returned 0 but perUid sum is positive, aggregate perUid sum
    val sumUidWifiRx = appList.sumOf { it.wifiRxBytes }
    val sumUidWifiTx = appList.sumOf { it.wifiTxBytes }
    val sumUidMobRx = appList.sumOf { it.mobileRxBytes }
    val sumUidMobTx = appList.sumOf { it.mobileTxBytes }

    val finalWifiRx = maxOf(totalWifiRx, sumUidWifiRx)
    val finalWifiTx = maxOf(totalWifiTx, sumUidWifiTx)
    val finalMobileRx = maxOf(totalMobileRx, sumUidMobRx)
    val finalMobileTx = maxOf(totalMobileTx, sumUidMobTx)

    return NetworkDataSummary(
      totalWifiRx = finalWifiRx,
      totalWifiTx = finalWifiTx,
      totalMobileRx = finalMobileRx,
      totalMobileTx = finalMobileTx,
      appList = appList,
      hasPermission = true,
    )
  }

  /**
   * Fallback using TrafficStats when PACKAGE_USAGE_STATS permission is not granted yet.
   * Reports current boot session stats.
   */
  @Suppress("DEPRECATION")
  private fun fetchStatsViaTrafficStatsFallback(context: Context): NetworkDataSummary {
    val totalRx = TrafficStats.getTotalRxBytes().coerceAtLeast(0L)
    val totalTx = TrafficStats.getTotalTxBytes().coerceAtLeast(0L)
    val mobileRx = TrafficStats.getMobileRxBytes().coerceAtLeast(0L)
    val mobileTx = TrafficStats.getMobileTxBytes().coerceAtLeast(0L)

    val wifiRx = (totalRx - mobileRx).coerceAtLeast(0L)
    val wifiTx = (totalTx - mobileTx).coerceAtLeast(0L)

    val pm = context.packageManager
    val installedApps =
      try {
        pm.getInstalledApplications(PackageManager.GET_META_DATA)
      } catch (_: Exception) {
        emptyList<ApplicationInfo>()
      }

    val appList = mutableListOf<AppUsageInfo>()
    val seenUids = mutableSetOf<Int>()

    for (app in installedApps) {
      if (!seenUids.add(app.uid)) continue
      val uidRx = TrafficStats.getUidRxBytes(app.uid).coerceAtLeast(0L)
      val uidTx = TrafficStats.getUidTxBytes(app.uid).coerceAtLeast(0L)
      if (uidRx + uidTx <= 512L) continue

      val label = pm.getApplicationLabel(app).toString()
      val icon =
        try {
          pm.getApplicationIcon(app)
        } catch (_: Exception) {
          null
        }

      appList.add(
        AppUsageInfo(
          uid = app.uid,
          packageName = app.packageName,
          appName = label,
          wifiRxBytes = uidRx,
          wifiTxBytes = uidTx,
          mobileRxBytes = 0L,
          mobileTxBytes = 0L,
          appIcon = icon,
        ),
      )
    }

    appList.sortByDescending { it.totalBytes }

    return NetworkDataSummary(
      totalWifiRx = wifiRx,
      totalWifiTx = wifiTx,
      totalMobileRx = mobileRx,
      totalMobileTx = mobileTx,
      appList = appList,
      hasPermission = false,
    )
  }

  fun formatBytes(bytes: Long): String {
    if (bytes <= 0L) return "0 B"
    val b = bytes.toDouble()
    val kb = b / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0

    return when {
      gb >= 1.0 -> String.format(Locale.US, "%.2f GB", gb)
      mb >= 1.0 -> String.format(Locale.US, "%.1f MB", mb)
      kb >= 1.0 -> String.format(Locale.US, "%.0f KB", kb)
      else -> String.format(Locale.US, "%d B", bytes)
    }
  }
}
