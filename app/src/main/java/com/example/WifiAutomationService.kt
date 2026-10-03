package com.example

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.lang.ref.WeakReference

class WifiAutomationService : AccessibilityService() {

  override fun onServiceConnected() {
    super.onServiceConnected()
    instance = WeakReference(this)
  }

  override fun onDestroy() {
    if (instance?.get() == this) {
      instance = null
    }
    super.onDestroy()
  }

  override fun onInterrupt() {}

  override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    if (!pendingAutoTurnOff) return
    val now = System.currentTimeMillis()
    if (now - turnOffRequestTime > 8000L) {
      pendingAutoTurnOff = false
      return
    }

    val root = rootInActiveWindow ?: return
    try {
      val turnedOff = findAndToggleWifiSwitch(root)
      if (turnedOff) {
        pendingAutoTurnOff = false
        // Dismiss the panel automatically
        Handler(Looper.getMainLooper()).postDelayed({
          try {
            performGlobalAction(GLOBAL_ACTION_BACK)
          } catch (_: Exception) {}
        }, 250L)
      }
    } catch (_: Exception) {}
  }

  private fun findAndToggleWifiSwitch(node: AccessibilityNodeInfo): Boolean {
    // 1. If this node is a Switch and is currently ON (checked)
    val className = node.className?.toString() ?: ""
    if (node.isCheckable || className.contains("Switch", ignoreCase = true)) {
      if (node.isChecked) {
        val clicked = node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        if (clicked) return true
        // If the switch itself isn't directly clickable, try its clickable parent
        var parent = node.parent
        while (parent != null) {
          if (parent.isClickable) {
            val parentClicked = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            if (parentClicked) return true
          }
          parent = parent.parent
        }
      }
    }

    // 2. Recursively check children
    val count = node.childCount
    for (i in 0 until count) {
      val child = node.getChild(i) ?: continue
      if (findAndToggleWifiSwitch(child)) {
        return true
      }
    }
    return false
  }

  companion object {
    private var instance: WeakReference<WifiAutomationService>? = null
    private var pendingAutoTurnOff: Boolean = false
    private var turnOffRequestTime: Long = 0L

    fun isServiceActive(): Boolean = instance?.get() != null

    /**
     * Checks if this accessibility service is enabled in Android System Settings
     */
    fun isAccessibilityPermissionGranted(context: Context): Boolean {
      try {
        val expectedServiceName = "${context.packageName}/${WifiAutomationService::class.java.canonicalName}"
        val enabledServices =
          Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
          ) ?: return false
        return enabledServices.contains(context.packageName) &&
          enabledServices.contains(WifiAutomationService::class.java.simpleName)
      } catch (_: Exception) {
        return false
      }
    }

    /**
     * Opens Android System Accessibility Settings for the user to toggle this service ON
     */
    fun openAccessibilitySettings(context: Context) {
      try {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
      } catch (_: Exception) {}
    }

    /**
     * Executes automatic Wi-Fi shut-off via multiple robust methods
     */
    fun executeAutoTurnOff(context: Context) {
      val appContext = context.applicationContext
      val wifiManager = appContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager

      // 1. Immediately disconnect network to stop traffic
      try {
        wifiManager?.disconnect()
      } catch (_: Exception) {}

      // 2. Direct setWifiEnabled(false) (allowed because targetSdk is 28)
      try {
        @Suppress("DEPRECATION")
        wifiManager?.setWifiEnabled(false)
      } catch (_: Exception) {}

      // 3. Reflection invocation of setWifiEnabled
      try {
        if (wifiManager != null) {
          val method = wifiManager.javaClass.getMethod("setWifiEnabled", java.lang.Boolean.TYPE)
          method.isAccessible = true
          method.invoke(wifiManager, false)
        }
      } catch (_: Exception) {}

      // 4. Shell commands (svc wifi disable & cmd wifi)
      try {
        Runtime.getRuntime().exec(arrayOf("svc", "wifi", "disable"))
      } catch (_: Exception) {}
      try {
        Runtime.getRuntime().exec(arrayOf("cmd", "wifi", "set-wifi-enabled", "disabled"))
      } catch (_: Exception) {}

      // 5. Global Settings
      try {
        Settings.Global.putInt(appContext.contentResolver, "wifi_on", 0)
      } catch (_: Exception) {}

      // If Wi-Fi is successfully off, stop here!
      if (wifiManager != null && !wifiManager.isWifiEnabled) {
        return
      }

      // 6. Accessibility Automation fallback (if enabled)
      if (isServiceActive() || isAccessibilityPermissionGranted(appContext)) {
        pendingAutoTurnOff = true
        turnOffRequestTime = System.currentTimeMillis()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          val panelIntent =
            Intent(Settings.Panel.ACTION_WIFI).apply {
              addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
          try {
            appContext.startActivity(panelIntent)
          } catch (_: Exception) {
            val fallbackIntent =
              Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
              }
            try {
              appContext.startActivity(fallbackIntent)
            } catch (_: Exception) {}
          }
        }
      }
    }
  }
}
