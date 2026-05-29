package com.example.modelviewer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Sci-Fi space dark aesthetic colors
val NeonCyan = Color(0xFF00F0FF)
val NeonPurple = Color(0xFFBD00FF)
val ElectricBlue = Color(0xFF0051FF)
val DeepBlackSpace = Color(0xFF05030A)
val GlowGreen = Color(0xFF39FF14)
val DangerRed = Color(0xFFFF073A)

// Semi-transparent overlay surface tokens
val GlassColor = Color(0x33100B26)
val GlassBorderSelected = Color(0xAA00F0FF)
val GlassBorderNormal = Color(0x44FFFFFF)

private val SpaceColorScheme = darkColorScheme(
    primary = NeonCyan,
    secondary = NeonPurple,
    tertiary = ElectricBlue,
    background = DeepBlackSpace,
    surface = GlassColor,
    error = DangerRed,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = Color(0xFFEDE8FC),
    onSurface = Color(0xFFEDE8FC)
)

@Composable
fun Space3DTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SpaceColorScheme,
        content = content
    )
}
