package com.nunoapps.cartelli.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PamRed = Color(0xFFE3031B)
private val PamRedDark = Color(0xFFB00216)

private val LightColors = lightColorScheme(
    primary = PamRed,
    onPrimary = Color.White,
    secondary = PamRedDark,
    onSecondary = Color.White,
)

@Composable
fun CartelliTheme(
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // Manteniamo lo schema chiaro/rosso PAM anche in dark per coerenza col cartello.
    MaterialTheme(colorScheme = LightColors, content = content)
}
