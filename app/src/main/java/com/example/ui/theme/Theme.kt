package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val NocturnalDarkColorScheme = darkColorScheme(
  primary = SleepAmber,
  onPrimary = NightObsidian,
  primaryContainer = SleepAmberDim,
  onPrimaryContainer = SleepAmberBright,
  secondary = CalmIndigo,
  onSecondary = NightObsidian,
  secondaryContainer = NightSurfaceElevated,
  onSecondaryContainer = TextPrimaryNight,
  tertiary = CalmCyan,
  background = NightObsidian,
  onBackground = TextPrimaryNight,
  surface = NightSurface,
  onSurface = TextPrimaryNight,
  surfaceVariant = NightSurfaceElevated,
  onSurfaceVariant = TextSecondaryNight,
  outline = NightOutline,
  error = AlertCoral,
  errorContainer = AlertCoralSurface,
  onErrorContainer = AlertCoral,
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit,
) {
  // Always use the eye-friendly nocturnal Dark Mode scheme designed for nighttime use
  MaterialTheme(
    colorScheme = NocturnalDarkColorScheme,
    typography = Typography,
    content = content,
  )
}
