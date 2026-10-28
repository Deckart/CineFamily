package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val CinemaColorScheme = darkColorScheme(
  primary = CinemaGold,
  onPrimary = CinemaDarkBackground,
  primaryContainer = CinemaGoldDark,
  onPrimaryContainer = CinemaTextPrimary,
  secondary = VelvetRuby,
  onSecondary = CinemaTextPrimary,
  secondaryContainer = VelvetRubyDark,
  onSecondaryContainer = CinemaTextPrimary,
  tertiary = ReelCyan,
  onTertiary = CinemaDarkBackground,
  background = CinemaDarkBackground,
  onBackground = CinemaTextPrimary,
  surface = CinemaDarkSurface,
  onSurface = CinemaTextPrimary,
  surfaceVariant = CinemaDarkSurfaceVariant,
  onSurfaceVariant = CinemaTextSecondary,
  outline = CinemaDarkBorder,
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Default to rich cinema dark theme
  dynamicColor: Boolean = false, // Keep signature cinema branding
  content: @Composable () -> Unit,
) {
  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window
      if (window != null) {
        window.statusBarColor = CinemaDarkBackground.toArgb()
        window.navigationBarColor = CinemaDarkBackground.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
      }
    }
  }

  MaterialTheme(
    colorScheme = CinemaColorScheme,
    typography = Typography,
    content = content
  )
}
