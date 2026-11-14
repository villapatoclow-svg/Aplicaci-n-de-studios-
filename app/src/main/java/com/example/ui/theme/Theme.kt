package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val MenteProShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun MenteProTheme(
    accentKey: String = "indigo",
    seasonId: String = "otono",
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val isXmas = seasonId == "navidad"

    val colorScheme = when {
        isXmas && darkTheme -> darkColorScheme(
            primary = XmasCrimsonBright,
            onPrimary = Color(0xFF2B0707),
            primaryContainer = Color(0xFF5C1111),
            onPrimaryContainer = Color(0xFFFEE2E2),
            secondary = XmasPineBright,
            onSecondary = Color(0xFF052E16),
            secondaryContainer = Color(0xFF14532D),
            onSecondaryContainer = Color(0xFFDCFCE7),
            tertiary = XmasGold,
            background = XmasNightDarkBg,
            onBackground = Color(0xFFF8FAFC),
            surface = XmasNightDarkSurface,
            onSurface = Color(0xFFF8FAFC),
            surfaceVariant = XmasNightDarkVariant,
            onSurfaceVariant = Color(0xFFCBD5E1),
            error = StatusDanger
        )
        isXmas -> lightColorScheme(
            primary = XmasCrimson,
            onPrimary = Color.White,
            primaryContainer = XmasSnowLightVariant,
            onPrimaryContainer = Color(0xFF450A0A),
            secondary = XmasPine,
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFDCFCE7),
            onSecondaryContainer = Color(0xFF14532D),
            tertiary = XmasGold,
            background = XmasSnowLightBg,
            onBackground = LightOnSurface,
            surface = XmasSnowLightSurface,
            onSurface = LightOnSurface,
            surfaceVariant = Color(0xFFF3F7F4),
            onSurfaceVariant = LightOnSurfaceVariant,
            error = StatusDanger
        )
        darkTheme -> {
            val darkPrimary = when (accentKey) {
                "emerald" -> Color(0xFF34D399)
                "amber" -> Color(0xFFFBBF24)
                "obsidian" -> Color(0xFF94A3B8)
                else -> when (seasonId) {
                    "lluvia" -> Color(0xFF38BDF8)
                    "soleado" -> Color(0xFFFACC15)
                    else -> Color(0xFF818CF8)
                }
            }
            darkColorScheme(
                primary = darkPrimary,
                onPrimary = Color(0xFF0F172A),
                primaryContainer = Color(0xFF1E1B4B),
                onPrimaryContainer = Color(0xFFE0E7FF),
                secondary = Color(0xFF2DD4BF),
                onSecondary = Color(0xFF042F2E),
                secondaryContainer = Color(0xFF134E4A),
                onSecondaryContainer = Color(0xFFCCFBF1),
                tertiary = IndigoTertiary,
                background = DarkBackground,
                onBackground = DarkOnSurface,
                surface = DarkSurface,
                onSurface = DarkOnSurface,
                surfaceVariant = DarkSurfaceVariant,
                onSurfaceVariant = DarkOnSurfaceVariant,
                error = StatusDanger
            )
        }
        else -> {
            val (primary, containerLight, secondary) = when (accentKey) {
                "emerald" -> Triple(EmeraldPrimary, EmeraldContainer, IndigoPrimary)
                "amber" -> Triple(AmberPrimary, AmberContainer, IndigoSecondary)
                "obsidian" -> Triple(ObsidianPrimary, ObsidianContainer, IndigoSecondary)
                else -> when (seasonId) {
                    "lluvia" -> Triple(RainStormPrimary, Color(0xFFE0F2FE), RainStormSecondary)
                    "soleado" -> Triple(SunnySolarGold, Color(0xFFFEF9C3), IndigoPrimary)
                    else -> Triple(IndigoPrimary, IndigoPrimaryContainer, IndigoSecondary)
                }
            }
            lightColorScheme(
                primary = primary,
                onPrimary = Color.White,
                primaryContainer = containerLight,
                onPrimaryContainer = Color(0xFF0F172A),
                secondary = secondary,
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFCCFBF1),
                onSecondaryContainer = Color(0xFF134E4A),
                tertiary = if (seasonId == "otono") AutumnCempasuchil else IndigoTertiary,
                background = LightBackground,
                onBackground = LightOnSurface,
                surface = LightSurface,
                onSurface = LightOnSurface,
                surfaceVariant = LightSurfaceVariant,
                onSurfaceVariant = LightOnSurfaceVariant,
                error = StatusDanger
            )
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MenteProTypography,
        shapes = MenteProShapes,
        content = content
    )
}
