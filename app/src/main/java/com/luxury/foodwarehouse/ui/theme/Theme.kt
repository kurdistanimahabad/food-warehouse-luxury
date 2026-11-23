package com.luxury.foodwarehouse.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val LuxuryDarkScheme = darkColorScheme(
    primary = ChampagneGold,
    onPrimary = ObsidianBackground,
    primaryContainer = RichBronze,
    onPrimaryContainer = SoftGold,
    secondary = EmeraldHealthy,
    onSecondary = ObsidianBackground,
    background = ObsidianBackground,
    onBackground = TextPrimaryLuxury,
    surface = LuxurySurface,
    onSurface = TextPrimaryLuxury,
    surfaceVariant = LuxurySurfaceElevated,
    onSurfaceVariant = TextSecondaryLuxury,
    error = CrimsonDanger,
    outline = BorderGoldSubtle
)

@Composable
fun FoodWarehouseLuxuryTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LuxuryDarkScheme,
        content = content
    )
}
